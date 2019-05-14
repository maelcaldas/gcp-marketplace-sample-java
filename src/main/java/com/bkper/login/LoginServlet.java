/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bkper.login;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.bkper.user.BkperUserService;
import com.bkper.user.GoogleUser;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;

public class LoginServlet extends HttpServlet {

    private static final String X_GCP_MARKETPLACE_TOKEN_PARAM = "x-gcp-marketplace-token";

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Inject
    private JsonFactory jsonFactory;
    
    @Inject
    private JsonParser jsonParser;

    @Inject
    private HttpTransport transport;
    
    @Inject
    private HttpRequestFactory requestFactory;
    
    @Inject
    private BkperUserService bkperUserService;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String gcpJwtToken = getGcpJwtToken(request);
            
            String googleIdTokenString = getGoogleIdToken(request);

            String gcpAccountId = verifyGcpAccountIdToken(gcpJwtToken);

            if (googleIdTokenString != null) {
                // User logged in with success. Verify googleIdToken
                GoogleIdToken idToken = verifyGoogleIdToken(googleIdTokenString);
                
                //Build Google together with the gcp Account id 
                GoogleUser googleUser = buildGoogleUser(idToken, gcpAccountId);
                
                bkperUserService.addOrUpdateUser(googleUser);
            } else if (gcpAccountId != null) {
                
                // Post from GCP Marketplace
                LOGGER.info("Hanlding login for GCP Account: " + gcpAccountId);
                
                //Here you redirect user in the popup window when registering through the GCP Marketplace
                //You should keep the JWT token in the auth flow state to be verified after user performs the login flow on screen
                //In case of JS login, you can encode the token in the URL to send it back later 
                resp.sendRedirect("https://your.login.url.com?"+X_GCP_MARKETPLACE_TOKEN_PARAM +"="+gcpJwtToken);
            }

        } catch (GeneralSecurityException | JWTVerificationException e) {
            LOGGER.log(Level.SEVERE, "Could not perform login.", e);
            resp.setStatus(401);
        }

    }

    private String getGcpJwtToken(HttpServletRequest request) {
        String gcpJwtToken = request.getHeader(X_GCP_MARKETPLACE_TOKEN_PARAM);
        if (gcpJwtToken == null) {
            gcpJwtToken = request.getParameter(X_GCP_MARKETPLACE_TOKEN_PARAM);
        }
        return gcpJwtToken;
    }

    private String verifyGcpAccountIdToken(String gcpJwtToken) throws IOException, CertificateException {
        if (gcpJwtToken == null) {
            return null;
        }
        
        DecodedJWT jwt = JWT.decode(gcpJwtToken);
        
        //Fetches certificate from issuer URL 
        GenericUrl issuerUrl = new GenericUrl(jwt.getIssuer());
        HttpRequest request = requestFactory.buildGetRequest(issuerUrl);
        String certsJson = request.execute().parseAsString();
        
        //Lookup certificate by kid
        JsonElement jsonRoot = jsonParser.parse(certsJson);
        String certificateStr = jsonRoot.getAsJsonObject().get(jwt.getKeyId()).getAsString();
        
        //Get public key from certificate
        InputStream is = new ByteArrayInputStream(certificateStr.getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(is);
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
        
        //Build JWT Verifier
        Algorithm algorithm = Algorithm.RSA256(publicKey, null);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("https://www.googleapis.com/robot/v1/metadata/x509/cloud-commerce-partner@system.gserviceaccount.com")
                .withAudience("your.domain.com")
                .build();
        
        //Verify
        jwt = verifier.verify(gcpJwtToken);
        
        return jwt.getSubject();
    }


    private GoogleIdToken verifyGoogleIdToken(String idTokenString)
            throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Lists.newArrayList("YOUR_CLIENT_ID"))
                // Or, if multiple clients access the backend:
                // .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new GeneralSecurityException("Invalid ID token: " + idTokenString);
        }
        return idToken;
    }

    //Gets Google Id Token from normal login flow
    private String getGoogleIdToken(HttpServletRequest request) {
        return request.getHeader("token");
    }

    private GoogleUser buildGoogleUser(GoogleIdToken idToken, String gcpAccountId) {
        GoogleUser googleUser = new GoogleUser();
        
        googleUser.setGcpAccountId(gcpAccountId);
        
        Payload payload = idToken.getPayload();

        String userId = payload.getSubject();
        String email = payload.getEmail();
        String hd = payload.getHostedDomain();
        
        googleUser.setId(userId);
        googleUser.setEmail(email);
        googleUser.setHd(hd);
        
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String locale = (String) payload.get("locale");
        String familyName = (String) payload.get("family_name");
        String givenName = (String) payload.get("given_name"); 
        
        googleUser.setName(name);
        googleUser.setPicture(pictureUrl);
        googleUser.setLocale(locale);
        googleUser.setFamilyName(familyName);
        googleUser.setGivenName(givenName);

        return googleUser;
    }
    

}

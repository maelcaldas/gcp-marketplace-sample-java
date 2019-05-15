package com.bkper.billing.google.pubsub;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bkper.billing.google.processors.GCPEventProcessor;
import com.bkper.billing.google.processors.GCPEventProcessorFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;

public class GCPPubSubServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(GCPPubSubServlet.class.getName());
    
    private final String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");

    @Inject
    private GCPEventProcessorFactory eventProcessorFactory;
    
    @Inject
    private Gson gson;
    
    @Inject
    protected JsonFactory jsonFactory;
    
    @Inject
    private JsonParser jsonParser;

    @Inject
    protected HttpTransport transport;
    
    
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        
        try {
            // Do not process message if request token does not match
            // pubsubVerificationToken
            if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
                throw new GeneralSecurityException("Error verifying Pub/Sub token");
            }
            
            // Get the Cloud Pub/Sub-generated JWT in the "Authorization" header.
            String authorizationHeader = req.getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()
                    || authorizationHeader.split(" ").length != 2) {
                throw new GeneralSecurityException("Error verifying Pub/Sub authorization");
            }
            String authorization = authorizationHeader.split(" ")[1];
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList("gcpmarketplace.bkper.com")).build();
            
            verifier.verify(authorization);

        } catch (GeneralSecurityException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
      
      try {
          
          PubsubMessage message = getMessage(req);
          
          LOGGER.info("GCP Pubsub message received: " + message.getData());
          
          GCPPubsubMessageData messageData = getMessageData(message);
          
          GCPEventProcessor gcpPubsubEventProcessor = eventProcessorFactory.get(messageData.getEventType());
          gcpPubsubEventProcessor.processMessage(messageData);
          
        // 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
        resp.setStatus(200);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error processing GCP Pubsub message", e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }

    private PubsubMessage getMessage(HttpServletRequest request) throws IOException {
      String requestBody = request.getReader().lines().collect(Collectors.joining("\n"));
      JsonElement jsonRoot = jsonParser.parse(requestBody);
      String messageStr = jsonRoot.getAsJsonObject().get("message").toString();
      PubsubMessage message = gson.fromJson(messageStr, PubsubMessage.class);
      // decode from base64
      String decoded = decode(message.getData());
      message.setData(decoded);
      return message;
    }
    
    
    private GCPPubsubMessageData getMessageData(PubsubMessage message) {
        return gson.fromJson(message.getData(), GCPPubsubMessageData.class);
    }

    private String decode(String data) {
      return new String(Base64.getDecoder().decode(data));
    }    


}

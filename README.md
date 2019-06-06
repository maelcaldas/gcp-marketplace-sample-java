# GCP Marketplace Java sample integration

This sample is complimentary to the [official integration documentation](https://cloud.google.com/marketplace/docs/partners/integrated-saas/technical-integration-setup) with focus on Java running on App Engine (Although it can be on any infrastructure)

Follow bellow some explanations and snippets of the key parts.

You can browse [com.bkper.billing.google](https://github.com/bkper/gcp-marketplace-sample-java/tree/master/src/main/java/com/bkper/billing/google) package for more details.


## Setup Partner Procurement API

The partner procurement API was generated with the [google-client-api-generator](https://github.com/google/apis-client-generator) and the code is published in this [github repo](https://github.com/bkper/cloudcommerceprocurement).

- Add the jar to your project.
- [Grant Service Account Token Creator role](https://medium.com/google-cloud/using-serviceaccountactor-iam-role-for-account-impersonation-on-google-cloud-platform-a9e7118480ed) to the App Engine default service account ([project-id]@appspot.serviceaccount.com), on the xxx-public service account you shared with Google.
- Build the Partner procurement service with the impersonated credentials:

```java
    @Provides
    public CloudCommercePartnerProcurementService getGCPPartnerProcurementService(HttpTransport transport,
            JsonFactory jsonFactory) throws IOException {
        return new CloudCommercePartnerProcurementService.Builder(transport, jsonFactory, getRequestInitializer())
                .setApplicationName("Your Application").build();
    }

    /**
     * 
     * Build a request initializer for using in the Procurement API as your service
     * account, by impersonating it.
     * 
     */
    private static HttpRequestInitializer getRequestInitializer() throws IOException {
        ImpersonatedCredentials targetCredentials = ImpersonatedCredentials.create(
                GoogleCredentials.getApplicationDefault(), GCP_MARKETPLACE_SERVICE_ACCOUNT, null,
                Lists.newArrayList(CloudCommercePartnerProcurementServiceScopes.all()), 300);
        return new HttpCredentialsAdapter(targetCredentials);
    }
```

References:

https://cloud.google.com/iam/docs/service-accounts#service_account_permissions
https://cloud.google.com/iam/docs/understanding-service-accounts#impersonating_a_service_account


## Verify JWT

For JWT verification you can use Auth0 [java-jwt](https://github.com/auth0/java-jwt)

```java
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
```

See more details about the auth flow at the [LoginServlet](https://github.com/bkper/gcp-marketplace-sample-java/blob/master/src/main/java/com/bkper/login/LoginServlet.java)


### Integrate Pub/Sub

- [Activate the service account](https://cloud.google.com/sdk/gcloud/reference/auth/activate-service-account) using gcloud.
- [Verify your domain](https://console.cloud.google.com/apis/credentials/domainverification) in the [xxx-public] project.
- [Create a subscription](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine-java8/pubsub) to the topic Google team sent you.
- Create a servlet to handle the authorized pubsub messages:

```java
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
                    .setAudience(Collections.singletonList("subdomain.yourdomain.com")).build();
            
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
```

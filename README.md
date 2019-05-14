# GCP Marketplace Java sample integration

This is sample is complimentary to the official documentation found at

https://cloud.google.com/marketplace/docs/partners/integrated-saas/technical-integration-setup

With focus on Java running on App Engine (Although can be on any infrastructure)

## Setup Partner Procurement API

- Generate the API using [google-client-api-generator](https://github.com/google/apis-client-generator)
- Grant **Service Account Token Creator** role to your infrastructure default service account. On gae is usuall <project-id>@appspot.serviceaccount.com
- Build the Partner procurement service with the impersonated credentials

```
    @Provides
    public CloudCommercePartnerProcurementService getGCPPartnerProcurementService(HttpTransport transport, JsonFactory jsonFactory) throws IOException {
        return new CloudCommercePartnerProcurementService.Builder(transport, jsonFactory, getRequestInitializer()).setApplicationName("bkper").build();
    }
    
    /**
     * 
     * Build a request initializer for using in the Procurement API as your service account, by impersonating it.
     * 
     */
    private static HttpRequestInitializer getRequestInitializer() throws IOException {
        ImpersonatedCredentials targetCredentials = ImpersonatedCredentials.create(GoogleCredentials.getApplicationDefault(),
                GCP_MARKETPLACE_SERVICE_ACCOUNT, null,
                Lists.newArrayList(CloudCommercePartnerProcurementServiceScopes.all()), 300);
        return new HttpCredentialsAdapter(targetCredentials);
    }
```

References:

https://cloud.google.com/iam/docs/service-accounts#service_account_permissions
https://cloud.google.com/iam/docs/understanding-service-accounts#impersonating_a_service_account


## Verify JWT Token

For JWT token verification you can use [Auth0 java-jwt](https://github.com/auth0/java-jwt)

```
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
                .withAudience("app.bkper.com")
                .build();
        
        //Verify
        jwt = verifier.verify(gcpJwtToken);
        
        return jwt.getSubject();
    }
```


### Integrate Pub/Sub

- [Activate a service account](https://cloud.google.com/sdk/gcloud/reference/auth/activate-service-account) using gcloud
- [Verify your domain](https://console.cloud.google.com/apis/credentials/domainverification) in the [xxx-public] project of the service account 
- [Create a subscription] (https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine-java8/pubsub) to the topic Google team sent you
- Create a servlet to handle the authorized pubsub messages

```
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
```

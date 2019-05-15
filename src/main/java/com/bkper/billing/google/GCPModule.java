package com.bkper.billing.google;

import java.io.IOException;
import java.util.Objects;

import com.bkper.billing.google.processors.GCPEventProcessor;
import com.bkper.billing.google.processors.GCPEventProcessorAccountCreated;
import com.bkper.billing.google.processors.GCPEventProcessorAccountDeleted;
import com.bkper.billing.google.processors.GCPEventProcessorEntitlementApproved;
import com.bkper.billing.google.processors.GCPEventProcessorEntitlementCanceledOrSuspended;
import com.bkper.billing.google.processors.GCPEventProcessorEntitlementCreationRequested;
import com.bkper.billing.google.processors.GCPEventProcessorEntitlementDeleted;
import com.bkper.billing.google.processors.GCPEventProcessorEntitlementPlanChangeRequested;
import com.bkper.billing.google.processors.GCPEventProcessorEntitlementPlanChanged;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.cloudcommerceprocurement.v1.CloudCommercePartnerProcurementService;
import com.google.cloudcommerceprocurement.v1.CloudCommercePartnerProcurementServiceScopes;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class GCPModule extends AbstractModule {

    private static final String GCP_MARKETPLACE_SERVICE_ACCOUNT = "<your-service-account-name>@<your-project>.iam.gserviceaccount.com";
    private final long hashcode;

    public GCPModule() {
        this.hashcode = longHash("GCP Module");
    }

    @Override
    protected void configure() {
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ACCOUNT_CREATED.name())).to(GCPEventProcessorAccountCreated.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ACCOUNT_DELETED.name())).to(GCPEventProcessorAccountDeleted.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_CREATION_REQUESTED.name())).to(GCPEventProcessorEntitlementCreationRequested.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_ACTIVATION_REQUESTED.name())).to(GCPEventProcessorEntitlementCreationRequested.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_PLAN_CHANGE_REQUESTED.name())).to(GCPEventProcessorEntitlementPlanChangeRequested.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_PLAN_CHANGED.name())).to(GCPEventProcessorEntitlementPlanChanged.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_PENDING_CANCELLATION.name())).to(GCPEventProcessorEntitlementCanceledOrSuspended.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_ACTIVE.name())).to(GCPEventProcessorEntitlementApproved.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_CANCELLED.name())).to(GCPEventProcessorEntitlementCanceledOrSuspended.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_SUSPENDED.name())).to(GCPEventProcessorEntitlementCanceledOrSuspended.class);
        bind(GCPEventProcessor.class).annotatedWith(Names.named(GCPEventType.ENTITLEMENT_DELETED.name())).to(GCPEventProcessorEntitlementDeleted.class);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GCPModule)) return false;
        GCPModule that = (GCPModule) o;
        return hashcode == that.hashcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashcode);
    }
    
    private static long longHash(String string) {
        long h = 98764321261L;
        int l = string.length();
        char[] chars = string.toCharArray();

        for (int i = 0; i < l; i++) {
            h = 31 * h + chars[i];
        }
        return h;
    }
}

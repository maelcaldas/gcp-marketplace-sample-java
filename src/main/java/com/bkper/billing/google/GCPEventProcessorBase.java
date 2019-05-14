package com.bkper.billing.google;

import java.io.IOException;

import com.bkper.billing.BillingService;
import com.bkper.user.BkperUserService;
import com.google.cloudcommerceprocurement.v1.CloudCommercePartnerProcurementService;
import com.google.cloudcommerceprocurement.v1.model.Account;
import com.google.cloudcommerceprocurement.v1.model.Entitlement;
import com.google.inject.Inject;

public abstract class GCPEventProcessorBase implements GCPEventProcessor {

    public static String PARTNER_ID = "xxxx-public"; // 
    public static String PROVIDER_PATH = "providers/" + PARTNER_ID;

    @Inject
    protected GCPAccountRepository gpaAccountRepository;

    @Inject
    protected BillingService billingService;

    @Inject
    protected CloudCommercePartnerProcurementService procurementService;

    @Inject
    protected BkperUserService registeredUserService;

    @Inject
    protected GCPAccountService gcpAccountService;

    protected Entitlement getEntitlement(String entitlementId) throws IOException {
        String name = PROVIDER_PATH + "/entitlements/" + entitlementId;
        Entitlement entitlement = procurementService.providers().entitlements().get(name).execute();
        return entitlement;
    }

    protected String getAccountId(Account account) {
        if (account == null) {
            return null;
        }
        String name = account.getName();
        String[] namePaths = name.split("/");
        return namePaths[namePaths.length - 1];
    }

}

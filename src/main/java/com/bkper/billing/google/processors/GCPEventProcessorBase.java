package com.bkper.billing.google.processors;

import java.io.IOException;

import com.bkper.billing.BillingService;
import com.bkper.billing.google.GCPAccountRepository;
import com.bkper.billing.google.GCPAccountService;
import com.bkper.user.BkperUserService;
import com.google.cloudcommerceprocurement.v1.CloudCommercePartnerProcurementService;
import com.google.cloudcommerceprocurement.v1.model.Account;
import com.google.cloudcommerceprocurement.v1.model.Entitlement;
import com.google.inject.Inject;

public abstract class GCPEventProcessorBase implements GCPEventProcessor {


    @Inject
    protected GCPAccountRepository gcpAccountRepository;

    @Inject
    protected BillingService billingService;

    @Inject
    protected CloudCommercePartnerProcurementService procurementService;

    @Inject
    protected BkperUserService registeredUserService;

    @Inject
    protected GCPAccountService gcpAccountService;

    protected Entitlement getEntitlement(String entitlementId) throws IOException {
        return gcpAccountService.getEntitlement(entitlementId);
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

package com.bkper.billing.google.processors;

import java.io.IOException;

import com.bkper.billing.google.GCPAccount;
import com.bkper.billing.google.pubsub.GCPPubsubMessageData;
import com.bkper.objectify.Transact;
import com.google.cloudcommerceprocurement.v1.model.Account;
import com.google.cloudcommerceprocurement.v1.model.Entitlement;
import com.googlecode.objectify.TxnType;

public class GCPEventProcessorEntitlementCreationRequested extends GCPEventProcessorBase {
    

    @Override
    @Transact(TxnType.REQUIRED)
    public void processMessage(GCPPubsubMessageData message) throws IOException {
        String entitlementId = message.getEntitlement().getId();
        Entitlement entitlement = getEntitlement(entitlementId);
        Account account = procurementService.providers().accounts().get(entitlement.getAccount()).execute();
        String gcpAccountId = getAccountId(account);
        GCPAccount gcpAccount = gcpAccountRepository.loadById(gcpAccountId);
        if (gcpAccount.getUserId() != null) {
            gcpAccountService.approveEntitlement(entitlementId);
        }
    }

}

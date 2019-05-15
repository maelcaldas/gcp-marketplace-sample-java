package com.bkper.billing.google.processors;

import com.bkper.billing.google.GCPAccount;
import com.bkper.billing.google.pubsub.GCPPubsubMessageData;
import com.bkper.objectify.Transact;
import com.googlecode.objectify.TxnType;

public class GCPEventProcessorAccountCreated extends GCPEventProcessorBase {

    @Override
    @Transact(TxnType.REQUIRED)
    public void processMessage(GCPPubsubMessageData message) {
        String gcpAccountId = message.getAccount().getId();
        GCPAccount account = gcpAccountRepository.loadById(gcpAccountId);
        if (account == null) {
            account = new GCPAccount(gcpAccountId);
        }
        gcpAccountRepository.persist(account);
    }

}

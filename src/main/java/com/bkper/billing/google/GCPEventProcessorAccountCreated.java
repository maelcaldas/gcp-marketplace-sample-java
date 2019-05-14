package com.bkper.billing.google;

import com.bkper.billing.google.pubsub.GCPPubsubMessageData;
import com.bkper.objectify.Transact;
import com.googlecode.objectify.TxnType;

public class GCPEventProcessorAccountCreated extends GCPEventProcessorBase {

    @Override
    @Transact(TxnType.REQUIRED)
    public void processMessage(GCPPubsubMessageData message) {
        String gcpAccountId = message.getAccount().getId();
        GCPAccount account = gpaAccountRepository.loadById(gcpAccountId);
        if (account == null) {
            account = new GCPAccount(gcpAccountId);
        }
        gpaAccountRepository.persist(account);
    }

}

package com.bkper.billing.google;

import java.io.IOException;

import com.bkper.billing.google.pubsub.GCPPubsubMessageData;
import com.bkper.objectify.Transact;
import com.bkper.user.BkperUser;
import com.googlecode.objectify.TxnType;

public class GCPEventProcessorAccountDeleted extends GCPEventProcessorBase {

    @Override
    @Transact(TxnType.REQUIRED)
    public void processMessage(GCPPubsubMessageData message) throws IOException {
        String gcpAccountId = message.getAccount().getId();
        GCPAccount gcpAccount = gpaAccountRepository.loadById(gcpAccountId);
        if (gcpAccount.getUserId() != null) {
            BkperUser user = registeredUserService.findById(gcpAccount.getUserId());
            billingService.updateBilling(user, null);
        }
        gpaAccountRepository.delete(gcpAccount);
    }

}

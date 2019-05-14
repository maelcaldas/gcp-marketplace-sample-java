package com.bkper.billing.google;

import java.io.IOException;

import com.bkper.billing.google.pubsub.GCPPubsubMessageData;

public class GCPEventProcessorEntitlementDeleted extends GCPEventProcessorBase {
    
    @Override
    public void processMessage(GCPPubsubMessageData message) throws IOException {
        //DO nothing. Will act on ACCOUNT_DELETED
    }

}

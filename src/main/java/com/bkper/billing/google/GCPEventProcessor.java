package com.bkper.billing.google;

import java.io.IOException;

import com.bkper.billing.google.pubsub.GCPPubsubMessageData;

public interface GCPEventProcessor {

    void processMessage(GCPPubsubMessageData message) throws IOException;

}

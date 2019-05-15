package com.bkper.billing.google.processors;

import java.io.IOException;

import com.bkper.billing.google.pubsub.GCPPubsubMessageData;

public interface GCPEventProcessor {

    void processMessage(GCPPubsubMessageData message) throws IOException;

}

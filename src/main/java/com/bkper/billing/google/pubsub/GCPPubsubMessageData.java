package com.bkper.billing.google.pubsub;

import com.bkper.billing.google.GCPEventType;

public class GCPPubsubMessageData {
    
    private GCPEventType eventType;
    
    private GCPPubsubAccount account;
    
    private GCPPubsubEntitlement entitlement;

    public GCPEventType getEventType() {
        if (eventType == null && account != null) {
            return GCPEventType.ACCOUNT_CREATED;
        }
        return eventType;
    }

    public void setEventType(GCPEventType eventType) {
        this.eventType = eventType;
    }

    public GCPPubsubAccount getAccount() {
        return account;
    }

    public void setAccount(GCPPubsubAccount account) {
        this.account = account;
    }

    public GCPPubsubEntitlement getEntitlement() {
        return entitlement;
    }

    public void setEntitlement(GCPPubsubEntitlement entitlement) {
        this.entitlement = entitlement;
    }

}

package com.bkper.billing.google.pubsub;

import com.bkper.billing.google.GCPPlan;

public class GCPPubsubEntitlement {

    private String id;
    
    private GCPPlan newPlan;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GCPPlan getNewPlan() {
        return newPlan;
    }

    public void setNewPlan(GCPPlan newPlan) {
        this.newPlan = newPlan;
    }
    
    
    
}

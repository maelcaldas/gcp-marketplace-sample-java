package com.bkper.billing.google;


/**
 * This corresponds to the service level SKU's agreed with Google during the onboarding process 
 * 
 * @author maelcaldas
 *
 */
public enum GCPPlan {
    PLAN1,
    PLAN2,
    PLAN3;
    
    public static GCPPlan getByString(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
    
    
}

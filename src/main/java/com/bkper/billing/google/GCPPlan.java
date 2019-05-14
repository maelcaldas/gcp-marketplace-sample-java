package com.bkper.billing.google;


/**
 * This corresponds to the service level SKU's agreed with Google during the onboarding process 
 * 
 * @author maelcaldas
 *
 */
public enum GCPPlan {
    free,
    standard,
    business,
    professional50k,
    professional100k,
    professional300k,
    professional1m,
    professional5m,
    professional50m;
    
    public static GCPPlan getByString(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
    
    
}

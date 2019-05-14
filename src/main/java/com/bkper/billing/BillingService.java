package com.bkper.billing;

import com.bkper.billing.google.GCPAccount;
import com.bkper.billing.google.GCPPlan;
import com.bkper.user.BkperUser;

/**
 * Your Billing service to update internal model
 * 
 * @author maelcaldas
 *
 */
public interface BillingService {

    public void updateBilling(BkperUser user, GCPAccount gcpAccount, GCPPlan gcpPlan);
    
    public void updateBilling(BkperUser user, GCPPlan gcpPlan);


}

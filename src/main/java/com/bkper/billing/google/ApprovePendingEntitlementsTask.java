package com.bkper.billing.google;

import java.io.IOException;
import java.util.List;

import com.bkper.billing.BillingService;
import com.bkper.user.BkperUser;
import com.bkper.user.BkperUserService;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.cloudcommerceprocurement.v1.model.Entitlement;
import com.google.inject.Inject;
/**
 * 
 * This task approves the pending plans upon account approval.
 * 
 * It should not fail and repeat until the plan is approved or rejected.
 * 
 * @author maelcaldas
 *
 */
public class ApprovePendingEntitlementsTask implements DeferredTask {
    
    @Inject
    private transient BillingService billingService;
    
    @Inject
    private transient BkperUserService userService;
    
    @Inject
    private transient GCPAccountService gcpAccountService;
    
    private GCPAccount account;

    public ApprovePendingEntitlementsTask(GCPAccount account) {
        this.account = account;
    }

    @Override
    public void run() {
        try {
            GCPPlan plan = GCPPlan.PLAN1;
            BkperUser user = userService.findById(account.getUserId());
            Entitlement pendingActivationEntitlement = getPendingActivationEntitlement(account);
            if (pendingActivationEntitlement != null) {
                String entitlementId = getEntitlementId(pendingActivationEntitlement);
                gcpAccountService.approveEntitlement(entitlementId);
                plan = GCPPlan.getByString(pendingActivationEntitlement.getPlan());
            }
            billingService.updateBilling(user, account, plan);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    

    private String getEntitlementId(Entitlement entitlement) {
        if (entitlement == null) {
            return null;
        }
        String name = entitlement.getName();
        String[] namePaths = name.split("/");
        return namePaths[namePaths.length - 1];
    }

    private Entitlement getPendingActivationEntitlement(GCPAccount gcpAccoount) throws IOException {
        String filter = String.format("account=%s state=ENTITLEMENT_ACTIVATION_REQUESTED", gcpAccoount.getId());
        List<Entitlement> entitlements = gcpAccountService.listEntitlements(filter);
        if (entitlements != null && entitlements.size() == 1) {
            Entitlement entitlement = entitlements.get(0);
            return entitlement;
        }
        return null;
    }

}

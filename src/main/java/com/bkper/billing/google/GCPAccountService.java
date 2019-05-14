package com.bkper.billing.google;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.bkper.billing.BillingService;
import com.bkper.objectify.Transact;
import com.bkper.user.BkperUser;
import com.google.cloudcommerceprocurement.v1.CloudCommercePartnerProcurementService;
import com.google.cloudcommerceprocurement.v1.model.ApproveAccountRequest;
import com.google.cloudcommerceprocurement.v1.model.ApproveEntitlementRequest;
import com.google.cloudcommerceprocurement.v1.model.Entitlement;
import com.google.cloudcommerceprocurement.v1.model.ListEntitlementsResponse;
import com.google.inject.Inject;
import com.googlecode.objectify.TxnType;

public class GCPAccountService {

    @Inject
    private GCPAccountRepository gpaAccountRepository;

    @Inject
    private BillingService billingService;

    @Inject
    private CloudCommercePartnerProcurementService procurementService;
    
    private static final Logger LOGGER = Logger.getLogger(GCPAccountService.class.getName());

    public void approveEntitlement(String entitlementId) throws IOException {
        LOGGER.info("Approving entitlement: " + entitlementId);
        ApproveEntitlementRequest content = new ApproveEntitlementRequest();
        String name = GCPEventProcessorBase.PROVIDER_PATH + "/entitlements/" + entitlementId;
        procurementService.providers().entitlements().approve(name, content).execute();
    }

    @Transact(TxnType.REQUIRED)
    public GCPAccount approveAccount(BkperUser user, String gcpAccountId) throws IOException {
        GCPAccount account = gpaAccountRepository.loadById(gcpAccountId);
        account.approve(gcpAccountId, user.getId(), user.getEmail());
        gpaAccountRepository.persist(account);
        ApproveAccountRequest content = new ApproveAccountRequest();
        content.setApprovalName("signup");
        String name = GCPEventProcessorBase.PROVIDER_PATH + "/accounts/" + gcpAccountId;
        procurementService.providers().accounts().approve(name, content).execute();
        
        GCPPlan plan = GCPPlan.free;
        
        Entitlement pendingActivationEntitlement = getPendingActivationEntitlement(account);
        if (pendingActivationEntitlement != null) {
            String entitlementId = getEntitlementId(pendingActivationEntitlement);
            approveEntitlement(entitlementId);
            plan = GCPPlan.getByString(pendingActivationEntitlement.getPlan());
        }
        
        billingService.updateBilling(user, account, plan);
        return account;
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
        String filter = String.format("account=%s, state=ENTITLEMENT_ACTIVATION_REQUESTED", gcpAccoount.getId());
        List<Entitlement> entitlements = listEntitlements(filter);
        if (entitlements != null && entitlements.size() == 1) {
            Entitlement entitlement = entitlements.get(0);
            return entitlement;
        }
        return null;
    }

    private List<Entitlement> listEntitlements(String filter) throws IOException {
        ListEntitlementsResponse response = procurementService.providers().entitlements().list(GCPEventProcessorBase.PROVIDER_PATH).setFilter(filter).execute();
        List<Entitlement> entitlements = response.getEntitlements();
        return entitlements;
    }

}

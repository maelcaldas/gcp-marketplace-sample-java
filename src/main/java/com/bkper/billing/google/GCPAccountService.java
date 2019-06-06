package com.bkper.billing.google;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.bkper.billing.BillingService;
import com.bkper.objectify.Transact;
import com.bkper.user.BkperUser;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.cloudcommerceprocurement.v1.CloudCommercePartnerProcurementService;
import com.google.cloudcommerceprocurement.v1.model.ApproveAccountRequest;
import com.google.cloudcommerceprocurement.v1.model.ApproveEntitlementRequest;
import com.google.cloudcommerceprocurement.v1.model.Entitlement;
import com.google.cloudcommerceprocurement.v1.model.ListEntitlementsResponse;
import com.google.inject.Inject;
import com.googlecode.objectify.TxnType;

public class GCPAccountService {
    
    public static String PARTNER_ID = "xxxx-public"; // 
    public static String PROVIDER_PATH = "providers/" + PARTNER_ID;

    @Inject
    private GCPAccountRepository gpaAccountRepository;

    @Inject
    private BillingService billingService;

    @Inject
    private CloudCommercePartnerProcurementService procurementService;
    
    private static final Logger LOGGER = Logger.getLogger(GCPAccountService.class.getName());
    
    @Transact(TxnType.REQUIRED)
    public GCPAccount approveAccount(BkperUser user, String gcpAccountId) throws IOException {
        GCPAccount account = gpaAccountRepository.loadById(gcpAccountId);
        account.approve(gcpAccountId, user.getId(), user.getEmail());
        gpaAccountRepository.persist(account);
        ApproveAccountRequest content = new ApproveAccountRequest();
        content.setApprovalName("signup");
        String name = PROVIDER_PATH + "/accounts/" + gcpAccountId;
        procurementService.providers().accounts().approve(name, content).execute();
        
        //Approve pending entitlements async to avoid break approval in case of failing
        Queue queue = QueueFactory.getQueue("pending-entitlemenst-queue");
        queue.add(ofy().getTransaction(), TaskOptions.Builder.withPayload(new ApprovePendingEntitlementsTask(account)));
        
        return account;
    }

    public void approveEntitlement(String entitlementId) throws IOException {
        LOGGER.info("Approving entitlement: " + entitlementId);
        ApproveEntitlementRequest content = new ApproveEntitlementRequest();
        String name = PROVIDER_PATH + "/entitlements/" + entitlementId;
        procurementService.providers().entitlements().approve(name, content).execute();
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
        List<Entitlement> entitlements = listEntitlements(filter);
        if (entitlements != null && entitlements.size() == 1) {
            Entitlement entitlement = entitlements.get(0);
            return entitlement;
        }
        return null;
    }
    
    public Entitlement getEntitlement(String entitlementId) throws IOException {
        String name = PROVIDER_PATH + "/entitlements/" + entitlementId;
        Entitlement entitlement = procurementService.providers().entitlements().get(name).execute();
        return entitlement;
    }

    public List<Entitlement> listEntitlements(String filter) throws IOException {
        ListEntitlementsResponse response = procurementService.providers().entitlements().list(PROVIDER_PATH).setFilter(filter).execute();
        List<Entitlement> entitlements = response.getEntitlements();
        return entitlements;
    }

}

package com.bkper.billing.google.processors;

import java.io.IOException;

import com.bkper.billing.google.GCPAccountService;
import com.bkper.billing.google.pubsub.GCPPubsubMessageData;
import com.bkper.objectify.Transact;
import com.google.cloudcommerceprocurement.v1.model.ApproveEntitlementPlanChangeRequest;
import com.googlecode.objectify.TxnType;

public class GCPEventProcessorEntitlementPlanChangeRequested extends GCPEventProcessorBase {

    @Override
    @Transact(TxnType.REQUIRED)
    public void processMessage(GCPPubsubMessageData message) throws IOException {
        //User can change plan any time, so approve it straight way
        String entitlementId = message.getEntitlement().getId();
        ApproveEntitlementPlanChangeRequest content = new ApproveEntitlementPlanChangeRequest();
        content.setPendingPlanName(message.getEntitlement().getNewPlan().name());
        String name = GCPAccountService.PROVIDER_PATH + "/entitlements/" + entitlementId;
        procurementService.providers().entitlements().approvePlanChange(name, content).execute();
    }
}

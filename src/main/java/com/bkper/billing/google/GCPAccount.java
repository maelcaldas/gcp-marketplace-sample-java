package com.bkper.billing.google;

import static com.google.common.base.Preconditions.checkArgument;

import com.bkper.objectify.DatastoreObject;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity(name = "GCPAccount")
/**
 * 
 * This entity connects user to its GCP Account
 * 
 * @author maelcaldas
 *
 */
public class GCPAccount extends DatastoreObject {

    @Id
    private String id;
    
    @Index
    private String userId;
    
    @Index //For caching and search purposes
    private String email;
    
    private GCPAccountState state = GCPAccountState.PENDING;

    protected GCPAccount() {}

    public GCPAccount(String id) {
        super();
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getUserId() {
        return userId;
    }

    public GCPAccountState getState() {
        return state;
    }

    public void approve(String gcpAccountId, String userId, String email) {
        checkArgument(this.id.equals(gcpAccountId));
        this.userId = userId;
        this.email = email;
        this.state = GCPAccountState.APPROVED;
    }
}

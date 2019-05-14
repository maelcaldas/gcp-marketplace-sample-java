package com.bkper.billing.google;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.Key;

public class GCPAccountRepository {

    public void persist(GCPAccount account) {
        ofy().save().entity(account).now();
    }
    
    public GCPAccount loadById(String id) {
        Key<GCPAccount> key = Key.create(GCPAccount.class, id);
        return ofy().load().key(key).now();
    }

    public void delete(GCPAccount gcpAccount) {
        ofy().delete().entity(gcpAccount);
    }
}

package com.bkper.user;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.Key;

class BkperUserRepository {

    public void persist(BkperUser registeredUser) {
        registeredUser.ensureIdFilled();
        ofy().save().entity(registeredUser).now();
    }

    public BkperUser loadById(String id) {
        BkperUser bkperUser;
        Key<BkperUser> key = Key.create(BkperUser.class, id);
        bkperUser = ofy().transactionless().load().key(key).now();
        return bkperUser;
    }

}

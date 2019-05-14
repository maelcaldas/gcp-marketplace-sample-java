package com.bkper.user;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.bkper.billing.google.GCPAccountService;
import com.bkper.objectify.Transact;
import com.google.inject.Inject;
import com.googlecode.objectify.TxnType;

public class BkperUserService {

    private static final Logger LOGGER = Logger.getLogger(BkperUserService.class.getName());

    @Inject
    private BkperUserRepository bkperUserRepository;
    
    @Inject
    private GCPAccountService gcpAccountService;

    public BkperUser findById(String id) {
        return bkperUserRepository.loadById(id);
    }

    @Nullable
    private BkperUser find(GoogleUser googleUser) {
        if (googleUser == null) {
            return null;
        }
        return findById(googleUser.getId());
    }


    @Transact(TxnType.REQUIRED)
    public BkperUser persist(final BkperUser registeredUser) {
        bkperUserRepository.persist(registeredUser);
        return registeredUser;
    }


    /**
     * Returns a registered user if registered otherwise returns absent.
     * @throws IOException 
     */
    @Transact(TxnType.REQUIRED)
    public BkperUser addOrUpdateUser(GoogleUser googleUser) throws IOException {
        BkperUser bkperUser = find(googleUser);
        if (bkperUser == null) {
            LOGGER.info("Registering new user " + googleUser.getEmail());
            //NEW USER
            bkperUser = new BkperUser();
            bkperUser.setId(googleUser.getId());
            bkperUser.setEmail(googleUser.getEmail());
            bkperUser.updateProfileInfo(googleUser);
            persist(bkperUser);
        } else if (googleUser != null) {
            //RETURNING USER
            boolean profileUpdated = bkperUser.updateProfileInfo(googleUser);
            if (profileUpdated) {
                persist(bkperUser);
            }
        }
        
        //GCP Account id found. Approve account.
        if (googleUser.getGcpAccountId() != null) {
            gcpAccountService.approveAccount(bkperUser, googleUser.getGcpAccountId());
        }

        return bkperUser;
    }


    public BkperUser loadById(String id) {
        return bkperUserRepository.loadById(id);
    }



}

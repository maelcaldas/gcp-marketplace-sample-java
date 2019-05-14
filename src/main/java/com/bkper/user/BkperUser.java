package com.bkper.user;

import java.util.UUID;

import com.bkper.objectify.DatastoreObject;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;

@Entity(name = "BkperUser")
public class BkperUser extends DatastoreObject {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Index
    private String email;
    
    private String name;
    private String familyName;
    private String givenName;
    private String locale;
    private String pictureUrl;
    private String hd;
    private String profileLink;
    private String gender;


    public BkperUser() {
        super();
    }

    public void setId(String id) {
        if (isNullOrEmpty(this.id) && !isNullOrEmpty(id)) {
            this.id = id;
        }
    }

    @OnSave
    public void ensureIdFilled() {
        if (isNullOrEmpty(this.id)) {
            this.id = UUID.randomUUID().toString();
        }
    }
    
    private boolean isNullOrEmpty(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return true;
        }
        
        text = CharMatcher.anyOf("_- ").trimFrom(text);
        
        if ("".equals(text)) {
            return true;
        }
        
        if ("null".equalsIgnoreCase(text)) {
            return true;
        }
        
        return false;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase() : null;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public String getHd() {
        return hd;
    }

    public void setHd(String hd) {
        this.hd = hd;
    }

    public String getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return email;
    }

    public boolean updateProfileInfo(GoogleUser userInfo) {
        boolean updatedProfile = false;
        if (userInfo != null) {
            if (!Objects.equal(this.name, userInfo.getName())) {
                this.name = userInfo.getName();
                updatedProfile = true;
            }
            if (!Objects.equal(this.familyName, userInfo.getFamilyName())) {
                this.familyName = userInfo.getFamilyName();
                updatedProfile = true;
            }
            if (!Objects.equal(this.givenName, userInfo.getGivenName())) {
                this.givenName = userInfo.getGivenName();
                updatedProfile = true;
            }
            if (!Objects.equal(this.pictureUrl, userInfo.getPicture())) {
                this.pictureUrl = userInfo.getPicture();
                updatedProfile = true;
            }
            if (!Objects.equal(this.hd, userInfo.getHd())) {
                this.hd = userInfo.getHd();
                updatedProfile = true;
            }
            if (!Objects.equal(this.profileLink, userInfo.getLink())) {
                this.profileLink = userInfo.getLink();
                updatedProfile = true;
            }
            if (!Objects.equal(this.locale, userInfo.getLocale())) {
                this.locale = userInfo.getLocale();
                updatedProfile = true;
            }
        }
        return updatedProfile;
    }

}

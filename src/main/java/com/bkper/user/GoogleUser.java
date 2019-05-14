package com.bkper.user;

public class GoogleUser {

    private String id;
    
    //Add gcp account id to payload to send login user 
    private String gcpAccountId;
    private String email;
    private String name;
    private String familyName;
    private String givenName;
    private String picture;
    private String hd;
    private String link;
    private String locale;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getGcpAccountId() {
        return gcpAccountId;
    }
    
    public void setGcpAccountId(String gcpAccountId) {
        this.gcpAccountId = gcpAccountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getHd() {
        return hd;
    }

    public void setHd(String hd) {
        this.hd = hd;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }


}

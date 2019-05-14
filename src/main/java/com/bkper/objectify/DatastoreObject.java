package com.bkper.objectify;

import java.io.Serializable;
import java.util.Date;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;

public abstract class DatastoreObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer version = 0;

    //Timestamp
    @Index
    private Date createdAt;
    @Index
    private Date updatedAt;

    public abstract Object getId();

    public Integer getVersion() {
        return version;
    }

    protected void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    protected void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    protected void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void refreshUpdateAt() {
        this.updatedAt = new Date();
    }

    @OnSave
    public void updateVersion() {
        if (version != null) {
            this.version++;
        } else {
            this.version = 1;
        }

        initDates();
    }

    public void resetVersion() {
        this.version = null;
    }

    protected void initDates() {
        if (createdAt == null) {
            createdAt = new Date();
        }

        if (updatedAt == null) {
            updatedAt = new Date();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DatastoreObject other = (DatastoreObject) obj;
        if (getId() == null) {
            return other.getId() == null;
        } else return getId().equals(other.getId());
    }

}

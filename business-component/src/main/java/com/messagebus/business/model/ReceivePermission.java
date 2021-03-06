package com.messagebus.business.model;

import java.io.Serializable;

public class ReceivePermission implements Serializable {

    private static final long serialVersionUID = 6665895459121648194L;

    private int targetId;
    private int grantId;

    private transient String targetName;
    private transient String grantName;

    public ReceivePermission() {
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getGrantId() {
        return grantId;
    }

    public void setGrantId(int grantId) {
        this.grantId = grantId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getGrantName() {
        return grantName;
    }

    public void setGrantName(String grantName) {
        this.grantName = grantName;
    }

    @Override
    public String toString() {
        return "ReceivePermission{" +
            "targetId=" + targetId +
            ", grantId=" + grantId +
            ", targetName='" + targetName + '\'' +
            ", grantName='" + grantName + '\'' +
            '}';
    }
}

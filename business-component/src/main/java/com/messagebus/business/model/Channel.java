package com.messagebus.business.model;

import java.io.Serializable;

/**
 * Created by yanghua on 3/17/15.
 */
public class Channel implements Serializable {

    private String pushFrom;
    private String pushTo;

    public Channel() {
    }

    public String getPushFrom() {
        return pushFrom;
    }

    public void setPushFrom(String pushFrom) {
        this.pushFrom = pushFrom;
    }

    public String getPushTo() {
        return pushTo;
    }

    public void setPushTo(String pushTo) {
        this.pushTo = pushTo;
    }
}

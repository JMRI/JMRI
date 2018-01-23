package jmri.util.node;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Get the name for the local host.
 *
 * @author Randall Wood Copyright 2017
 */
public final class HostName {

    public static final String UNKNOWN_HOST = "unknown-host";
    private String hostName = null;

    public HostName() {
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.setHostName(UNKNOWN_HOST);
        }
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isKnownHost() {
        return !UNKNOWN_HOST.equals(this.hostName);
    }

}

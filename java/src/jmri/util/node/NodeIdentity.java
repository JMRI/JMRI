package jmri.util.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a unique network identity for JMRI. If a stored identity does not
 * exist, the identity is created by taking the MAC address of the first
 * {@link java.net.InetAddress} and prepending it with "jmri-". and removing all
 * :s from the address.
 *
 * If a stored identity is found, it is discarded if none of the InetAddresses
 * for the host match the MAC address in the identity and regenerated.
 *
 * If the MAC address of the localhost cannot be read, fall back on using the
 * hostname or IP address. If no local IP address is available provide an empty
 * string.
 *
 * @author Randall Wood (C) 2013
 */
public class NodeIdentity {

    private static NodeIdentity instance = null;
    private String identity = null;
    private final File identityFile = new File(FileUtil.getPreferencesPath() + "networkIdentity.properties"); // NOI18N
    private final static Logger log = LoggerFactory.getLogger(NodeIdentity.class);

    private NodeIdentity() {
        init();
    }

    synchronized private void init() {
        Properties p = new Properties();
        FileInputStream is = null;
        if (this.identityFile.exists()) {
            try {
                is = new FileInputStream(this.identityFile);
                p.loadFromXML(is);
                is.close();
            } catch (IOException ex) {
                if (is != null) {
                    try {
                        log.error("Unable to read network identity file: {}", ex.getLocalizedMessage());
                        is.close();
                    } catch (IOException ex1) {
                        log.error("Unable to close network identify file: {}", ex1.getLocalizedMessage());
                    }
                }
            }
            this.setIdentity(p.getProperty("networkIdentity")); // NOI18N
        } else {
            this.setIdentity(null);
        }
    }

    synchronized public static String identity() {
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.identity);
        }
        return instance.identity;
    }

    private void setIdentity(String identity) {
        if (identity == null || !this.validateIdentity(identity)) {
            this.saveIdentity();
        }
        this.identity = identity;
    }

    private boolean validateIdentity(String identity) {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                if (this.createIdentity(enumeration.nextElement().getHardwareAddress()).equals(identity)) {
                    return true;
                }
            }
        } catch (SocketException ex) {
            log.error("Error accessing interface: {}", ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    synchronized private void saveIdentity() {
        Properties p = new Properties();
        FileOutputStream os = null;
        try {
            this.identity = this.createIdentity(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
        } catch (UnknownHostException ex) {
            try {
                this.identity = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex1) {
                try {
                    this.identity = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException ex2) {
                    this.identity = "";
                    log.error("Cannot get host address or name {}", ex2.getLocalizedMessage());
                }
            }
        } catch (SocketException ex) {
            try {
                this.identity = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex1) {
                try {
                    this.identity = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException ex2) {
                    this.identity = "";
                    log.error("Cannot get host address or name {}", ex2.getLocalizedMessage());
                }
            }
        }
        if (this.identity != null) {
            p.setProperty("networkIdentity", this.identity); // NOI18N
        }
        if (!this.identityFile.exists()) {
            log.error("Unable to create file at {}", this.identityFile.getAbsolutePath()); // NOI18N
        }
        try {
            os = new FileOutputStream(this.identityFile);
            p.storeToXML(os, "Network identity configuration (saved at " + (new Date()).toString() + ")"); // NOI18N
            os.close();
        } catch (IOException ex) {
            if (os != null) {
                try {
                    log.error("Unable to create network identity file: {}", ex.getLocalizedMessage());
                    os.close();
                } catch (IOException ex1) {
                    log.error("Unable to close network identify file: {}", ex1.getLocalizedMessage());
                }
            }
        }
    }

    private String createIdentity(byte[] mac) {
        StringBuilder sb = new StringBuilder("jmri-"); // NOI18N
        try {
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i])); // NOI18N
            }
        } catch (NullPointerException ex) {
            // not all interfaces have MAC addresses, but we don't care
        }
        return sb.toString();
    }
}

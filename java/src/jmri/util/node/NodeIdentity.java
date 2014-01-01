package jmri.util.node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import jmri.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a unique network identity for JMRI. If a stored
 * identity does not exist, the identity is created by taking the MAC address of
 * the first {@link java.net.InetAddress} and prepending it with "jmri-". and
 * removing all :s from the address.
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

    private final ArrayList<String> formerIdentities = new ArrayList<String>();
    private String identity = null;
    private final File identityFile = new File(FileUtil.getPreferencesPath() + "nodeIdentity.xml"); // NOI18N

    private static NodeIdentity instance = null;
    private final static Logger log = LoggerFactory.getLogger(NodeIdentity.class);

    private final static String ROOT_ELEMENT = "nodeIdentityConfig"; // NOI18N
    private final static String NODE_IDENTITY = "nodeIdentity"; // NOI18N
    private final static String FORMER_IDENTITIES = "formerIdentities"; // NOI18N

    private NodeIdentity() {
        init();
    }

    synchronized private void init() {
        if (this.identityFile.exists()) {
            try {
                Document doc = (new SAXBuilder()).build(this.identityFile);
                String id = doc.getRootElement().getChild(NODE_IDENTITY).getAttributeValue(NODE_IDENTITY);
                this.formerIdentities.clear();
                for (Element e : (List<Element>) doc.getRootElement().getChild(FORMER_IDENTITIES).getChildren()) {
                    this.formerIdentities.add(e.getAttributeValue(NODE_IDENTITY));
                }
                if (!this.validateIdentity(id)) {
                    log.debug("Node identity {} is invalid. Generating new node identity.", id);
                    this.formerIdentities.add(id);
                    this.getIdentity(true);
                } else {
                    this.getIdentity(true);
                }
            } catch (JDOMException ex) {
                log.error("Unable to read node identities: {}", ex.getLocalizedMessage());
                this.getIdentity(true);
            } catch (IOException ex) {
                log.error("Unable to read node identities: {}", ex.getLocalizedMessage());
                this.getIdentity(true);
            }
        } else {
            this.getIdentity(true);
        }
    }

    synchronized public static String identity() {
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.identity);
        }
        return instance.identity;
    }

    synchronized private boolean validateIdentity(String identity) {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (!networkInterface.isVirtual() && this.createIdentity(networkInterface.getHardwareAddress()).equals(identity)) {
                    return true;
                }
            }
        } catch (SocketException ex) {
            log.error("Error accessing interface: {}", ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    synchronized private void getIdentity(boolean save) {
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
        if (save) {
            this.saveIdentity();
        }
    }

    private void saveIdentity() {
        FileWriter fw = null;
        Document doc = new Document();
        doc.setRootElement(new Element(ROOT_ELEMENT));
        Element identityElement = new Element(NODE_IDENTITY);
        Element formerIdentitiesElement = new Element(FORMER_IDENTITIES);
        if (this.identity == null) {
            this.getIdentity(false);
        }
        identityElement.setAttribute(NODE_IDENTITY, this.identity);
        for (String formerIdentity : this.formerIdentities) {
            log.debug("Retaining former node identity {}", formerIdentity);
            Element e = new Element(NODE_IDENTITY);
            e.setAttribute(NODE_IDENTITY, formerIdentity);
            formerIdentitiesElement.addContent(e);
        }
        doc.getRootElement().addContent(identityElement);
        doc.getRootElement().addContent(formerIdentitiesElement);
        try {
            fw = new FileWriter(this.identityFile);
            (new XMLOutputter(Format.getPrettyFormat())).output(doc, fw);
            fw.close();
        } catch (IOException ex) {
            // close fw if possible
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex1) {
                    log.error("Unable to store node identities: {}", ex1.getLocalizedMessage());
                }
            }
            log.error("Unable to store node identities: {}", ex.getLocalizedMessage());
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

    private void retainFormerIdentitytt(String identity) {
        this.formerIdentities.add(identity);
        this.saveIdentity();
    }
}

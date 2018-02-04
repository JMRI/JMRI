package jmri.util.node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import jmri.InstanceManager;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.web.server.WebServerPreferences;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a unique network identity for JMRI. If a stored identity does not
 * exist, the identity is created by taking the MAC address of the first
 * {@link java.net.InetAddress} and prepending it with "jmri-". and removing all
 * colons from the address.
 *
 * If a stored identity is found, the identity is replaced if none of the
 * InetAddresses for the host match the MAC address in the identity and
 * regenerated.
 *
 * A list of former identities is retained to aid in migrating from the former
 * identity to the new identity.
 *
 * If the MAC address of the localhost cannot be read, fall back on using the
 * hostname or IP address. If no local IP address is available, fall back on
 * using the railroad name.
 *
 * @author Randall Wood (C) 2013, 2014, 2016
 */
public class NodeIdentity {

    private final ArrayList<String> formerIdentities = new ArrayList<>();
    private String identity = null;

    private static NodeIdentity instance = null;
    private final static Logger log = LoggerFactory.getLogger(NodeIdentity.class);

    private final static String ROOT_ELEMENT = "nodeIdentityConfig"; // NOI18N
    private final static String NODE_IDENTITY = "nodeIdentity"; // NOI18N
    private final static String FORMER_IDENTITIES = "formerIdentities"; // NOI18N

    private NodeIdentity() {
        init(); // init as a method so the init can be synchronized.
    }

    synchronized private void init() {
        File identityFile = this.identityFile();
        if (identityFile.exists()) {
            try {
                Document doc = (new SAXBuilder()).build(identityFile);
                String id = doc.getRootElement().getChild(NODE_IDENTITY).getAttributeValue(NODE_IDENTITY);
                this.formerIdentities.clear();
                doc.getRootElement().getChild(FORMER_IDENTITIES).getChildren().stream().forEach((e) -> {
                    this.formerIdentities.add(e.getAttributeValue(NODE_IDENTITY));
                });
                if (!this.validateIdentity(id)) {
                    log.debug("Node identity {} is invalid. Generating new node identity.", id);
                    this.formerIdentities.add(id);
                    this.getIdentity(true);
                } else {
                    this.getIdentity(true);
                }
            } catch (JDOMException | IOException ex) {
                log.error("Unable to read node identities: {}", ex.getLocalizedMessage());
                this.getIdentity(true);
            }
        } else {
            this.getIdentity(true);
        }
    }

    /**
     * Return the node's current identity.
     *
     * @return An identity. If this identity is not in the form
     * <i>jmri-MACADDRESS-profileId</i>, this identity should be considered
     * unreliable and subject to change across JMRI restarts.
     */
    synchronized public static String identity() {
        String uniqueId = "-";
        try {
            uniqueId += ProfileManager.getDefault().getActiveProfile().getUniqueId();
        } catch (NullPointerException ex) {
            uniqueId += ProfileManager.createUniqueId();
        }
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.getIdentity() + uniqueId);
        }
        return instance.getIdentity() + uniqueId;
    }

    /**
     * If network hardware on a node was replaced, the identity will change.
     *
     * @return A list of other identities this node may have had in the past.
     */
    synchronized public static List<String> formerIdentities() {
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.getIdentity());
        }
        return instance.getFormerIdentities();
    }

    /**
     * Verify that the current identity is a valid identity for this hardware.
     *
     * @return true if the identity is based on this hardware.
     */
    synchronized private boolean validateIdentity(String identity) {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface nic = enumeration.nextElement();
                if (!nic.isVirtual() && !nic.isLoopback()) {
                    String nicIdentity = this.createIdentity(nic.getHardwareAddress());
                    if (nicIdentity != null && nicIdentity.equals(identity)) {
                        return true;
                    }
                }
            }
        } catch (SocketException ex) {
            log.error("Error accessing interface: {}", ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    /**
     * Get a node identity from the current hardware.
     *
     */
    synchronized private void getIdentity(boolean save) {
        try {
            try {
                try {
                    this.identity = this.createIdentity(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
                } catch (NullPointerException ex) {
                    // NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress() failed
                    // this can be due to multiple reasons, most likely getLocalHost() failing on certain platforms.
                    // Only set this.identity = null, since the following null checks address all potential problems
                    // with getLocalHost() including some expected conditions (such as InetAddress.getLocalHost()
                    // returning the loopback interface).
                    this.identity = null;
                }
                if (this.identity == null) {
                    Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                    while (nics.hasMoreElements()) {
                        NetworkInterface nic = nics.nextElement();
                        if (!nic.isLoopback() && !nic.isVirtual()) {
                            this.identity = this.createIdentity(nic.getHardwareAddress());
                            if (this.identity != null) {
                                break;
                            }
                        }
                    }
                }
                if (this.identity == null) {
                    try {
                        this.identity = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException ex1) {
                        this.identity = InetAddress.getLocalHost().getHostAddress();
                    }
                }
            } catch (SocketException ex) {
                try {
                    this.identity = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException ex1) {
                    this.identity = InetAddress.getLocalHost().getHostAddress();
                }
            }
        } catch (UnknownHostException ex) {
            this.identity = InstanceManager.getDefault(WebServerPreferences.class).getRailroadName().replaceAll("[^A-Za-z0-9 ]", "-"); // NOI18N
            log.error("Cannot get host address or name {}", ex.getLocalizedMessage());
            log.error("Using {} as a fallback.", this.identity);
        }
        if (save) {
            this.saveIdentity();
        }
    }

    /**
     * Save the current node identity and all former identities to file.
     */
    private void saveIdentity() {
        Document doc = new Document();
        doc.setRootElement(new Element(ROOT_ELEMENT));
        Element identityElement = new Element(NODE_IDENTITY);
        Element formerIdentitiesElement = new Element(FORMER_IDENTITIES);
        if (this.identity == null) {
            this.getIdentity(false);
        }
        identityElement.setAttribute(NODE_IDENTITY, this.identity);
        this.formerIdentities.stream().forEach((formerIdentity) -> {
            log.debug("Retaining former node identity {}", formerIdentity);
            Element e = new Element(NODE_IDENTITY);
            e.setAttribute(NODE_IDENTITY, formerIdentity);
            formerIdentitiesElement.addContent(e);
        });
        doc.getRootElement().addContent(identityElement);
        doc.getRootElement().addContent(formerIdentitiesElement);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(this.identityFile()), "UTF-8")) { // NOI18N
            XMLOutputter fmt = new XMLOutputter();
            fmt.setFormat(Format.getPrettyFormat()
                    .setLineSeparator(System.getProperty("line.separator"))
                    .setTextMode(Format.TextMode.PRESERVE));
            fmt.output(doc, w);
        } catch (IOException ex) {
            log.error("Unable to store node identities: {}", ex.getLocalizedMessage());
        }
    }

    /**
     * Create an identity string given a MAC address.
     *
     * @param mac a byte array representing a MAC address.
     * @return An identity or null if input is null.
     */
    private String createIdentity(byte[] mac) {
        StringBuilder sb = new StringBuilder("jmri-"); // NOI18N
        try {
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i])); // NOI18N
            }
        } catch (NullPointerException ex) {
            return null;
        }
        return sb.toString();
    }

    private File identityFile() {
        return new File(FileUtil.getPreferencesPath() + "nodeIdentity.xml"); // NOI18N
    }

    /**
     * @return the identity
     */
    synchronized public String getIdentity() {
        if (this.identity == null) {
            this.getIdentity(false);
        }
        return this.identity;
    }

    /**
     * @return the formerIdentities
     */
    public List<String> getFormerIdentities() {
        return new ArrayList<>(this.formerIdentities);
    }
}

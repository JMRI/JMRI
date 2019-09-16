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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide unique identities for JMRI.
 * <p>
 * A list of former identities is retained to aid in migrating from the former
 * identity to the new identity.
 * <p>
 * Currently the storageIdentity is a randomly generated UUID, that is also used
 * for backwards compatibility with JMRI 4.14. If we find a reliable
 * cross-platform mechanism to tie that to the machine's unique identity (from
 * the CPU or motherboard), not from a NIC, this may change. If a JMRI 4.14
 * generated UUID is available, it is retained and used as the storageIdentity.
 *
 * @author Randall Wood (C) 2013, 2014, 2016
 * @author Dave Heap (C) 2018
 */
public class NodeIdentity {

    private final Set<String> formerIdentities = new HashSet<>();
    private UUID uuid = null;
    private String networkIdentity = null;
    private String storageIdentity = null;
    private final Map<Profile, String> profileStorageIdentities = new HashMap<>();

    private static NodeIdentity instance = null;
    private static final Logger log = LoggerFactory.getLogger(NodeIdentity.class);

    private static final String ROOT_ELEMENT = "nodeIdentityConfig"; // NOI18N
    private static final String UUID_ELEMENT = "uuid"; // NOI18N
    private static final String NODE_IDENTITY = "nodeIdentity"; // NOI18N
    private static final String STORAGE_IDENTITY = "storageIdentity"; // NOI18N
    private static final String FORMER_IDENTITIES = "formerIdentities"; // NOI18N
    private static final String IDENTITY_PREFIX = "jmri-";

    /**
     * A string of 64 URL compatible characters.
     * <p>
     * Used by {@link #uuidToCompactString uuidToCompactString} and
     * {@link #uuidFromCompactString uuidFromCompactString}.
     */
    protected static final String URL_SAFE_CHARACTERS =
            "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789"; // NOI18N

    private NodeIdentity() {
        init(); // initialize as a method so the initialization can be synchronized.
    }

    private synchronized void init() {
        File identityFile = this.identityFile();
        if (identityFile.exists()) {
            try {
                boolean save = false;
                this.formerIdentities.clear();
                Document doc = (new SAXBuilder()).build(identityFile);
                Element ue = doc.getRootElement().getChild(UUID_ELEMENT);
                if (ue != null) {
                    try {
                        String attr = ue.getAttributeValue(UUID_ELEMENT);
                        this.uuid = uuidFromCompactString(attr);
                        this.storageIdentity = this.uuid.toString(); // backwards compatible, see class docs
                        this.formerIdentities.add(this.storageIdentity);
                        this.formerIdentities.add(IDENTITY_PREFIX + attr);
                    } catch (IllegalArgumentException ex) {
                        // do nothing
                    }
                }
                Element si = doc.getRootElement().getChild(STORAGE_IDENTITY);
                if (si != null) {
                    try {
                        this.storageIdentity = si.getAttributeValue(STORAGE_IDENTITY);
                        if (this.uuid == null || !this.storageIdentity.equals(this.uuid.toString())) {
                            this.uuid = UUID.fromString(this.storageIdentity);
                            save = true; // updated UUID
                        }
                    } catch (IllegalArgumentException ex) {
                        save = true; // save if attribute not available
                    }
                } else {
                    save = true; // element missing, need to save
                }
                if (this.storageIdentity == null) {
                    save = true;
                    this.getStorageIdentity(false);
                }
                String id = null;
                try {
                    id = doc.getRootElement().getChild(NODE_IDENTITY).getAttributeValue(NODE_IDENTITY);
                    doc.getRootElement().getChild(FORMER_IDENTITIES).getChildren().stream().forEach((e) -> {
                        this.formerIdentities.add(e.getAttributeValue(NODE_IDENTITY));
                    });
                } catch (NullPointerException ex) {
                    // do nothing -- if id was not set, it will be generated
                }
                if (!this.validateNetworkIdentity(id)) {
                    log.warn("Node identity {} is invalid. Generating new node identity.", id);
                    save = true;
                    this.getNetworkIdentity(false);
                } else {
                    this.networkIdentity = id;
                }
                // save if new identities were created or expected attribute did not exist
                if (save) {
                    this.saveIdentity();
                }
            } catch (JDOMException | IOException ex) {
                log.error("Unable to read node identities: {}", ex.getLocalizedMessage());
                this.getNetworkIdentity(true);
            }
        } else {
            this.getNetworkIdentity(true);
        }
    }

    /**
     * Return the node's current network identity. For historical purposes, the
     * network identity is also referred to as the {@literal node} or
     * {@literal node identity}.
     *
     * @return A network identity. If this identity is not in the form
     *         {@code jmri-MACADDRESS-profileId}, or if {@code MACADDRESS} is a
     *         multicast MAC address, this identity should be considered
     *         unreliable and subject to change across JMRI restarts. Note that
     *         if the identity is in the form {@code jmri-MACADDRESS} the JMRI
     *         instance has not loaded a configuration profile, and the network
     *         identity will change once that a configuration profile is loaded.
     */
    public static synchronized String networkIdentity() {
        String uniqueId = "";
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            uniqueId = "-" + profile.getUniqueId();
        }
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.getNetworkIdentity() + uniqueId);
        }
        return instance.getNetworkIdentity() + uniqueId;
    }

    /**
     * Return the node's current storage identity for the active profile. This
     * is a convenience method that calls {@link #storageIdentity(Profile)} with
     * the result of {@link jmri.profile.ProfileManager#getActiveProfile()}.
     *
     * @return A storage identity.
     * @see #storageIdentity(Profile)
     */
    public static synchronized String storageIdentity() {
        return storageIdentity(ProfileManager.getDefault().getActiveProfile());
    }

    /**
     * Return the node's current storage identity. This can be used in networked
     * file systems to ensure per-computer storage is available.
     * <p>
     * <strong>Note</strong> this only ensure uniqueness if the preferences path
     * is not shared between multiple computers as documented in
     * {@link jmri.util.FileUtil#getPreferencesPath()} (the most common cause of
     * this would be sharing a user's home directory in its entirety between two
     * computers with similar operating systems as noted in
     * getPreferencesPath()).
     *
     * @param profile The profile to get the identity for. This is only needed
     *                    to check that the identity should not be in an older
     *                    format.
     *
     * @return A storage identity. If this identity is not in the form of a UUID
     *         or {@code jmri-UUID-profileId}, this identity should be
     *         considered unreliable and subject to change across JMRI restarts.
     *         When generating a new storage ID, the form is always a UUID and
     *         other forms are used only to ensure continuity where other forms
     *         may have been used in the past.
     */
    public static synchronized String storageIdentity(Profile profile) {
        if (instance == null) {
            instance = new NodeIdentity();
        }
        String id = instance.getStorageIdentity();
        // this entire check is so that a JMRI 4.14 style identity string can be built
        // and checked against the given profile to determine if that should be used
        // instead of just returning the non-profile-specific machine identity
        if (profile != null) {
            // using a map to store profile-specific identities allows for the possibility
            // that, although there is only one active profile at a time, other profiles
            // may be manipulated by JMRI while that profile is active (this happens to a
            // limited extent already in the profile configuration UI)
            // (a map also allows for ensuring the info message is displayed once per profile)
            if (!instance.profileStorageIdentities.containsKey(profile)) {
                String oldId = IDENTITY_PREFIX + uuidToCompactString(instance.uuid) + "-" + profile.getUniqueId();
                File local = new File(new File(profile.getPath(), Profile.PROFILE), oldId);
                if (local.exists() && local.isDirectory()) {
                    id = oldId;
                }
                instance.profileStorageIdentities.put(profile, id);
                log.info("Using {} as the JMRI storage identity for profile id {}", id, profile.getUniqueId());
            }
            id = instance.profileStorageIdentities.get(profile);
        }
        return id;
    }

    /**
     * If network hardware on a node was replaced, the identity will change.
     *
     * @return A list of other identities this node may have had in the past.
     */
    public static synchronized List<String> formerIdentities() {
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.getNetworkIdentity());
        }
        return instance.getFormerIdentities();
    }

    /**
     * Verify that the current identity is a valid identity for this hardware.
     *
     * @param identity the identity to validate; may be null
     * @return true if the identity is based on this hardware; false otherwise
     */
    private synchronized boolean validateNetworkIdentity(String identity) {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface nic = enumeration.nextElement();
                if (!nic.isVirtual() && !nic.isLoopback()) {
                    String nicIdentity = this.createNetworkIdentity(nic.getHardwareAddress());
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
     * @param save whether to save this identity or not
     */
    private synchronized void getNetworkIdentity(boolean save) {
        try {
            try {
                try {
                    this.networkIdentity = this.createNetworkIdentity(
                            NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
                } catch (NullPointerException ex) {
                    // NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress() failed
                    // this can be due to multiple reasons, most likely getLocalHost() failing on certain platforms.
                    // Only set this.identity = null, since the following null checks address all potential problems
                    // with getLocalHost() including some expected conditions (such as InetAddress.getLocalHost()
                    // returning the loopback interface).
                    this.networkIdentity = null;
                }
                if (this.networkIdentity == null) {
                    Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                    while (nics.hasMoreElements()) {
                        NetworkInterface nic = nics.nextElement();
                        if (!nic.isLoopback() && !nic.isVirtual() && (nic.getHardwareAddress() != null)) {
                            this.networkIdentity = this.createNetworkIdentity(nic.getHardwareAddress());
                            if (this.networkIdentity != null) {
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                this.networkIdentity = null;
            }
        } catch (UnknownHostException ex) {
            this.networkIdentity = null;
        }
        if (this.networkIdentity == null) {
            log.info("No MAC addresses found, generating a random multicast MAC address as per RFC 4122.");
            byte[] randBytes = new byte[6];
            Random randGen = new Random();
            randGen.nextBytes(randBytes);
            randBytes[0] = (byte) (randBytes[0] | 0x01); // set multicast bit in first octet
            this.networkIdentity = this.createNetworkIdentity(randBytes);
        }
        this.formerIdentities.add(this.networkIdentity);
        if (save) {
            this.saveIdentity();
        }
    }

    /**
     * Get a node identity from the current hardware.
     *
     * @param save whether to save this identity or not
     */
    private synchronized void getStorageIdentity(boolean save) {
        if (this.storageIdentity == null) {
            // also generate UUID to protect against case where user
            // migrates from JMRI < 4.14 to JMRI > 4.14 back to JMRI = 4.14
            if (this.uuid == null) {
                this.uuid = UUID.randomUUID();
            }
            this.storageIdentity = this.uuid.toString();
            this.formerIdentities.add(this.storageIdentity);
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
        Element networkIdentityElement = new Element(NODE_IDENTITY);
        Element storageIdentityElement = new Element(STORAGE_IDENTITY);
        Element formerIdentitiesElement = new Element(FORMER_IDENTITIES);
        Element uuidElement = new Element(UUID_ELEMENT);
        if (this.networkIdentity == null) {
            this.getNetworkIdentity(false);
        }
        if (this.storageIdentity == null) {
            this.getStorageIdentity(false);
        }
        // ensure formerIdentities contains current identities as well
        this.formerIdentities.add(this.networkIdentity);
        this.formerIdentities.add(this.storageIdentity);
        if (this.uuid != null) {
            this.formerIdentities.add(IDENTITY_PREFIX + uuidToCompactString(this.uuid));
        }
        networkIdentityElement.setAttribute(NODE_IDENTITY, this.networkIdentity);
        storageIdentityElement.setAttribute(STORAGE_IDENTITY, this.storageIdentity);
        this.formerIdentities.stream().forEach((formerIdentity) -> {
            log.debug("Retaining former node identity {}", formerIdentity);
            Element e = new Element(NODE_IDENTITY);
            e.setAttribute(NODE_IDENTITY, formerIdentity);
            formerIdentitiesElement.addContent(e);
        });
        doc.getRootElement().addContent(networkIdentityElement);
        doc.getRootElement().addContent(storageIdentityElement);
        if (this.uuid != null) {
            uuidElement.setAttribute(UUID_ELEMENT, uuidToCompactString(this.uuid));
            doc.getRootElement().addContent(uuidElement);
        }
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
    private String createNetworkIdentity(byte[] mac) {
        StringBuilder sb = new StringBuilder(IDENTITY_PREFIX); // NOI18N
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
     * @return the network identity
     */
    private synchronized String getNetworkIdentity() {
        if (this.networkIdentity == null) {
            this.getNetworkIdentity(false);
        }
        return this.networkIdentity;
    }

    /**
     * @return the storage identity
     */
    private synchronized String getStorageIdentity() {
        if (this.storageIdentity == null) {
            this.getStorageIdentity(false);
        }
        return this.storageIdentity;
    }

    /**
     * Encodes a UUID into a 22 character URL compatible string. This is used to
     * store the UUID in a manner compatible with JMRI 4.14.
     * <p>
     * From an example by <a href="https://stackoverflow.com/">Tom Lobato</a>.
     *
     * @param uuid the UUID to encode
     * @return the 22 character string
     */
    protected static String uuidToCompactString(UUID uuid) {
        char[] c = new char[22];
        long buffer = 0;
        int val6;
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= 22; i++) {
            switch (i) {
                case 1:
                    buffer = uuid.getLeastSignificantBits();
                    break;
                case 12:
                    buffer = uuid.getMostSignificantBits();
                    break;
                default:
                    break;
            }
            val6 = (int) (buffer & 0x3F);
            c[22 - i] = URL_SAFE_CHARACTERS.charAt(val6);
            buffer = buffer >>> 6;
        }
        return sb.append(c).toString();
    }

    /**
     * Decodes the original UUID from a 22 character string generated by
     * {@link #uuidToCompactString uuidToCompactString}. This is used to store
     * the UUID in a manner compatible with JMRI 4.14.
     *
     * @param compact the 22 character string
     * @return the original UUID
     */
    protected static UUID uuidFromCompactString(String compact) {
        long mostSigBits = 0;
        long leastSigBits = 0;
        long buffer = 0;
        int val6;

        for (int i = 0; i <= 21; i++) {
            switch (i) {
                case 0:
                    buffer = 0;
                    break;
                case 11:
                    mostSigBits = buffer;
                    buffer = 0;
                    break;
                default:
                    buffer = buffer << 6;
                    break;
            }
            val6 = URL_SAFE_CHARACTERS.indexOf(compact.charAt(i));
            buffer = buffer | (val6 & 0x3F);
        }
        leastSigBits = buffer;
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * @return the former identities; this is a combination of former network
     *         and storage identities
     */
    public List<String> getFormerIdentities() {
        return new ArrayList<>(this.formerIdentities);
    }
}

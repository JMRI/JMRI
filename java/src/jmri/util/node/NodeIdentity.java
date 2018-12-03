package jmri.util.node;

import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoField.YEAR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
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
 * Provide a unique network identity for JMRI. If a stored identity does not
 * exist, the identity is created by taking the MAC address of the first
 * {@link java.net.InetAddress}, generating a Type 1 UUID, storing it,
 * compressing it into a 22 character representation and prepending it with
 * "jmri-".
 * <p>
 * If a stored UUID is found, it is always used.
 * <p>
 * A list of former identities is retained to aid in migrating from the former
 * identity to the new identity.
 * <p>
 * If a MAC address cannot be read, fall back on generating a random multicast
 * MAC address as per RFC 4122.
 *
 * @author Randall Wood (C) 2013, 2014, 2016
 * @author Dave Heap (C) 2018
 */
public class NodeIdentity {

    private final ArrayList<String> formerIdentities = new ArrayList<>();
    private String identity = null;
    private String uuid = null;

    private static NodeIdentity instance = null;
    private static final Logger log = LoggerFactory.getLogger(NodeIdentity.class);

    private static final String ROOT_ELEMENT = "nodeIdentityConfig"; // NOI18N
    private static final String UUID = "uuid"; // NOI18N
    private static final String NODE_IDENTITY = "nodeIdentity"; // NOI18N
    private static final String FORMER_IDENTITIES = "formerIdentities"; // NOI18N
    private static final String IDENTITY_PREFIX = "jmri-";

    /**
     * A string of 64 URL compatible characters.
     * <p>
     * Used by {@link #uuidToCompactString uuidToCompactString} and
     * {@link #uuidFromCompactString uuidFromCompactString}.
     */
    protected static final String URL_SAFE_CHARACTERS
            = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789"; // NOI18N

    private NodeIdentity() {
        init(); // init as a method so the init can be synchronized.
    }

    private synchronized void init() {
        File identityFile = this.identityFile();
        if (identityFile.exists()) {
            try {
                Document doc = (new SAXBuilder()).build(identityFile);
                Element uu = doc.getRootElement().getChild(UUID);
                if (uu != null) {
                    this.uuid = uu.getAttributeValue(UUID);
                } else {
                    this.uuid = null;
                    this.getIdentity(true);
                }
                String id = doc.getRootElement().getChild(NODE_IDENTITY).getAttributeValue(NODE_IDENTITY);
                this.formerIdentities.clear();
                doc.getRootElement().getChild(FORMER_IDENTITIES).getChildren().stream().forEach((e) -> {
                    this.formerIdentities.add(e.getAttributeValue(NODE_IDENTITY));
                });
                if (!this.validateIdentity(id)) {
                    log.warn("Node identity {} is invalid. Generating new node identity.", id);
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
            this.uuid = null;
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
    public static synchronized String identity() {
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
    public static synchronized List<String> formerIdentities() {
        if (instance == null) {
            instance = new NodeIdentity();
            log.info("Using {} as the JMRI Node identity", instance.getIdentity());
        }
        return instance.getFormerIdentities();
    }

    /**
     * Verify that the current identity is a valid identity for this hardware.
     *
     * @param ident an identity to check.
     *
     * @return true if the identity is based on the current UUID.
     */
    private synchronized boolean validateIdentity(String ident) {
        log.debug("Validating Node identity {}.", ident);
        return (this.uuid != null && ((IDENTITY_PREFIX + this.uuid).equals(ident)));
    }

    /**
     * Get a node identity from the current hardware.
     *
     * @param save whether to save this identity or not
     * <p>
     */
    private synchronized void getIdentity(boolean save) {
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
                        if (!nic.isLoopback() && !nic.isVirtual()
                                && (nic.getHardwareAddress() != null)) {
                            this.identity = this.createIdentity(nic.getHardwareAddress());
                            if (this.identity != null) {
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                this.identity = null;
            }
        } catch (UnknownHostException ex) {
            this.identity = null;
        }
        if (this.identity == null) {
            log.info("No MAC addresses found, generating a random multicast MAC address as per RFC 4122.");
            byte[] randBytes = new byte[6];
            Random randGen = new Random();
            randGen.nextBytes(randBytes);
            randBytes[0] = (byte) (randBytes[0] | 0x01); // set multicast bit in first octet
            this.identity = this.createIdentity(randBytes);
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
        Element uuidElement = new Element(UUID);
        Element identityElement = new Element(NODE_IDENTITY);
        Element formerIdentitiesElement = new Element(FORMER_IDENTITIES);
        if (this.identity == null) {
            this.getIdentity(false);
        }
        uuidElement.setAttribute(UUID, this.uuid);
        identityElement.setAttribute(NODE_IDENTITY, this.identity);
        this.formerIdentities.stream().forEach((formerIdentity) -> {
            log.debug("Retaining former node identity {}", formerIdentity);
            Element e = new Element(NODE_IDENTITY);
            e.setAttribute(NODE_IDENTITY, formerIdentity);
            formerIdentitiesElement.addContent(e);
        });
        doc.getRootElement().addContent(uuidElement);
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
        if (mac == null) {
            return null;
        }

        if (this.uuid == null) {
            UUID uu = generateUuid(mac);
            log.debug("Original UUID= {}", uu.toString());

            this.uuid = uuidToCompactString(uu);
            log.debug("Compact string ='{}'", this.uuid);
        }

        StringBuilder sb = new StringBuilder(IDENTITY_PREFIX); // NOI18N
        sb.append(this.uuid);
        return sb.toString();
    }

    private File identityFile() {
        return new File(FileUtil.getPreferencesPath() + "nodeIdentity.xml"); // NOI18N
    }

    /**
     * Generates a Version 1 Variant 2 UUID for this installation.
     * <p>
     * Once generated, this should be stored in {@code nodeIdentity.xml} and
     * always used for all profiles.
     *
     * @param seed a seed for UUID generation, typically the MAC address of any
     *             interface on this computer.
     * @return the UUID
     */
    public static UUID generateUuid(@Nonnull byte[] seed) {
        long mostSigBits = 0;
        long leastSigBits = 0;
        long time;

        if (seed == null) {
            throw new IllegalArgumentException("Uuid seed cannot be null");
        }

        for (byte b : seed) {
            leastSigBits = leastSigBits << 8;
            leastSigBits = leastSigBits | (b & 0xFF);
        }
        leastSigBits = leastSigBits & Long.parseUnsignedLong("0000FFFFFFFFFFFF", 16); // just to be sure no overflow from node
        leastSigBits = leastSigBits | Long.parseUnsignedLong("8000000000000000", 16); // variant 2

        ZonedDateTime startDate;
        startDate = ZonedDateTime.parse("1582-10-15T00:00:00Z");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(NodeIdentity.class.getName()).log(Level.SEVERE, null, ex);
        }
        ZonedDateTime endDate = now();
        int midYear = (endDate.get(YEAR) + startDate.get(YEAR)) / 2; // need this to avoid overflow
        ZonedDateTime midDate = ZonedDateTime.parse(midYear + "-01-01T00:00:00Z");
        time = (ChronoUnit.NANOS.between(startDate, midDate)) / 100;
        time = time + (ChronoUnit.NANOS.between(midDate, endDate)) / 100;
        log.debug("Interval={}, Hex={}, now={}", time, Long.toHexString(time),
                now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)));

        mostSigBits = mostSigBits | Long.parseUnsignedLong("1000", 16); // version 1
        long temp = time;
        mostSigBits = mostSigBits | ((temp & Long.parseUnsignedLong("FFFFFFFF", 16)) << 32); // time_low
        temp = temp >>> 32;
        mostSigBits = mostSigBits | ((temp & Long.parseUnsignedLong("FFFF", 16)) << 16); // time_mid
        temp = temp >>> 16;
        mostSigBits = mostSigBits | (temp & Long.parseUnsignedLong("FFF", 16)); // time_mid

        log.debug("mostSigBits= {}", Long.toHexString(mostSigBits));
        log.debug("leastSigBits= {}", Long.toHexString(leastSigBits));

        UUID uu = new UUID(mostSigBits, leastSigBits);

        log.debug("node= {}", Long.toHexString(uu.node()));
        log.debug("version= {}", uu.version());
        log.debug("variant= {}", uu.variant());
        log.debug("Generated UUID= {}", uu.toString());
        return uu;
    }

    /**
     * Encodes a UUID into a 22 character URL compatible string.
     * <p>
     * From an example by
     * <a href="https://stackoverflow.com/">Tom Lobato</a>.
     *
     * @param uuid the UUID to encode
     * @return the 22 character string
     */
    public static String uuidToCompactString(UUID uuid) {
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
     * {@link #uuidToCompactString uuidToCompactString}.
     *
     * @param compact the 22 character string
     * @return the original UUID
     */
    public static UUID uuidFromCompactString(String compact) {
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
     * Creates a copy of the last-used old node identity for use with the new
     * identity.
     *
     * @param oldPath the old node identity folder
     * @param newPath the new node identity folder
     * @return true if successful
     */
    public static boolean copyFormerIdentity(File oldPath, File newPath) {
        String uniqueId = "-";
        try {
            uniqueId += ProfileManager.getDefault().getActiveProfile().getUniqueId();
        } catch (NullPointerException ex) { // because there is no active profile
            uniqueId += ProfileManager.createUniqueId();
            log.debug("created uniqueID \"{}\" because of there (probably) is no active profile", uniqueId);
        }
        List<String> formerIdList = NodeIdentity.formerIdentities();
        int listSize = formerIdList.size();
        if (listSize < 1) {
            log.debug("Unable to copy from a former identity; no former identities found.");
            return false;
        }
        log.debug("{} former identies found", listSize);
        for (int i = (listSize - 1); i >= 0; i--) {
            String theIdentity = formerIdList.get(i);
            log.debug("Trying to copy former identity {}, \"{}\"", i + 1, theIdentity);
            File theDir = new File(oldPath, theIdentity + uniqueId);
            if (theDir.exists()) {
                try {
                    log.info("Copying from old node \"{}\"", theDir.toString());
                    log.info("  to new node \"{}\"", newPath.toString());
                    FileUtil.copy(theDir, newPath);
                } catch (IOException ex) {
                    log.warn("Unable to copy \"{}\" to \"{}\"", theDir.toString(), newPath.toString());
                    return false;
                }
                return true;
            } else {
                log.warn("Non-existent old node \"{}\"", theDir);
            }
        }
        return false;
    }

    /**
     * @return the identity
     */
    public synchronized String getIdentity() {
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

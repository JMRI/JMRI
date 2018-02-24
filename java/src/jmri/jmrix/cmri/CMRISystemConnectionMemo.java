package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Manager.NameValidity;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialLightManager;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialSensorManager;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialTurnoutManager;
import jmri.jmrix.cmri.swing.CMRIComponentFactory;
import jmri.jmrix.swing.ComponentFactory;

/**
 * Minimal SystemConnectionMemo for C/MRI systems.
 *
 * @author Randall Wood
 */
public class CMRISystemConnectionMemo extends SystemConnectionMemo {

    public CMRISystemConnectionMemo() {
        this("C", CMRIConnectionTypeList.CMRI); // default to "C" prefix
    }

    public CMRISystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        register(); // registers general type
        InstanceManager.store(this, CMRISystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory for the GUI
        cf = new CMRIComponentFactory(this);
        InstanceManager.store(cf, ComponentFactory.class);

        log.debug("Created CMRISystemConnectionMemo");
    }

    private SerialTrafficController tc = null;
    ComponentFactory cf = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.cmri.serial.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s) {
        tc = s;
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     *
     * @return the traffic controller, created if needed
     */
    public SerialTrafficController getTrafficController() {
        if (tc == null) {
            setTrafficController(new SerialTrafficController());
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Get the user name for a valid system name.
     *
     * @param systemName the system name
     * @return "" (null string) if the system name is not valid or does not
     *         exist
     */
    public String getUserNameFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return "";
        }
        if (systemName.length() < offset + 1) {
            // not a valid system name for C/MRI
            return "";
        }
        switch (systemName.charAt(offset)) {
            case 'S':
                Sensor s = InstanceManager.sensorManagerInstance().getBySystemName(systemName);
                if (s != null) {
                    return s.getUserName();
                } else {
                    return "";
                }
            case 'T':
                Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
                if (t != null) {
                    return t.getUserName();
                } else {
                    return "";
                }
            case 'L':
                Light lgt = InstanceManager.lightManagerInstance().getBySystemName(systemName);
                if (lgt != null) {
                    return lgt.getUserName();
                } else {
                    return "";
                }
            default:
                break;
        }
        // not any known sensor, light, or turnout
        return "";
    }

    /**
     * Get the bit number from a C/MRI system name. Bits are numbered from 1.
     * Does not check whether that node is defined on current system.
     *
     * @param systemName the system name
     * @return 0 if an error is found
     */
    public int getBitFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
//            log.error("invalid system prefix in CMRI system name: {}", systemName); // fix test first
            return 0;
        }
        if (validSystemNameFormat(systemName, systemName.charAt(offset)) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            return 0;
        }
        // Find the beginning of the bit number field
        int k = 0;
        for (int i = offset + 1; (i < systemName.length()) && (k == 0); i++) {
            if (systemName.charAt(i) == 'B') {
                k = i + 1;
            }
        }
        int n; // bit number
        if (k == 0) {
            // here if 'B' not found, name must be CLnnxxx format
            int num;
            try {
                num = Integer.parseInt(systemName.substring(offset + 1));
            } catch (NumberFormatException e) {
                log.warn("invalid character in number field of system name: {}", systemName);
                return 0;
            }
            if (num > 0) {
                n = num - ((num / 1000) * 1000);
            } else {
                log.warn("invalid CMRI system name: {}", systemName);
                return 0;
            }
        } else { // k = position of "B" char in name
            try {
                n = Integer.parseInt(systemName.substring(k));
            } catch (NumberFormatException e) {
                log.warn("invalid character in bit number field of CMRI system name: {}", systemName);
                return 0;
            }
        }
        return n;
    }

    /**
     * Check and skip the System Prefix string on a system name.
     *
     * @param systemName the system name
     * @return offset of the 1st character past the prefix, or -1 if not valid
     *         for this connection
     */
    public int checkSystemPrefix(String systemName) {
        if (!systemName.startsWith(getSystemPrefix())) {
            return -1;
        }
        return getSystemPrefix().length();
    }

    /**
     * Test if a C/MRI output bit is free for assignment. Test is not performed
     * if the node address or bit number is invalid.
     *
     * @param nAddress the node address
     * @param bitNum   the output bit number
     * @return "" (empty string) if the specified output bit is free for
     *         assignment, else returns the system name of the conflicting
     *         assignment.
     */
    public String isOutputBitFree(int nAddress, int bitNum) {
        if ((nAddress < 0) || (nAddress > 127)) {
            log.warn("invalid node address in free bit test");
            return "";
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.warn("invalid bit number in free bit test");
            return "";
        }
        String sysName = makeSystemName("T", nAddress, bitNum);
        Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
        if (t != null) {
            return sysName;
        }
        String altName = convertSystemNameToAlternate(sysName);
        t = InstanceManager.turnoutManagerInstance().getBySystemName(altName);
        if (t != null) {
            return altName;
        }
        if (bitNum > 1) {
            sysName = makeSystemName("T", nAddress, bitNum - 1);
            t = InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
            if (t != null) {
                if (t.getNumberOutputBits() == 2) {
                    return sysName;
                }
            } else {
                altName = convertSystemNameToAlternate(sysName);
                t = InstanceManager.turnoutManagerInstance().getBySystemName(altName);
                if (t != null) {
                    if (t.getNumberOutputBits() == 2) {
                        return altName;
                    }
                }
            }
        }
        sysName = makeSystemName("L", nAddress, bitNum);
        Light lgt = InstanceManager.lightManagerInstance().getBySystemName(sysName);
        if (lgt != null) {
            return sysName;
        }
        altName = convertSystemNameToAlternate(sysName);
        lgt = InstanceManager.lightManagerInstance().getBySystemName(altName);
        if (lgt != null) {
            return altName;
        }
        // not assigned to a turnout or a light
        return "";
    }

    /**
     * Normalize a C/MRI system name.
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one C/MRI bit, by removing extra zeros inserted by the user.
     *
     * @param systemName the system name
     * @return "" (empty string) if the supplied system name does not have a
     *         valid format. Otherwise a normalized name is returned in the same
     *         format as the input name.
     */
    public String normalizeSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
//            log.error("invalid system prefix in CMRI system name: {}", systemName); // fix test first
            return "";
        }
        if (validSystemNameFormat(systemName, systemName.charAt(offset)) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName;
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            int num = Integer.parseInt(systemName.substring(offset + 1));
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            nName = systemName.substring(0, offset + 1) + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            int nAddress = Integer.parseInt(s);
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            nName = systemName.substring(0, offset + 1) + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        }
        return nName;
    }

    /**
     * Convert one format C/MRI system name to the alternate format.
     *
     * @param systemName the system name
     * @return "" (empty string) if the supplied system name does not have a
     *         valid format, or if there is no representation in the alternate
     *         naming scheme
     */
    public String convertSystemNameToAlternate(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            log.error("invalid system prefix in CMRI system name: {}", systemName);
            return "";
        }
        if (validSystemNameFormat(systemName, systemName.charAt(offset)) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName;
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            int num = Integer.parseInt(systemName.substring(offset + 1));
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            altName = systemName.substring(0, offset + 1) + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        } else {
            int nAddress = Integer.parseInt(s);
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            if (bitNum > 999) {
                // bit number is out-of-range for a CLnnnxxx address
                return "";
            }
            altName = systemName.substring(0, offset + 1) + Integer.toString((nAddress * 1000) + bitNum);
        }
        return altName;
    }

    /**
     * Validate system name format. Does not check whether that node is defined
     * on current system.
     *
     * @param systemName the system name
     * @param type       the device type
     * @return enum indicating current validity, which might be just as a prefix
     */
    public NameValidity validSystemNameFormat(String systemName, char type) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            log.error("invalid system prefix in CMRI system name: {}", systemName);
            return NameValidity.INVALID;
        }
        if (systemName.charAt(offset) != type) {
            log.error("invalid type character in CMRI system name: {}", systemName);
            return NameValidity.INVALID;
        }
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a CLnnnxxx pattern address
            int num;
            try {
                num = Integer.parseInt(systemName.substring(offset + 1));
            } catch (NumberFormatException e) {
                log.debug("invalid character in number field of CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 1) || (num >= 128000)) {
                log.debug("number field out of range in CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num - ((num / 1000) * 1000)) == 0) {
                log.debug("bit number not in range 1 - 999 in CMRI system name: {}", systemName);
                if (systemName.length() <= offset + 6) {
                    return NameValidity.VALID_AS_PREFIX_ONLY;
                    // may become valid by adding 1 or more digits > 0
                } else { // unless systemName.length() > offset + 6
                    return NameValidity.INVALID;
                }
            }
        } else {
            // This is a CLnBxxx pattern address
            if (s.length() == 0) {
                log.debug("no node address before 'B' in CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
            int num;
            try {
                num = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                log.debug("invalid character in node address field of CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 0) || (num >= 128)) {
                log.debug("node address field out of range in CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
            try {
                num = Integer.parseInt(systemName.substring(k));
            } catch (NumberFormatException e) {
                log.debug("invalid character in bit number field of CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if (num == 0) {
                return NameValidity.VALID_AS_PREFIX_ONLY;
                // may become valid by adding 1 or more digits > 0, all zeros will be removed later so total length irrelevant
            }
            if ((num < 1) || (num > 2048)) {
                log.debug("bit number field out of range in CMRI system name: {}", systemName);
                return NameValidity.INVALID;
            }
        } // TODO add format check for CLnn:xxx format
        return NameValidity.VALID;
    }

    /**
     * Test if a C/MRI input bit is free for assignment. Test is not performed
     * if the node address is invalid or bit number is greater than 2048.
     *
     * @param nAddress the address to test
     * @param bitNum   the bit number to tests
     * @return "" (empty string) if the specified input bit is free for
     *         assignment, else returns the system name of the conflicting
     *         assignment.
     */
    public String isInputBitFree(int nAddress, int bitNum) {
        if ((nAddress < 0) || (nAddress > 127)) {
            log.warn("invalid node address in free bit test");
            return "";
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.warn("invalid bit number in free bit test");
            return "";
        }
        String sysName = makeSystemName("S", nAddress, bitNum);
        Sensor s = InstanceManager.sensorManagerInstance().getBySystemName(sysName);
        if (s != null) {
            return sysName;
        }
        String altName = convertSystemNameToAlternate(sysName);
        s = InstanceManager.sensorManagerInstance().getBySystemName(altName);
        if (s != null) {
            return altName;
        }
        // not assigned to a sensor
        return "";
    }

    /**
     * Construct a C/MRI system name from type character, node address, and bit
     * number.
     * <p>
     * If the supplied character is not valid, or the node address is out of the
     * 0 - 127 range, or the bit number is out of the 1 - 2048 range, an error
     * message is logged and the null string "" is returned.
     *
     * @param type     the device type
     * @param nAddress the address to use
     * @param bitNum   the bit number to assign
     * @return a system name in the CLnnnxxx, CTnnnxxx, or CSnnnxxx format if
     *         the bit number is 1 - 999. If the bit number is 1000 - 2048, the
     *         system name is returned in the CLnnnBxxxx, CTnnnBxxxx, or
     *         CSnnnBxxxx format. The returned name is normalized.
     */
    public String makeSystemName(String type, int nAddress, int bitNum) {
        String nName = "";
        if ((!type.equals("S")) && (!type.equals("L")) && (!type.equals("T"))) {
            log.error("invalid type character proposed for system name");
            return nName;
        }
        if ((nAddress < 0) || (nAddress > 127)) {
            log.warn("invalid node address proposed for system name");
            return nName;
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.warn("invalid bit number proposed for system name");
            return nName;
        }
        if (bitNum < 1000) {
            nName = getSystemPrefix() + type + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            nName = getSystemPrefix() + type + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        }
        return nName;
    }

    /**
     * Get the serial node from a C/MRI system name.
     *
     * @param systemName the system name
     * @param tc         the controller for the node
     * @return the node or null if invalid systemName format or if the node is
     *         not found
     */
    public AbstractNode getNodeFromSystemName(String systemName, SerialTrafficController tc) {
        // get the node address
        int ua;
        ua = getNodeAddressFromSystemName(systemName);
        if (ua == -1) {
            return null;
        }
        return tc.getNodeFromAddress(ua);
    }

    /**
     * Validate C/MRI system name for configuration. Validates node number and
     * system prefix.
     *
     * @param systemName the system name to check
     * @param type       the device type
     * @param tc         the controller for the device
     * @return true if system name has a valid meaning in current configuration;
     *         otherwise false
     */
    public boolean validSystemNameConfig(String systemName, char type, SerialTrafficController tc) {
        if (validSystemNameFormat(systemName, type) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return false;
        }
        SerialNode node = (SerialNode) getNodeFromSystemName(systemName, tc);
        if (node == null) {
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName);
        if ((type == 'T') || (type == 'L')) {
            if ((bit <= 0) || (bit > (node.numOutputCards() * node.getNumBitsPerCard()))) {
                // The bit is not valid for this defined Serial node
                return false;
            }
        } else if (type == 'S') {
            if ((bit <= 0) || (bit > (node.numInputCards() * node.getNumBitsPerCard()))) {
                // The bit is not valid for this defined Serial node
                return false;
            }
        } else {
            log.error("Invalid type specification in validSystemNameConfig call");
            return false;
        }
        // System name has passed all tests
        return true;
    }

    /**
     * Get the serial node address from a C/MRI system name.
     * <p>
     * Nodes are numbered from 0 - 127. Does not check whether that node is
     * defined on current system.
     *
     * @param systemName the name containing the node
     * @return '-1' if invalid systemName format or if the node is not found.
     */
    public int getNodeAddressFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return -1;
        }
        if ((systemName.charAt(offset) != 'L') && (systemName.charAt(offset) != 'S') && (systemName.charAt(offset) != 'T')) {
            log.error("invalid character in header field of system name: {}", systemName);
            return -1;
        }
        String s = "";
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                noB = false;
            }
        }
        int ua;
        if (noB) {
            int num = Integer.parseInt(systemName.substring(offset + 1));
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.warn("invalid CMRI system name: {}", systemName);
                return -1;
            }
        } else {
            if (s.length() == 0) {
                log.warn("no node address before 'B' in CMRI system name: {}", systemName);
                return -1;
            } else {
                try {
                    ua = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    log.warn("invalid character in CMRI system name: {}", systemName);
                    return -1;
                }
            }
        }
        return ua;
    }

    /**
     * See {@link jmri.NamedBean#compareSystemNameSuffix} for background.
     * 
     * This is a common implementation for C/MRI Lights, Sensors and Turnouts
     * of the comparison method.
     */
    @CheckReturnValue
    public static int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2) {
        
        // extract node numbers and bit numbers
        int node1 = 0, node2 = 0, bit1, bit2;
        int t; // a temporary
        
        t = suffix1.indexOf("B");
        if (t < 0) t = suffix1.indexOf(":");
        if (t >= 0) {
            // alt format
            bit1 = Integer.parseInt(suffix1.substring(t+1));
            if (t>0) node1 = Integer.parseInt(suffix1.substring(0, t));
        } else {
            // std format
            int len = suffix1.length();
            bit1 = Integer.parseInt(suffix1.substring(Math.max(0, len-3)));
            if (len>3) node1 = Integer.parseInt(suffix1.substring(0, len-3));
        }
        
        t = suffix2.indexOf("B");
        if (t < 0) t = suffix2.indexOf(":");
        if (t >= 0) {
            // alt format
            bit2 = Integer.parseInt(suffix2.substring(t+1));
            if (t>0) node2 = Integer.parseInt(suffix2.substring(0, t));
        } else {
            // std format
            int len = suffix2.length();
            bit2 = Integer.parseInt(suffix2.substring(Math.max(0, len-3)));
            if (len>3) node2 = Integer.parseInt(suffix2.substring(0, len-3));
        }
        
        if (node1 != node2 ) return Integer.signum(node1-node2);
        return Integer.signum(bit1-bit2);
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        return null; // nothing by default
    }

    /**
     * Configure the common managers for CMRI connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        InstanceManager.setSensorManager(getSensorManager());
        getTrafficController().setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(getTurnoutManager());

        InstanceManager.setLightManager(getLightManager());
    }

    protected SerialTurnoutManager turnoutManager;

    public SerialTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new SerialTurnoutManager(this);
        }
        return turnoutManager;
    }

    protected SerialSensorManager sensorManager;

    public SerialSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new SerialSensorManager(this);
        }
        return sensorManager;
    }

    protected SerialLightManager lightManager;

    public SerialLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new SerialLightManager(this);
        }
        return lightManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, CMRISystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, ComponentFactory.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, SerialTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, SerialLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, SerialSensorManager.class);
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CMRISystemConnectionMemo.class);

}

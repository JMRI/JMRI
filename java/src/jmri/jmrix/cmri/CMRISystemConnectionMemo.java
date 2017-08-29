package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.cmri.serial.*;

/**
 * Minimal SystemConnectionMemo for C/MRI systems.
 *
 * @author Randall Wood
 */
public class CMRISystemConnectionMemo extends SystemConnectionMemo {

    /**
     * Public static method to the user name for a valid system name.
     *
     * @return "" (null string) if the system name is not valid or does not exist
     */
    public String getUserNameFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return "";
        }
        if (systemName.length() < 3) {
            // not a valid system name for C/MRI
            return "";
        }
        if (systemName.charAt(offset) == 'S') {
            Sensor s = null;
            s = InstanceManager.sensorManagerInstance().getBySystemName(systemName);
            if (s != null) {
                return s.getUserName();
            } else {
                return "";
            }
        } else if (systemName.charAt(offset) == 'T') {
            Turnout t = null;
            t = InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
            if (t != null) {
                return t.getUserName();
            } else {
                return "";
            }
        } else if (systemName.charAt(offset) == 'L') {
            Light lgt = null;
            lgt = InstanceManager.lightManagerInstance().getBySystemName(systemName);
            if (lgt != null) {
                return lgt.getUserName();
            } else {
                return "";
            }
        }
        // not any known sensor, light, or turnout
        return "";
    }

    /**
     * Public static method to parse a C/MRI system name and return the bit
     * number. Notes: Bits are numbered from 1.
     *
     * @return 0 if an error is found.
     * Does not check whether that node is defined on current system.
     */
    public int getBitFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return 0;
        }
        if ((systemName.charAt(offset) != 'L') && (systemName.charAt(offset) != 'S') && (systemName.charAt(offset) != 'T')) {
            log.error("illegal character in header field of system name: " + systemName);
            return 0;
        }
        // Find the beginning of the bit number field
        int k = 0;
        for (int i = offset + 1; (i < systemName.length()) && (k == 0); i++) {
            if (systemName.charAt(i) == 'B') {
                k = i + 1;
            }
        }
        int n = 0;
        if (k == 0) {
            // here if 'B' not found, name must be CLnnxxx format
            int num;
            try {
                num = Integer.valueOf(systemName.substring(offset + 1)).intValue();
            } catch (Exception e) {
                log.error("illegal character in number field of system name: " + systemName);
                return 0;
            }
            if (num > 0) {
                n = num - ((num / 1000) * 1000);
            } else {
                log.error("invalid CMRI system name: " + systemName);
                return 0;
            }
        } else {
            try {
                n = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.error("illegal character in bit number field of CMRI system name: " + systemName);
                return 0;
            }
        }
        return n;
    }

    /**
     * Public static method to check and skip the System Prefix
     * string on a system name.
     *
     * @return offset of the 1st character past the prefix, or -1 if not valid
     * for this connection
     */
    public int checkSystemPrefix(String systemName) {
        if (!systemName.startsWith(getSystemPrefix())) {
            return -1;
        }
        return getSystemPrefix().length();
    }

    /**
     * Public static method to test if a C/MRI output bit is free for assignment.
     * Test is not performed if the node address or bit number are illegal.
     *
     * @return "" (empty string) if the specified output bit is free for
     * assignment, else returns the system name of the conflicting assignment.
     */
    public String isOutputBitFree(int nAddress, int bitNum) {
        if ((nAddress < 0) || (nAddress > 127)) {
            log.error("illegal node adddress in free bit test");
            return "";
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.error("illegal bit number in free bit test");
            return "";
        }
        Turnout t = null;
        String sysName = "";
        sysName = makeSystemName("T", nAddress, bitNum);
        t = InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
        if (t != null) {
            return sysName;
        }
        String altName = "";
        altName = convertSystemNameToAlternate(sysName);
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
                if (altName != null) {
                    t = InstanceManager.turnoutManagerInstance().getBySystemName(altName);
                    if (t != null) {
                        if (t.getNumberOutputBits() == 2) {
                            return altName;
                        }
                    }
                }
            }
        }
        Light lgt = null;
        sysName = makeSystemName("L", nAddress, bitNum);
        lgt = InstanceManager.lightManagerInstance().getBySystemName(sysName);
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
     * Public static method to normalize a C/MRI system name.
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one C/MRI bit, by removing extra zeros inserted by the user.
     *
     * @return "" (empty string) if the supplied system name does not have a valid format.
     * Otherwise a normalized name is returned in the same format as the input name.
     */
    public String normalizeSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return "";
        }
        if (!validSystemNameFormat(systemName, systemName.charAt(offset))) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName = "";
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
            int num = Integer.valueOf(systemName.substring(offset+1)).intValue();
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            nName = systemName.substring(0, offset + 1) + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            nName = systemName.substring(0, offset + 1) + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        }
        return nName;
    }

    /**
     * Public static method to convert one format C/MRI system name for the
     * alternate format.
     *
     * @return "" (empty string) if the supplied system name does not have a valid
     * format, or if there is no representation in the alternate naming scheme
     */
    public String convertSystemNameToAlternate(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return "";
        }
        if (!validSystemNameFormat(systemName, systemName.charAt(offset))) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName = "";
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
            int num = Integer.valueOf(systemName.substring(offset + 1)).intValue();
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            altName = systemName.substring(0, offset + 1) + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        } else {
            int nAddress = Integer.valueOf(s).intValue();
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
     * Public static method to validate system name format.
     * Does not check whether that node is defined on current system.
     *
     * @return 'true' if system name has a valid format,
     * else returns 'false'.
     */
    public boolean validSystemNameFormat(String systemName, char type) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            log.error("illegal system prefix in CMRI system name: " + systemName);
            return false;
        }

        if (systemName.charAt(offset) != type) {
            log.error("illegal type character in CMRI system name: " + systemName);
            return false;
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
            // This is a CLnnnxxx address
            int num;
            try {
                num = Integer.valueOf(systemName.substring(offset+1)).intValue();
            } catch (Exception e) {
                log.error("illegal character in number field of CMRI system name: " + systemName);
                return false;
            }
            if ((num < 1) || (num >= 128000)) {
                log.warn("number field out of range in CMRI system name: " + systemName);
                return false;
            }
            if ((num - ((num / 1000) * 1000)) == 0) {
                log.warn("bit number not in range 1 - 999 in CMRI system name: " + systemName);
                return false;
            }
        } else {
            if (s.length() == 0) {
                log.warn("no node address before 'B' in CMRI system name: " + systemName);
                return false;
            }
            int num;
            try {
                num = Integer.valueOf(s).intValue();
            } catch (Exception e) {
                log.warn("illegal character in node address field of CMRI system name: " + systemName);
                return false;
            }
            if ((num < 0) || (num >= 128)) {
                log.warn("node address field out of range in CMRI system name: " + systemName);
                return false;
            }
            try {
                num = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.warn("illegal character in bit number field of CMRI system name: " + systemName);
                return false;
            }
            if ((num < 1) || (num > 2048)) {
                log.warn("bit number field out of range in CMRI system name: " + systemName);
                return false;
            }
        }
        return true;
    }

    /**
     * Public static method to test if a C/MRI input bit is free for assignment
     * Test is not performed if the node address is illegal or bit number is
     * greater than 2048.
     *
     * @return "" (empty string) if the specified input bit is free for
     * assignment, else returns the system name of the conflicting assignment.
     */
    public String isInputBitFree(int nAddress, int bitNum) {
        if ((nAddress < 0) || (nAddress > 127)) {
            log.error("illegal node adddress in free bit test");
            return "";
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.error("illegal bit number in free bit test");
            return "";
        }
        Sensor s = null;
        String sysName = "";
        sysName = makeSystemName("S", nAddress, bitNum);
        s = InstanceManager.sensorManagerInstance().getBySystemName(sysName);
        if (s != null) {
            return sysName;
        }
        String altName = "";
        altName = convertSystemNameToAlternate(sysName);
        s = InstanceManager.sensorManagerInstance().getBySystemName(altName);
        if (s != null) {
            return altName;
        }
        // not assigned to a sensor
        return "";
    }

    /**
     * Public static method to construct a C/MRI system name from type
     * character, node address, and bit number.
     * <p>
     * If the supplied character is not valid, or the node address is out of the
     * 0 - 127 range, or the bit number is out of the 1 - 2048 range, an error
     * message is logged and the null string "" is returned.
     *
     * @return a system name in the CLnnnxxx, CTnnnxxx, or CSnnnxxx
     * format if the bit number is 1 - 999. If the bit number is 1000 - 2048,
     * the system name is returned in the CLnnnBxxxx, CTnnnBxxxx, or CSnnnBxxxx
     * format. The returned name is normalized.
     */
    public String makeSystemName(String type, int nAddress, int bitNum) {
        String nName = "";
        if ((!type.equals("S")) && (!type.equals("L")) && (!type.equals("T"))) {
            log.error("illegal type character proposed for system name");
            return nName;
        }
        if ((nAddress < 0) || (nAddress > 127)) {
            log.error("illegal node adddress proposed for system name");
            return nName;
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.error("illegal bit number proposed for system name");
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
     * Public static method to parse a C/MRI system name and return the Serial
     * Node.
     *
     * @return 'null' if illegal systemName format or if the node is not found
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
     * Public static method to validate C/MRI system name for configuration.
     * Does validate node number and system prefix.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false'.
     */
    public boolean validSystemNameConfig(String systemName, char type, SerialTrafficController tc) {
        if (!validSystemNameFormat(systemName, type)) {
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
     * Public static method to parse a C/MRI system name and return the Serial
     * Node Address
     * <p>
     * Nodes are numbered from 0 - 127. Does not check
     * whether that node is defined on current system.
     *
     * @return '-1' if illegal systemName format or if the node is not found.
     */
    public int getNodeAddressFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) {
            return -1;
        }
        if ((systemName.charAt(offset) != 'L') && (systemName.charAt(offset) != 'S') && (systemName.charAt(offset) != 'T')) {
            log.error("illegal character in header field of system name: " + systemName);
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
            int num = Integer.valueOf(systemName.substring(offset+1)).intValue();
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid CMRI system name: " + systemName);
                return -1;
            }
        } else {
            if (s.length() == 0) {
                log.error("no node address before 'B' in CMRI system name: " + systemName);
                return -1;
            } else {
                try {
                    ua = Integer.parseInt(s);
                } catch (Exception e) {
                    log.error("illegal character in CMRI system name: " + systemName);
                    return -1;
                }
            }
        }
        return ua;
    }

    private SerialTrafficController tc = null;

    public CMRISystemConnectionMemo() {
        this("C", CMRIConnectionTypeList.CMRI);
    }
    
    public CMRISystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);
        
        register(); // registers general type
        InstanceManager.store(this, CMRISystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory for the GUI
        InstanceManager.store(cf = new jmri.jmrix.cmri.swing.CMRIComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.cmri.serial.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s){
        tc = s;
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     */
    public SerialTrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController());
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
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
        } else {
            return false; // nothing, by default
        }
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
     * Configure the common managers for CMRI connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {
        InstanceManager.setSensorManager(
                getSensorManager());
        getTrafficController().setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setLightManager(
                getLightManager());
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
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.cmri.serial.SerialTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.cmri.serial.SerialLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.cmri.serial.SerialSensorManager.class);
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CMRISystemConnectionMemo.class.getName());

}

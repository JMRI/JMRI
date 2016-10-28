package jmri.jmrix;

import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains lists equipment manufacturers that JMRI Supports.
 * <p>
 * Since JMRI 3.4.5, {@link jmri.jmrix.ConnectionTypeList} subclasses loadable
 * by a {@link java.util.ServiceLoader} are used for this purpose.</p>
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @deprecated Since 3.4.5 ensure the {@link jmri.jmrix.ConnectionTypeList} is
 * complete and registered as a service provider.
 */
@Deprecated
public class DCCManufacturerList {

    private final static Logger log = LoggerFactory.getLogger(DCCManufacturerList.class);

    /**
     * Get a list of system manufacturers.
     *
     * @return The list of system manufacturers
     * @deprecated use
     * {@link jmri.jmrix.ConnectionConfigManager#getConnectionManufacturers()}.
     */
    @Deprecated
    public static String[] getSystemNames() {
        return InstanceManager.getDefault(ConnectionConfigManager.class).getConnectionManufacturers();
    }

    /**
     * Get a list of class names that handle the given system.
     *
     * @param System the manufacturer name
     * @return The list of class names
     * @deprecated use
     * {@link jmri.jmrix.ConnectionConfigManager#getConnectionTypes(java.lang.String)}.
     */
    @Deprecated
    public static String[] getConnectionList(String System) {
        return InstanceManager.getDefault(ConnectionConfigManager.class).getConnectionTypes(System);
    }

    /**
     * Returns the name of a system given its connection's first character if
     * the system does not use a {@link jmri.jmrix.SystemConnectionMemo} to
     * maintain that information.
     *
     * Note: No system should not be using a SystemConnectionMemo at this point.
     *
     * @param a A character representing a connection type
     * @return A system name
     * @deprecated Without replacement. Use
     * {@link jmri.util.ConnectionNameFromSystemName#getConnectionName(java.lang.String)}
     * for equivalent functionality.
     */
    @Deprecated
    public static String getDCCSystemFromType(char a) {
        log.error("Called with parameter \"{}\". Please report to JMRI developers at https://github.com/JMRI/JMRI/issues/new", a, new Exception());
        if (a == 'I') {
            return "Internal";
        } else if (a == 'A') {
            return "Acela";
        } else if (a == 'B') {
            return "DCC Direct";
        } else if (a == 'C') {
            return "C/MRI";
        } else if (a == 'D') {
            return "SRCP";
        } else if (a == 'E') {
            return "EasyDCC";
        } else if (a == 'F') {
            return "RFID";
        } else if (a == 'G') {
            return "Grapevine";
        } else if (a == 'K') {
            return "Maple";
        } else if (a == 'L') {
            return "LocoNet";
        } else if (a == 'N') {
            return "NCE";
        } else if (a == 'O') {
            return "Oak Tree";
        } else if (a == 'M') {
            return "OpenLCB";  // duplicates MERG?
            //} else if (a == 'M') {
            //    return "MERG";
        } else if (a == 'P') {
            return "PowerLine";
        } else if (a == 'Q') {
            return "QSI";
        } else if (a == 'R') {
            return "RPS";
        } else if (a == 'S') {
            return "Sprog";
        } else if (a == 'T') {
            return "Lionel TMCC";
        } else if (a == 'U') {
            return "ECoS";
        } else if (a == 'V') {
            return "SECSI";
        } else if (a == 'W') {
            return "Wangrow";
        } else if (a == 'X') {
            return "XpressNet";
        } else if (a == 'Z') {
            return "Zimo";
        }
        return "Unknown";
    }

    /**
     * Returns the connection prefix given a system's name if the system does
     * not use a {@link jmri.jmrix.SystemConnectionMemo} to maintain that
     * information.
     *
     * Note: No system should not be using a SystemConnectionMemo at this point.
     *
     * @param a the system manufacturer
     * @return a default system prefix
     * @deprecated Without replacement. Use
     * {@link jmri.util.ConnectionNameFromSystemName#getPrefixFromName(java.lang.String)}
     * for equivalent functionality.
     */
    @Deprecated
    public static char getTypeFromDCCSystem(String a) {
        log.error("Called with parameter \"{}\". Please report to JMRI developers at https://github.com/JMRI/JMRI/issues/new", a, new Exception());
        if (a.equals("Internal")) {
            return 'I';
        } else if (a.equals("Acela")) {
            return 'A';
        } else if (a.equals("DCC Direct")) {
            return 'B';
        } else if (a.equals("C/MRI")) {
            return 'C';
        } else if (a.equals("SRCP")) {
            return 'D';
        } else if (a.equals("EasyDCC")) {
            return 'E';
        } else if (a.equals("RFID")) {
            return 'F';
        } else if (a.equals("Grapevine")) {
            return 'G';
        } else if (a.equals("Maple")) {
            return 'K';
        } else if (a.equals("LocoNet")) {
            return 'L';
        } else if (a.equals("MERG")) {
            return 'M';
        } else if (a.equals("OpenLCB")) {
            return 'M';
        } else if (a.equals("NCE")) {
            return 'N';
        } else if (a.equals("Oak Tree")) {
            return 'O';
        } else if (a.equals("PowerLine")) {
            return 'P';
        } else if (a.equals("QSI")) {
            return 'Q';
        } else if (a.equals("RPS")) {
            return 'R';
        } else if (a.equals("Sprog")) {
            return 'S';
        } else if (a.equals("Lionel TMCC")) {
            return 'T';
        } else if (a.equals("ECoS")) {
            return 'U';
        } else if (a.equals("SECSI")) {
            return 'V';
        } else if (a.equals("Wangrow")) {
            return 'W';
        } else if (a.equals("XpressNet")) {
            return 'X';
        } else if (a.equals("Zimo")) {
            return 'Z';
        }
        return '\0';
    }
}

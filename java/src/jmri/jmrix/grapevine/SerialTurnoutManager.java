package jmri.jmrix.grapevine;

import java.util.Locale;
import jmri.JmriException;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Grapevine systems.
 * <p>
 * System names are "GTnnn", where G is the (multichar) system connection prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager(GrapevineSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GrapevineSystemConnectionMemo getMemo() {
        return (GrapevineSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        String prefix = getSystemPrefix();
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, prefix);
        if (sName.equals("")) {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return null;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName, prefix);
        t = getBySystemName(altName);
        if (t != null) {
            return null;
        }
        // create the turnout
        t = new SerialTurnout(sName, userName, getMemo());

        // does system name correspond to configured hardware
        if (!SerialAddress.validSystemNameConfig(sName, 'T', getMemo().getTrafficController())) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '{}' refers to an undefined Serial Node.", sName);
        }
        log.debug("new turnout {} for prefix {}", systemName, prefix);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false; // Turnout address format is more than a simple number.
    }

    /** {@inheritDoc} */
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        String tmpSName = prefix + "T" + curAddress;

        if (curAddress.contains(":")) {
            // Address format passed is in the form of node:cardOutput or node:card:address
            int separator = curAddress.indexOf(":");
            try {
                nNode = Integer.parseInt(curAddress.substring(0, separator));
                int nxSeparator = curAddress.indexOf(":", separator + 1);
                if (nxSeparator == -1) {
                    //Address has been entered in the format node:cardOutput
                    bitNum = Integer.parseInt(curAddress.substring(separator + 1));
                } else {
                    //Address has been entered in the format node:card:output
                    nCard = Integer.parseInt(curAddress.substring(separator + 1, nxSeparator)) * 100;
                    bitNum = Integer.parseInt(curAddress.substring(nxSeparator + 1));
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} Hardware Address to a number", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
            }
            tmpSName = prefix + "T" + nNode + (nCard + bitNum);
        } else {
            bitNum = SerialAddress.getBitFromSystemName(tmpSName, prefix);
            nNode = SerialAddress.getNodeAddressFromSystemName(tmpSName, prefix);
            tmpSName = prefix + "T" + nNode + bitNum;
            log.debug("createSystemName {}", tmpSName);
        }
        return (tmpSName);
    }

    int nCard = 0;
    int bitNum = 0;
    int nNode = 0;

    /**
     * Return the next valid free turnout hardware address.
     */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {

        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            throw ex;
        }

        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if (t == null) {
            return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
            //return ""+nNode+(nCard+bitNum);
        }

        // The Number of Output Bits of the previous turnout will help determine the next
        // valid address.
        bitNum = bitNum + t.getNumberOutputBits();
        // Check to determine if the systemName is in use, return null if it is,
        // otherwise return the next valid address.
        tmpSName = prefix + "T" + nNode + (nCard + bitNum);
        t = getBySystemName(tmpSName);
        if (t != null) {
            for (int x = 1; x < 10; x++) {
                bitNum = bitNum + t.getNumberOutputBits();
                tmpSName = prefix + "T" + nNode + (nCard + bitNum);
                t = getBySystemName(tmpSName);
                if (t == null) {
                    return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
                }
            }
            return null;
        } else {
            return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return SerialAddress.validateSystemNameFormat(name, this, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return SerialAddress.validSystemNameFormat(systemName, typeLetter(), getSystemPrefix());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}

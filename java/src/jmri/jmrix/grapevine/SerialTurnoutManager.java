package jmri.jmrix.grapevine;

import java.util.Locale;
import javax.annotation.Nonnull;
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
    @Nonnull
    public GrapevineSystemConnectionMemo getMemo() {
        return (GrapevineSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String prefix = getSystemPrefix();
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, prefix);
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Cannot create System Name from " + systemName);
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return t;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName, prefix);
        t = getBySystemName(altName);
        if (t != null) {
            return t;
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
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false; // Turnout address format is more than a simple number.
    }

    /** {@inheritDoc} */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
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

    private int nCard = 0;
    private int bitNum = 0;
    private int nNode = 0;

    /**
     * Return the next valid free turnout hardware address.
     */
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {

        String tmpSName = createSystemName(curAddress, prefix);

        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if (t == null && !ignoreInitialExisting) {
            return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
            //return ""+nNode+(nCard+bitNum);
        }

        // The Number of Output Bits of the previous turnout will help determine the next
        // valid address.
        int increment = ( t==null ? 1 : t.getNumberOutputBits());
        for (int x = 0; x < 10; x++) {
            bitNum = bitNum + increment;
            tmpSName = prefix + "T" + nNode + (nCard + bitNum);
            t = getBySystemName(tmpSName);
            if (t == null) {
                return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
            }
        }
        throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,tmpSName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return SerialAddress.validateSystemNameFormat(name, this, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
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

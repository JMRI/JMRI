package jmri.jmrix.bidib;

import java.util.ArrayList;
import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for BiDiB systems.
 * <p>
 * System names are "BTnnn", where B is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public class BiDiBTurnoutManager extends jmri.managers.AbstractTurnoutManager {// implements EasyDccListener {

    // Whether we accumulate partially loaded turnouts in pendingTurnouts.
    private boolean isLoading = false;
    // Turnouts that are being loaded from XML.
    private final ArrayList<BiDiBTurnout> pendingTurnouts = new ArrayList<>();


    /**
     * Create an new BiDiB TurnoutManager.
     *
     * @param memo the SystemConnectionMemo for this connection (contains the prefix string needed to parse names)
     */
    public BiDiBTurnoutManager(BiDiBSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBSystemConnectionMemo getMemo() {
        return (BiDiBSystemConnectionMemo) memo;
    }

    /**
     * Create a new Turnout based on the system name.
     * Assumes calling method has checked that a Turnout with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format.
     */
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        log.trace("createNewTurnout {} - {}", systemName, userName);
        //String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(systemName);
        } catch (IllegalArgumentException e) {
            log.error("failed to validate:", e);
            throw e;
        }
        
        
        BiDiBTurnout t;
        t = new BiDiBTurnout(systemName, this);
        t.setUserName(userName);

        synchronized (pendingTurnouts) {
            if (isLoading) {
                pendingTurnouts.add(t);
            } else {
                t.finishLoad();
            }
        }

        return t;
        //return null;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created turnouts until finishLoad because the type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        synchronized (pendingTurnouts) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all turnouts are instantiated
     * and their config is read in. We use this hook to finalize the construction of the
     * objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        log.info("Turnout manager : finish load");
        synchronized (pendingTurnouts) {
            pendingTurnouts.forEach((t) -> {
                t.finishLoad();
            });
            pendingTurnouts.clear();
            isLoading = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException { //TODO some validation? Throw exception then? - see parent class
        log.trace("createSystemName from {} - {}", curAddress, prefix);
        try {
            int i = 1;
            int curNum = Integer.parseInt(curAddress);
            for (Turnout t : getNamedBeanSet()) {
                //log.trace("turnout: {}/{} {}", i, curNum, t.getSystemName());
                if (i++ == curNum) {
                    return t.getSystemName();
                }
            }
        } catch (java.lang.NumberFormatException ex) {
            throw new JmriException("Hardware Address passed "+curAddress+" should be a number");
        }
        return prefix + typeLetter() + curAddress;
    }

/* obsolete
    /** {@inheritDoc} * /
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) {
        log.trace("getNextValidAddress from {} - {}", curAddress, prefix);
        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), null, "", true, false);
            return null;
        }

        Turnout t = getBySystemName(tmpSName);
        if (t == null && !ignoreInitialExisting) {
            return curAddress;
        }
        return null;
    }
 */
    
    /**
     * Public method to validate system name format.
     *
     * @param systemName system name
     * @return VALID if system name has a valid format, else return INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        log.trace("validSystemNameFormat");
        // TODO!!
        //return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
        return NameValidity.VALID; //TODO
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        log.trace("validateSystemNameFormat: name: {}, typeLetter: {}", name, typeLetter());
        validateSystemNamePrefix(name, locale);
        //validateAddressFormat(name.substring(getSystemNamePrefix().length()));
        if (!BiDiBAddress.isValidSystemNameFormat(name, typeLetter(), getMemo())) {
            throw new jmri.NamedBean.BadSystemNameException(Locale.getDefault(), "InvalidSystemName",name);
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBTurnoutManager.class);

}

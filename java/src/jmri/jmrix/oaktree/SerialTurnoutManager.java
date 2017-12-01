package jmri.jmrix.oaktree;

import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Oak Tree systems
 * <P>
 * System names are "OTnnn", where nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
  */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    OakTreeSystemConnectionMemo _memo = null;
    protected String prefix = "O";

    public SerialTurnoutManager(OakTreeSystemConnectionMemo memo) {
        _memo = memo;
        prefix = getSystemPrefix();
    }

    /**
     * Return the Oak Tree system prefix
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();

    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
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
        t = new SerialTurnout(sName, userName, _memo);

        // does system name correspond to configured hardware
        if (!SerialAddress.validSystemNameConfig(sName, 'T', _memo)) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '{}' refers to an undefined Serial Node.", sName);
        }
        return t;
    }

    /**
     * Public method to validate system name format.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'T', prefix));
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Deprecated
    static public SerialTurnoutManager instance() {
        return null;
    }


    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}

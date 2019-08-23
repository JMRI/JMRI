package jmri.jmrix.easydcc;

import java.util.Locale;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for EasyDcc systems.
 * <p>
 * System names are "ETnnn", where E is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccTurnoutManager extends jmri.managers.AbstractTurnoutManager implements EasyDccListener {

    private EasyDccTrafficController trafficController = null;
    public final static int MAX_ACC_DECODER_ADDRESS = 511;

    /**
     * Create an new EasyDCC TurnoutManager.
     *
     * @param memo the SystemConnectionMemo for this connection (contains the prefix string needed to parse names)
     */
    public EasyDccTurnoutManager(EasyDccSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        trafficController = memo.getTrafficController();
        // listen for turnout creation
        trafficController.addEasyDccListener(this);
        log.debug("EasyDCC TurnoutManager prefix={}", getSystemPrefix());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EasyDccSystemConnectionMemo getMemo() {
        return (EasyDccSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        t = new EasyDccTurnout(getSystemPrefix(), addr, getMemo());
        t.setUserName(userName);

        return t;
    }

    /**
     * Listeners for messages from the command station.
     */
    @Override
    public void message(EasyDccMessage m) {
        log.debug("message received unexpectedly: {}", m.toString());
    }

    // Listen for status changes from EasyDcc system
    @Override
    public void reply(EasyDccReply r) {
        // There isn't anything meaningful coming back at this time.
        log.debug("reply received unexpectedly: {}", r.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, MAX_ACC_DECODER_ADDRESS, locale);
    }

    /**
     * Get the bit address from the system name.
     * @param systemName a valid LocoNet-based Turnout System Name
     * @return the turnout number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        try {
            validateSystemNameFormat(systemName, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            return 0;
        }
        return Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManager.class);

}

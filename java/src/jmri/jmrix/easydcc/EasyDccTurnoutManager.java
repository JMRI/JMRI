package jmri.jmrix.easydcc;

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

    EasyDccSystemConnectionMemo _memo = null;
    private String prefix = "E";
    private EasyDccTrafficController trafficController = null;
    public final static int MAX_ACC_DECODER_ADDRESS = 511;

    public EasyDccTurnoutManager() {
        log.debug("EasyDCC TurnoutManager null");
    }

    /**
     * Create an new EasyDCC TurnoutManager.
     *
     * @param memo the SystemConnectionMemo for this connection (contains the prefix string needed to parse names)
     */
    public EasyDccTurnoutManager(EasyDccSystemConnectionMemo memo) {
        _memo = memo;
        prefix = memo.getSystemPrefix();
        // connect to the TrafficManager
        trafficController = memo.getTrafficController();
        // listen for turnout creation
        trafficController.addEasyDccListener(this);
        log.debug("EasyDCC TurnoutManager prefix={}", prefix);
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new EasyDccTurnout(prefix, addr, _memo);
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
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(prefix + typeLetter()))) {
            // here if an illegal EasyDCC turnout system name
            log.error("illegal character in header field of EasyDCC turnout system name: {} prefix {} type {}",
                    systemName, getSystemPrefix(), typeLetter());
            return (0);
        }
        // name must be in the ETnnnnn format (E is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length()));
        } catch (Exception e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid EasyDCC turnout system name: {}", systemName);
            return (0);
        } else if (num > MAX_ACC_DECODER_ADDRESS) {
            log.debug("bit number out of range in EasyDCC turnout system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
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

package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the LocoNet-specific Sensor implementation.
 * System names are "LSnnn", where L is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnSensorManager extends jmri.managers.AbstractSensorManager implements LocoNetListener {

    public LnSensorManager(LnTrafficController tc, String prefix) {
        this.prefix = prefix;
        if (tc == null) {
            log.error("SensorManager Created, yet there is no Traffic Controller");
            return;
        }
        this.tc = tc;
        // ctor has to register for LocoNet events
        tc.addLocoNetListener(~0, this);

        // start the update sequence. Until JMRI 2.9.4, this waited
        // until files have been read, but starts automatically
        // since 2.9.5 for multi-system support.
        updateAll();
    }

    protected LnTrafficController tc;
    protected String prefix = "L";

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        Thread t = thread;
        if (t != null) {
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException ex) {
                log.warn("dispose interrupted");
            } finally {
                thread = null;
            }
        }
        super.dispose();
    }

    // LocoNet-specific methods
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        return new LnSensor(systemName, userName, tc, prefix);
    }

    // listen for sensors, creating them as needed
    @Override
    public void message(LocoNetMessage l) {
        // parse message type
        LnSensorAddress a;
        switch (l.getOpCode()) {
            case LnConstants.OPC_INPUT_REP:                /* page 9 of LocoNet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                a = new LnSensorAddress(sw1, sw2, prefix);
                log.debug("INPUT_REP received with address {}", a);
                break;
            default:  // here we didn't find an interesting command
                return;
        }
        // reach here for LocoNet sensor input command; make sure we know about this one
        String s = a.getNumericAddress();
        if (null == getBySystemName(s)) {
            // need to store a new one
            if (log.isDebugEnabled()) {
                log.debug("Create new LnSensor as {}", s);
            }
            LnSensor ns = (LnSensor) newSensor(s, null);
            ns.message(l);  // have it update state
        }
    }

    volatile LnSensorUpdateThread thread;

    /**
     * Requests status updates from all layout sensors.
     */
    @Override
    public void updateAll() {
        if (!busy) {
            setUpdateBusy();
            thread = new LnSensorUpdateThread(this, tc);
            thread.setName("LnSensorUpdateThread");
            thread.start();
        }
    }

    /**
     * Set Route busy when commands are being issued to Route turnouts.
     */
    public void setUpdateBusy() {
        busy = true;
    }

    /**
     * Set Route not busy when all commands have been issued to Route
     * turnouts.
     */
    public void setUpdateNotBusy() {
        busy = false;
    }

    private boolean busy = false;

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (curAddress.contains(":")) {
            int board = 0;
            int channel = 0;
            // Address format passed is in the form of board:channel or T:turnout address
            int seperator = curAddress.indexOf(":");
            boolean turnout = false;
            if (curAddress.substring(0, seperator).toUpperCase().equals("T")) {
                turnout = true;
            } else {
                try {
                    board = Integer.parseInt(curAddress.substring(0, seperator));
                } catch (NumberFormatException ex) {
                    log.error("Unable to convert '{}' into the cab and channel format of nn:xx", curAddress); // NOI18N
                    throw new JmriException("Hardware Address passed should be a number"); // NOI18N
                }
            }
            try {
                channel = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                log.error("Unable to convert '{}' into the cab and channel format of nn:xx", curAddress); // NOI18N
                throw new JmriException("Hardware Address passed should be a number"); // NOI18N
            }
            if (turnout) {
                iName = 2 * (channel - 1) + 1;
            } else {
                iName = 16 * board + channel - 16;
            }
        } else {
            // Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert '{}' Hardware Address to a number", curAddress); // NOI18N
                throw new JmriException("Hardware Address passed should be a number"); // NOI18N
            }
        }
        return prefix + typeLetter() + iName;
    }

    int iName;

    /**
     * Get the bit address from the system name.
     * @param systemName a valid LocoNet-based Sensor System Name
     * @return the sensor number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "S"))) {
            // here if an illegal LocoNet Light system name
            log.error("illegal character in header field of loconet sensor system name: {}", systemName);
            return (0);
        }
        // name must be in the LSnnnnn format (L is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                        getSystemPrefix().length() + 1, systemName.length()
                    ));
        } catch (Exception e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid loconet sensor system name: {}", systemName);
            return (0);
        } else if (num > 4096) {
            log.debug("bit number out of range in loconet sensor system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false); // I18N
            return null;
        }

        // Check to determine if the systemName is in use, return null if it is,
        // otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            // feedback when next 10 addresses are also in use
            log.warn("10 hardware addresses starting at {} already in use. No new LocoNet Sensors added", curAddress);
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * Class providing a thread to update sensor states.
     */
    static class LnSensorUpdateThread extends Thread {

        /**
         * Constructs the thread
         */
        public LnSensorUpdateThread(LnSensorManager sm, LnTrafficController tc) {
            this.sm = sm;
            this.tc = tc;
        }

        /**
         * Runs the thread - sends 8 commands to query status of all stationary
         * sensors per LocoNet PE Specs, page 12-13.
         * Thread waits 500 msec between commands.
         */
        @Override
        public void run() {
            sm.setUpdateBusy();
            byte sw1[] = {0x78, 0x79, 0x7a, 0x7b, 0x78, 0x79, 0x7a, 0x7b};
            byte sw2[] = {0x27, 0x27, 0x27, 0x27, 0x07, 0x07, 0x07, 0x07};
            // create and initialize LocoNet message
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_SW_REQ);
            for (int k = 0; k < 8; k++) {
                try {
                    // Delay 500 mSec to allow init of traffic controller, listeners.
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                    sm.setUpdateNotBusy();
                    return; // and stop work
                }
                msg.setElement(1, sw1[k]);
                msg.setElement(2, sw2[k]);
                        
                tc.sendLocoNetMessage(msg);
                log.debug("LnSensorUpdate sent");
            }
            sm.setUpdateNotBusy();
        }

        private LnSensorManager sm = null;
        private LnTrafficController tc = null;

    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorManager.class);

}

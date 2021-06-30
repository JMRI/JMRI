package jmri.jmrix.loconet;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.temporal.ChronoUnit;
import java.time.Instant;

/**
 * Manage the LocoNet-specific Sensor implementation.
 * System names are "LSnnn", where L is the user configurable system prefix,
 * nnn is the sensor number without padding.  Valid sensor numbers are in the
 * range 1 to 2048, inclusive.
 *
 * Provides a mechanism to perform the LocoNet "Interrogate" process in order
 * to get initial values from those LocoNet devices which support the process
 * and provide LocoNet Sensor (and/or LocoNet Turnout) functionality.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author B. Milhaupt  Copyright (C) 2020
 */
public class LnSensorManager extends jmri.managers.AbstractSensorManager implements LocoNetListener {

    protected final LnTrafficController tc;

    /**
     * Minimum amount of time since previous LocoNet Sensor State report or
     * previous LocoNet Turnout State report or previous LocoNet "interrogate"
     * message.
     */
    protected int restingTime;

    /**
     * Instant at which last LocoNet Sensor State message or LocoNet Turnout state
     * message or LocoNet Interrogation request message was received.
     *
     */
    private volatile Instant lastSensTurnInterrog;

    public LnSensorManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        this.restingTime = 1250;
        tc = memo.getLnTrafficController();
        if (tc == null) {
            log.error("SensorManager Created, yet there is no Traffic Controller");
            return;
        }
        lastSensTurnInterrog = Instant.now();   // a baseline starting-point for
                    // interrogation timing

        // ctor has to register for LocoNet events
        tc.addLocoNetListener(~0, this);

        // start the update sequence. Until JMRI 2.9.4, this waited
        // until files have been read, but starts automatically
        // since 2.9.5 for multi-system support.
        updateAll();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public LocoNetSystemConnectionMemo getMemo() {
        return (LocoNetSystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return new LnSensor(systemName, userName, tc, getSystemPrefix());
    }

    /**
     * Listen for sensor messages, creating them as needed.
     * @param l LocoNet message to be examined
     */
    @Override
    public void message(LocoNetMessage l) {
        // parse message type
        LnSensorAddress a;
        LnSensor ns;
        switch (l.getOpCode()) {
            case LnConstants.OPC_INPUT_REP:                /* page 9 of LocoNet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                a = new LnSensorAddress(sw1, sw2, getSystemPrefix());
                log.debug("INPUT_REP received with address {}", a);
                lastSensTurnInterrog = Instant.now();
                break;
            case LnConstants.OPC_SW_REP:
                lastSensTurnInterrog = Instant.now();
                return;
            case LnConstants.OPC_SW_REQ:
            case LnConstants.OPC_SW_ACK:
                int address = ((l.getElement(1)& 0x7f) + 128*(l.getElement(2) & 0x0f));
                switch (address) {
                    case 0x3F8:
                    case 0x3F9:
                    case 0x3FA:
                    case 0x3FB:
                        lastSensTurnInterrog = Instant.now();
                        return;
                    default:
                        break;
                }
            //$FALL-THROUGH$
            default:  // here we didn't find an interesting command
                return;
        }
        // reach here for LocoNet sensor input command; make sure we know about this one
        String s = a.getNumericAddress();
        ns = (LnSensor) getBySystemName(s);
        if (ns == null) {
            // need to store a new one
            if (log.isDebugEnabled()) {
                log.debug("Create new LnSensor as {}", s);
            }
            ns = (LnSensor) newSensor(s, null);
        }
        ns.messageFromManager(l);  // have it update state
    }

    volatile LnSensorUpdateThread thread;

    /**
     * Requests status updates from all layout sensors.
     */
    @Override
    public void updateAll() {
        if (!busy) {
            setUpdateBusy();
            thread = new LnSensorUpdateThread(this, tc, getRestingTime());
            thread.setName("LnSensorUpdateThread"); // NOI18N
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

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (curAddress.contains(":")) { // NOI18N

            // NOTE: This format is deprecated in JMRI 4.17.4 on account the
            // "byte:bit" format cannot be used under normal JMRI usage
            // circumstances.  It is retained for the normal deprecation period
            // in order to support any atypical usage patterns.

            int board = 0;
            int channel = 0;
            // Address format passed is in the form of board:channel or T:turnout address
            int seperator = curAddress.indexOf(":"); // NOI18N
            boolean turnout = false;
            if (curAddress.substring(0, seperator).toUpperCase().equals("T")) { // NOI18N
                turnout = true;
            } else {
                try {
                    board = Integer.parseInt(curAddress.substring(0, seperator));
                } catch (NumberFormatException ex) {
                    throw new JmriException("Unable to convert '"+curAddress+"' into the cab and channel format of nn:xx"); // NOI18N
                }
            }
            try {
                channel = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert '"+curAddress+"' into the cab and channel format of nn:xx"); // NOI18N
            }
            if (turnout) {
                iName = 2 * (channel - 1) + 1;
            } else {
                iName = 16 * board + channel - 16;
            }
            jmri.util.LoggingUtil.warnOnce(log,
                    "LnSensorManager.createSystemName(curAddress, prefix) support for curAddress using the '{}' format is deprecated as of JMRI 4.17.4 and will be removed in a future JMRI release.  Use the curAddress format '{}' instead.",  // NOI18N
                    curAddress, iName);
        } else {
            // Entered in using the old format
            log.debug("LnSensorManager creating system name for {}", curAddress);
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                throw new JmriException("Hardware Address passed "+curAddress+" should be a number"); // NOI18N
            }
        }
        return prefix + typeLetter() + iName;
    }

    int iName;

    /** {@inheritDoc} */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, 4096, locale);
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

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * Set "resting time" for the Interrogation process.
     *
     * A minimum of 500 (milliseconds) and a maximum of 200000 (milliseconds) is
     * implemented.  Values below this lower limit are forced to the lower
     * limit, and values above this upper limit are forced to the upper limit.
     *
     * @param rest Number of milliseconds (minimum) before sending next
     *      LocoNet Interrogate message.
     */
    public void setRestingTime(int rest) {
        if (rest < 500) {
            rest = 500;
        } else if (rest > 200000) {
            rest = 200000;
        }
        restingTime = rest;
    }

    /**
     * get Interrogation process's "resting time"
     * @return Resting time, in milliseconds
     */
    public int getRestingTime() {
        return restingTime;
    }

    /**
     * Class providing a thread to query LocoNet Sensor and Turnout states.
     */
    class LnSensorUpdateThread extends Thread {
        private LnSensorManager sm = null;
        private LnTrafficController tc = null;
        private java.time.Duration restingTime;

        /**
         * Constructs the thread
         * @param sm SensorManager to use
         * @param tc TrafficController to use
         * @param restingTime Min time before next LN query message sent
         */
        public LnSensorUpdateThread(LnSensorManager sm, LnTrafficController tc,
                int restingTime) {
            this.sm = sm;
            this.tc = tc;
            this.restingTime = java.time.Duration.ofMillis(restingTime);
        }

        /**
         * Runs the thread - sends 8 commands to query status of all stationary
         * sensors (per LocoNet PE Specs, page 12-13).
         *
         * Timing between query messages is determined by certain previous LocoNet
         * traffic (as noted by lastSensTurnInterrog) and restingTime.
         */
        @Override
        public void run() {
            sm.setUpdateBusy();
            while (!tc.status()) {
                try {
                    // Delay 500 mSec to allow init of traffic controller,
                    // listeners, and to limit amount of LocoNet traffic at
                    // JMRI start-up.
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                    sm.setUpdateNotBusy();
                    return; // and stop work
                }
            }
            byte sw1[] = {0x78, 0x79, 0x7a, 0x7b, 0x78, 0x79, 0x7a, 0x7b};
            byte sw2[] = {0x27, 0x27, 0x27, 0x27, 0x07, 0x07, 0x07, 0x07};
            // create and initialize LocoNet message
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_SW_REQ);
            for (int k = 0; k < 8; k++) {
                Instant n = Instant.now();
                Instant n2 = lastSensTurnInterrog.plus(restingTime);
                int result = n.compareTo(n2);
                log.debug("Interrogation phase {}: now {}, lastSensInterrog {}, target{}, time compare result {}",
                        k, n, lastSensTurnInterrog, n2, result);
                while (result < 0) {
                    try {
                        // Delay 100 mSec
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                        sm.setUpdateNotBusy();
                        return; // and stop work
                    }
                    n = Instant.now();
                    result = n.compareTo(n2);
                    log.debug("Interrogation phase {}: now {}, lastSensInterrog {}, target{}, time compare result {}",
                            k, n, lastSensTurnInterrog, n2, result);
                }
                msg.setElement(1, sw1[k]);
                msg.setElement(2, sw2[k]);

                tc.sendLocoNetMessage(msg);

                /* lastSensTurnInterrog needs to be updated here to prevent quick
                 * sending of the next query message.  (It will be updated upon
                 * reception of the LocoNet "echo".)
                 */
                lastSensTurnInterrog = Instant.now();

                log.debug("LnSensorUpdate sent");
            }
            sm.setUpdateNotBusy();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorManager.class);

}

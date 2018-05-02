package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the NCE-specific Sensor implementation.
 * <P>
 * System names are "NSnnn", where nnn is the sensor number without padding.
 * <P>
 * This class is responsible for generating polling messages for the
 * NceTrafficController, see nextAiuPoll()
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class NceSensorManager extends jmri.managers.AbstractSensorManager
        implements NceListener {

    public NceSensorManager(NceTrafficController tc, String prefix) {
        super();
        this.tc = tc;
        this.prefix = prefix;
        for (int i = MINAIU; i <= MAXAIU; i++) {
            aiuArray[i] = null;
        }
        listener = new NceListener() {
            @Override
            public void message(NceMessage m) {
            }

            @Override
            public void reply(NceReply r) {
                if (r.isSensorMessage()) {
                    mInstance.handleSensorMessage(r);
                }
            }
        };
        tc.addNceListener(listener);
    }

    NceTrafficController tc = null;
    String prefix = "N";

    private NceSensorManager mInstance = null;

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        stopPolling = true;  // tell polling thread to go away
        Thread thread = pollThread;
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException ex) {
                log.warn("dispose interrupted");
            }
        }
        tc.removeNceListener(listener);
        super.dispose();
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        int number = Integer.valueOf(systemName.substring(getSystemPrefix().length() + 1)).intValue();

        Sensor s = new NceSensor(systemName);
        s.setUserName(userName);

        // ensure the AIU exists
        int index = (number / 16) + 1;
        if (aiuArray[index] == null) {
            aiuArray[index] = new NceAIU();
            buildActiveAIUs();
        }

        // register this sensor with the AIU
        aiuArray[index].registerSensor(s, number - (index - 1) * 16);

        return s;
    }

    NceAIU[] aiuArray = new NceAIU[MAXAIU + 1];  // element 0 isn't used
    int[] activeAIUs = new int[MAXAIU];  // keep track of those worth polling
    int activeAIUMax = 0;       // last+1 element used of activeAIUs
    private static final int MINAIU = 1;
    private static final int MAXAIU = 63;
    private static final int MAXPIN = 14;    // only pins 1 - 14 used on NCE AIU

    volatile Thread pollThread;
    volatile boolean stopPolling = false;
    NceListener listener;

    // polling parameters and variables
    private final int shortCycleInterval = 200;
    private final int longCycleInterval = 10000;  // when we know async messages are flowing
    private final long maxSilentInterval = 30000;  // max slow poll time without hearing an async message
    private final int pollTimeout = 20000;    // in case of lost response
    private int aiuCycleCount;
    private long lastMessageReceived;     // time of last async message
    private NceAIU currentAIU;
    private boolean awaitingReply = false;
    private boolean awaitingDelay = false;

    /**
     * Build the array of the indices of AIUs which have been polled, and
     * ensures that pollManager has all the information it needs to work
     * correctly.
     *
     */
    /* Some logic notes
     * 
     * Sensor polling normally happens on a short cycle - the NCE round-trip
     * response time (normally 50mS, set by the serial line timeout) plus
     * the "shortCycleInterval" defined above. If an async sensor message is received,
     * we switch to the longCycleInterval since really we don't need to poll at all.
     * 
     * We use the long poll only if the following conditions are satisified:
     * 
     * -- there have been at least two poll cycle completions since the last change
     * to the list of active sensor - this means at least one complete poll cycle,
     * so we are sure we know the states of all the sensors to begin with
     * 
     * -- we have received an async message in the last maxSilentInterval, so that
     * if the user turns off async messages (possible, though dumb in mid session)
     * the system will stumble back to life
     * 
     * The interaction between buildActiveAIUs and pollManager is designed so that
     * no explicit sync or locking is needed when the former changes the list of active
     * AIUs used by the latter. At worst, there will be one cycle which polls the same
     * sensor twice.
     * 
     * Be VERY CAREFUL if you change any of this.
     * 
     */
    private void buildActiveAIUs() {
        activeAIUMax = 0;
        for (int a = MINAIU; a <= MAXAIU; ++a) {
            if (aiuArray[a] != null) {
                activeAIUs[activeAIUMax++] = a;
            }
        }
        aiuCycleCount = 0;    // force another polling cycle
        lastMessageReceived = Long.MIN_VALUE;
        if (activeAIUMax > 0) {
            if (pollThread == null) {
                pollThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pollManager();
                    }
                });
                pollThread.setName("NCE Sensor Poll");
                pollThread.setDaemon(true);
                pollThread.start();
            } else {
                synchronized (this) {
                    if (awaitingDelay) {  // interrupt long between-poll wait
                        notify();
                    }
                }
            }
        }
    }

    public NceMessage makeAIUPoll(int aiuNo) {
        // use old 4 byte read command if not USB
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
            return makeAIUPoll4ByteReply(aiuNo);
        } else {
            return makeAIUPoll2ByteReply(aiuNo);
        }
    }

    /**
     * Construct a binary-formatted AIU poll message
     *
     * @param aiuNo number of AIU to poll
     * @return message to be queued
     */
    private NceMessage makeAIUPoll4ByteReply(int aiuNo) {
        NceMessage m = new NceMessage(2);
        m.setBinary(true);
        m.setReplyLen(4);
        m.setElement(0, NceBinaryCommand.READ_AUI4_CMD);
        m.setElement(1, aiuNo);
        m.setTimeout(pollTimeout);
        return m;
    }

    /**
     * construct a binary-formatted AIU poll message
     *
     * @param aiuNo number of AIU to poll
     * @return message to be queued
     */
    private NceMessage makeAIUPoll2ByteReply(int aiuNo) {
        NceMessage m = new NceMessage(2);
        m.setBinary(true);
        m.setReplyLen(2);
        m.setElement(0, NceBinaryCommand.READ_AUI2_CMD);
        m.setElement(1, aiuNo);
        m.setTimeout(pollTimeout);
        return m;
    }

    /**
     * Send poll messages for AIU sensors. Also interact with
     * asynchronous sensor state messages. Adjust poll cycle according to
     * whether any async messages have been received recently. Also we require
     * one poll of each sensor before squelching active polls.
     */
    private void pollManager() {
        while (!stopPolling) {
            for (int a = 0; a < activeAIUMax; ++a) {
                int aiuNo = activeAIUs[a];
                currentAIU = aiuArray[aiuNo];
                if (currentAIU != null) {    // in case it has gone away
                    NceMessage m = makeAIUPoll(aiuNo);
                    synchronized (this) {
                        if (log.isDebugEnabled()) {
                            log.debug("queueing poll request for AIU " + aiuNo);
                        }
                        tc.sendNceMessage(m, this);
                        awaitingReply = true;
                        try {
                            wait(pollTimeout);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            return;
                        }
                    }
                    int delay = shortCycleInterval;
                    if (aiuCycleCount >= 2
                            && lastMessageReceived >= System.currentTimeMillis() - maxSilentInterval) {
                        delay = longCycleInterval;
                    }
                    synchronized (this) {
                        if (awaitingReply && !stopPolling) {
                            log.warn("timeout awaiting poll response for AIU " + aiuNo);
                            // slow down the poll since we're not getting responses
                            // this lets NceConnectionStatus to do its thing
                            delay = pollTimeout;
                        }
                        try {
                            awaitingDelay = true;
                            wait(delay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            return;
                        } finally {
                            awaitingDelay = false;
                        }
                    }
                }
            }
            ++aiuCycleCount;
        }
    }

    @Override
    public void message(NceMessage r) {
        log.warn("unexpected message");
    }

    /**
     * Process single received reply from sensor poll.
     */
    @Override
    public void reply(NceReply r) {
        if (!r.isUnsolicited()) {
            int bits;
            synchronized (this) {
                bits = r.pollValue();  // bits is the value in hex from the message
                awaitingReply = false;
                this.notify();
            }
            currentAIU.markChanges(bits);
            if (log.isDebugEnabled()) {
                String str = jmri.util.StringUtil.twoHexFromInt((bits >> 4) & 0xf);
                str += " ";
                str = jmri.util.StringUtil.appendTwoHexFromInt(bits & 0xf, str);
                log.debug("sensor poll reply received: \"" + str + "\"");
            }
        }
    }

    /**
     * Handle an unsolicited sensor (AIU) state message.
     *
     * @param r sensor message
     */
    public void handleSensorMessage(AbstractMRReply r) {
        int index = r.getElement(1) - 0x30;
        int indicator = r.getElement(2);
        if (r.getElement(0) == 0x61 && r.getElement(1) >= 0x30 && r.getElement(1) <= 0x6f
                && ((indicator >= 0x41 && indicator <= 0x5e) || (indicator >= 0x61 && indicator <= 0x7e))) {
            lastMessageReceived = System.currentTimeMillis();
            if (aiuArray[index] == null) {
                log.debug("unsolicited message \"" + r.toString() + "\" for unused sensor array");
            } else {
                int sensorNo;
                int newState;
                if (indicator >= 0x60) {
                    sensorNo = indicator - 0x61;
                    newState = Sensor.ACTIVE;
                } else {
                    sensorNo = indicator - 0x41;
                    newState = Sensor.INACTIVE;
                }
                Sensor s = aiuArray[index].getSensor(sensorNo);
                if (s.getInverted()) {
                    if (newState == Sensor.ACTIVE) {
                        newState = Sensor.INACTIVE;
                    } else if (newState == Sensor.INACTIVE) {
                        newState = Sensor.ACTIVE;
                    }
                }

                if (log.isDebugEnabled()) {
                    String msg = "Handling sensor message \"" + r.toString() + "\" for ";
                    msg += s.getSystemName();

                    if (newState == Sensor.ACTIVE) {
                        msg += ": ACTIVE";
                    } else {
                        msg += ": INACTIVE";
                    }
                    log.debug(msg);
                }
                aiuArray[index].sensorChange(sensorNo, newState);
            }
        } else {
            log.warn("incorrect sensor message: " + r.toString());
        }
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (curAddress.contains(":")) {
            // Sensor address is presented in the format AIU Cab Address:Pin Number On AIU
            // Should we be validating the values of aiucab address and pin number?
            // Yes we should, added check for valid AIU and pin ranges DBoudreau 2/13/2013
            int seperator = curAddress.indexOf(":");
            try {
                aiucab = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                pin = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " into the cab and pin format of nn:xx");
                throw new JmriException("Hardware Address passed should be a number");
            }
            iName = (aiucab - 1) * 16 + pin - 1;

        } else {
            //Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Hardware Address passed should be a number");
            }
            pin = iName % 16 + 1;
            aiucab = iName / 16 + 1;
        }
        // only pins 1 through 14 are valid
        if (pin == 0 || pin > MAXPIN) {
            log.error("NCE sensor " + curAddress + " pin number " + pin + " is out of range; only pin numbers 1 - 14 are valid");
            throw new JmriException("Sensor pin number is out of range");
        }
        if (aiucab == 0 || aiucab > MAXAIU) {
            log.error("NCE sensor " + curAddress + " AIU number " + aiucab + " is out of range; only AIU 1 - 63 are valid");
            throw new JmriException("AIU number is out of range");

        }
        return prefix + typeLetter() + iName;
    }

    int aiucab = 0;
    int pin = 0;
    int iName = 0;

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }

        // Check to determine if the systemName is in use, return null if it is,
        // otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                pin = pin + 1;
                if (pin > MAXPIN) {
                    return null;
                }
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    /**
     * Get the bit address from the system name.
     * Does not support the M:p format, so preprocess that beforehand.
     *
     * @param systemName system name for sensor
     * @return index value for sensor
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "S"))) {
            // here if an illegal nce light system name
            log.error("illegal character in header field of nce sensor system name: " + systemName);
            return (0);
        }
        // system name must be in the NLnnnnn format (N is user configurable)
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())
            ).intValue();
        } catch (Exception e) {
            log.debug("illegal character in number field of system name: " + systemName);
            return (0);
        }
        if (num <= 0) {
            log.error("invalid nce sensor system name: " + systemName);
            return (0);
        } else if (num > 4096) {
            log.warn("bit number out of range in nce sensor system name: " + systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format.
     *
     * @param systemName to be checked
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        if (systemName.contains(":") && !systemName.endsWith(":")) { // prevent to try parsing too soon
            // If sensor address is presented in the AIU Cab Address:Pin Number On AIU format,
            // translate it into nnnn. Copied from createSystemName()
            String curAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length());
            int seperator = curAddress.indexOf(":"); // assuming the ":" is not in the prefix
            int _aiucab;
            int _pin;
            log.debug(curAddress);
            try {
                _aiucab = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                _pin = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + curAddress + " into the cab and pin format of nn:xx");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
                return NameValidity.INVALID;
            }
            systemName = getSystemPrefix() + "S" + ((_aiucab - 1) * 16 + _pin - 1);
        }
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(NceSensorManager.class);

}

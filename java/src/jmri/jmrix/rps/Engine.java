package jmri.jmrix.rps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import javax.vecmath.Point3d;
import jmri.CommandStation;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Engine does basic computations of RPS system.
 * <p>
 * Holds all the alignment info. Receivers are indexed by their RPS receiver
 * number in all cases.
 * <p>
 * Gets a reading from the Distributor and passes back a Measurement
 * <p>
 * Bound properties:
 * <ul>
 * <li>vSound - velocity of sound, in whatever units are in use
 * </ul>
 * <p>
 * This class maintains a collection of "Transmitter" objects representing the
 * RPS-equipped rolling stock (usually engines) on the layout. This is an
 * extension to the common Roster, and every entry in this class's collection
 * must be present in the Roster.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class Engine implements ReadingListener {

    public Engine() {
    }

    void loadValues() {
        // load dummy contents
        setInitialAlignment();
        loadInitialTransmitters();
    }

    public void dispose() {
    }

    public void setVSound(double v) {
        double oldVal = vsound;
        vsound = v;
        log.info("change vsound from {} to {}", oldVal, v);
        prop.firePropertyChange("vSound", Double.valueOf(oldVal), Double.valueOf(v));
    }

    public double getVSound() {
        return vsound;
    }
    private double vsound = 0.013544;  // 0.013544 inches/usec, .000345 m/usec,
    private int offset = 0;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    Measurement lastPoint = null;

    Receiver[] receivers;

    /**
     * Set the maximum receiver number expected. If the highest value in the
     * hardware is 5, that's what's needed here.
     */
    public void setMaxReceiverNumber(int n) {
        log.debug("setReceiverCount to {}", n);
        if ((receivers != null) && (n == receivers.length + 1)) {
            return;
        }
        Receiver[] oldReceivers = receivers;
        receivers = new Receiver[n + 1];  // n is highest address, so need n+1
        if (oldReceivers == null) {
            return;
        }
        // clear new array
        for (int i = 0; i < receivers.length; i++) {
            receivers[i] = null;
        }
        // copy the existing receivers
        for (int i = 0; i < Math.min(n + 1, oldReceivers.length); i++) {
            receivers[i] = oldReceivers[i];
        }
    }

    public int getMaxReceiverNumber() {
        if (receivers == null) {
            return 0;
        }
        return receivers.length - 1;
    }

    /**
     * Get a particular receiver by address (starting at 1).
     */
    public void setReceiver(int address, Receiver receiver) {
        if (receivers == null) {
            throw new IllegalArgumentException("Must initialize first");
        }
        if (address >= receivers.length) {
            throw new IllegalArgumentException("Index " + address + " is larger than expected " + receivers.length);
        }
        log.debug("store receiver {} in {}", address, this);
        receivers[address] = receiver;
    }

    public Receiver getReceiver(int i) {
        return receivers[i];
    }

    public void setReceiverPosition(int i, Point3d p) {
        receivers[i].setPosition(p);
    }

    public Point3d getReceiverPosition(int i) {
        if (receivers[i] == null) {
            log.debug("getReceiverPosition of null receiver index i={}", i);
            return null;
        }
        return receivers[i].getPosition();
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    String algorithm = "Ash 2.1";  // default value, configured separately

    @Override
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work done.

        // ok to send next poll
        log.debug("po false {}", r.getId());
        pollOutstanding = false;

        // make a list of receiver positions to provide
        // to the new Calculator.  Missing/unconfigured receivers
        // are null.
        Point3d list[] = new Point3d[receivers.length];
        for (int i = 0; i < receivers.length; i++) {

            if (receivers[i] == null) {
                list[i] = null;
                continue;  // skip receivers not present
            }

            Point3d p = getReceiverPosition(i);
            if (p != null) {
                receivers[i].setLastTime((int) r.getValue(i));  // receivers numbered from 1
                log.debug("    {}th value min {} < time {} < max {} at {}",
                        i, receivers[i].getMinTime(), r.getValue(i), receivers[i].getMaxTime(), p);
                if (receivers[i].isActive() && (receivers[i].getMinTime() <= r.getValue(i))
                        && (r.getValue(i) <= receivers[i].getMaxTime())) {
                    list[i] = p;
                } else {
                    list[i] = null;
                }
            } else {
                list[i] = null;
                log.error("Unexpected null position from receiver {}", i);
            }
        }

        Calculator c = Algorithms.newCalculator(list, getVSound(),
                getOffset(), getAlgorithm());

        Measurement m = c.convert(r, lastPoint);

        saveLastMeasurement(r.getId(), m);

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }

    // Store the lastMeasurement
    void saveLastMeasurement(String id, Measurement m) {
        for (int i = 0; i < getNumTransmitters(); i++) {
            if (getTransmitter(i).getId().equals(id) && getTransmitter(i).isPolled()) {
                getTransmitter(i).setLastMeasurement(m);
                // might be more than one, so don't end here
            }
        }
    }

    // Store alignment info
    public void storeAlignment(File file) throws IOException {
        PositionFile pf = new PositionFile();
        pf.prepare();
        pf.setConstants(getVSound(), getOffset(), getAlgorithm());

        for (int i = 1; i <= getMaxReceiverNumber(); i++) {
            if (getReceiver(i) == null) {
                continue;
            }
            pf.setReceiver(i, getReceiver(i));
        }
        pf.store(file);
    }

    public void loadAlignment(File file) throws org.jdom2.JDOMException, IOException {
        // start by getting the file
        PositionFile pf = new PositionFile();
        pf.loadFile(file);

        // get VSound
        setVSound(pf.getVSound());

        // get offset
        setOffset(pf.getOffset());

        // get algorithm
        setAlgorithm(pf.getAlgorithm());

        // get receivers
        setMaxReceiverNumber(pf.maxReceiver());  // count from 1
        Point3d p;
        boolean a;
        int min;
        int max;
        for (int i = 1; i <= getMaxReceiverNumber(); i++) {
            p = pf.getReceiverPosition(i);
            if (p == null) {
                continue;
            }

            a = pf.getReceiverActive(i);
            min = pf.getReceiverMin(i);
            max = pf.getReceiverMax(i);

            log.debug("load {} with {}", i, p);
            Receiver r = new Receiver(p);
            r.setActive(a);
            r.setMinTime(min);
            r.setMaxTime(max);
            setReceiver(i, r);
        }

    }

    protected void setInitialAlignment() {
        File defaultFile = new File(PositionFile.defaultFilename());
        try {
            loadAlignment(defaultFile);
        } catch (Exception e) {
            log.debug("load exception ", e);
            // load dummy values
            setDefaultAlignment();
        }
    }

    protected void setDefaultAlignment() {
        setMaxReceiverNumber(2);
        setReceiver(1, new Receiver(new Point3d(0.0, 0.0, 72.0)));
        setReceiver(2, new Receiver(new Point3d(72.0, 0.0, 72.0)));
    }

    //**************************************
    // Methods to handle polling
    //**************************************
    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
    int pollingInterval = 500;

    public int getPollingInterval() {
        return pollingInterval;
    }

    boolean polling = false;

    public void setPolling(boolean polling) {
        this.polling = polling;
        if (polling) {
            startPoll();
        } else {
            stopPoll();
        }
    }

    public boolean getPolling() {
        return polling;
    }

    java.util.ArrayList<Transmitter> transmitters;

    void loadInitialTransmitters() {
        transmitters = new java.util.ArrayList<Transmitter>();
        // load transmitters from the JMRI roster
        java.util.List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, null);
        log.debug("Got {} roster entries", l.size());
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = null;
            try {
                r = l.get(i);
                int address = Integer.parseInt(r.getDccAddress());
                Transmitter t = new Transmitter(r.getId(), false, address, r.isLongAddress());
                t.setRosterName(r.getId());
                transmitters.add(t);
            } catch (NumberFormatException e) {
                // just skip this entry
                if (r != null) {
                    log.warn("Skip roster entry: {}", r.getId());
                } else {
                    log.warn("Failed roster entry skipped");
                }
            }
        }

        // load the polling status, custom IDs, etc, from file if possible
        try {
            loadPollConfig(new File(PollingFile.defaultFilename()));
        } catch (IOException | JDOMException e) {
            log.error("Unable to load {}", PollingFile.defaultFilename(), e);
        }
    }

    // Store polling info
    public void storePollConfig(File file) throws IOException {
        PollingFile pf = new PollingFile();
        pf.prepare();
        pf.setPoll();

        for (int i = 0; i < getNumTransmitters(); i++) {
            pf.setTransmitter(i);
        }
        pf.store(file);
    }

    public void loadPollConfig(File file) throws org.jdom2.JDOMException, IOException {
        if (file.exists()) {
            PollingFile pf = new PollingFile();
            pf.loadFile(file);
            // first make sure transmitters defined
            pf.getTransmitters(this);
            // and possibly start polling
            pf.getPollValues();
        }
    }

    public Transmitter getTransmitterByAddress(int addr) {
        if (addr < 0) {
            return null;
        }
        if (transmitters == null) {
            return null;
        }
        for (int i = 0; i < getNumTransmitters(); i++) {
            if (getTransmitter(i).getAddress() == addr) {
                return getTransmitter(i);
            }
        }
        return null;
    }

    public Transmitter getTransmitter(int i) {
        if (i < 0) {
            return null;
        }
        if (transmitters == null) {
            return null;
        }
        return transmitters.get(i);
    }

    public int getNumTransmitters() {
        if (transmitters == null) {
            return 0;
        }
        return transmitters.size();
    }

    public String getPolledID() {
        Transmitter t = getTransmitter(pollIndex);
        if (t == null) {
            return "";
        }
        return t.getId();
    }

    public int getPolledAddress() {
        Transmitter t = getTransmitter(pollIndex);
        if (t == null) {
            return -1;
        }
        return t.getAddress();
    }

    /**
     * The real core of the polling, this selects the next one to poll. -1 means
     * none selected, try again later.
     */
    int selectNextPoll() {
        int startindex = pollIndex;
        while (++pollIndex < getNumTransmitters()) {
            if (getTransmitter(pollIndex).isPolled()) {
                return pollIndex;
            }
        }
        // here, we got to the end without finding somebody to poll
        // try the start
        pollIndex = -1; // will autoincrement to 0
        while (++pollIndex <= startindex) {
            if (getTransmitter(pollIndex).isPolled()) {
                return pollIndex;
            }
        }
        // no luck, say so
        return -1;
    }

    int pollIndex = -1; // left at last one done
    boolean bscPoll = false;
    boolean throttlePoll = false;

    public void setBscPollMode() {
        bscPoll = true;
        throttlePoll = false;
    }

    public void setDirectPollMode() {
        bscPoll = false;
        throttlePoll = false;
    }

    public void setThrottlePollMode() {
        bscPoll = false;
        throttlePoll = true;
    }

    public boolean getBscPollMode() {
        return bscPoll;
    }

    public boolean getThrottlePollMode() {
        return throttlePoll;
    }

    public boolean getDirectPollMode() {
        return !(bscPoll || throttlePoll);
    }

    void startPoll() {
        // time to start operation
        pollThread = new Thread() {
            @Override
            public void run() {
                log.debug("Polling starts");
                while (true) {
                    try {
                        int i = selectNextPoll();
                        log.debug("Poll {}", i);
                        setOn(i);
                        log.debug("po true {}", i);
                        pollOutstanding = true;
                        synchronized (this) {
                            wait(20);
                        }
                        setOff(i);
                        log.debug("start wait");
                        waitBeforeNextPoll(pollingInterval);
                        log.debug("end wait");
                    } catch (InterruptedException e) {
                        // cancel whatever is happening
                        log.debug("Polling stops");
                        Thread.currentThread().interrupt(); // retain if needed later
                        return; // end operation
                    }
                }
            }
        };
        pollThread.start();
    }

    Thread pollThread;
    boolean pollOutstanding;

    /**
     * Wait before sending next poll.
     * <p>
     * Waits specified time, and then checks to see if response has been
     * returned. If not, it waits again (twice) by 1/2 the interval, then
     * finally polls anyway.
     */
    void waitBeforeNextPoll(int pollingInterval) throws InterruptedException {
        synchronized (this) {
            wait(pollingInterval);
        }
        if (!pollOutstanding) {
            return;
        }
        log.debug("--- extra wait");
        for (int i = 0; i < 20; i++) {
            synchronized (this) {
                wait(pollingInterval / 4);
            }
            log.debug("-------------extra wait");
            if (!pollOutstanding) {
                return;
            }
        }
    }

    void stopPoll() {
        if (pollThread != null) {
            pollThread.interrupt();
        }
    }

    void setOn(int i) {
        Transmitter t = getTransmitter(i);
        byte[] packet;
        if (bscPoll) {
            // poll using BSC instruction
            packet = jmri.NmraPacket.threeBytePacket(
                    t.getAddress(), t.isLongAddress(),
                    (byte) 0xC0, (byte) 0xA5, (byte) 0xFE);
            if (jmri.InstanceManager.getNullableDefault(CommandStation.class) != null) {
                jmri.InstanceManager.getDefault(CommandStation.class).sendPacket(packet, 1);
            }
        } else {
            // poll using F2
            if (throttlePoll) {
                // use throttle; first, get throttle
                if (t.checkInit()) {
                    // now send F2
                    t.getThrottle().setF2(true);
                } else {
                    return;  // bail if not ready
                }
            } else {
                // send packet direct
                packet = jmri.NmraPacket.function0Through4Packet(
                        t.getAddress(), t.isLongAddress(),
                        false, false, true, false, false);
                if (jmri.InstanceManager.getNullableDefault(CommandStation.class) != null) {
                    jmri.InstanceManager.getDefault(CommandStation.class).sendPacket(packet, 1);
                }
            }
        }
    }

    void setOff(int i) {
        if (!bscPoll) {
            // have to turn off F2 since not using BSC
            Transmitter t = getTransmitter(i);
            if (throttlePoll) {
                // use throttle; first, get throttle
                if (t.checkInit()) {
                    // now send F2
                    t.getThrottle().setF2(false);
                } else {
                    return;  // bail if not ready
                }
            } else {
                // send direct
                byte[] packet = jmri.NmraPacket.function0Through4Packet(
                        t.getAddress(), t.isLongAddress(),
                        false, false, false, false, false);
                if (jmri.InstanceManager.getNullableDefault(CommandStation.class) != null) {
                    jmri.InstanceManager.getDefault(CommandStation.class).sendPacket(packet, 1);
                }
            }
        }
    }

    // for now, we only allow one Engine
    @SuppressFBWarnings(value = "MS_PKGPROTECT") // for tests
    static volatile protected Engine _instance = null;

    @SuppressFBWarnings(value = "LI_LAZY_INIT_UPDATE_STATIC") // see comment in method
    static public Engine instance() {
        if (_instance == null) {
            // NOTE: _instance has to be initialized before loadValues()
            // is called, because it invokes instance() indirectly.
            _instance = new Engine();
            _instance.loadValues();
        }
        return _instance;
    }

    // handle outgoing parameter notification
    java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);

    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) {
        prop.removePropertyChangeListener(p);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) {
        prop.addPropertyChangeListener(p);
    }

    private final static Logger log = LoggerFactory.getLogger(Engine.class);

}

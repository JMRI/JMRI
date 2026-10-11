package jmri.jmrix.dcc4pc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;

/**
 * Implement SensorManager for Dcc4Pc systems. The Manager handles all the state
 * changes.
 * <p>
 * System names are "DSnn:yy", where D is the user configurable system prefix,
 * nn is the board id and yy is the port on that board.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class Dcc4PcSensorManager extends jmri.managers.AbstractSensorManager
        implements Dcc4PcListener {

    public Dcc4PcSensorManager(Dcc4PcTrafficController tc, Dcc4PcSystemConnectionMemo memo) {
        super(memo);
        this.tc = tc;
        this.reportManager = (Dcc4PcReporterManager) memo.get(jmri.ReporterManager.class);
        jmri.InstanceManager.store(Dcc4PcSensorManager.this, Dcc4PcSensorManager.class);
        this.boardManager = new Dcc4PcBoardManager(tc, this);
        // Finally, create and register a shutdown task to ensure clean exit
        this.pollShutDownTask = this::stopPolling;
        startPolling();
    }

    Dcc4PcReporterManager reportManager;
    Runnable pollShutDownTask;
    Dcc4PcBoardManager boardManager;

    Dcc4PcTrafficController tc;

    @Override
    public Dcc4PcSensor getSensor(@Nonnull String name) {
        return (Dcc4PcSensor) super.getSensor(name);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Dcc4PcSystemConnectionMemo getMemo() {
        return (Dcc4PcSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Sensor s = new Dcc4PcSensor(systemName, userName);
        s.setUserName(userName);
        extractBoardID(systemName);
        return s;
    }

    /**
     * This extracts the board id out from the system name.
     * @param systemName including system prefix and type letter.
     */
    void extractBoardID(String systemName) {
        if (systemName.contains(":")) {
            int indexOfSplit = systemName.indexOf(":");
            systemName = systemName.substring(0, indexOfSplit);
            indexOfSplit = getSystemPrefix().length() + 1; // +1 includes the typeletter which is a char
            systemName = systemName.substring(indexOfSplit);
            int boardNo;
            try {
                boardNo = Integer.parseInt(systemName);
            } catch (NumberFormatException ex) {
                log.error("Unable to find the board address from system name {}", systemName);
                return;
            }
            addBoard(boardNo);
        }
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    // we want the system name to be in the format of board:input
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        String iName;
        if (curAddress.contains(":")) {
            int board = 0;
            int channel;
            // Address format passed is in the form of board:channel or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.parseInt(curAddress.substring(0, seperator));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert "+curAddress+" into the cab and channel format of nn:xx");
            }

            try {
                channel = Integer.parseInt(curAddress.substring(seperator + 1));
                if ((channel > 16) || (channel < 1)) {
                    throw new JmriException("In Address "+curAddress+" Channel number should be in the range of 1 to 16");
                }
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert "+curAddress+" into the cab and channel format of nn:xx");
            }
            iName = curAddress;
            addBoard(board);
        } else {
            throw new JmriException("Unable to convert "+curAddress+" into the cab and channel format of nn:xx");
        }
        return prefix + typeLetter() + iName;
    }

    public void notifyReply(Dcc4PcReply m) {
        // is this a list of sensors?
    }

    public void notifyMessage(Dcc4PcMessage m) {
        // messages are ignored
    }

    Thread pollThread;
    boolean stopPolling = true;

    protected void stopPolling() {
        synchronized (this) {
            stopPolling = true;
        }
        if (pollThread != null) {
            // we want to wait for the polling thread to finish what it is currently working on.
            try {
                pollThread.join();
            } catch (InterruptedException e) {
                // Don't need to worry
            }
        }
    }

    protected final void startPolling() {
        if (stopPolling && pollThread != null) {
            pollThread = null;
        }
        stopPolling = false;

        if (pollThread == null) {
            pollThread = new Thread(this::pollManager, "DCC4PC Sensor Poll");
            pollThread.start();
        }
    }

    void addBoard(int newBoard) {
        boardManager.addBoard(newBoard);
    }

    @Override
    public void reply(Dcc4PcReply r) {
        if (log.isDebugEnabled()) {
            log.debug("Reply details sm: {}", r.toHexString());
        }

        if (r.getMessageType() == Dcc4PcMessage.INFO) {
            // the check that follows a speed change
            ActiveBoard board = activeBoards.get(r.getBoard());
            if (board != null) {
                board.speedCheckDone(!r.isError() && r.getNumDataElements() > 0);
            }
            return;
        }
        if (r.getMessageType() == Dcc4PcMessage.CHILDRESET) {
            ActiveBoard board = activeBoards.get(r.getBoard());
            if (board != null) {
                board.resetReplied(!r.isError());
            }
            return;
        }
        if (r.getNumDataElements() == 0 && r.getElement(0) == 0x00) {
            //Simple acknowledgement reply, no further action required
            return;
        }
        if (r.getBoard() == -1) {
            log.debug("Message is not for a detection board so ignore");
            return;
        }
        if (r.isError()) {
            log.debug("Reply is in error {}", r.toHexString());
            synchronized (this) {
                awaitingReply = false;
                this.notify();
            }
            if (r.getMessageType() == Dcc4PcMessage.CHILDPOLL) {
                // the reader has moved on without us, so the stream is broken
                ActiveBoard board = activeBoards.get(r.getBoard());
                if (board != null) {
                    board.requestResync("Get Data failed");
                }
            }
        } else if (!r.isUnsolicited()) {
            synchronized (this) {
                awaitingReply = false;
                this.notify();
            }
            if (log.isDebugEnabled()) {
                log.debug("Get Data inputs {}", r.toHexString());
            }
            ActiveBoard board = activeBoards.get(r.getBoard());
            if (board == null) {
                log.error("Do not know who this board message is for");
                return;
            }
            // the raw encoding is a stateful stream, so replies are decoded one at a time, in order
            packetProcessor.execute(() -> board.processRawReply(r));
        }
    }

    private final java.util.concurrent.ExecutorService packetProcessor =
            java.util.concurrent.Executors.newSingleThreadExecutor(runnable -> {
                Thread t = new Thread(runnable, "Dcc4PCSensor Process Packet");
                t.setDaemon(true);
                return t;
            });

    public static String decodeInputState(int state) {
        String rtr;
        switch (state) {
            case Sensor.INACTIVE:
                rtr = "UnOccupied";
                break;
            case Sensor.ACTIVE:
                rtr = "Occupied No RailCom Data";
                break;
            case Dcc4PcSensor.ORIENTA:
                rtr = "Occupied RailCom Orientation A";
                break;
            case Dcc4PcSensor.ORIENTB:
                rtr = "Occupied RailCom Orientation B";
                break;
            default:
                rtr = "Unknown";
        }
        return rtr;
    }

    public static final int NO_ADDRESS = 0x00;
    public static final int SHORT_ADDRESS = 0x02;
    public static final int LONG_ADDRESS = 0x04;
    public static final int CONSIST_ADDRESS = 0x08;

    private final int shortCycleInterval = 1; // Time to wait between sending out poll messages
    private final int pollTimeout = 3500;    // in case of lost response; longer than the Get Data timeout
    private boolean awaitingReply = false;

    void pollManager() {
        for (ActiveBoard board : activeBoards.values()) {
            if (!board.doNotPoll() && board.speedChecked) {
                board.requestResync("polling started");
            }
        }
        while (!stopPolling) {
            if (activeBoards.isEmpty()) {
                //If we have no boards to poll then wait a second.
                try {
                    Thread.sleep(1000);
                } catch (java.lang.InterruptedException e) {
                }
            } else {
                for (int boardAddress : activeBoards.keySet()) {
                    ActiveBoard board = activeBoards.get(boardAddress);
                    if (!board.doNotPoll() && !board.isSynced()) {
                        // Get Data is useless until a Reset Encoding State has been acknowledged
                        board.checkSpeedTimeout();
                        board.resendResetIfNeeded();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else if (!board.doNotPoll()) {
                        board.retrySpeedIfDue();
                        if (!board.isSynced()) {
                            continue; // a speed change has just started
                        }
                        log.debug("Poll board {}", boardAddress);
                        Dcc4PcMessage m = Dcc4PcMessage.pollBoard(boardAddress);
                        log.debug("queueing poll request for board {}", boardAddress);
                        tc.sendDcc4PcMessage(m, this);
                        synchronized (this) {
                            awaitingReply = true;
                            try {
                                wait(pollTimeout);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt(); // retain if needed later
                            }
                        }
                        int delay = shortCycleInterval;
                        synchronized (this) {
                            if (awaitingReply) {
                                log.warn("timeout awaiting poll response for board {}", boardAddress);
                                delay = pollTimeout;
                            }
                            try {
                                wait(delay);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt(); // retain if needed later
                            } finally {
                                /*awaitingDelay = false;*/
                            }
                        }
                    } else {
                        log.debug("Board set to Do Not Poll {}", boardAddress);
                    }
                    synchronized (this) {
                        if (stopPolling) {
                            log.debug("Polling stopped {}", stopPolling);
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
        }
    }

    ConcurrentHashMap<Integer, ActiveBoard> activeBoards = new ConcurrentHashMap<>(5);

    /**
     * Generate all the sensor and reporter details based upon
     * the reply message from the board.
     *
     * @param r Reply that we build the information from
     */
    protected void createSensorsFromReply(Dcc4PcReply r) {
        int boardAddress = r.getBoard();
        log.debug("createSensorsFromReply: Get enabled inputs {}", r.toHexString());

        String sensorPrefix = getSystemPrefix() + typeLetter() + boardAddress + ":";
        String reporterPrefix = getSystemPrefix() + "R" + boardAddress + ":";

        int x = 1;
        List<Integer> enabled = new ArrayList<>();
        for (int i = 0; i < r.getNumDataElements(); i++) {

            for (int j = 0; j < 8; j++) {
                Dcc4PcSensor s = (Dcc4PcSensor) createNewSensor(sensorPrefix + (j + x), null);
                register(s);
                s.setInput(j + x);
                activeBoards.get(boardAddress).addSensor(j + x, s);
                // one bit per input, the first input in bit 0
                if (((r.getElement(i) >> j) & 0x01) == 0x01) {
                    s.setEnabled(true);
                    enabled.add(j + x);
                }
                Dcc4PcReporter report = (Dcc4PcReporter) reportManager.createNewReporter(reporterPrefix + (j + x), null);
                activeBoards.get(boardAddress).addReporter(j + x, report);

            }
            x = x + 8;
        }
        log.info("Board {} has inputs {} enabled", boardAddress, enabled);
        activeBoards.get(boardAddress).setEnabledInputs(enabled);
        activeBoards.get(boardAddress).setDoNotPoll(false);
        log.debug("     created {} sensors", x);
    }

    @Override
    public void handleTimeout(Dcc4PcMessage m) {
        log.debug("timeout received to our last message {}", m);
        ActiveBoard board = activeBoards.get(m.getBoard());
        if (board != null) {
            if (m.getMessageType() == Dcc4PcMessage.INFO) {
                board.speedCheckDone(false);
            } else if (m.getMessageType() == Dcc4PcMessage.CHILDRESET) {
                board.resetReplied(false);
            } else if (m.getMessageType() == Dcc4PcMessage.CHILDPOLL) {
                // the reply may have been lost part way, so the stream can't be trusted
                board.requestResync("Get Data timed out");
            }
        }
        if (!stopPolling) {
            synchronized (this) {
                awaitingReply = false;
                this.notify();
            }
        }
    }

    @Override
    public void message(Dcc4PcMessage m) {

    }

    public void changeBoardAddress(int oldAddress, int newAddress) {
        // Block polling on this board
        ActiveBoard board = activeBoards.get(oldAddress);
        board.setDoNotPoll(true);
        Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) oldAddress, (byte) 0x03, (byte) newAddress});
        tc.sendDcc4PcMessage(m, null);
        // Need to stop polling otherwise we get a concurrent modification exception
        stopPolling();
        activeBoards.remove(oldAddress);
        activeBoards.put(newAddress, board);
        board.setAddress(newAddress);
        startPolling();
        String sensorPrefix = getSystemPrefix() + typeLetter() + newAddress + ":";
        String reporterPrefix = getSystemPrefix() + "R" + newAddress + ":";
        // We create a new set of sensors and reporters, but leave the old ones.
        for (int i = 1; i < board.getNumEnabledSensors() + 1; i++) {
            board.getSensorAtIndex(i).setEnabled(false);
            int input = board.getSensorAtIndex(i).getInput();
            Dcc4PcSensor s = (Dcc4PcSensor) createNewSensor(sensorPrefix + (input), null);
            register(s);
            s.setInput(input);
            s.setEnabled(true);
            board.addSensor(input, s);
            Dcc4PcReporter report = (Dcc4PcReporter) reportManager.createNewReporter(reporterPrefix + (input), null);
            board.addReporter(input, report);
        }
        // Need to update the sensors used.
        board.setDoNotPoll(false);
    }

    class ActiveBoard {

        ActiveBoard(int address, String version, int inputs, int encoding) {
            this.version = version;
            this.inputs = inputs;
            this.encoding = encoding;
            this.address = address;
        }

        void setAddress(int address) {
            this.address = address;
        }

        int inputs;
        int encoding;
        String version;
        String description;
        int address;

        int failedRequests = 0;

        void addFailedRequests() {
            failedRequests++;
        }

        void clearFailedRequests() {
            failedRequests = 0;
        }

        boolean doNotPoll = true;

        void setDoNotPoll(boolean poll) {
            doNotPoll = poll;
            if (!poll) {
                if (speedChecked) {
                    requestResync("polling enabled");
                } else {
                    startSpeedChange();
                }
            }
        }

        boolean doNotPoll() {
            return doNotPoll;
        }

        HashMap<Integer, Dcc4PcSensor> inputPorts = new HashMap<>(16);

        void addSensor(int port, Dcc4PcSensor sensor) {
            inputPorts.put(port, sensor);
        }

        String getEncodingAsString() {
            if ((encoding & 0x01) == 0x01) {
                return "Supports Cooked RailCom Encoding";

            } else {
                return "Supports Raw RailCom Encoding";
            }
        }

        void setDescription(String description) {
            this.description = description;
        }

        Dcc4PcSensor getSensorAtIndex(int i) {
            if (!inputPorts.containsKey(i)) {
                return null;
            }
            return inputPorts.get(i);
        }

        int getNumEnabledSensors() {
            return inputPorts.size();
        }

        HashMap<Integer, Dcc4PcReporter> inputReportersPorts = new HashMap<>(16);

        void addReporter(int port, Dcc4PcReporter reporter) {
            inputReportersPorts.put(port, reporter);
        }

        Dcc4PcReporter getReporterAtIndex(int i) {
            if (!inputReportersPorts.containsKey(i)) {
                return null;
            }
            return inputReportersPorts.get(i);
        }

        Dcc4PcRawDecoder decoder = new Dcc4PcRawDecoder(new int[0]);
        volatile boolean synced = false;
        volatile boolean resetPending = false;
        private final HashMap<Integer, Integer> appliedStates = new HashMap<>(16);

        void setEnabledInputs(List<Integer> enabled) {
            int[] inputs = new int[enabled.size()];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = enabled.get(i);
            }
            decoder = new Dcc4PcRawDecoder(inputs);
        }

        boolean isSynced() {
            return synced;
        }

        /**
         * Stop decoding and ask the reader to start its Raw encoding stream
         * again. Decoding resumes once the reader acknowledges the Reset
         * Encoding State.
         *
         * @param reason why, for the log
         */
        void requestResync(String reason) {
            if (synced) {
                log.info("Resynchronising board {}: {}", address, reason);
            } else {
                log.debug("Resynchronising board {}: {}", address, reason);
            }
            synced = false;
            sendReset();
        }

        /** Send another Reset Encoding State unless one is already waiting for its reply. */
        void resendResetIfNeeded() {
            if (!synced && !resetPending && !speedPending) {
                sendReset();
            }
        }

        volatile boolean speedChecked = false;
        volatile boolean speedPending = false;
        volatile long speedStarted = 0;
        int failedResets = 0;

        /** Give up on a speed change that never got an answer, so polling can start at 8 kbps. */
        void checkSpeedTimeout() {
            if (speedPending && System.currentTimeMillis() - speedStarted > 10000) {
                log.warn("No answer to the speed change for board {}", address);
                speedCheckDone(false);
            }
        }
        int busSpeed = 0;

        /**
         * Move this board to 62.5 kbps: at 8 kbps its buffer overflows between
         * polls and every Get Data takes over half a second. The board's Set
         * Speed comes first, then the interface's, then a Get Info to check the
         * board still answers; polling starts once that is settled.
         */
        private void startSpeedChange() {
            speedPending = true;
            speedStarted = System.currentTimeMillis();
            synced = false;
            log.info("Switching board {} to 62.5 kbps", address);
            sendWakeUp();
            tc.sendDcc4PcMessage(Dcc4PcMessage.setBoardSpeed(address, Dcc4PcMessage.SPEED_62K5), null);
            tc.sendDcc4PcMessage(Dcc4PcMessage.busSetSpeed(address, Dcc4PcMessage.SPEED_62K5), null);
            Dcc4PcMessage check = Dcc4PcMessage.getInfo(address);
            check.setTimeout(1000);
            tc.sendDcc4PcMessage(check, Dcc4PcSensorManager.this);
        }

        /**
         * The Get Info that follows a speed change has been answered or has
         * failed.
         *
         * @param ok true if the board answered at the new speed
         */
        void speedCheckDone(boolean ok) {
            if (!speedPending) {
                return;
            }
            if (ok) {
                busSpeed = Dcc4PcMessage.SPEED_62K5;
                speedRetryDelay = FIRST_SPEED_RETRY;
                log.info("Board {} is running at 62.5 kbps", address);
            } else {
                busSpeed = 0;
                // try again later: the board may only have been without track power
                nextSpeedRetry = System.currentTimeMillis() + speedRetryDelay;
                log.warn("Board {} did not answer at 62.5 kbps, going back to 8 kbps; trying again in {} s",
                        address, speedRetryDelay / 1000);
                speedRetryDelay = Math.min(speedRetryDelay * 2, MAX_SPEED_RETRY);
                tc.sendDcc4PcMessage(Dcc4PcMessage.busResetSpeeds(), null);
                tc.sendDcc4PcMessage(Dcc4PcMessage.busSetSpeed(address, 0), null);
            }
            speedChecked = true;
            speedPending = false;
            requestResync("bus speed set");
        }

        /**
         * A board that has just powered up (it runs on track power) ignores
         * every command except Get Info until it has answered a Get Info, so
         * send one before anything else. No listener: the reply is not the
         * speed check.
         */
        private void sendWakeUp() {
            tc.sendDcc4PcMessage(Dcc4PcMessage.getInfo(address), null);
        }

        private void sendReset() {
            resetPending = true;
            sendWakeUp();
            tc.sendDcc4PcMessage(Dcc4PcMessage.selectRawEncoding(address), null);
            Dcc4PcMessage m = Dcc4PcMessage.resetBoardData(address);
            m.setTimeout(2000);
            tc.sendDcc4PcMessage(m, Dcc4PcSensorManager.this);
        }

        /**
         * The reader answered a Reset Encoding State.
         *
         * @param ok true if it was accepted
         */
        void resetReplied(boolean ok) {
            if (!ok) {
                failedResets++;
                log.debug("Reset Encoding State failed for board {} ({} in a row)", address, failedResets);
                if (failedResets >= 3 && !speedPending) {
                    // The board is powered from the track, so a short or power cut restarts
                    // it at 8 kbps while the interface is still on the faster speed.
                    log.warn("Board {} not answering, setting the bus speed again from 8 kbps", address);
                    failedResets = 0;
                    busSpeed = 0;
                    tc.sendDcc4PcMessage(Dcc4PcMessage.busResetSpeeds(), null);
                    tc.sendDcc4PcMessage(Dcc4PcMessage.busSetSpeed(address, 0), null);
                    startSpeedChange();
                }
                resetPending = false;
                return;
            }
            failedResets = 0;
            // queued behind any Get Data replies that were already on their way, which are dropped
            packetProcessor.execute(() -> {
                decoder.reset();
                resetPending = false;
                synced = true;
                log.debug("Board {} synchronised", address);
            });
        }

        static final long FIRST_SPEED_RETRY = 5000;
        static final long MAX_SPEED_RETRY = 300000;
        long speedRetryDelay = FIRST_SPEED_RETRY;
        volatile long nextSpeedRetry = 0;

        /**
         * Called from the poll loop: if a speed change fell back to 8 kbps
         * (often because track power was still off), try 62.5 kbps again once
         * the retry time has come.
         */
        void retrySpeedIfDue() {
            if (synced && speedChecked && !speedPending && busSpeed == 0
                    && nextSpeedRetry != 0 && System.currentTimeMillis() >= nextSpeedRetry) {
                nextSpeedRetry = 0;
                startSpeedChange();
            }
        }

        /**
         * Decode one Get Data reply and update the sensors and reporters.
         * Called on the packet processor thread, in the order the replies
         * arrived.
         *
         * @param r the reply
         */
        void processRawReply(Dcc4PcReply r) {
            if (!synced) {
                log.debug("Board {} not synchronised, dropping {}", address, r.toHexString());
                return;
            }
            int[] data = new int[r.getNumDataElements()];
            for (int i = 0; i < data.length; i++) {
                data[i] = r.getElement(i) & 0xFF;
            }
            for (Dcc4PcRawDecoder.Packet p : decoder.decode(data)) {
                switch (p.kind) {
                    case OVERFLOW:
                        log.debug("Board {} buffer overflowed, packets lost", address);
                        break;
                    case UNKNOWN_COMMAND:
                        requestResync("unknown command in Raw encoding stream");
                        return;
                    default:
                        applyPacket(p);
                        break;
                }
            }
        }

        private void applyPacket(Dcc4PcRawDecoder.Packet p) {
            int[] inputs = decoder.getInputs();
            for (int i = 0; i < inputs.length; i++) {
                int state = sensorState(p.states[i]);
                Integer old = appliedStates.put(inputs[i], state);
                if (old != null && old == state) {
                    continue;
                }
                Dcc4PcSensor s = getSensorAtIndex(inputs[i]);
                if (s != null) {
                    s.setOwnState(state);
                }
                Dcc4PcReporter rep = getReporterAtIndex(inputs[i]);
                if (rep != null) {
                    rep.setRailComState(state);
                }
            }
            for (Dcc4PcRawDecoder.InputData d : p.railCom) {
                if (d.data == null || d.sourceDcc == null) {
                    continue; // a duplicate of data lost to an overflow
                }
                Dcc4PcReporter rep = getReporterAtIndex(d.input);
                if (rep == null) {
                    continue;
                }
                try {
                    decodeDCCPacket(d.sourceDcc);
                } catch (RuntimeException ex) {
                    log.error("decodeDCCPacket Exception", ex);
                    continue;
                }
                rep.setPacket(d.data, dcc_addr_type, addr, cvNumber, speed, 0);
            }
        }

        private int sensorState(int rawState) {
            switch (rawState) {
                case Dcc4PcRawDecoder.STATE_UNOCCUPIED:
                    return Sensor.INACTIVE;
                case Dcc4PcRawDecoder.STATE_OCCUPIED:
                    return Sensor.ACTIVE;
                case Dcc4PcRawDecoder.STATE_ORIENTATION_A:
                    return Dcc4PcSensor.ORIENTA;
                default:
                    return Dcc4PcSensor.ORIENTB;
            }
        }

        int addr = 0;
        int dcc_addr_type = NO_ADDRESS;
        int cvNumber = 0;
        int speed = 0;

        int[] lastDCCPacketSeen = new int[0];

        void decodeDCCPacket(int[] packet) {
            if (Arrays.equals(packet, lastDCCPacketSeen)) {
                return;
            }
            for (int idx = 0; idx < packet.length; ++idx) {
                lastDCCPacketSeen = packet.clone();
            }
            // lastDCCPacketSeen = packet;
            speed = 0;
            addr = 0;
            dcc_addr_type = NO_ADDRESS;
            if (log.isDebugEnabled()) {

                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < packet.length; ++i) {
                    buf.append(Integer.toHexString(packet[i])).append(",");
                }
                String s = buf.toString();
                log.debug("bytes to process {}", s);
            }
            // Basic Accessory Decoder packet 10aaaaaa 1aaacddd eeeeeeee
            // Extended Accessory decoder packet starts 10aaaaaa 0aaa0aa1 000xxxxx eeeeeeee
            int i = 0;
            // Skip accessory decoder packets at this point
            if ((packet[i] & 0xFF) == 0x00) {
                return;
                //Broadcast packet
            } else if ((packet[i] & 0xff) == 0xff) {
                // log.debug("Idle Packet");
                return;
            } else if ((packet[i] & 0x80) == 0x80) {
                // Accessory decoinfoder packet
                if ((packet[i] & 0x80) == 0x80) {
                    // i++;  don't increment as the for loop will do this
                    // basic Accessory Decoder packet one byte to follow
                } else {
                    i++;
                    // TODO extended decoder packet two bytes
                }
                return;
            } else {
                int addr_1;
                //The way that this determins of there is a two part extended address packet isn't great./
                if (packet.length > 0) {
                    addr_1 = packet[i] & 0xFF;
                    i++;
                } else {
                    addr_1 = 0;
                }
                if (addr_1 == 0) {
                    dcc_addr_type = NO_ADDRESS;
                } else if ((addr_1 >= 1) && (addr_1 <= 127)) { //Short address
                    dcc_addr_type = SHORT_ADDRESS;
                    addr = addr_1;
                } else if ((addr_1 >= 128) && (addr_1 <= 191)) {
                    dcc_addr_type = NO_ADDRESS; //this is an accessory decoder address should have already of been filtered out
                } else if ((addr_1 >= 192) && (addr_1 <= 231)) { //14 bit address
                    dcc_addr_type = LONG_ADDRESS;
                    addr = (((addr_1 & 0x3F) << 8) | (packet[i] & 0xFF));
                    i++;
                } else {
                    dcc_addr_type = NO_ADDRESS;
                }
                String addt;
                switch (dcc_addr_type) {
                    case LONG_ADDRESS:
                        addt = "Long";
                        break;
                    case SHORT_ADDRESS:
                        addt = "Short";
                        break;
                    default:
                        addt = "No Address";
                        break;
                }

                log.debug("DCC address type {} addr {}", addt, addr);
                if ((dcc_addr_type != NO_ADDRESS)) {
                    log.debug("Current index  {} value {}", i, packet[i] & 0xFF);
                    if ((packet[i] & 0xE0) == 0xE0) {
                        i++;
                        cvNumber = ((packet[i] & 0xff) + 1);
                        log.debug("CV Access cv:{}", cvNumber);
                        // two byte instruction when 1111
                        // three byte instuction when 1110
                    } else if ((packet[i] & 0xC0) == 0xC0) {
                        log.debug("Future");
                        i++;
                        if ((packet[i] & 0x1F) == 0x1F) {
                            // F21-F28 Two byte instruction
                            i++;
                        } else if ((packet[i] & 0x1E) == 0x1E) {
                            // F13-F20 Two byte instruction
                            i++;
                        } else if ((packet[i] & 0x1D) == 0x1D) {
                            // Two byte instruction
                            i++;

                        } else if ((packet[i] >> 3) == 0x00) {
                            // This should fall through to 00000 three byte instruction
                            // i = i + 2;

                        } else {
                            // Remainder are reserved
                        }
                        // Two or three byte instruction
                    } else if ((packet[i] & 0xA0) == 0xa0) {
                        log.debug("For Function Group 2");
                        if ((packet[i] & 0x10) == 0x10) {
                            log.debug("Functions 5 to 8");
                            if ((packet[i] & 0x08) == 0x08) {
                                log.debug("Function 8 on");
                            } else {
                                log.debug("Function 8 off");
                            }
                            if ((packet[i] & 0x04) == 0x04) {
                                log.debug("Function 7 on");
                            } else {
                                log.debug("Function 7 off");
                            }
                            if ((packet[i] & 0x02) == 0x02) {
                                log.debug("Function 6 on");
                            } else {
                                log.debug("Function 6 off");
                            }
                            if ((packet[i] & 0x01) == 0x01) {
                                log.debug("Function 5 on");
                            } else {
                                log.debug("Function 5 off");
                            }
                        } else {
                            log.debug("Functions 9 to 12");
                            if ((packet[i] & 0x08) == 0x08) {
                                log.debug("Function 12 on");
                            } else {
                                log.debug("Function 12 off");
                            }
                            if ((packet[i] & 0x04) == 0x04) {
                                log.debug("Function 11 on");
                            } else {
                                log.debug("Function 11 off");
                            }
                            if ((packet[i] & 0x02) == 0x02) {
                                log.debug("Function 10 on");
                            } else {
                                log.debug("Function 10 off");
                            }
                            if ((packet[i] & 0x01) == 0x01) {
                                log.debug("Function 9 on");
                            } else {
                                log.debug("Function 9 off");
                            }
                        }
                        // Single byte instruction
                        // i++;
                    } else if ((packet[i] & 0x80) == 0x80) {
                        log.debug("For Function Group 1");
                    } else if ((packet[i] & 0x60) == 0x60) {
                        speed = (packet[i] & 0xff) - 0x60;
                        log.debug("Speed for forward 14 speed steps {}", speed);
                        //Only a single byte instruction
                    } else if ((packet[i] & 0x40) == 0x40) {
                        speed = ((packet[i] & 0xff) - 0x40);
                        log.debug("Speed for reverse 14 speed steps {}", speed);
                        //Only a single byte instruction
                    } else if ((packet[i] & 0x20) == 0x20) {
                        log.debug("Advanced Op");
                        //Two byte instruction
                        if ((packet[i] & 0x1f) == 0x1f) {
                            i++;
                            log.debug("128 speed step control");
                            if ((packet[i] & 0x80) == 0x80) {
                                speed = ((packet[i]) & 0xff) - 0x80;
                                log.debug("Forward {}", speed);
                            } else {
                                speed = ((packet[i]) & 0xff);
                                log.debug("Reverse {}", speed);
                            }
                        }
                    } else {
                        log.debug("Decoder and Consist Instruction");
                        // decoder control is 0000
                        // consist control is 0001
                    }
                }

            }
            log.debug("---End Decode DCC Packet---");
        }

        int getInputs() {
            return inputs;
        }

        String getDescription() {
            return description;
        }

        String getVersion() {
            return version;
        }
    }

    public List<Integer> getBoards() {
        ArrayList<Integer> list = new ArrayList<>();
        Enumeration<Integer> keys = activeBoards.keys();
        while (keys.hasMoreElements()) {
            Integer key = keys.nextElement();
            list.add(key);
        } // end while
        return list;
    }

    public int getBoardInputs(int board) {
        if (!activeBoards.containsKey(board)) {
            return -1;
        }
        return activeBoards.get(board).getInputs();
    }

    public String getBoardEncodingAsString(int board) {
        if (!activeBoards.containsKey(board)) {
            return "unknown";
        }
        return activeBoards.get(board).getEncodingAsString();
    }

    public String getBoardVersion(int board) {
        if (!activeBoards.containsKey(board)) {
            return "unknown";
        }
        return activeBoards.get(board).getVersion();
    }

    public String getBoardDescription(int board) {
        if (!activeBoards.containsKey(board)) {
            return "unknown";
        }
        return activeBoards.get(board).getDescription();
    }

    protected boolean isBoardCreated(int address) {
        return activeBoards.containsKey(address);
    }

    protected void addActiveBoard(int address, String version, int inputs, int encoding) {
        activeBoards.put(address, new ActiveBoard(address, version, inputs, encoding));
    }

    protected void setBoardDescription(int address, String description) {
        ActiveBoard board = activeBoards.get(address);
        board.setDescription(description);
    }

    protected void createSensorsForBoard(Dcc4PcReply r) {
        if (r.getBoard() == -1) {
            log.debug("Reply has no board associated with it");
            return;
        }
        class SensorMaker implements Runnable {

            Dcc4PcReply reply;

            SensorMaker(Dcc4PcReply r) {
                reply = r;
            }

            @Override
            public void run() {
                createSensorsFromReply(reply);
            }
        }

        Thread thr = new Thread(new SensorMaker(r), "Dcc4PCSensor Maker board " + r.getBoard());
        try {
            thr.start();
        } catch (java.lang.IllegalThreadStateException ex) {
            log.error("Exception {} {}", thr.getName(), ex.getMessage());
        }
    }

    /**
     * Validates to contain at least 1 number . . .
     * <p>
     * TODO: Custom validation for Dcc4PcSensorManager could be improved.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateTrimmedMin1NumberSystemNameFormat(name,locale);
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dcc4PcSensorManager.class);

}

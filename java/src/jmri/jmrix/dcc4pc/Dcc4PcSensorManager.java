package jmri.jmrix.dcc4pc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import jmri.JmriException;
import jmri.Sensor;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

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
        this.reportManager = memo.get(jmri.ReporterManager.class);
        jmri.InstanceManager.store(this, Dcc4PcSensorManager.class);
        this.boardManager = new Dcc4PcBoardManager(tc, this);
        // Finally, create and register a shutdown task to ensure clean exit
        this.pollShutDownTask = new QuietShutDownTask("DCC4PC Board Poll Shutdown") {
            @Override
            public boolean execute() {
                stopPolling();
                return true;
            }
        };
        startPolling();
    }

    Dcc4PcReporterManager reportManager;
    ShutDownTask pollShutDownTask;
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

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s = new Dcc4PcSensor(systemName, userName);
        s.setUserName(userName);
        extractBoardID(systemName);
        return s;
    }

    /*
     * This extracts the board id out from the system name.
     */
    void extractBoardID(String systemName) {
        if (systemName.contains(":")) {
            int indexOfSplit = systemName.indexOf(":");
            systemName = systemName.substring(0, indexOfSplit);
            indexOfSplit = getSystemPrefix().length() + 1; //+1 includes the typeletter which is a char
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

    //we want the system name to be in the format of board:input
    @Override
    @Nonnull
    public String createSystemName(String curAddress, @Nonnull String prefix) throws JmriException {
        String iName;
        if (curAddress.contains(":")) {
            board = 0;
            channel = 0;
            // Address format passed is in the form of board:channel or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.parseInt(curAddress.substring(0, seperator));
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} into the cab and channel format of nn:xx", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
            }

            try {
                channel = Integer.parseInt(curAddress.substring(seperator + 1));
                if ((channel > 16) || (channel < 1)) {
                    log.error("Channel number is out of range");
                    throw new JmriException("Channel number should be in the range of 1 to 16");
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} into the cab and channel format of nn:xx", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
            }
            iName = curAddress;
            addBoard(board);
        } else {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
            throw new JmriException("Unable to convert " + curAddress + " Hardware Address to a number");
        }
        return prefix + typeLetter() + iName;
    }

    int board;
    int channel;

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }

        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                if (channel < 16) {
                    channel++;
                } else {
                    board++;
                    channel = 1;
                }
                s = getBySystemName(prefix + typeLetter() + board + ":" + channel);
                if (s == null) {
                    return board + ":" + channel;
                }
            }
            return null;
        } else {
            return curAddress;
        }
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

    protected void startPolling() {
        if (stopPolling && pollThread != null) {
            pollThread = null;
        }
        stopPolling = false;

        if (pollThread == null) {
            pollThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    pollManager();
                }
            }, "DCC4PC Sensor Poll");
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
        } else if (!r.isUnsolicited()) {
            synchronized (this) {
                awaitingReply = false;
                this.notify();
            }
            if (log.isDebugEnabled()) {
                log.debug("Get Data inputs {}", r.toHexString());
            }
            class ProcessPacket implements Runnable {

                Dcc4PcReply reply;

                ProcessPacket(Dcc4PcReply r) {
                    reply = r;
                }

                @Override
                public void run() {
                    ActiveBoard curBoard = activeBoards.get(r.getBoard());
                    if (curBoard != null) {
                        curBoard.processInputPacket(reply);
                    } else {
                        log.error("Board disappeared from system {}", r.getBoard());
                    }
                }
            }
            if (r.getBoard() > -1) {
                Thread thr = new Thread(new ProcessPacket(r), "Dcc4PCSensor Process Packet for " + r.getBoard());
                try {
                    thr.start();
                } catch (java.lang.IllegalThreadStateException ex) {
                    log.error(ex.toString());
                }
            } else {
                log.error("Do not know who this board message is for");
            }

        }
    }

    //This needs to be handled better possibly
    void getInputState(int[] longArray, int board) {
        String sensorPrefix = getSystemPrefix() + typeLetter() + board + ":";
        String reporterPrefix = getSystemPrefix() + "R" + board + ":";
        int inputNo = 1; //Maximum number of inputs is 16, but some might not be enabled, so need to handle this some how at a later date
        for (int i = 0; i < 4; i++) {
            Dcc4PcSensor s;
            Dcc4PcReporter r;
            int state = getInputState(longArray[i], inputNo);
            s = getSensor(sensorPrefix + (inputNo));
            if (s != null) {
                s.setOwnState(state);
            }
            r = ((Dcc4PcReporter) reportManager.getReporter(reporterPrefix + (inputNo)));
            if (r != null) {
                r.setRailComState(state);
            }
            inputNo++;

            state = getInputState(longArray[i], inputNo);
            s = getSensor(sensorPrefix + (inputNo));
            if (s != null) {
                s.setOwnState(state);
            }
            r = ((Dcc4PcReporter) reportManager.getReporter(reporterPrefix + (inputNo)));
            if (r != null) {
                r.setRailComState(state);
            }
            inputNo++;

            state = getInputState(longArray[i], inputNo);
            s = getSensor(sensorPrefix + (inputNo));
            if (s != null) {
                s.setOwnState(state);
            }
            r = ((Dcc4PcReporter) reportManager.getReporter(reporterPrefix + (inputNo)));
            if (r != null) {
                r.setRailComState(state);
            }
            inputNo++;

            state = getInputState(longArray[i], inputNo);
            s = getSensor(sensorPrefix + (inputNo));
            if (s != null) {
                s.setOwnState(state);
            }
            r = ((Dcc4PcReporter) reportManager.getReporter(reporterPrefix + (inputNo)));
            if (r != null) {
                r.setRailComState(state);
            }
            inputNo++;
        }
    }

    int getInputState(int value, int input) {
        int lastbit = 7;
        switch (input) {
            case 5:
            case 9:
            case 13:
            case 1:
                lastbit = 1;
                break;
            case 6:
            case 10:
            case 14:
            case 2:
                lastbit = 3;
                break;
            case 7:
            case 11:
            case 15:
            case 3:
                lastbit = 5;
                break;
            case 8:
            case 12:
            case 16:
            case 4:
                lastbit = 7;
                break;
            default:
                break;
        }
        int tempValue = value << (31 - lastbit);
        switch (tempValue >>> (31 - lastbit + (lastbit - 1))) {
            case 0:
                return Sensor.INACTIVE;
            case 1:
                return Sensor.ACTIVE;
            case 2:
                return Dcc4PcSensor.ORIENTA; //Occupied RailCom Orientation A
            case 3:
                return Dcc4PcSensor.ORIENTB; //Occupied RailCom Orientation B
            default:
                return Sensor.UNKNOWN;
        }
    }

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

    public final static int NO_ADDRESS = 0x00;
    public final static int SHORT_ADDRESS = 0x02;
    public final static int LONG_ADDRESS = 0x04;
    public final static int CONSIST_ADDRESS = 0x08;

    /**
     * Determining if the railcommdata is duplicated. If it is then this
     * instructs the rc input to move things about.
     */
    int decodeDuplicatePacket(int value, int seq, Dcc4PcReporter rc) {
        int lastbit = 7;
        //probably a better way of doing this..
        while (seq >= 4) {
            seq = seq - 4;
        }

        switch (seq) {
            case 0:
                lastbit = 1;
                break;
            case 1:
                lastbit = 3;
                break;
            case 2:
                lastbit = 5;
                break;
            case 3:
                lastbit = 7;
                break;
            default:
                break;
        }
        int tempValue = value << (31 - lastbit);
        tempValue = (tempValue >>> (31 - lastbit + (lastbit - 1)));
        if (tempValue != 0) {
            rc.duplicatePacket(tempValue);
        }
        return tempValue;
    }

    private final int shortCycleInterval = 1; // Time to wait between sending out poll messages
    private final int pollTimeout = 600;    // in case of lost response
    private boolean awaitingReply = false;

    void pollManager() {
        for (int boardAddress : activeBoards.keySet()) {
            Dcc4PcMessage m = Dcc4PcMessage.resetBoardData(boardAddress);
            m.setTimeout(100);
            tc.sendDcc4PcMessage(m, null);
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
                    if (!activeBoards.get(boardAddress).doNotPoll()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Poll board {}", boardAddress);
                        }
                        Dcc4PcMessage m = Dcc4PcMessage.pollBoard(boardAddress);
                        if (log.isDebugEnabled()) {
                            log.debug("queueing poll request for board {}", boardAddress);
                        }
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
                        if (log.isDebugEnabled()) {
                            log.debug("Board set to Do Not Poll {}", boardAddress);
                        }
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

    ConcurrentHashMap<Integer, ActiveBoard> activeBoards = new ConcurrentHashMap<Integer, ActiveBoard>(5);

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
        for (int i = 0; i < r.getNumDataElements(); i++) {

            for (int j = 0; j < 8; j++) {
                Dcc4PcSensor s = (Dcc4PcSensor) createNewSensor(sensorPrefix + (j + x), null);
                register(s);
                s.setInput(j + x);
                activeBoards.get(boardAddress).addSensor(j + x, s);
                if ((r.getElement(i) & 0x01) == 0x01) {
                    s.setEnabled(true);
                }
                Dcc4PcReporter report = (Dcc4PcReporter) reportManager.createNewReporter(reporterPrefix + (j + x), null);
                activeBoards.get(boardAddress).addReporter(j + x, report);

            }
            x = x + 8;
        }
        activeBoards.get(boardAddress).setDoNotPoll(false);
        log.debug("     created {} sensors", x);
    }

    @Override
    public void handleTimeout(Dcc4PcMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("timeout received to our last message " + m.toString());
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
            if (!poll) {
                Dcc4PcMessage m = Dcc4PcMessage.resetBoardData(address);
                m.setTimeout(100);
                tc.sendDcc4PcMessage(m, null);
            }
            doNotPoll = poll;
        }

        boolean doNotPoll() {
            return doNotPoll;
        }

        HashMap<Integer, Dcc4PcSensor> inputPorts = new HashMap<Integer, Dcc4PcSensor>(16);

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

        HashMap<Integer, Dcc4PcReporter> inputReportersPorts = new HashMap<Integer, Dcc4PcReporter>(16);

        void addReporter(int port, Dcc4PcReporter reporter) {
            inputReportersPorts.put(port, reporter);
        }

        Dcc4PcReporter getReporterAtIndex(int i) {
            if (!inputReportersPorts.containsKey(i)) {
                return null;
            }
            return inputReportersPorts.get(i);
        }

        synchronized void processInputPacket(Dcc4PcReply r) {
            if (log.isDebugEnabled()) {
                log.debug("==== Process Packet ====");
                log.debug(r.toHexString());
            }
            int packetTypeCmd = 0x00;
            int currentByteLocation = 0;
            while (currentByteLocation < r.getNumDataElements()) {
                log.debug("--- Start {} ---", currentByteLocation);
                int oldstart = currentByteLocation;
                if ((r.getElement(currentByteLocation) & 0x80) == 0x80) {
                    log.debug("Error at head");
                    packetTypeCmd = 0x03;
                    ++currentByteLocation;
                } else if ((r.getElement(currentByteLocation) & 0x40) == 0x40) {
                    log.debug("Correct Type 2 packet");
                    currentByteLocation = processPacket(r, 0x02, packetTypeCmd, currentByteLocation);
                } else {
                    log.debug("Correct Type 1 packet");
                    currentByteLocation = processPacket(r, 0x01, packetTypeCmd, currentByteLocation);
                }
                if (log.isDebugEnabled()) {
                    StringBuilder buf = new StringBuilder();
                    for (int i = oldstart; i < currentByteLocation; i++) {
                        buf.append(Integer.toHexString(r.getElement(i) & 0xff) + ",");
                    }
                    log.debug(buf.toString());
                    log.debug("--- finish packet {} ---", (currentByteLocation - 1));
                }
            }
            log.debug("==== Finish Processing Packet ====");
        }

        public int processPacket(Dcc4PcReply r, int packetType, int packetTypeCmd, int currentByteLocation) {
            // int packetType;
            int dccpacketlength;
            if (packetType == 0x02) {
                dccpacketlength = (r.getElement(currentByteLocation) - 0x40) + 1;
            } else {
                dccpacketlength = (r.getElement(currentByteLocation)) + 1;
            }

            ++currentByteLocation;

            int[] dcc_Data = new int[dccpacketlength];

            for (int i = 0; i < dccpacketlength; i++) {
                dcc_Data[i] = r.getElement(currentByteLocation);
                ++currentByteLocation;
            }
            try {
                decodeDCCPacket(dcc_Data);
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }

            if (packetType == 0x02) {
                getInputState(Arrays.copyOfRange(r.getDataAsArray(), currentByteLocation, currentByteLocation + 4), address);
                currentByteLocation = currentByteLocation + 4;
            }

            ArrayList<Dcc4PcReporter> railCommDataForSensor = new ArrayList<Dcc4PcReporter>();
            for (int i = 1; i < getNumEnabledSensors() + 1; i++) {
                if (getReporterAtIndex(i).getRailComState() >= Dcc4PcSensor.ORIENTA) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding reporter for input {}", getReporterAtIndex(i).getSystemName());
                    }
                    railCommDataForSensor.add(getReporterAtIndex(i));
                }
            }

            int railComDupPacket = (int) Math.ceil((railCommDataForSensor.size()) / 4.0f);
            log.debug("We have {} Byte(s) to read on data", railComDupPacket);

            log.debug("Now to handle the duplicate packet data");
            int j = 0;

            for (int i = 0; i < railComDupPacket; i++) {
                int inputNo = 0;
                int dup = decodeDuplicatePacket(r.getElement(currentByteLocation), inputNo, railCommDataForSensor.get(j));
                if (log.isDebugEnabled()) {
                    log.debug("Input " + railCommDataForSensor.get(j).getDisplayName() + " - " + dup);
                }
                if (dup == 0) {
                    j++;
                } else {
                    railCommDataForSensor.remove(j);
                }

                inputNo++;
                if (j < railCommDataForSensor.size()) {
                    dup = decodeDuplicatePacket(r.getElement(currentByteLocation), inputNo, railCommDataForSensor.get(j));
                    if (log.isDebugEnabled()) {
                        log.debug("Input " + railCommDataForSensor.get(j).getDisplayName() + " - " + dup);
                    }
                    if (dup == 0) {
                        j++;
                    } else {
                        railCommDataForSensor.remove(j);
                    }
                    inputNo++;
                }
                if (j < railCommDataForSensor.size()) {
                    dup = decodeDuplicatePacket(r.getElement(currentByteLocation), inputNo, railCommDataForSensor.get(j));
                    if (log.isDebugEnabled()) {
                        log.debug("Input " + railCommDataForSensor.get(j).getDisplayName() + " - " + dup);
                    }
                    if (dup == 0) {
                        j++;
                    } else {
                        railCommDataForSensor.remove(j);
                    }

                    inputNo++;
                }
                if (j < railCommDataForSensor.size()) {
                    dup = decodeDuplicatePacket(r.getElement(currentByteLocation), inputNo, railCommDataForSensor.get(j));
                    if (log.isDebugEnabled()) {
                        log.debug("Input " + railCommDataForSensor.get(j).getDisplayName() + " - " + dup);
                    }
                    if (dup == 0) {
                        j++;
                    } else {
                        railCommDataForSensor.remove(j);
                    }
                }
                currentByteLocation++;
            }

            if (log.isDebugEnabled()) {
                for (int i = 0; i < railCommDataForSensor.size(); i++) {
                    log.debug("Data for sensor " + railCommDataForSensor.get(i).getDisplayName());
                }
            }
            // re-use the variable to gather the size of each railcom input data
            railComDupPacket = (int) Math.ceil((railCommDataForSensor.size()) / 2.0f);
            if (log.isDebugEnabled()) {
                log.debug("We have " + railComDupPacket + " size byte(s) to read on data");
            }

            // This now becomes the length bytes for the rail comm information
            j = 0;
            for (int i = 0; i < railComDupPacket; i++) {
                int tempValue = r.getElement(currentByteLocation) << (31 - 3);
                tempValue = (tempValue >>> (31 - 3 + (0)));
                railCommDataForSensor.get(j).setPacketLength(tempValue);
                j++;
                if (j < railCommDataForSensor.size()) {
                    tempValue = r.getElement(currentByteLocation) << (31 - 7);
                    tempValue = (tempValue >>> (31 - 7 + 4));
                    railCommDataForSensor.get(j).setPacketLength(tempValue);
                    j++;
                }
                currentByteLocation++;
            }
            for (int i = 0; i < railCommDataForSensor.size(); i++) {
                log.debug(railCommDataForSensor.get(i).getDisplayName() + " " + railCommDataForSensor.get(i).getPacketLength());
                int[] arraytemp = new int[railCommDataForSensor.get(i).getPacketLength()];
                for (j = 0; j < railCommDataForSensor.get(i).getPacketLength(); j++) {
                    arraytemp[j] = 0xFF & r.getElement(currentByteLocation);
                    currentByteLocation++;
                }
                railCommDataForSensor.get(i).setPacket(arraytemp, dcc_addr_type, addr, cvNumber, speed, packetTypeCmd);
            }
            return currentByteLocation;
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
                    buf.append(Integer.toHexString(packet[i]) + ",");
                }
                String s = buf.toString();
                log.debug("bytes to process " + s);
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
                    // extended decoder packet two bytes to follow
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

                log.debug("DCC address type " + addt + " addr " + addr);
                if ((dcc_addr_type != NO_ADDRESS)) {
                    log.debug("Current index  " + i + " value " + (packet[i] & 0xFF));
                    if ((packet[i] & 0xE0) == 0xE0) {
                        i++;
                        cvNumber = ((packet[i] & 0xff) + 1);
                        log.debug("CV Access cv:" + cvNumber);
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
                        log.debug("Speed for forward 14 speed steps " + speed);
                        //Only a single byte instruction
                    } else if ((packet[i] & 0x40) == 0x40) {
                        speed = ((packet[i] & 0xff) - 0x40);
                        log.debug("Speed for reverse 14 speed steps " + speed);
                        //Only a single byte instruction
                    } else if ((packet[i] & 0x20) == 0x20) {
                        log.debug("Advanced Op");
                        //Two byte instruction
                        if ((packet[i] & 0x1f) == 0x1f) {
                            i++;
                            log.debug("128 speed step control");
                            if ((packet[i] & 0x80) == 0x80) {
                                speed = ((packet[i]) & 0xff) - 0x80;
                                log.debug("Forward " + speed);
                            } else {
                                speed = ((packet[i]) & 0xff);
                                log.debug("Reverse " + speed);
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
        ArrayList<Integer> list = new ArrayList<Integer>();
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
        if (activeBoards.containsKey(address)) {
            return true;
        }
        return false;
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
            log.error(ex.toString());
        }
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcSensorManager.class);

}

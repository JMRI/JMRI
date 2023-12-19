package jmri.jmrix.loconet.locobuffer;

import java.util.Arrays;
import java.util.Vector;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnPacketizerStrict;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to LocoNet via a LocoBuffer attached to a serial com port.
 * <p>
 * Normally controlled by the LocoBufferFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 */
public class LocoBufferAdapter extends LnPortController {

    public LocoBufferAdapter() {
        this(new LocoNetSystemConnectionMemo());
    }

    public LocoBufferAdapter(LocoNetSystemConnectionMemo adapterMemo) {
        super(adapterMemo);
        option1Name = "FlowControl"; // NOI18N
        option2Name = "CommandStation"; // NOI18N
        option3Name = "TurnoutHandle"; // NOI18N
        option4Name = "PacketizerType"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("XconnectionUsesLabel", Bundle.getMessage("TypeSerial")), validOption1));  // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), getCommandStationListWithStandaloneLN(), false));  // NOI18N
        options.put(option3Name, new Option(Bundle.getMessage("TurnoutHandling"),
                new String[]{Bundle.getMessage("HandleNormal"), Bundle.getMessage("HandleSpread"), Bundle.getMessage("HandleOneOnly"), Bundle.getMessage("HandleBoth")})); // I18N
        options.put(option4Name, new Option(Bundle.getMessage("PacketizerTypeLabel"), packetizerOptions()));  // NOI18N
        options.put("TranspondingPresent", new Option(Bundle.getMessage("TranspondingPresent"),
                new String[]{Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")} )); // NOI18N
        options.put("InterrogateOnStart", new Option(Bundle.getMessage("InterrogateOnStart"),
                new String[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo")} )); // NOI18N
        options.put("LoconetProtocolAutoDetect", new Option(Bundle.getMessage("LoconetProtocolAutoDetectLabel"),
                new String[]{Bundle.getMessage("ButtonNo"),Bundle.getMessage("LoconetProtocolAutoDetect")} )); // NOI18N
    }
    
    /**
     * Create a list of possible command stations and append "Standalone LocoNet"
     * 
     * Note: This is not suitable for use by any class which extends this class if
     * the hardware interface is part of a command station.
     * 
     * @return String[] containing the array of command stations, plus "Standalone 
     *          LocoNet"
     */
    public String[] getCommandStationListWithStandaloneLN() {
        String[] result = new String[commandStationNames.length + 1];
        for (int i = 0 ; i < result.length-1; ++i) {
            result[i] = commandStationNames[i];
        }
        result[commandStationNames.length] = LnCommandStationType.COMMAND_STATION_STANDALONE.getName();
        return result;
    }
    
    Vector<String> portNameVector = null;

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect LocoBuffer to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        reportOpen(portName);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        setLocalFlowControl();

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * Allow subtypes to change the opening message
     * @param portName To appear in message
     */
    protected void reportOpen(String portName) {
        log.info("Connecting LocoBuffer via {} {}", portName, currentSerialPort);
    }
    
    /**
     * Allow subtypes to change the flow control algorithm
     */
    protected void setLocalFlowControl() {
        FlowControl flow = FlowControl.RTSCTS;
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = FlowControl.NONE;
        }
        setFlowControl(currentSerialPort, flow);
    }
    
    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     * 
     * @return an indication of whether the interface is accepting transmit messages.
     */
    @Override
    public boolean okToSend() {
        return currentSerialPort.getCTS();
    }

    /**
     * Set up all of the other objects to operate with a LocoBuffer connected to
     * this port.
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        setTranspondingAvailable(getOptionState("TranspondingPresent"));
        setInterrogateOnStart(getOptionState("InterrogateOnStart"));
        setLoconetProtocolAutoDetect(getOptionState("LoconetProtocolAutoDetect"));
        // connect to a packetizing traffic controller
        LnPacketizer packets = getPacketizer(getOptionState(option4Name));
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config

        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable, mInterrogateAtStart, mLoconetProtocolAutoDetect);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud19200LB"), Bundle.getMessage("Baud57600LB")};
    protected int[] validSpeedValues = new int[]{19200, 57600};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionHwRecomm"), Bundle.getMessage("FlowOptionNo")};

    /**
     *  Define the readable data and internal code
     */
    private static String[][] packetizers = { {Bundle.getMessage("PacketizerTypelnPacketizer"),"lnPacketizer" },
            {Bundle.getMessage("PacketizerTypelnPacketizerStrict"),"lnPacketizerStrict"} };

    /**
     *
     * @return String array of readable choices
     */
    private String[] packetizerOptions() {
        String[] retval = new String[packetizers.length];
        for (int i=0;i < packetizers.length; i++) {
            retval[i] = packetizers[i][0];
        }
        return retval;
    }
    /**
     * for a given readable choice return internal value
     * or the default
     *
     * @param s  string containing ?a packetizer name?
     * @return internal value
     */
    protected String getPacketizerOption(String s) {
        for (int i=0;i < packetizers.length; i++) {
            if (packetizers[i][0].equals(s)) {
                return packetizers[i][1];
            }
        }
        return "lnPacketizer";
    }
    /**
     * 
     * @param s the packetizer to use in its readable form.
     * @return a LnPacketizer
     */
    protected LnPacketizer getPacketizer(String s) {
        LnPacketizer packets;
        String packetSelection = getPacketizerOption(s);
        switch (packetSelection) {
            case "lnPacketizer":
                packets = new LnPacketizer(this.getSystemConnectionMemo());
                break;
            case "lnPacketizerStrict":
                packets = new LnPacketizerStrict(this.getSystemConnectionMemo());
                break;
            default:
                packets = new LnPacketizer(this.getSystemConnectionMemo());
                log.warn("Using Normal do not understand option [{}]", packetSelection);
        }
        return packets;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoBufferAdapter.class);

}

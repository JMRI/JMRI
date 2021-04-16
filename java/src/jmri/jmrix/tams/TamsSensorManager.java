package jmri.jmrix.tams;

import java.util.Hashtable;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement sensor manager for Tams systems. The Manager handles all the state\
 * changes Requires v1.4.7 or higher of TAMS software to work correctly
 * <p>
 * System names are "TSnnn:yy", where T is the user configurable system prefix,
 * nnn is the Tams Object Number for a given S88 Bus Module and
 * yy is the port on that module.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 * @author Jan Boen and Sergiu Costan
 *
 * Rework Poll for status using binary commands send xEvtSen (78 CB)h this
 * returns multiple bytes first byte address of the S88 sensor, second and third
 * bytes = values of that sensor this repeats for each sensor with changes the
 * last byte contains 00h this means all reports have been received
 *
 * xEvtSen reports sensor changes
 */
public class TamsSensorManager extends jmri.managers.AbstractSensorManager implements TamsListener {

    public int maxSE; //Will hold the highest value of board number x 2 and we use this value to determine to tell the Tams MC how many S88 half-modules to poll

    public TamsSensorManager(TamsSystemConnectionMemo memo) {
        super(memo);
        TamsTrafficController tc = memo.getTrafficController();
        //Connect to the TrafficManager
        tc.addTamsListener(this);
        TamsMessage tm = TamsMessage.setXSR();//auto reset after reading S88
        tc.sendTamsMessage(tm, this);
        log.debug("Sending TamsMessage = {} , isBinary = {} and replyType = {}",
                tm.toString(), tm.isBinary(), tm.getReplyType());
        //Add polling for sensor state changes
        tm = TamsMessage.getXEvtSen(); //reports only sensors with changed states
        //tc.sendTamsMessage(tm, this);
        tc.addPollMessage(tm, this);
        log.debug("TamsMessage added to poll queue = {} {} and replyType = {}",
                jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, ""),
                jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, ""),
                tm.getReplyType());
    }

    //The hash table simply holds the object number against the TamsSensor ref.
    private final Hashtable<Integer, Hashtable<Integer, TamsSensor>> _ttams = new Hashtable<>(); // stores known Tams Obj

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public TamsSystemConnectionMemo getMemo() {
        return (TamsSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * System name is normalized to ensure uniqueness.
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        TamsTrafficController tc = getMemo().getTrafficController();
        TamsSensor s = new TamsSensor(systemName, userName);
        log.debug("Creating new TamsSensor: {}", systemName);
        if (systemName.contains(":")) {
            int board;
            int channel;

            String curAddress = systemName.substring(getSystemPrefix().length() + 1);
            int seperator = curAddress.indexOf(':');
            try {
                board = Integer.parseInt(curAddress.substring(0, seperator));
                log.debug("Creating new TamsSensor with board: {}", board);
                if (!_ttams.containsKey(board)) {
                    _ttams.put(board, new Hashtable<>());
                    //log.debug("_ttams: {}", _ttams.toString());
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert " +  // NOI18N
                        systemName.substring(getSystemPrefix().length() + 1) +
                        " into the Module and port format of nn:xx"); // NOI18N
            }
            Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
            try {
                channel = Integer.parseInt(curAddress.substring(seperator + 1));
                if (!sensorList.containsKey(channel)) {
                    sensorList.put(channel, s);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert " +  // NOI18N
                        systemName.substring(getSystemPrefix().length() + 1) +
                        " into the Module and port format of nn:xx"); // NOI18N
            }
            if ((board * 2) > maxSE) {//Check if newly defined board number is higher than what we know
                maxSE = board * 2;//adjust xSE and inform Tams MC
                log.debug("Changed xSE to {}", maxSE);
                TamsMessage tm = new TamsMessage("xSE " + Integer.toString(maxSE));
                tm.setBinary(false);
                tm.setReplyType('S');
                tc.sendTamsMessage(tm, this);
            }
        }
        //Probably sending the status check 16 times but should work...
        //Get initial status of sensors
        TamsMessage tm = TamsMessage.setXSensOff(); //force report from sensors with at least 1 port set
        tc.sendTamsMessage(tm, this);
        tm = TamsMessage.getXEvtSen(); //reports only sensors with changed states
        tc.sendTamsMessage(tm, this);
        log.debug("Returning this sensor: {}", s.toString());
        return s;
    }

    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (!curAddress.contains(":")) {
            throw new JmriException("Hardware Address passed should be past in the form 'Module:port', was " + curAddress);
        }

        //Address format passed is in the form of board:channel or T:turnout address
        int seperator = curAddress.indexOf(':');
        try {
            board = Integer.parseInt(curAddress.substring(0, seperator));
        } catch (NumberFormatException ex) {
            throw new JmriException("First part of "+curAddress+" in front of : should be a number");
        }
        try {
            port = Integer.parseInt(curAddress.substring(seperator + 1));
        } catch (NumberFormatException ex) {
            throw new JmriException("Port Address, Second part of "+curAddress+" after : should be a number");
        }

        if (port == 0 || port > 16) {
            throw new JmriException("Port number in "+curAddress+" must be between 1 and 16");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPrefix());
        sb.append("S");
        sb.append(board);
        sb.append(":");
        //Little work around to pad single digit address out.
        padPortNumber(port, sb);
        return sb.toString();
    }

    /**
     * Validates to contain at least 1 number . . .
     * <p>
     * TODO: add custom TamsSensor validation.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateTrimmedMin1NumberSystemNameFormat(name,locale);
    }
    
    int board = 0;
    int port = 0;

    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {

        String tmpSName = createSystemName(curAddress, prefix);
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null || ignoreInitialExisting) {
            port++;
            while (port < 17) {
                try {
                    tmpSName = createSystemName(board + ":" + port, prefix);
                } catch (JmriException e) {
                    throw new JmriException("Error creating system name from "+curAddress+" for "+board+":"+port);
                }
                s = getBySystemName(tmpSName);
                if (s == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(board);
                    sb.append(":");
                    //Little work around to pad single digit address out.
                    padPortNumber(port, sb);
                    return sb.toString();
                }
                port++;
            }
            throw new JmriException("Error creating system name from "+curAddress+" , port needs to be less than 16");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(board);
            sb.append(":");
            //Little work around to pad single digit address out.
            padPortNumber(port, sb);
            return sb.toString();
        }
    }

    void padPortNumber(int portNo, StringBuilder sb) {
        if (portNo < 10) {
            sb.append("0");
        }
        sb.append(portNo);
    }

    /**
     * Determine if it is possible to add a range of sensors in numerical order
     * eg 1 to 16, primarily used to enable/disable the add range box in the add
     * sensor panel.
     *
     * @return true
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    // to listen for status changes from Tams system
    @Override
    public void reply(TamsReply r) {
        //log.debug("ReplyType = " + tm.getReplyType() + ", Binary? = " +  tm.isBinary()+ ", OneByteReply = " + tm.getReplyOneByte());
        if (TamsTrafficController.replyType == 'S') {//Only handle Sensor events
            log.debug("*** Tams Sensor Reply ***");
            if (TamsTrafficController.replyBinary) {
                log.debug("Reply to binary command = {}", r.toString());
                if ((r.getNumDataElements() > 1) && (r.getElement(0) > 0x00)) {
                    // Here we break up a long sensor related TamsReply into individual S88 module status'
                    int numberOfReplies = r.getNumDataElements() / 3;
                    //log.debug("Incoming Reply = ");
                    //for (int i = 0; i < r.getNumDataElements(); i++) {
                        //log.debug("Byte " + i + " = " + jmri.util.StringUtil.appendTwoHexFromInt(r.getElement(i) & 0xFF, ""));
                    //}
                    //log.debug("length of reply = " + r.getNumDataElements() + " & number of replies = " + numberOfReplies);
                    for (int i = 0; i < numberOfReplies; i++) {
                        //create a new TamsReply and pass it to the decoder
                        TamsReply tr = new TamsReply();
                        tr.setBinary(true);
                        tr.setElement(0, r.getElement(3 * i));
                        tr.setElement(1, r.getElement(3 * i + 1));
                        tr.setElement(2, r.getElement(3 * i + 2));
                        log.debug("Going to pass this to the decoder = {} {} {}",
                                tr.getElement(0), tr.getElement(1), tr.getElement(2));
                        //The decodeSensorState will do the actual decoding of each individual S88 port
                        decodeSensorState(tr);
                    }
                }
            }
        }
    }

    @Override
    public void message(TamsMessage m) {
        // messages are ignored
    }

    private void decodeSensorState(TamsReply r) {
        //reply to XEvtSen consists of 3 bytes per S88 module
        //byte 1 = S88 board number 1 to 52 in binary format
        //byte 2 = bits 1 to 8
        //byte 3 = bits 9 to 16
        String sensorprefix = getSystemPrefix() + "S" + r.getElement(0) + ":";
        log.debug("Decoding sensor: {}", sensorprefix);
        log.debug("Lower Byte: {}", r.getElement(1));
        log.debug("Upper Byte: {}", r.getElement(2));
        Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
        int i = (r.getElement(1) & 0xff) << 8;//first 8 ports in second element of the reply
        log.debug("i after loading first byte= {}", Integer.toString(i,2));
        i = i + (r.getElement(2) & 0xff);//first 8 ports in third element of the reply
        log.debug("i after loading second byte= {}", Integer.toString(i,2));
        int mask = 0b1000000000000000;
        for (int j = 1; j <= 16; j++) {
            int result = i & mask;
            //log.debug("mask= {}", Integer.toString(mask, 2));
            //log.debug("result= {}", Integer.toString(result, 2));
            if (sensorList != null) {
                TamsSensor ms = sensorList.get(j);
                log.debug("ms: {}", ms);
                if (ms == null) {
                    log.debug("ms = NULL!");
                    StringBuilder sb = new StringBuilder();
                    sb.append(sensorprefix);
                    //Little work around to pad single digit address out.
                    padPortNumber(j, sb);
                    try {
                        ms = (TamsSensor) provideSensor(sb.toString());
                    } catch (Exception e){
                        log.warn("Could not provide Sensor {}: {}",sb.toString(),e.getLocalizedMessage());
                    }
                }
                if (ms != null) {
                    log.debug("ms = exists and is not null");
                    if (result == 0) {
                        ms.setOwnState(Sensor.INACTIVE);
                        log.debug("{}{} INACTIVE", sensorprefix, j);
                    } else {
                        log.debug("{}{} ACTIVE", sensorprefix, j);
                        ms.setOwnState(Sensor.ACTIVE);
                    }
                }
            }
            mask = mask / 2;
        }
        log.debug("sensor decoding is done");
    }

    private final static Logger log = LoggerFactory.getLogger(TamsSensorManager.class);
}

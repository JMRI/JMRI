// TamsSensorManager.java
package jmri.jmrix.tams;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement sensor manager for Tams systems. The Manager handles all the state\
 * changes Requires v1.4.7 or higher of TAMS software to work correctly
 * <P>
 * System names are "TMSnnn:yy", where nnn is the Tams Object Number for a given
 * S88 Bus Module and yy is the port on that module.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 * @author Jan Boen & Sergiu Costan
 * @version $Revision: 20820 $
 * 
 *          Rework Poll for status using binary commands send xEvtSen (78 CB)h
 *          this returns multiple bytes first byte address of the S88 sensor,
 *          second and third bytes = values of that sensor this repeats for each
 *          sensor with changes the last byte contains 00h this means all
 *          reports have been received
 * 
 *          xEvtSen reports sensor changes
 */
public class TamsSensorManager extends jmri.managers.AbstractSensorManager implements TamsListener {

    //Create a local TamsMessage Queue which we will use in combination with TamsReplies
    private Queue<TamsMessage> tmq = new LinkedList<TamsMessage>();
        
    //This dummy message is used in case we expect a reply from polling
    static private TamsMessage myDummy() {
        //log.info("*** myDummy ***");
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.POLLMSG & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XEVTSEN & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(false);
        m.setReplyType('S');
        return m;
    }
    static private TamsMessage xSR() {
        //log.info("*** xSR ***");
        TamsMessage m = new TamsMessage("xSR 1");
        m.setBinary(false);
        m.setReplyOneByte(false);
        m.setReplyType('S');
        return m;
    }
    //A local TamsMessage is held at all time
    //When no TamsMessage is being generated via the UI this dummy is used which means the TamsReply is a result of polling
    TamsMessage tm = myDummy();

    public int maxSE; //Will hold the highest value of board number x 2 and we use this value to determine to tell the Tams MC how many S88 half-modules to poll

    public TamsSensorManager(TamsSystemConnectionMemo memo) {
        this.memo = memo;
        tc = memo.getTrafficController();
        //Connect to the TrafficManager
        tc.addTamsListener(this);
        tm = xSR();//auto reset after reading S88
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        //log.info("Sending TamsMessage = " + tm.toString() + " , isBinary = " + tm.isBinary() + " and replyType = " + tm.getReplyType());
        //Add polling for sensor state changes
        tm = TamsMessage.getXEvtSen(); //reports only sensors with changed states
        //startPolling();
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        tc.addPollMessage(tm, this);
        //log.info("TamsMessage added to pollqueue = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
    }

    TamsSystemConnectionMemo memo;
    TamsTrafficController tc;
    //The hash table simply holds the object number against the TamsSensor ref.
    private Hashtable<Integer, Hashtable<Integer, TamsSensor>> _ttams = new Hashtable<Integer, Hashtable<Integer, TamsSensor>>(); // stores known Tams Obj

    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    public Sensor createNewSensor(String systemName, String userName) {
        TamsSensor s = new TamsSensor(systemName, userName);
        //log.info("Creating new TamsSensor: " + systemName);
        if (systemName.contains(":")) {
            int board = 0;
            int channel = 0;

            String curAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length());
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                //log.info("Creating new TamsSensor with board: " + board);
                if (!_ttams.containsKey(board)) {
                    _ttams.put(board, new Hashtable<Integer, TamsSensor>());
                    //log.info("_ttams: " + _ttams.toString());
                    /*if (_ttams.size() == 1) {
                        synchronized (pollHandler) {
                            pollHandler.notify();
                        }
                    }*/
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
            Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
            try {
                channel = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
                if (!sensorList.containsKey(channel)) {
                    sensorList.put(channel, s);
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
            if ((board * 2) > maxSE) {//Check if newly defined board number is higher than what we know
                maxSE = board * 2;//adjust xSE and inform Tams MC
                //log.info("Changed xSE to " + maxSE);
                tm = new TamsMessage("xSE " + Integer.toString(maxSE));
                tm.setBinary(false);
                tm.setReplyType('S');
                tc.sendTamsMessage(tm, this);
                tmq.add(tm);
                //no need to add a message for this board as the polling process will capture all board anyway
            }
        }
        //log.info("Returning this sensor: " + s.toString());
        return s;
    }

    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (!curAddress.contains(":")) {
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Hardware Address passed should be past in the form 'Module:port'");
        }

        //Address format passed is in the form of board:channel or T:turnout address
        int seperator = curAddress.indexOf(":");
        try {
            board = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Module Address passed should be a number");
        }
        try {
            port = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Port Address passed should be a number");
        }

        if (port == 0 || port > 16) {
            log.error("Port number must be between 1 and 16");
            throw new JmriException("Port number must be between 1 and 16");
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

    int board = 0;
    int port = 0;

    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage("Error", "Unable to convert " +
                    curAddress +
                    " to a valid Hardware Address", "" + ex, "", true, false);
            return null;
        }

        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null) {
            port++;
            while (port < 17) {
                try {
                    tmpSName = createSystemName(board + ":" + port, prefix);
                } catch (Exception e) {
                    log.error("Error creating system name for " + board + ":" + port);
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
            return null;
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
     * Determine if it is possible to add a range of sensors in
     * numerical order eg 1 to 16, primarily used to enable/disable the add
     * range box in the add sensor panel
     */
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    // to listen for status changes from Tams system
    public void reply(TamsReply r) {
        //log.info("*** TamsReply ***");
        if(tmq.isEmpty()){
            tm = myDummy();
        } else
        {
            tm = tmq.poll();
        }
        //log.info("ReplyType = " + tm.getReplyType() + ", Binary? = " +  tm.isBinary()+ ", OneByteReply = " + tm.getReplyOneByte());
        if (tm.getReplyType() == 'S'){//Only handle Sensor events
            if (tm.isBinary() == true){//Typical polling message
                if ((r.getNumDataElements() > 1) && (r.getElement(0) > 0x00)){
                    //Here we break up a long sensor related TamsReply into individual S88 module status'
                    int numberOfReplies = r.getNumDataElements() / 3;
                    //log.info("Incoming Reply = ");
                    for (int i = 0; i < r.getNumDataElements(); i++){
                        //log.info("Byte " + i + " = " + jmri.util.StringUtil.appendTwoHexFromInt(r.getElement(i) & 0xFF, ""));
                    }
                    //log.info("length of reply = " + r.getNumDataElements() + " & number of replies = " + numberOfReplies);
                    for (int i = 0; i < numberOfReplies; i++) {
                        //create a new TamsReply and pass it to the decoder
                        TamsReply tr = new TamsReply();
                        tr.setBinary(r.isBinary());
                        tr.setElement(0, r.getElement(3 * i));
                        tr.setElement(1, r.getElement(3 * i + 1));
                        tr.setElement(2, r.getElement(3 * i + 2));
                        //log.info("Going to pass this to the decoder = " + tr.toString());
                        //The decodeSensorState will do the actual decoding of each individual S88 port
                        decodeSensorState(tr);
                    }
                }
            } else {//xSR is an ASCII message
                //Nothing to do really
                //log.info("Reply to ACSII command = " + r.toString());
            }
            tm = myDummy();
        }
    }

    Thread PollThread;
    boolean stopPolling = true;

    protected Runnable pollHandler;

    protected void startPolling() {
        stopPolling = false;
        //log.info("Completed build of active readers " + _ttams.size());
        if (_ttams.size() > 0) {
            if (pollHandler == null) {
                pollHandler = new PollHandler(this);
            }
            Thread pollThread = new Thread(pollHandler, "TAMS Sensor Poll handler");
            pollThread.setDaemon(true);
            pollThread.setPriority(Thread.MAX_PRIORITY - 1);
            pollThread.start();
            pollHandler.notify();
        } else {
            //log.info("No active boards found");
        }
    }

    class PollHandler implements Runnable {//Why do we need this?

        TamsSensorManager sm = null;

        PollHandler(TamsSensorManager tsm) {
            sm = tsm;
        }

        public void run() {
            while (true) {
                new jmri.util.WaitHandler(this);
                //Not sure if this is needed
                /*
                //All we do here is issue the XEvtSen message
                log.info("Adding XEvtSen to poll queue: " + TamsMessage.getXEvtSen());
                tm = TamsMessage.getXEvtSen();
                tc.sendTamsMessage(tm, null);
                tc.addPollMessage(tm, tl); */
            }
        }
    }

    public void handleTimeout(TamsMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("timeout received to our last message " + m.toString());
        }

        if (!stopPolling) {
            synchronized (pollHandler) {
                pollHandler.notify();
            }
            if (log.isDebugEnabled()) {
                log.debug("time out to sensor status request");
            }
        }
    }

    public void message(TamsMessage m) {
        // messages are ignored
    }

    private void decodeSensorState(TamsReply r) {
        //reply to XEvtSen consists of 3 bytes per S88 module
        //byte 1 = S88 board number 1 to 52 in binary format
        //byte 2 = bits 1 to 8
        //byte 3 = bits 9 to 16
        String sensorprefix = getSystemPrefix() + "S" + r.getElement(0) + ":";
        //log.info("Decoding sensor: " + sensorprefix);
        //log.info("Lower Byte: " + r.getElement(1));
        //log.info("Upper Byte: " + r.getElement(2));
        Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
        int i = (r.getElement(1) & 0xff) << 8;//first 8 ports in second element of the reply
        //log.info("i after loading first byte= " + Integer.toString(i,2));
        i = i + (r.getElement(2) & 0xff);//first 8 ports in third element of the reply
        //log.info("i after loading second byte= " + Integer.toString(i,2));
        int mask = 0b100000000000000;
        for (int port = 1; port <= 16; port++) {
            int result = i & mask;
            //log.info("mask= " + Integer.toString(mask,2));
            //log.info("result= " + Integer.toString(result,2));
            if (sensorList != null) {
                TamsSensor ms = sensorList.get(port);
                if (ms == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sensorprefix);
                    //Little work around to pad single digit address out.
                    padPortNumber(port, sb);
                    ms = (TamsSensor) provideSensor(sb.toString());
                }
                if (ms != null) {
                    if (result == 0) {
                        ms.setOwnState(Sensor.INACTIVE);
                        //log.info(sensorprefix + port + " INACTIVE");
                    } else {
                        //log.info(sensorprefix + port + " ACTIVE");
                        ms.setOwnState(Sensor.ACTIVE);
                    }
                }
                mask = mask / 2;
            }
        }
        if (!stopPolling) {
            synchronized (pollHandler) {
                pollHandler.notify();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TamsSensorManager.class);
}

/* @(#)TamsSensorManager.java */

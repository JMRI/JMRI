package jmri.jmrix.tams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedList;
import java.util.Queue;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a TAMS connection.
 * <p>
 * Based on Glen Oberhauser's original LnThrottle implementation and work by
 * Kevin Dickerson
 *
 * @author Jan Boen
 */
public class TamsThrottle extends AbstractThrottle implements TamsListener {

    //Create a local TamsMessage Queue which we will use in combination with TamsReplies
    private final Queue<TamsMessage> tmq = new LinkedList<>();

    //This dummy message is used in case we expect a reply from polling
    static private TamsMessage myDummy() {
        log.trace("*** myDummy ***");
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.POLLMSG & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XEVTLOK & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(false);
        m.setReplyType('L');
        return m;
    }

    public TamsThrottle(TamsSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        super.speedStepMode = jmri.SpeedStepMode.NMRA_DCC_128;
        tc = memo.getTrafficController();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;

        //get the status if known of the current loco
        TamsMessage tm = new TamsMessage("xL " + address.getNumber());
        tm.setTimeout(10000);
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        //tc.addPollMessage(m, this);

        tm = new TamsMessage("xF " + address.getNumber());
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        //tc.addPollMessage(tm, this);

        tm = new TamsMessage("xFX " + address.getNumber());
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        //tc.addPollMessage(tm, this);

        //Add binary polling message
        tm = TamsMessage.getXEvtLok();
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        tc.addPollMessage(tm, this);

    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4. To
     * send function group 1 we have to also send speed, direction etc.
     */
    @Override
    protected void sendFunctionGroup1() {

        StringBuilder sb = new StringBuilder();
        sb.append("xL ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append(",");
        sb.append((getFunction(0) ? "1" : "0"));
        sb.append(",");
        sb.append(",");
        sb.append((getFunction(1) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(2) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(3) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(4) ? "1" : "0"));
        TamsMessage tm = new TamsMessage(sb.toString());
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        StringBuilder sb = new StringBuilder();
        sb.append("xF ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append((getFunction(5) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(6) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(7) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(8) ? "1" : "0"));

        TamsMessage tm = new TamsMessage(sb.toString());
        tm.setBinary(false);
        tm.setReplyType('T');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
    }

    @Override
    protected void sendFunctionGroup3() {
        StringBuilder sb = new StringBuilder();
        sb.append("xFX ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append((getFunction(9) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(10) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(11) ? "1" : "0"));
        sb.append(",");
        sb.append((getFunction(12) ? "1" : "0"));

        TamsMessage tm = new TamsMessage(sb.toString());
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
    }

    /**
     * Set the speed and direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;

        int value = (int) ((127 - 1) * this.speedSetting);     // -1 for rescale to avoid estop
        if (value > 0) {
            value = value + 1;  // skip estop
        }
        if (value > 127) {
            value = 127;    // max possible speed
        }
        if (value < 0) {
            value = 1;        // emergency stop
        }
        StringBuilder sb = new StringBuilder();
        sb.append("xL ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append(value);
        sb.append(",");
        sb.append(",");
        sb.append((isForward ? "f" : "r"));
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(",");

        TamsMessage tm = new TamsMessage(sb.toString());
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);

        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        synchronized(this) {
            setSpeedSetting(speedSetting);  // send the command
        }
        firePropertyChange(ISFORWARD, old, isForward);
    }

    private final DccLocoAddress address;

    TamsTrafficController tc;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    public void throttleDispose() {
        active = false;
        TamsMessage tm = TamsMessage.getXEvtLok();
        tc.removePollMessage(tm, this);
        finishRecord();
    }

    @Override
    public void message(TamsMessage m) {
        // messages are ignored
    }

    /**
     * Convert a Tams speed integer to a float speed value.
     *
     * @param lSpeed Tams speed
     * @return speed as -1 or number between 0 and 1, inclusive
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) {
            return 0.f;
        } else if (lSpeed == 1) {
            return -1.f;   // estop
        } else if (super.speedStepMode == jmri.SpeedStepMode.NMRA_DCC_128) {
            return ((lSpeed - 1) / 126.f);
        } else {
            return (int) (lSpeed * 27.f + 0.5) + 1;
        }
    }

    @Override
    public void reply(TamsReply tr) {
        log.trace("*** Loco reply ***");
        TamsMessage tm = tmq.isEmpty() ? myDummy() : tmq.poll();
        if (tm.isBinary()) {//Binary reply
            //The binary logic as created by Jan
            //Complete Loco status is given by:
            //    element(0) = Speed: 0..127, 0 = Stop, 1 = not used, 2 = min. Speed, 127 = max. Speed
            //    element(1) = F1..F8 (bit #0..7)
            //    element(2) = low byte of Loco# (A7..A0)
            //    element(3) = high byte of Loco#, plus Dir and Light status as in:
            //        bit#   7     6     5     4     3     2     1     0
            //            +-----+-----+-----+-----+-----+-----+-----+-----+
            //            | Dir |  FL | A13 | A12 | A11 | A10 | A9  | A8  |
            //            +-----+-----+-----+-----+-----+-----+-----+-----+
            //        where:
            //            Dir Loco direction (1 = forward)
            //            FL  Light status
            //            A13..8  high bits of Loco#
            //    element(4) = 'real' Loco speed (in terms of the Loco type/configuration)
            //        (please check XLokSts in P50X_LT.TXT for doc on 'real' speed)
            //Decode address
            int msb = tr.getElement(3) & 0x3F;
            int lsb = tr.getElement(2) & 0xFF;
            int receivedAddress = msb * 256 + lsb;
            if (log.isTraceEnabled()) { // avoid overhead of StringUtil calls
                log.trace("reply for loco = {}", receivedAddress);
                log.trace("reply = {} {} {} {} {}", StringUtil.appendTwoHexFromInt(tr.getElement(4) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(3) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(2) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(1) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(0) & 0xFF, ""));
            }
            if (receivedAddress == address.getNumber()) {//If correct address then decode the content
                log.trace("Is my address");
                try {
                    StringBuilder sb = new StringBuilder();
                    Float newSpeed = floatSpeed(tr.getElement(0));
                    super.setSpeedSetting(newSpeed);
                    log.trace("f0 = {}", tr.getElement(3) & 0x40);
                    
                    appendFuncString(0,sb,((tr.getElement(3) & 0x40) == 64));
                    
                    if (((tr.getElement(3) & 0x80) == 0) && isForward) {
                        isForward = false;
                        firePropertyChange(ISFORWARD, true, isForward);
                    }
                    if (((tr.getElement(3) & 0x80) == 128) && !isForward) {
                        isForward = true;
                        firePropertyChange(ISFORWARD, false, isForward);
                    }
                    
                    appendFuncString(1,sb,((tr.getElement(1) & 0x01) == 0x01));
                    appendFuncString(2,sb,((tr.getElement(1) & 0x02) == 0x02));
                    appendFuncString(3,sb,((tr.getElement(1) & 0x04) == 0x04));
                    appendFuncString(4,sb,((tr.getElement(1) & 0x08) == 0x08));
                    appendFuncString(5,sb,((tr.getElement(1) & 0x10) == 0x10));
                    appendFuncString(6,sb,((tr.getElement(1) & 0x20) == 0x20));
                    appendFuncString(7,sb,((tr.getElement(1) & 0x40) == 0x40));
                    appendFuncString(8,sb,((tr.getElement(1) & 0x80) == 0x80));
                    
                    log.trace(sb.toString());
                } catch (RuntimeException ex) {
                    log.error("Error handling reply from MC", ex);
                }
            }

        } else {//ASCII reply
            //The original logic as provided by Kevin
            if (tr.match("WARNING") >= 0) {
                return;
            }
            if (tr.match("L " + address.getNumber()) >= 0) {
                try {
                    log.trace("ASCII address = {}", address.getNumber());
                    String[] lines = tr.toString().split(" ");
                    Float newSpeed = floatSpeed(Integer.parseInt(lines[2]));
                    super.setSpeedSetting(newSpeed);
                    updateFunction(0,lines[3].equals("1"));
                    
                    if (lines[4].equals("r") && isForward) {
                        isForward = false;
                        firePropertyChange(ISFORWARD, true, isForward);
                    } else if (lines[4].equals("f") && !isForward) {
                        isForward = true;
                        firePropertyChange(ISFORWARD, false, isForward);
                    }
                    
                    updateFunction(1,lines[5].equals("1"));
                    updateFunction(2,lines[6].equals("1"));
                    updateFunction(3,lines[7].equals("1"));
                    updateFunction(4,lines[8].equals("1"));
                } catch (NumberFormatException ex) {
                    log.error("Error phrasing reply from MC", ex);
                }
            } else if (tr.match("FX " + address.getNumber()) >= 0) {
                String[] lines = tr.toString().split(" ");
                try {
                    updateFunction(9,lines[2].equals("1"));
                    updateFunction(10,lines[3].equals("1"));
                    updateFunction(11,lines[4].equals("1"));
                    updateFunction(12,lines[5].equals("1"));
                    updateFunction(13,lines[6].equals("1"));
                    updateFunction(14,lines[7].equals("1"));
                } catch (RuntimeException ex) {
                    log.error("Error phrasing reply from MC", ex);
                }
            } else if (tr.match("F " + address.getNumber()) >= 0) {
                String[] lines = tr.toString().split(" ");
                try {
                    updateFunction(1,lines[2].equals("1"));
                    updateFunction(2,lines[3].equals("1"));
                    updateFunction(3,lines[4].equals("1"));
                    updateFunction(4,lines[5].equals("1"));
                    updateFunction(5,lines[6].equals("1"));
                    updateFunction(6,lines[7].equals("1"));
                    updateFunction(7,lines[8].equals("1"));
                    updateFunction(8,lines[9].equals("1"));
                } catch (RuntimeException ex) {
                    log.error("Error phrasing reply from MC", ex);
                }
            } else if (tr.toString().equals("ERROR: no data.")) {
                log.debug("Loco has no data");
            }
        }
    }
    
    private void appendFuncString(int Fn, StringBuilder sb, boolean value){
        updateFunction(Fn,value);
        if (getFunction(Fn)){
            sb.append("f");
            sb.append(String.valueOf(Fn));
        } else {
            sb.append(String.valueOf(Fn));
            sb.append("f");
        }
        if (Fn<8){
            sb.append(" ");
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(TamsThrottle.class);

}

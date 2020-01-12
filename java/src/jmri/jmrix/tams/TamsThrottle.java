package jmri.jmrix.tams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedList;
import java.util.Queue;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
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
 *
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
        this.speedSetting = 0;
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
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
        sb.append((f0 ? "1" : "0"));
        sb.append(",");
        sb.append(",");
        sb.append((f1 ? "1" : "0"));
        sb.append(",");
        sb.append((f2 ? "1" : "0"));
        sb.append(",");
        sb.append((f3 ? "1" : "0"));
        sb.append(",");
        sb.append((f4 ? "1" : "0"));
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
        sb.append((f5 ? "1" : "0"));
        sb.append(",");
        sb.append((f6 ? "1" : "0"));
        sb.append(",");
        sb.append((f7 ? "1" : "0"));
        sb.append(",");
        sb.append((f8 ? "1" : "0"));

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
        sb.append((f9 ? "1" : "0"));
        sb.append(",");
        sb.append((f10 ? "1" : "0"));
        sb.append(",");
        sb.append((f11 ? "1" : "0"));
        sb.append(",");
        sb.append((f12 ? "1" : "0"));

        TamsMessage tm = new TamsMessage(sb.toString());
        tm.setBinary(false);
        tm.setReplyType('L');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
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

        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, isForward);
        }
    }

    private final DccLocoAddress address;

    TamsTrafficController tc;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    protected void throttleDispose() {
        active = false;
        TamsMessage tm = TamsMessage.getXEvtLok();
        tc.removePollMessage(tm, this);
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
                    if ((((tr.getElement(3) & 0x40) == 64)) && !this.f0) {
                        notifyPropertyChangeListener(Throttle.F0, this.f0, true);
                        sb.append("f0 ");
                        this.f0 = true;
                    }
                    if (((tr.getElement(3) & 0x40) == 0) && this.f0) {
                        notifyPropertyChangeListener(Throttle.F0, this.f0, false);
                        sb.append("0f ");
                        this.f0 = false;
                    }
                    if (((tr.getElement(3) & 0x80) == 0) && isForward) {
                        notifyPropertyChangeListener(ISFORWARD, isForward, false);
                        isForward = false;
                    }
                    if (((tr.getElement(3) & 0x80) == 128) && !isForward) {
                        notifyPropertyChangeListener(ISFORWARD, isForward, true);
                        isForward = true;
                    }
                    if (((tr.getElement(1) & 0x01) == 1) && !this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, true);
                        sb.append("f1 ");
                        this.f1 = true;
                    }
                    if (((tr.getElement(1) & 0x01) == 0) && this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, false);
                        sb.append("1f ");
                        this.f1 = false;
                    }
                    if (((tr.getElement(1) & 0x02) == 2) && !this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, true);
                        sb.append("f2 ");
                        this.f2 = true;
                    }
                    if (((tr.getElement(1) & 0x02) == 0) && this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, false);
                        sb.append("2f ");
                        this.f2 = false;
                    }
                    if (((tr.getElement(1) & 0x04) == 4) && !this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, true);
                        sb.append("f3 ");
                        this.f3 = true;
                    }
                    if (((tr.getElement(1) & 0x04) == 0) && this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, false);
                        sb.append("3f ");
                        this.f3 = false;
                    }
                    if (((tr.getElement(1) & 0x08) == 8) && !this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, true);
                        sb.append("f4 ");
                        this.f4 = true;
                    }
                    if (((tr.getElement(1) & 0x08) == 0) && this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, false);
                        sb.append("4f ");
                        this.f4 = false;
                    }
                    if (((tr.getElement(1) & 0x10) == 16) && !this.f5) {
                        notifyPropertyChangeListener(Throttle.F5, this.f5, true);
                        sb.append("f5 ");
                        this.f5 = true;
                    }
                    if (((tr.getElement(1) & 0x10) == 0) && this.f5) {
                        notifyPropertyChangeListener(Throttle.F5, this.f5, false);
                        sb.append("5f ");
                        this.f5 = false;
                    }
                    if (((tr.getElement(1) & 0x20) == 32) && !this.f6) {
                        notifyPropertyChangeListener(Throttle.F6, this.f6, true);
                        sb.append("f6 ");
                        this.f6 = true;
                    }
                    if (((tr.getElement(1) & 0x20) == 0) && this.f6) {
                        notifyPropertyChangeListener(Throttle.F6, this.f6, false);
                        sb.append("6f ");
                        this.f6 = false;
                    }
                    if (((tr.getElement(1) & 0x40) == 64) && !this.f7) {
                        notifyPropertyChangeListener(Throttle.F7, this.f7, true);
                        sb.append("f7 ");
                        this.f7 = true;
                    }
                    if (((tr.getElement(1) & 0x40) == 0) && this.f7) {
                        notifyPropertyChangeListener(Throttle.F7, this.f7, false);
                        sb.append("7f ");
                        this.f7 = false;
                    }
                    if (((tr.getElement(1) & 0x80) == 128) && !this.f8) {
                        notifyPropertyChangeListener(Throttle.F8, this.f8, true);
                        sb.append("f8");
                        this.f8 = true;
                    }
                    if (((tr.getElement(1) & 0x80) == 0) && this.f8) {
                        notifyPropertyChangeListener(Throttle.F8, this.f8, false);
                        sb.append("8f");
                        this.f8 = false;
                    }
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
                    if (lines[3].equals("1") && !this.f0) {
                        notifyPropertyChangeListener(Throttle.F0, this.f0, true);
                        this.f0 = true;
                    } else if (lines[3].equals("0") && this.f0) {
                        notifyPropertyChangeListener(Throttle.F0, this.f0, false);
                        this.f0 = false;
                    }
                    if (lines[4].equals("r") && isForward) {
                        notifyPropertyChangeListener(ISFORWARD, isForward, false);
                        isForward = false;
                    } else if (lines[4].equals("f") && !isForward) {
                        notifyPropertyChangeListener(ISFORWARD, isForward, true);
                        isForward = true;
                    }
                    if (lines[5].equals("1") && !this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, true);
                        this.f1 = true;
                    } else if (lines[5].equals("0") && this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, false);
                        this.f1 = false;
                    }
                    if (lines[6].equals("1") && !this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, true);
                        this.f2 = true;
                    } else if (lines[6].equals("0") && this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, false);
                        this.f2 = false;
                    }
                    if (lines[7].equals("1") && !this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, true);
                        this.f3 = true;
                    } else if (lines[7].equals("0") && this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, false);
                        this.f3 = false;
                    }
                    if (lines[8].equals("1") && !this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, true);
                        this.f4 = true;
                    } else if (lines[8].equals("0") && this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, false);
                        this.f4 = false;
                    }
                } catch (NumberFormatException ex) {
                    log.error("Error phrasing reply from MC", ex);
                }
            } else if (tr.match("FX " + address.getNumber()) >= 0) {
                String[] lines = tr.toString().split(" ");
                try {
                    if (lines[2].equals("1") && !this.f9) {
                        notifyPropertyChangeListener(Throttle.F9, this.f9, true);
                        this.f9 = true;
                    } else if (lines[2].equals("0") && this.f9) {
                        notifyPropertyChangeListener(Throttle.F9, this.f9, false);
                        this.f9 = false;
                    }
                    if (lines[3].equals("1") && !this.f10) {
                        notifyPropertyChangeListener(Throttle.F10, this.f10, true);
                        this.f10 = true;
                    } else if (lines[3].equals("0") && this.f10) {
                        notifyPropertyChangeListener(Throttle.F10, this.f10, false);
                        this.f10 = false;
                    }
                    if (lines[4].equals("1") && !this.f11) {
                        notifyPropertyChangeListener(Throttle.F11, this.f11, true);
                        this.f11 = true;
                    } else if (lines[4].equals("0") && this.f11) {
                        notifyPropertyChangeListener(Throttle.F11, this.f11, false);
                        this.f11 = false;
                    }
                    if (lines[5].equals("1") && !this.f12) {
                        notifyPropertyChangeListener(Throttle.F12, this.f12, true);
                        this.f12 = true;
                    } else if (lines[5].equals("0") && this.f12) {
                        notifyPropertyChangeListener(Throttle.F12, this.f12, false);
                        this.f12 = false;
                    }
                    if (lines[6].equals("1") && !this.f13) {
                        notifyPropertyChangeListener(Throttle.F13, this.f13, true);
                        this.f13 = true;
                    } else if (lines[6].equals("0") && this.f13) {
                        notifyPropertyChangeListener(Throttle.F13, this.f13, false);
                        this.f13 = false;
                    }
                    if (lines[7].equals("1") && !this.f14) {
                        notifyPropertyChangeListener(Throttle.F14, this.f14, true);
                        this.f14 = true;
                    } else if (lines[7].equals("0") && this.f14) {
                        notifyPropertyChangeListener(Throttle.F14, this.f14, false);
                        this.f14 = false;
                    }
                } catch (RuntimeException ex) {
                    log.error("Error phrasing reply from MC", ex);
                }
            } else if (tr.match("F " + address.getNumber()) >= 0) {
                String[] lines = tr.toString().split(" ");
                try {
                    if (lines[2].equals("1") && !this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, true);
                        this.f1 = true;
                    } else if (lines[2].equals("0") && this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, false);
                        this.f1 = false;
                    }
                    if (lines[3].equals("1") && !this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, true);
                        this.f2 = true;
                    } else if (lines[3].equals("0") && this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, false);
                        this.f2 = false;
                    }
                    if (lines[4].equals("1") && !this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, true);
                        this.f3 = true;
                    } else if (lines[4].equals("0") && this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, false);
                        this.f3 = false;
                    }
                    if (lines[5].equals("1") && !this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, true);
                        this.f4 = true;
                    } else if (lines[5].equals("0") && this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, false);
                        this.f4 = false;
                    }

                    if (lines[6].equals("1") && !this.f5) {
                        notifyPropertyChangeListener(Throttle.F5, this.f5, true);
                        this.f5 = true;
                    } else if (lines[6].equals("0") && this.f5) {
                        notifyPropertyChangeListener(Throttle.F5, this.f5, false);
                        this.f5 = false;
                    }
                    if (lines[7].equals("1") && !this.f6) {
                        notifyPropertyChangeListener(Throttle.F6, this.f6, true);
                        this.f6 = true;
                    } else if (lines[7].equals("0") && this.f6) {
                        notifyPropertyChangeListener(Throttle.F6, this.f6, false);
                        this.f6 = false;
                    }
                    if (lines[8].equals("1") && !this.f7) {
                        notifyPropertyChangeListener(Throttle.F7, this.f7, true);
                        this.f7 = true;
                    } else if (lines[8].equals("0") && this.f7) {
                        notifyPropertyChangeListener(Throttle.F7, this.f7, false);
                        this.f7 = false;
                    }
                    if (lines[9].equals("1") && !this.f8) {
                        notifyPropertyChangeListener(Throttle.F8, this.f8, true);
                        this.f8 = true;
                    } else if (lines[9].equals("0") && this.f8) {
                        notifyPropertyChangeListener(Throttle.F8, this.f8, false);
                        this.f8 = false;
                    }
                } catch (RuntimeException ex) {
                    log.error("Error phrasing reply from MC", ex);
                }
            } else if (tr.toString().equals("ERROR: no data.")) {
                log.debug("Loco has no data");
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(TamsThrottle.class);

}

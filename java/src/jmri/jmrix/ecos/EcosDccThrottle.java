package jmri.jmrix.ecos;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.HeadlessException;
import javax.swing.JOptionPane;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an ECoS connection.
 *
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author Bob Jacobsen Copyright (C) 2001, modified 2009 by Kevin Dickerson
 */
public class EcosDccThrottle extends AbstractThrottle implements EcosListener {

    /**
     * Constructor.
     */
    String objectNumber;
    int ecosretry = 0;
    private EcosLocoAddress objEcosLoco;
    private EcosLocoAddressManager objEcosLocoManager;
    final EcosPreferences p;
    //This boolean is used to prevent un-necessary commands from being sent to the ECOS if we have already lost
    //control of the object
    private boolean _haveControl = false;
    private boolean _hadControl = false;
    private boolean _control = true;

    public EcosDccThrottle(DccLocoAddress address, EcosSystemConnectionMemo memo, boolean control) {
        super(memo);
        super.speedStepMode = SpeedStepMode.NMRA_DCC_128;
        p = memo.getPreferenceManager();
        tc = memo.getTrafficController();
        objEcosLocoManager = memo.getLocoAddressManager();
        //The script will go through and read the values from the Ecos

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

        // extended values
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
        this.f13 = false;
        this.f14 = false;
        this.f15 = false;
        this.f16 = false;
        this.f17 = false;
        this.f18 = false;
        this.f19 = false;
        this.f20 = false;
        this.f21 = false;
        this.f22 = false;
        this.f23 = false;
        this.f24 = false;
        this.f25 = false;
        this.f26 = false;
        this.f27 = false;
        this.f28 = false;

        this.address = address;
        this.isForward = true;
        this._control = control;

        ecosretry = 0;

        log.debug("EcosDccThrottle constructor " + address);

        //We go on a hunt to find an object with the dccaddress sent by our controller.
        if (address.getNumber() < EcosLocoAddress.MFX_DCCAddressOffset) {
            objEcosLoco = objEcosLocoManager.provideByDccAddress(address.getNumber());
        } else {
            int ecosID = address.getNumber()-EcosLocoAddress.MFX_DCCAddressOffset;
            objEcosLoco = objEcosLocoManager.provideByEcosObject(String.valueOf(ecosID));
        }

        this.objectNumber = objEcosLoco.getEcosObject();
        if (this.objectNumber == null) {
            createEcosLoco();
        } else {
            getControl();
        }
    }

    private void getControl() {
        String message;
        setSpeedStepMode(objEcosLoco.getSpeedStepMode());
        message = "get(" + this.objectNumber + ", speed)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        message = "get(" + this.objectNumber + ", dir)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

        if (_control) {
            if (p.getLocoControl()) {
                message = "request(" + this.objectNumber + ", view, control, force)";
            } else {
                message = "request(" + this.objectNumber + ", view, control)";
            }
        } else {
            message = "request(" + this.objectNumber + ", view)";
        }

        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    //The values here might need a bit of re-working

    /**
     * Convert an Ecos speed integer to a float speed value.
     * @param lSpeed speed value as an integer
     * @return speed value as a float
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) {
            return 0.0f;
        }
        if (getSpeedStepMode() == SpeedStepMode.NMRA_DCC_28) {
            int step = (int) Math.ceil(lSpeed / 4.65);
            return step * getSpeedIncrement();
        }
        return ((lSpeed) / 126.f);
    }

    @Override
    public void setF0(boolean f0) {
        boolean old = this.f0;
        this.f0 = f0;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[0, " + (getF0() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f0) {
            notifyPropertyChangeListener(Throttle.F0, old, this.f0);
        }
    }

    @Override
    public void setF1(boolean f1) {
        boolean old = this.f1;
        this.f1 = f1;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[1, " + (getF1() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f1) {
            notifyPropertyChangeListener(Throttle.F1, old, this.f1);
        }
    }

    @Override
    public void setF2(boolean f2) {
        boolean old = this.f2;
        this.f2 = f2;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[2, " + (getF2() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f2) {
            notifyPropertyChangeListener(Throttle.F2, old, this.f2);
        }
    }

    @Override
    public void setF3(boolean f3) {
        boolean old = this.f3;
        this.f3 = f3;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[3, " + (getF3() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f3) {
            notifyPropertyChangeListener(Throttle.F3, old, this.f3);
        }
    }

    @Override
    public void setF4(boolean f4) {
        boolean old = this.f4;
        this.f4 = f4;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[4, " + (getF4() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f4) {
            notifyPropertyChangeListener(Throttle.F4, old, this.f4);
        }
    }

    @Override
    public void setF5(boolean f5) {
        boolean old = this.f5;
        this.f5 = f5;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[5, " + (getF5() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f5) {
            notifyPropertyChangeListener(Throttle.F5, old, this.f5);
        }
    }

    @Override
    public void setF6(boolean f6) {
        boolean old = this.f6;
        this.f6 = f6;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[6, " + (getF6() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f6) {
            notifyPropertyChangeListener(Throttle.F6, old, this.f6);
        }
    }

    @Override
    public void setF7(boolean f7) {
        boolean old = this.f7;
        this.f7 = f7;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[7, " + (getF7() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f7) {
            notifyPropertyChangeListener(Throttle.F7, old, this.f7);
        }
    }

    @Override
    public void setF8(boolean f8) {
        boolean old = this.f8;
        this.f8 = f8;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[8, " + (getF8() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f8) {
            notifyPropertyChangeListener(Throttle.F8, old, this.f8);
        }
    }

    @Override
    public void setF9(boolean f9) {
        boolean old = this.f9;
        this.f9 = f9;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[9, " + (getF9() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f9) {
            notifyPropertyChangeListener(Throttle.F9, old, this.f9);
        }
    }

    @Override
    public void setF10(boolean f10) {
        boolean old = this.f10;
        this.f10 = f10;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[10, " + (getF10() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f10) {
            notifyPropertyChangeListener(Throttle.F10, old, this.f10);
        }
    }

    @Override
    public void setF11(boolean f11) {
        boolean old = this.f11;
        this.f11 = f11;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[11, " + (getF11() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f11) {
            notifyPropertyChangeListener(Throttle.F11, old, this.f11);
        }
    }

    @Override
    public void setF12(boolean f12) {
        boolean old = this.f12;
        this.f12 = f12;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[12, " + (getF12() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f12) {
            notifyPropertyChangeListener(Throttle.F12, old, this.f12);
        }
    }

    @Override
    public void setF13(boolean f13) {
        boolean old = this.f13;
        this.f13 = f13;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[13, " + (getF13() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f13) {
            notifyPropertyChangeListener(Throttle.F13, old, this.f13);
        }
    }

    @Override
    public void setF14(boolean f14) {
        boolean old = this.f14;
        this.f14 = f14;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[14, " + (getF14() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f14) {
            notifyPropertyChangeListener(Throttle.F14, old, this.f14);
        }
    }

    @Override
    public void setF15(boolean f15) {
        boolean old = this.f15;
        this.f15 = f15;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[15, " + (getF15() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f15) {
            notifyPropertyChangeListener(Throttle.F15, old, this.f15);
        }
    }

    @Override
    public void setF16(boolean f16) {
        boolean old = this.f16;
        this.f16 = f16;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[16, " + (getF16() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f16) {
            notifyPropertyChangeListener(Throttle.F16, old, this.f16);
        }
    }

    @Override
    public void setF17(boolean f17) {
        boolean old = this.f17;
        this.f17 = f17;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[17, " + (getF17() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f17) {
            notifyPropertyChangeListener(Throttle.F17, old, this.f17);
        }
    }

    @Override
    public void setF18(boolean f18) {
        boolean old = this.f18;
        this.f18 = f18;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[18, " + (getF18() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f18) {
            notifyPropertyChangeListener(Throttle.F18, old, this.f18);
        }
    }

    @Override
    public void setF19(boolean f19) {
        boolean old = this.f19;
        this.f19 = f19;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[19, " + (getF19() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f19) {
            notifyPropertyChangeListener(Throttle.F19, old, this.f19);
        }
    }

    @Override
    public void setF20(boolean f20) {
        boolean old = this.f20;
        this.f20 = f20;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[20, " + (getF20() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f20) {
            notifyPropertyChangeListener(Throttle.F20, old, this.f20);
        }
    }

    @Override
    public void setF21(boolean f21) {
        boolean old = this.f21;
        this.f21 = f21;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[21, " + (getF21() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f21) {
            notifyPropertyChangeListener(Throttle.F21, old, this.f21);
        }
    }

    @Override
    public void setF22(boolean f22) {
        boolean old = this.f22;
        this.f22 = f22;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[22, " + (getF22() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f22) {
            notifyPropertyChangeListener(Throttle.F22, old, this.f22);
        }
    }

    @Override
    public void setF23(boolean f23) {
        boolean old = this.f23;
        this.f23 = f23;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[23, " + (getF23() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f23) {
            notifyPropertyChangeListener(Throttle.F23, old, this.f23);
        }
    }

    @Override
    public void setF24(boolean f24) {
        boolean old = this.f24;
        this.f24 = f24;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[24, " + (getF24() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f24) {
            notifyPropertyChangeListener(Throttle.F24, old, this.f24);
        }
    }

    @Override
    public void setF25(boolean f25) {
        boolean old = this.f25;
        this.f25 = f25;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[25, " + (getF25() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f25) {
            notifyPropertyChangeListener(Throttle.F25, old, this.f25);
        }
    }

    @Override
    public void setF26(boolean f26) {
        boolean old = this.f26;
        this.f26 = f26;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[26, " + (getF26() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f26) {
            notifyPropertyChangeListener(Throttle.F26, old, this.f26);
        }
    }

    @Override
    public void setF27(boolean f27) {
        boolean old = this.f27;
        this.f27 = f27;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[27, " + (getF27() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f27) {
            notifyPropertyChangeListener(Throttle.F27, old, this.f27);
        }
    }

    @Override
    public void setF28(boolean f28) {
        boolean old = this.f28;
        this.f28 = f28;
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[28, " + (getF28() ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
        if (old != this.f28) {
            notifyPropertyChangeListener(Throttle.F28, old, this.f28);
        }
    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    //The values here might need a bit of re-working
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point
    @Override
    public void setSpeedSetting(float speed) {
        if (!_haveControl) {
            return;
        }
        if (speed == this.speedSetting && speedMessageSent <= 0) {
            return;
        }
        int value = (int) ((127 - 1) * speed);     // -1 for rescale to avoid estop
        if (value > 128) {
            value = 126;    // max possible speed
        }
        if ((value > 0) || (value == 0.0)) {
            String message = "set(" + this.objectNumber + ", speed[" + value + "])";
            EcosMessage m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
            if (speedMessageSent != 0) {
                if (System.currentTimeMillis() - lastSpeedMessageTime > 500 || speedMessageSent < 0) {
                    speedMessageSent = 0;
                }
            }
            lastSpeedMessageTime = System.currentTimeMillis();
            speedMessageSent++;
        } else {
            //Not sure if this performs an emergency stop or a normal one.
            String message = "set(" + this.objectNumber + ", stop)";
            this.speedSetting = 0.0f;
            EcosMessage m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);

        }
        //record(speed);
    }

    long lastSpeedMessageTime = 0l;

    EcosTrafficController tc;

    int speedMessageSent = 0;

    @Override
    public void setIsForward(boolean forward) {
        if (!_haveControl) {
            return;
        }

        String message;
        if (this.speedSetting > 0.0f) {
            // Need to send current speed as well as direction, otherwise
            // speed will be set to zero on direction change
            int speedValue = (int) ((127 - 1) * this.speedSetting);     // -1 for rescale to avoid estop
            if (speedValue > 128) {
                speedValue = 126;    // max possible speed
            }
            message = "set(" + this.objectNumber + ", dir[" + (forward ? 0 : 1) + "], speed[" + speedValue + "])";
        } else {
            message = "set(" + this.objectNumber + ", dir[" + (forward ? 0 : 1) + "])";
        }
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    private DccLocoAddress address;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    protected void throttleDispose() {
        String message = "release(" + this.objectNumber + ", control)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        _haveControl = false;
        _hadControl = false;
        finishRecord();
    }

    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point
    @Override
    public void reply(EcosReply m) {
        int resultCode = m.getResultCode();
        if (resultCode == 0) {
            String replyType = m.getReplyType();
            if (replyType.equals("create")) {
                String[] msgDetails = m.getContents();
                for (String line : msgDetails) {
                    if (line.startsWith("10 id[")) {
                        String EcosAddr = EcosReply.getContentDetail(line);
                        objEcosLoco.setEcosObject(EcosAddr);
                        objEcosLocoManager.deregister(objEcosLoco);
                        objEcosLocoManager.register(objEcosLoco);
                        objEcosLoco.setEcosTempEntry(true);
                        objEcosLoco.doNotAddToRoster();
                        this.objectNumber = EcosAddr;
                        getControl();
                    }
                }
                return;
            }

            /*if (lines[lines.length-1].contains("<END 0 (NERROR_OK)>")){
             //Need to investigate this a bit futher to see what the significance of the message is
             //we may not have to worry much about it.
             log.info("Loco has been created on the ECoS Sucessfully.");
             return;
             }*/
            if (m.getEcosObjectId() != objEcosLoco.getEcosObjectAsInt()) {
                log.debug("message is not for us");
                return;
            }
            if (replyType.equals("set")) {
                //This might need to use speedstep, rather than speed
                //This is for standard response to set and request.
                String[] msgDetails = m.getContents();
                for (String line : msgDetails) {
                    if (line.contains("speed") && !line.contains("speedstep")) {
                        speedMessageSent--;
                        if (speedMessageSent <= 0) {
                            Float newSpeed = floatSpeed(Integer.parseInt(EcosReply.getContentDetails(line, "speed")));
                            super.setSpeedSetting(newSpeed);
                        }
                    }
                    if (line.contains("dir")) {
                        boolean newDirection = false;
                        if (EcosReply.getContentDetails(line, "dir").equals("0")) {
                            newDirection = true;
                        }
                        super.setIsForward(newDirection);
                    }
                }
                if (msgDetails.length == 0) {
                    //For some reason in recent ECOS software releases we do not get the contents, only a header and End State
                    if (m.toString().contains("speed") && !m.toString().contains("speedstep")) {
                        speedMessageSent--;
                        if (speedMessageSent <= 0) {
                            Float newSpeed = floatSpeed(Integer.parseInt(EcosReply.getContentDetails(m.toString(), "speed")));
                            super.setSpeedSetting(newSpeed);
                        }
                    }
                    if (m.toString().contains("dir")) {
                        boolean newDirection = false;
                        if (EcosReply.getContentDetails(m.toString(), "dir").equals("0")) {
                            newDirection = true;
                        }
                        super.setIsForward(newDirection);
                    }
                }
            } //Treat gets and events as the same.
            else if ((replyType.equals("get")) || (m.isUnsolicited())) {
                //log.debug("The last command was accepted by the ecos");
                String[] msgDetails = m.getContents();
                for (String line : msgDetails) {
                    if (speedMessageSent > 0 && m.isUnsolicited() && line.contains("speed")) {
                        //We want to ignore these messages.
                    } else if (speedMessageSent <= 0 && line.contains("speed") && !line.contains("speedstep")) {
                        Float newSpeed = floatSpeed(Integer.parseInt(EcosReply.getContentDetails(line, "speed")));
                        super.setSpeedSetting(newSpeed);
                    } else if (line.contains("dir")) {
                        boolean newDirection = false;
                        if (EcosReply.getContentDetails(line, "dir").equals("0")) {
                            newDirection = true;
                        }
                        super.setIsForward(newDirection);
                    } else if (line.contains("protocol")) {
                        String pro = EcosReply.getContentDetails(line, "protocol");
                        if (pro.equals("DCC128")) {
                            setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
                        } else if (pro.equals("DCC28")) {
                            setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
                        } else if (pro.equals("DCC14")) {
                            setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
                        }
                    } else if (line.contains("func[")) {
                        String funcStr = EcosReply.getContentDetails(line, "func");
                        int function = Integer.parseInt(funcStr.substring(0, funcStr.indexOf(",")).trim());
                        int functionValue = Integer.parseInt(funcStr.substring((funcStr.indexOf(",") + 1), funcStr.length()).trim());
                        boolean functionresult = false;
                        if (functionValue == 1) {
                            functionresult = true;
                        }
                        switch (function) {
                            case 0:
                                if (this.f0 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F0, this.f0, functionresult);
                                    this.f0 = functionresult;
                                }
                                break;
                            case 1:
                                if (this.f1 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F1, this.f1, functionresult);
                                    this.f1 = functionresult;
                                }
                                break;
                            case 2:
                                if (this.f2 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F2, this.f2, functionresult);
                                    this.f2 = functionresult;
                                }
                                break;
                            case 3:
                                if (this.f3 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F3, this.f3, functionresult);
                                    this.f3 = functionresult;
                                }
                                break;
                            case 4:
                                if (this.f4 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F4, this.f4, functionresult);
                                    this.f4 = functionresult;
                                }
                                break;
                            case 5:
                                if (this.f5 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F5, this.f5, functionresult);
                                    this.f5 = functionresult;
                                }
                                break;
                            case 6:
                                if (this.f6 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F6, this.f6, functionresult);
                                    this.f6 = functionresult;
                                }
                                break;
                            case 7:
                                if (this.f7 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F7, this.f7, functionresult);
                                    this.f7 = functionresult;
                                }
                                break;
                            case 8:
                                if (this.f8 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F8, this.f8, functionresult);
                                    this.f8 = functionresult;
                                }
                                break;
                            case 9:
                                if (this.f9 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F9, this.f9, functionresult);
                                    this.f9 = functionresult;
                                }
                                break;
                            case 10:
                                if (this.f10 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F10, this.f10, functionresult);
                                    this.f10 = functionresult;
                                }
                                break;
                            case 11:
                                if (this.f11 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F11, this.f11, functionresult);
                                    this.f11 = functionresult;
                                }
                                break;
                            case 12:
                                if (this.f12 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F12, this.f12, functionresult);
                                    this.f12 = functionresult;
                                }
                                break;
                            case 13:
                                if (this.f13 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F13, this.f13, functionresult);
                                    this.f13 = functionresult;
                                }
                                break;
                            case 14:
                                if (this.f14 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F14, this.f14, functionresult);
                                    this.f14 = functionresult;
                                }
                                break;
                            case 15:
                                if (this.f15 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F15, this.f15, functionresult);
                                    this.f15 = functionresult;
                                }
                                break;
                            case 16:
                                if (this.f16 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F16, this.f16, functionresult);
                                    this.f16 = functionresult;
                                }
                                break;
                            case 17:
                                if (this.f17 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F17, this.f17, functionresult);
                                    this.f17 = functionresult;
                                }
                                break;
                            case 18:
                                if (this.f18 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F18, this.f18, functionresult);
                                    this.f18 = functionresult;
                                }
                                break;
                            case 19:
                                if (this.f19 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F19, this.f19, functionresult);
                                    this.f19 = functionresult;
                                }
                                break;
                            case 20:
                                if (this.f20 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F20, this.f20, functionresult);
                                    this.f20 = functionresult;
                                }
                                break;
                            case 21:
                                if (this.f21 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F21, this.f21, functionresult);
                                    this.f21 = functionresult;
                                }
                                break;
                            case 22:
                                if (this.f22 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F22, this.f22, functionresult);
                                    this.f22 = functionresult;
                                }
                                break;
                            case 23:
                                if (this.f23 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F23, this.f23, functionresult);
                                    this.f23 = functionresult;
                                }
                                break;
                            case 24:
                                if (this.f24 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F24, this.f24, functionresult);
                                    this.f24 = functionresult;
                                }
                                break;
                            case 25:
                                if (this.f25 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F25, this.f25, functionresult);
                                    this.f25 = functionresult;
                                }
                                break;
                            case 26:
                                if (this.f26 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F26, this.f26, functionresult);
                                    this.f26 = functionresult;
                                }
                                break;
                            case 27:
                                if (this.f27 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F27, this.f27, functionresult);
                                    this.f27 = functionresult;
                                }
                                break;
                            case 28:
                                if (this.f28 != functionresult) {
                                    notifyPropertyChangeListener(Throttle.F28, this.f28, functionresult);
                                    this.f28 = functionresult;
                                }
                                break;
                            default:
                                break;
                        }

                    } else if (line.contains("msg")) {
                        //We get this lost control error because we have registered as a viewer.
                        if (line.contains("CONTROL_LOST")) {
                            retryControl();
                            log.debug("We have no control over the ecos object, but will retry.");
                        }

                    }

                }
            } else if (replyType.equals("release")) {
                log.debug("Released " + this.objectNumber + " from the Ecos");
                _haveControl = false;
            } else if (replyType.equals("request")) {
                log.debug("We have control over " + this.objectNumber + " from the Ecos");
                ecosretry = 0;
                if (_control) {
                    _haveControl = true;
                }
                if (!_hadControl) {
                    ((EcosDccThrottleManager) adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, true);
                    getInitialStates();
                }
            }
        } else if (resultCode == 35) {
            /**
             * This message occurs when have already created a loco, but have
             * not appended it to the database. The Ecos will not allow another
             * loco to be created until the previous entry has been appended.
             */

            //Potentially need to deal with this error better.
            log.info("Another loco create operation is already taking place unable to create another.");

        } else if (resultCode == 25) {
            /**
             * This section deals with no longer having control over the ecos
             * loco object. we try three times to request control, on the fourth
             * attempt we try a forced control, if that fails we inform the user
             * and reset the counter to zero.
             */
            retryControl();
        } else if (resultCode == 15) {
            log.info("Loco can not be accessed via the Ecos Object Id " + this.objectNumber);
            try {
                javax.swing.JOptionPane.showMessageDialog(null, Bundle.getMessage("UnknownLocoDialog", this.address),
                        Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }
            jmri.InstanceManager.throttleManagerInstance().releaseThrottle(this, null);
        } else {
            log.debug("Last Message resulted in an END code we do not understand " + resultCode);
        }
    }

    @Override
    public void message(EcosMessage m) {
        //System.out.println("Ecos message - "+ m);
        // messages are ignored
    }

    public void forceControl() {
        String message = "request(" + this.objectNumber + ", control, force)";
        EcosMessage ms = new EcosMessage(message);
        tc.sendEcosMessage(ms, this);
    }

    //Converts the int value of the protocol to the ESU protocol string
    private String protocol(LocoAddress.Protocol protocol) {
        switch (protocol) {
            case MOTOROLA:
                return "MM28";
            case SELECTRIX:
                return "SX28";
            case MFX:
                return "MMFKT";
            case LGB:
                return "LGB";
            default:
                return "DCC128";
        }
    }

    private void createEcosLoco() {
        objEcosLoco.setEcosDescription(Bundle.getMessage("CreatedByJMRI"));
        objEcosLoco.setProtocol(protocol(address.getProtocol()));
        String message = "create(10, addr[" + objEcosLoco.getNumber() + "], name[\"Created By JMRI\"], protocol[" + objEcosLoco.getECOSProtocol() + "], append)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    private void retryControl() {
        if (_haveControl) {
            _hadControl = true;
        }
        _haveControl = false;
        if (ecosretry < 3) {
            //It might be worth adding in a sleep/pause of discription between retries.
            ecosretry++;

            String message = "request(" + this.objectNumber + ", view, control)";
            EcosMessage ms = new EcosMessage(message);
            tc.sendEcosMessage(ms, this);
            log.error("We have no control over the ecos object " + this.objectNumber + " Retrying Attempt " + ecosretry);
        } else if (ecosretry == 3) {
            ecosretry++;
            int val = 0;
            if (p.getForceControlFromEcos() == 0x00) {
                try {
                    val = javax.swing.JOptionPane.showConfirmDialog(null, "UnableToGainDialog",
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
                } catch (HeadlessException he) {
                    val = 1;
                }
            } else {
                if (p.getForceControlFromEcos() == 0x01) {
                    val = 1;
                }
            }
            if (val == 0) {
                String message = "request(" + this.objectNumber + ", view, control, force)";
                EcosMessage ms = new EcosMessage(message);
                tc.sendEcosMessage(ms, this);
                log.error("We have no control over the ecos object " + this.objectNumber + "Trying a forced control");
            } else {
                if (_hadControl) {
                    notifyPropertyChangeListener("LostControl", 0, 0);
                    _hadControl = false;
                    ecosretry = 0;
                } else {
                    ((EcosDccThrottleManager) adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, false);
                }
            }
        } else {
            ecosretry = 0;
            if (_hadControl) {
                notifyPropertyChangeListener("LostControl", 0, 0);
            } else {
                ((EcosDccThrottleManager) adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, false);
            }
            ((EcosDccThrottleManager) adapterMemo.get(jmri.ThrottleManager.class)).releaseThrottle(this, null);
        }
    }

    void getInitialStates() {
        String message = "get(" + this.objectNumber + ", speed)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        message = "get(" + this.objectNumber + ", dir)";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
        for (int i = 0; i <= 28; i++) {
            message = "get(" + this.objectNumber + ", func[" + i + "])";
            m = new EcosMessage(message);
            tc.sendEcosMessage(m, this);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EcosDccThrottle.class);

}

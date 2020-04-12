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
        // Functions 0-28 default to false

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
    
    private void setF(int Fn, boolean val){
        updateFunction(Fn,val);
        if (_haveControl) {
            EcosMessage m = new EcosMessage("set(" + this.objectNumber + ", func[" + 
                String.valueOf(Fn) + ", " + (getFunction(Fn) ? 1 : 0) + "])");
            tc.sendEcosMessage(m, this);
        }
    }

    @Override
    public void setF0(boolean f0) {
        setF(0,f0);
    }

    @Override
    public void setF1(boolean f1) {
        setF(1,f1);
    }

    @Override
    public void setF2(boolean f2) {
        setF(2,f2);
    }

    @Override
    public void setF3(boolean f3) {
        setF(3,f3);
    }

    @Override
    public void setF4(boolean f4) {
        setF(4,f4);
    }

    @Override
    public void setF5(boolean f5) {
        setF(5,f5);
    }

    @Override
    public void setF6(boolean f6) {
        setF(6,f6);
    }

    @Override
    public void setF7(boolean f7) {
        setF(7,f7);
    }

    @Override
    public void setF8(boolean f8) {
        setF(8,f8);
    }

    @Override
    public void setF9(boolean f9) {
        setF(9,f9);
    }

    @Override
    public void setF10(boolean f10) {
        setF(10,f10);
    }

    @Override
    public void setF11(boolean f11) {
        setF(11,f11);
    }

    @Override
    public void setF12(boolean f12) {
        setF(12,f12);
    }

    @Override
    public void setF13(boolean f13) {
        setF(13,f13);
    }

    @Override
    public void setF14(boolean f14) {
        setF(14,f14);
    }

    @Override
    public void setF15(boolean f15) {
        setF(15,f15);
    }

    @Override
    public void setF16(boolean f16) {
        setF(16,f16);
    }

    @Override
    public void setF17(boolean f17) {
        setF(17,f17);
    }

    @Override
    public void setF18(boolean f18) {
        setF(18,f18);
    }

    @Override
    public void setF19(boolean f19) {
        setF(19,f19);
    }

    @Override
    public void setF20(boolean f20) {
        setF(20,f20);
    }

    @Override
    public void setF21(boolean f21) {
        setF(21,f21);
    }

    @Override
    public void setF22(boolean f22) {
        setF(22,f22);
    }

    @Override
    public void setF23(boolean f23) {
        setF(23,f23);
    }

    @Override
    public void setF24(boolean f24) {
        setF(24,f24);
    }

    @Override
    public void setF25(boolean f25) {
        setF(25,f25);
    }

    @Override
    public void setF26(boolean f26) {
        setF(26,f26);
    }

    @Override
    public void setF27(boolean f27) {
        setF(27,f27);
    }

    @Override
    public void setF28(boolean f28) {
        setF(28,f28);
    }

    /**
     * Set the speed and direction.
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

    long lastSpeedMessageTime = 0L;

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
                        updateFunction(function,functionValue == 1);
                        
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
                    firePropertyChange("LostControl", 0, 0);
                    _hadControl = false;
                    ecosretry = 0;
                } else {
                    ((EcosDccThrottleManager) adapterMemo.get(jmri.ThrottleManager.class)).throttleSetup(this, this.address, false);
                }
            }
        } else {
            ecosretry = 0;
            if (_hadControl) {
                firePropertyChange("LostControl", 0, 0);
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

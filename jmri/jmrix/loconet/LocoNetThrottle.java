package jmri.jmrix.loconet;

import jmri.DccThrottle;

/**
 * An implementation of DccThrottle with code specific to a LocoNet connection.
 */
public class LocoNetThrottle implements DccThrottle
{
    private float speedSetting;
    private float speedIncrement;
    private int address;
    private boolean isForward;
    private boolean f0, f1, f2, f3, f4, f5, f6, f7, f8;

    private LocoNetSlot slot;
    private LocoNetInterface network;

    /**
     * Constructor
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public LocoNetThrottle(LocoNetSlot slot)
    {
        this.slot = slot;
        network = LnTrafficController.instance();
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_MOVE_SLOTS);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, slot.getSlot());
        network.sendLocoNetMessage(msg);

        // cache settings
        this.speedSetting = slot.speed();
        this.f0           = slot.isF0();
        this.f1           = slot.isF1();
        this.f2           = slot.isF2();
        this.f3           = slot.isF3();
        this.f4           = slot.isF4();
        this.f5           = slot.isF5();
        this.f6           = slot.isF6();
        this.f7           = slot.isF7();
        this.f8           = slot.isF8();
        this.address      = slot.locoAddr();
        this.isForward    = slot.isForward();

        switch(slot.decoderType())
        {
            case LnConstants.DEC_MODE_128:
            case LnConstants.DEC_MODE_128A: this.speedIncrement = 1; break;
            case LnConstants.DEC_MODE_28:
            case LnConstants.DEC_MODE_28A:
            case LnConstants.DEC_MODE_28TRI: this.speedIncrement = 4; break;
            case LnConstants.DEC_MODE_14: this.speedIncrement = 8; break;
        }
    }


    /**
     * Send the LocoNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    private void sendLowerFunctions()
    {
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_DIRF);
        msg.setElement(1, slot.getSlot());
        int bytes = (getIsForward() ? 0 : LnConstants.DIRF_DIR) |
                    (getF0() ? LnConstants.DIRF_F0 : 0) |
                    (getF1() ? LnConstants.DIRF_F1 : 0) |
                    (getF2() ? LnConstants.DIRF_F2 : 0) |
                    (getF3() ? LnConstants.DIRF_F3 : 0) |
                    (getF4() ? LnConstants.DIRF_F4 : 0);
        msg.setElement(2, bytes);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Send the LocoNet message to set the state of
     * functions F5, F6, F7, F8
     */
    private void sendHigherFunctions()
    {
       LocoNetMessage msg = new LocoNetMessage(4);
       msg.setOpCode(LnConstants.OPC_LOCO_SND);
       msg.setElement(1, slot.getSlot());
       int bytes =  (getF8() ? LnConstants.SND_F8 : 0) |
                    (getF7() ? LnConstants.SND_F7 : 0) |
                    (getF6() ? LnConstants.SND_F6 : 0) |
                    (getF5() ? LnConstants.SND_F5 : 0);
        msg.setElement(2, bytes);
       network.sendLocoNetMessage(msg);

    }

   /** speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting()
    {
        return speedSetting;
    }

    public void setSpeedSetting(float speed)
    {
        this.speedSetting = speed;
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_SPD);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, (int)(127*speed));
        network.sendLocoNetMessage(msg);
    }

    /** direction
     * This is an bound parameter.
     */
    public boolean getIsForward()
    {
        return isForward;
    }

    public void setIsForward(boolean forward)
    {
        isForward = forward;
        sendLowerFunctions();
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0()
    {
        return f0;
    }

    public void setF0(boolean f0)
    {
        this.f0 = f0;
        sendLowerFunctions();
    }

    public boolean getF1()
    {
        return f1;
    }

    public void setF1(boolean f1)
    {
        this.f1 = f1;
        sendLowerFunctions();

    }

    public boolean getF2()
    {
        return f2;
    }

    public void setF2(boolean f2)
    {
        this.f2 = f2;
        sendLowerFunctions();
    }

    public boolean getF3()
    {
        return f3;
    }

    public void setF3(boolean f3)
    {
        this.f3 = f3;
        sendLowerFunctions();
    }

    public boolean getF4()
    {
        return f4;
    }

    public void setF4(boolean f4)
    {
        this.f4 = f4;
        sendLowerFunctions();
    }


    public boolean getF5()
    {
        return f5;
    }

    public void setF5(boolean f5)
    {
        this.f5 = f5;
        sendHigherFunctions();
    }

    public boolean getF6()
    {
        return f6;
    }

    public void setF6(boolean f6)
    {
        this.f6 = f6;
        sendHigherFunctions();
    }


    public boolean getF7()
    {
        return f7;
    }

    public void setF7(boolean f7)
    {
        this.f7 = f7;
        sendHigherFunctions();

    }


    public boolean getF8()
    {
        return f8;
    }

    public void setF8(boolean f8)
    {
        this.f8 = f8;
        sendHigherFunctions();
    }


    /**
     * Locomotive identification.  The exact format is defined by the
     * specific implementation, but its intended that this is a user-specified
     * name like "UP 777", or whatever convention the user wants to employ.
     *
     * This is an unbound parameter.
     */
    public String getLocoIdentification()
    {
        return "";
    }


    /**
     * Locomotive address.  The exact format is defined by the
     * specific implementation, but for DCC systems it is intended that this
     * will be the DCC address in the form "nnnn" (extended) vs "nnn" or "nn" (short).
     * Non-DCC systems may use a different form.
     *
     * This is an unbound parameter.
     */
    public String getLocoAddress()
    {
        return "";
    }


    // register for notification if any of the properties change
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p)
    {
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p)
    {
    }


    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when
     * it's the last user.
     */
    public void dispose()
    {
    }


    public int getDccAddress()
    {
        return address;
    }

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement()
    {
        return speedIncrement;
    }

    // information on consisting  (how do we set consisting?)

    // register for notification

}
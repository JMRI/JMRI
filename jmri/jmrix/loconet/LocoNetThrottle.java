package jmri.jmrix.loconet;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific to a LocoNet connection.
 */
public class LocoNetThrottle extends AbstractThrottle {
    private LocoNetSlot slot;
    private LocoNetInterface network;

    /**
     * Constructor
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public LocoNetThrottle(LocoNetSlot slot) {
        super();

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

        // f9 through 12 are not in the slot; we have to maintain them
        // locally

        this.f9  = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
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

        // start a periodically sending the speed, to keep this
        // attached
        startRefresh();

    }


    /**
     * Send the LocoNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1()
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
    protected void sendFunctionGroup2()
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

    protected void sendFunctionGroup3() {
        // not implemented yet
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    public void setSpeedSetting(float speed)
    {
        this.speedSetting = speed;
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_SPD);
        msg.setElement(1, slot.getSlot());
        int value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>127) value = 127;    // max possible speed
        if (value<0) value = 1;        // emergency stop
        msg.setElement(2, value);
        network.sendLocoNetMessage(msg);
    }

    /**
     * LocoNet actually puts forward and backward in the same message
     * as the first function group.
     */
    public void setIsForward(boolean forward)
    {
        isForward = forward;
        sendFunctionGroup1();
    }

    /**
     * Release the loco from this throttle, then clean up the
     * object.
     */
    public void release() {
        if (!active) log.warn("release called when not active");

        // set status to common
        LnTrafficController.instance().sendLocoNetMessage(
                slot.writeStatus(LnConstants.LOCO_COMMON));

        dispose();
    }

    /**
     * Dispatch the loco from this throttle, then clean up the
     * object.
     */
    public void dispatch() {
        if (!active) log.warn("dispatch called when not active");

        // set status to common
        LnTrafficController.instance().sendLocoNetMessage(
                slot.writeStatus(LnConstants.LOCO_COMMON));

        // and dispatch to slot 0
        LnTrafficController.instance().sendLocoNetMessage(slot.dispatchSlot());

        dispose();
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        log.debug("dispose");
        super.dispose();

        // stop timeout
        mRefreshTimer.stop();

        // release connections
        mRefreshTimer = null;
        slot = null;
        network = null;

        // if this object has registered any listeners, remove those.

        // is there a dispose method in the superclass?
     }

    javax.swing.Timer mRefreshTimer = null;

    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(50000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                timeout();
            }
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void timeout() {
        setSpeedSetting(speedSetting);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetThrottle.class.getName());

}
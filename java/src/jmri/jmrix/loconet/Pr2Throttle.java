package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific
 * to a PR2 connection.
 * <P>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in LocoNet is an int
 * with values from 0 to 127.
 * <P>
 * @author  Bob Jacobsen  Copyright (C) 2006
 * @version $Revision$
 */
public class Pr2Throttle extends AbstractThrottle {
    private int addr;
    DccLocoAddress address;
    
    /**
     * Constructor
     */
    public Pr2Throttle(LocoNetSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        this.address = address;
        addr = address.getNumber();
        this.speedIncrement = 1;  // 128 step mode only
    }


    /**
     * Convert a LocoNet speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) return 0.f;
        else if (lSpeed == 1) return -1.f;   // estop
        if(getSpeedStepMode()==DccThrottle.SpeedStepMode28){
            if(lSpeed<=15) //Value less than 15 is in the stop/estop range bracket
                return 0.f;
            return (((lSpeed-12)/4)/28.f);
        }  else if (getSpeedStepMode()==DccThrottle.SpeedStepMode14){
            if(lSpeed<=15) //Value less than 15 is in the stop/estop range bracket
                return 0.f;
            return ((lSpeed-8)/8)/14.f;
        }
        else return ( (lSpeed-1)/126.f);
    }

    /**
     * Convert a float speed value to a LocoNet speed integer
     */
    protected int intSpeed(float fSpeed) {
      if (fSpeed == 0.f)
        return 0;
      else if (fSpeed < 0.f)
        return 1;   // estop
      if(getSpeedStepMode()==DccThrottle.SpeedStepMode28){
        return (int)((fSpeed*28)*4)+12;
      } else if (getSpeedStepMode()==DccThrottle.SpeedStepMode14){
          return (int)((fSpeed*14)*8)+8;
      }
        // add the 0.5 to handle float to int round for positive numbers
      return (int)(fSpeed * 126.f + 0.5) + 1 ;
    }

    public void writeData() {
        // convert contents
        int stat = 0;

        int speed = intSpeed(speedSetting);
        
        int dirf = 0; // contains dir, f0, f4-1
        if (getF0()) dirf |= (1<<4);
        if (getF1()) dirf |= (1<<0);
        if (getF2()) dirf |= (1<<1);
        if (getF3()) dirf |= (1<<2);
        if (getF4()) dirf |= (1<<3);
        if (!getIsForward()) dirf |= (1<<5);  // note sign of bit
        
        // contains f11-5
        int f11 = 0;
        if (getF5())  f11 |= (1<<0);
        if (getF6())  f11 |= (1<<1);
        if (getF7())  f11 |= (1<<2);
        if (getF8())  f11 |= (1<<3);
        if (getF9())  f11 |= (1<<4);
        if (getF10()) f11 |= (1<<5);
        if (getF11()) f11 |= (1<<6);
        
        // contains F19-F13
        int f19 = 0;

        // contains F27-F21
        int f27 = 0;

        // contains F28, F20, F12
        int f28 = 0;
        if (getF12()) f28 |= (1<<4);
        
        LocoNetMessage l = new LocoNetMessage(21);
        l.setOpCode( LnConstants.OPC_WR_SL_DATA_EXP );
        int i = 1;
        l.setElement(i++, 21);      // length
        l.setElement(i++, 0);       // EXP_MAST
        l.setElement(i++, 1);       // EXP_SLOT
        l.setElement(i++, stat & 0x7F);     // EXPD_STAT
        l.setElement(i++, addr & 0x7F);     // EXPD_ADRL
        l.setElement(i++, (addr/128)&0x7F); // EXPD_ADRH
        l.setElement(i++, 0);               // EXPD_FLAGS
        l.setElement(i++, speed & 0x7F);    // EXPD_SPD
        l.setElement(i++, f28 & 0x7F);      // EXPD_F28F20F12
        l.setElement(i++, dirf & 0x7F);     // EXPD_DIR_F0F4_F1
        l.setElement(i++, f11 & 0x7F);      // EXPD_F11_F5
        l.setElement(i++, f19 & 0x7F);      // EXPD_F19_F13
        l.setElement(i++, f27 & 0x7F);      // EXPD_F27_F21
        // rest are zero
        
        ((LocoNetSystemConnectionMemo)adapterMemo).getLnTrafficController().sendLocoNetMessage(l);
        //LnTrafficController.instance().sendLocoNetMessage(l);
    }

    /**
     * Send the LocoNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4.
     * Invoked by AbstractThrottle when needed.
     */
    protected void sendFunctionGroup1() {
        writeData();
    }

    /**
     * Send the LocoNet message to set the state of
     * functions F5, F6, F7, F8.
     * Invoked by AbstractThrottle when needed.
     */
    protected void sendFunctionGroup2() {
        writeData();
    }

    protected void sendFunctionGroup3() {
        writeData();
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed<0) this.speedSetting = -1.f;

        writeData();
        if (oldSpeed != this.speedSetting)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
        record(speed);
    }

    /**
     * LocoNet actually puts forward and backward in the same message
     * as the first function group.
     */
    public void setIsForward(boolean forward)
    {
        boolean old = isForward; 
        isForward = forward;
        sendFunctionGroup1();
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    /**
     * Release the loco from this throttle, then clean up the
     * object.
     */
    public void release() {
        dispose();
    }

    /**
     * Dispatch the loco from this throttle, then clean up the
     * object.
     */
    public void dispatch() {
        dispose();
    }

    public String toString() {
        return getLocoAddress().toString();
    }
    
    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        log.debug("dispose");
        super.dispose();
     }

    public LocoAddress getLocoAddress() {
        return address;
    }

    protected void throttleDispose(){ finishRecord(); }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(Pr2Throttle.class.getName());

}

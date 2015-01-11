package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.DccThrottle;
import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.BasicRosterEntry;

import java.util.Date;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import jmri.Throttle;

/**
 * An abstract implementation of DccThrottle.
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 * <P>
 * Note that this implements DccThrottle, not Throttle directly, so 
 * it has some DCC-specific content.
 *
 * @author  Bob Jacobsen  Copyright (C) 2001, 2005
 * @version $Revision$
 */
abstract public class AbstractThrottle implements DccThrottle {
	public final static float SPEED_STEP_14_INCREMENT=1.0f/14.0f;
	public final static float SPEED_STEP_27_INCREMENT=1.0f/27.0f;
	public final static float SPEED_STEP_28_INCREMENT=1.0f/28.0f;
	public final static float SPEED_STEP_128_INCREMENT=1.0f/126.0f; // remember there are only 126 
																	// non-stop values in 128 speed 
	
    protected float speedSetting;
    protected float speedIncrement;
    /**
     * Question: should we set a default speed step mode so it's never zero?
     */
    protected int speedStepMode; 
    protected boolean isForward;
    protected boolean f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12;
    protected boolean f13, f14, f15, f16, f17, f18, f19, f20, f21, f22, f23,
			f24, f25, f26, f27, f28;
    protected boolean f0Momentary, f1Momentary, f2Momentary, f3Momentary,
			f4Momentary, f5Momentary, f6Momentary, f7Momentary, f8Momentary,
			f9Momentary, f10Momentary, f11Momentary, f12Momentary;
    protected boolean f13Momentary, f14Momentary, f15Momentary, f16Momentary,
			f17Momentary, f18Momentary, f19Momentary, f20Momentary,
			f21Momentary, f22Momentary, f23Momentary, f24Momentary,
			f25Momentary, f26Momentary, f27Momentary, f28Momentary; 

    /**
     * Is this object still usable?  Set false after dispose, this
     * variable is used to check for incorrect usage.
     */
    protected boolean active;

    public AbstractThrottle(SystemConnectionMemo memo) {
        active = true;
        adapterMemo = memo;
    }
    
    protected SystemConnectionMemo adapterMemo;

    /** speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting() {
        return speedSetting;
    }

    /*
     * setSpeedSetting - Implementing functions should override this 
     * function, but should either make a call to super.setSpeedSetting()
     * to notify the listeners, or should notify the listeners themselves.
     */
    public void setSpeedSetting(float speed)
    {
        if(Math.abs(this.speedSetting - speed)>0.0001 )
                notifyPropertyChangeListener("SpeedSetting",
                                             new Float(this.speedSetting),
                                             new Float(this.speedSetting =
                                                       speed));
        record(speed);
   }

    /** direction
     * This is an bound parameter.
     */
    public boolean getIsForward() {
        return isForward;
    }

    /*
     * setIsForward - Implementing functions should override this 
     * function, but should either make a call to super.setIsForward()
     * to notify the listeners, or should notify the listeners themselves.
     */
    public void setIsForward(boolean forward)
    {
        if(forward!=this.isForward)
           notifyPropertyChangeListener("IsForward",
                                 Boolean.valueOf(this.isForward),
                                 Boolean.valueOf(this.isForward=forward));
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0() {
        return f0;
    }

    public boolean getF1() {
        return f1;
    }

    public boolean getF2() {
        return f2;
    }

    public boolean getF3() {
        return f3;
    }

    public boolean getF4() {
        return f4;
    }

    public boolean getF5() {
        return f5;
    }

    public boolean getF6() {
        return f6;
    }

    public boolean getF7() {
        return f7;
    }

    public boolean getF8() {
        return f8;
    }

    public boolean getF9() {
        return f9;
    }

    public boolean getF10() {
        return f10;
    }

    public boolean getF11() {
        return f11;
    }

    public boolean getF12() {
        return f12;
    }
    
    public boolean getF13() {
        return f13;
    }
    
    public boolean getF14() {
        return f14;
    }
    
    public boolean getF15() {
        return f15;
    }
    
    public boolean getF16() {
        return f16;
    }
    
    public boolean getF17() {
        return f17;
    }
    
    public boolean getF18() {
        return f18;
    }
    
    public boolean getF19() {
        return f19;
    }
    
    public boolean getF20() {
        return f20;
    }
    
    public boolean getF21() {
        return f21;
    }
    
    public boolean getF22() {
        return f22;
    }
    
    public boolean getF23() {
        return f23;
    }
    
    public boolean getF24() {
        return f24;
    }
    
    public boolean getF25() {
        return f25;
    }
    
    public boolean getF26() {
        return f26;
    }
    
    public boolean getF27() {
        return f27;
    }
    
    public boolean getF28() {
        return f28;
    }

    // function momentary status  - note that we use the naming for DCC, 
    // though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0Momentary() {
        return f0Momentary;
    }

    public boolean getF1Momentary() {
        return f1Momentary;
    }

    public boolean getF2Momentary() {
        return f2Momentary;
    }

    public boolean getF3Momentary() {
        return f3Momentary;
    }

    public boolean getF4Momentary() {
        return f4Momentary;
    }

    public boolean getF5Momentary() {
        return f5Momentary;
    }

    public boolean getF6Momentary() {
        return f6Momentary;
    }

    public boolean getF7Momentary() {
        return f7Momentary;
    }

    public boolean getF8Momentary() {
        return f8Momentary;
    }

    public boolean getF9Momentary() {
        return f9Momentary;
    }

    public boolean getF10Momentary() {
        return f10Momentary;
    }

    public boolean getF11Momentary() {
        return f11Momentary;
    }

    public boolean getF12Momentary() {
        return f12Momentary;
    }

    public boolean getF13Momentary() {
        return f13Momentary;
    }

    public boolean getF14Momentary() {
        return f14Momentary;
    }

    public boolean getF15Momentary() {
        return f15Momentary;
    }

    public boolean getF16Momentary() {
        return f16Momentary;
    }

    public boolean getF17Momentary() {
        return f17Momentary;
    }

    public boolean getF18Momentary() {
        return f18Momentary;
    }

    public boolean getF19Momentary() {
        return f19Momentary;
    }

    public boolean getF20Momentary() {
        return f20Momentary;
    }

    public boolean getF21Momentary() {
        return f21Momentary;
    }

    public boolean getF22Momentary() {
        return f22Momentary;
    }

    public boolean getF23Momentary() {
        return f23Momentary;
    }

    public boolean getF24Momentary() {
        return f24Momentary;
    }

    public boolean getF25Momentary() {
        return f25Momentary;
    }

    public boolean getF26Momentary() {
        return f26Momentary;
    }

    public boolean getF27Momentary() {
        return f27Momentary;
    }

    public boolean getF28Momentary() {
        return f28Momentary;
    }


    // register for notification if any of the properties change
    public void removePropertyChangeListener(PropertyChangeListener l) {
        log.debug("Removing property change " + l);
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
        log.debug("remove listeners size is " + listeners.size());
        if ((listeners.size()==0)){
            log.debug("Listener Size is 0 so will call the dispose in the InstanceManger with an empty throttleListenr null value");
            InstanceManager.throttleManagerInstance().disposeThrottle(this, new ThrottleListener(){
                public void notifyFailedThrottleRequest(DccLocoAddress address, String reason){ }
                public void notifyThrottleFound(DccThrottle t) { }
            });
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        log.debug("listeners added " + l);
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
        log.debug("listeners size is " + listeners.size());
    }

    /**
     * Trigger the notification of all PropertyChangeListeners
     */
    @SuppressWarnings("unchecked")
	protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        if (oldValue.equals(newValue)) log.error("notifyPropertyChangeListener without change");
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this)
            {
                v = (Vector<PropertyChangeListener>) listeners.clone();
            }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" listeners about property "
                                            +property);
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    public Vector<PropertyChangeListener> getListeners(){
        return listeners;
    }

    // data members to hold contact with the property listeners
    final private Vector<PropertyChangeListener> listeners = new Vector<PropertyChangeListener>();

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    @Deprecated
    public void dispose() {
        if (!active) log.error("Dispose called when not active");
        log.warn("Dispose called without knowing the original throttle listener");
        InstanceManager.throttleManagerInstance().disposeThrottle(this, null);
    }
    
    public void dispose(ThrottleListener l) {
        if (!active) log.error("Dispose called when not active");
        InstanceManager.throttleManagerInstance().disposeThrottle(this, l);
    }
    
    @Deprecated
    public void dispatch() {
        if (!active) log.warn("dispatch called when not active");
        log.warn("dispatch called without knowing the original throttle listener");
        InstanceManager.throttleManagerInstance().dispatchThrottle(this, null);
    }
    
    public void dispatch(ThrottleListener l) {
        if (!active) log.warn("dispatch called when not active");
        InstanceManager.throttleManagerInstance().dispatchThrottle(this, l);
    }
    
    @Deprecated
    public void release() {
        if (!active) log.warn("release called when not active");
        log.warn("Release called without knowing the original throttle listener");
        InstanceManager.throttleManagerInstance().releaseThrottle(this, null);
    }
    
    public void release(ThrottleListener l) {
        if (!active) log.warn("release called when not active");
        InstanceManager.throttleManagerInstance().releaseThrottle(this, l);
    }

    abstract protected void throttleDispose();

    /**
     * to handle quantized speed. Note this can change! Valued returned is
     * always positive.
     */
    public float getSpeedIncrement() {
        return speedIncrement;
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public void setF0(boolean f0) {
    	boolean old = this.f0;
        this.f0 = f0;
        sendFunctionGroup1();
        if (old != this.f0)
        	notifyPropertyChangeListener(Throttle.F0, old, this.f0 );
    }

    public void setF1(boolean f1) {
    	boolean old = this.f1;
        this.f1 = f1;
        sendFunctionGroup1();
        if (old != this.f1)
        	notifyPropertyChangeListener(Throttle.F1, old, this.f1 );
    }

    public void setF2(boolean f2) {
    	boolean old = this.f2;
        this.f2 = f2;
        sendFunctionGroup1();
        if (old != this.f2)
        	notifyPropertyChangeListener(Throttle.F2, old, this.f2 );
    }

    public void setF3(boolean f3) {
    	boolean old = this.f3;
        this.f3 = f3;
        sendFunctionGroup1();
        if (old != this.f3)
        	notifyPropertyChangeListener(Throttle.F3, old, this.f3 );
    }

    public void setF4(boolean f4) {
    	boolean old = this.f4;
        this.f4 = f4;
        sendFunctionGroup1();
        if (old != this.f4)
        	notifyPropertyChangeListener(Throttle.F4, old, this.f4 );
    }

    public void setF5(boolean f5) {
    	boolean old = this.f5;
        this.f5 = f5;
        sendFunctionGroup2();
        if (old != this.f5)
        	notifyPropertyChangeListener(Throttle.F5, old, this.f5 );
    }

    public void setF6(boolean f6) {
    	boolean old = this.f6;
        this.f6 = f6;
        sendFunctionGroup2();
        if (old != this.f6)
        	notifyPropertyChangeListener(Throttle.F6, old, this.f6 );
    }

    public void setF7(boolean f7) {
    	boolean old = this.f7;
        this.f7 = f7;
        sendFunctionGroup2();
        if (old != this.f7)
        	notifyPropertyChangeListener(Throttle.F7, old, this.f7 );
    }

    public void setF8(boolean f8) {
    	boolean old = this.f8;
        this.f8 = f8;
        sendFunctionGroup2();
        if (old != this.f8)
        	notifyPropertyChangeListener(Throttle.F8, old, this.f8 );
    }

    public void setF9(boolean f9) {
    	boolean old = this.f9;
        this.f9 = f9;
        sendFunctionGroup3();
        if (old != this.f9)
        	notifyPropertyChangeListener(Throttle.F9, old, this.f9 );
    }

    public void setF10(boolean f10) {
    	boolean old = this.f10;
        this.f10 = f10;
        sendFunctionGroup3();
        if (old != this.f10)
        	notifyPropertyChangeListener(Throttle.F10, old, this.f10 );
    }

    public void setF11(boolean f11) {
    	boolean old = this.f11;
        this.f11 = f11;
        sendFunctionGroup3();
        if (old != this.f11)
        	notifyPropertyChangeListener(Throttle.F11, old, this.f11 );
    }

    public void setF12(boolean f12) {
    	boolean old = this.f12;
        this.f12 = f12;
        sendFunctionGroup3();
        if (old != this.f12)
        	notifyPropertyChangeListener(Throttle.F12, old, this.f12 );
    }
    
    public void setF13(boolean f13) {
    	boolean old = this.f13;
        this.f13 = f13;
        sendFunctionGroup4();
        if (old != this.f13)
        	notifyPropertyChangeListener(Throttle.F13, old, this.f13 );
    }
    
    public void setF14(boolean f14) {
    	boolean old = this.f14;
        this.f14 = f14;
        sendFunctionGroup4();
        if (old != this.f14)
        	notifyPropertyChangeListener(Throttle.F14, old, this.f14 );
    }
    
    public void setF15(boolean f15) {
    	boolean old = this.f15;
        this.f15 = f15;
        sendFunctionGroup4();
        if (old != this.f15)
        	notifyPropertyChangeListener(Throttle.F15, old, this.f15 );
    }
    
    public void setF16(boolean f16) {
    	boolean old = this.f16;
        this.f16 = f16;
        sendFunctionGroup4();
        if (old != this.f16)
        	notifyPropertyChangeListener(Throttle.F16, old, this.f16 );
    }
    
    public void setF17(boolean f17) {
    	boolean old = this.f17;
        this.f17 = f17;
        sendFunctionGroup4();
        if (old != this.f17)
        	notifyPropertyChangeListener(Throttle.F17, old, this.f17 );
    }
    
    public void setF18(boolean f18) {
    	boolean old = this.f18;
        this.f18 = f18;
        sendFunctionGroup4();
        if (old != this.f18)
        	notifyPropertyChangeListener(Throttle.F18, old, this.f18 );
    }
    
    public void setF19(boolean f19) {
    	boolean old = this.f19;
        this.f19 = f19;
        sendFunctionGroup4();
        if (old != this.f19)
        	notifyPropertyChangeListener(Throttle.F19, old, this.f19 );
    }
    
    public void setF20(boolean f20) {
    	boolean old = this.f20;
        this.f20 = f20;
        sendFunctionGroup4();
        if (old != this.f20)
        	notifyPropertyChangeListener(Throttle.F20, old, this.f20 );
    }
    
    public void setF21(boolean f21) {
    	boolean old = this.f21;
        this.f21 = f21;
        sendFunctionGroup5();
        if (old != this.f21)
        	notifyPropertyChangeListener(Throttle.F21, old, this.f21 );
    }
    
    public void setF22(boolean f22) {
    	boolean old = this.f22;
        this.f22 = f22;
        sendFunctionGroup5();
        if (old != this.f22)
        	notifyPropertyChangeListener(Throttle.F22, old, this.f22 );
    }
    
    public void setF23(boolean f23) {
    	boolean old = this.f23;
        this.f23 = f23;
        sendFunctionGroup5();
        if (old != this.f23)
        	notifyPropertyChangeListener(Throttle.F23, old, this.f23 );
    }
    
    public void setF24(boolean f24) {
    	boolean old = this.f24;
        this.f24 = f24;
        sendFunctionGroup5();
        if (old != this.f24)
        	notifyPropertyChangeListener(Throttle.F24, old, this.f24 );
    }
    
    public void setF25(boolean f25) {
    	boolean old = this.f25;
        this.f25 = f25;
        sendFunctionGroup5();
        if (old != this.f25)
        	notifyPropertyChangeListener(Throttle.F25, old, this.f25 );
    }
    
    public void setF26(boolean f26) {
    	boolean old = this.f26;
        this.f26 = f26;
        sendFunctionGroup5();
        if (old != this.f26)
        	notifyPropertyChangeListener(Throttle.F26, old, this.f26 );
    }
    
    public void setF27(boolean f27) {
    	boolean old = this.f27;
        this.f27 = f27;
        sendFunctionGroup5();
        if (old != this.f27)
        	notifyPropertyChangeListener(Throttle.F27, old, this.f27 );
    }
    
    public void setF28(boolean f28) {
    	boolean old = this.f28;
        this.f28 = f28;
        sendFunctionGroup5();
        if (old != this.f28)
        	notifyPropertyChangeListener(Throttle.F28, old, this.f28 );
    }


    /**
     * Send the message to set the state of
     * functions F0, F1, F2, F3, F4.
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup1() {
        log.error("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup2() {
        log.error("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup3() {
        log.error("sendFunctionGroup3 needs to be implemented if invoked");
    }
    
    /**
     * Send the message to set the state of
     * functions F13, F14, F15, F16, F17, F18, F19, F20
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup4() {
        DccLocoAddress a = (DccLocoAddress) getLocoAddress();
        byte[] result = jmri.NmraPacket.function13Through20Packet(
                a.getNumber(), a.isLongAddress(), 
                getF13(), getF14(), getF15(), getF16(),
                getF17(), getF18(), getF19(), getF20());
        
        //if the result returns as null, we should quit.
        if (result==null)
            return;
        CommandStation c;
        if((adapterMemo!=null) && (adapterMemo.get(jmri.CommandStation.class)!=null))
            c = adapterMemo.get(jmri.CommandStation.class);
        else
            c = InstanceManager.commandStationInstance();
            
        // send it 3 times
        if (c != null) 
            c.sendPacket(result,3);
        else
            log.error("Can't send F13-F20 since no command station defined");
        return;
    }
    
    /**
     * Send the message to set the state of
     * functions F21, F22, F23, F24, F25, F26, F27, F28
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup5() {
        DccLocoAddress a = (DccLocoAddress) getLocoAddress();
        byte[] result = jmri.NmraPacket.function21Through28Packet(
                a.getNumber(), a.isLongAddress(), 
                getF21(), getF22(), getF23(), getF24(),
                getF25(), getF26(), getF27(), getF28());
        //if the result returns as null, we should quit.
        if (result==null)
            return;
        CommandStation c;
        if((adapterMemo!=null) && (adapterMemo.get(jmri.CommandStation.class)!=null))
            c = adapterMemo.get(jmri.CommandStation.class);
        else
            c = InstanceManager.commandStationInstance();
            
        // send it 3 times
        if (c != null) 
            c.sendPacket(result,3);
        else
            log.error("Can't send F21-F28 since no command station defined");
        return;
    }
    

    // function momentary status  - note that we use the naming for DCC, 
    // though that's not the implication;
    // see also DccThrottle interface
    public void setF0Momentary(boolean f0Momentary) {
    	boolean old = this.f0Momentary;
        this.f0Momentary = f0Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f0Momentary)
        	notifyPropertyChangeListener(Throttle.F0Momentary, old, this.f0Momentary );
    }

    public void setF1Momentary(boolean f1Momentary) {
    	boolean old = this.f1Momentary;
        this.f1Momentary = f1Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f1Momentary)
        	notifyPropertyChangeListener(Throttle.F1Momentary, old, this.f1Momentary );
    }

    public void setF2Momentary(boolean f2Momentary) {
    	boolean old = this.f2Momentary;
        this.f2Momentary = f2Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f2Momentary)
        	notifyPropertyChangeListener(Throttle.F2Momentary, old, this.f2Momentary );
    }

    public void setF3Momentary(boolean f3Momentary) {
    	boolean old = this.f3Momentary;
        this.f3Momentary = f3Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f3Momentary)
        	notifyPropertyChangeListener(Throttle.F3Momentary, old, this.f3Momentary );
    }

    public void setF4Momentary(boolean f4Momentary) {
    	boolean old = this.f4Momentary;
        this.f4Momentary = f4Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f4Momentary)
        	notifyPropertyChangeListener(Throttle.F4Momentary, old, this.f4Momentary );
    }

    public void setF5Momentary(boolean f5Momentary) {
    	boolean old = this.f5Momentary;
        this.f5Momentary = f5Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f5Momentary)
        	notifyPropertyChangeListener(Throttle.F5Momentary, old, this.f5Momentary );
    }

    public void setF6Momentary(boolean f6Momentary) {
    	boolean old = this.f6Momentary;
        this.f6Momentary = f6Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f6Momentary)
        	notifyPropertyChangeListener(Throttle.F6Momentary, old, this.f6Momentary );
    }

    public void setF7Momentary(boolean f7Momentary) {
    	boolean old = this.f7Momentary;
        this.f7Momentary = f7Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f7Momentary)
        	notifyPropertyChangeListener(Throttle.F7Momentary, old, this.f7Momentary );
    }

    public void setF8Momentary(boolean f8Momentary) {
    	boolean old = this.f8Momentary;
        this.f8Momentary = f8Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f8Momentary)
        	notifyPropertyChangeListener(Throttle.F8Momentary, old, this.f8Momentary );
    }

    public void setF9Momentary(boolean f9Momentary) {
    	boolean old = this.f9Momentary;
        this.f9Momentary = f9Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f9Momentary)
        	notifyPropertyChangeListener(Throttle.F9Momentary, old, this.f9Momentary );
    }

    public void setF10Momentary(boolean f10Momentary) {
    	boolean old = this.f10Momentary;
        this.f10Momentary = f10Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f10Momentary)
        	notifyPropertyChangeListener(Throttle.F10Momentary, old, this.f10Momentary );
    }

    public void setF11Momentary(boolean f11Momentary) {
    	boolean old = this.f11Momentary;
        this.f11Momentary = f11Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f11Momentary)
        	notifyPropertyChangeListener(Throttle.F11Momentary, old, this.f11Momentary );
    }

    public void setF12Momentary(boolean f12Momentary) {
    	boolean old = this.f12Momentary;
        this.f12Momentary = f12Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f12Momentary)
        	notifyPropertyChangeListener(Throttle.F12Momentary, old, this.f12Momentary );
    }

    public void setF13Momentary(boolean f13Momentary) {
    	boolean old = this.f13Momentary;
        this.f13Momentary = f13Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f13Momentary)
        	notifyPropertyChangeListener(Throttle.F13Momentary, old, this.f13Momentary );
    }

    public void setF14Momentary(boolean f14Momentary) {
    	boolean old = this.f14Momentary;
        this.f14Momentary = f14Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f14Momentary)
        	notifyPropertyChangeListener(Throttle.F14Momentary, old, this.f14Momentary );
    }

    public void setF15Momentary(boolean f15Momentary) {
    	boolean old = this.f15Momentary;
        this.f15Momentary = f15Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f15Momentary)
        	notifyPropertyChangeListener(Throttle.F15Momentary, old, this.f15Momentary );
    }

    public void setF16Momentary(boolean f16Momentary) {
    	boolean old = this.f16Momentary;
        this.f16Momentary = f16Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f16Momentary)
        	notifyPropertyChangeListener(Throttle.F16Momentary, old, this.f16Momentary );
    }

    public void setF17Momentary(boolean f17Momentary) {
    	boolean old = this.f17Momentary;
        this.f17Momentary = f17Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f17Momentary)
        	notifyPropertyChangeListener(Throttle.F17Momentary, old, this.f17Momentary );
    }

    public void setF18Momentary(boolean f18Momentary) {
    	boolean old = this.f18Momentary;
        this.f18Momentary = f18Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f18Momentary)
        	notifyPropertyChangeListener(Throttle.F18Momentary, old, this.f18Momentary );
    }

    public void setF19Momentary(boolean f19Momentary) {
    	boolean old = this.f19Momentary;
        this.f19Momentary = f19Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f19Momentary)
        	notifyPropertyChangeListener(Throttle.F19Momentary, old, this.f19Momentary );
    }

    public void setF20Momentary(boolean f20Momentary) {
    	boolean old = this.f20Momentary;
        this.f20Momentary = f20Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f20Momentary)
        	notifyPropertyChangeListener(Throttle.F20Momentary, old, this.f20Momentary );
    }

    public void setF21Momentary(boolean f21Momentary) {
    	boolean old = this.f21Momentary;
        this.f21Momentary = f21Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f21Momentary)
        	notifyPropertyChangeListener(Throttle.F21Momentary, old, this.f21Momentary );
    }

    public void setF22Momentary(boolean f22Momentary) {
    	boolean old = this.f22Momentary;
        this.f22Momentary = f22Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f22Momentary)
        	notifyPropertyChangeListener(Throttle.F22Momentary, old, this.f22Momentary );
    }

    public void setF23Momentary(boolean f23Momentary) {
    	boolean old = this.f23Momentary;
        this.f23Momentary = f23Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f23Momentary)
        	notifyPropertyChangeListener(Throttle.F23Momentary, old, this.f23Momentary );
    }

    public void setF24Momentary(boolean f24Momentary) {
    	boolean old = this.f24Momentary;
        this.f24Momentary = f24Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f24Momentary)
        	notifyPropertyChangeListener(Throttle.F24Momentary, old, this.f24Momentary );
    }

    public void setF25Momentary(boolean f25Momentary) {
    	boolean old = this.f25Momentary;
        this.f25Momentary = f25Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f25Momentary)
        	notifyPropertyChangeListener(Throttle.F25Momentary, old, this.f25Momentary );
    }

    public void setF26Momentary(boolean f26Momentary) {
    	boolean old = this.f26Momentary;
        this.f26Momentary = f26Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f26Momentary)
        	notifyPropertyChangeListener(Throttle.F26Momentary, old, this.f26Momentary );
    }

    public void setF27Momentary(boolean f27Momentary) {
    	boolean old = this.f27Momentary;
        this.f27Momentary = f27Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f27Momentary)
        	notifyPropertyChangeListener(Throttle.F27Momentary, old, this.f27Momentary );
    }

    public void setF28Momentary(boolean f28Momentary) {
    	boolean old = this.f28Momentary;
        this.f28Momentary = f28Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f28Momentary)
        	notifyPropertyChangeListener(Throttle.F28Momentary, old, this.f28Momentary );
    }

    /**
     * Send the message to set the momentary state of
     * functions F0, F1, F2, F3, F4.
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, a real implementation needs to be provided if the 
     * hardware supports setting functions momentary. 
     */
    protected void sendMomentaryFunctionGroup1() {
    }

    /**
     * Send the message to set the momentary state of
     * functions F5, F6, F7, F8.
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, but a real implementation needs to be provided if the 
     * hardware supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup2() {
    }

    /**
     * Send the message to set the Momentary state of
     * functions F9, F10, F11, F12
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, but a real implementation needs to be provided if the 
     * hardware supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup3() {
    }
    
    /**
     * Send the message to set the Momentary state of
     * functions F13, F14, F15, F16, F17, F18, F19, F20
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, but a real implementation needs to be provided if the 
     * hardware supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup4() {
     }

    
    /**
     * Send the message to set the Momentary state of
     * functions F21, F22, F23, F24, F25, F26, F27, F28
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, but a real implementation needs to be provided if the 
     * hardware supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup5() {
    }


    /*
     * setSpeedStepMode - set the speed step value and the related
     *                    speedIncrement value.
     * <P>
     * specific implementations should override this function
     * <P>
     * @param Mode - the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
    public void setSpeedStepMode(int Mode) {
    	if(log.isDebugEnabled()) log.debug("Speed Step Mode Change to Mode: " + Mode +
                " Current mode is: " + this.speedStepMode);
	if(speedStepMode!=Mode)
        	notifyPropertyChangeListener("SpeedSteps", this.speedStepMode, 
					      this.speedStepMode=Mode );
    	if(Mode==DccThrottle.SpeedStepMode14)
    		speedIncrement=SPEED_STEP_14_INCREMENT;
    	else if(Mode==DccThrottle.SpeedStepMode27)
    		speedIncrement=SPEED_STEP_27_INCREMENT;
    	else if(Mode==DccThrottle.SpeedStepMode28)
    		speedIncrement=SPEED_STEP_28_INCREMENT;
    	else // default to 128 speed step mode
    		speedIncrement=SPEED_STEP_128_INCREMENT; 
    }

    /*
     * getSpeedStepMode - get the current speed step value.
     * <P>
     */
     public int getSpeedStepMode() {
	    return speedStepMode;
     }
     
     long durationRunning = 0;
     long start;
   
    protected void record(float speed){
        if(re==null)
            return;
        if(speed==0){
            stopClock();
        } else {
            startClock();
        }
    }
    protected void startClock(){
        if(start==0)
            start = System.currentTimeMillis();
    }
    
    void stopClock() {
        if(start==0)
            return;
        long stop = System.currentTimeMillis();
        //Set running duration in seconds
        durationRunning = durationRunning + ((stop-start)/1000);
        start=0;
    }
    
    protected void finishRecord(){
        if(re==null){
            return;
        }
        stopClock();
        String currentDurationString = re.getAttribute("OperatingDuration");
        long currentDuration = 0;
        try {
            currentDuration = Long.valueOf(currentDurationString);
        } catch (Exception e){
            log.warn("current stored duration is not a valid number \"" + currentDurationString +" \"");
        }
        currentDuration = currentDuration + durationRunning;
        re.putAttribute("OperatingDuration", ""+currentDuration);
        Date date = new Date();
        re.putAttribute("LastOperated", ""+date);
        //Only store if the roster entry isn't open.
        if(!re.isOpen())
            re.store();
        else
            log.warn("Roster Entry " + re.getId() + " running time not saved as entry is already open for editing");
        re=null;
    }
    
     BasicRosterEntry re = null;
     
     public void setRosterEntry(BasicRosterEntry re){
        this.re = re;
     }
     
     public BasicRosterEntry getRosterEntry(){
        return re;
     }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(AbstractThrottle.class.getName());

}

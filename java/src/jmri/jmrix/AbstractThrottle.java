package jmri.jmrix;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Vector;
import jmri.BasicRosterEntry;
import jmri.CommandStation;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Throttle;
import jmri.ThrottleListener;

/**
 * An abstract implementation of DccThrottle. Based on Glen Oberhauser's
 * original LnThrottleManager implementation.
 * <p>
 * Note that this implements DccThrottle, not Throttle directly, so it has some
 * DCC-specific content.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005
 */
abstract public class AbstractThrottle implements DccThrottle {

    protected float speedSetting;
    /**
     * Question: should we set a default speed step mode so it's never zero?
     */
    protected SpeedStepMode speedStepMode = SpeedStepMode.UNKNOWN;
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
     * Is this object still usable? Set false after dispose, this variable is
     * used to check for incorrect usage.
     */
    protected boolean active;

    public AbstractThrottle(SystemConnectionMemo memo) {
        active = true;
        adapterMemo = memo;
        // set defaults for Momentary status.
        f0Momentary = false;
        f1Momentary = false;
        f2Momentary = false;
        f3Momentary = false;
        f4Momentary = false;
        f5Momentary = false;
        f6Momentary = false;
        f7Momentary = false;
        f9Momentary = false;
        f10Momentary = false;
        f11Momentary = false;
        f12Momentary = false;
        f13Momentary = false;
        f14Momentary = false;
        f15Momentary = false;
        f16Momentary = false;
        f17Momentary = false;
        f18Momentary = false;
        f19Momentary = false;
        f20Momentary = false;
        f21Momentary = false;
        f22Momentary = false;
        f23Momentary = false;
        f24Momentary = false;
        f25Momentary = false;
        f26Momentary = false;
        f27Momentary = false;
        f28Momentary = false;
    }

    protected SystemConnectionMemo adapterMemo;

    /**
     * speed - expressed as a value {@literal 0.0 -> 1.0.} Negative means
     * emergency stop. This is an bound parameter.
     *
     * @return speed
     */
    @Override
    public float getSpeedSetting() {
        return speedSetting;
    }

    /**
     * setSpeedSetting - Implementing functions should override this function,
     * but should either make a call to super.setSpeedSetting() to notify the
     * listeners at the end of their work, or should notify the listeners themselves.
     *
     */
    @Override
    public void setSpeedSetting(float speed) {
        setSpeedSetting(speed, false, false);
    }

    /**
     * setSpeedSetting - Implementations should override this method only if they normally suppress
     * messages to the system if, as far as JMRI can tell, the new message would make no difference
     * to the system state (eg. the speed is the same, or effectivly the same, as the existing speed).
     * Then, the boolean options can affect this behaviour.
     *
     * @param speed  the new speed
     * @param allowDuplicates  don't suppress messages
     * @param allowDuplicatesOnStop  don't suppress messages if the new speed is 'stop'
     */
    @Override
    public void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop) {
        if (Math.abs(this.speedSetting - speed) > 0.0001) {
            notifyPropertyChangeListener(SPEEDSETTING, this.speedSetting, this.speedSetting = speed);
        }
        record(speed);
    }

    /**
     * setSpeedSettingAgain - set the speed and don't ever supress the sending of messages to the system
     *
     * @param speed  the new speed
     */
    @Override
    public void setSpeedSettingAgain(float speed) {
        setSpeedSetting(speed, true, true);
    }

    /**
     * direction This is an bound parameter.
     *
     * @return true if locomotive is running forward
     */
    @Override
    public boolean getIsForward() {
        return isForward;
    }

    /**
     * setIsForward - Implementing functions should override this function, but
     * should either make a call to super.setIsForward() to notify the
     * listeners, or should notify the listeners themselves.
     *
     */
    @Override
    public void setIsForward(boolean forward) {
        if (forward != this.isForward) {
            notifyPropertyChangeListener(ISFORWARD, this.isForward, this.isForward = forward);
        }
    }

    /**
     * functions - note that we use the naming for DCC, though that's not the implication;
     * see also DccThrottle interface
     * {@inheritDoc}
     */
    @Override
    public boolean getF0() {
        return f0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF1() {
        return f1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF2() {
        return f2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF3() {
        return f3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF4() {
        return f4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF5() {
        return f5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF6() {
        return f6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF7() {
        return f7;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF8() {
        return f8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF9() {
        return f9;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF10() {
        return f10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF11() {
        return f11;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF12() {
        return f12;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF13() {
        return f13;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF14() {
        return f14;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF15() {
        return f15;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF16() {
        return f16;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF17() {
        return f17;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF18() {
        return f18;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF19() {
        return f19;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF20() {
        return f20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF21() {
        return f21;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF22() {
        return f22;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF23() {
        return f23;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF24() {
        return f24;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF25() {
        return f25;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF26() {
        return f26;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF27() {
        return f27;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF28() {
        return f28;
    }

    /**
     * function momentary status  - note that we use the naming for DCC, 
     * though that's not the implication;
     * see also DccThrottle interface
     * {@inheritDoc}
     */
    @Override
    public boolean getF0Momentary() {
        return f0Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF1Momentary() {
        return f1Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF2Momentary() {
        return f2Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF3Momentary() {
        return f3Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF4Momentary() {
        return f4Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF5Momentary() {
        return f5Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF6Momentary() {
        return f6Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF7Momentary() {
        return f7Momentary;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF8Momentary() {
        return f8Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF9Momentary() {
        return f9Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF10Momentary() {
        return f10Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF11Momentary() {
        return f11Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF12Momentary() {
        return f12Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF13Momentary() {
        return f13Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF14Momentary() {
        return f14Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF15Momentary() {
        return f15Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF16Momentary() {
        return f16Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF17Momentary() {
        return f17Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF18Momentary() {
        return f18Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF19Momentary() {
        return f19Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF20Momentary() {
        return f20Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF21Momentary() {
        return f21Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF22Momentary() {
        return f22Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF23Momentary() {
        return f23Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF24Momentary() {
        return f24Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF25Momentary() {
        return f25Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF26Momentary() {
        return f26Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF27Momentary() {
        return f27Momentary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getF28Momentary() {
        return f28Momentary;
    }
    
    /**
     * Notify listeners that a Throttle has disconnected
     * and is no longer available for use.
     * <p>
     * For when throttles have been stolen or encounter hardware 
     * error, and a normal release / dispose is not possible.
     *
     */
    protected void notifyThrottleDisconnect() {
        notifyPropertyChangeListener("ThrottleConnected", true, false ); // NOI18N
    }
    
    // set initial values purely for changelistener following
    // the 1st true or false will always get sent
    private Boolean _dispatchEnabled = null; 
    private Boolean _releaseEnabled = null;
    
    
    /**
     * Notify listeners that a Throttle has Dispatch enabled or disabled.
     * <p>
     * For systems where dispatch availability is variable.
     * <p>
     * Does not notify if existing value is unchanged.
     *
     * @param newVal true if Dispatch enabled, else false
     *
     */
    @Override
    public void notifyThrottleDispatchEnabled( boolean newVal ) {
        if (_dispatchEnabled == null){
            _dispatchEnabled = !newVal; // make sure the 1st time is always sent
        }
        if ( newVal == _dispatchEnabled ) {
            return;
        }
        else {
            notifyPropertyChangeListener("DispatchEnabled", _dispatchEnabled, newVal ); // NOI18N
            _dispatchEnabled = newVal;
        }
    }
    
    /**
     * Notify listeners that a Throttle has Release enabled or disabled.
     * <p>
     * For systems where release availability is variable.
     * <p>
     * Does not notify if existing value is unchanged.
     *
     * @param newVal true if Release enabled, else false
     *
     */
    @Override
    public void notifyThrottleReleaseEnabled( boolean newVal ) {
        if (_releaseEnabled == null){
            _releaseEnabled = !newVal; // make sure the 1st time is always sent
        }
        if ( newVal == _releaseEnabled ) {
            return;
        }
        else {
            notifyPropertyChangeListener("ReleaseEnabled", _releaseEnabled, newVal ); // NOI18N
            _releaseEnabled = newVal;
        }
    }

    /**
     * Remove notification listener
     * {@inheritDoc}
     *
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        log.debug("Removing property change {}", l);
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
        log.debug("remove listeners size is {}", listeners.size());
        if ((listeners.isEmpty())) {
            log.debug("No listeners so will call the dispose in the InstanceManger with an empty throttleListener null value");
            InstanceManager.throttleManagerInstance().disposeThrottle(this, new ThrottleListener() {
                @Override
                public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                }

                @Override
                public void notifyThrottleFound(DccThrottle t) {
                }
                
                /**
                 * {@inheritDoc}
                 * @deprecated since 4.15.7; use #notifyDecisionRequired
                 */
                @Override
                @Deprecated
                public void notifyStealThrottleRequired(jmri.LocoAddress address) {
                }
    
                @Override
                public void notifyDecisionRequired(LocoAddress address, DecisionType question){
                }
            });
        }
    }

    /**
     * Register for notification if any of the properties change
     * {@inheritDoc}
     *
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        log.debug("listeners added {}", l);
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
        log.debug("listeners size is {}", listeners.size());
    }

    /**
     * Trigger the notification of all PropertyChangeListeners
     */
    @SuppressWarnings("unchecked")
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        if ((oldValue != null && oldValue.equals(newValue)) || oldValue == newValue) {
            log.error("notifyPropertyChangeListener without change");
        }
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized (this) {
            v = (Vector<PropertyChangeListener>) listeners.clone();
        }
        log.debug("notify {} listeners about property {}",v.size(),property);
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector<PropertyChangeListener> getListeners() {
        return listeners;
    }

    // data members to hold contact with the property listeners
    final private Vector<PropertyChangeListener> listeners = new Vector<>();

    /**
     * Call from a ThrottleListener to dispose of the throttle instance
     * 
     * @param l the propertychangelistener instance
     *
     */
    @Override
    public void dispose(ThrottleListener l) {
        if (!active) {
            log.error("Dispose called when not active");
        }
        InstanceManager.throttleManagerInstance().disposeThrottle(this, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatch(ThrottleListener l) {
        if (!active) {
            log.warn("dispatch called when not active");
        }
        InstanceManager.throttleManagerInstance().dispatchThrottle(this, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release(ThrottleListener l) {
        if (!active) {
            log.warn("release called when not active");
        }
        InstanceManager.throttleManagerInstance().releaseThrottle(this, l);
    }

    abstract protected void throttleDispose();

    /**
     * to handle quantized speed. Note this can change! Valued returned is
     * always positive.
     *
     * @return 1 divided by the number of speed steps this DCC throttle supports
     */
    @Override
    public float getSpeedIncrement() {
        return speedStepMode.increment;
    }

    /**
     * functions - note that we use the naming for DCC, though that's not the implication;
     * see also DccThrottle interface
     * {@inheritDoc}
     */
    @Override
    public void setF0(boolean f0) {
        boolean old = this.f0;
        this.f0 = f0;
        sendFunctionGroup1();
        if (old != this.f0) {
            notifyPropertyChangeListener(Throttle.F0, old, this.f0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF1(boolean f1) {
        boolean old = this.f1;
        this.f1 = f1;
        sendFunctionGroup1();
        if (old != this.f1) {
            notifyPropertyChangeListener(Throttle.F1, old, this.f1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF2(boolean f2) {
        boolean old = this.f2;
        this.f2 = f2;
        sendFunctionGroup1();
        if (old != this.f2) {
            notifyPropertyChangeListener(Throttle.F2, old, this.f2);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF3(boolean f3) {
        boolean old = this.f3;
        this.f3 = f3;
        sendFunctionGroup1();
        if (old != this.f3) {
            notifyPropertyChangeListener(Throttle.F3, old, this.f3);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF4(boolean f4) {
        boolean old = this.f4;
        this.f4 = f4;
        sendFunctionGroup1();
        if (old != this.f4) {
            notifyPropertyChangeListener(Throttle.F4, old, this.f4);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF5(boolean f5) {
        boolean old = this.f5;
        this.f5 = f5;
        sendFunctionGroup2();
        if (old != this.f5) {
            notifyPropertyChangeListener(Throttle.F5, old, this.f5);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF6(boolean f6) {
        boolean old = this.f6;
        this.f6 = f6;
        sendFunctionGroup2();
        if (old != this.f6) {
            notifyPropertyChangeListener(Throttle.F6, old, this.f6);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF7(boolean f7) {
        boolean old = this.f7;
        this.f7 = f7;
        sendFunctionGroup2();
        if (old != this.f7) {
            notifyPropertyChangeListener(Throttle.F7, old, this.f7);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF8(boolean f8) {
        boolean old = this.f8;
        this.f8 = f8;
        sendFunctionGroup2();
        if (old != this.f8) {
            notifyPropertyChangeListener(Throttle.F8, old, this.f8);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF9(boolean f9) {
        boolean old = this.f9;
        this.f9 = f9;
        sendFunctionGroup3();
        if (old != this.f9) {
            notifyPropertyChangeListener(Throttle.F9, old, this.f9);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF10(boolean f10) {
        boolean old = this.f10;
        this.f10 = f10;
        sendFunctionGroup3();
        if (old != this.f10) {
            notifyPropertyChangeListener(Throttle.F10, old, this.f10);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF11(boolean f11) {
        boolean old = this.f11;
        this.f11 = f11;
        sendFunctionGroup3();
        if (old != this.f11) {
            notifyPropertyChangeListener(Throttle.F11, old, this.f11);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF12(boolean f12) {
        boolean old = this.f12;
        this.f12 = f12;
        sendFunctionGroup3();
        if (old != this.f12) {
            notifyPropertyChangeListener(Throttle.F12, old, this.f12);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF13(boolean f13) {
        boolean old = this.f13;
        this.f13 = f13;
        sendFunctionGroup4();
        if (old != this.f13) {
            notifyPropertyChangeListener(Throttle.F13, old, this.f13);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF14(boolean f14) {
        boolean old = this.f14;
        this.f14 = f14;
        sendFunctionGroup4();
        if (old != this.f14) {
            notifyPropertyChangeListener(Throttle.F14, old, this.f14);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF15(boolean f15) {
        boolean old = this.f15;
        this.f15 = f15;
        sendFunctionGroup4();
        if (old != this.f15) {
            notifyPropertyChangeListener(Throttle.F15, old, this.f15);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF16(boolean f16) {
        boolean old = this.f16;
        this.f16 = f16;
        sendFunctionGroup4();
        if (old != this.f16) {
            notifyPropertyChangeListener(Throttle.F16, old, this.f16);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF17(boolean f17) {
        boolean old = this.f17;
        this.f17 = f17;
        sendFunctionGroup4();
        if (old != this.f17) {
            notifyPropertyChangeListener(Throttle.F17, old, this.f17);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF18(boolean f18) {
        boolean old = this.f18;
        this.f18 = f18;
        sendFunctionGroup4();
        if (old != this.f18) {
            notifyPropertyChangeListener(Throttle.F18, old, this.f18);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF19(boolean f19) {
        boolean old = this.f19;
        this.f19 = f19;
        sendFunctionGroup4();
        if (old != this.f19) {
            notifyPropertyChangeListener(Throttle.F19, old, this.f19);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF20(boolean f20) {
        boolean old = this.f20;
        this.f20 = f20;
        sendFunctionGroup4();
        if (old != this.f20) {
            notifyPropertyChangeListener(Throttle.F20, old, this.f20);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF21(boolean f21) {
        boolean old = this.f21;
        this.f21 = f21;
        sendFunctionGroup5();
        if (old != this.f21) {
            notifyPropertyChangeListener(Throttle.F21, old, this.f21);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF22(boolean f22) {
        boolean old = this.f22;
        this.f22 = f22;
        sendFunctionGroup5();
        if (old != this.f22) {
            notifyPropertyChangeListener(Throttle.F22, old, this.f22);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF23(boolean f23) {
        boolean old = this.f23;
        this.f23 = f23;
        sendFunctionGroup5();
        if (old != this.f23) {
            notifyPropertyChangeListener(Throttle.F23, old, this.f23);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF24(boolean f24) {
        boolean old = this.f24;
        this.f24 = f24;
        sendFunctionGroup5();
        if (old != this.f24) {
            notifyPropertyChangeListener(Throttle.F24, old, this.f24);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF25(boolean f25) {
        boolean old = this.f25;
        this.f25 = f25;
        sendFunctionGroup5();
        if (old != this.f25) {
            notifyPropertyChangeListener(Throttle.F25, old, this.f25);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF26(boolean f26) {
        boolean old = this.f26;
        this.f26 = f26;
        sendFunctionGroup5();
        if (old != this.f26) {
            notifyPropertyChangeListener(Throttle.F26, old, this.f26);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF27(boolean f27) {
        boolean old = this.f27;
        this.f27 = f27;
        sendFunctionGroup5();
        if (old != this.f27) {
            notifyPropertyChangeListener(Throttle.F27, old, this.f27);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF28(boolean f28) {
        boolean old = this.f28;
        this.f28 = f28;
        sendFunctionGroup5();
        if (old != this.f28) {
            notifyPropertyChangeListener(Throttle.F28, old, this.f28);
        }
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup1() {
        log.error("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup2() {
        log.error("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup3() {
        log.error("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of functions F13, F14, F15, F16, F17,
     * F18, F19, F20
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup4() {
        DccLocoAddress a = (DccLocoAddress) getLocoAddress();
        byte[] result = jmri.NmraPacket.function13Through20Packet(
                a.getNumber(), a.isLongAddress(),
                getF13(), getF14(), getF15(), getF16(),
                getF17(), getF18(), getF19(), getF20());

        //if the result returns as null, we should quit.
        if (result == null) {
            return;
        }
        CommandStation c;
        if ((adapterMemo != null) && (adapterMemo.get(jmri.CommandStation.class) != null)) {
            c = adapterMemo.get(jmri.CommandStation.class);
        } else {
            c = InstanceManager.getNullableDefault(CommandStation.class);
        }

        // send it 3 times
        if (c != null) {
            c.sendPacket(result, 3);
        } else {
            log.error("Can't send F13-F20 since no command station defined");
        }
    }

    /**
     * Send the message to set the state of functions F21, F22, F23, F24, F25,
     * F26, F27, F28
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup5() {
        DccLocoAddress a = (DccLocoAddress) getLocoAddress();
        byte[] result = jmri.NmraPacket.function21Through28Packet(
                a.getNumber(), a.isLongAddress(),
                getF21(), getF22(), getF23(), getF24(),
                getF25(), getF26(), getF27(), getF28());
        //if the result returns as null, we should quit.
        if (result == null) {
            return;
        }
        CommandStation c;
        if ((adapterMemo != null) && (adapterMemo.get(jmri.CommandStation.class) != null)) {
            c = adapterMemo.get(jmri.CommandStation.class);
        } else {
            c = InstanceManager.getNullableDefault(CommandStation.class);
        }

        // send it 3 times
        if (c != null) {
            c.sendPacket(result, 3);
        } else {
            log.error("Can't send F21-F28 since no command station defined");
        }
    }

    /**
     * function momentary status  - note that we use the naming for DCC, 
     * though that's not the implication;
     * see also DccThrottle interface
     * {@inheritDoc}
     */
    @Override
    public void setF0Momentary(boolean f0Momentary) {
        boolean old = this.f0Momentary;
        this.f0Momentary = f0Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f0Momentary) {
            notifyPropertyChangeListener(Throttle.F0Momentary, old, this.f0Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF1Momentary(boolean f1Momentary) {
        boolean old = this.f1Momentary;
        this.f1Momentary = f1Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f1Momentary) {
            notifyPropertyChangeListener(Throttle.F1Momentary, old, this.f1Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF2Momentary(boolean f2Momentary) {
        boolean old = this.f2Momentary;
        this.f2Momentary = f2Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f2Momentary) {
            notifyPropertyChangeListener(Throttle.F2Momentary, old, this.f2Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF3Momentary(boolean f3Momentary) {
        boolean old = this.f3Momentary;
        this.f3Momentary = f3Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f3Momentary) {
            notifyPropertyChangeListener(Throttle.F3Momentary, old, this.f3Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF4Momentary(boolean f4Momentary) {
        boolean old = this.f4Momentary;
        this.f4Momentary = f4Momentary;
        sendMomentaryFunctionGroup1();
        if (old != this.f4Momentary) {
            notifyPropertyChangeListener(Throttle.F4Momentary, old, this.f4Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF5Momentary(boolean f5Momentary) {
        boolean old = this.f5Momentary;
        this.f5Momentary = f5Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f5Momentary) {
            notifyPropertyChangeListener(Throttle.F5Momentary, old, this.f5Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF6Momentary(boolean f6Momentary) {
        boolean old = this.f6Momentary;
        this.f6Momentary = f6Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f6Momentary) {
            notifyPropertyChangeListener(Throttle.F6Momentary, old, this.f6Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF7Momentary(boolean f7Momentary) {
        boolean old = this.f7Momentary;
        this.f7Momentary = f7Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f7Momentary) {
            notifyPropertyChangeListener(Throttle.F7Momentary, old, this.f7Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF8Momentary(boolean f8Momentary) {
        boolean old = this.f8Momentary;
        this.f8Momentary = f8Momentary;
        sendMomentaryFunctionGroup2();
        if (old != this.f8Momentary) {
            notifyPropertyChangeListener(Throttle.F8Momentary, old, this.f8Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF9Momentary(boolean f9Momentary) {
        boolean old = this.f9Momentary;
        this.f9Momentary = f9Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f9Momentary) {
            notifyPropertyChangeListener(Throttle.F9Momentary, old, this.f9Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF10Momentary(boolean f10Momentary) {
        boolean old = this.f10Momentary;
        this.f10Momentary = f10Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f10Momentary) {
            notifyPropertyChangeListener(Throttle.F10Momentary, old, this.f10Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF11Momentary(boolean f11Momentary) {
        boolean old = this.f11Momentary;
        this.f11Momentary = f11Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f11Momentary) {
            notifyPropertyChangeListener(Throttle.F11Momentary, old, this.f11Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF12Momentary(boolean f12Momentary) {
        boolean old = this.f12Momentary;
        this.f12Momentary = f12Momentary;
        sendMomentaryFunctionGroup3();
        if (old != this.f12Momentary) {
            notifyPropertyChangeListener(Throttle.F12Momentary, old, this.f12Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF13Momentary(boolean f13Momentary) {
        boolean old = this.f13Momentary;
        this.f13Momentary = f13Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f13Momentary) {
            notifyPropertyChangeListener(Throttle.F13Momentary, old, this.f13Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF14Momentary(boolean f14Momentary) {
        boolean old = this.f14Momentary;
        this.f14Momentary = f14Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f14Momentary) {
            notifyPropertyChangeListener(Throttle.F14Momentary, old, this.f14Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF15Momentary(boolean f15Momentary) {
        boolean old = this.f15Momentary;
        this.f15Momentary = f15Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f15Momentary) {
            notifyPropertyChangeListener(Throttle.F15Momentary, old, this.f15Momentary);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setF16Momentary(boolean f16Momentary) {
        boolean old = this.f16Momentary;
        this.f16Momentary = f16Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f16Momentary) {
            notifyPropertyChangeListener(Throttle.F16Momentary, old, this.f16Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF17Momentary(boolean f17Momentary) {
        boolean old = this.f17Momentary;
        this.f17Momentary = f17Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f17Momentary) {
            notifyPropertyChangeListener(Throttle.F17Momentary, old, this.f17Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF18Momentary(boolean f18Momentary) {
        boolean old = this.f18Momentary;
        this.f18Momentary = f18Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f18Momentary) {
            notifyPropertyChangeListener(Throttle.F18Momentary, old, this.f18Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF19Momentary(boolean f19Momentary) {
        boolean old = this.f19Momentary;
        this.f19Momentary = f19Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f19Momentary) {
            notifyPropertyChangeListener(Throttle.F19Momentary, old, this.f19Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF20Momentary(boolean f20Momentary) {
        boolean old = this.f20Momentary;
        this.f20Momentary = f20Momentary;
        sendMomentaryFunctionGroup4();
        if (old != this.f20Momentary) {
            notifyPropertyChangeListener(Throttle.F20Momentary, old, this.f20Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF21Momentary(boolean f21Momentary) {
        boolean old = this.f21Momentary;
        this.f21Momentary = f21Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f21Momentary) {
            notifyPropertyChangeListener(Throttle.F21Momentary, old, this.f21Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF22Momentary(boolean f22Momentary) {
        boolean old = this.f22Momentary;
        this.f22Momentary = f22Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f22Momentary) {
            notifyPropertyChangeListener(Throttle.F22Momentary, old, this.f22Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF23Momentary(boolean f23Momentary) {
        boolean old = this.f23Momentary;
        this.f23Momentary = f23Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f23Momentary) {
            notifyPropertyChangeListener(Throttle.F23Momentary, old, this.f23Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF24Momentary(boolean f24Momentary) {
        boolean old = this.f24Momentary;
        this.f24Momentary = f24Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f24Momentary) {
            notifyPropertyChangeListener(Throttle.F24Momentary, old, this.f24Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF25Momentary(boolean f25Momentary) {
        boolean old = this.f25Momentary;
        this.f25Momentary = f25Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f25Momentary) {
            notifyPropertyChangeListener(Throttle.F25Momentary, old, this.f25Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF26Momentary(boolean f26Momentary) {
        boolean old = this.f26Momentary;
        this.f26Momentary = f26Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f26Momentary) {
            notifyPropertyChangeListener(Throttle.F26Momentary, old, this.f26Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF27Momentary(boolean f27Momentary) {
        boolean old = this.f27Momentary;
        this.f27Momentary = f27Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f27Momentary) {
            notifyPropertyChangeListener(Throttle.F27Momentary, old, this.f27Momentary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setF28Momentary(boolean f28Momentary) {
        boolean old = this.f28Momentary;
        this.f28Momentary = f28Momentary;
        sendMomentaryFunctionGroup5();
        if (old != this.f28Momentary) {
            notifyPropertyChangeListener(Throttle.F28Momentary, old, this.f28Momentary);
        }
    }

    /**
     * Send the message to set the momentary state of functions F0, F1, F2, F3,
     * F4.
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup1() {
    }

    /**
     * Send the message to set the momentary state of functions F5, F6, F7, F8.
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup2() {
    }

    /**
     * Send the message to set the Momentary state of functions F9, F10, F11,
     * F12
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup3() {
    }

    /**
     * Send the message to set the Momentary state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup4() {
    }

    /**
     * Send the message to set the Momentary state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup5() {
    }

    /**
     * Set the speed step value. Default should be 128 speed step mode in most cases.
     *
     * Specific implementations should override this function.
     *
     * @param Mode the current speed step mode
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        log.debug("Speed Step Mode Change from:{} to Mode:{}",this.speedStepMode,Mode);
        if (speedStepMode != Mode) {
            notifyPropertyChangeListener(SPEEDSTEPS, this.speedStepMode,
                    this.speedStepMode = Mode);
        }
    }

    @Override
    public SpeedStepMode getSpeedStepMode() {
        return speedStepMode;
    }

    long durationRunning = 0;
    long start;

    /**
     * Processes updated speed from subclasses.
     * Used to keep track of total operating time.
     */
    protected void record(float speed) {
        if (re == null) {
            return;
        }
        if (speed == 0) {
            stopClock();
        } else {
            startClock();
        }
    }

    protected void startClock() {
        if (start == 0) {
            start = System.currentTimeMillis();
        }
    }

    void stopClock() {
        if (start == 0) {
            return;
        }
        long stop = System.currentTimeMillis();
        //Set running duration in seconds
        durationRunning = durationRunning + ((stop - start) / 1000);
        start = 0;
    }

    protected void finishRecord() {
        if (re == null) {
            return;
        }
        stopClock();
        String currentDurationString = re.getAttribute("OperatingDuration");
        long currentDuration = 0;
        if (currentDurationString == null) {
            currentDurationString = "0";
            log.info("operating duration for {} starts as zero", getLocoAddress());
        }
        try {
            currentDuration = Long.parseLong(currentDurationString);
        } catch (NumberFormatException e) {
            log.warn("current stored duration is not a valid number \"" + currentDurationString + " \"");
        }
        currentDuration = currentDuration + durationRunning;
        re.putAttribute("OperatingDuration", "" + currentDuration);
        re.putAttribute("LastOperated", new StdDateFormat().format(new Date()));
        //Only store if the roster entry isn't open.
        if (!re.isOpen()) {
            re.store();
        } else {
            log.warn("Roster Entry {} running time not saved as entry is already open for editing",re.getId());
        }
        re = null;
    }

    BasicRosterEntry re = null;

    @Override
    public void setRosterEntry(BasicRosterEntry re) {
        this.re = re;
    }

    @Override
    public BasicRosterEntry getRosterEntry() {
        return re;
    }

    /**
     * Get an integer speed for the given raw speed value. This is a convenience
     * method that calls {@link #intSpeed(float, int) } with a maxStep of 127.
     *
     * @return an integer in the range 0-127
     */
    protected int intSpeed(float speed) {
        return this.intSpeed(speed, 127);
    }

    /**
     * Get an integer speed for the given raw speed value.
     *
     * @param speed the speed as a percentage of maximum possible speed.
     *              Negative values indicate a need for an emergency stop.
     * @param steps number of possible speeds. Values less than 2 will cause
     *              errors.
     * @return an integer in the range 0-steps
     */
    protected int intSpeed(float speed, int steps) {
        // test that speed is < 0 for emergency stop since calculation of
        // value returns 0 for some values of -1 < rawSpeed < 0
        if (speed < 0) {
            return 1; // emergency stop
        }
        // since Emergency Stop (estop) is speed 1, and a negative speed
        // is used for estop, subtract 1 from steps to avoid the estop
        // Use ceil() to prevent smaller positive values from being 0
        int value = (int) Math.ceil((steps - 1) * speed);
        if (value < 0) {
            // if we get here, something is wrong and needs to be reported.
            Exception ex = new Exception("Error calculating speed. Please send logs to the JMRI developers.");
            log.error(ex.getMessage(), ex);
            return 1;
        } else if (value >= steps) {
            return steps; // maximum possible speed
        } else if (value > 0) {
            return value + 1; // add 1 to the value to avoid the estop
        } else {
            return 0; // non-emergency stop
        }
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractThrottle.class);

}

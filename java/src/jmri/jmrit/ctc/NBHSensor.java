package jmri.jmrit.ctc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Reporter;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * This object attempts to "extend" (implements) Sensor functionality that
 * is needed by the CTC system.
 *
 * Goals:
 * 1.) Catches and thereby prevents exceptions from propagating upward.  No need
 *     for this everywhere in your code:
 *     try { sensor.getBean().setKnownState(Sensor.ACTIVE); } catch (JmriException ex) {}
 *     We ASSUME here that you are ALWAYS passing a valid "newState".  If not,
 *     then this call is effectively a no-op (do nothing).
 * 2.) Use ONLY named beans internally for proper JMRI support of renaming objects.
 * 3.) Support renaming of this object fully (if possible via normal JMRI methods). (FUTURE CODE)
 * 4.) Prevents "null" access to improperly constructed internal objects.  For example:
 *     If the caller passes invalid parameter(s) to the constructor(s), then this object's
 *     internal Sensor will be set to null by the constructor(s).  If the USER then
 *     attempts to use the CTC panel and the underlying code winds up calling method(s)
 *     in here that reference that null sensor, the JMRI system would crash.
 * 5.) If the internal Sensor is null and you call a function that returns a value,
 *     that function will return a "sane" value.  See the constants below for
 *     general return values in this situation and individual functions for specific info.
 * 6.) Support for required sensors.  If the sensor name doesn't exist or is invalid in ANY way,
 *     then it is considered an error, and this object logs it as such.  We do
 *     NOT create the JMRI object automatically in this situation.
 * 7.) Support for optional sensors.  In my system, a sensor may be optional.
 *     If specified, it MUST be valid and exist in the system already.
 * 8.) In each of the two above situations, ANY error situation will leave
 *     this object properly protected.
 * 9.) My CTC system that uses this object can "mindlessly" call methods on this
 *     object at any time, and return "sane" values in ANY error situation and
 *     rely on these values being consistent.  This prevents the need of calling
 *     routines to CONSTANTLY check the internal status of this object, and can
 *     rely on those "sane" return values.  I'm a lazy programmer, and want to
 *     prevent the following at the call site:
 *     int blah;
 *     if (NBHSensor.valid()) blah = NBHSensor.getKnownState();
 *     or
 *     try { blah = NBHSensor.getKnownState()} catch() {}
 *     it becomes just:
 *     blah = NBHSensor.getKnownState();
 *     You may (and I do) have specific circumstances whereby you need to know
 *     the internal state of this object is valid or not, and act on
 *     it differently than the default sane values.  There is a function "valid()"
 *     for that situation.
 */

// Prefix NBH = Named Bean Handler....

public class NBHSensor implements Sensor {
//  Special case sane return values:
    public static final int DEFAULT_SENSOR_STATE_RV = Sensor.INACTIVE;
//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.
//  Functions that don't return any of the above have specific implementations.  Ex: PropertyChangeListener[] or ArrayList<>

    private static final NamedBeanHandleManager NAMED_BEAN_HANDLE_MANAGER = InstanceManager.getDefault(NamedBeanHandleManager.class);

//  The "thing" we're protecting:
    private final NamedBeanHandle<Sensor> _mNamedBeanHandleSensor;
    public Sensor getBean() {
        if (valid()) return _mNamedBeanHandleSensor.getBean();
        return null;
    }
    public boolean matchSensor(Sensor sensor) {
        if (valid()) return _mNamedBeanHandleSensor.getBean() == sensor;
        return false;
    }

    public NBHSensor(String module, String userIdentifier, String parameter, String sensor, boolean optional) {
        Sensor tempSensor = optional ? getSafeOptionalJMRISensor(module, userIdentifier, parameter, sensor) : getSafeExistingJMRISensor(module, userIdentifier, parameter, sensor);
        if (tempSensor != null) {
            _mNamedBeanHandleSensor = NAMED_BEAN_HANDLE_MANAGER.getNamedBeanHandle(sensor, tempSensor);
        } else {
            _mNamedBeanHandleSensor = null;
        }
    }
//  Use when something else has the thing we help with:
    public NBHSensor(NamedBeanHandle<Sensor> namedBeanHandleSensor) {
        _mNamedBeanHandleSensor = namedBeanHandleSensor;
    }
    public boolean valid() { return _mNamedBeanHandleSensor != null; }  // For those that want to know the internal state.

    private static Sensor getSafeExistingJMRISensor(String module, String userIdentifier, String parameter, String sensor) {
        try { return getExistingJMRISensor(module, userIdentifier, parameter, sensor); } catch (CTCException e) { e.logError(); }
        return null;
    }
    private static Sensor getSafeOptionalJMRISensor(String module, String userIdentifier, String parameter, String sensor) {
        try { return getOptionalJMRISensor(module, userIdentifier, parameter, sensor); } catch (CTCException e) { e.logError(); }
        return null;
    }
//  sensor is NOT optional and cannot be null.  Raises Exception in ALL error cases.
    private static Sensor getExistingJMRISensor(String module, String userIdentifier, String parameter, String sensor) throws CTCException {
        if (!ProjectsCommonSubs.isNullOrEmptyString(sensor)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            Sensor returnValue = InstanceManager.getDefault(SensorManager.class).getSensor(sensor.trim());
            if (returnValue == null) { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHSensorDoesNotExist") + " " + sensor); }  // NOI18N
            return returnValue;
        } else { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHSensorRequiredSensorMissing")); }  // NOI18N
    }
//  sensor is optional, but must exist if given.  Raises Exception in ALL error cases.
    private static Sensor getOptionalJMRISensor(String module, String userIdentifier, String parameter, String sensor) throws CTCException {
        if (!ProjectsCommonSubs.isNullOrEmptyString(sensor)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            Sensor returnValue = InstanceManager.getDefault(SensorManager.class).getSensor(sensor.trim());
            if (returnValue == null) { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHSensorDoesNotExist") + " " + sensor); }  // NOI18N
            return returnValue;
        } else { return null; }
    }

    @Override
    public int getKnownState() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_SENSOR_STATE_RV;
        return _mNamedBeanHandleSensor.getBean().getKnownState();
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not do anything if it fails.")
    @Override
    public void setKnownState(int newState) {
        if (_mNamedBeanHandleSensor == null) return;
        try { _mNamedBeanHandleSensor.getBean().setKnownState(newState); } catch (JmriException ex) {}
    }

    @Override
    public void requestUpdateFromLayout() {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().requestUpdateFromLayout();
    }

    @Override
    public void setInverted(boolean inverted) {
        if (_mNamedBeanHandleSensor == null) return;   // Do nothing.
        _mNamedBeanHandleSensor.getBean().setInverted(inverted);
    }

    @Override
    public boolean getInverted() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSensor.getBean().getInverted();
    }

    @Override
    public boolean canInvert() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSensor.getBean().canInvert();
    }

    @Override
    public void dispose() {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().dispose();
    }

    @Override
    public int getRawState() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_SENSOR_STATE_RV;
        return _mNamedBeanHandleSensor.getBean().getRawState();
    }

    @Override
    public void setSensorDebounceGoingActiveTimer(long timer) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setSensorDebounceGoingActiveTimer(timer);
    }

    @Override
    public long getSensorDebounceGoingActiveTimer() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_LONG_RV;
        return _mNamedBeanHandleSensor.getBean().getSensorDebounceGoingActiveTimer();
    }

    @Override
    public void setSensorDebounceGoingInActiveTimer(long timer) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setSensorDebounceGoingInActiveTimer(timer);
    }

    @Override
    public long getSensorDebounceGoingInActiveTimer() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_LONG_RV;
        return _mNamedBeanHandleSensor.getBean().getSensorDebounceGoingInActiveTimer();
    }

    @Override
    public void setUseDefaultTimerSettings(boolean flag) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setUseDefaultTimerSettings(flag);
    }

    @Override
    public boolean getUseDefaultTimerSettings() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSensor.getBean().getUseDefaultTimerSettings();
    }

    @Override
    @Deprecated // 4.9.2
    public void useDefaultTimerSettings(boolean flag) {
        throw new UnsupportedOperationException("Deprecated since JMRI 4.9.2"); // NOI18N
    }

    @Override
    @Deprecated // 4.9.2
    public boolean useDefaultTimerSettings() {
        throw new UnsupportedOperationException("Deprecated since JMRI 4.9.2"); // NOI18N
    }

    @Override
    public void setReporter(Reporter re) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setReporter(re);
    }

    @Override
    public Reporter getReporter() {
        if (_mNamedBeanHandleSensor == null) return (Reporter)DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleSensor.getBean().getReporter();
    }

    @Override
    public void setPullResistance(PullResistance r) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setPullResistance(r);
    }

    @Override
    public PullResistance getPullResistance() {
        if (_mNamedBeanHandleSensor == null) return (PullResistance)DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleSensor.getBean().getPullResistance();
    }

    @Override
    public String getUserName() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getUserName();
    }

    @Override
    public void setUserName(String s) {
        if (_mNamedBeanHandleSensor == null) return;
        try { _mNamedBeanHandleSensor.getBean().setUserName(s); } catch (BadUserNameException e) {}
    }

    @Override
    public String getSystemName() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getSystemName();
    }

    @Override
    public String getDisplayName() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getDisplayName();
    }

    @Override
    public String getFullyFormattedDisplayName() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getFullyFormattedDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().addPropertyChangeListener(l, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().removePropertyChangeListener(l);
    }

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().updateListenerRef(l, newName);
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "I don't use this function, let it not do anything if it fails.")
    @Override
    public void vetoableChange(PropertyChangeEvent evt) {
        if (_mNamedBeanHandleSensor == null) return;
        try { _mNamedBeanHandleSensor.getBean().vetoableChange(evt); } catch (PropertyVetoException e) {}
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        if (_mNamedBeanHandleSensor == null) return new ArrayList<>();
        return _mNamedBeanHandleSensor.getBean().getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSensor.getBean().getNumPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        if (_mNamedBeanHandleSensor == null) return new PropertyChangeListener[0];
        return _mNamedBeanHandleSensor.getBean().getPropertyChangeListenersByReference(name);
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not do anything if it fails.")
    @Override
    public void setState(int s) {
        if (_mNamedBeanHandleSensor == null) return;
        try { _mNamedBeanHandleSensor.getBean().setState(s); } catch (JmriException e) {}
    }

    @Override
    public int getState() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_SENSOR_STATE_RV;
        return _mNamedBeanHandleSensor.getBean().getState();
    }

    @Override
    public String describeState(int state) {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().describeState(state);
    }

    @Override
    public String getComment() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getComment();
    }

    @Override
    public void setComment(String comment) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleSensor.getBean().getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        if (_mNamedBeanHandleSensor == null) return Collections.emptySet();
        return _mNamedBeanHandleSensor.getBean().getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSensor.getBean().getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_INT_RV; // What should I return?
        return _mNamedBeanHandleSensor.getBean().compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name,
            String listenerRef) {
        _mNamedBeanHandleSensor.getBean().addPropertyChangeListener(propertyName, listener, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _mNamedBeanHandleSensor.getBean().addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return _mNamedBeanHandleSensor.getBean().getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _mNamedBeanHandleSensor.getBean().getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _mNamedBeanHandleSensor.getBean().removePropertyChangeListener(propertyName, listener);
    }
}

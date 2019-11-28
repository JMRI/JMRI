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
 * This object attempts to "extend" (implements) Sensor functionality that
 * is needed by the CTC system.
 * <p>
 * Goals:
 * <ol>
 * <li> Catches and thereby prevents exceptions from propagating upward.  No need
 *     for this everywhere in your code:
 *     try { sensor.getBean().setKnownState(Sensor.ACTIVE); } catch (JmriException ex) {}
 *     We ASSUME here that you are ALWAYS passing a valid "newState".  If not,
 *     then this call is effectively a no-op (do nothing).
 * <li> Use ONLY named beans internally for proper JMRI support of renaming objects.
 * <li> Support renaming of this object fully (if possible via normal JMRI methods). (FUTURE CODE)
 * <li> Prevents "null" access to improperly constructed internal objects.  For example:
 *     If the caller passes invalid parameter(s) to the constructor(s), then this object's
 *     internal Sensor will be set to null by the constructor(s).  If the USER then
 *     attempts to use the CTC panel and the underlying code winds up calling method(s)
 *     in here that reference that null sensor, the JMRI system would crash.
 * <li> If the internal Sensor is null and you call a function that returns a value,
 *     that function will return a "sane" value.  See the constants below for
 *     general return values in this situation and individual functions for specific info.
 * <li> Support for required sensors.  If the sensor name doesn't exist or is invalid in ANY way,
 *     then it is considered an error, and this object logs it as such.  We do
 *     NOT create the JMRI object automatically in this situation.
 * <li> Support for optional sensors.  In my system, a sensor may be optional.
 *     If specified, it MUST be valid and exist in the system already.
 * <li> In each of the two above situations, ANY error situation will leave
 *     this object properly protected.
 * <li> My CTC system that uses this object can "mindlessly" call methods on this
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
 * </ol>
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

// Prefix NBH = Named Bean Handler....

public class NBHSensor {
//  Special case sane return values:
    public static final int DEFAULT_SENSOR_STATE_RV = Sensor.INACTIVE;
//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.
//  Functions that don't return any of the above have specific implementations.  Ex: PropertyChangeListener[] or ArrayList<>

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
            _mNamedBeanHandleSensor = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor, tempSensor);
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
            Sensor returnValue = InstanceManager.getDefault(SensorManager.class).getSensor(sensor);
            if (returnValue == null) { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHSensorDoesNotExist") + " " + sensor); }  // NOI18N
            return returnValue;
        } else { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHSensorRequiredSensorMissing")); }  // NOI18N
    }

//  sensor is optional, but must exist if given.  Raises Exception in ALL error cases.
    private static Sensor getOptionalJMRISensor(String module, String userIdentifier, String parameter, String sensor) throws CTCException {
        if (!ProjectsCommonSubs.isNullOrEmptyString(sensor)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            Sensor returnValue = InstanceManager.getDefault(SensorManager.class).getSensor(sensor);
            if (returnValue == null) { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHSensorDoesNotExist") + " " + sensor); }  // NOI18N
            return returnValue;
        } else { return null; }
    }

    public int getKnownState() {
        if (_mNamedBeanHandleSensor == null) return DEFAULT_SENSOR_STATE_RV;
        return _mNamedBeanHandleSensor.getBean().getKnownState();
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not do anything if it fails.")
    public void setKnownState(int newState) {
        if (_mNamedBeanHandleSensor == null) return;
        try { _mNamedBeanHandleSensor.getBean().setKnownState(newState); } catch (JmriException ex) {}
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSensor == null) return;
        _mNamedBeanHandleSensor.getBean().removePropertyChangeListener(l);
    }
}

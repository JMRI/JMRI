package jmri.jmrit.ctc;

import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalAppearanceMap;
import jmri.SignalMast;
import jmri.SignalHead;
import jmri.jmrit.ctc.ctcserialdata.*;

/**
 * Provide access to both signal masts and signal heads for the CTC system.
 * <p>
 * This class combines the NBHAbstractSignalCommon, NBHSignalMast and NBHSignalHead
 * classes.  OtherData _mSignalSystemType determines whether masts or heads are enabled.
 * @author Dave Sand Copyright (C) 2020
 */
public class NBHSignal {

//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

//  The "things" we're protecting:
    private final NamedBeanHandle<SignalMast> _mNamedBeanHandleSignalMast;
    private final NamedBeanHandle<SignalHead> _mNamedBeanHandleSignalHead;

    private final boolean isSignalMast;   // True for signal mast, false for signal head
    private final String _mDangerAppearance;  //  The string to determine "Is the Signal all Red":

    /**
     * Create the named bean handle for either a signal mast or signal head.
     * @param signal The signal name.
     */
    public NBHSignal(String signal) {
        isSignalMast = setSignalType();
        if (!ProjectsCommonSubs.isNullOrEmptyString(signal)) {
            if (isSignalMast) {
                // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
                SignalMast signalMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signal);
                if (signalMast != null) {
                    _mNamedBeanHandleSignalMast = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(signal, signalMast);
                    String temp = getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                    if (temp == null) temp = "Stop"; // NOI18N // Safety
                    _mDangerAppearance = temp;
                    _mNamedBeanHandleSignalHead = null;
                    if (valid()) InstanceManager.getDefault(CtcManager.class).putNBHSignal(signal, this);
                    return;
                }
            } else {
                // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
                SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signal);
                if (signalHead != null) {
                    _mNamedBeanHandleSignalHead = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(signal, signalHead);
                    _mDangerAppearance = "Stop";            // NOI18N // Never used, just required for "final"
                    _mNamedBeanHandleSignalMast = null;
                    if (valid()) InstanceManager.getDefault(CtcManager.class).putNBHSignal(signal, this);
                    return;
                }
            }
        }
        _mDangerAppearance = "Stop";            // NOI18N // Never used, just required for "final"
        _mNamedBeanHandleSignalMast = null;
        _mNamedBeanHandleSignalHead = null;
    }

    /**
     * Set signal type using {@link OtherData#_mSignalSystemType}.
     * @return true for mast, false if head.
     */
    private boolean setSignalType() {
        OtherData otherData = InstanceManager.getDefault(CtcManager.class).getOtherData();
        return otherData._mSignalSystemType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALMAST ? true : false;

    }

    public boolean valid() {
        return _mNamedBeanHandleSignalMast != null || _mNamedBeanHandleSignalHead != null;
    }  // For those that want to know the internal state.

    public Object getBean() {
        if (!valid()) return null;
        return isSignalMast ? _mNamedBeanHandleSignalMast.getBean() : _mNamedBeanHandleSignalHead.getBean();
    }

    public Object getBeanHandle() {
        if (!valid()) return null;
        return isSignalMast ? _mNamedBeanHandleSignalMast : _mNamedBeanHandleSignalHead;
    }

    /**
     * @return The signal's handle name.
     */
    public String getHandleName() {
        if (!valid()) return null;
        return isSignalMast ? _mNamedBeanHandleSignalMast.getName() : _mNamedBeanHandleSignalHead.getName();
    }

    public String getDisplayName() {
        if (isSignalMast) {
            if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
            return _mNamedBeanHandleSignalMast.getBean().getDisplayName();
        } else {
            if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
            return _mNamedBeanHandleSignalHead.getBean().getDisplayName();
        }
    }

    public boolean isDanger() {
        if (getHeld()) return true;     // Safety.  Problem in signal head, maybe same problem here?
        return isSignalMast ? getAspect().equals(_mDangerAppearance) : SignalHead.RED == getAppearance();
    }

    public void setCTCHeld(boolean held) {
        setHeld(held);
    }

    public boolean getHeld() {
        if (isSignalMast) {
            if (_mNamedBeanHandleSignalMast == null) return DEFAULT_BOOLEAN_RV;
            return _mNamedBeanHandleSignalMast.getBean().getHeld();
        } else {
            if (_mNamedBeanHandleSignalHead == null) return DEFAULT_BOOLEAN_RV;
            return _mNamedBeanHandleSignalHead.getBean().getHeld();
        }
    }

    public void setHeld(boolean newHeld) {
        if (isSignalMast) {
            if (_mNamedBeanHandleSignalMast == null) return;
            _mNamedBeanHandleSignalMast.getBean().setHeld(newHeld);
            if (newHeld) {
                _mNamedBeanHandleSignalMast.getBean().setPermissiveSmlDisabled(true);
            }
        } else {
            if (_mNamedBeanHandleSignalHead == null) return;
            _mNamedBeanHandleSignalHead.getBean().setHeld(newHeld);
        }
    }

    public void allowPermissiveSML() {
        _mNamedBeanHandleSignalMast.getBean().setPermissiveSmlDisabled(false);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (isSignalMast) {
            if (_mNamedBeanHandleSignalMast == null) return;
            _mNamedBeanHandleSignalMast.getBean().addPropertyChangeListener(l);
        } else {
            if (_mNamedBeanHandleSignalHead == null) return;
            _mNamedBeanHandleSignalHead.getBean().addPropertyChangeListener(l);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (isSignalMast) {
            if (_mNamedBeanHandleSignalMast == null) return;
            _mNamedBeanHandleSignalMast.getBean().removePropertyChangeListener(l);
        } else {
            if (_mNamedBeanHandleSignalHead == null) return;
            _mNamedBeanHandleSignalHead.getBean().removePropertyChangeListener(l);
        }
    }

    /**
     *
     * Function to insure that a non null aspect value is always returned to the caller.
     *
     * Background (regarding the value contained in "_mDangerAppearance"):
     * In this objects constructor, "_mDangerAppearance" is set to getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER).
     * If "...getSpecificAppearance..." returns "null" (undocumented in JMRI documents as of 9/18/2019),
     * "_mDangerAppearance" is set to "Stop" for safety.
     * So "_mDangerAppearance" will NEVER be null for use as follows:
     *
     * SignalMast.getAspect() can return "null" (undocumented in JMRI documents as of 9/18/2019) if (for instance) the signal has no
     * rules (i.e. no "Discover" done yet, or the signal is shown on the screen as a big red "X").
     * In this case, we return "_mDangerAppearance".
     *
     * @return  Return a guaranteed non null aspect name.
     */
    public String getAspect() {
        if (!isSignalMast) log.info("NBHSignal: getAspect called by signal head", new Exception("traceback"));
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        String returnAspect = _mNamedBeanHandleSignalMast.getBean().getAspect();
        if (returnAspect == null) return _mDangerAppearance;    // Safety
        return returnAspect;
    }

    public SignalAppearanceMap getAppearanceMap() {
        if (!isSignalMast) log.info("NBHSignal: getAppearanceMap called by signal head", new Exception("traceback"));
        if (_mNamedBeanHandleSignalMast == null) return null;
        return _mNamedBeanHandleSignalMast.getBean().getAppearanceMap();
    }

    public int getAppearance() {
        if (isSignalMast) log.info("NBHSignal: getAppearance called by signal mast", new Exception("traceback"));
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalHead.getBean().getAppearance();
    }

    public void setAppearance(int newAppearance) {
        if (isSignalMast) log.info("NBHSignal: setAppearance called by signal mast", new Exception("traceback"));
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setAppearance(newAppearance);
    }

    /**
     * Get an array of appearance indexes valid for the mast type.
     *
     * @return array of appearance state values available on this mast type
     */
    public int[] getValidStates() {
        if (isSignalMast) log.info("NBHSignal: getValidStates called by signal mast", new Exception("traceback"));
        if (_mNamedBeanHandleSignalHead == null) return new int[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStates();
    }

    /**
     * Get an array of non-localized appearance keys valid for the mast type.
     * For GUI application consider using (capitalized) {@link #getValidStateNames()}
     *
     * @return array of translated appearance names available on this mast type
     */
    public String[] getValidStateKeys() {
        if (isSignalMast) log.info("NBHSignal: getValidStateKeys called by signal mast", new Exception("traceback"));
        if (_mNamedBeanHandleSignalHead == null) return new String[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStateKeys();
    }

    /**
     * Get an array of localized appearance descriptions valid for the mast type.
     * For persistance and comparison consider using {@link #getValidStateKeys()}
     *
     * @return array of translated appearance names
     */
    public String[] getValidStateNames() {
        if (isSignalMast) log.info("NBHSignal: getValidStateNames called by signal mast", new Exception("traceback"));
        if (_mNamedBeanHandleSignalHead == null) return new String[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStateNames();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NBHSignal.class);
}


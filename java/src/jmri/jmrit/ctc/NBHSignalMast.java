package jmri.jmrit.ctc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalAppearanceMap;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.SignalSystem;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class NBHSignalMast extends NBHAbstractSignalCommon {

//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

//  The "thing" we're protecting:
    private final NamedBeanHandle<SignalMast> _mNamedBeanHandleSignalMast;

//  The string to determine "Is the Signal Mast all Red":
    private final String _mDangerAppearance;

    protected NBHSignalMast(String signal) {
        if (!ProjectsCommonSubs.isNullOrEmptyString(signal)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            SignalMast signalMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signal);
            if (signalMast != null) {
                _mNamedBeanHandleSignalMast = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(signal, signalMast);
                String temp = getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                if (temp == null) temp = "Stop"; // Safety
                _mDangerAppearance = temp;
                return;
            }
        }
        _mDangerAppearance = "Stop";            // Never used, just required for "final"
        _mNamedBeanHandleSignalMast = null;
    }

    public boolean valid() { return _mNamedBeanHandleSignalMast != null; }  // For those that want to know the internal state.
    @Override
    public Object getBean() {
        if (!valid()) return null;
        return _mNamedBeanHandleSignalMast.getBean();
    }

    public void setCTCHeld(boolean held) { setHeld(held); }

    @Override
    public boolean isDanger() {
        if (getHeld()) return true;     // Safety.  Problem in signal head, maybe same problem here?
        return getAspect().equals(_mDangerAppearance);
    }

//  Fake for SignalHead support:
    @Override
    public int[] getValidStates() { return new int[0]; }

    @Override
    public String[] getValidStateKeys() { return new String[0]; }

    @Override
    public String[] getValidStateNames() { return new String[0]; }

    @Override
    public void setAppearance(int newAppearance) {}

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
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        String returnAspect = _mNamedBeanHandleSignalMast.getBean().getAspect();
        if (returnAspect == null) return _mDangerAppearance;    // Safety
        return returnAspect;
    }

    public SignalAppearanceMap getAppearanceMap() {
        if (_mNamedBeanHandleSignalMast == null) return null;
        return _mNamedBeanHandleSignalMast.getBean().getAppearanceMap();
    }

    @Override
    public boolean getHeld() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalMast.getBean().getHeld();
    }

    @Override
    public void setHeld(boolean newHeld) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setHeld(newHeld);
    }

    @Override
    public String getDisplayName() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().removePropertyChangeListener(l);
    }

}

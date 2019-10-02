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
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class NBHSignalHead extends NBHAbstractSignalCommon {

//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

//  The "thing" we're protecting:
    private final NamedBeanHandle<SignalHead> _mNamedBeanHandleSignalHead;

    protected NBHSignalHead(String signal) {
        if (!ProjectsCommonSubs.isNullOrEmptyString(signal)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signal);
            if (signalHead != null) {
                _mNamedBeanHandleSignalHead = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(signal, signalHead);
                return;
            }
        }
        _mNamedBeanHandleSignalHead = null;
    }
    public boolean valid() { return _mNamedBeanHandleSignalHead != null; }  // For those that want to know the internal state.

    @Override
    public Object getBean() {
        if (!valid()) return null;
        return _mNamedBeanHandleSignalHead.getBean();
    }

    public void setCTCHeld(boolean held) { setHeld(held); }
//  JMRI BUG: If "Held" attribute is True, then appearance is NOT the constant HELD!
    @Override
    public boolean isDanger() {
        if (getHeld()) return true;
        return SignalHead.RED == getAppearance();
    }

    public int getAppearance() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalHead.getBean().getAppearance();
    }

    @Override
    public void setAppearance(int newAppearance) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setAppearance(newAppearance);
    }

    @Override
    public boolean getHeld() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalHead.getBean().getHeld();
    }

    @Override
    public void setHeld(boolean newHeld) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setHeld(newHeld);
    }

    /**
     * Get an array of appearance indexes valid for the mast type.
     *
     * @return array of appearance state values available on this mast type
     */
    @Override
    public int[] getValidStates() {
        if (_mNamedBeanHandleSignalHead == null) return new int[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStates();
    }

    /**
     * Get an array of non-localized appearance keys valid for the mast type.
     * For GUI application consider using (capitalized) {@link #getValidStateNames()}
     *
     * @return array of translated appearance names available on this mast type
     */
    @Override
    public String[] getValidStateKeys() {
        if (_mNamedBeanHandleSignalHead == null) return new String[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStateKeys();
    }

    /**
     * Get an array of localized appearance descriptions valid for the mast type.
     * For persistance and comparison consider using {@link #getValidStateKeys()}
     *
     * @return array of translated appearance names
     */
    @Override
    public String[] getValidStateNames() {
        if (_mNamedBeanHandleSignalHead == null) return new String[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStateNames();
    }

    @Override
    public String getDisplayName() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().removePropertyChangeListener(l);
    }
}

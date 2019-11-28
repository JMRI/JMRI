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
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperation;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * This object additionally supports "inverted feedback", so that when someone
 * calls "getKnownState", (typically my own code in this CTC project) we return
 * the "adjusted" value based upon "inverted feedback".
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

// Prefix NBH = Named Bean Handler....

public class NBHTurnout {

//  Special case sane return values:
    public static final int DEFAULT_TURNOUT_STATE_RV = Turnout.CLOSED;  // A valid state, just "B.S.".
//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

//  The "thing" we're protecting:
    private final NamedBeanHandle<Turnout> _mNamedBeanHandleTurnout;
    private final boolean _mFeedbackDifferent;

    public NBHTurnout(String module, String userIdentifier, String parameter, String turnout, boolean FeedbackDifferent) {
        Turnout tempTurnout = getSafeExistingJMRITurnout(module, userIdentifier, parameter, turnout);
        if (tempTurnout != null) {
            _mNamedBeanHandleTurnout = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout, tempTurnout);
        } else {
            _mNamedBeanHandleTurnout = null;
        }
        _mFeedbackDifferent = FeedbackDifferent;
    }
    public boolean valid() { return _mNamedBeanHandleTurnout != null; }  // For those that want to know the internal state.

    public Turnout getBean() {
        if (valid()) return _mNamedBeanHandleTurnout.getBean();
        return null;
    }

    private static Turnout getSafeExistingJMRITurnout(String module, String userIdentifier, String parameter, String turnout) {
        try { return getExistingJMRITurnout(module, userIdentifier, parameter, turnout); } catch (CTCException e) { e.logError(); }
        return null;
    }
//  turnout is NOT optional and cannot be null.  Raises Exception in ALL error cases.
    static private Turnout getExistingJMRITurnout(String module, String userIdentifier, String parameter, String turnout) throws CTCException {
        if (!ProjectsCommonSubs.isNullOrEmptyString(turnout)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            Turnout returnValue = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnout);
            if (returnValue == null) { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHTurnoutDoesNotExist") + " " + turnout); }    // NOI18N
            return returnValue;
        } else { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("RequiredTurnoutMissing")); }    // NOI18N
    }

    public int getKnownState() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_TURNOUT_STATE_RV;
        int knownState = _mNamedBeanHandleTurnout.getBean().getKnownState();
        if (!_mFeedbackDifferent) { // Normal:
            return knownState;
        } else { // Reversed:
            return knownState == Turnout.CLOSED ? Turnout.THROWN : Turnout.CLOSED;
        }
    }

    public void setCommandedState(int s) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setCommandedState(s);
    }

    public int getFeedbackMode() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getFeedbackMode();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().removePropertyChangeListener(l);
    }
}

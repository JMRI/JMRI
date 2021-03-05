package jmri.jmrit.ctc;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * This object additionally supports "inverted feedback", so that when someone
 * calls "getKnownState", (typically my own code in this CTC project) we return
 * the "adjusted" value based upon "inverted feedback".
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 * Prefix NBH = Named Bean Handler....
 */

public class NBHTurnout {

//  Special case sane return values:
    public static final int DEFAULT_TURNOUT_STATE_RV = Turnout.CLOSED;  // A valid state, just "B.S.".
//  Standard sane return values for the types indicated:
//  public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
//  public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
//  public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
//  public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
//  public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

//  The "thing" we're protecting:
    private NamedBeanHandle<Turnout> _mNamedBeanHandleTurnout;
    private final String _mUserIdentifier;
    private final String _mParameter;
    private final boolean _mFeedbackDifferent;
    private final ArrayList<PropertyChangeListener> _mArrayListOfPropertyChangeListeners = new ArrayList<>();

    public NBHTurnout(String module, String userIdentifier, String parameter, String turnout, boolean FeedbackDifferent) {
        _mUserIdentifier = userIdentifier;
        _mParameter = parameter;
        _mFeedbackDifferent = FeedbackDifferent;
        Turnout tempTurnout = getSafeExistingJMRITurnout(module, _mUserIdentifier, _mParameter, turnout);
        if (tempTurnout != null) {
            _mNamedBeanHandleTurnout = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout, tempTurnout);
        } else {
            _mNamedBeanHandleTurnout = null;
        }
        if (valid()) InstanceManager.getDefault(CtcManager.class).putNBHTurnout(turnout, this);
    }

// Special constructor to create a NBHTurnout with a null NamedBeanHandle.  Used to initialize turnout fields.
    public NBHTurnout(String module, String userIdentifier, String parameter) {
        _mUserIdentifier = userIdentifier;
        _mParameter = parameter;
        _mFeedbackDifferent = false;
        _mNamedBeanHandleTurnout = null;
        if (valid()) InstanceManager.getDefault(CtcManager.class).putNBHTurnout("dummy", this);
    }

    public boolean valid() { return _mNamedBeanHandleTurnout != null; }  // For those that want to know the internal state.

    public Turnout getBean() {
        if (valid()) return _mNamedBeanHandleTurnout.getBean();
        return null;
    }

    public NamedBeanHandle getBeanHandle() {
        if (valid()) return _mNamedBeanHandleTurnout;
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

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().addPropertyChangeListener(propertyChangeListener);
        _mArrayListOfPropertyChangeListeners.add(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().removePropertyChangeListener(propertyChangeListener);
        _mArrayListOfPropertyChangeListeners.remove(propertyChangeListener);
    }

    /**
     * @return The turnout's handle name.
     */
    public String getHandleName() {
        return valid() ? _mNamedBeanHandleTurnout.getName() : "";
    }

    /**
     * For Unit testing only.
     * @return Returns the present number of property change listeners registered with us so far.
     */
    public int testingGetCountOfPropertyChangeListenersRegistered() {
        return _mArrayListOfPropertyChangeListeners.size();
    }

}

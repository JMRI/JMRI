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
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * Prefix NBH = Named Bean Handler....
 *
 * This object additionally supports "inverted feedback", so that when someone
 * calls "getKnownState", (typically my own code in this CTC project) we return
 * the "adjusted" value based upon "inverted feedback".
 */

public class NBHTurnout implements Turnout {

//  Special case sane return values:
    public static final int DEFAULT_TURNOUT_STATE_RV = Turnout.CLOSED;  // A valid state, just "B.S.".
//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

    private static final NamedBeanHandleManager NAMED_BEAN_HANDLE_MANAGER = InstanceManager.getDefault(NamedBeanHandleManager.class);

//  The "thing" we're protecting:
    private final NamedBeanHandle<Turnout> _mNamedBeanHandleTurnout;
    private final boolean _mFeedbackDifferent;

    public NBHTurnout(String module, String userIdentifier, String parameter, String turnout, boolean FeedbackDifferent) {
        Turnout tempTurnout = getSafeExistingJMRITurnout(module, userIdentifier, parameter, turnout);
        if (tempTurnout != null) {
            _mNamedBeanHandleTurnout = NAMED_BEAN_HANDLE_MANAGER.getNamedBeanHandle(turnout, tempTurnout);
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
            Turnout returnValue = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnout.trim());
            if (returnValue == null) { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHTurnoutDoesNotExist") + " " + turnout); }    // NOI18N
            return returnValue;
        } else { throw new CTCException(module, userIdentifier, parameter, Bundle.getMessage("RequiredTurnoutMissing")); }    // NOI18N
    }

    @Override
    public int getKnownState() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_TURNOUT_STATE_RV;
        int knownState = _mNamedBeanHandleTurnout.getBean().getKnownState();
        if (!_mFeedbackDifferent) { // Normal:
            return knownState;
        } else { // Reversed:
            return knownState == Turnout.CLOSED ? Turnout.THROWN : Turnout.CLOSED;
        }
    }

    @Override
    public void setCommandedState(int s) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setCommandedState(s);
    }

    @Override
    public int getCommandedState() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_TURNOUT_STATE_RV;
        return _mNamedBeanHandleTurnout.getBean().getCommandedState();
    }

    @Override
    public boolean isConsistentState() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().isConsistentState();
    }

    @Override
    public int getValidFeedbackTypes() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getValidFeedbackTypes();
    }

    @Override
    public String[] getValidFeedbackNames() {
        if (_mNamedBeanHandleTurnout == null) return new String[0];
        return _mNamedBeanHandleTurnout.getBean().getValidFeedbackNames();
    }

    @Override
    public void setFeedbackMode(String mode) throws IllegalArgumentException {
        if (_mNamedBeanHandleTurnout == null) return;
        try { _mNamedBeanHandleTurnout.getBean().setFeedbackMode(mode); } catch (IllegalArgumentException ex){}
    }

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        if (_mNamedBeanHandleTurnout == null) return;
        try { _mNamedBeanHandleTurnout.getBean().setFeedbackMode(mode); } catch (IllegalArgumentException ex){}
    }

    @Override
    public String getFeedbackModeName() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getFeedbackModeName();
    }

    @Override
    public int getFeedbackMode() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getFeedbackMode();
    }

    @Override
    public void requestUpdateFromLayout() {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().requestUpdateFromLayout();
    }

    @Override
    public boolean getInhibitOperation() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().getInhibitOperation();
    }

    @Override
    public void setInhibitOperation(boolean io) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setInhibitOperation(io);
    }

    @Override
    public TurnoutOperation getTurnoutOperation() {
        if (_mNamedBeanHandleTurnout == null) return (TurnoutOperation)DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleTurnout.getBean().getTurnoutOperation();
    }

    @Override
    public void setTurnoutOperation(TurnoutOperation toper) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setTurnoutOperation(toper);
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not do anything if it fails.")
    @Override
    public void provideFirstFeedbackSensor(String pName) throws JmriException {
        if (_mNamedBeanHandleTurnout == null) return;
        try { _mNamedBeanHandleTurnout.getBean().provideFirstFeedbackSensor(pName); } catch (JmriException ex){}
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not do anything if it fails.")
    @Override
    public void provideSecondFeedbackSensor(String pName) throws JmriException {
        if (_mNamedBeanHandleTurnout == null) return;
        try { _mNamedBeanHandleTurnout.getBean().provideSecondFeedbackSensor(pName); } catch (JmriException ex){}
    }

    @Override
    public Sensor getFirstSensor() {
        if (_mNamedBeanHandleTurnout == null) return (Sensor)DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleTurnout.getBean().getFirstSensor();
    }

    @Override
    public NamedBeanHandle<Sensor> getFirstNamedSensor() {
        if (_mNamedBeanHandleTurnout == null) return null;
        return _mNamedBeanHandleTurnout.getBean().getFirstNamedSensor();
    }

    @Override
    public Sensor getSecondSensor() {
        if (_mNamedBeanHandleTurnout == null) return (Sensor)DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleTurnout.getBean().getSecondSensor();
    }

    @Override
    public NamedBeanHandle<Sensor> getSecondNamedSensor() {
        if (_mNamedBeanHandleTurnout == null) return null;
        return _mNamedBeanHandleTurnout.getBean().getSecondNamedSensor();
    }

    @Override
    public void setInitialKnownStateFromFeedback() {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setInitialKnownStateFromFeedback();
    }

    @Override
    public int getNumberOutputBits() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getNumberOutputBits();
    }

    @Override
    public void setNumberOutputBits(int num) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setNumberOutputBits(num);
    }

    @Override
    public int getControlType() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getControlType();
    }

    @Override
    public void setControlType(int num) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setControlType(num);
    }

    @Override
    public boolean getInverted() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().getInverted();
    }

    @Override
    public void setInverted(boolean inverted) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setInverted(inverted);
    }

    @Override
    public boolean canInvert() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().canInvert();
    }

    @Override
    public boolean getLocked(int turnoutLockout) {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().getLocked(turnoutLockout);
    }

    @Override
    public void enableLockOperation(int turnoutLockout, boolean locked) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().enableLockOperation(turnoutLockout, locked);
    }

    @Override
    public boolean canLock(int turnoutLockout) {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().canLock(turnoutLockout);
    }

    @Override
    public int getPossibleLockModes() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getPossibleLockModes();
    }

    @Override
    public void setLocked(int turnoutLockout, boolean locked) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setLocked(turnoutLockout, locked);
    }

    @Override
    public boolean getReportLocked() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleTurnout.getBean().getReportLocked();
    }

    @Override
    public void setReportLocked(boolean reportLocked) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setReportLocked(reportLocked);
    }

    @Override
    public String[] getValidDecoderNames() {
        if (_mNamedBeanHandleTurnout == null) return new String[0];
        return _mNamedBeanHandleTurnout.getBean().getValidDecoderNames();
    }

    @Override
    public String getDecoderName() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getDecoderName();
    }

    @Override
    public void setDecoderName(String decoderName) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setDecoderName(decoderName);
    }

    @Override
    public void setBinaryOutput(boolean state) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setBinaryOutput(state);
    }

    @Override
    public float getDivergingLimit() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_FLOAT_RV;
        return _mNamedBeanHandleTurnout.getBean().getDivergingLimit();
    }

    @Override
    public String getDivergingSpeed() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getDivergingSpeed();
    }

    @Override
    public void setDivergingSpeed(String s) throws JmriException {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setDivergingSpeed(s);
    }

    @Override
    public float getStraightLimit() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_FLOAT_RV;
        return _mNamedBeanHandleTurnout.getBean().getStraightLimit();
    }

    @Override
    public String getStraightSpeed() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getStraightSpeed();
    }

    @Override
    public void setStraightSpeed(String s) throws JmriException {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setStraightSpeed(s);
    }

    @Override
    public String getUserName() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getUserName();
    }

    @Override
    public void setUserName(String s) throws BadUserNameException {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setUserName(s);
    }

    @Override
    public String getSystemName() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getSystemName();
    }

    @Override
    public String getDisplayName() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getDisplayName();
    }

    @Override
    public String getFullyFormattedDisplayName() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getFullyFormattedDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().addPropertyChangeListener(l, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().removePropertyChangeListener(l);
    }

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().updateListenerRef(l, newName);
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "I don't use this function, let it not do anything if it fails.")
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (_mNamedBeanHandleTurnout == null) return;
        try { _mNamedBeanHandleTurnout.getBean().vetoableChange(evt); } catch (PropertyVetoException ex){}
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        if (_mNamedBeanHandleTurnout == null) return new ArrayList<>();
        return _mNamedBeanHandleTurnout.getBean().getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getNumPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        if (_mNamedBeanHandleTurnout == null) return new PropertyChangeListener[0];
        return _mNamedBeanHandleTurnout.getBean().getPropertyChangeListenersByReference(name);
    }

    @Override
    public void dispose() {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().dispose();
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Let it not do anything if it fails.")
    @Override
    public void setState(int s) throws JmriException {
        if (_mNamedBeanHandleTurnout == null) return;
        try { _mNamedBeanHandleTurnout.getBean().setState(s); } catch (JmriException ex){}
    }

    @Override
    public int getState() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().getState();
    }

    @Override
    public String describeState(int state) {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().describeState(state);
    }

    @Override
    public String getComment() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getComment();
    }

    @Override
    public void setComment(String comment) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleTurnout.getBean().getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        if (_mNamedBeanHandleTurnout == null) return;
        _mNamedBeanHandleTurnout.getBean().removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        if (_mNamedBeanHandleTurnout == null) return Collections.emptySet();
        return _mNamedBeanHandleTurnout.getBean().getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleTurnout.getBean().getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        if (_mNamedBeanHandleTurnout == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleTurnout.getBean().compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name,
            String listenerRef) {
        _mNamedBeanHandleTurnout.getBean().addPropertyChangeListener(propertyName, listener, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _mNamedBeanHandleTurnout.getBean().addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return _mNamedBeanHandleTurnout.getBean().getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _mNamedBeanHandleTurnout.getBean().getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _mNamedBeanHandleTurnout.getBean().removePropertyChangeListener(propertyName, listener);
    }
}

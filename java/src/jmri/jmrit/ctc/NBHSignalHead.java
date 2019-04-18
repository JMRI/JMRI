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
public class NBHSignalHead extends NBHAbstractSignalCommon implements SignalHead {

//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // NOI18N  For any function that returns String.

    private static final NamedBeanHandleManager NAMED_BEAN_HANDLE_MANAGER = InstanceManager.getDefault(NamedBeanHandleManager.class);

//  The "thing" we're protecting:
    private final NamedBeanHandle<SignalHead> _mNamedBeanHandleSignalHead;

    protected NBHSignalHead(String signal) {
        if (!ProjectsCommonSubs.isNullOrEmptyString(signal)) {
            // Cannot use a constant Instance manager reference due to the dynamic nature of tests.
            SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signal.trim());
            if (signalHead != null) {
                _mNamedBeanHandleSignalHead = NAMED_BEAN_HANDLE_MANAGER.getNamedBeanHandle(signal, signalHead);
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

    @Override
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
    public String getAppearanceName() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getAppearanceName();
    }

    @Override
    public String getAppearanceName(int appearance) {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getAppearanceName(appearance);
    }

    @Override
    public boolean getLit() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalHead.getBean().getLit();
    }

    @Override
    public void setLit(boolean newLit) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setLit(newLit);
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

    @Override
    public int[] getValidStates() {
        if (_mNamedBeanHandleSignalHead == null) return new int[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStates();
    }

    @Override
    public String[] getValidStateNames() {
        if (_mNamedBeanHandleSignalHead == null) return new String[0];
        return _mNamedBeanHandleSignalHead.getBean().getValidStateNames();
    }

    @Override
    public boolean isCleared() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalHead.getBean().isCleared();
    }

    @Override
    public boolean isShowingRestricting() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalHead.getBean().isShowingRestricting();
    }

    @Override
    public boolean isAtStop() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalHead.getBean().isAtStop();
    }

    @Override
    public String getUserName() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getUserName();
    }

    @Override
    public void setUserName(String s) throws BadUserNameException {
        if (_mNamedBeanHandleSignalHead == null) return;
        try { _mNamedBeanHandleSignalHead.getBean().setUserName(s); } catch (BadUserNameException ex){}
    }

    @Override
    public String getSystemName() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getSystemName();
    }

    @Override
    public String getDisplayName() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getDisplayName();
    }

    @Override
    public String getFullyFormattedDisplayName() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getFullyFormattedDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().addPropertyChangeListener(l, name, listenerRef);
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

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().updateListenerRef(l, newName);
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "I don't use this function, let it not do anything if it fails.")
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (_mNamedBeanHandleSignalHead == null) return;
        try { _mNamedBeanHandleSignalHead.getBean().vetoableChange(evt); } catch (PropertyVetoException ex){}
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        if (_mNamedBeanHandleSignalHead == null) return new ArrayList<>();
        return _mNamedBeanHandleSignalHead.getBean().getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalHead.getBean().getNumPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        if (_mNamedBeanHandleSignalHead == null) return new PropertyChangeListener[0];
        return _mNamedBeanHandleSignalHead.getBean().getPropertyChangeListenersByReference(name);
    }

    @Override
    public void dispose() {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().dispose();
    }

    @Override
    public void setState(int s) throws JmriException {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setState(s);
    }

    @Override
    public int getState() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalHead.getBean().getState();
    }

    @Override
    public String describeState(int state) {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().describeState(state);
    }

    @Override
    public String getComment() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getComment();
    }

    @Override
    public void setComment(String comment) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleSignalHead.getBean().getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        if (_mNamedBeanHandleSignalHead == null) return;
        _mNamedBeanHandleSignalHead.getBean().removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        if (_mNamedBeanHandleSignalHead == null) return Collections.emptySet();
        return _mNamedBeanHandleSignalHead.getBean().getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalHead.getBean().getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        if (_mNamedBeanHandleSignalHead == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalHead.getBean().compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name,
            String listenerRef) {
        _mNamedBeanHandleSignalHead.getBean().addPropertyChangeListener(propertyName, listener, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _mNamedBeanHandleSignalHead.getBean().addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return _mNamedBeanHandleSignalHead.getBean().getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _mNamedBeanHandleSignalHead.getBean().getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _mNamedBeanHandleSignalHead.getBean().removePropertyChangeListener(propertyName, listener);
    }
}

package jmri.jmrit.ctc;

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
public class NBHSignalMast extends NBHAbstractSignalCommon implements SignalMast {
    
//  Standard sane return values for the types indicated:
    public static final Object DEFAULT_OBJECT_RV = null;       // For any function that returns something derived from Java's Object.
    public static final boolean DEFAULT_BOOLEAN_RV = false;    // For any function that returns boolean.
    public static final int DEFAULT_INT_RV = 0;                // For any function that returns int.
    public static final long DEFAULT_LONG_RV = 0;              // For any function that returns long.
    public static final float DEFAULT_FLOAT_RV = (float)0.0;   // For any function that returns float.
    public static final String DEFAULT_STRING_RV = "UNKNOWN";  // For any function that returns String.

    private static final SignalMastManager SIGNAL_MAST_MANAGER = InstanceManager.getDefault(jmri.SignalMastManager.class);
    private static final NamedBeanHandleManager NAMED_BEAN_HANDLE_MANAGER = InstanceManager.getDefault(NamedBeanHandleManager.class);

//  The "thing" we're protecting:    
    private final NamedBeanHandle<SignalMast> _mNamedBeanHandleSignalMast;

//  The string to determine "Is the Signal Mast all Red":
    private final String _mDangerAppearance;
    
    protected NBHSignalMast(String signal) {
        if (!ProjectsCommonSubs.isNullOrEmptyString(signal)) {
            SignalMast signalMast = SIGNAL_MAST_MANAGER.getSignalMast(signal.trim());
            if (signalMast != null) {
                _mNamedBeanHandleSignalMast = NAMED_BEAN_HANDLE_MANAGER.getNamedBeanHandle(signal, signalMast);
                _mDangerAppearance = getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                return;
            }
        }
        _mDangerAppearance = "";                // Never used, just required for "final"
        _mNamedBeanHandleSignalMast = null;
    }
        
    public boolean valid() { return _mNamedBeanHandleSignalMast != null; }  // For those that want to know the internal state.
    public Object getBean() { return _mNamedBeanHandleSignalMast.getBean(); }
    public void setCTCHeld(boolean held) { setHeld(held); }
    public boolean isDanger() {
        if (getHeld()) return true;     // Safety.  Problem in signal head, maybe same problem here?
        return getAspect().equals(_mDangerAppearance);
    }
//  Fake for SignalHead support:    
    public String[] getValidStateNames() { return new String[0]; }
    public int[] getValidStates() { return new int[0]; }
    public void setAppearance(int newAppearance) {}
            
    @Override
    public void setAspect(String aspect) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setAspect(aspect);
    }

    @Override
    public String getAspect() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getAspect();
    }

    @Override
    public Vector<String> getValidAspects() {
        if (_mNamedBeanHandleSignalMast == null) return new Vector<>();
        return _mNamedBeanHandleSignalMast.getBean().getValidAspects();
    }

//  Without a default "SignalSystem", just return null if the object is invalid, let the caller deal with it!    
    @Override
    public SignalSystem getSignalSystem() {
        if (_mNamedBeanHandleSignalMast == null) return null;
        return _mNamedBeanHandleSignalMast.getBean().getSignalSystem();
    }

//  Without a default "SignalAppearanceMap", just return null if the object is invalid, let the caller deal with it!    
    @Override
    public SignalAppearanceMap getAppearanceMap() {
        if (_mNamedBeanHandleSignalMast == null) return null;
        return _mNamedBeanHandleSignalMast.getBean().getAppearanceMap();
    }

    @Override
    public void setMastType(String type) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setMastType(type);
    }

    @Override
    public String getMastType() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getMastType();
    }

    @Override
    public boolean getLit() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalMast.getBean().getLit();
    }

    @Override
    public void setLit(boolean newLit) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setLit(newLit);
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
    public boolean isAspectDisabled(String aspect) {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalMast.getBean().isAspectDisabled(aspect);
    }

    @Override
    public void setAllowUnLit(boolean boo) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setAllowUnLit(boo);
    }

    @Override
    public boolean allowUnLit() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_BOOLEAN_RV;
        return _mNamedBeanHandleSignalMast.getBean().allowUnLit();
    }

    @Override
    public String getUserName() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getUserName();
    }

    @Override
    public void setUserName(String s) throws BadUserNameException {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setUserName(s);
    }

    @Override
    public String getSystemName() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getSystemName();
    }

    @Override
    public String getDisplayName() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getDisplayName();
    }

    @Override
    public String getFullyFormattedDisplayName() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getFullyFormattedDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().addPropertyChangeListener(l, name, listenerRef);
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

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().updateListenerRef(l, newName);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (_mNamedBeanHandleSignalMast == null) return;
        try { _mNamedBeanHandleSignalMast.getBean().vetoableChange(evt); } catch (PropertyVetoException ex){}
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        if (_mNamedBeanHandleSignalMast == null) return new ArrayList<>();
        return _mNamedBeanHandleSignalMast.getBean().getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalMast.getBean().getNumPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        if (_mNamedBeanHandleSignalMast == null) return new PropertyChangeListener[0];
        return _mNamedBeanHandleSignalMast.getBean().getPropertyChangeListenersByReference(name);
    }

    @Override
    public void dispose() {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().dispose();
    }

    @Override
    public void setState(int s) throws JmriException {
        if (_mNamedBeanHandleSignalMast == null) return;
        try { _mNamedBeanHandleSignalMast.getBean().setState(s); } catch (JmriException ex){}
    }

    @Override
    public int getState() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalMast.getBean().getState();
    }

    @Override
    public String describeState(int state) {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().describeState(state);
    }

    @Override
    public String getComment() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getComment();
    }

    @Override
    public void setComment(String comment) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_OBJECT_RV;
        return _mNamedBeanHandleSignalMast.getBean().getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        if (_mNamedBeanHandleSignalMast == null) return;
        _mNamedBeanHandleSignalMast.getBean().removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        if (_mNamedBeanHandleSignalMast == null) return Collections.emptySet();
        return _mNamedBeanHandleSignalMast.getBean().getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_STRING_RV;
        return _mNamedBeanHandleSignalMast.getBean().getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        if (_mNamedBeanHandleSignalMast == null) return DEFAULT_INT_RV;
        return _mNamedBeanHandleSignalMast.getBean().compareSystemNameSuffix(suffix1, suffix2, n2);
    }
}

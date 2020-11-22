package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Evaluates the state of a SignalMast.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionSignalMast extends AbstractDigitalAction
        implements VetoableChangeListener {

    private NamedBeanHandle<SignalMast> _signalMastHandle;
    private OperationType _operationType = OperationType.Aspect;
    private String _signalMastAspect = "";

    public ActionSignalMast(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSignalMast copy = new ActionSignalMast(sysName, userName);
        copy.setComment(getComment());
        if (_signalMastHandle != null) copy.setSignalMast(_signalMastHandle);
        copy.setOperationType(_operationType);
        copy.setAspect(_signalMastAspect);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setSignalMast(@Nonnull String signalMastName) {
        assertListenersAreNotRegistered(log, "setSignalMast");
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalMastName);
        if (signalMast != null) {
            setSignalMast(signalMast);
        } else {
            removeSignalMast();
            log.error("signalMast \"{}\" is not found", signalMastName);
        }
    }
    
    public void setSignalMast(@Nonnull NamedBeanHandle<SignalMast> handle) {
        assertListenersAreNotRegistered(log, "setSignalMast");
        _signalMastHandle = handle;
        InstanceManager.getDefault(SignalMastManager.class).addVetoableChangeListener(this);
    }
    
    public void setSignalMast(@Nonnull SignalMast signalMast) {
        assertListenersAreNotRegistered(log, "setSignalMast");
        setSignalMast(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalMast.getDisplayName(), signalMast));
    }
    
    public void removeSignalMast() {
        assertListenersAreNotRegistered(log, "setSignalMast");
        if (_signalMastHandle != null) {
            InstanceManager.getDefault(SignalMastManager.class).removeVetoableChangeListener(this);
            _signalMastHandle = null;
        }
    }
    
    public NamedBeanHandle<SignalMast> getSignalMast() {
        return _signalMastHandle;
    }
    
    public void setAspect(String aspect) {
        if (aspect == null) _signalMastAspect = "";
        else _signalMastAspect = aspect;
    }
    
    public String getAspect() {
        return _signalMastAspect;
    }
    
    public void setOperationType(OperationType state) {
        _operationType = state;
    }
    
    public OperationType getOperationType() {
        return _operationType;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalMast) {
                if (evt.getOldValue().equals(getSignalMast().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalMast_SignalMastInUseSignalMastActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalMast) {
                if (evt.getOldValue().equals(getSignalMast().getBean())) {
                    removeSignalMast();
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_signalMastHandle == null) return;
        
        switch (_operationType) {
            case Aspect:
                if (! "".equals(_signalMastAspect)) {
                    _signalMastHandle.getBean().setAspect(_signalMastAspect);
                }
                break;
            case Lit:
                _signalMastHandle.getBean().setLit(true);
                break;
            case NotLit:
                _signalMastHandle.getBean().setLit(false);
                break;
            case Held:
                _signalMastHandle.getBean().setHeld(true);
                break;
            case NotHeld:
                _signalMastHandle.getBean().setHeld(false);
                break;
            case PermissiveSmlDisabled:
                _signalMastHandle.getBean().setPermissiveSmlDisabled(true);
                break;
            case PermissiveSmlNotDisabled:
                _signalMastHandle.getBean().setPermissiveSmlDisabled(false);
                break;
            default:
                throw new RuntimeException("Unknown enum: "+_operationType.name());
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SignalMast_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String turnoutName;
        if (_signalMastHandle != null) {
            turnoutName = _signalMastHandle.getBean().getDisplayName();
        } else {
            turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        if (_operationType == OperationType.Aspect) {
            return Bundle.getMessage(locale, "SignalMast_LongAspect", turnoutName, _signalMastAspect.isEmpty() ? "''" : _signalMastAspect);
        } else {
            return Bundle.getMessage(locale, "SignalMast_Long", turnoutName, _operationType._text);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum OperationType {
        Aspect(Bundle.getMessage("SignalMastOperationType_Aspect")),
        Lit(Bundle.getMessage("SignalMastOperationType_Lit")),
        NotLit(Bundle.getMessage("SignalMastOperationType_NotLit")),
        Held(Bundle.getMessage("SignalMastOperationType_Held")),
        NotHeld(Bundle.getMessage("SignalMastOperationType_NotHeld")),
        PermissiveSmlDisabled(Bundle.getMessage("SignalMastOperationType_PermissiveSmlDisabled")),
        PermissiveSmlNotDisabled(Bundle.getMessage("SignalMastOperationType_PermissiveSmlNotDisabled"));
        
        private final String _text;
        
        private OperationType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMast.class);
    
}

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
 * Evaluates the state of a SignalHead.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionSignalHead extends AbstractDigitalAction
        implements VetoableChangeListener {

    private NamedBeanHandle<SignalHead> _signalHeadHandle;
    private OperationType _operationType = OperationType.Appearance;
    private int _signalHeadAppearance = SignalHead.DARK;

    public ActionSignalHead(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSignalHead copy = new ActionSignalHead(sysName, userName);
        copy.setComment(getComment());
        if (_signalHeadHandle != null) copy.setSignalHead(_signalHeadHandle);
        copy.setOperationType(_operationType);
        copy.setAppearance(_signalHeadAppearance);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setSignalHead(@Nonnull String signalHeadName) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName);
        if (signalHead != null) {
            setSignalHead(signalHead);
        } else {
            removeSignalHead();
            log.error("signalHead \"{}\" is not found", signalHeadName);
        }
    }
    
    public void setSignalHead(@Nonnull NamedBeanHandle<SignalHead> handle) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        _signalHeadHandle = handle;
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
    }
    
    public void setSignalHead(@Nonnull SignalHead signalHead) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        setSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead));
    }
    
    public void removeSignalHead() {
        assertListenersAreNotRegistered(log, "setSignalHead");
        if (_signalHeadHandle != null) {
            InstanceManager.getDefault(SignalHeadManager.class).removeVetoableChangeListener(this);
            _signalHeadHandle = null;
        }
    }
    
    public NamedBeanHandle<SignalHead> getSignalHead() {
        return _signalHeadHandle;
    }
    
    public void setAppearance(int appearance) {
        _signalHeadAppearance = appearance;
    }
    
    public int getAppearance() {
        return _signalHeadAppearance;
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
            if (evt.getOldValue() instanceof SignalHead) {
                if (evt.getOldValue().equals(getSignalHead().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalHead_SignalHeadInUseSignalHeadActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalHead) {
                if (evt.getOldValue().equals(getSignalHead().getBean())) {
                    removeSignalHead();
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
        if (_signalHeadHandle == null) return;
        
        switch (_operationType) {
            case Appearance:
                _signalHeadHandle.getBean().setAppearance(_signalHeadAppearance);
                break;
            case Lit:
                _signalHeadHandle.getBean().setLit(true);
                break;
            case NotLit:
                _signalHeadHandle.getBean().setLit(false);
                break;
            case Held:
                _signalHeadHandle.getBean().setHeld(true);
                break;
            case NotHeld:
                _signalHeadHandle.getBean().setHeld(false);
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
        return Bundle.getMessage(locale, "SignalHead_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String turnoutName;
        if (_signalHeadHandle != null) {
            turnoutName = _signalHeadHandle.getBean().getDisplayName();
        } else {
            turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        String appearence;
        if ((_signalHeadHandle != null) && (_signalHeadHandle.getBean() != null)) {
            appearence = _signalHeadHandle.getBean().getAppearanceName(_signalHeadAppearance);
        } else {
            appearence = "";
        }
        if (_operationType == OperationType.Appearance) {
            return Bundle.getMessage(locale, "SignalHead_LongApperance", turnoutName, appearence);
        } else {
            return Bundle.getMessage(locale, "SignalHead_Long", turnoutName, _operationType._text);
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
        Appearance(Bundle.getMessage("SignalHeadOperationType_Appearance")),
        Lit(Bundle.getMessage("SignalHeadOperationType_Lit")),
        NotLit(Bundle.getMessage("SignalHeadOperationType_NotLit")),
        Held(Bundle.getMessage("SignalHeadOperationType_Held")),
        NotHeld(Bundle.getMessage("SignalHeadOperationType_NotHeld"));
        
        private final String _text;
        
        private OperationType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHead.class);
    
}

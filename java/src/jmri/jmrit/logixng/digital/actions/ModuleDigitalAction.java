package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action executes a module.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ModuleDigitalAction extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Module> _moduleHandle;
    
    public ModuleDigitalAction(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setModule(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setModule");
        Module memory = InstanceManager.getDefault(ModuleManager.class).getModule(memoryName);
        if (memory != null) {
            setModule(memory);
        } else {
            removeModule();
            log.error("memory \"{}\" is not found", memoryName);
        }
    }
    
    public void setModule(@Nonnull NamedBeanHandle<Module> handle) {
        assertListenersAreNotRegistered(log, "setModule");
        _moduleHandle = handle;
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setModule(@Nonnull Module memory) {
        assertListenersAreNotRegistered(log, "setModule");
        setModule(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }
    
    public void removeModule() {
        assertListenersAreNotRegistered(log, "setModule");
        if (_moduleHandle != null) {
            InstanceManager.memoryManagerInstance().removeVetoableChangeListener(this);
            _moduleHandle = null;
        }
    }
    
    public NamedBeanHandle<Module> getModule() {
        return _moduleHandle;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Module) {
                if (evt.getOldValue().equals(getModule().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ModuleDigitalAction_ModuleInUseModuleExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Module) {
                if (evt.getOldValue().equals(getModule().getBean())) {
                    removeModule();
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (_moduleHandle == null) return;
        
        Module module = _moduleHandle.getBean();
        
        System.out.format("%n%nexecute module %s%n", module.getSystemName());
        
        FemaleSocket femaleSocket = module.getRootSocket();
        
        if (! (femaleSocket instanceof FemaleDigitalActionSocket)) {
            log.error("module.rootSocket is not a FemaleDigitalActionSocket");
            return;
        }
        
        ((FemaleDigitalActionSocket)femaleSocket).execute();
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
        return Bundle.getMessage(locale, "ModuleDigitalAction_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName;
        if (_moduleHandle != null) {
            memoryName = _moduleHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        return Bundle.getMessage(locale, "ModuleDigitalAction_Long", memoryName);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // A module never listen on beans
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // A module never listen on beans
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        removeModule();
    }
    
    
    public enum ModuleOperation {
        SET_TO_NULL,
        SET_TO_STRING,
        COPY_MEMORY;
    }
    
    private final static Logger log = LoggerFactory.getLogger(ModuleDigitalAction.class);
    
}

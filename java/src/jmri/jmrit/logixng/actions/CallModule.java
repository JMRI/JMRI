package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;

/**
 * This action executes a module.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class CallModule extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Module> _moduleHandle;
    private final Map<String, SymbolTable.ParameterData> _parameterData = new HashMap<>();
    
    public CallModule(String sys, String user)
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
        InstanceManager.getDefault(ModuleManager.class).addVetoableChangeListener(this);
    }
    
    public void setModule(@Nonnull Module module) {
        assertListenersAreNotRegistered(log, "setModule");
        setModule(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(module.getDisplayName(), module));
    }
    
    public void removeModule() {
        assertListenersAreNotRegistered(log, "setModule");
        if (_moduleHandle != null) {
            InstanceManager.getDefault(ModuleManager.class).removeVetoableChangeListener(this);
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
                    throw new PropertyVetoException(Bundle.getMessage("CallModule_ModuleInUseVeto", getDisplayName()), e); // NOI18N
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
        
        FemaleSocket femaleSocket = module.getRootSocket();
        
        if (! (femaleSocket instanceof FemaleDigitalActionSocket)) {
            log.error("module.rootSocket is not a FemaleDigitalActionSocket");
            return;
        }
        
        int currentStackPos = InstanceManager.getDefault(LogixNG_Manager.class).getStack().getCount();
        
        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable();
        newSymbolTable.createSymbols(_parameterData.values());
        newSymbolTable.createSymbols(module.getLocalVariables());
        InstanceManager.getDefault(LogixNG_Manager.class).setSymbolTable(newSymbolTable);
        
        ((FemaleDigitalActionSocket)femaleSocket).execute();
        
        newSymbolTable.returnSymbols(_parameterData.values());
        
        InstanceManager.getDefault(LogixNG_Manager.class).getStack().setCount(currentStackPos);
        
        InstanceManager.getDefault(LogixNG_Manager.class).setSymbolTable(newSymbolTable.getPrevSymbolTable());
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
        return Bundle.getMessage(locale, "CallModule_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName;
        if (_moduleHandle != null) {
            memoryName = _moduleHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        return Bundle.getMessage(locale, "CallModule_Long", memoryName);
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
    
    public void addParameter(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData,
            SymbolTable.ReturnValueType returnValueType,
            String returnValueData) {
        
        _parameterData.put(name,
                new DefaultSymbolTable.DefaultParameterData(
                        name,
                        initialValueType,
                        initialValueData,
                        returnValueType,
                        returnValueData));
    }
    
    public void removeParameter(String name) {
        _parameterData.remove(name);
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CallModule.class);
    
}

package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.Module.ReturnValueType;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;

/**
 * This expression evaluates a module.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalCallModule extends AbstractDigitalExpression implements VetoableChangeListener {

    private NamedBeanHandle<Module> _moduleHandle;
    private final List<ParameterData> _parameterData = new ArrayList<>();
    
    public DigitalCallModule(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = systemNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalCallModule copy = new DigitalCallModule(sysName, userName);
        if (_moduleHandle != null) copy.setModule(_moduleHandle);
        for (ParameterData data : _parameterData) {
            copy.addParameter(
                    data.getName(),
                    data.getInitalValueType(),
                    data.getInitialValueData(),
                    data.getReturnValueType(),
                    data.getReturnValueData());
        }
        return manager.registerExpression(copy);
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
    
    /**
     * Return the symbols
     * @param symbolTable the symbol table
     * @param symbolDefinitions list of symbols to return
     * @throws jmri.JmriException if an exception occurs
     */
    public void returnSymbols(
            DefaultSymbolTable symbolTable, Collection<ParameterData> symbolDefinitions)
            throws JmriException {
        
        for (ParameterData parameter : symbolDefinitions) {
            Object returnValue = symbolTable.getValue(parameter.getName());
            
            switch (parameter.getReturnValueType()) {
                case None:
                    break;
                    
                case LocalVariable:
                    symbolTable.getPrevSymbolTable()
                            .setValue(parameter.getReturnValueData(), returnValue);
                    break;
                    
                case Memory:
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(parameter.getReturnValueData());
                    if (m != null) m.setValue(returnValue);
                    break;
                    
                default:
                    log.error("definition.returnValueType has invalid value: {}", parameter.getReturnValueType().name());
                    throw new IllegalArgumentException("definition._returnValueType has invalid value: " + parameter.getReturnValueType().name());
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        if (_moduleHandle == null) return false;
        
        Module module = _moduleHandle.getBean();
        
        ConditionalNG oldConditionalNG = getConditionalNG();
        module.setCurrentConditionalNG(getConditionalNG());
        
        FemaleSocket femaleSocket = module.getRootSocket();
        
        if (! (femaleSocket instanceof FemaleDigitalExpressionSocket)) {
            log.error("module.rootSocket is not a FemaleDigitalExpressionSocket");
            return false;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG);
        newSymbolTable.createSymbols(conditionalNG.getSymbolTable(), _parameterData);
        newSymbolTable.createSymbols(module.getLocalVariables());
        conditionalNG.setSymbolTable(newSymbolTable);
        
        boolean result = ((FemaleDigitalExpressionSocket)femaleSocket).evaluate();
        
        returnSymbols(newSymbolTable, _parameterData);
        
        conditionalNG.getStack().setCount(currentStackPos);
        
        conditionalNG.setSymbolTable(newSymbolTable.getPrevSymbolTable());
        
        module.setCurrentConditionalNG(oldConditionalNG);
        
        return result;
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
        return Bundle.getMessage(locale, "DigitalCallModule_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String moduleName;
        if (_moduleHandle != null) {
            moduleName = _moduleHandle.getBean().getDisplayName();
        } else {
            moduleName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        return Bundle.getMessage(locale, "DigitalCallModule_Long", moduleName);
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
            InitialValueType initialValueType,
            String initialValueData,
            ReturnValueType returnValueType,
            String returnValueData) {
        
        _parameterData.add(
                new Module.ParameterData(
                        name,
                        initialValueType,
                        initialValueData,
                        returnValueType,
                        returnValueData));
    }
    
//    public void removeParameter(String name) {
//        _parameterData.remove(name);
//    }
    
    public List<ParameterData> getParameterData() {
        return _parameterData;
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalCallModule.class);
    
}

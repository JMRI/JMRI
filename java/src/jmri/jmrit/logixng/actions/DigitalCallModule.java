package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.Module.ReturnValueType;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * This action executes a module.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class DigitalCallModule extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<Module> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Module.class, InstanceManager.getDefault(ModuleManager.class), this);
    private final List<ParameterData> _parameterData = new ArrayList<>();

    public DigitalCallModule(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalCallModule copy = new DigitalCallModule(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        for (ParameterData data : _parameterData) {
            copy.addParameter(
                    data.getName(),
                    data.getInitialValueType(),
                    data.getInitialValueData(),
                    data.getReturnValueType(),
                    data.getReturnValueData());
        }
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Module> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.FLOW_CONTROL;
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
    public void execute() throws JmriException {
        Module module = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (module == null) return;

        FemaleSocket femaleSocket = module.getRootSocket();

        if (! (femaleSocket instanceof FemaleDigitalActionSocket)) {
            log.error("module.rootSocket is not a FemaleDigitalActionSocket");
            return;
        }

        ConditionalNG conditionalNG = getConditionalNG();

        int currentStackPos = conditionalNG.getStack().getCount();

        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG);
        newSymbolTable.createSymbols(conditionalNG.getSymbolTable(), _parameterData);
        newSymbolTable.createSymbols(module.getLocalVariables());
        conditionalNG.setSymbolTable(newSymbolTable);

        try {
            ((FemaleDigitalActionSocket)femaleSocket).execute();
        } catch (ReturnException e) {
            // Do nothing
        }

        returnSymbols(newSymbolTable, _parameterData);

        conditionalNG.getStack().setCount(currentStackPos);

        conditionalNG.setSymbolTable(newSymbolTable.getPrevSymbolTable());
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
        String moduleName = _selectNamedBean.getDescription(locale);

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
        _selectNamedBean.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
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

    public List<ParameterData> getParameterData() {
        return _parameterData;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalCallModule.class);

}

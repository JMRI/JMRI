package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

/**
 * This action prints the local variables to the log.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class LogLocalVariables extends AbstractDigitalAction {

    private boolean _includeGlobalVariables = true;
    private boolean _expandArraysAndMaps = false;
    private boolean _showClassName = false;


    public LogLocalVariables(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        LogLocalVariables copy = new LogLocalVariables(sysName, userName);
        copy.setComment(getComment());
        copy._includeGlobalVariables = _includeGlobalVariables;
        copy._expandArraysAndMaps = _expandArraysAndMaps;
        copy._showClassName = _showClassName;
        return manager.registerAction(copy);
    }

    public void setIncludeGlobalVariables(boolean value) {
        _includeGlobalVariables = value;
    }

    public boolean isIncludeGlobalVariables() {
        return _includeGlobalVariables;
    }

    public void setExpandArraysAndMaps(boolean value) {
        _expandArraysAndMaps = value;
    }

    public boolean isExpandArraysAndMaps() {
        return _expandArraysAndMaps;
    }

    public void setShowClassName(boolean value) {
        _showClassName = value;
    }

    public boolean isShowClassName() {
        return _showClassName;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.OTHER;
    }

    /** {@inheritDoc} */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="I18N in Warning strings.")
    @Override
    public void execute() {
        ConditionalNG c = getConditionalNG();
        log.warn(Bundle.getMessage("LogLocalVariables_Start"));
        for (SymbolTable.Symbol s : c.getSymbolTable().getSymbols().values()) {
            SymbolTable.printVariable(
                    log,
                    "    ",
                    s.getName(),
                    c.getSymbolTable().getValue(s.getName()),
                    _expandArraysAndMaps,
                    _showClassName,
                    Bundle.getMessage("LogLocalVariables_VariableName"),
                    Bundle.getMessage("LogLocalVariables_VariableValue"));
        }
        if (_includeGlobalVariables) {
            log.warn(Bundle.getMessage("LogLocalVariables_GlobalVariables_Start"));
            for (GlobalVariable gv : InstanceManager.getDefault(GlobalVariableManager.class).getNamedBeanSet()) {
                SymbolTable.printVariable(
                        log,
                        "    ",
                        gv.getUserName(),
                        gv.getValue(),
                        _expandArraysAndMaps,
                        _showClassName,
                        Bundle.getMessage("LogLocalVariables_GlobalVariableName"),
                        Bundle.getMessage("LogLocalVariables_GlobalVariableValue"));
            }
        }
        log.warn(Bundle.getMessage("LogLocalVariables_End"));
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
        return Bundle.getMessage(locale, "LogLocalVariables_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "LogLocalVariables_Long");
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogLocalVariables.class);
}

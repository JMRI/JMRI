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
        return manager.registerAction(copy);
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
    public void execute() {
        ConditionalNG c = getConditionalNG();
        log.warn(Bundle.getMessage("LogLocalVariables_Start"));
        for (SymbolTable.Symbol s : c.getSymbolTable().getSymbols().values()) {
            log.warn(Bundle.getMessage("LogLocalVariables_Variable", s.getName(), c.getSymbolTable().getValue(s.getName())));
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

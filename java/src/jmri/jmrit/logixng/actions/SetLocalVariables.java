package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

/**
 * This action sets some local variables. It is used internally.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class SetLocalVariables extends AbstractDigitalAction {

    private final Map<String, Object> _variablesWithValues = new HashMap<>();

    public SetLocalVariables(String sysName)
            throws BadUserNameException {
        super(sysName, null);
    }

    public SetLocalVariables(String sysName, String userName)
            throws BadUserNameException {
        super(sysName, userName);
    }

    public Map<String, Object> getMap() {
        return _variablesWithValues;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalActionBean copy = new SetLocalVariables(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        ConditionalNG cng = this.getConditionalNG();
        for (var entry : _variablesWithValues.entrySet()) {
            cng.getSymbolTable().setValue(entry.getKey(), entry.getValue());
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
        return Bundle.getMessage(locale, "SetLocalVariables_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "SetLocalVariables_Long");
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
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

}

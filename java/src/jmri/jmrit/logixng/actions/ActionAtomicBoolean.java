package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultMaleAnalogActionSocket;

/**
 * This action sets the value of an AtomicBoolean. It is mostly used for tests.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionAtomicBoolean extends AbstractDigitalAction {

    private AtomicBoolean _atomicBoolean;
    private boolean _newValue;
    
    public ActionAtomicBoolean(AtomicBoolean atomicBoolean, boolean newValue)
            throws BadUserNameException {
        super(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        _atomicBoolean = atomicBoolean;
        _newValue = newValue;
    }

    public ActionAtomicBoolean(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _atomicBoolean = new AtomicBoolean();
    }
    
    public ActionAtomicBoolean(String sys, String user, AtomicBoolean atomicBoolean, boolean newValue)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _atomicBoolean = atomicBoolean;
        _newValue = newValue;
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalActionBean copy = new ActionAtomicBoolean(sysName, userName, new AtomicBoolean(), _newValue);
        copy.setComment(getComment());
        return manager.registerAction(copy);
    }
    
    public void setAtomicBoolean(AtomicBoolean atomicBoolean) {
        _atomicBoolean = atomicBoolean;
    }
    
    public AtomicBoolean getAtomicBoolean() {
        return _atomicBoolean;
    }
    
    public void setNewValue(boolean newValue) {
        _newValue = newValue;
    }
    
    public boolean getNewValue() {
        return _newValue;
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        _atomicBoolean.set(_newValue);
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
        return Bundle.getMessage(locale, "ActionAtomicBoolean_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionAtomicBoolean_Long", _newValue);
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

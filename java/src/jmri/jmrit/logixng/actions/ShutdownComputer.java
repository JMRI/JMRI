package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrit.logixng.*;

/**
 * This action sets the state of a turnout.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ShutdownComputer extends AbstractDigitalAction {

    public ShutdownComputer(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ShutdownComputer copy = new ShutdownComputer(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.EXRAVAGANZA;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        InstanceManager.getDefault(ShutDownManager.class).shutdownOS();

        // If we are here, shutdown has failed
        log.error("Shutdown failed");  // NOI18N
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
        return Bundle.getMessage(locale, "ShutdownComputer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ShutdownComputer_Long");
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

    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShutdownComputer.class);
}

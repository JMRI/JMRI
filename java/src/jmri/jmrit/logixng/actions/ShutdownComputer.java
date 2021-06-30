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

    private Operation _operation = Operation.ShutdownJMRI;
    
    
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
        copy.setOperation(_operation);
        return manager.registerAction(copy);
    }
    
    public void setOperation(Operation operation) {
        _operation = operation;
    }
    
    public Operation getOperation() {
        return _operation;
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
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            switch (_operation) {
                case ShutdownComputer:
                    InstanceManager.getDefault(ShutDownManager.class).shutdownOS();
                    break;
                    
                case RebootComputer:
                    InstanceManager.getDefault(ShutDownManager.class).restartOS();
                    break;
                    
                case ShutdownJMRI:
                    InstanceManager.getDefault(ShutDownManager.class).shutdown();
                    break;
                    
                case RebootJMRI:
                    InstanceManager.getDefault(ShutDownManager.class).restart();
                    break;
                    
                default:
                    throw new RuntimeException("_operation has invalid value: "+_operation.name());
            }
        });

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
        return Bundle.getMessage(locale, "ShutdownComputer_Long", _operation._text);
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
    
    
    
    public enum Operation {
        ShutdownComputer(Bundle.getMessage("ShutdownComputer_ShutdownComputer")),
        RebootComputer(Bundle.getMessage("ShutdownComputer_RebootComputer")),
        ShutdownJMRI(Bundle.getMessage("ShutdownComputer_ShutdownJMRI")),
        RebootJMRI(Bundle.getMessage("ShutdownComputer_RebootJMRI"));
        
        private final String _text;
        
        private Operation(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShutdownComputer.class);
}

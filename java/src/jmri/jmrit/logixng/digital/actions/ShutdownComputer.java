package jmri.jmrit.logixng.digital.actions;

import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action sets the state of a turnout.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ShutdownComputer extends AbstractDigitalAction {

    private int _seconds;
    
    public ShutdownComputer(String sys, String user, int seconds)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setSeconds(seconds);
    }
    
    public void setSeconds(int seconds) {
        if (seconds < 0) throw new IllegalArgumentException("seconds must not be negative");
        _seconds = seconds;
    }
    
    public int getSeconds() {
        return _seconds;
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
        try {
            String shutdownCommand;
            if (SystemType.isLinux() || SystemType.isUnix() || SystemType.isMacOSX()) {
                String time = (_seconds == 0) ? "now" : Integer.toString(_seconds);
                shutdownCommand = "shutdown -h " + time;
            } else if (SystemType.isWindows()) {
                shutdownCommand = "shutdown.exe -s -t " + Integer.toString(_seconds);
            } else {
                throw new UnsupportedOperationException("Unknown OS: "+SystemType.getOSName());
            }
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(shutdownCommand);
            
            InstanceManager.getDefault(ShutDownManager.class).shutdown();
            
            // If we are here, shutdown has failed
            log.error("Shutdown failed");  // NOI18N
            
        } catch (SecurityException | IOException e) {
            log.error("Shutdown failed", e);  // NOI18N
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
        return Bundle.getMessage(locale, "ShutdownComputer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ShutdownComputer_Long", _seconds);
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

    
    private final static Logger log = LoggerFactory.getLogger(ShutdownComputer.class);
}

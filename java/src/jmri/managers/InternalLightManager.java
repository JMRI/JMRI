package jmri.managers;

import jmri.Light;
import jmri.implementation.AbstractVariableLight;

/**
 * Implement a light manager for "Internal" (virtual) lights.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @deprecated As of 4.3.5, use jmri.jmrix.internal classes
 */
@Deprecated
public class InternalLightManager extends AbstractLightManager {

    /**
     * Create and return an internal (no layout connection) Light
     */
    @Override
    protected Light createNewLight(String systemName, String userName) {
        return new AbstractVariableLight(systemName, userName) {
 
            //protected void forwardCommandChangeToLayout(int s) {}
            @Override
            protected void sendIntensity(double intensity) {
            }

            @Override
            protected void sendOnOffCommand(int newState) {
            }

            @Override
            protected int getNumberOfSteps() {
                return 100;
            }
        };
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public boolean validSystemNameConfig(String systemName) {
        return true;
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    @Override
    public boolean supportsVariableLights(String systemName) {
        return true;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

}

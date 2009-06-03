// InternalLightManager.java

package jmri.managers;

import jmri.Light;
import jmri.implementation.AbstractLight;
import jmri.managers.AbstractLightManager;

/**
 * Implement a light manager for "Internal" (virtual) lights.
 *
 * @author			Bob Jacobsen Copyright (C) 2009
 * @version			$Revision: 1.3 $
 */
public class InternalLightManager extends AbstractLightManager {

    /**
     * Create and return an internal (no layout connection) Light
     */
    protected Light createNewLight(String systemName, String userName) {
        return new AbstractLight(systemName, userName){
            //protected void forwardCommandChangeToLayout(int s) {}
        };
    }
    
    public char systemLetter() { return 'I'; }
    public boolean validSystemNameConfig(String systemName) {
        return true;
    }
    public boolean validSystemNameFormat(String systemName) {
        return true;
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InternalLightManager.class.getName());
}

/* @(#)InternalLightManager.java */

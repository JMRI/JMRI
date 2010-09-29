// SpecificLightManager.java

package jmri.jmrix.powerline.insteon2412s;

import jmri.Light;
import jmri.jmrix.powerline.SerialAddress;

/**
 * Implement light manager for powerline serial systems with Insteon 2412S adapters
 * <P>
 * Just provide the specific objects at creation time.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009
 * @version	$Revision: 1.2 $
 */
public class SpecificLightManager extends jmri.jmrix.powerline.SerialLightManager {

    public SpecificLightManager() {
        super();
    }

    /** 
     * Create light of a specific type for the interface
     */
    protected Light createNewSpecificLight(String systemName, String userName) {
        if (isInsteon(systemName)) {
            return new SpecificInsteonLight(systemName, userName);
        } else {
            return new SpecificX10Light(systemName, userName);
        }
    }
    
    boolean isInsteon(String systemName) {
        return SerialAddress.isInsteon(systemName);
    }
}

/* @(#)SpecificLightManager.java */

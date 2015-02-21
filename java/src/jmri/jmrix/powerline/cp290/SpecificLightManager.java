// SpecificLightManager.java
package jmri.jmrix.powerline.cp290;

import jmri.Light;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implement light manager for powerline serial systems with CP290 adapters
 * <P>
 * Just provide the specific objects at creation time.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SpecificLightManager extends jmri.jmrix.powerline.SerialLightManager {

    /**
     *
     */
    private static final long serialVersionUID = -581430981553242970L;

    public SpecificLightManager(SerialTrafficController tc) {
        super(tc);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    /**
     * Create light of a specific type for the interface
     */
    protected Light createNewSpecificLight(String systemName, String userName) {
        return new SpecificLight(systemName, tc, userName);
    }

}

/* @(#)SpecificLightManager.java */

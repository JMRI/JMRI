// SpecificLightManager.java
package jmri.jmrix.powerline.cm11;

import jmri.Light;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implement light manager for powerline serial systems with CM11 adapters
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
    private static final long serialVersionUID = -3686959276956865593L;

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

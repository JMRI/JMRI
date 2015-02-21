// SpecificLightManager.java
package jmri.jmrix.powerline.simulator;

import jmri.Light;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implement light manager for powerline serial systems with Insteon 2412S
 * adapters
 * <P>
 * Just provide the specific objects at creation time.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SpecificLightManager extends jmri.jmrix.powerline.SerialLightManager {

    /**
     *
     */
    private static final long serialVersionUID = -8171888128741447856L;

    public SpecificLightManager(SerialTrafficController tc) {
        super(tc);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    /**
     * Create light of a specific type for the interface
     */
    protected Light createNewSpecificLight(String systemName, String userName) {
        if (isInsteon(systemName)) {
            return new SpecificInsteonLight(systemName, tc, userName);
        } else {
            return new SpecificX10Light(systemName, tc, userName);
        }
    }

    boolean isInsteon(String systemName) {
        return tc.getAdapterMemo().getSerialAddress().isInsteon(systemName);
    }
}

/* @(#)SpecificLightManager.java */

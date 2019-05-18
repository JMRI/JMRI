package jmri.jmrix.powerline.cm11;

import jmri.Light;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implement LightManager for powerline serial systems with CM11 adapters.
 * <p>
 * Just provide the specific objects at creation time.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificLightManager extends jmri.jmrix.powerline.SerialLightManager {

    public SpecificLightManager(SerialTrafficController tc) {
        super(tc);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    /**
     * Create light of a specific type for the interface
     */
    @Override
    protected Light createNewSpecificLight(String systemName, String userName) {
        return new SpecificLight(systemName, tc, userName);
    }

}

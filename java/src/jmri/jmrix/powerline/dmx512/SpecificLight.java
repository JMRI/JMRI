package jmri.jmrix.powerline.dmx512;

import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implementation of the Light Object for DMX512 Serial interfaces.
 * <p>
 * The intensity is mapped from 0.0 to 1.0 into 255 steps as a hex byte.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009, 2010
 * @author Ken Cameron Copyright (C) 2009 Converted to multiple connection
 * @author Ken Cameron Copyright (C) 2023
 */
public class SpecificLight extends jmri.jmrix.powerline.dmx512.SpecificDmxLight {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     *
     * @param systemName systemName for light
     * @param tc         tc for connection
     */
    public SpecificLight(String systemName, SerialTrafficController tc) {
        super(systemName, tc);
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     *
     * @param systemName systemName for light
     * @param tc         tc for connection
     * @param userName   userName for light
     */
    public SpecificLight(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, tc, userName);
    }

}

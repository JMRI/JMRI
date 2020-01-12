package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implementation of the Light Object for X10 CM11 interfaces.
 * <p>
 * Uses X10 dimming commands to set intensity unless the value is 0.0 or 1.0, in
 * which case it uses on/off commands only.
 * <p>
 * Since the dim/bright step of the hardware is unknown then the Light object is
 * first created, the first time the intensity (not state) is set to other than
 * 0.0 or 1.0, the output is run to it's maximum dim or bright step so that we
 * know the count is right.
 * <p>
 * Keeps track of the controller's "dim count", and if not certain forces it to
 * zero to be sure.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009, 2010
 * @author Ken Cameron Copyright (C) 2009 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificLight extends jmri.jmrix.powerline.SerialX10Light {

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

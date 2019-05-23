package jmri.jmrix.rps;

import jmri.Manager.NameValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses for RPS
 * <p>
 * Address format supported:
 * <ul>
 *   <li>
 *   RS(0,0,0);(1,0,0);(1,1,0);(0,1,0) where (0,0,0) is the first point coordinate of the associated region
 *   </li>
 * </ul>
 *
 * @author Egbert Broerse (C) 2019 Based on lenz.XNetAddress example and rps.Region code.
 */
public class RpsAddress {

    public RpsAddress() {
    }

    /**
     * Public static method to validate RPS system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix + type))) {
            // here if an illegal format 
            log.error("invalid character in header field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        String s = systemName.substring(prefix.length() + 1);
        String[] pStrings = s.split(";");
        if (pStrings.length < 3) {
            log.warn("need to have at least 3 points in {}", systemName);
            return NameValidity.INVALID;
        }
        for (int i = 0; i < pStrings.length; i++) {
            if (!(pStrings[i].startsWith("(")) || !(pStrings[i].endsWith(")"))) {
                // here if an illegal format
                log.warn("missing brackets in point {}: \"{}\"", i, pStrings[i]);
                return NameValidity.INVALID;
            }
            // remove leading ( and trailing )
            String coords = pStrings[i].substring(1, pStrings[i].length() - 1);
            try {
                String[] coord = coords.split(",");
                if (coord.length != 3) {
                    log.warn("need to have three coordinates in point {}: \"{}\"", i, pStrings[i]);
                    return NameValidity.INVALID;
                }
                double x = Double.valueOf(coord[0]);
                double y = Double.valueOf(coord[1]);
                double z = Double.valueOf(coord[2]);
                log.debug("succes converting systemName point {} to {},{},{}", i, x, y, z);
                // valid, continue
            } catch (Exception e) {
                return NameValidity.INVALID;
            }
        }
        return NameValidity.VALID;
    }

    private final static Logger log = LoggerFactory.getLogger(RpsAddress.class);

}

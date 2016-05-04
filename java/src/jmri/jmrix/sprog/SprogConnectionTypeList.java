// SprogConnectionTypeList.java
package jmri.jmrix.sprog;

/**
 * Returns a list of valid Sprog Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class SprogConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String SPROG = "SPROG DCC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.sprog.sprog.ConnectionConfig",
            "jmri.jmrix.sprog.sprogCS.ConnectionConfig",
            "jmri.jmrix.sprog.sprognano.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogone.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogonecs.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprognano.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{SPROG};
    }

}

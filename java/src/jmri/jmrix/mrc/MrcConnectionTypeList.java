// MRCConnectionTypeList.java
package jmri.jmrix.mrc;

/**
 * Returns a list of valid MRC Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2014
 * @version	$Revision: 17977 $
 *
 */
public class MrcConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String MRC = "MRC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.mrc.serialdriver.ConnectionConfig",
            "jmri.jmrix.mrc.simulator.ConnectionConfig",}; //IN18N
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MRC};
    }

}

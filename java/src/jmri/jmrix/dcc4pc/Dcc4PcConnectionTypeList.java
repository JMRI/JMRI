// Dcc4PcConnectionTypeList.java
package jmri.jmrix.dcc4pc;

/**
 * Returns a list of valid Dcc4Pc Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 17977 $
 *
 */
public class Dcc4PcConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DCC4PC = "DCC4PC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.dcc4pc.serialdriver.ConnectionConfig",};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DCC4PC};
    }

}

// AcelaConnectionTypeList.java
package jmri.jmrix.acela;

/**
 * Returns a list of valid CTI Electronics Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class AcelaConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String CTI = "CTI Electronics";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.acela.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{CTI};
    }

}

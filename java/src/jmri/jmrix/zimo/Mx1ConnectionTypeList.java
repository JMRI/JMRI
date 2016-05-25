// Mx1ConnectionTypeList.java
package jmri.jmrix.zimo;

/**
 * Returns a list of valid Zimo Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class Mx1ConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ZIMO = "Zimo";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.zimo.mx1.ConnectionConfig",
            "jmri.jmrix.zimo.mxulf.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ZIMO};
    }

}

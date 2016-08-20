// RaspberryPiConnectionTypeList.java
package jmri.jmrix.pi;

/**
 * Returns a list of valid Raspberry Pi Connection Types
 * <P>
 * @author Paul Bender Copyright (C) 2015
 * @version	$Revision$
 *
 */
public class RaspberryPiConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String PI = "Raspberry Pi Foundation";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.pi.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{PI};
    }

}

// WangrowConnectionTypeList.java
package jmri.jmrix.wangrow;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class WangrowConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String WANGROW = "Wangrow";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.wangrow.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{WANGROW};
    }

}

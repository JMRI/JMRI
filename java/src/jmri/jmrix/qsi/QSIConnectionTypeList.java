// QSIConnectionTypeList.java
package jmri.jmrix.qsi;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class QSIConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String QSI = "QSI Solutions";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.qsi.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{QSI};
    }

}

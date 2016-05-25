// LenzConnectionTypeList.java
package jmri.jmrix.lenz.ztc640;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class ZTC640ConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ZTC = "ZTC";

    @Override
    public String[] getAvailableProtocolClasses() {
        String[] masterList = new jmri.jmrix.lenz.LenzConnectionTypeList().getAvailableProtocolClasses();

        String[] tempList = new String[masterList.length + 1];
        tempList[0] = "jmri.jmrix.lenz.ztc640.ConnectionConfig";
        int x = 1;
        for (int i = 0; i < masterList.length; i++) {
            tempList[x] = masterList[i];
            x++;
        }
        return tempList;
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ZTC};
    }

}

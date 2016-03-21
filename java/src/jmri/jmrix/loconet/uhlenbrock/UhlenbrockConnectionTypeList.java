// UhlenbrockConnectionTypeList.java.java
package jmri.jmrix.loconet.uhlenbrock;

/**
 * Returns a list of valid Uhlenbrock Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010, 2014
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 17977 $
 *
 */
public class UhlenbrockConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String UHLEN = "Uhlenbrock";

    @Override
    public String[] getAvailableProtocolClasses() {
        // replace existing LocoNet protocol list with just our two
        String[] tempList = new String[]{
            "jmri.jmrix.loconet.uhlenbrock.ConnectionConfig",
            "jmri.jmrix.loconet.Intellibox.ConnectionConfig"};
        return tempList;
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{UHLEN};
    }
}

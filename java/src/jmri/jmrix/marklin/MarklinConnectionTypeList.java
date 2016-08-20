// MarklinConnectionTypeList.java
package jmri.jmrix.marklin;

/**
 * Returns a list of valid ESU Marklin Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 17977 $
 *
 */
public class MarklinConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String MARKLIN = "Marklin";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.marklin.networkdriver.ConnectionConfig"/*,
         "jmri.jmrix.ecos.csreloaded.ConnectionConfig",*/

        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MARKLIN};
    }

}

package jmri.jmrix.marklin;

import org.openide.util.lookup.ServiceProvider;
import jmri.jmrix.ConnectionTypeList;

/**
 * Returns a list of valid ESU Marklin Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
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

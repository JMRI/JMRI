package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide a list of valid Uhlenbrock Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2014
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class UhlenbrockConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String UHLEN = "Uhlenbrock";

    @Override
    public String[] getAvailableProtocolClasses() {
        // replace existing LocoNet protocol list with just our two
        String[] tempList = new String[]{
            "jmri.jmrix.loconet.uhlenbrock.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.Intellibox.ConnectionConfig"}; // NOI18N
        return tempList;
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{UHLEN};
    }

}

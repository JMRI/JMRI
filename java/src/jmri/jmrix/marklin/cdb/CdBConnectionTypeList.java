package jmri.jmrix.marklin.cdb;

import javax.annotation.Nonnull;
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
public class CdBConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String CDB = "CAN-digital-Bahn"; // NOI18N

    @Override
    @Nonnull
    public String[] getAvailableProtocolClasses() {
        // replace existing LocoNet protocol list with just our three
        String[] tempList = new String[]{
            "jmri.jmrix.marklin.cdb.serialdriver.ConnectionConfig"}; // NOI18N
        return tempList;
    }

    @Override
    @Nonnull
    public String[] getManufacturers() {
        return new String[]{CDB};
    }

}

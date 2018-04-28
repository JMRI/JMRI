package jmri.jmrix;

import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class OtherConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String OTHER = "Others";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.direct.serial.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{OTHER};
    }

}

package jmri.jmrix.bachrus;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid drM Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2022
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Andrew Crosland Copyright (C) 2010
 * @author Lolke Bijlsma Copyright (C) 2025
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class DRMConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DRM = "drM";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.bachrus.drmserialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DRM};
    }

}

package jmri.jmrix.ecos;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid ESU Ecos Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class EcosConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ESU = "ESU";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.ecos.networkdriver.ConnectionConfig",};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ESU};
    }

}

package jmri.jmrix.ecos;

/**
 * Returns a list of valid ESU Ecos Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
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

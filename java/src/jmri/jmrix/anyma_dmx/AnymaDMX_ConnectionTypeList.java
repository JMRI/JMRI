package jmri.jmrix.anyma_dmx;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Anyma DMX Connection Types
 * <P>
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class AnymaDMX_ConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ANYMA_DMX = "Anyma DMX";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.anyma_dmx.AnymaDMX_ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ANYMA_DMX};
    }

}

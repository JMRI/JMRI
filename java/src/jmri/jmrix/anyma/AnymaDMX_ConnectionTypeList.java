package jmri.jmrix.anyma;

import jmri.jmrix.ConnectionTypeList;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Anyma DMX Connection Types
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since       4.9.6
 */
@ServiceProvider(service = ConnectionTypeList.class)
@API(status = EXPERIMENTAL)
public class AnymaDMX_ConnectionTypeList implements ConnectionTypeList {

    protected static final String ANYMA_DMX = "Anyma DMX512";

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.anyma.AnymaDMX_ConnectionConfig"
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getManufacturers() {
        return new String[]{ANYMA_DMX};
    }
}

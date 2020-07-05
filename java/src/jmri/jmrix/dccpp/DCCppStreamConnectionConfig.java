package jmri.jmrix.dccpp;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Handle configuring an DCC++ layout connection via an DCCppStreamPortController
 * adapter.
 * <p>
 * This uses the {@link DCCppStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 *
 * @see DCCppStreamPortController
 */
@API(status = EXPERIMENTAL)
public class DCCppStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p stream port controller.
     */
    public DCCppStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public DCCppStreamConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("DCCppStreamName");
    }

    String manufacturerName = "DCC++"; // NOI18N

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppStreamPortController();
        }
    }

}

package jmri.jmrix.lenz.lzv200;

import jmri.util.SystemType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "standard naming for ConnectionConfig objects in XpressNet")


/**
 * Handle configuring an XpressNet layout connection via a Lenz LZV200adapter.
 * <p>
 * This uses the {@link LZV200Adapter} class to do the actual connection.
 *
 * @author Paul Bender Copyright (C) 2005,2019
 *
 * @see LZV200Adapter
 */
public class ConnectionConfig extends jmri.jmrix.lenz.liusb.ConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Lenz LZV200"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LZV200Adapter();
        }
    }

}

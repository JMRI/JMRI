package jmri.jmrix.roco.z21;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuring the XpressNet tunnel for the z21 Connection.
 * <P>
 * This uses the {@link Z21XNetStreamPortController} class to do the actual 
 * connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2015
 *
 * @see Z21XNetStreamPortController
 */
public class Z21XNetConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public Z21XNetConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public Z21XNetConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Z21 XpressNet Stream";
    }

    String manufacturerName = "Roco";

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    @Override
    protected void setInstance() {
       log.error("Unexpected call to setInstance");
    }

    /**
     * Determine if configuration needs to be written to disk.
     *
     * this implementation always returns false.
     *
     * @return true if configuration need to be saved, false otherwise
     */
    @Override
    public boolean isDirty() {
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetConnectionConfig.class);

}

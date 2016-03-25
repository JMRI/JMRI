package jmri.jmrix.roco.z21;

/**
 * Handle configuring the XPressNet tunnel for the z21 Connection.
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

    public String name() {
        return "Z21 XPressNet Stream";
    }

    String manufacturerName = "Roco";

    public String getManufacturer() {
        return manufacturerName;
    }

    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    protected void setInstance() {
        if (adapter == null) {
            //adapter = new z21XNetStreamPortController();
        }
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

}

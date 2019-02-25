package jmri.jmrix.dccpp;

/**
 * Handle configuring an DCC++ layout connection via an DCCppStreamPortController
 * adapter.
 * <P>
 * This uses the {@link DCCppStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
  *
 * @see DCCppStreamPortController
 */
public class DCCppStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public DCCppStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
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

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppStreamPortController();
        }
    }
}

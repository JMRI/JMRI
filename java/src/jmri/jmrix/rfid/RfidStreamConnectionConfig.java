package jmri.jmrix.rfid;

/**
 * Handle configuring a standalone RFID layout connection via an RfidStreamPortController
 * adapter.
 * <P>
 * This uses the {@link RfidStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
  *
 * @see RfidStreamPortController
 */
public class RfidStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public RfidStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public RfidStreamConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("RfidStreamName");
    }

    String manufacturerName = "JMRI (Streams)"; // NOI18N

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
            //adapter = new RfidStreamPortController();
        }
    }

}

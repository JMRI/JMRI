package jmri.jmrix.loconet.streamport;

/**
 * Handle configuring an LocoNet layout connection via an LnStreamPortController
 * adapter.
 * <p>
 * This uses the {@link LnStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 *
 * @see LnStreamPortController
 */
public class LnStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public LnStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public LnStreamConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("LnStreamName");
    }

    String manufacturerName = "Digitrax"; // NOI18N

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
            adapter = new LnStreamPortController();
        }
    }

}

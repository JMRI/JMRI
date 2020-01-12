package jmri.jmrix.lenz;

/**
 * Handle configuring an XpressNet layout connection via an XNetStreamPortController
 * adapter.
 * <p>
 * This uses the {@link XNetStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 *
 * @see XNetStreamPortController
 */
public class XNetStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public XNetStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public XNetStreamConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("XNetStreamName");
    }

    String manufacturerName = "Lenz"; // NOI18N

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
            adapter = new XNetStreamPortController();
        }
    }

}

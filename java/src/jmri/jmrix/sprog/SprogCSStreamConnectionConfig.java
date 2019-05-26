package jmri.jmrix.sprog;

/**
 * Handle configuring an SPROG layout connection via an SprogCSStreamPortController
 * adapter.
 * <p>
 * This uses the {@link SprogCSStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 *
 * @see SprogCSStreamPortController
 */
public class SprogCSStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public SprogCSStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public SprogCSStreamConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("SprogCSStreamName");
    }

    String manufacturerName = "SPROG DCC"; // NOI18N

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
            adapter = new SprogCSStreamPortController();
        }
    }

}

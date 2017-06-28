package jmri.jmrix.can.adapters.gridconnect.net;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring a connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * 
 */
public class MergConnectionConfig extends ConnectionConfig {

    public final static String NAME = "CAN via MERG Network Interface";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public MergConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public MergConnectionConfig() {
        super();
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new MergNetworkDriverAdapter();
            adapter.setPort(5550);
        }
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }
}

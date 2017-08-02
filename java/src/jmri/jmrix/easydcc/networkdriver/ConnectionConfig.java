package jmri.jmrix.easydcc.networkdriver;

import jmri.jmrix.JmrixConfigPane;

/**
 * Definition of objects to handle configuring an EasyDCC layout connection via
 * a NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
  */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    @Override
    public String name() {
        return "EasyDCC via network";
    }

    /*public String getMode() {
     return opt2Box.getSelectedItem().toString();
     }*/
    /**
     * Reimplement this method to show the connected host, rather than the usual
     * port name.
     *
     * @return human-readable connection information
     */
    @Override
    public String getInfo() {
        String t = adapter.getHostName();
        if (t != null && !t.equals("")) {
            return t;
        } else {
            return JmrixConfigPane.NONE;
        }
    }

    @Override
    protected void setInstance() {
        adapter = NetworkDriverAdapter.instance();
    }

    @Override
    public boolean isPortAdvanced() {
        return false;
    }
}

package jmri.jmrix.direct.serial;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a layout connection via a
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
  */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        if (SystemType.isMacOSX()
                || (SystemType.isWindows() && Double.valueOf(System.getProperty("os.version")) >= 6)) {
            return "(Direct Drive (Serial) not available)";
        }

        return "Direct Drive (Serial)";
    }

    @Override
    protected void setInstance() {
        if(adapter == null ){
           adapter = SerialDriverAdapter.instance();
        }
    }
}

// ConnectionConfig.java

package jmri.jmrix.zimo.mx1;
import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via a Zimo MX-1 SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.zimo.ZimoActionListBundle");
    }
    
    public String name() { return "MX-1"; }

    protected void setInstance() { adapter = Mx1Adapter.instance(); }
}


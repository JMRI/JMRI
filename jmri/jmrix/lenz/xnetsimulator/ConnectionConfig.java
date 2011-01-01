// ConnectionConfig.java

package jmri.jmrix.lenz.xnetsimulator;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Handle configuring an XPressNet layout connection
 * via a XNetSimulator adapter.
 * <P>
 * This uses the {@link XNetSimulatorAdapter} class to do the actual
 * connection.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author      Paul Bender    Copyright (C) 2009
 * @version	$Revision: 1.6 $
 *
 * @see XNetSimulatorAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig {

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

    public String name() { return "XPressNet Simulator"; }
    
    String manufacturerName = "Lenz";
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
    
    public void loadDetails(JPanel details) {
        details.add(new JLabel("No options"));
    }

    protected void setInstance() {
       if(adapter==null)
          adapter = new XNetSimulatorAdapter(); 
    }
}


// ConnectionConfig.java

package jmri.jmrix.lenz.liusbserver;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrix.JmrixConfigPane;


/**
 * Handle configuring an XPressNet layout connection
 * via a LIUSB Server.
 * <P>
 * This uses the {@link LIUSBServerAdapter} class to do the actual
 * connection.
 *
 * @author	Paul Bender Copyright (C) 2009
 * @version	$Revision: 1.6 $
 *
 * @see LIUSBServerAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {


    protected jmri.jmrix.PortAdapter adapter = null;
    protected JTextField ipField = new JTextField(LIUSBServerAdapter.DEFAULT_IP_ADDRESS);
    protected JTextField commPortField = new JTextField(String.valueOf(LIUSBServerAdapter.COMMUNICATION_TCP_PORT));
    protected JTextField bcastPortField = new JTextField(String.valueOf(LIUSBServerAdapter.BROADCAST_TCP_PORT));

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

    public String name() { return "Lenz LIUSB Server"; }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it has already been set.
     */
    protected void setInstance() { adapter = LIUSBServerAdapter.instance(); }

    public String getInfo() {
        String t = (String)portBox.getSelectedItem();
        if (t!=null) return t;
        else return JmrixConfigPane.NONE;
    }

    public void loadDetails(JPanel details) {
     	details.add(new JLabel("No options"));
    }
}

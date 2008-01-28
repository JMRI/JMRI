// ConnectionConfig.java

package jmri.jmrix.loconet.loconetovertcp;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

/**
 * Definition of objects to handle configuring a LocoNetOverTcp layout connection
 * via a LnTcpDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author      Stephen Williams Copyright (C) 2008
 *
 * @version     $Revision: 1.5 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(String h, String p){
        super();
        hostName = h;
        portNumber = p;
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public JTextField host;
    String hostName ="localhost";
    public JTextField port;
    String portNumber ="1234";
    public JComboBox commandStation;

    public String name() { return "LocoNetOverTcp LbServer"; }

    public void loadDetails(JPanel details) {
        setInstance();

        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JPanel temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("Server Host Name:"));
        host = new JTextField(hostName);
        temp.add(host);
        details.add(temp);

        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("TCP Port Number:"));
        port = new JTextField(portNumber);
        temp.add(port);
        details.add(temp);

        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("Command station type:"));
        commandStation = new JComboBox();
        String [] csTypes = ((LnTcpDriverAdapter) adapter).getCommandStationNames();
        commandStation.removeAllItems();
        for (int ii=0; ii < csTypes.length; ii++)
            commandStation.addItem(csTypes[ii]);
        commandStation.setEnabled(true);
        commandStation.setSelectedItem(((LnTcpDriverAdapter) adapter).getCurrentCommandStation());
        temp.add(commandStation);
        
        details.add(temp);

    }

    /**
     * Reimplement this method to show the connected host,
     * rather than the usual port name.
     * @return human-readable connection information
     */
    public String getInfo() {
        String t = host.getText();
        if (t != null && !t.equals("")) return t;
        else return "(none)";
    }
    protected void setInstance() {
        adapter = LnTcpDriverAdapter.instance();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfig.class.getName());
}


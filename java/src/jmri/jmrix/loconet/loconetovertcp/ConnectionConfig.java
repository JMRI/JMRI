// ConnectionConfig.java

package jmri.jmrix.loconet.loconetovertcp;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import jmri.jmrix.JmrixConfigPane;

/**
 * Definition of objects to handle configuring a LocoNetOverTcp layout connection
 * via a LnTcpDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author      Stephen Williams Copyright (C) 2008
 *
 * @version     $Revision: 1.10 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(String h, String p, String m){
        super();
        hostName = h;
        portNumber = p;
        if (m!=null) manufacturerName=m;
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
        if(adapter.getSystemConnectionMemo()!=null){
            temp = new JPanel();
            temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
            temp.add(new JLabel("Connection Prefix"));
            systemPrefixField = new JTextField(adapter.getSystemConnectionMemo().getSystemPrefix());
            temp.add(systemPrefixField);
            details.add(temp);
            
            temp = new JPanel();
            temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
            temp.add(new JLabel("Connection Prefix"));
            connectionNameField = new JTextField(adapter.getSystemConnectionMemo().getUserName());
            temp.add(connectionNameField);
            details.add(temp);
        }
    }

    /**
     * Reimplement this method to show the connected host,
     * rather than the usual port name.
     * @return human-readable connection information
     */
    public String getInfo() {
        String t = host.getText();
        if (t != null && !t.equals("")) return t;
        else return JmrixConfigPane.NONE;
    }
    protected void setInstance() {
        adapter = LnTcpDriverAdapter.instance();
    }
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.DIGITRAX;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfig.class.getName());
}


// ConnectionConfig.java

package jmri.jmrix.nce.networkdriver;

import javax.swing.*;

/**
 * Definition of objects to handle configuring an NCE layout connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.2 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(String h, String p, String m){
        super();
        hostName = h;
        portNumber = p;
        mode = m;
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public JTextField host;
    String hostName ="";
    public JTextField port;
    String portNumber ="";

    String mode = "";
    
    public String name() { return "NCE via network"; }

    public void loadDetails(JPanel details) {
        JPanel temp = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("Server hostname:"));
        host = new JTextField(hostName);
        temp.add(host);
        details.add(temp);
        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("Port number:"));
        port = new JTextField(portNumber);
        temp.add(port);
        details.add(temp);

        if (adapter == null) adapter = NetworkDriverAdapter.instance();
        String[] opt2List = adapter.validOption2();
        opt2Box.removeAllItems();
        for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);
        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        if (opt2List.length>1) {
            temp.add(new JLabel(adapter.option2Name()));
            temp.add(opt2Box);
            opt2Box.setSelectedItem(adapter.getCurrentOption2Setting());
            if (! mode.equals("")) opt2Box.setSelectedItem(mode);
        }
        details.add(temp);
    }

    /**
     * Access to current selected command station mode
     */
    public String getMode() {
        return opt2Box.getSelectedItem().toString();
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
        log.error("Unexpected call to setInstance");
        new Exception().printStackTrace();
    }
}


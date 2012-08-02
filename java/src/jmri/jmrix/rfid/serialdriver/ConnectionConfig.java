// ConnectionConfig.java

package jmri.jmrix.rfid.serialdriver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection
 *
 * @author      Bob Jacobsen   Copyright (C) 2003, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version	$Revision$
 * @since       2.11.4
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
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }
	
    @Override
    public void loadDetails(JPanel details) {
        // have to embed the usual one in a new JPanel

        JPanel p = new JPanel();
        super.loadDetails(p);

        details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        details.add(p);

        // add another button
        //JButton b = new JButton("Configure nodes");

        //details.add(b);
        
        //b.addActionListener(new NodeConfigAction());

        // Add an extra ActionListener to make option 2
        // dependant on option 1 choice

        /*opt1Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableOpt2(opt1Box.getSelectedItem());
            }
        });*/

        //opt1Box.setToolTipText("Choose RFID reader type");

        //enableOpt2(opt1Box.getSelectedItem());
    }

    /*private void enableOpt2(Object o) {
        boolean enable = o.equals("MERG Concentrator");
        opt2BoxLabel.setEnabled(enable);
        opt2Box.setEnabled(enable);
        opt2Box.setToolTipText(enable?
            "Choose RFID concentrator range setting":
            "Range setting not applicable for selected RFID reader type");
    }*/

    public String name() { return "RFID Device Connection"; }

    //public boolean isOptList1Advanced() { return false; }

    //public boolean isOptList2Advanced() { return false; }
    
    protected void setInstance() {
        if (adapter==null)
            adapter = new SerialDriverAdapter();
    }
}


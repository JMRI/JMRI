package jmri.jmrix.cmri.serial.sim;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction;

/**
 * Definition of objects to handle configuring a layout connection via an C/MRI
 * Simulator object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008
 * @author Chuck Catania Copyright (C) 2017
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
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

        setInstance();

        // have to embed the usual one in a new JPanel

        JPanel p = new JPanel();
        super.loadDetails(p);

        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(p);

        // add another button
        JButton b = new JButton(Bundle.getMessage("ConfigureNodesTitle"));

        details.add(b);

        b.addActionListener(new NodeConfigManagerAction((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo())); //c2
//        b.addActionListener(new NodeConfigAction((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()));

    }

    @Override
    public String name() {
        return "Simulator";
    }

    @Override
    protected void setInstance() {
        if(adapter == null ) {
           adapter = new SimDriverAdapter();
           adapter.configure(); // make sure the traffic controller 
                                // loads so that node details can be 
                                // saved.
        }
    }
}

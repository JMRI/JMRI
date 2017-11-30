package jmri.jmrix.secsi.serialdriver;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.secsi.nodeconfig.NodeConfigAction;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Definition of objects to handle configuring a SECSI layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007
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
    public void loadDetails(JPanel details) {
        // have to embed the usual one in a new JPanel

        JPanel p = new JPanel();
        super.loadDetails(p);

        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(p);

        // add another button
        JButton b = new JButton("Configure nodes");

        details.add(b);

        b.addActionListener(new NodeConfigAction((SecsiSystemConnectionMemo)adapter.getSystemConnectionMemo()));

    }

    @Override
    public String name() {
        return "SECSI Layout Bus";
    }

    @Override
    protected void setInstance() {
        if(adapter == null ) {
           adapter = new SerialDriverAdapter();
        }
    }

}

// NodeConfigToolFrame.java

package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Create Frame containing NodeConfig tool frame
 *
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision$
 * @since 2.3.1
 */
public class NodeConfigToolFrame extends jmri.util.JmriJFrame {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle");
    
    NodeConfigToolPane pane;
    
    public NodeConfigToolFrame() {
        this(ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle").getString("Title"));
    }
    
    public NodeConfigToolFrame(String Name) {
        super(Name);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        pane = new NodeConfigToolPane();
        getContentPane().add(pane);
        
        // add help
    	addHelpMenu("package.jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolFrame", true);
        
        // prep for display
        pack();
    }



    public void dispose() {
        // disconnect the config pane from the CBUS
        pane.dispose();

        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Logger.getLogger(NodeConfigToolFrame.class.getName());
}

// NodeTableFrame.java

package jmri.jmrix.grapevine.nodetable;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.Border;


/**
 * Frame for user configuration of serial nodes
 * @author	Bob Jacobsen   Copyright (C) 2004, 2007
 * @author	Dave Duchamp   Copyright (C) 2004, 2006
 * @version	$Revision: 1.1 $
 */
public class NodeTableFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.grapevine.nodetable.NodeTableBundle");

		
    /**
     * Constructor method
     */
    public NodeTableFrame() {
    	super();
    }

    /** 
     *  Initialize the window
     */
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));
			
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			

        // add table
        NodeTablePane p = new NodeTablePane();
        p.initComponents();
        contentPane.add(p);
        
        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.grapevine.nodetable.NodeTableFrame", true);

        // pack for display
        pack();
    }

}

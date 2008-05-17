// AlignTableFrame.java

package jmri.jmrix.rps.aligntable;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * Frame for user configuration of serial nodes
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class AlignTableFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");

		
    /**
     * Constructor method
     */
    public AlignTableFrame() {
    	super();
    }

    AlignTablePane p;
    
    /** 
     *  Initialize the window
     */
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));
			
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			

        // add table
        p = new AlignTablePane();
        p.initComponents();
        contentPane.add(p);
        
        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.rps.aligntable.AlignTableFrame", true);

        // register
        // SerialTrafficController.instance().addSerialListener(p);
        
        // pack for display
        pack();
    }

    public void dispose() {
        // SerialTrafficController.instance().removeSerialListener(p);
    }
}

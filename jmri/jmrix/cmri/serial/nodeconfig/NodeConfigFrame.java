// NodeConfigFrame.java

package jmri.jmrix.cmri.serial.nodeconfig;

import javax.swing.*;

/**
 * Frame for user configuration of CMRI serial nodes
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @version	$Revision: 1.1 $
 */
public class NodeConfigFrame extends javax.swing.JFrame {

    public NodeConfigFrame() {
    	super();
    }

    public void initComponents() {




		this.getContentPane().add(new JButton("Push me!"));



        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    thisWindowClosing(e);
                }
            });

        // pack for display
        pack();
    }


    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }
}

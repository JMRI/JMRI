// TreeFrame.java
package jmri.jmrix.jinput.treecontrol;

import java.awt.Container;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for controlling JInput access to USN
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public class TreeFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 5687145517875452389L;
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.jinput.treecontrol.TreeBundle");

    @Override
    public void initComponents() throws Exception {

        // set the frame's initial state
        setTitle(rb.getString("WindowTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // add only content pane 
        contentPane.add(new TreePanel());

        // add help menu
        addHelpMenu("package.jmri.jmrix.jinput.treecontrol.TreeFrame", true);

        // pack for display
        pack();
    }

    private final static Logger log = LoggerFactory.getLogger(TreeFrame.class.getName());

}

/* @(#)TreeFrame.java */

// PowerPanelFrame.java

 package jmri.jmrit.powerpanel;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JMenuBar;

import jmri.util.JmriJFrame;

/**
 * Frame for controlling layout power via a PowerManager.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version             $Revision: 1.5 $
 */
public class PowerPanelFrame extends JmriJFrame {

    // GUI member declarations
    PowerPane pane	= new PowerPane();

    public PowerPanelFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle").getString("TitlePowerPanel"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);
        pack();
    }

    // handle resizing when first shown
    private boolean mShown = false;
    public void addNotify() {
        super.addNotify();
        if (mShown)
            return;
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        pane.dispose();
        dispose();
	// and disconnect from the SlotManager
    }
}

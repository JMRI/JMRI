// LoaderFrame.java

package jmri.jmrix.loconet.downloader;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JMenuBar;

import jmri.util.JmriJFrame;

/**
 * Frame for downloading (mangled) .hex files
 *
 * This is just an enclosure for the LoaderPane, which does the real work.
 *
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version             $Revision: 1.1 $
 */
public class LoaderFrame extends JmriJFrame {

    // GUI member declarations
    LoaderPane pane	= new LoaderPane();

    public LoaderFrame() {
        super(ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.Loader").getString("TitleLoader"));
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

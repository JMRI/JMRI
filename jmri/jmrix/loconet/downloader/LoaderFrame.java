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
 * @version             $Revision: 1.3 $
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

    public void dispose() {
        pane.dispose();
    }
    
    // Close the window when the close box is clicked
    public void windowClosing(java.awt.event.WindowEvent e) {
        // AJS - This does not seem to be called when the window closes
        if(pane.abortButton.isEnabled())
            pane.setOperationAborted(true);
        
        super.windowClosing(e);
    }
}

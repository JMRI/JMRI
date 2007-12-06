// LoaderFrame.java

package jmri.jmrix.loconet.soundloader;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JMenuBar;

import jmri.util.JmriJFrame;

/**
 * Frame for downloading Digitrax SPJ files to sound decoders
 *
 * This is just an enclosure for the LoaderPane, which does the real work.
 *
 * @author		Bob Jacobsen   Copyright (C) 2006
 * @version             $Revision: 1.4 $
 */
public class LoaderFrame extends JmriJFrame {

    // GUI member declarations
    LoaderPane pane	= new LoaderPane();

    public LoaderFrame() {
        super(ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Loader").getString("TitleLoader"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.loconet.soundloader.LoaderFrame", true);

        pack();
    }

    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}

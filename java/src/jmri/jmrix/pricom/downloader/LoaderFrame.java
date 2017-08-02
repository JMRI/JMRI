package jmri.jmrix.pricom.downloader;

import java.util.ResourceBundle;
import jmri.util.JmriJFrame;

/**
 * Frame for downloading (mangled) .hex files
 *
 * This is just an enclosure for the LoaderPane, which does the real work.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public class LoaderFrame extends JmriJFrame {

    // GUI member declarations
    LoaderPane pane = new LoaderPane();

    public LoaderFrame() {
        super(ResourceBundle.getBundle("jmri.jmrix.pricom.downloader.Loader").getString("TitleLoader"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);
        pack();
    }

    // Clean up this window
    @Override
    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}

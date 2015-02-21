// LoaderFrame.java
package jmri.jmrix.pricom.downloader;

import java.util.ResourceBundle;
import jmri.util.JmriJFrame;

/**
 * Frame for downloading (mangled) .hex files
 *
 * This is just an enclosure for the LoaderPane, which does the real work.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version $Revision$
 */
public class LoaderFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -2659741505895230693L;
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
    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}

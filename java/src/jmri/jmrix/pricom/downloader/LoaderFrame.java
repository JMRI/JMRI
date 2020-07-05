package jmri.jmrix.pricom.downloader;

import jmri.util.JmriJFrame;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Frame for downloading (mangled) .hex files
 * <p>
 * This is just an enclosure for the LoaderPane, which does the real work.
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
@API(status = EXPERIMENTAL)
public class LoaderFrame extends JmriJFrame {

    // GUI member declarations
    LoaderPane pane = new LoaderPane();

    public LoaderFrame() {
        super(Bundle.getMessage("TitleLoader"));
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

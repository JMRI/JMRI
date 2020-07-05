package jmri.jmrit.powerpanel;

import jmri.util.JmriJFrame;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Frame for controlling layout power via a PowerManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = MAINTAINED)
public class PowerPanelFrame extends JmriJFrame {

    // GUI member declarations
    PowerPane pane = new PowerPane();

    public PowerPanelFrame() {
        super(Bundle.getMessage("TitlePowerPanel"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.powerpanel.PowerPanelFrame", true);

        pack();
    }

    @Override
    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}

// PowerPanelFrame.java
package jmri.jmrit.powerpanel;

import java.util.ResourceBundle;
import jmri.util.JmriJFrame;

/**
 * Frame for controlling layout power via a PowerManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version $Revision$
 */
public class PowerPanelFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -3373655770765350662L;
    // GUI member declarations
    PowerPane pane = new PowerPane();

    public PowerPanelFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle").getString("TitlePowerPanel"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.powerpanel.PowerPanelFrame", true);

        pack();
    }

    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}

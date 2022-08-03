package jmri.jmrix.can.cbus.swing.power;

import jmri.util.JmriJFrame;

/**
 * Frame for controlling CBUS programming track power via a PowerManager.
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerPanelFrame extends JmriJFrame {
    
    // GUI member declarations
    CbusProgPowerPane pane = new CbusProgPowerPane();

    public CbusProgPowerPanelFrame() {
        super(Bundle.getMessage("MenuItemProgTrackPower"));
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

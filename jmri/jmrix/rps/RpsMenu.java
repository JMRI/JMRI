// RpsMenu.java

package jmri.jmrix.rps;

import javax.swing.JMenu;

/**
 * Create a "RPS" menu containing the Jmri RPS-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2006
 * @version     $Revision: 1.1 $
 */
public class RpsMenu extends JMenu {
    public RpsMenu(String name) {
        this();
        setText(name);
    }

    public RpsMenu() {

        super();

        setText("RPS");  // Product name, not translated.

        add(new jmri.jmrix.rps.rpsmon.RpsMonAction());
        add(new jmri.jmrix.rps.display.DisplayAction());
        add(new jmri.jmrix.rps.DataSourceAction());
        add(new jmri.jmrix.rps.trackingpanel.RpsTrackingFrameAction());
        add(new jmri.jmrix.rps.reversealign.AlignmentPanelAction());

    }

}



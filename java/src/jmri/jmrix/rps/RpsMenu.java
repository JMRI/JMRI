package jmri.jmrix.rps;

import javax.swing.JMenu;
import javax.swing.JSeparator;

/**
 * Create a "RPS" menu containing the Jmri RPS-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2006, 2007, 2008
 */
public class RpsMenu extends JMenu {

    public RpsMenu(String name) {
        this();
        setText(name);
    }

    public RpsMenu() {

        super();

        setText("RPS");  // Product name, not translated.

        // tools that work
        add(new jmri.jmrix.rps.rpsmon.RpsMonAction());
        add(new jmri.jmrix.rps.aligntable.AlignTableAction());
        add(new jmri.jmrix.rps.swing.polling.PollTableAction());
        add(new jmri.jmrix.rps.swing.debugger.DebuggerAction());
        add(new jmri.jmrix.rps.trackingpanel.RpsTrackingFrameAction());
        add(new jmri.jmrix.rps.swing.soundset.SoundSetAction());

        add(new JSeparator());

        // old, obsolete or not updated tools
        add(new jmri.jmrix.rps.reversealign.AlignmentPanelAction());

    }

}

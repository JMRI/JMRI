package jmri.jmrix.rps;

import javax.swing.JMenu;
import javax.swing.JSeparator;

/**
 * Create an "RPS" menu containing the Jmri RPS-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2006, 2007, 2008
 */
public class RpsMenu extends JMenu {

    private RpsSystemConnectionMemo _memo = null;

    public RpsMenu(String name, RpsSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public RpsMenu(RpsSystemConnectionMemo memo) {

        super();
        _memo = memo;

        setText("RPS");  // Product name, not translated.

        // tools that work
        add(new jmri.jmrix.rps.rpsmon.RpsMonAction(_memo));
        add(new jmri.jmrix.rps.aligntable.AlignTableAction(_memo));
        add(new jmri.jmrix.rps.swing.polling.PollTableAction(_memo));
        add(new jmri.jmrix.rps.swing.debugger.DebuggerAction(_memo));
        add(new jmri.jmrix.rps.trackingpanel.RpsTrackingFrameAction(_memo));
        add(new jmri.jmrix.rps.swing.soundset.SoundSetAction(_memo));

        add(new JSeparator());

        // old, obsolete or not updated tools
        add(new jmri.jmrix.rps.reversealign.AlignmentPanelAction(_memo));
    }

}

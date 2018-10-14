package jmri.jmrix.rps.swing.soundset;

import javax.swing.BoxLayout;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Frame for controlling sound-speed calculation for RPS system.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class SoundSetFrame extends jmri.util.JmriJFrame {

    RpsSystemConnectionMemo memo = null;

    public SoundSetFrame(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;
        setTitle(title());
    }

    protected String title() {
        return "RPS Sound Speed Control";
    }  // product name, not translated

    SoundSetPane pane;

    @Override
    public void dispose() {
        if (pane != null) {
            pane.dispose();
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public void initComponents() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add pane
        pane = new SoundSetPane();
        pane.initComponents();
        getContentPane().add(pane);

        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.soundset.SoundSetFrame", true);

        // prepare for display
        pack();
    }
}

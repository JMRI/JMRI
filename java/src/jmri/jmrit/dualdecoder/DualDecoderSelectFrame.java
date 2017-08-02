package jmri.jmrit.dualdecoder;

/**
 * Frame for user dual-decoder select too. This allows the user (person) to
 * select an active decoder from multiple ones in a loco
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class DualDecoderSelectFrame extends jmri.util.JmriJFrame {

    public DualDecoderSelectFrame() {
        this("Multi-Decoder Control");
    }

    public DualDecoderSelectFrame(String title) {
        super(title);
        getContentPane().add(new DualDecoderSelectPane());

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.dualdecoder.DualDecoderSelectFrame", true);

        pack();
    }
}

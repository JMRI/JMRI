// DualDecoderSelectFrame.java
package jmri.jmrit.dualdecoder;

/**
 * Frame for user dual-decoder select too. This allows the user (person) to
 * select an active decoder from multiple ones in a loco
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class DualDecoderSelectFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1029903266811472795L;

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

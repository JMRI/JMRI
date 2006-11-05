// DualDecoderSelectFrame.java

package jmri.jmrit.dualdecoder;


/**
 * Frame for user dual-decoder select too.
 * This allows the user (person) to select an active decoder from multiple ones in a loco
 * @author   Bob Jacobsen   Copyright (C) 2003
 * @version  $Revision: 1.2 $
 */
public class DualDecoderSelectFrame extends javax.swing.JFrame {

    public DualDecoderSelectFrame() {
        getContentPane().add(new DualDecoderSelectPane());
        pack();
    }
}

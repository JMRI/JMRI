package jmri.jmrit.logixng.tools.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to show the inline LogixNGs.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class InlineLogixNGsAction extends AbstractAction {

    public InlineLogixNGsAction(String s) {
        super(s);
    }

    public InlineLogixNGsAction() {
        this(Bundle.getMessage("MenuInlineLogixNGs")); // NOI18N
    }

    private static InlineLogixNGsFrame inlineLogixNGsFrame = null;

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Only one ImportLogixFrame")
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (inlineLogixNGsFrame == null || !inlineLogixNGsFrame.isVisible()) {
            inlineLogixNGsFrame = new InlineLogixNGsFrame();
            inlineLogixNGsFrame.initComponents();
        }
        inlineLogixNGsFrame.setExtendedState(Frame.NORMAL);
        inlineLogixNGsFrame.setVisible(true); // this also brings the frame into focus
    }

}

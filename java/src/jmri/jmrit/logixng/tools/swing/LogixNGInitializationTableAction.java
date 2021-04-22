package jmri.jmrit.logixng.tools.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LogixNGEditor object.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class LogixNGInitializationTableAction extends AbstractAction {

    public LogixNGInitializationTableAction(String s) {
        super(s);
    }

    public LogixNGInitializationTableAction() {
        this(Bundle.getMessage("MenuLogixNGInitializationTable")); // NOI18N
    }

    static ImportLogixFrame importLogixFrame = null;

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Only one ImportLogixFrame")
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (importLogixFrame == null || !importLogixFrame.isVisible()) {
            importLogixFrame = new ImportLogixFrame();
            importLogixFrame.initComponents();
        }
        importLogixFrame.setExtendedState(Frame.NORMAL);
        importLogixFrame.setVisible(true); // this also brings the frame into focus
    }
    
}

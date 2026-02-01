package jmri.jmrit.operations.setup.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to open a window that allows a user to edit the day to name mapping.
 *
 * @author Daniel Boudreau Copyright (C) 2026
 * 
 */
public class EditDayToNameMapAction extends AbstractAction {

    public EditDayToNameMapAction() {
        super(Bundle.getMessage("TitleDayToNameMap"));
    }

    EditDayToNameMapFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (f == null || !f.isVisible()) {
            f = new EditDayToNameMapFrame();
            f.initComponents();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}



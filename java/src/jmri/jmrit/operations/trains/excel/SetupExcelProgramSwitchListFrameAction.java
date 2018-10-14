package jmri.jmrit.operations.trains.excel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create a SetupExcelProgramSwitchListFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class SetupExcelProgramSwitchListFrameAction extends AbstractAction {

    public SetupExcelProgramSwitchListFrameAction(String s) {
        super(s);
    }

    SetupExcelProgramSwitchListFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new SetupExcelProgramSwitchListFrame();
        f.initComponents();
        f.setExtendedState(Frame.NORMAL);
    }
}



package jmri.jmrit.operations.trains.excel;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to launch the SetupExcelProgramFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
@API(status = MAINTAINED)
public class SetupExcelProgramFrameAction extends AbstractAction {

    public SetupExcelProgramFrameAction() {
        super(Bundle.getMessage("MenuItemSetupExcelProgram"));
    }

    SetupExcelProgramManifestFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new SetupExcelProgramManifestFrame();
        f.initComponents();
        f.setExtendedState(Frame.NORMAL);
    }
}



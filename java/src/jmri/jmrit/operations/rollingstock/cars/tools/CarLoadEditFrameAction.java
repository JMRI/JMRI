package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create a CarLoadedEditFrame
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2023
 */
public class CarLoadEditFrameAction extends AbstractAction {

    public CarLoadEditFrameAction() {
        super(Bundle.getMessage("TitleCarEditLoad"));
    }

    CarLoadEditFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
        if (f == null || !f.isVisible()) {
            f = new CarLoadEditFrame();
        }
        f.initComponents(null, null);
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}



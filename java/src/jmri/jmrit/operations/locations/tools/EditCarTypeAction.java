package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.rollingstock.cars.tools.CarAttributeEditFrame;

/**
 * Swing action to create and register a LocationCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 */
public class EditCarTypeAction extends AbstractAction {

    public EditCarTypeAction() {
        super(Bundle.getMessage("MenuItemEditCarType"));
    }

    CarAttributeEditFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
        if (f == null || !f.isVisible()) {
            f = new CarAttributeEditFrame();
        }
        f.initComponents(CarAttributeEditFrame.TYPE, null);
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}



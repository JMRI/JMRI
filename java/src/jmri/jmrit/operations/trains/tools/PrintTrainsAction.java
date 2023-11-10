package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.TrainsTableFrame;

/**
 * Action to print a summary of each train in operations.
 * <p>
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2009, 2014, 2023
 */
public class PrintTrainsAction extends AbstractAction {

    public PrintTrainsAction(boolean isPreview, TrainsTableFrame trainsTableFrame) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _trainsTableFrame = trainsTableFrame;
    }

    boolean _isPreview;
    TrainsTableFrame _trainsTableFrame;

    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintTrainsFrame(_isPreview, _trainsTableFrame);
    }
}

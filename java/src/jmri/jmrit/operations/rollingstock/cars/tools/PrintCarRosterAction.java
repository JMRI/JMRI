package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

/**
 * Action to print a summary of the Roster contents
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * MacIntosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013, 2014
 */
public class PrintCarRosterAction extends AbstractAction {

    public PrintCarRosterAction(boolean isPreview, CarsTableFrame carsTableFrame) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _carsTableFrame = carsTableFrame;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean _isPreview;
    CarsTableFrame _carsTableFrame;
    PrintCarRosterFrame pcrf = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (pcrf == null) {
            pcrf = new PrintCarRosterFrame(_isPreview, _carsTableFrame);
        } else {
            pcrf.setVisible(true);
        }
    }
}

package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;

/**
 * Action to print a summary of the Location Roster contents
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012, 2014, 2022
 */
public class PrintLocationsAction extends AbstractAction {

    public PrintLocationsAction(boolean isPreview) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
    }

    public PrintLocationsAction(boolean isPreview, Location location) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _location = location;
    }

    boolean _isPreview;
    Location _location = null;
    PrintLocationsFrame lpof = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (lpof == null) {
            lpof = new PrintLocationsFrame(_isPreview, _location);
        } else {
            lpof.setVisible(true);
        }
        lpof.initComponents();
    }
}

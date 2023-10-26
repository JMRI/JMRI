package jmri.jmrit.operations.routes.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.routes.Route;

/**
 * Action to print a summary of a route.
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2009, 2012, 2023
 */
public class PrintRouteAction extends AbstractAction {

    public PrintRouteAction(boolean isPreview, Route route) {
        super(isPreview? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _route = route;
    }

    boolean _isPreview;
    Route _route;

    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintRoutes(_isPreview, _route);
    }
}

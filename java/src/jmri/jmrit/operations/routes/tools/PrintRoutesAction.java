package jmri.jmrit.operations.routes.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action to print all of the routes used in operations.
 *
 * @author Daniel Boudreau Copyright (C) 2012, 2023
 */
public class PrintRoutesAction extends AbstractAction {

    public PrintRoutesAction(boolean isPreview) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
    }

    boolean _isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintRoutes(_isPreview);
    }
}

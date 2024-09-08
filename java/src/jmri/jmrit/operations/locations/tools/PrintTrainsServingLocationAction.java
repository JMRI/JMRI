package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to preview or print trains serving a location.
 *
 * @author Daniel Boudreau Copyright (C) 2024
 */
public class PrintTrainsServingLocationAction extends AbstractAction {

    public PrintTrainsServingLocationAction(ShowTrainsServingLocationFrame stslf, boolean isPreview) {
        super(isPreview? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        this.stslf = stslf;
        this.isPreview = isPreview;
    }

    ShowTrainsServingLocationFrame stslf;
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintTrainsServingLocation(isPreview, stslf._location, stslf._track, stslf._carType);
    }
}



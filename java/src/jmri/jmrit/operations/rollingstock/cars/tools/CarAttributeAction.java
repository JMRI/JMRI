package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
final class CarAttributeAction extends AbstractAction {

    public CarAttributeAction(CarAttributeEditFrame caef) {
        super(Bundle.getMessage("CarQuantity"));
        this.caef = caef;
    }

    CarAttributeEditFrame caef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        caef.toggleShowQuanity();
    }
}

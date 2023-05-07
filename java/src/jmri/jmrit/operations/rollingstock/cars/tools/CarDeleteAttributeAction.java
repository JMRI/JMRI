package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

final class CarDeleteAttributeAction extends AbstractAction {

    public CarDeleteAttributeAction(CarAttributeEditFrame caef) {
        super(Bundle.getMessage("DeleteUnusedAttributes"));
        this.caef = caef;
    }

    CarAttributeEditFrame caef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        caef.deleteUnusedAttributes();
    }
}

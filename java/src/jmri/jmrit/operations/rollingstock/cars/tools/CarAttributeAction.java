package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
final class CarAttributeAction extends AbstractAction {

    public CarAttributeAction(String actionName, CarAttributeEditFrame caef) {
        super(actionName);
        this.caef = caef;
    }

    CarAttributeEditFrame caef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("Show attribute quanity");
        caef.toggleShowQuanity();
    }

    private final static Logger log = LoggerFactory.getLogger(CarAttributeAction.class);
}

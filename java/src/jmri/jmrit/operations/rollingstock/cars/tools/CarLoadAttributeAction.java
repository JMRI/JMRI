package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2010, 2011
 */
final class CarLoadAttributeAction extends AbstractAction {

    public CarLoadAttributeAction(String actionName, CarLoadEditFrame clef) {
        super(actionName);
        this.clef = clef;
    }

    CarLoadEditFrame clef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("Show attribute quanity");
        clef.toggleShowQuanity();
    }

    private final static Logger log = LoggerFactory.getLogger(CarAttributeEditFrame.class);
}

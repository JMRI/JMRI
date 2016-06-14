// CarAttributeEditFrame.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CarDeleteAttributeAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6118522896008395241L;

    public CarDeleteAttributeAction(String actionName, CarAttributeEditFrame caef) {
        super(actionName);
        this.caef = caef;
    }

    CarAttributeEditFrame caef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("Delete unused attributes");
        caef.deleteUnusedAttributes();
    }

    private final static Logger log = LoggerFactory.getLogger(CarDeleteAttributeAction.class.getName());
}

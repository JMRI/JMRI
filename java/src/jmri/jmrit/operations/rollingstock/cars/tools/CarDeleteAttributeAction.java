package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = MAINTAINED)
final class CarDeleteAttributeAction extends AbstractAction {

    public CarDeleteAttributeAction(CarAttributeEditFrame caef) {
        super(Bundle.getMessage("DeleteUnusedAttributes"));
        this.caef = caef;
    }

    CarAttributeEditFrame caef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("Delete unused attributes");
        caef.deleteUnusedAttributes();
    }

    private final static Logger log = LoggerFactory.getLogger(CarDeleteAttributeAction.class);
}

package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
final class EngineAttributeAction extends AbstractAction {

    public EngineAttributeAction(EngineAttributeEditFrame caef) {
        super(Bundle.getMessage("EngineQuantity"));
        this.eaef = caef;
    }

    EngineAttributeEditFrame eaef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        eaef.toggleShowQuanity();
    }
}

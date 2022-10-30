package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

final class EngineDeleteAttributeAction extends AbstractAction {

    public EngineDeleteAttributeAction(EngineAttributeEditFrame caef) {
        super(Bundle.getMessage("DeleteUnusedAttributes"));
        this.eaef = caef;
    }

    EngineAttributeEditFrame eaef;

    @Override
    public void actionPerformed(ActionEvent ae) {
        eaef.deleteUnusedAttributes();
    }
}

package jmri.jmrit.operations.rollingstock.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Acton for changing the default attribute character length
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public final class AttributeCharacterLengthAction extends AbstractAction {

    public AttributeCharacterLengthAction() {
        super(Bundle.getMessage("ChangeCharLength"));
    }
    
    private AttributeCharacterLengthFrame aclf;

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (aclf != null) {
            aclf.dispose();
        }
        aclf = new AttributeCharacterLengthFrame();
    }
}

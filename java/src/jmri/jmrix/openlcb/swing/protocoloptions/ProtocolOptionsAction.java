package jmri.jmrix.openlcb.swing.protocoloptions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Created by bracz on 11/24/18.
 */

class ProtocolOptionsAction extends AbstractAction {

    final CanSystemConnectionMemo sc;

    ProtocolOptionsAction(CanSystemConnectionMemo sc) {
        super(Bundle.getMessage("WindowTitle"));
        this.sc = sc;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // @todo create and open frame for setting the properties.

    }
}

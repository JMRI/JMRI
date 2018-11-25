package jmri.jmrix.openlcb.swing.protocoloptions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JmriJFrame;

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
        JmriJFrame f = new ProtocolOptionsFrame(sc);
        f.initComponents();
        f.setVisible(true);
    }
}

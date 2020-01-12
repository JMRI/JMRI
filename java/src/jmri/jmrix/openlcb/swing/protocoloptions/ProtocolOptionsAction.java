package jmri.jmrix.openlcb.swing.protocoloptions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JmriJFrame;

/**
 * Invokeable action to open the protocol settings frame.
 *
 * @author Balazs Racz, (C) 2018.
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

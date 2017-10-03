package jmri.jmrix.easydcc.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a EasyDccPacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class EasyDccPacketGenAction extends AbstractAction {

    public EasyDccPacketGenAction(String s) {
        super(s);
    }

    public EasyDccPacketGenAction() {
        this("Generate EasyDCC message");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EasyDccPacketGenFrame f = new EasyDccPacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(EasyDccPacketGenAction.class);
}

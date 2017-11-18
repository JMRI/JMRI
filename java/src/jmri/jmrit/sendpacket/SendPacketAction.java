package jmri.jmrit.sendpacket;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SendPacketFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class SendPacketAction extends AbstractAction {

    public SendPacketAction(String s) {
        super(s);

        // disable ourself if there is no command Station object available
        if (jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class) == null) {
            setEnabled(false);
        }
    }

    public SendPacketAction() {
        this(Bundle.getMessage("SendPacketTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SendPacketFrame
        SendPacketFrame f = new SendPacketFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SendPacketAction.class);

}

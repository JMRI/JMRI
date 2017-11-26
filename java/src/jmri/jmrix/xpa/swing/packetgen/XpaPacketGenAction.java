package jmri.jmrix.xpa.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.xpa.XpaSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an XpaPacketGenFrame object
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class XpaPacketGenAction extends AbstractAction {

    XpaSystemConnectionMemo memo = null;

    public XpaPacketGenAction(String s,XpaSystemConnectionMemo m) {
        super(s);
        memo = m;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        XpaPacketGenFrame f = new XpaPacketGenFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(XpaPacketGenAction.class);

}

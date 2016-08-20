package jmri.jmrix.xpa.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.xpa.XpaSystemConnectionMemo;

/**
 * Swing action to create and register a XpaPacketGenFrame object
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class XpaPacketGenAction extends AbstractAction {

    XpaSystemConnectionMemo memo = null;

    public XpaPacketGenAction(String s,XpaSystemConnectionMemo m) {
        super(s);
        memo = m;
    }

    public void actionPerformed(ActionEvent e) {
        XpaPacketGenFrame f = new XpaPacketGenFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(XpaPacketGenAction.class.getName());
}


/* @(#)XpaPacketGenAction.java */

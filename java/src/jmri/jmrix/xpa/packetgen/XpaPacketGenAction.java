// XpaPacketGenAction.java
package jmri.jmrix.xpa.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a XpaPacketGenFrame object
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
 */
public class XpaPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 832569728819734426L;

    public XpaPacketGenAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        XpaPacketGenFrame f = new XpaPacketGenFrame();
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

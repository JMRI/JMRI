// SprogPacketGenAction.java
package jmri.jmrix.sprog.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogPacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 4279427035479187090L;

    public SprogPacketGenAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        SprogPacketGenFrame f = new SprogPacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SprogPacketGenAction.class.getName());
}


/* @(#)SprogPacketGenAction.java */

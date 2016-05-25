// QsiPacketGenAction.java
package jmri.jmrix.qsi.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 * @version	$Revision$
 */
public class PacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6640550274313020857L;

    public PacketGenAction(String s) {
        super(s);
    }

    public PacketGenAction() {
        this(java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle")
                .getString("MenuItemSendCommand"));
    }

    public void actionPerformed(ActionEvent e) {
        PacketGenFrame f = new PacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class.getName());
}


/* @(#)PacketGenAction.java */

// SerialPacketGenAction.java
package jmri.jmrix.powerline.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.powerline.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialPacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SerialPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6499014957221691885L;

    public SerialPacketGenAction(String s, SerialTrafficController tc) {
        super(s);
        this.tc = tc;
    }

    public SerialPacketGenAction(SerialTrafficController tc) {
        this("Send powerline device message", tc);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    public void actionPerformed(ActionEvent e) {
        SerialPacketGenFrame f = new SerialPacketGenFrame(tc);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */

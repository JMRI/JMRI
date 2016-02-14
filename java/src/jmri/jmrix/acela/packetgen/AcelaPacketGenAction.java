// AcelaPacketGenAction.java
package jmri.jmrix.acela.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an AcelaPacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 7642432788366749061L;

    public AcelaPacketGenAction(String s) {
        super(s);
    }

    public AcelaPacketGenAction() {
        this("Generate Acela message");
    }

    public void actionPerformed(ActionEvent e) {
        AcelaPacketGenFrame f = new AcelaPacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaPacketGenAction.class.getName());
}

/* @(#)AcelaPacketGenAction.java */

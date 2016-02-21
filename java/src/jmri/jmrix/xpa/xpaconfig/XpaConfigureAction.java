// XpaConfigureAction.java
package jmri.jmrix.xpa.xpaconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a XpaConfigureFrame object
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
 */
public class XpaConfigureAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 5845992931132270975L;

    public XpaConfigureAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        XpaConfigureFrame f = new XpaConfigureFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(XpaConfigureAction.class.getName());
}


/* @(#)XpaPacketGenAction.java */

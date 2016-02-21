// SignallingAction.java
package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignallingFrame object
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @version	$Revision$
 */
public class SignallingAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -9089310555904109131L;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    public SignallingAction(String s) {
        super(s);
    }

    public SignallingAction() {
        super(rb.getString("SignallingPairs"));
    }

    public void setMast(jmri.SignalMast source, jmri.SignalMast dest) {
        this.source = source;
        this.dest = dest;

    }

    jmri.SignalMast source = null;
    jmri.SignalMast dest = null;

    public void actionPerformed(ActionEvent e) {
        SignallingFrame f = new SignallingFrame();
        try {
            f.initComponents(source, dest);
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
            ex.printStackTrace();
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SignallingAction.class.getName());
}


/* @(#)SignallingAction.java */

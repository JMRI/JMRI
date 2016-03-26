// SignallingSourceAction.java
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
public class SignallingSourceAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 5614597834004934713L;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    public SignallingSourceAction(String s) {
        super(s);
    }

    public SignallingSourceAction(String s, jmri.SignalMast source) {
        super(s + " : " + source.getDisplayName());
        this.source = source;
    }

    public SignallingSourceAction() {
        super(rb.getString("SignallingPairs"));
    }

    public void setMast(jmri.SignalMast source) {
        this.source = source;
    }

    jmri.SignalMast source = null;

    public void actionPerformed(ActionEvent e) {
        SignallingSourceFrame f = new SignallingSourceFrame();
        try {
            f.initComponents(source);
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
            ex.printStackTrace();
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SignallingSourceAction.class.getName());
}


/* @(#)SignallingSourceAction.java */

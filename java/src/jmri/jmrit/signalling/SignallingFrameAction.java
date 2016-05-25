// SignallingFrameAction.java
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
public class SignallingFrameAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8722069814853245097L;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    public SignallingFrameAction(String s) {
        super(s);
    }

    public SignallingFrameAction() {
        super(rb.getString("SignallingPairs"));
    }

    public void actionPerformed(ActionEvent e) {
        SignallingFrame f = new SignallingFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
            ex.printStackTrace();
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SignallingFrameAction.class.getName());
}


/* @(#)SignallingFrameAction.java */

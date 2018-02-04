package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignallingFrame object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingAction extends AbstractAction {

    public SignallingAction(String s) {
        super(s);
    }

    public SignallingAction() {
        super(Bundle.getMessage("SignallingPairs"));  // NOI18N
    }

    public void setMast(jmri.SignalMast source, jmri.SignalMast dest) {
        this.source = source;
        this.dest = dest;
    }

    jmri.SignalMast source = null;
    jmri.SignalMast dest = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        SignallingFrame f = new SignallingFrame();
        try {
            f.initComponents(source, dest);
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SignallingAction.class);
}

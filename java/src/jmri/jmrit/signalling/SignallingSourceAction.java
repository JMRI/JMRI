package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignallingFrame object.
 * Displayed when user clicks Edit Logic button in the Signal Mast table.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingSourceAction extends AbstractAction {

    public SignallingSourceAction(String s) {
        super(s);
    }

    public SignallingSourceAction(String s, jmri.SignalMast source) {
        super(s + " : " + source.getDisplayName()); // set title of pane to include source mast name
        this.source = source;
    }

    public SignallingSourceAction() {
        super(Bundle.getMessage("SignallingPairs"));  // NOI18N
    }

    public void setMast(jmri.SignalMast source) {
        this.source = source;
    }

    jmri.SignalMast source = null;

    /**
     * Open a SignallingSourceFrame pane.
     * Displayed when user clicks Edit Logic button in the Signal Mast table.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SignallingSourceFrame f = new SignallingSourceFrame();
        try {
            f.initComponents(source);
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SignallingSourceAction.class);
}

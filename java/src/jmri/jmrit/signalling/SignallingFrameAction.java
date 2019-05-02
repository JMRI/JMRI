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
public class SignallingFrameAction extends AbstractAction {

    /**
     * Create an action with the supplied name.
     *
     * @param s The name of the resulting action
     */
    public SignallingFrameAction(String s) {
        super(s);
    }

    /**
     * Create an action with a preset name, localizable via the Bundle mechanism.
     */
    public SignallingFrameAction() {
        super(Bundle.getMessage("SignallingPairs"));  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SignallingFrame f = new SignallingFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SignallingFrameAction.class);
}

// PollTableAction.java
package jmri.jmrix.rps.swing.polling;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PollTableFrame object.
 * <p>
 * We only permit one, because notification is not entirely right yet.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class PollTableAction extends AbstractAction {

    public PollTableAction(String s) {
        super(s);
    }

    public PollTableAction() {
        this("RPS Polling Control");
    }

    PollTableFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        if (f == null) {
            f = new PollTableFrame();
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("Exception starting frame.", ex);
            }
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(PollTableAction.class.getName());

}


/* @(#)PollTableAction.java */

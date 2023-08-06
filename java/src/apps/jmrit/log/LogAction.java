package apps.jmrit.log;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LogFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class LogAction extends AbstractAction {

    public LogAction(String s) {
        super(s);
    }

    public LogAction() {
        this("Add Log Entry"); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LogFrame f = new LogFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception in startup", ex);
        }
        f.setVisible(true);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogAction.class);

}

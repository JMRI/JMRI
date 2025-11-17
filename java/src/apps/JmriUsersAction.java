package apps;

import java.awt.event.ActionEvent;
import javax.swing.*;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to open the JMRIusers site in a web browser
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2010, 2025
 */
public class JmriUsersAction extends jmri.util.swing.JmriAbstractAction {

    public JmriUsersAction() {
        super("License");
    }

    public JmriUsersAction(String s, Icon i, WindowInterface w) {
        super(s, i, w);
    }

    public JmriUsersAction(String s, WindowInterface w) {
        super(s, w);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            jmri.util.HelpUtil.openWebPage("https://groups.io/g/jmriusers");
        } catch (jmri.JmriException e) {
            log.error("failed to open page", e);
        }

    }

    @Override
    public JmriPanel makePanel() {
        // do nothing
        return null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriUsersAction.class);

}


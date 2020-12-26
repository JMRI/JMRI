package apps.swing;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import jmri.util.swing.*;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class AboutAction extends JmriAbstractAction {

    public AboutAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public AboutAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public AboutAction() {
        super("About");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new AboutDialog(null, true).setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    //private static final Logger log = LoggerFactory.getLogger(AboutAction.class);
}

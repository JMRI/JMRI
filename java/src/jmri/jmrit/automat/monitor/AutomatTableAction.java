package jmri.jmrit.automat.monitor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a AutomatTable GUI
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2008
 */
public class AutomatTableAction extends AbstractAction {

    public AutomatTableAction(String actionName) {
        super(actionName);
    }

    public AutomatTableAction() {
        this(
                java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle")
                .getString("MenuItemMonitor"));
    }

    AutomatTableDataModel m;

    AutomatTableFrame f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the frame
        f = new AutomatTableFrame(new AutomatTableDataModel());
        f.pack();
        f.setVisible(true);
    }

}

// AutomatTableAction.java
package jmri.jmrit.automat.monitor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a AutomatTable GUI
 *
 * @author	Bob Jacobsen Copyright (C) 2004, 2008
 * @version $Revision$
 */
public class AutomatTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 247180783423184258L;

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

    public void actionPerformed(ActionEvent e) {
        // create the frame
        f = new AutomatTableFrame(new AutomatTableDataModel());
        f.pack();
        f.setVisible(true);
    }

}
/* @(#)AutomatTableAction.java */

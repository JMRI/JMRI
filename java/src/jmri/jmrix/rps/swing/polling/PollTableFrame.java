// PollTableFrame.java
package jmri.jmrix.rps.swing.polling;

import javax.swing.JDialog;
import jmri.util.JmriJFrame;

/**
 * Frame for control of RPS polling
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class PollTableFrame extends JmriJFrame {

    PollTablePane pane;

    public PollTableFrame() {
        super(Bundle.getMessage("TitlePolling"));
    }

    public void dispose() {
        pane.dispose(); // drop table
        super.dispose();
    }

    public void initComponents() {
        // only one, so keep around on close
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        // create a table and add
        pane = new PollTablePane(this);
        getContentPane().add(pane);

        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.polling.PollTableFrame", true);

        // check at shutdown
        setShutDownTask();

        // prepare for display
        pack();
    }

    protected void storeValues() {
        pane.setDefaults();
        setModifiedFlag(false);
    }
}

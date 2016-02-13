// PollTableFrame.java
package jmri.jmrix.rps.swing.polling;

import java.util.ResourceBundle;
import javax.swing.JDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for control of RPS polling
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class PollTableFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 5780499097528063540L;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.swing.polling.PollingBundle");
    PollTablePane pane;

    public PollTableFrame() {
        super();
        setTitle(title());
    }

    protected String title() {
        return rb.getString("TitlePolling");
    }  // product name, not translated

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

    private final static Logger log = LoggerFactory.getLogger(PollTableFrame.class.getName());
}

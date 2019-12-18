package jmri.jmrix.rps.swing.polling;

import javax.swing.JDialog;
import jmri.util.JmriJFrame;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Frame for control of RPS polling.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class PollTableFrame extends JmriJFrame {

    RpsSystemConnectionMemo memo = null;

    PollTablePane pane;

    public PollTableFrame(RpsSystemConnectionMemo _memo) {
        super(Bundle.getMessage("TitlePolling"));
        memo = _memo;
    }

    @Override
    public void dispose() {
        if(pane!=null){
           pane.dispose(); // drop table
        }
        super.dispose();
    }

    @Override
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

    @Override
    protected void storeValues() {
        pane.setDefaults();
        setModifiedFlag(false);
    }

}

package jmri.jmrix.rps.aligntable;

import java.awt.Container;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Frame for user configuration of RPS alignment.
 * <p>
 * We only allow one of these right now, and so don't dispose on close
 *
 * @see AlignTableAction
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class AlignTableFrame extends jmri.util.JmriJFrame {

    RpsSystemConnectionMemo memo = null;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");

    /**
     * Constructor method
     */
    public AlignTableFrame(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    AlignTablePane p;

    /**
     * Initialize the window
     */
    @Override
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // add table
        p = new AlignTablePane(this);
        p.initComponents();
        contentPane.add(p);

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.rps.aligntable.AlignTableFrame", true);

        // check at shutdown
        setShutDownTask();

        // pack for display
        pack();
    }

    @Override
    protected void storeValues() {
        p.storeDefault();
        setModifiedFlag(false);
    }

}

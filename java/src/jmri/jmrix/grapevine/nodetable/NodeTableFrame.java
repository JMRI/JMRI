package jmri.jmrix.grapevine.nodetable;

import java.awt.Container;
import javax.swing.BoxLayout;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;

/**
 * Frame for user configuration of serial nodes
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 * @author Dave Duchamp Copyright (C) 2004, 2006
 */
public class NodeTableFrame extends jmri.util.JmriJFrame {

    private GrapevineSystemConnectionMemo memo = null;

    /**
     * Constructor method
     */
    public NodeTableFrame(GrapevineSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    NodeTablePane p;

    /**
     * Initialize the window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("WindowTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // add table
        p = new NodeTablePane(memo);
        p.initComponents();
        contentPane.add(p);

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.grapevine.nodetable.NodeTableFrame", true);

        // register
        memo.getTrafficController().addSerialListener(p);
        // pack for display
        pack();
    }

    @Override
    public void dispose() {
        memo.getTrafficController().removeSerialListener(p);
        super.dispose();
    }

}

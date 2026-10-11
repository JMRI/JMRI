package jmri.jmrix.jinput.treecontrol;

import java.awt.Container;
import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;

/**
 * Frame for controlling JInput access to USN
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class TreeFrame extends JmriJFrame {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

        // set the frame's initial state
        setTitle(Bundle.getMessage("WindowTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // add only content pane
        contentPane.add(new TreePanel());

        // add help menu
        addHelpMenu("package.jmri.jmrix.jinput.treemodel.TreeFrame", true);

        // pack for display
        pack();
    }

}

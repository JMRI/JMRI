package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.jmrit.logixng.*;
import jmri.util.JmriJFrame;

/**
 * Show the action/expression tree.
 * <P>
 * Base class for ConditionalNG editors
 *
 * @author Daniel Bergqvist 2018
 */
public class TreeViewer extends JmriJFrame {

    private static final int panelWidth = 500;
    private static final int panelHeight = 300;

    private boolean _rootVisible = true;

    public final TreePane _treePane;


    /**
     * Construct a ConditionalEditor.
     *
     * @param femaleRootSocket the root of the tree
     */
    public TreeViewer(FemaleSocket femaleRootSocket) {
        _treePane = new TreePane(femaleRootSocket);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenuItem closeWindowItem = new JMenuItem(Bundle.getMessage("CloseWindow"));
        closeWindowItem.addActionListener((ActionEvent e) -> {
            dispose();
        });
        fileMenu.add(closeWindowItem);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N

        _treePane.initComponents();

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(_treePane);

        initMinimumSize(new Dimension(panelWidth, panelHeight));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }

    public boolean getRootVisible() {
        return _rootVisible;
    }

    public void setRootVisible(boolean rootVisible) {
        _rootVisible = rootVisible;
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeViewer.class);

}

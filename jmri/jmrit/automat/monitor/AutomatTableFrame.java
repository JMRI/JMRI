// AutomatTableFrame.java

package jmri.jmrit.automat.monitor;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import java.util.*;
import javax.swing.JScrollPane;
import javax.swing.*;

import jmri.util.JTableUtil;

/**
 * Frame providing a table of Automat instances
 *
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @version	$Revision: 1.3 $
 */
public class AutomatTableFrame extends javax.swing.JFrame {

    AutomatTableDataModel	dataModel;
    JTable			dataTable;
    JScrollPane 		dataScroll;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.automat.monitor.AutomatTableBundle");

    public AutomatTableFrame(AutomatTableDataModel model) {

        super();
        dataModel 	= model;

        dataTable	= JTableUtil.sortableDataModel(dataModel);
        dataScroll	= new JScrollPane(dataTable);

        // configure items for GUI

        dataModel.configureTable(dataTable);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        getContentPane().add(dataScroll);
        pack();
        pane1.setMaximumSize(pane1.getSize());

		setTitle(rb.getString("TitleAutomatTable"));

        pack();
    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        dataModel.dispose();
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
    }
}

// BeanTableFrame.java

package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Frame providing a table of NamedBeans
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public class BeanTableFrame extends javax.swing.JFrame {

    BeanTableDataModel		dataModel;
    JTable			dataTable;
    JScrollPane 		dataScroll;

    public BeanTableFrame(BeanTableDataModel model) {

        super();
        dataModel 	= model;
        dataTable	= new JTable(dataModel);
        dataScroll	= new JScrollPane(dataTable);

        // configure items for GUI

        dataModel.configureTable(dataTable);

        // general GUI config
        setTitle("Bean Table");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        getContentPane().add(dataScroll);
        pack();
        pane1.setMaximumSize(pane1.getSize());
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

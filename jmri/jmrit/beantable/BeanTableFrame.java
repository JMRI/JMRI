// BeanTableFrame.java

package jmri.jmrit.beantable;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import java.util.*;
import javax.swing.JScrollPane;
import javax.swing.*;

/**
 * Frame providing a table of NamedBeans.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.4 $
 */
public class BeanTableFrame extends javax.swing.JFrame {

    BeanTableDataModel		dataModel;
    JTable			dataTable;
    JScrollPane 		dataScroll;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public BeanTableFrame(BeanTableDataModel model) {

        super();
        dataModel 	= model;
        dataTable	= new JTable(dataModel);
        dataScroll	= new JScrollPane(dataTable);

        // configure items for GUI

        dataModel.configureTable(dataTable);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.StoreXmlConfigAction());
        setJMenuBar(menuBar);

        // install items in GUI
        JPanel pane1 = new JPanel();
        getContentPane().add(dataScroll);
        pack();
        pane1.setMaximumSize(pane1.getSize());

        // add extras, if desired by subclass
        extras();

        pack();
    }

    void extras() {}

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

// BeanTableFrame.java

package jmri.jmrit.beantable;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import java.util.*;
import javax.swing.JScrollPane;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import jmri.util.JTableUtil;
import jmri.util.com.sun.TableSorter;

/**
 * Frame providing a table of NamedBeans.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.7 $
 */
public class BeanTableFrame extends javax.swing.JFrame {

    BeanTableDataModel		dataModel;
    JTable			dataTable;
    JScrollPane 		dataScroll;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public BeanTableFrame(BeanTableDataModel model) {

        super();
        dataModel 	= model;

        dataTable	= JTableUtil.sortableDataModel(dataModel);
        dataScroll	= new JScrollPane(dataTable);

        // give system name column a smarter sorter and use it initially
        TableSorter tmodel = ((TableSorter)dataTable.getModel());
        tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
        tmodel.setSortingStatus(BeanTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);

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

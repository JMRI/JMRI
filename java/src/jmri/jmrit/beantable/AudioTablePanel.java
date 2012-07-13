// AudioTablePanel.java

package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;
import jmri.util.JTableUtil;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;

/**
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision$
 */
public class AudioTablePanel extends JPanel {

    private AudioTableDataModel     listenerDataModel;
    private AudioTableDataModel     bufferDataModel;
    private AudioTableDataModel     sourceDataModel;
    private JTable                  listenerDataTable;
    private JTable                  bufferDataTable;
    private JTable                  sourceDataTable;
    private JScrollPane             listenerDataScroll;
    private JScrollPane             bufferDataScroll;
    private JScrollPane             sourceDataScroll;
    private JTabbedPane             audioTabs;
    Box bottomBox;  		    // panel at bottom for extra buttons etc
    int bottomBoxIndex;             // index to insert extra stuff

    static final int bottomStrutWidth = 20;

    //private static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    private static final ResourceBundle rba = ResourceBundle.getBundle("jmri.jmrit.audio.swing.AudioTableBundle");

    public AudioTablePanel(AudioTableDataModel listenerModel,
                           AudioTableDataModel bufferModel,
                           AudioTableDataModel sourceModel,
                           String helpTarget) {

        super();
        listenerDataModel = listenerModel;
        listenerDataTable = JTableUtil.sortableDataModel(listenerDataModel);
        listenerDataScroll = new JScrollPane(listenerDataTable);
        listenerDataTable.setColumnModel(new XTableColumnModel());
        listenerDataTable.createDefaultColumnsFromModel();

        bufferDataModel = bufferModel;
        bufferDataTable = JTableUtil.sortableDataModel(bufferDataModel);
        bufferDataScroll = new JScrollPane(bufferDataTable);
        bufferDataTable.setColumnModel(new XTableColumnModel());
        bufferDataTable.createDefaultColumnsFromModel();

        sourceDataModel = sourceModel;
        sourceDataTable = JTableUtil.sortableDataModel(sourceDataModel);
        sourceDataScroll = new JScrollPane(sourceDataTable);
        sourceDataTable.setColumnModel(new XTableColumnModel());
        sourceDataTable.createDefaultColumnsFromModel();

        // give system name column as smarter sorter and use it initially
        try {
            // Listener first
            TableSorter ltmodel = ((TableSorter)listenerDataTable.getModel());
            ltmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            ltmodel.setSortingStatus(AudioTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);

            // Buffers next
            TableSorter btmodel = ((TableSorter)listenerDataTable.getModel());
            btmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            btmodel.setSortingStatus(AudioTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);

            // Sources last
            TableSorter stmodel = ((TableSorter)listenerDataTable.getModel());
            stmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            stmodel.setSortingStatus(AudioTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);
        } catch (java.lang.ClassCastException e) {}  // happens if not sortable table

        // configure items for GUI
        listenerDataModel.configureTable(listenerDataTable);
        listenerDataModel.configEditColumn(listenerDataTable);
        bufferDataModel.configureTable(bufferDataTable);
        bufferDataModel.configEditColumn(bufferDataTable);
        sourceDataModel.configureTable(sourceDataTable);
        sourceDataModel.configEditColumn(sourceDataTable);

        // general GUI config
        this.setLayout(new BorderLayout());

        // install items in GUI
        audioTabs = new JTabbedPane();
        audioTabs.addTab(rba.getString("LabelListener"), listenerDataScroll);
        audioTabs.addTab(rba.getString("LabelBuffers"), bufferDataScroll);
        audioTabs.addTab(rba.getString("LabelSources"), sourceDataScroll);
        
        add(audioTabs, BorderLayout.CENTER);
        
        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());	// stays at end of box
        bottomBoxIndex = 0;

        add(bottomBox, BorderLayout.SOUTH);

        // add extras, if desired by subclass
        extras();

        // set preferred scrolling options
        listenerDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        listenerDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        bufferDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        bufferDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sourceDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sourceDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    }

    /**
     * Hook to allow sub-types to install more items in GUI
     */
        void extras() {}

    protected Box getBottomBox() { return bottomBox; }

     
    public JMenuItem getPrintItem(){
        ResourceBundle rbapps = ResourceBundle.getBundle("apps.AppsBundle");
        JMenuItem printItem = new JMenuItem(rbapps.getString("PrintTable"));
        
        printItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        MessageFormat footerFormat = new MessageFormat("Page {0,number}");
                        listenerDataTable.print(JTable.PrintMode.FIT_WIDTH , new MessageFormat("Listener Table"), footerFormat);
                        bufferDataTable.print(JTable.PrintMode.FIT_WIDTH , new MessageFormat("Buffer Table"), footerFormat);
                        sourceDataTable.print(JTable.PrintMode.FIT_WIDTH , new MessageFormat("Source Table"), footerFormat);
                    } catch (java.awt.print.PrinterException e1) {
                        log.warn("error printing: "+e1,e1);
                    }
                }
        });
        return printItem;
    }
    /**
     * Add a component to the bottom box. Takes care of organising glue, struts etc
     * @param comp
     */
    protected void addToBottomBox(Component comp) {
        bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
        ++bottomBoxIndex;
        bottomBox.add(comp, bottomBoxIndex);
        ++bottomBoxIndex;
    }

    public void dispose() {
        if (listenerDataModel != null){
            listenerDataModel.saveTableColumnDetails(listenerDataTable);
            listenerDataModel.dispose();
        }
        listenerDataModel = null;
        listenerDataTable = null;
        listenerDataScroll = null;
        if (bufferDataModel != null){
            bufferDataModel.saveTableColumnDetails(bufferDataTable);
            bufferDataModel.dispose();
        }
        bufferDataModel = null;
        bufferDataTable = null;
        bufferDataScroll = null;
        if (sourceDataModel != null){
            sourceDataModel.saveTableColumnDetails(sourceDataTable);
            sourceDataModel.dispose();
        }
        sourceDataModel = null;
        sourceDataTable = null;
        sourceDataScroll = null;
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AudioTablePanel.class.getName());

}

/* @(#)AudioTablePanel.java */
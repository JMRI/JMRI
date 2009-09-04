// AudioTableFrame.java

package jmri.jmrit.beantable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;
import jmri.util.JTableUtil;
import jmri.util.JmriJFrame;
import jmri.util.com.sun.TableSorter;
import jmri.util.davidflanagan.HardcopyWriter;

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
 * @version $Revision: 1.1 $
 */
public class AudioTableFrame extends JmriJFrame {

    AudioTableDataModel             listenerDataModel;
    AudioTableDataModel             bufferDataModel;
    AudioTableDataModel             sourceDataModel;
    JTable                          listenerDataTable;
    JTable                          bufferDataTable;
    JTable                          sourceDataTable;
    JScrollPane                     listenerDataScroll;
    JScrollPane                     bufferDataScroll;
    JScrollPane                     sourceDataScroll;
    Box bottomBox;  		    // panel at bottom for extra buttons etc
    int bottomBoxIndex;             // index to insert extra stuff

    static final int bottomStrutWidth = 20;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final ResourceBundle rba = ResourceBundle.getBundle("jmri.jmrit.audio.swing.AudioTableBundle");

    public AudioTableFrame(AudioTableDataModel listenerModel,
                           AudioTableDataModel bufferModel,
                           AudioTableDataModel sourceModel,
                           String helpTarget) {

        super();
        listenerDataModel = listenerModel;
        listenerDataTable = JTableUtil.sortableDataModel(listenerDataModel);
        listenerDataScroll = new JScrollPane(listenerDataTable);

        bufferDataModel = bufferModel;
        bufferDataTable = JTableUtil.sortableDataModel(bufferDataModel);
        bufferDataScroll = new JScrollPane(bufferDataTable);

        sourceDataModel = sourceModel;
        sourceDataTable = JTableUtil.sortableDataModel(sourceDataModel);
        sourceDataScroll = new JScrollPane(sourceDataTable);

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
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        ResourceBundle rbapps = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(rbapps.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.SaveMenu());

        // TODO: Fix printing and print preview
        JMenuItem printItem = new JMenuItem(rbapps.getString("PrintTable"));
        //fileMenu.add(printItem);
        final jmri.util.JmriJFrame tableFrame = this;
        printItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HardcopyWriter writer = null;
                    try {
                        writer = new HardcopyWriter(tableFrame,tableFrame.getTitle() ,10, .8, .5, .5, .5, false);
                    } catch (HardcopyWriter.PrintCanceledException ex) {
                        //log.debug("Print cancelled");
                        return;
                    }
                    writer.increaseLineSpacing(20);
                    listenerDataModel.printTable(writer);
                    bufferDataModel.printTable(writer);
                    sourceDataModel.printTable(writer);
                }
        });
        JMenuItem previewItem = new JMenuItem(rbapps.getString("PreviewTable"));
        //fileMenu.add(previewItem);
        previewItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HardcopyWriter writer = null;
                    try {
                        writer = new HardcopyWriter(tableFrame,tableFrame.getTitle() ,10, .8, .5, .5, .5, true);
                    } catch (HardcopyWriter.PrintCanceledException ex) {
                        //log.debug("Print cancelled");
                        return;
                    }
                    writer.increaseLineSpacing(20);
                    listenerDataModel.printTable(writer);
                    bufferDataModel.printTable(writer);
                    sourceDataModel.printTable(writer);
                }
        });

        setJMenuBar(menuBar);

        addHelpMenu(helpTarget,true);

        // install items in GUI
        getContentPane().add(new JLabel(rba.getString("LabelListener")));
        getContentPane().add(listenerDataScroll);

        getContentPane().add(new JLabel(rba.getString("LabelBuffers")));
        getContentPane().add(bufferDataScroll);

        getContentPane().add(new JLabel(rba.getString("LabelSources")));
        getContentPane().add(sourceDataScroll);

        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());	// stays at end of box
        bottomBoxIndex = 0;

        getContentPane().add(bottomBox);

        // add extras, if desired by subclass
        extras();

        // set Viewport preferred size from size of table
        java.awt.Dimension listenerDataTableSize = listenerDataTable.getPreferredSize();
        java.awt.Dimension bufferDataTableSize = bufferDataTable.getPreferredSize();
        java.awt.Dimension sourceDataTableSize = sourceDataTable.getPreferredSize();

        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        listenerDataTableSize.height = Math.max(listenerDataTableSize.height, 30);
        listenerDataScroll.getViewport().setPreferredSize(listenerDataTableSize);
        bufferDataTableSize.height = Math.max(bufferDataTableSize.height, 100);
        bufferDataScroll.getViewport().setPreferredSize(bufferDataTableSize);
        sourceDataTableSize.height = Math.max(sourceDataTableSize.height, 200);
        sourceDataScroll.getViewport().setPreferredSize(sourceDataTableSize);

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

    @Override
    public void dispose() {
        if (listenerDataModel != null)
            listenerDataModel.dispose();
        listenerDataModel = null;
        listenerDataTable = null;
        listenerDataScroll = null;
        if (bufferDataModel != null)
            bufferDataModel.dispose();
        bufferDataModel = null;
        bufferDataTable = null;
        bufferDataScroll = null;
        if (sourceDataModel != null)
            sourceDataModel.dispose();
        sourceDataModel = null;
        sourceDataTable = null;
        sourceDataScroll = null;
        super.dispose();
    }

    //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AudioTableFrame.class.getName());

}

/* @(#)AudioTableFrame.java */
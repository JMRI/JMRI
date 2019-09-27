package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;
import jmri.swing.RowSorterUtil;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Matthew Harris copyright (c) 2009
 */
public class AudioTablePanel extends JPanel {

    private AudioTableDataModel listenerDataModel;
    private AudioTableDataModel bufferDataModel;
    private AudioTableDataModel sourceDataModel;
    private JTable listenerDataTable;
    private JTable bufferDataTable;
    private JTable sourceDataTable;
    private JScrollPane listenerDataScroll;
    private JScrollPane bufferDataScroll;
    private JScrollPane sourceDataScroll;
    private JTabbedPane audioTabs;
    Box bottomBox;                  // panel at bottom for extra buttons etc
    int bottomBoxIndex;             // index to insert extra stuff

    static final int bottomStrutWidth = 20;

    private static final ResourceBundle rba = ResourceBundle.getBundle("jmri.jmrit.audio.swing.AudioTableBundle");

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AudioTablePanel(AudioTableDataModel listenerModel,
            AudioTableDataModel bufferModel,
            AudioTableDataModel sourceModel,
            String helpTarget) {

        super();
        listenerDataModel = listenerModel;
        TableRowSorter<AudioTableDataModel> sorter = new TableRowSorter<>(listenerDataModel);

        // use NamedBean's built-in Comparator interface for sorting the system name column
        RowSorterUtil.setSortOrder(sorter, AudioTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);
        listenerDataTable = listenerDataModel.makeJTable(listenerDataModel.getMasterClassName(), listenerDataModel, sorter);
        listenerDataScroll = new JScrollPane(listenerDataTable);
        listenerDataTable.setColumnModel(new XTableColumnModel());
        listenerDataTable.createDefaultColumnsFromModel();

        bufferDataModel = bufferModel;
        sorter = new TableRowSorter<>(bufferDataModel);
        RowSorterUtil.setSortOrder(sorter, AudioTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);
        bufferDataTable = bufferDataModel.makeJTable(bufferDataModel.getMasterClassName(), bufferDataModel, sorter);
        bufferDataScroll = new JScrollPane(bufferDataTable);
        bufferDataTable.setColumnModel(new XTableColumnModel());
        bufferDataTable.createDefaultColumnsFromModel();

        sourceDataModel = sourceModel;
        sorter = new TableRowSorter<>(sourceDataModel);
        RowSorterUtil.setSortOrder(sorter, AudioTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);
        sourceDataTable = sourceDataModel.makeJTable(sourceDataModel.getMasterClassName(), sourceDataModel, sorter);
        sourceDataScroll = new JScrollPane(sourceDataTable);
        sourceDataTable.setColumnModel(new XTableColumnModel());
        sourceDataTable.createDefaultColumnsFromModel();

        // configure items for GUI
        listenerDataModel.configureTable(listenerDataTable);
        listenerDataModel.configEditColumn(listenerDataTable);
        listenerDataModel.persistTable(listenerDataTable);
        bufferDataModel.configureTable(bufferDataTable);
        bufferDataModel.configEditColumn(bufferDataTable);
        bufferDataModel.persistTable(bufferDataTable);
        sourceDataModel.configureTable(sourceDataTable);
        sourceDataModel.configEditColumn(sourceDataTable);
        sourceDataModel.persistTable(sourceDataTable);

        // general GUI config
        this.setLayout(new BorderLayout());

        // install items in GUI
        audioTabs = new JTabbedPane();
        audioTabs.addTab(rba.getString("LabelListener"), listenerDataScroll);
        audioTabs.addTab(rba.getString("LabelBuffers"), bufferDataScroll);
        audioTabs.addTab(rba.getString("LabelSources"), sourceDataScroll);

        add(audioTabs, BorderLayout.CENTER);

        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
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
    void extras() {
    }

    protected Box getBottomBox() {
        return bottomBox;
    }

    public JMenuItem getPrintItem() {
        JMenuItem printItem = new JMenuItem(Bundle.getMessage("PrintTable"));

        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageFormat footerFormat = new MessageFormat("Page {0,number}");
                    listenerDataTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Listener Table"), footerFormat);
                    bufferDataTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Buffer Table"), footerFormat);
                    sourceDataTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Source Table"), footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: " + e1, e1);
                }
            }
        });
        return printItem;
    }

    /**
     * Add a component to the bottom box. Takes care of organising glue, struts
     * etc
     *
     * @param comp {@link Component} to add
     */
    protected void addToBottomBox(Component comp) {
        bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
        ++bottomBoxIndex;
        bottomBox.add(comp, bottomBoxIndex);
        ++bottomBoxIndex;
    }

    public void dispose() {
        if (listenerDataModel != null) {
            listenerDataModel.stopPersistingTable(listenerDataTable);
            listenerDataModel.dispose();
        }
        listenerDataModel = null;
        listenerDataTable = null;
        listenerDataScroll = null;
        if (bufferDataModel != null) {
            bufferDataModel.stopPersistingTable(bufferDataTable);
            bufferDataModel.dispose();
        }
        bufferDataModel = null;
        bufferDataTable = null;
        bufferDataScroll = null;
        if (sourceDataModel != null) {
            sourceDataModel.stopPersistingTable(sourceDataTable);
            sourceDataModel.dispose();
        }
        sourceDataModel = null;
        sourceDataTable = null;
        sourceDataScroll = null;
    }

    private static final Logger log = LoggerFactory.getLogger(AudioTablePanel.class);

}

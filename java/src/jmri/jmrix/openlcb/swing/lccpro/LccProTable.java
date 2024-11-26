package jmri.jmrix.openlcb.swing.lccpro;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jmri.InstanceManager;
import jmri.jmrit.roster.*;
import jmri.jmrit.roster.rostergroup.*;
import jmri.jmrit.roster.swing.*;

import jmri.jmrix.can.CanSystemConnectionMemo;

import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.JmriMouseAdapter;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.*;

/**
 * Provide a table of LCC node entries as a JmriJPanel.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010, 2024
 * @author Randall Wood Copyright (C) 2013
 */
public class LccProTable extends JmriPanel {

    private LccProTableModel dataModel;
    private TableRowSorter<LccProTableModel> sorter;
    private JTable dataTable;
    private JScrollPane dataScroll;
    private final XTableColumnModel columnModel = new XTableColumnModel();
//    private RosterGroupSelector rosterGroupSource = null;
    protected transient ListSelectionListener tableSelectionListener;
//     private RosterEntry[] selectedRosterEntries = null;
//     private RosterEntry[] sortedRosterEntries = null;
//     private RosterEntry re = null;

    public LccProTable(CanSystemConnectionMemo memo) {
        super();
        dataModel = new LccProTableModel(memo);
        sorter = new TableRowSorter<>(dataModel);
//         sorter.addRowSorterListener(rowSorterEvent -> {
//             if (rowSorterEvent.getType() ==  RowSorterEvent.Type.SORTED) {
//                 // clear sorted cache
//                 sortedRosterEntries = null;
//             }
//         });
        dataTable = new JTable(dataModel);
        dataTable.setRowSorter(sorter);
        dataScroll = new JScrollPane(dataTable);
        dataTable.setRowHeight(InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize() + 4);

        sorter.setComparator(LccProTableModel.IDCOL, new jmri.util.AlphanumComparator());

        // set initial sort
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(LccProTableModel.NAMECOL, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dataTable.setColumnModel(columnModel);
        dataTable.createDefaultColumnsFromModel();
        dataTable.setAutoCreateColumnsFromModel(false);

        // resize columns as requested
        resetColumnWidths();

        // Button rendering
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        columnModel.getColumn(LccProTableModel.CONFIGURECOL).setCellRenderer(buttonRenderer);
        columnModel.getColumn(LccProTableModel.UPGRADECOL).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        dataTable.setDefaultEditor(JButton.class, buttonEditor);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        add(dataScroll);

        // set Viewport preferred size from size of table
        java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        dataTableSize.width = Math.max(dataTableSize.width, 400);
        dataScroll.getViewport().setPreferredSize(dataTableSize);

        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JmriMouseListener mouseHeaderListener = new TableHeaderListener();
        dataTable.getTableHeader().addMouseListener(JmriMouseListener.adapt(mouseHeaderListener));

//         tableSelectionListener = (ListSelectionEvent e) -> {
//             if (!e.getValueIsAdjusting()) {
//                 selectedRosterEntries = null; // clear cached list of selections
//                 if (dataTable.getSelectedRowCount() == 1) {
//                     re = Roster.getDefault().getEntryForId(dataModel.getValueAt(sorter
//                         .convertRowIndexToModel(dataTable.getSelectedRow()), LccProTableModel.IDCOL).toString());
//                 } else if (dataTable.getSelectedRowCount() > 1) {
//                     re = null;
//                 } // leave last selected item visible if no selection
//             } else if (e.getFirstIndex() == -1) {
//                 // A reorder of the table may have occurred so ensure the selected item is still in view
//                 moveTableViewToSelected();
//             }
//         };
        dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
    }

    public JTable getTable() {
        return dataTable;
    }

    public LccProTableModel getModel() {
        return dataModel;
    }

    public final void resetColumnWidths() {
        Enumeration<TableColumn> en = columnModel.getColumns(false);
        while (en.hasMoreElements()) {
            TableColumn tc = en.nextElement();
            int width = dataModel.getPreferredWidth(tc.getModelIndex());
            tc.setPreferredWidth(width);
        }
        dataTable.sizeColumnsToFit(-1);
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
        dataTable = null;
        super.dispose();
    }

//     public void setRosterGroup(String rosterGroup) {
//         this.dataModel.setRosterGroup(rosterGroup);
//     }
// 
//     public String getRosterGroup() {
//         return this.dataModel.getRosterGroup();
//     }
// 
//     /**
//      * @return the rosterGroupSource
//      */
//     public RosterGroupSelector getRosterGroupSource() {
//         return this.rosterGroupSource;
//     }
// 
//     /**
//      * @param rosterGroupSource the rosterGroupSource to set
//      */
//     public void setRosterGroupSource(RosterGroupSelector rosterGroupSource) {
//         if (this.rosterGroupSource != null) {
//             this.rosterGroupSource.removePropertyChangeListener(SELECTED_ROSTER_GROUP, dataModel);
//         }
//         this.rosterGroupSource = rosterGroupSource;
//         if (this.rosterGroupSource != null) {
//             this.rosterGroupSource.addPropertyChangeListener(SELECTED_ROSTER_GROUP, dataModel);
//         }
//     }

    protected void showTableHeaderPopup(JmriMouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        for (int i = 0; i < columnModel.getColumnCount(false); i++) {
            TableColumn tc = columnModel.getColumnByModelIndex(i);
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dataTable.getModel()
                .getColumnName(i), columnModel.isColumnVisible(tc));
            menuItem.addActionListener(new HeaderActionListener(tc));
            popupMenu.add(menuItem);

        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

//     protected void moveTableViewToSelected() {
//         if (re == null) {
//             return;
//         }
//         //Remove the listener as this change will re-activate it and we end up in a loop!
//         dataTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
//         dataTable.clearSelection();
//         int entires = dataTable.getRowCount();
//         for (int i = 0; i < entires; i++) {
//             if (dataModel.getValueAt(sorter.convertRowIndexToModel(i), RosterTableModel.IDCOL).equals(re.getId())) {
//                 dataTable.addRowSelectionInterval(i, i);
//                 dataTable.scrollRectToVisible(new Rectangle(dataTable.getCellRect(i, 0, true)));
//             }
//         }
//         dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
//     }

//     @Override
//     public String getSelectedRosterGroup() {
//         return dataModel.getRosterGroup();
//     }

//     // cache selectedRosterEntries so that multiple calls to this
//     // between selection changes will not require the creation of a new array
//     @Override
//     public RosterEntry[] getSelectedRosterEntries() {
//         if (selectedRosterEntries == null) {
//             int[] rows = dataTable.getSelectedRows();
//             selectedRosterEntries = new RosterEntry[rows.length];
//             for (int idx = 0; idx < rows.length; idx++) {
//                 selectedRosterEntries[idx] = Roster.getDefault().getEntryForId(
//                     dataModel.getValueAt(sorter.convertRowIndexToModel(rows[idx]), RosterTableModel.IDCOL).toString());
//             }
//         }
//         return Arrays.copyOf(selectedRosterEntries, selectedRosterEntries.length);
//     }

    // cache getSortedRosterEntries so that multiple calls to this
    // between selection changes will not require the creation of a new array
//     public RosterEntry[] getSortedRosterEntries() {
//         if (sortedRosterEntries == null) {
//             sortedRosterEntries = new RosterEntry[sorter.getModelRowCount()];
//             for (int idx = 0; idx < sorter.getModelRowCount(); idx++) {
//                 sortedRosterEntries[idx] = Roster.getDefault().getEntryForId(
//                     dataModel.getValueAt(sorter.convertRowIndexToModel(idx), RosterTableModel.IDCOL).toString());
//             }
//         }
//         return Arrays.copyOf(sortedRosterEntries, sortedRosterEntries.length);
//     }

//     public void setEditable(boolean editable) {
//         this.dataModel.editable = editable;
//     }
// 
//     public boolean getEditable() {
//         return this.dataModel.editable;
//     }

    public void setSelectionMode(int selectionMode) {
        dataTable.setSelectionMode(selectionMode);
    }

    public int getSelectionMode() {
        return dataTable.getSelectionModel().getSelectionMode();
    }

//     public boolean setSelection(RosterEntry... selection) {
//         //Remove the listener as this change will re-activate it and we end up in a loop!
//         dataTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
//         dataTable.clearSelection();
//         boolean foundIt = false;
//         if (selection != null) {
//             for (RosterEntry entry : selection) {
//                 re = entry;
//                 int entries = dataTable.getRowCount();
//                 for (int i = 0; i < entries; i++) {
//                     if (dataModel.getValueAt(sorter
//                         .convertRowIndexToModel(i), RosterTableModel.IDCOL).equals(re.getId())) {
//                         dataTable.addRowSelectionInterval(i, i);
//                         foundIt = true;
//                     }
//                 }
//             }
//             if (selection.length > 1 || !foundIt) {
//                 re = null;
//             } else {
//                 this.moveTableViewToSelected();
//             }
//         } else {
//             re = null;
//         }
//         dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
//         return foundIt;
//     }

    private class HeaderActionListener implements ActionListener {

        TableColumn tc;

        HeaderActionListener(TableColumn tc) {
            this.tc = tc;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && columnModel.getColumnCount(true) == 1) {
                return;
            }
            columnModel.setColumnVisible(tc, check.isSelected());
        }
    }

    private class TableHeaderListener extends JmriMouseAdapter {

        @Override
        public void mousePressed(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e);
            }
        }

        @Override
        public void mouseReleased(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e);
            }
        }

        @Override
        public void mouseClicked(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e);
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LccProTable.class);

}

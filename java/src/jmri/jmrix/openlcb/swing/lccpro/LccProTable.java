package jmri.jmrix.openlcb.swing.lccpro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import jmri.InstanceManager;

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
 * <p>
 * Despite the name, this is-a JPanel, not a JTable.  You
 * access the contained JTable with the {@link #getTable()} method.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010, 2024
 * @author Randall Wood Copyright (C) 2013
 */
public class LccProTable extends JmriPanel {

    private LccProTableModel dataModel;
    TableRowSorter<LccProTableModel> sorter;
    private JTable dataTable;
    private JScrollPane dataScroll;
    private final XTableColumnModel columnModel = new XTableColumnModel();

    public LccProTable(CanSystemConnectionMemo memo) {
        super();
        dataModel = new LccProTableModel(memo) {
            @Override
            public void forceFocus() {
                requestFocus();
            }
        };
        
        sorter = new TableRowSorter<>(dataModel);
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
        TableCellEditor buttonEditor = new ButtonEditor(new JButton()){
            // don't want keystrokes to repeatedly fire buttons
            @Override
            public void editingStarted(EventObject event) {
            }
        };
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
    }

    /**
     * A LccProTable is actually a JPanel 
     * containing a JTable.  This returns
     * that contained JTable.
     */
    public JTable getTable() {
        return dataTable;
    }

    /** 
     * Provides the DataModel for the contained JTable.
     */
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
        dataTable = null;
        super.dispose();
    }

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

    public void setSelectionMode(int selectionMode) {
        dataTable.setSelectionMode(selectionMode);
    }

    public int getSelectionMode() {
        return dataTable.getSelectionModel().getSelectionMode();
    }

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

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LccProTable.class);

}

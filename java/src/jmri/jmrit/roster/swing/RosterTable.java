package jmri.jmrit.roster.swing;

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

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.JmriMouseAdapter;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.XTableColumnModel;

/**
 * Provide a table of roster entries as a JmriJPanel.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010
 * @author Randall Wood Copyright (C) 2013
 */
public class RosterTable extends JmriPanel implements RosterEntrySelector, RosterGroupSelector {

    private RosterTableModel dataModel;
    private TableRowSorter<RosterTableModel> sorter;
    private JTable dataTable;
    private JScrollPane dataScroll;
    private final XTableColumnModel columnModel = new XTableColumnModel();
    private RosterGroupSelector rosterGroupSource = null;
    protected transient ListSelectionListener tableSelectionListener;
    private RosterEntry[] selectedRosterEntries = null;
    private RosterEntry[] sortedRosterEntries = null;
    private RosterEntry re = null;

    public RosterTable() {
        this(false);
    }

    public RosterTable(boolean editable) {
        // set to single selection
        this(editable, ListSelectionModel.SINGLE_SELECTION);
    }

    public RosterTable(boolean editable, int selectionMode) {
        super();
        dataModel = new RosterTableModel(editable);
        sorter = new TableRowSorter<>(dataModel);
        sorter.addRowSorterListener(rowSorterEvent -> {
            if (rowSorterEvent.getType() ==  RowSorterEvent.Type.SORTED) {
                // clear sorted cache
                sortedRosterEntries = null;
            }
        });
        dataTable = new JTable(dataModel);
        dataTable.setRowSorter(sorter);
        dataScroll = new JScrollPane(dataTable);
        dataTable.setRowHeight(InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize() + 4);

        sorter.setComparator(RosterTableModel.IDCOL, new jmri.util.AlphanumComparator());

        // set initial sort
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(RosterTableModel.ADDRESSCOL, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dataTable.setColumnModel(columnModel);
        dataTable.createDefaultColumnsFromModel();
        dataTable.setAutoCreateColumnsFromModel(false);

        // format the last updated date time, last operated date time.
        dataTable.setDefaultRenderer(Date.class, new DateTimeCellRenderer());

        // Start with two columns not visible
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(RosterTableModel.DECODERMFGCOL), false);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(RosterTableModel.DECODERFAMILYCOL), false);

        TableColumn tc = columnModel.getColumnByModelIndex(RosterTableModel.PROTOCOL);
        columnModel.setColumnVisible(tc, false);

        // if the total time operated column exists, set it to DurationRenderer
        var columns = columnModel.getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            if ( Bundle.getMessage(RosterEntry.ATTRIBUTE_OPERATING_DURATION)
                .equals( column.getHeaderValue().toString())) {
                column.setCellRenderer( new DurationRenderer() );
                column.setCellEditor(new DurationCellEditor());
            }
        }

        // resize columns as requested
        resetColumnWidths();

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

        dataTable.setSelectionMode(selectionMode);
        JmriMouseListener mouseHeaderListener = new TableHeaderListener();
        dataTable.getTableHeader().addMouseListener(JmriMouseListener.adapt(mouseHeaderListener));

        dataTable.setDefaultEditor(Object.class, new RosterCellEditor());
        dataTable.setDefaultEditor(Date.class, new DateTimeCellEditor());

        tableSelectionListener = (ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                selectedRosterEntries = null; // clear cached list of selections
                if (dataTable.getSelectedRowCount() == 1) {
                    re = Roster.getDefault().getEntryForId(dataModel.getValueAt(sorter
                        .convertRowIndexToModel(dataTable.getSelectedRow()), RosterTableModel.IDCOL).toString());
                } else if (dataTable.getSelectedRowCount() > 1) {
                    re = null;
                } // leave last selected item visible if no selection
            } else if (e.getFirstIndex() == -1) {
                // A reorder of the table may have occurred so ensure the selected item is still in view
                moveTableViewToSelected();
            }
        };
        dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
    }

    public JTable getTable() {
        return dataTable;
    }

    public RosterTableModel getModel() {
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
        this.setRosterGroupSource(null);
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
        dataTable = null;
        super.dispose();
    }

    public void setRosterGroup(String rosterGroup) {
        this.dataModel.setRosterGroup(rosterGroup);
    }

    public String getRosterGroup() {
        return this.dataModel.getRosterGroup();
    }

    /**
     * @return the rosterGroupSource
     */
    public RosterGroupSelector getRosterGroupSource() {
        return this.rosterGroupSource;
    }

    /**
     * @param rosterGroupSource the rosterGroupSource to set
     */
    public void setRosterGroupSource(RosterGroupSelector rosterGroupSource) {
        if (this.rosterGroupSource != null) {
            this.rosterGroupSource.removePropertyChangeListener(SELECTED_ROSTER_GROUP, dataModel);
        }
        this.rosterGroupSource = rosterGroupSource;
        if (this.rosterGroupSource != null) {
            this.rosterGroupSource.addPropertyChangeListener(SELECTED_ROSTER_GROUP, dataModel);
        }
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

    protected void moveTableViewToSelected() {
        if (re == null) {
            return;
        }
        //Remove the listener as this change will re-activate it and we end up in a loop!
        dataTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
        dataTable.clearSelection();
        int entires = dataTable.getRowCount();
        for (int i = 0; i < entires; i++) {
            if (dataModel.getValueAt(sorter.convertRowIndexToModel(i), RosterTableModel.IDCOL).equals(re.getId())) {
                dataTable.addRowSelectionInterval(i, i);
                dataTable.scrollRectToVisible(new Rectangle(dataTable.getCellRect(i, 0, true)));
            }
        }
        dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
    }

    @Override
    public String getSelectedRosterGroup() {
        return dataModel.getRosterGroup();
    }

    // cache selectedRosterEntries so that multiple calls to this
    // between selection changes will not require the creation of a new array
    @Override
    public RosterEntry[] getSelectedRosterEntries() {
        if (selectedRosterEntries == null) {
            int[] rows = dataTable.getSelectedRows();
            selectedRosterEntries = new RosterEntry[rows.length];
            for (int idx = 0; idx < rows.length; idx++) {
                selectedRosterEntries[idx] = Roster.getDefault().getEntryForId(
                    dataModel.getValueAt(sorter.convertRowIndexToModel(rows[idx]), RosterTableModel.IDCOL).toString());
            }
        }
        return Arrays.copyOf(selectedRosterEntries, selectedRosterEntries.length);
    }

    // cache getSortedRosterEntries so that multiple calls to this
    // between selection changes will not require the creation of a new array
    public RosterEntry[] getSortedRosterEntries() {
        if (sortedRosterEntries == null) {
            sortedRosterEntries = new RosterEntry[sorter.getModelRowCount()];
            for (int idx = 0; idx < sorter.getModelRowCount(); idx++) {
                sortedRosterEntries[idx] = Roster.getDefault().getEntryForId(
                    dataModel.getValueAt(sorter.convertRowIndexToModel(idx), RosterTableModel.IDCOL).toString());
            }
        }
        return Arrays.copyOf(sortedRosterEntries, sortedRosterEntries.length);
    }

    public void setEditable(boolean editable) {
        this.dataModel.editable = editable;
    }

    public boolean getEditable() {
        return this.dataModel.editable;
    }

    public void setSelectionMode(int selectionMode) {
        dataTable.setSelectionMode(selectionMode);
    }

    public int getSelectionMode() {
        return dataTable.getSelectionModel().getSelectionMode();
    }

    public boolean setSelection(RosterEntry... selection) {
        //Remove the listener as this change will re-activate it and we end up in a loop!
        dataTable.getSelectionModel().removeListSelectionListener(tableSelectionListener);
        dataTable.clearSelection();
        boolean foundIt = false;
        if (selection != null) {
            for (RosterEntry entry : selection) {
                re = entry;
                int entries = dataTable.getRowCount();
                for (int i = 0; i < entries; i++) {
                                    
                    // skip over entry being deleted from the group
                    if (dataModel.getValueAt(sorter.convertRowIndexToModel(i), 
                                                                RosterTableModel.IDCOL) == null) {
                        continue;
                    }

                    if (dataModel.getValueAt(sorter.convertRowIndexToModel(i), 
                                            RosterTableModel.IDCOL)
                                    .equals(re.getId())) {
                        dataTable.addRowSelectionInterval(i, i);
                        foundIt = true;
                    }
                }
            }
            if (selection.length > 1 || !foundIt) {
                re = null;
            } else {
                this.moveTableViewToSelected();
            }
        } else {
            re = null;
        }
        dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
        return foundIt;
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

    public class RosterCellEditor extends DefaultCellEditor {

        public RosterCellEditor() {
            super(new JTextField() {

                @Override
                public void setBorder(Border border) {
                    //No border required
                }
            });
        }

        //This allows the cell to be edited using a single click if the row was previously selected, this allows a double on an unselected row to launch the programmer
        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            if (re == null) {
                //No previous roster entry selected so will take this as a select so no return false to prevent editing
                return false;
            }

            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) e;
                //If the click count is not equal to 1 then return false.
                if (me.getClickCount() != 1) {
                    return false;
                }
            }
            return re.getId().equals(dataModel.getValueAt(sorter.convertRowIndexToModel(dataTable.getSelectedRow()), RosterTableModel.IDCOL));
        }
    }

    private static class DurationRenderer extends DefaultTableCellRenderer {

        @Override
        public void setValue(Object value) {
            try {
                int duration = Integer.parseInt(value.toString());
                if ( duration != 0 ) {
                    super.setValue(jmri.util.DateUtil.userDurationFromSeconds(duration));
                    super.setToolTipText(Bundle.getMessage("DurationViewTip"));
                    return;
                }
            }
            catch (NumberFormatException e) {
                log.debug("could not format duration ( String integer of total seconds ) in {}", value, e);
            }
            super.setValue(null);
        }
    }

    private static class DateTimeCellRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            if ( value instanceof Date) {
                super.setValue(DateFormat.getDateTimeInstance().format((Date) value));
            } else {
                super.setValue(value);
            }
        }
    }

    private class DateTimeCellEditor extends RosterCellEditor {

        DateTimeCellEditor() {
            super();
        }

        private static final String EDITOR_DATE_FORMAT =  "yyyy-MM-dd HH:mm";
        private Date startDate = new Date();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            if (!(value instanceof Date) ) {
                value = new Date(); // field pre-populated if currently empty to show entry format
            }
            startDate = (Date)value;
            String formatted = new SimpleDateFormat(EDITOR_DATE_FORMAT).format((Date)value);
            ((JTextField)editorComponent).setText(formatted);
            editorComponent.setToolTipText("e.g. 2022-12-25 12:34");
            return editorComponent;
        }

        @Override
        public Object getCellEditorValue() {
            String o = (String)super.getCellEditorValue();
            if ( o.isBlank() ) { // user cancels the date / time
                return null;
            }
            SimpleDateFormat fm = new SimpleDateFormat(EDITOR_DATE_FORMAT);
            try {
                // get Date in local time before passing to StdDateFormat
                startDate = fm.parse(o.trim());
            } catch (ParseException e) {
            } // return value unchanged in case of user mis-type
            return new StdDateFormat().format(startDate);
        }

    }

    private class DurationCellEditor extends RosterCellEditor {

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            editorComponent.setToolTipText(Bundle.getMessage("DurationEditTip"));
            return editorComponent;
        }

        @Override
        public Object getCellEditorValue() {
            return String.valueOf(super.getCellEditorValue());
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RosterTable.class);

}

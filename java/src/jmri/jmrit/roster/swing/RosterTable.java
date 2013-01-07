// BeanTableFrame.java
package jmri.jmrit.roster.swing;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.XTableColumnModel;

/**
 * Provide a table of roster entries as a JmriJPanel
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2010
 * @author Randall Wood Copyright (C) 2013
 * @version	$Revision$
 */
public class RosterTable extends JmriPanel implements RosterEntrySelector, RosterGroupSelector {

    RosterTableModel dataModel;
    TableSorter sorter;
    JTable dataTable;
    JScrollPane dataScroll;
    XTableColumnModel columnModel = new XTableColumnModel();
    private RosterGroupSelector rosterGroupSource = null;
    protected ListSelectionListener tableSelectionListener;
    private RosterEntry[] selectedRosterEntries = null;
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
        sorter = new TableSorter(dataModel);
        dataTable = new JTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());
        dataScroll = new JScrollPane(dataTable);

        // set initial sort
        TableSorter tmodel = ((TableSorter) dataTable.getModel());
        tmodel.setSortingStatus(RosterTableModel.ADDRESSCOL, TableSorter.ASCENDING);


        // some columns, e.g. date, will need custom sorters.  See e.g.
        // jmri.jmrit.beantable.BeanTableFrame for an example of
        // setting that up
        //tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dataTable.setColumnModel(columnModel);
        dataModel.setColumnModel(columnModel);
        dataTable.createDefaultColumnsFromModel();
        dataTable.setAutoCreateColumnsFromModel(false);

        TableColumn tc = columnModel.getColumnByModelIndex(RosterTableModel.PROTOCOL);
        columnModel.setColumnVisible(tc, false);

        for (String s : Roster.instance().getAllAttributeKeys()) {
            if (!s.contains("RosterGroup") && !s.toLowerCase().startsWith("sys") && !s.toUpperCase().startsWith("VSD")) { // NOI18N
                String[] r = s.split("(?=\\p{Lu})"); // NOI18N
                StringBuilder sb = new StringBuilder();
                sb.append(r[0].trim());
                //System.out.println("'"+r[0]+",");
                for (int j = 1; j < r.length; j++) {
                    //System.out.println("'"+r[j]+",");
                    sb.append(" ");
                    sb.append(r[j].trim());
                }
                TableColumn c = new TableColumn(dataModel.getColumnCount());
                c.setHeaderValue((sb.toString()).trim());
                dataTable.addColumn(c);
                dataModel.addColumn(c.getHeaderValue());
                columnModel.setColumnVisible(c, false);
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
        MouseListener mouseHeaderListener = new tableHeaderListener();
        dataTable.getTableHeader().addMouseListener(mouseHeaderListener);

        dataTable.setDefaultEditor(Object.class, new RosterCellEditor());

        dataTable.getSelectionModel().addListSelectionListener(tableSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    selectedRosterEntries = null; // clear cached list of selections
                    if (dataTable.getSelectedRowCount() == 1) {
                        re = Roster.instance().getEntryForId(sorter.getValueAt(dataTable.getSelectedRow(), RosterTableModel.IDCOL).toString());
                    } else if (dataTable.getSelectedRowCount() > 1) {
                        re = null;
                    } // leave last selected item visible if no selection
                } else if (e.getFirstIndex() == -1) {
                    //A reorder of the table might of occured therefore we are going to make sure that the selected item is still in view
                    moveTableViewToSelected();
                }
            }
        });

    }

    public JTable getTable() {
        return dataTable;
    }

    public TableSorter getModel() {
        return sorter;
    }

    public void resetColumnWidths() {
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
        dataTable = null;
        dataScroll = null;
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
            this.rosterGroupSource.removePropertyChangeListener(RosterGroupSelector.selectedRosterGroupProperty, dataModel);
        }
        this.rosterGroupSource = rosterGroupSource;
        if (this.rosterGroupSource != null) {
            this.rosterGroupSource.addPropertyChangeListener(RosterGroupSelector.selectedRosterGroupProperty, dataModel);
        }
    }

    public XTableColumnModel getXTableColumnModel() {
        return columnModel;
    }

    protected void showTableHeaderPopup(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        for (int i = 0; i < columnModel.getColumnCount(false); i++) {
            TableColumn tc = columnModel.getColumnByModelIndex(i);
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dataTable.getModel().getColumnName(i), columnModel.isColumnVisible(tc));
            menuItem.addActionListener(new headerActionListener(tc));
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
            if (dataTable.getValueAt(i, RosterTableModel.IDCOL).equals(re.getId())) {
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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP",
    justification = "Want to give access to mutable, original roster objects")
    public RosterEntry[] getSelectedRosterEntries() {
        if (selectedRosterEntries == null) {
            int[] rows = dataTable.getSelectedRows();
            selectedRosterEntries = new RosterEntry[rows.length];
            for (int idx = 0; idx < rows.length; idx++) {
                selectedRosterEntries[idx] = Roster.instance().getEntryForId(sorter.getValueAt(rows[idx], RosterTableModel.IDCOL).toString());
            }
        }
        return selectedRosterEntries;
    }

    class headerActionListener implements ActionListener {

        TableColumn tc;

        headerActionListener(TableColumn tc) {
            this.tc = tc;
        }

        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && columnModel.getColumnCount(true) == 1) {
                return;
            }
            columnModel.setColumnVisible(tc, check.isSelected());
        }
    }

    class tableHeaderListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e);
            }
        }
    }

    public class RosterCellEditor extends DefaultCellEditor implements TableCellEditor {

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
            if (sorter.getValueAt(dataTable.getSelectedRow(), RosterTableModel.IDCOL).equals(re.getId())) {
                //if the current select roster entry matches the one that we have selected, then we can allow this field to be edited.
                return true;
            }
            return false;
        }
    }
}

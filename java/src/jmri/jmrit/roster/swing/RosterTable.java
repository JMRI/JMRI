// BeanTableFrame.java

package jmri.jmrit.roster.swing;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;

/**
 * Provide a table of roster entries
 * as a JmriJPanel
 * 
 * @author	Bob Jacobsen   Copyright (C) 2003, 2010
 * @version	$Revision$
 */
public class RosterTable extends jmri.util.swing.JmriPanel {

    RosterTableModel    dataModel;
    TableSorter         sorter;
    JTable			    dataTable;
    JScrollPane 		dataScroll;
    XTableColumnModel columnModel = new XTableColumnModel();
    
    private RosterGroupSelector rosterGroupSource = null;

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
        dataScroll	= new JScrollPane(dataTable);

        // set initial sort
        TableSorter tmodel = ((TableSorter)dataTable.getModel());
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
        dataTable.createDefaultColumnsFromModel();

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

    }
	   
	public JTable getTable() { return dataTable; }
	public TableSorter getModel() { return sorter; }

    public void resetColumnWidths(){
        Enumeration<TableColumn> en = columnModel.getColumns(false);
        while(en.hasMoreElements()){
            TableColumn tc = en.nextElement();
            int width = dataModel.getPreferredWidth(tc.getModelIndex());
            tc.setPreferredWidth(width);
        }
        dataTable.sizeColumnsToFit(-1);
    }
	
    public void dispose() {
        this.setRosterGroupSource(null);
        if (dataModel != null)
            dataModel.dispose();
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
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
            this.rosterGroupSource.removePropertyChangeListener("selectedRosterGroup", dataModel);
        }
        this.rosterGroupSource = rosterGroupSource;
        if (this.rosterGroupSource != null) {
            this.rosterGroupSource.addPropertyChangeListener("selectedRosterGroup", dataModel);
        }
    }
    
    public XTableColumnModel getXTableColumnModel(){
        return columnModel;
    }
    
    protected void showTableHeaderPopup(MouseEvent e){
        JPopupMenu popupMenu = new JPopupMenu();
        for (int i = 0; i < columnModel.getColumnCount(false); i++) {
            TableColumn tc = columnModel.getColumnByModelIndex(i);
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dataTable.getModel().getColumnName(i), columnModel.isColumnVisible(tc));
            menuItem.addActionListener(new headerActionListener(tc));
            popupMenu.add(menuItem);
            
        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    class headerActionListener implements ActionListener {
        TableColumn tc;
        headerActionListener(TableColumn tc){
             this.tc = tc;
        }
        
        public void actionPerformed(ActionEvent e){
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if(!check.isSelected() && columnModel.getColumnCount(true)==1){
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
}

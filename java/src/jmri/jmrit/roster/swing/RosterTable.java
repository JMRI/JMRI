// BeanTableFrame.java

package jmri.jmrit.roster.swing;

import javax.swing.*;

import jmri.util.com.sun.TableSorter;

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

        // resize columns as requested
        for (int i=0; i<dataTable.getColumnCount(); i++) {
            int width = dataModel.getPreferredWidth(i);
            dataTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        dataTable.sizeColumnsToFit(-1);

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
 	    
        // set preferred scrolling options
        //dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        dataTable.setSelectionMode(selectionMode);

    }
	   
	public JTable getTable() { return dataTable; }
	public TableSorter getModel() { return sorter; }
	
    public void dispose() {
        if (dataModel != null)
            dataModel.dispose();
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
    }
}

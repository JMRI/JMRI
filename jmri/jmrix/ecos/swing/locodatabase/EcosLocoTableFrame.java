// EcosLocoTableFrame.java

package jmri.jmrix.ecos.swing.locodatabase;

import java.util.ResourceBundle;

import javax.swing.*;
import java.awt.*;


import jmri.util.JTableUtil;
import jmri.util.com.sun.TableSorter;

//import jmri.util.davidflanagan.HardcopyWriter;


/**
 * Provide a JFrame to display the Ecos Loco Data
 * Based upon BeanTableFrame.
 * <P>
 * This frame includes the table itself at the top,
 * plus a "bottom area" for things like an Add... button
 * and checkboxes that control display options.
 * <p>
 * The usual menus are also provided here.
 * <p>
 * Specific uses are customized via the EcosLocoTableDataModel
 * implementation they provide, and by 
 * providing a {@link #extras} implementation
 * that can in turn invoke {@link #addToBottomBox} as needed.
 * 
 * @author	Kevin Dickerson   Copyright (C) 2009
 * @version	$Revision: 1.3 $
 */
public class EcosLocoTableFrame extends jmri.util.JmriJFrame {

    EcosLocoTableDataModel		dataModel;
    JTable			dataTable;
    JScrollPane 		dataScroll;
    Box bottomBox;		// panel at bottom for extra buttons etc
    int bottomBoxIndex;	// index to insert extra stuff
    static final int bottomStrutWidth = 20;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public EcosLocoTableFrame(EcosLocoTableDataModel model, String helpTarget) {

        super();
        dataModel 	= model;

        dataTable	= JTableUtil.sortableDataModel(dataModel);
        dataScroll	= new JScrollPane(dataTable);

        // give system name column as smarter sorter and use it initially
        try {
            TableSorter tmodel = ((TableSorter)dataTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(EcosLocoTableDataModel.ECOSOBJECTCOL, TableSorter.ASCENDING);
        } catch (java.lang.ClassCastException e) {}  // happens if not sortable table
        
        // configure items for GUI
        dataModel.configureTable(dataTable);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        /*ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        //fileMenu.add(new jmri.configurexml.SaveMenu());
        
        JMenuItem printItem = new JMenuItem(rb.getString("PrintTable"));
        fileMenu.add(printItem);
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
                    //dataModel.printTable(writer);
                }
        });
        JMenuItem previewItem = new JMenuItem(rb.getString("PreviewTable"));
        fileMenu.add(previewItem);        
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
                    //dataModel.printTable(writer);
                }
        });*/

        setJMenuBar(menuBar);

        addHelpMenu(helpTarget,true);

        // install items in GUI
        getContentPane().add(dataScroll);
        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());	// stays at end of box
        bottomBoxIndex = 0;
        
        //getContentPane().add(bottomBox);
        
        // add extras, if desired by subclass
        extras();

        // set Viewport preferred size from size of table
        java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        dataScroll.getViewport().setPreferredSize(dataTableSize);
 	    
        // set preferred scrolling options
        dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

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
    
    public void dispose() {
        if (dataModel != null)
            dataModel.dispose();
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
    }
}

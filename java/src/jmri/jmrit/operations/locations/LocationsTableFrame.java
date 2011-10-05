// LocationsTableFrame.java

package jmri.jmrit.operations.locations;
 
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;



/**
 * Frame for adding and editing the location roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision$
 */
public class LocationsTableFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

	LocationsTableModel locationsModel = new LocationsTableModel();
	javax.swing.JTable locationsTable = new javax.swing.JTable(locationsModel);
	JScrollPane locationsPane;
	
	// labels
	javax.swing.JLabel textSort = new javax.swing.JLabel();
	javax.swing.JLabel textSep = new javax.swing.JLabel();
	
	// radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(rb.getString("Name"));
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(rb.getString("Id"));

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();

    public LocationsTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle").getString("TitleLocationsTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	locationsPane = new JScrollPane(locationsTable);
    	locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	locationsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	locationsModel.initTable(locationsTable);
     	getContentPane().add(locationsPane);
     	
     	// Set up the control panel
    	JPanel controlPanel = new JPanel();
    	controlPanel.setLayout(new FlowLayout());
    	
    	textSort.setText("Sort by");
    	controlPanel.add(textSort);
    	controlPanel.add(sortByName);
    	sortByName.setSelected(true);
    	controlPanel.add(sortById);
    	textSep.setText("          ");
    	controlPanel.add(textSep);

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
		controlPanel.add (addButton);
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		
		addRadioButtonAction (sortByName);
		addRadioButtonAction (sortById);
    	
		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		Frame newFrame = new Frame();
		toolMenu.add(new PrintLocationsAction(rb.getString("MenuItemPrint"), newFrame, false, this));
		toolMenu.add(new PrintLocationsAction(rb.getString("MenuItemPreview"), newFrame, true, this));
		toolMenu.add(new SchedulesTableAction(rb.getString("Schedules")));
		toolMenu.add(new ModifyLocationsAction(rb.getString("TitleModifyLocations")));
		toolMenu.add(new ShowCarsByLocationAction(false, null, null));
		toolMenu.add(new SetPhysicalLocationAction(rb.getString("MenuSetPhysicalLocation"), null));
		menuBar.add(toolMenu);
		menuBar.add(new jmri.jmrit.operations.OperationsMenu());
		setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true);
    	
    	pack();
    	if ((getWidth()<670)) setSize(670, getHeight());
    	
     	// now load the cars and engines
    	CarManagerXml.instance();
    	EngineManagerXml.instance();
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			sortByName.setSelected(true);
			sortById.setSelected(false);
			locationsModel.setSort(locationsModel.SORTBYNAME);
		}
		if (ae.getSource() == sortById){
			sortByName.setSelected(false);
			sortById.setSelected(true);
			locationsModel.setSort(locationsModel.SORTBYID);
		}
	}
    
	// add button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("location button actived");
		if (ae.getSource() == addButton){
			LocationEditFrame f = new LocationEditFrame();
			f.initComponents(null);
			f.setTitle("Add Location");
		}
	}

    public void dispose() {
    	locationsModel.dispose();
        super.dispose();
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(LocationsTableFrame.class.getName());
}

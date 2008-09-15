// RoutesEditFrame.java

package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SidingEditFrame;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.cars.CarLengths;
import jmri.jmrit.operations.cars.CarTypes;
import jmri.jmrit.operations.cars.CarManagerXml;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of route
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */

public class RouteEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
	
	RouteLocationsTableModel routeModel = new RouteLocationsTableModel();
	javax.swing.JTable routeTable = new javax.swing.JTable(routeModel);
	JScrollPane routePane;
	
	RouteManager manager;
	RouteManagerXml managerXml;
	LocationManager locationManager = LocationManager.instance();

	Route _route = null;
	RouteLocation _routeLocation = null;
	ArrayList checkBoxes;

	// labels
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	
	javax.swing.JButton addLocationButton = new javax.swing.JButton();
	javax.swing.JButton saveRouteButton = new javax.swing.JButton();
	javax.swing.JButton deleteRouteButton = new javax.swing.JButton();
	javax.swing.JButton addRouteButton = new javax.swing.JButton();

	// check boxes
	javax.swing.JCheckBox checkBox;
	
	// radio buttons
    javax.swing.JRadioButton addLocAtTop = new javax.swing.JRadioButton(rb.getString("Top"));
    javax.swing.JRadioButton addLocAtBottom = new javax.swing.JRadioButton(rb.getString("Bottom"));
    ButtonGroup group = new ButtonGroup();
	
	// text field
	javax.swing.JTextField routeNameTextField = new javax.swing.JTextField(20);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo boxes
	javax.swing.JComboBox locationBox = LocationManager.instance().getComboBox();

	public static final String NAME = rb.getString("Name");
	public static final String LENGTH = rb.getString("Length");
	public static final String DISPOSE = "dispose" ;

	public RouteEditFrame() {
		super();
	}

	public void initComponents(Route route) {
				
		_route = route;

		// load managers
		manager = RouteManager.instance();
		managerXml = RouteManagerXml.instance();
		
		OperationsXml.instance();					// force settings to load 

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);

		textOptional.setText("-------------------------------- Optional ------------------------------------");
		textOptional.setVisible(true);
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		space1.setText("     ");
		space1.setVisible(true);
		space2.setText("     ");
		space2.setVisible(true);

		deleteRouteButton.setText(rb.getString("DeleteRoute"));
		deleteRouteButton.setVisible(true);
		addRouteButton.setText(rb.getString("AddRoute"));
		addRouteButton.setVisible(true);
		saveRouteButton.setText(rb.getString("SaveRoute"));
		saveRouteButton.setVisible(true);
		addLocationButton.setText(rb.getString("AddLocation"));
		addLocationButton.setVisible(true);
		
	   	// Set up the jtable in a Scroll Pane..
    	routePane = new JScrollPane(routeTable);
    	routePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

 		
		if (_route != null){
			routeNameTextField.setText(_route.getName());
			commentTextField.setText(_route.getComment());
	      	routeModel.initTable(routeTable, route);
	      	enableButtons(true);
		} else {
			enableButtons(false);
		}
		
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
		// row 1
		addItem(p1, textName, 0, 1);
		addItemWidth(p1, routeNameTextField, 3, 1, 1);

		// row 2

		// row 3

		
		// row 4
		

		// row 5

		int y = 6;

		// row 7

		// row 8
		
    	JPanel p3 = new JPanel();
    	p3.setLayout(new GridBagLayout());
    	addItem(p3, locationBox, 0, 1);
    	addItem(p3, addLocationButton, 1, 1);
    	addItem(p3, addLocAtTop, 2, 1);
    	addItem(p3, addLocAtBottom, 3, 1);
    	group.add(addLocAtTop);
    	group.add(addLocAtBottom);
    	addLocAtBottom.setSelected(true);
		Border border = BorderFactory.createEtchedBorder();
		p3.setBorder(border);
		
		// row 9

		
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		
		// row 10
		addItem (p4, space1, 0, ++y);
    	
		// row 11
		addItem(p4, textComment, 0, ++y);
		addItemWidth(p4, commentTextField, 3, 1, y);
				
		// row 12
		addItem(p4, space2, 0, ++y);
		// row 13
		addItem(p4, deleteRouteButton, 0, ++y);
		addItem(p4, addRouteButton, 1, y);
		addItem(p4, saveRouteButton, 3, y);
		
		getContentPane().add(p1);
       	getContentPane().add(routePane);
       	getContentPane().add(p3);
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(addLocationButton);
		addButtonAction(deleteRouteButton);
		addButtonAction(addRouteButton);
		addButtonAction(saveRouteButton);
		
		// setup combobox
       	

		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true);

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		
		// set frame size and route for display
		pack();
		if((getWidth()<800)) setSize(800, getHeight());
		setSize(getWidth(), 600);
		setVisible(true);
	}
	
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLocationButton){
			log.debug("route add location button actived");
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals(""))
					return;
				else
					addNewRouteLocation();
			}
		}
		if (ae.getSource() == saveRouteButton){
			log.debug("route save button actived");
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (_route == null && route == null){
				saveNewRoute();
			} else {
				if (route != null && route != _route){
					reportRouteExists("save");
					return;
				}
				saveRoute();
			}
		}
		if (ae.getSource() == deleteRouteButton){
			log.debug("route delete button actived");
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (route == null)
				return;
			
			manager.deregister(route);
			_route = null;

			enableButtons(false);
			// save route file
			managerXml.writeOperationsRouteFile();
		}
		if (ae.getSource() == addRouteButton){
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (route != null){
				reportRouteExists("add");
				return;
			}
			saveNewRoute();
		}
	}
	
	private void addNewRouteLocation(){
		// add location to this route
		if (addLocAtTop.isSelected())
			_route.addLocation((Location)locationBox.getSelectedItem(),0);
		else
			_route.addLocation((Location)locationBox.getSelectedItem());
	}
	
	private void saveNewRoute(){
		if (!checkName())
			return;
		Route route = manager.newRoute(routeNameTextField.getText());
		routeModel.initTable(routeTable, route);
		_route = route;
		// enable checkboxes
		enableButtons(true);
		saveRoute();
	}
	
	private void saveRoute (){
		if (!checkName())
			return;
		_route.setName(routeNameTextField.getText());
		_route.setComment(commentTextField.getText());

		// save route file
		managerXml.writeOperationsRouteFile();
	}
	

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (routeNameTextField.getText().length() > 25){
			log.error("Route name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					"Route name must be less than 26 charaters", "Can not add route!",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportRouteExists(String s){
		log.info("Can not " + s + ", route already exists");
		JOptionPane.showMessageDialog(this,
				"Route with this name already exists", "Can not " + s + " route!",
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		locationBox.setEnabled(enabled);
		addLocationButton.setEnabled(enabled);
		addLocAtTop.setEnabled(enabled);
		addLocAtBottom.setEnabled(enabled);
		saveRouteButton.setEnabled(enabled);
		deleteRouteButton.setEnabled(enabled);
		routeTable.setEnabled(enabled);
		// the inverse!
		addRouteButton.setEnabled(!enabled);
	}
	
	private void addRadioButtonAction(JRadioButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonRadioActionPerformed(e);
			}
		});
	}
	
	public void buttonRadioActionPerformed(java.awt.event.ActionEvent ae) {

	}
	
	private void radioButtonYards (boolean enabled){
//		yardPane.setVisible(enabled);
//		addYardButton.setVisible(enabled);
	}
	
	private void enableCheckboxes(boolean enable){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			checkBox.setEnabled(enable);
		}
	}
		
	private void addCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (_route == null)
			return;
	}
	
	
	private void addCheckBoxTrainAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionTrainPerformed(e);
			}
		});
	}
	
	private void checkBoxActionTrainPerformed(java.awt.event.ActionEvent ae) {
		// save train directions serviced by this route
		if (_route == null)
			return;
	}
	
	public void dispose() {
		routeModel.dispose();
		super.dispose();
	}
	
	private void updateComboBoxes(){
		locationManager.updateComboBox(locationBox);
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH)){
			updateComboBoxes();
		}
	}
 	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(RouteEditFrame.class.getName());
}

// RoutesEditFrame.java

package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Frame for user edit of route
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.19 $
 */

public class RouteEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
	
	RouteLocationsTableModel routeModel = new RouteLocationsTableModel();
	JTable routeTable = new JTable(routeModel);
	JScrollPane routePane;
	
	RouteManager manager;
	RouteManagerXml managerXml;
	LocationManager locationManager = LocationManager.instance();

	Route _route = null;
	RouteLocation _routeLocation = null;

	// labels
	JLabel textName = new JLabel();
	JLabel textComment = new JLabel();

	// major buttons
	JButton addLocationButton = new JButton();
	JButton saveRouteButton = new JButton();
	JButton deleteRouteButton = new JButton();
	JButton addRouteButton = new JButton();

	// check boxes
	JCheckBox checkBox;
	
	// radio buttons
    JRadioButton addLocAtTop = new JRadioButton(rb.getString("Top"));
    JRadioButton addLocAtBottom = new JRadioButton(rb.getString("Bottom"));
    ButtonGroup group = new ButtonGroup();
	
	// text field
	JTextField routeNameTextField = new JTextField(20);
	JTextField commentTextField = new JTextField(35);

	// for padding out panel
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();
	
	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();

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
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
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
		int y = 10;
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
       	

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new PrintRouteAction(rb.getString("MenuItemPrint"), new Frame(), false, _route));
		toolMenu.add(new PrintRouteAction(rb.getString("MenuItemPreview"), new Frame(), true, _route));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true);

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		
		// set frame size and route for display
		pack();
		if((getWidth()<900)) setSize(900, getHeight());
		setSize(getWidth(), Control.panelHeight);
		setVisible(true);
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLocationButton){
			log.debug("route add location button actived");
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals(""))
					return;
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
					reportRouteExists(rb.getString("save"));
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
				reportRouteExists(rb.getString("add"));
				return;
			}
			saveNewRoute();
		}
	}
	
	private void addNewRouteLocation(){
		// add location to this route
		RouteLocation rl;
		if (addLocAtTop.isSelected())
			rl = _route.addLocation((Location)locationBox.getSelectedItem(),0);
		else
			rl =_route.addLocation((Location)locationBox.getSelectedItem());
		rl.setTrainDirection(routeModel.getLastTrainDirection());
		rl.setMaxTrainLength(routeModel.getLastMaxTrainLength());
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
		
		if(routeTable.isEditing()){
			log.debug("route table edit true");
			routeTable.getCellEditor().stopCellEditing();
		}

		// save route file
		managerXml.writeOperationsRouteFile();
	}
	

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (routeNameTextField.getText().trim().equals(""))
			return false;
		if (routeNameTextField.getText().length() > 25){
			log.error("Route name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					rb.getString("RouteNameLess"), rb.getString("CanNotAddRoute"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportRouteExists(String s){
		log.info("Can not " + s + ", route already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("ReportExists"), MessageFormat.format(rb.getString("CanNotRoute"),new Object[]{s}),
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
	
	public void dispose() {
		routeModel.dispose();
		super.dispose();
	}
	
	private void updateComboBoxes(){
		locationManager.updateComboBox(locationBox);
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateComboBoxes();
		}
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(RouteEditFrame.class.getName());
}

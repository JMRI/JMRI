// RoutesEditFrame.java

package jmri.jmrit.operations.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;

/**
 * Frame for user edit of route
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011
 * @version $Revision$
 */

public class RouteEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	RouteEditTableModel routeModel = new RouteEditTableModel();
	JTable routeTable = new JTable(routeModel);
	JScrollPane routePane;

	RouteManager manager;
	RouteManagerXml managerXml;
	LocationManager locationManager = LocationManager.instance();

	Route _route = null;
	RouteLocation _routeLocation = null;
	Train _train = null;

	// major buttons
	JButton addLocationButton = new JButton(Bundle.getMessage("AddLocation"));
	JButton saveRouteButton = new JButton(Bundle.getMessage("SaveRoute"));
	JButton deleteRouteButton = new JButton(Bundle.getMessage("DeleteRoute"));
	JButton addRouteButton = new JButton(Bundle.getMessage("AddRoute"));

	// check boxes
	JCheckBox checkBox;

	// radio buttons
	JRadioButton addLocAtTop = new JRadioButton(Bundle.getMessage("Top"));
	JRadioButton addLocAtBottom = new JRadioButton(Bundle.getMessage("Bottom"));
	ButtonGroup group = new ButtonGroup();

	JRadioButton showWait = new JRadioButton(Bundle.getMessage("Wait"));
	JRadioButton showDepartTime = new JRadioButton(Bundle.getMessage("DepartTime"));
	ButtonGroup groupTime = new ButtonGroup();

	// text field
	JTextField routeNameTextField = new JTextField(20);
	JTextField commentTextField = new JTextField(35);

	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();

	public static final String NAME = Bundle.getMessage("Name");
	public static final String DISPOSE = "dispose"; // NOI18N

	public RouteEditFrame() {
		super();
	}
	
	public void initComponents(Route route, Train train) {
		_train = train;
		initComponents(route);
	}

	public void initComponents(Route route) {

		_route = route;
		String routeName = null;

		// load managers
		manager = RouteManager.instance();
		managerXml = RouteManagerXml.instance();

		// Set up the jtable in a Scroll Pane..
		routePane = new JScrollPane(routeTable);
		routePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		routePane.setBorder(BorderFactory.createTitledBorder(""));

		if (_route != null) {
			routeName = _route.getName();
			routeNameTextField.setText(routeName);
			commentTextField.setText(_route.getComment());
			routeModel.initTable(this, routeTable, route);
			enableButtons(true);
		} else {
			enableButtons(false);
		}

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		JScrollPane p1Pane = new JScrollPane(p1);
		p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		p1Pane.setMinimumSize(new Dimension(300, 3 * routeNameTextField.getPreferredSize().height));
		p1Pane.setBorder(BorderFactory.createTitledBorder(""));

		// name panel
		JPanel pName = new JPanel();
		pName.setLayout(new GridBagLayout());
		pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
		addItem(pName, routeNameTextField, 0, 0);

		// comment panel
		JPanel pComment = new JPanel();
		pComment.setLayout(new GridBagLayout());
		pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
		addItem(pComment, commentTextField, 0, 0);

		p1.add(pName);
		p1.add(pComment);

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

		// location panel
		JPanel pLoc = new JPanel();
		pLoc.setLayout(new GridBagLayout());
		pLoc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
		addItem(pLoc, locationBox, 0, 1);
		addItem(pLoc, addLocationButton, 1, 1);
		addItem(pLoc, addLocAtTop, 2, 1);
		addItem(pLoc, addLocAtBottom, 3, 1);
		group.add(addLocAtTop);
		group.add(addLocAtBottom);
		addLocAtBottom.setSelected(true);

		// Wait or Depart Time panel
		JPanel pWait = new JPanel();
		pWait.setLayout(new GridBagLayout());
		pWait.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Display")));
		addItem(pWait, showWait, 0, 1);
		addItem(pWait, showDepartTime, 1, 1);
		groupTime.add(showWait);
		groupTime.add(showDepartTime);

		p2.add(pLoc);
		p2.add(pWait);

		// row 12 buttons
		JPanel pB = new JPanel();
		pB.setLayout(new GridBagLayout());
		pB.setBorder(BorderFactory.createTitledBorder(""));
		addItem(pB, deleteRouteButton, 0, 0);
		addItem(pB, addRouteButton, 1, 0);
		addItem(pB, saveRouteButton, 3, 0);

		getContentPane().add(p1Pane);
		getContentPane().add(routePane);
		getContentPane().add(p2);
		getContentPane().add(pB);

		// setup buttons
		addButtonAction(addLocationButton);
		addButtonAction(deleteRouteButton);
		addButtonAction(addRouteButton);
		addButtonAction(saveRouteButton);

		// setup radio buttons
		addRadioButtonAction(showWait);
		addRadioButtonAction(showDepartTime);
		setTimeWaitRadioButtons();

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new RouteCopyAction(Bundle.getMessage("MenuItemCopy"), routeName));
		toolMenu.add(new SetTrainIconRouteAction(Bundle.getMessage("MenuSetTrainIconRoute"),
				routeName));
		toolMenu.add(new PrintRouteAction(Bundle.getMessage("MenuItemPrint"), false, _route));
		toolMenu.add(new PrintRouteAction(Bundle.getMessage("MenuItemPreview"), true, _route));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_EditRoute", true); // NOI18N

		// get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);

		// set frame size and route for display
		pack();
		if (getWidth() < Control.panelWidth)
			setSize(Control.panelWidth, Control.panelHeight);
		setVisible(true);

		// create ShutDownTasks
		createShutDownTask();
	}

	// Save, Delete, Add
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLocationButton) {
			log.debug("route add location button activated");
			if (locationBox.getSelectedItem() != null) {
				if (locationBox.getSelectedItem().equals(""))
					return;
				addNewRouteLocation();
			}
		}
		if (ae.getSource() == saveRouteButton) {
			log.debug("route save button activated");
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (_route == null && route == null) {
				saveNewRoute();
			} else {
				if (route != null && route != _route) {
					reportRouteExists(Bundle.getMessage("save"));
					return;
				}
				saveRoute();
			}
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
		if (ae.getSource() == deleteRouteButton) {
			log.debug("route delete button activated");
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(Bundle.getMessage("AreYouSure?"),
							new Object[] { routeNameTextField.getText() }), Bundle
							.getMessage("DeleteRoute?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (route == null)
				return;

			manager.deregister(route);
			_route = null;

			enableButtons(false);
			routeModel.dispose();
			// save route file
			OperationsXml.save();
		}
		if (ae.getSource() == addRouteButton) {
			Route route = manager.getRouteByName(routeNameTextField.getText());
			if (route != null) {
				reportRouteExists(Bundle.getMessage("add"));
				return;
			}
			saveNewRoute();
		}
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		routeModel.setWait(showWait.isSelected());
	}

	private void addNewRouteLocation() {
		if (routeTable.isEditing()) {
			log.debug("route table edit true");
			routeTable.getCellEditor().stopCellEditing();
		}
		// add location to this route
		Location l = (Location) locationBox.getSelectedItem();
		RouteLocation rl;
		if (addLocAtTop.isSelected())
			rl = _route.addLocation(l, 0);
		else
			rl = _route.addLocation(l);
		rl.setTrainDirection(routeModel.getLastTrainDirection());
		rl.setMaxTrainLength(routeModel.getLastMaxTrainLength());
		// set train icon location
		rl.setTrainIconCoordinates();
	}

	private void saveNewRoute() {
		if (!checkName(Bundle.getMessage("add")))
			return;
		Route route = manager.newRoute(routeNameTextField.getText());
		routeModel.initTable(this, routeTable, route);
		_route = route;
		// enable checkboxes
		enableButtons(true);
		// assign route to a train?
		if (_train != null)
			_train.setRoute(route);
		saveRoute();
	}

	private void saveRoute() {
		if (!checkName(Bundle.getMessage("save")))
			return;
		_route.setName(routeNameTextField.getText());
		_route.setComment(commentTextField.getText());

		if (routeTable.isEditing()) {
			log.debug("route table edit true");
			routeTable.getCellEditor().stopCellEditing();
		}

		saveTableDetails(routeTable);

		// save route file
		OperationsXml.save();
	}

	/**
	 * 
	 * @return true if name is length is okay
	 */
	private boolean checkName(String s) {
		if (routeNameTextField.getText().trim().equals("")) {
			log.debug("Must enter a name for the route");
			JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"),
					MessageFormat.format(Bundle.getMessage("CanNotRoute"), new Object[] { s }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (routeNameTextField.getText().length() > Control.max_len_string_route_name) {
			JOptionPane
					.showMessageDialog(this, MessageFormat.format(
							Bundle.getMessage("RouteNameLess"),
							new Object[] { Control.max_len_string_route_name + 1 }), MessageFormat
							.format(Bundle.getMessage("CanNotRoute"), new Object[] { s }),
							JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void reportRouteExists(String s) {
		log.info("Can not " + s + ", route already exists");
		JOptionPane.showMessageDialog(this, Bundle.getMessage("ReportExists"),
				MessageFormat.format(Bundle.getMessage("CanNotRoute"), new Object[] { s }),
				JOptionPane.ERROR_MESSAGE);
	}

	private void enableButtons(boolean enabled) {
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

	private void updateComboBoxes() {
		locationManager.updateComboBox(locationBox);
	}

	// if the route has a departure time in the first location set the showDepartTime radio button
	private void setTimeWaitRadioButtons() {
		showWait.setSelected(true);
		if (_route != null) {
			RouteLocation rl = _route.getDepartsRouteLocation();
			if (rl != null && !rl.getDepartureTime().equals(""))
				showDepartTime.setSelected(true);
			routeModel.setWait(showWait.isSelected());
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
			updateComboBoxes();
		}
	}

	static Logger log = LoggerFactory.getLogger(RouteEditFrame.class
			.getName());
}

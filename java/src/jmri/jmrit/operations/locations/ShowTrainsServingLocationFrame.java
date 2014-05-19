// ShowTrainsServingLocationFram.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;
import java.text.MessageFormat;

import javax.swing.*;

/**
 * Frame to show which trains can service this location
 * 
 * @author Dan Boudreau Copyright (C) 2014
 * @version $Revision: 24984 $
 */

public class ShowTrainsServingLocationFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	// location
	Location _location = null;
	Track _track = null;

	// panels
	JPanel pTrains = new JPanel();

	// radio buttons

	// for padding out panel

	// combo boxes
	JComboBox typeComboBox = new JComboBox();

	// Blank space
	String blank = "";

	// The car currently selected
	Car car;

	public ShowTrainsServingLocationFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {

		_location = location;
		_track = track;

		// general GUI config
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		JPanel pCarType = new JPanel();
		pCarType.setLayout(new GridBagLayout());
		pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
		pCarType.setMaximumSize(new Dimension(2000, 50));

		addItem(pCarType, typeComboBox, 0, 0);

		pTrains.setLayout(new GridBagLayout());
		JScrollPane trainsPane = new JScrollPane(pTrains);
		trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		trainsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Trains")));

		getContentPane().add(pCarType);
		getContentPane().add(trainsPane);

		// setup combo box
		updateComboBox();
		typeComboBox.setSelectedItem(blank);
		addComboBoxAction(typeComboBox);

		// increase width of combobox so large text names display properly
		Dimension boxsize = typeComboBox.getMinimumSize();
		if (boxsize != null) {
			boxsize.setSize(boxsize.width + 10, boxsize.height);
			typeComboBox.setMinimumSize(boxsize);
		}

		updateTrainPane();

		location.addPropertyChangeListener(this);
		addPropertyChangeAllTrains();

		if (_track != null) {
			_track.addPropertyChangeListener(this);
			setTitle(MessageFormat.format(Bundle.getMessage("TitleShowTrains"), new Object[] {_track.getName()}));
		} else {
			setTitle(MessageFormat.format(Bundle.getMessage("TitleShowTrains"), new Object[] {_location.getName()}));
		}

		setPreferredSize(null);
		initMinimumSize();
	}

	private void updateTrainPane() {
		pTrains.removeAll();
		int y = 0;
		for (Train train : TrainManager.instance().getTrainsByNameList()) {
			Route route = train.getRoute();
			if (route == null)
				continue;
			for (RouteLocation rl : route.getLocationsBySequenceList()) {
				if (rl.getName().equals(_location.getName())) {
					addItem(pTrains, new JLabel(train.getName()), 0, y);
					if (rl.isDropAllowed()
							&& rl.getMaxCarMoves() > 0
							&& !train.skipsLocation(rl.getId())
							&& (typeComboBox.getSelectedItem() == null || typeComboBox.getSelectedItem().equals(blank) || train
									.acceptsTypeName((String) typeComboBox.getSelectedItem()))
							&& (rl.getTrainDirection() & _location.getTrainDirections()) > 0
							&& (_track == null || ((rl.getTrainDirection() & _track.getTrainDirections()) > 0)
							&& _track.acceptsDropTrain(train)))
						addItem(pTrains, new JLabel("Yes set out"), 1, y);
					else
						addItem(pTrains, new JLabel("No set out"), 1, y);
					if (rl.isPickUpAllowed()
							&& rl.getMaxCarMoves() > 0
							&& !train.skipsLocation(rl.getId())
							&& (typeComboBox.getSelectedItem() == null || typeComboBox.getSelectedItem().equals(blank) || train
									.acceptsTypeName((String) typeComboBox.getSelectedItem()))
							&& (rl.getTrainDirection() & _location.getTrainDirections()) > 0
							&& (_track == null || ((rl.getTrainDirection() & _track.getTrainDirections()) > 0)
							&& _track.acceptsPickupTrain(train)))
						addItem(pTrains, new JLabel("Yes pick up"), 2, y);
					else
						addItem(pTrains, new JLabel("No pick up"), 2, y);
					y++;
				}
			}
		}
		pTrains.repaint();
		pTrains.revalidate();
	}

	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		if (ae.getSource().equals(typeComboBox))
			updateTrainPane();
	}

	private void updateComboBox() {
		log.debug("update combobox");
		CarTypes.instance().updateComboBox(typeComboBox);
		// remove car types not serviced by this location and track
		for (int i = typeComboBox.getItemCount() - 1; i >= 0; i--) {
			String type = (String) typeComboBox.getItemAt(i);
			if (_location != null && !_location.acceptsTypeName(type)) {
				typeComboBox.removeItem(type);
			}
			if (_track != null && !_track.acceptsTypeName(type)) {
				typeComboBox.removeItem(type);
			}
		}
		typeComboBox.insertItemAt(blank, 0);
	}

	public void dispose() {
		_location.removePropertyChangeListener(this);
		if (_track != null)
			_track.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		removePropertyChangeAllTrains();
		super.dispose();
	}

	public void addPropertyChangeAllTrains() {
		for (Train train : TrainManager.instance().getTrainsByNameList()) {
			train.addPropertyChangeListener(this);
		}
	}

	public void removePropertyChangeAllTrains() {
		for (Train train : TrainManager.instance().getTrainsByNameList()) {
			train.removePropertyChangeListener(this);
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY))
			updateComboBox();
		if (e.getPropertyName().equals(Location.TRAINDIRECTION_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.TRAINDIRECTION_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.DROP_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.PICKUP_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.TRAIN_ROUTE_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.STOPS_CHANGED_PROPERTY))
			updateTrainPane();
	}

	static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationFrame.class.getName());
}

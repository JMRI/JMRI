// YardmasterFrame.java

package jmri.jmrit.operations;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarSetFrame;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import org.apache.log4j.Logger;

/**
 * Common elements for the Conductor and Yardmaster Frames.
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 18630 $
 */

public class CommonConductorYardmasterFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	protected Location _location = null;
	protected Train _train = null;

	protected TrainManager trainManager = TrainManager.instance();
	protected EngineManager engManager = EngineManager.instance();
	protected CarManager carManager = CarManager.instance();
	protected TrainCommon trainCommon = new TrainCommon();

	protected JScrollPane locoPane;
	protected JScrollPane pickupPane;
	protected JScrollPane setoutPane;
	protected JScrollPane movePane;

	// labels
	protected JLabel textRailRoadName = new JLabel();
	protected JLabel textTrainDescription = new JLabel();
	protected JLabel textTrainRouteComment = new JLabel();
	protected JLabel textTrainRouteLocationComment = new JLabel();
	protected JLabel textLocationName = new JLabel();
	protected JLabel textStatus = new JLabel();

	// major buttons
	protected JButton selectButton = new JButton(Bundle.getMessage("Select"));
	protected JButton clearButton = new JButton(Bundle.getMessage("Clear"));
	protected JButton setButton = new JButton(Bundle.getMessage("Set"));
	protected JButton moveButton = new JButton(Bundle.getMessage("Move"));

	// text panes
	protected JTextPane textLocationComment = new JTextPane();
	protected JTextPane textTrainComment = new JTextPane();

	// panels
	protected JPanel pRailRoadName = new JPanel();
	
	protected JPanel pTrainDescription = new JPanel();
	protected JPanel pTrainComment = new JPanel();
	protected JPanel pTrainRouteComment = new JPanel();
	protected JPanel pTrainRouteLocationComment = new JPanel();
	
	protected JPanel pLocationName = new JPanel();
	protected JPanel pLocationComment = new JPanel();
	
	protected JPanel pLocos = new JPanel();
	protected JPanel pPickupLocos = new JPanel();
	protected JPanel pSetoutLocos = new JPanel();
	
	protected JPanel pPickups = new JPanel();
	protected JPanel pSetouts = new JPanel();
	protected JPanel pWorkPanes = new JPanel();	// place car pick ups and set outs side by side
	protected JPanel pMoves = new JPanel();
	
	protected JPanel pStatus = new JPanel();
	protected JPanel pButtons = new JPanel();

	// check boxes
	protected Hashtable<String, JCheckBox> carCheckBoxes = new Hashtable<String, JCheckBox>();
	protected List<RollingStock> rollingStock = new ArrayList<RollingStock>();

	// flags
	protected boolean setMode = false; // when true, cars that aren't selected (checkbox) can be "set"

	public CommonConductorYardmasterFrame() {
		super();
	}
	
	public CommonConductorYardmasterFrame(String s) {
		super(s);
	}

	public void initComponents() {

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		pLocos.setLayout(new BoxLayout(pLocos, BoxLayout.X_AXIS));
		pLocos.add(pPickupLocos);
		pLocos.add(pSetoutLocos);

		locoPane = new JScrollPane(pLocos);
		locoPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Engines")));
		locoPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		pickupPane = new JScrollPane(pPickups);
		pickupPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Pickup")));
		pickupPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		setoutPane = new JScrollPane(pSetouts);
		setoutPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SetOut")));
		setoutPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		movePane = new JScrollPane(pMoves);
		movePane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocalMoves")));
		movePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		// Set up the panels
		pPickupLocos.setLayout(new BoxLayout(pPickupLocos, BoxLayout.Y_AXIS));
		pSetoutLocos.setLayout(new BoxLayout(pSetoutLocos, BoxLayout.Y_AXIS));
		pPickups.setLayout(new BoxLayout(pPickups, BoxLayout.Y_AXIS));
		pSetouts.setLayout(new BoxLayout(pSetouts, BoxLayout.Y_AXIS));
		pMoves.setLayout(new BoxLayout(pMoves, BoxLayout.Y_AXIS));

		// railroad name
		pRailRoadName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RailroadName")));
		pRailRoadName.add(textRailRoadName);

		// location name		
		pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
		pLocationName.add(textLocationName);

		// location comment
		pLocationComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationComment")));
		pLocationComment.add(textLocationComment);
		textLocationComment.setBackground(null);
		
		// train description
		pTrainDescription.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
		pTrainDescription.add(textTrainDescription);

		// train comment
		pTrainComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainComment")));
		pTrainComment.add(textTrainComment);
		textTrainComment.setBackground(null);

		// train route comment
		pTrainRouteComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RouteComment")));
		pTrainRouteComment.add(textTrainRouteComment);

		// train route location comment
		pTrainRouteLocationComment.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("RouteLocationComment")));
		pTrainRouteLocationComment.add(textTrainRouteLocationComment);

		// row 12
		pWorkPanes.setLayout(new BoxLayout(pWorkPanes, BoxLayout.X_AXIS));
		pWorkPanes.add(pickupPane);
		pWorkPanes.add(setoutPane);

		// row 13
		pStatus.setLayout(new GridBagLayout());
		pStatus.setBorder(BorderFactory.createTitledBorder(""));
		addItem(pStatus, textStatus, 0, 0);

		// row 14
		pButtons.setLayout(new GridBagLayout());
		pButtons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Work")));
		addItem(pButtons, selectButton, 0, 0);
		addItem(pButtons, clearButton, 1, 0);
		addItem(pButtons, setButton, 2, 0);

		// setup buttons
		addButtonAction(selectButton);
		addButtonAction(clearButton);
		addButtonAction(setButton);

		setMinimumSize(new Dimension(600, Control.panelHeight));
	}

	// Select, Clear, and Set Buttons
	public void buttonActionPerformed(ActionEvent ae) {
		if (ae.getSource() == selectButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
		if (ae.getSource() == setButton) {
			setMode = !setMode; // toggle setMode
		}
		check();
	}
	
	protected void initialize() {
		removePropertyChangeListerners();
		pPickupLocos.removeAll();
		pSetoutLocos.removeAll();
		pPickups.removeAll();
		pSetouts.removeAll();
		pMoves.removeAll();

		// turn everything off and re-enable if needed
		pickupPane.setVisible(false);
		setoutPane.setVisible(false);
		locoPane.setVisible(false);
		movePane.setVisible(false);

		setButtonText();
	}

	protected void updateComplete() {
		pPickupLocos.repaint();
		pSetoutLocos.repaint();
		pPickups.repaint();
		pSetouts.repaint();
		pMoves.repaint();

		pPickupLocos.validate();
		pSetoutLocos.validate();
		pPickups.validate();
		pSetouts.validate();
		pMoves.validate();
		
		selectButton.setEnabled(carCheckBoxes.size() > 0);
		clearButton.setEnabled(carCheckBoxes.size() > 0);
		check();
	}

	CarSetFrame csf = null;

	// set button for a car, opens the set car window
	public void setCarButtonActionPerfomed(ActionEvent ae) {
		String name = ((JButton) ae.getSource()).getName();
		log.debug("Set button for car " + name);
		Car car = carManager.getById(name);
		if (csf != null)
			csf.dispose();
		csf = new CarSetFrame();
		csf.initComponents();
		csf.loadCar(car);
		// csf.setTitle(Bundle.getMessage("TitleCarSet"));
		csf.setVisible(true);
		csf.setExtendedState(Frame.NORMAL);
	}

	// confirm that all work is done
	protected void checkBoxActionPerformed(ActionEvent ae) {
		check();
	}

	// Determines if all car checkboxes are selected.  Disables the Set button if
	// all checkbox are selected.
	protected void check() {
		Enumeration<JCheckBox> en = carCheckBoxes.elements();
		while (en.hasMoreElements()) {
			JCheckBox checkBox = en.nextElement();
			if (!checkBox.isSelected()) {
				log.debug("Checkbox (" + checkBox.getText() + ") isn't selected ");
				moveButton.setEnabled(false);
				setButton.setEnabled(true);
				return;
			}
		}
		// all selected, work done!
		moveButton.setEnabled(true);
		setButton.setEnabled(false);
		setMode = false;
	}

	protected void selectCheckboxes(boolean enable) {
		Enumeration<JCheckBox> en = carCheckBoxes.elements();
		while (en.hasMoreElements()) {
			JCheckBox checkBox = en.nextElement();
			checkBox.setSelected(enable);
		}
		setMode = false;
	}
	
	protected void updateLocoPanes(RouteLocation rl) {
		// check for locos
		List<String> engList = engManager.getByTrainList(_train);
		for (int k = 0; k < engList.size(); k++) {
			Engine engine = engManager.getById(engList.get(k));
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals("")) {
				locoPane.setVisible(true);
				rollingStock.add(engine);
				engine.addPropertyChangeListener(this);
				JCheckBox checkBox = new JCheckBox(trainCommon.pickupEngine(engine));
				setCheckBoxFont(checkBox);
				pPickupLocos.add(checkBox);
			}
			if (engine.getRouteDestination() == rl) {
				locoPane.setVisible(true);
				rollingStock.add(engine);
				engine.addPropertyChangeListener(this);
				JCheckBox checkBox = new JCheckBox(trainCommon.dropEngine(engine));
				setCheckBoxFont(checkBox);
				pSetoutLocos.add(checkBox);
			}
		}
	}
	
	/**
	 * Block cars by pick up and set out for each location in a train's route.
	 */
	protected void blockCarsByPickUpAndSetOut(RouteLocation rl, boolean isManifest) {
		List<String> routeList = _train.getRoute().getLocationsBySequenceList();
		List<String> carList = carManager.getByTrainDestinationList(_train);
		// block pick ups by destination
		for (int j = 0; j < routeList.size(); j++) {
			RouteLocation rld = _train.getRoute().getLocationById(routeList.get(j));
			for (int k = 0; k < carList.size(); k++) {
				Car car = carManager.getById(carList.get(k));
				if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
						&& car.getRouteDestination() == rld && car.getRouteDestination() != rl) {
					pickupPane.setVisible(true);
					rollingStock.add(car);
					car.addPropertyChangeListener(this);
					if (carCheckBoxes.containsKey("p" + car.getId())) {
						if (setMode && !carCheckBoxes.get("p" + car.getId()).isSelected()) {
							// change to set button so user can remove car from train
							pPickups.add(addSet(car));
						} else {
							pPickups.add(carCheckBoxes.get("p" + car.getId()));
						}
					} else {
						JCheckBox checkBox = new JCheckBox(trainCommon.pickupCar(car, isManifest));
						setCheckBoxFont(checkBox);
						addCheckBoxAction(checkBox);
						pPickups.add(checkBox);
						carCheckBoxes.put("p" + car.getId(), checkBox);
					}
				}
			}
		}
		// set outs
		for (int j = 0; j < carList.size(); j++) {
			Car car = carManager.getById(carList.get(j));
			if ((car.getRouteLocation() != rl && car.getRouteDestination() == rl && !car.getTrackName()
					.equals(""))
					|| (car.getTrackName().equals("") && car.getRouteDestination() == rl)) {
				setoutPane.setVisible(true);
				if (!rollingStock.contains(car)) {
					rollingStock.add(car);
					car.addPropertyChangeListener(this);
				}
				if (carCheckBoxes.containsKey("s" + car.getId())) {
					if (setMode && !carCheckBoxes.get("s" + car.getId()).isSelected()) {
						// change to set button so user can remove car from train
						pSetouts.add(addSet(car));
					} else {
						pSetouts.add(carCheckBoxes.get("s" + car.getId()));
					}
				} else {
					JCheckBox checkBox = new JCheckBox(trainCommon.dropCar(car, isManifest));
					setCheckBoxFont(checkBox);
					addCheckBoxAction(checkBox);
					pSetouts.add(checkBox);
					carCheckBoxes.put("s" + car.getId(), checkBox);
				}
			} else if (car.getRouteLocation() == rl && car.getRouteDestination() == rl
					&& !car.getTrackName().equals("")) {
				movePane.setVisible(true);
				if (!rollingStock.contains(car)) {
					rollingStock.add(car);
					car.addPropertyChangeListener(this);
				}
				if (carCheckBoxes.containsKey("m" + car.getId())) {
					if (setMode && !carCheckBoxes.get("m" + car.getId()).isSelected()) {
						// change to set button so user can remove car from train
						pMoves.add(addSet(car));
					} else {
						pMoves.add(carCheckBoxes.get("m" + car.getId()));
					}
				} else {
					JCheckBox checkBox = new JCheckBox(trainCommon.moveCar(car, isManifest));
					setCheckBoxFont(checkBox);
					addCheckBoxAction(checkBox);
					pMoves.add(checkBox);
					carCheckBoxes.put("m" + car.getId(), checkBox);
				}
			}
		}
	}
	
	/**
	 * Block cars by track, then pick up and set out for each location in a train's route.
	 */
	protected void blockCarsByTrack(RouteLocation rl, boolean isManifest) {
		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
		for (int i = 0; i < trackIds.size(); i++) {
			Track track = rl.getLocation().getTrackById(trackIds.get(i));
			List<String> routeList = _train.getRoute().getLocationsBySequenceList();
			List<String> carList = carManager.getByTrainDestinationList(_train);
			// block pick ups by destination
			for (int j = 0; j < routeList.size(); j++) {
				RouteLocation rld = _train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
							&& car.getTrack() == track
							&& car.getRouteDestination() == rld && car.getRouteDestination() != rl) {
						pickupPane.setVisible(true);
						rollingStock.add(car);
						car.addPropertyChangeListener(this);
						if (carCheckBoxes.containsKey("p" + car.getId())) {
							if (setMode && !carCheckBoxes.get("p" + car.getId()).isSelected()) {
								// change to set button so user can remove car from train
								pPickups.add(addSet(car));
							} else {
								pPickups.add(carCheckBoxes.get("p" + car.getId()));
							}
						} else {
							JCheckBox checkBox = new JCheckBox(trainCommon.pickupCar(car, isManifest));
							setCheckBoxFont(checkBox);
							addCheckBoxAction(checkBox);
							pPickups.add(checkBox);
							carCheckBoxes.put("p" + car.getId(), checkBox);
						}
					}
				}
			}
			// set outs
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if ((car.getRouteLocation() != rl && car.getRouteDestination() == rl && !car.getTrackName()
						.equals("") && car.getDestinationTrack() == track)
						|| (car.getTrackName().equals("") && car.getRouteDestination() == rl)) {
					setoutPane.setVisible(true);
					if (!rollingStock.contains(car)) {
						rollingStock.add(car);
						car.addPropertyChangeListener(this);
					}
					if (carCheckBoxes.containsKey("s" + car.getId())) {
						if (setMode && !carCheckBoxes.get("s" + car.getId()).isSelected()) {
							// change to set button so user can remove car from train
							pSetouts.add(addSet(car));
						} else {
							pSetouts.add(carCheckBoxes.get("s" + car.getId()));
						}
					} else {
						JCheckBox checkBox = new JCheckBox(trainCommon.dropCar(car, isManifest));
						setCheckBoxFont(checkBox);
						addCheckBoxAction(checkBox);
						pSetouts.add(checkBox);
						carCheckBoxes.put("s" + car.getId(), checkBox);
					}
				} else if (car.getRouteLocation() == rl && car.getRouteDestination() == rl
						&& !car.getTrackName().equals("") && car.getTrack() == track) {
					movePane.setVisible(true);
					if (!rollingStock.contains(car)) {
						rollingStock.add(car);
						car.addPropertyChangeListener(this);
					}
					if (carCheckBoxes.containsKey("m" + car.getId())) {
						if (setMode && !carCheckBoxes.get("m" + car.getId()).isSelected()) {
							// change to set button so user can remove car from train
							pMoves.add(addSet(car));
						} else {
							pMoves.add(carCheckBoxes.get("m" + car.getId()));
						}
					} else {
						JCheckBox checkBox = new JCheckBox(trainCommon.moveCar(car, isManifest));
						setCheckBoxFont(checkBox);
						addCheckBoxAction(checkBox);
						pMoves.add(checkBox);
						carCheckBoxes.put("m" + car.getId(), checkBox);
					}
				}
			}
		}
	}

	// replace the car checkbox and text with the car's road and number and a Set button
	protected JPanel addSet(Car car) {
		JPanel pSet = new JPanel();
		pSet.setLayout(new GridBagLayout());
		JButton carSetButton = new JButton(Bundle.getMessage("Set"));
		carSetButton.setName(car.getId());
		carSetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCarButtonActionPerfomed(e);
			}
		});
		JLabel label = new JLabel(car.toString());
		addItem(pSet, label, 0, 0);
		addItemLeft(pSet, carSetButton, 1, 0);
		return pSet;
	}

	protected void setCheckBoxFont(JCheckBox checkBox) {
		if (Setup.isTabEnabled()) {
			Font font = new Font(Setup.getFontName(), Font.PLAIN, checkBox.getFont().getSize());
			checkBox.setFont(font);
		}
	}

	protected void setButtonText() {
		if (setMode)
			setButton.setText(Bundle.getMessage("Done"));
		else
			setButton.setText(Bundle.getMessage("Set"));
	}

	// returns one of two possible departure strings for a train
	protected String getStatus(RouteLocation rl) {
		if (Setup.isPrintLoadsAndEmptiesEnabled()) {
			int emptyCars = _train.getNumberEmptyCarsInTrain(rl);
			return MessageFormat.format(Bundle.getMessage("TrainDepartsLoads"), new Object[] {
					TrainCommon.splitString(rl.getName()), rl.getTrainDirectionString(),
					_train.getNumberCarsInTrain(rl) - emptyCars, emptyCars, _train.getTrainLength(rl),
					Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl) });
		} else {
			return MessageFormat.format(Bundle.getMessage("TrainDepartsCars"), new Object[] {
					TrainCommon.splitString(rl.getName()), rl.getTrainDirectionString(),
					_train.getNumberCarsInTrain(rl), _train.getTrainLength(rl),
					Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl) });
		}
	}

	protected void removePropertyChangeListerners() {
		for (int i = 0; i < rollingStock.size(); i++) {
			rollingStock.get(i).removePropertyChangeListener(this);
		}
		rollingStock.clear();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e){
		//if (Control.showProperty && log.isDebugEnabled()) 
		log.debug("Property change " +e.getPropertyName() + " for: "+e.getSource().toString()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue()); // NOI18N
	}
	
	static Logger log = Logger.getLogger(CommonConductorYardmasterFrame.class.getName());
}

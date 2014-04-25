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
import jmri.jmrit.operations.trains.TrainManifestText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected JTextPane textTrainRouteComment = new JTextPane();
	protected JTextPane textTrainRouteLocationComment = new JTextPane();

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
	protected JPanel pWorkPanes = new JPanel();	// place car pick ups and set outs side by side using two columns
	protected JPanel pMoves = new JPanel();
	
	protected JPanel pStatus = new JPanel();
	protected JPanel pButtons = new JPanel();

	// check boxes
	protected Hashtable<String, JCheckBox> carCheckBoxes = new Hashtable<String, JCheckBox>();
	protected List<RollingStock> rollingStock = new ArrayList<RollingStock>();

	// flags
	protected boolean isSetMode = false; // when true, cars that aren't selected (checkbox) can be "set"

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
		textTrainRouteComment.setBackground(null);

		// train route location comment
		pTrainRouteLocationComment.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("RouteLocationComment")));
		pTrainRouteLocationComment.add(textTrainRouteLocationComment);
		textTrainRouteLocationComment.setBackground(null);

		// row 12
		if ((getPreferredSize().width > Control.widePanelWidth && Setup.isTabEnabled())
				|| (getPreferredSize().width > Control.widePanelWidth-200 && !Setup.isTabEnabled()))
			pWorkPanes.setLayout(new BoxLayout(pWorkPanes, BoxLayout.X_AXIS));
		else
			pWorkPanes.setLayout(new BoxLayout(pWorkPanes, BoxLayout.Y_AXIS));
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

		setMinimumSize(new Dimension(Control.panelWidth, Control.panelHeight));
	}

	// Select, Clear, and Set Buttons
	public void buttonActionPerformed(ActionEvent ae) {
		if (ae.getSource() == selectButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
		if (ae.getSource() == setButton) {
			isSetMode = !isSetMode; // toggle setMode
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
		pWorkPanes.setVisible(false);
		pickupPane.setVisible(false);
		setoutPane.setVisible(false);
		locoPane.setVisible(false);
		movePane.setVisible(false);
		
		pTrainRouteLocationComment.setVisible(false);
		pLocationComment.setVisible(false);

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

	// action for set button for a car, opens the set car window
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
		isSetMode = false;
	}

	protected void selectCheckboxes(boolean enable) {
		Enumeration<JCheckBox> en = carCheckBoxes.elements();
		while (en.hasMoreElements()) {
			JCheckBox checkBox = en.nextElement();
			checkBox.setSelected(enable);
		}
		isSetMode = false;
	}
	
	protected void updateLocoPanes(RouteLocation rl) {
		// check for locos
		List<Engine> engList = engManager.getByTrainBlockingList(_train);
		for (Engine engine : engList) {
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
	 * Block cars by track (optional), then pick up and set out for each location in a train's route. This shows each
	 * car with a check box or with a set button. The set button is displayed when the checkbox isn't selected and the
	 * display is in "set" mode. If the car is a utility. Show the number of cars that have the same attributes, and not
	 * the car's road and number. Each car is displayed only once in one of three panes. The three panes are pick up,
	 * set out, or local move. To keep track of each car and which pane to use, they are placed in the list
	 * "rollingStock" with the prefix "p", "s" or "m" and the car's unique id.
	 */
	protected void blockCars(RouteLocation rl, boolean isManifest) {
		List<Track> tracks = rl.getLocation().getTrackByNameList(null);
		for (Track track : tracks) {
			List<RouteLocation> routeList = _train.getRoute().getLocationsBySequenceList();
			List<Car> carList = carManager.getByTrainDestinationList(_train);
			// block pick ups by destination
			for (RouteLocation rld : routeList) {
				for (Car car : carList) {
					// determine if car is a pick up from the right track
					if (car.getTrack() != null
							&& (!Setup.isSortByTrackEnabled() || car.getTrackName().equals(track.getName()))
							&& car.getRouteLocation() == rl && car.getRouteDestination() == rld
							&& car.getRouteDestination() != rl) {
						// yes we have a pick up
						pWorkPanes.setVisible(true);
						pickupPane.setVisible(true);
						if (!rollingStock.contains(car)) {
							rollingStock.add(car);
							car.addPropertyChangeListener(this);
						}
						// did we already process this car?
						if (carCheckBoxes.containsKey("p" + car.getId())) {
							if (isSetMode && !carCheckBoxes.get("p" + car.getId()).isSelected()) {
								// change to set button so user can remove car from train
								pPickups.add(addSet(car));
							} else {
								pPickups.add(carCheckBoxes.get("p" + car.getId()));
							}
						// figure out the checkbox text, either single car or utility
						} else {
							String text = trainCommon.pickupCar(car, isManifest);
							if (car.isUtility()) {
								text = trainCommon.pickupUtilityCars(carList, car, rl, rld, isManifest);
								if (text == null)
									continue;	// this car type has already been processed
							}
							JCheckBox checkBox = new JCheckBox(text);
							setCheckBoxFont(checkBox);
							addCheckBoxAction(checkBox);
							pPickups.add(checkBox);
							carCheckBoxes.put("p" + car.getId(), checkBox);
						}
					}
				}
			}
			// set outs
			for (Car car : carList) {
				if (!car.getTrackName().equals("")
						&& car.getDestinationTrack() != null
						&& (!Setup.isSortByTrackEnabled() || car.getDestinationTrack().getName().equals(track.getName()))
						&& (car.getRouteLocation() != rl && car.getRouteDestination() == rl)
						|| (car.getTrackName().equals("")
								&& car.getDestinationTrack() != null
								&& (!Setup.isSortByTrackEnabled() || car.getDestinationTrack().getName().equals(track.getName())) 
								&& car.getRouteDestination() == rl)) {
					// we have set outs
					pWorkPanes.setVisible(true);
					setoutPane.setVisible(true);
					if (!rollingStock.contains(car)) {
						rollingStock.add(car);
						car.addPropertyChangeListener(this);
					}
					if (carCheckBoxes.containsKey("s" + car.getId())) {
						if (isSetMode && !carCheckBoxes.get("s" + car.getId()).isSelected()) {
							// change to set button so user can remove car from train
							pSetouts.add(addSet(car));
						} else {
							pSetouts.add(carCheckBoxes.get("s" + car.getId()));
						}
					} else {
						String text = trainCommon.dropCar(car, isManifest);
						if (car.isUtility()) {
							text = trainCommon.setoutUtilityCars(carList, car, rl, false, isManifest);
							if (text == null)
								continue;	// this car type has already been processed
						}
						JCheckBox checkBox = new JCheckBox(text);
						setCheckBoxFont(checkBox);
						addCheckBoxAction(checkBox);
						pSetouts.add(checkBox);
						carCheckBoxes.put("s" + car.getId(), checkBox);
					}
				// local move?
				} else if (!car.getTrackName().equals("") && car.getDestinationTrack() != null 
						&&(!Setup.isSortByTrackEnabled() || car.getDestinationTrack().getName().equals(track.getName()))
						&& car.getRouteLocation() == rl
						&& car.getRouteDestination() == rl) {
					movePane.setVisible(true);
					if (!rollingStock.contains(car)) {
						rollingStock.add(car);
						car.addPropertyChangeListener(this);
					}
					if (carCheckBoxes.containsKey("m" + car.getId())) {
						if (isSetMode && !carCheckBoxes.get("m" + car.getId()).isSelected()) {
							// change to set button so user can remove car from train
							pMoves.add(addSet(car));
						} else {
							pMoves.add(carCheckBoxes.get("m" + car.getId()));
						}
					} else {
						String text = trainCommon.moveCar(car, isManifest);
						if (car.isUtility()) {
							text = trainCommon.setoutUtilityCars(carList, car, rl, true, isManifest);
							if (text == null)
								continue;	// this car type has already been processed
						}
						JCheckBox checkBox = new JCheckBox(text);
						setCheckBoxFont(checkBox);
						addCheckBoxAction(checkBox);
						pMoves.add(checkBox);
						carCheckBoxes.put("m" + car.getId(), checkBox);
					}
				}
			}
			// if not sorting by track, we're done
			if (!Setup.isSortByTrackEnabled())
				break;
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
		JLabel label = new JLabel(TrainCommon.padString(car.toString(), Control.max_len_string_attibute
				+ Control.max_len_string_road_number));
		setLabelFont(label);
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
	
	protected void setLabelFont(JLabel label) {
		if (Setup.isTabEnabled()) {
			Font font = new Font(Setup.getFontName(), Font.PLAIN, label.getFont().getSize());
			label.setFont(font);
		}
	}

	protected void setButtonText() {
		if (isSetMode)
			setButton.setText(Bundle.getMessage("Done"));
		else
			setButton.setText(Bundle.getMessage("Set"));
	}

	// returns one of two possible departure strings for a train
	protected String getStatus(RouteLocation rl) {
		if (Setup.isPrintLoadsAndEmptiesEnabled()) {
			int emptyCars = _train.getNumberEmptyCarsInTrain(rl);
			return MessageFormat.format(TrainManifestText.getStringTrainDepartsLoads(), new Object[] {
					TrainCommon.splitString(rl.getName()), rl.getTrainDirectionString(),
					_train.getNumberCarsInTrain(rl) - emptyCars, emptyCars, _train.getTrainLength(rl),
					Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl) });
		} else {
			return MessageFormat.format(TrainManifestText.getStringTrainDepartsCars(), new Object[] {
					TrainCommon.splitString(rl.getName()), rl.getTrainDirectionString(),
					_train.getNumberCarsInTrain(rl), _train.getTrainLength(rl),
					Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl) });
		}
	}

	protected void removePropertyChangeListerners() {
		for (RollingStock rs : rollingStock)
			rs.removePropertyChangeListener(this);
		rollingStock.clear();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e){
		//if (Control.showProperty && log.isDebugEnabled()) 
		log.debug("Property change " +e.getPropertyName() + " for: "+e.getSource().toString()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue()); // NOI18N
	}
	
	static Logger log = LoggerFactory.getLogger(CommonConductorYardmasterFrame.class.getName());
}

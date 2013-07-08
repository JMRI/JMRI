// CarSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockSetFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;

/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2013
 * @version $Revision$
 */

public class CarSetFrame extends RollingStockSetFrame implements java.beans.PropertyChangeListener {

	protected static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	CarManager carManager = CarManager.instance();
	CarManagerXml carManagerXml = CarManagerXml.instance();

	Car _car;

	JLabel textName4 = new JLabel(Bundle.getMessage("Name"));
	JLabel textTrack4 = new JLabel(Bundle.getMessage("Track"));

	// combo boxes
	protected JComboBox destReturnWhenEmptyBox = LocationManager.instance().getComboBox();
	protected JComboBox trackReturnWhenEmptyBox = new JComboBox();
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);
	JComboBox kernelComboBox = carManager.getKernelComboBox();

	// buttons
	JButton editLoadButton = new JButton(Bundle.getMessage("Edit"));
	JButton editKernelButton = new JButton(Bundle.getMessage("Edit"));

	// check boxes
	protected JCheckBox ignoreRWECheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
	protected JCheckBox autoReturnWhenEmptyTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
	protected JCheckBox ignoreLoadCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
	protected JCheckBox ignoreKernelCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));

	// Auto checkbox state
	private static boolean autoReturnWhenEmptyTrackCheckBoxSelected = false;

	CarLoadEditFrame lef = null;

	private static boolean enableDestination = false;

	public CarSetFrame() {
		super(Bundle.getMessage("TitleCarSet"));
	}

	public void initComponents() {
		super.initComponents();

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new EnableDestinationAction(Bundle.getMessage("MenuEnableDestination"), this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_CarsSet", true); // NOI18N

		// optional panel return when empty
		pOptional.setVisible(true);
		pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));
		pOptional.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptional")));

		// row 5
		JPanel pReturnWhenEmpty = new JPanel();
		pReturnWhenEmpty.setLayout(new GridBagLayout());
		pReturnWhenEmpty.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutReturnWhenEmpty")));
		addItem(pReturnWhenEmpty, textName4, 1, 0);
		addItem(pReturnWhenEmpty, textTrack4, 2, 0);
		addItem(pReturnWhenEmpty, ignoreRWECheckBox, 0, 1);
		addItem(pReturnWhenEmpty, destReturnWhenEmptyBox, 1, 1);
		addItem(pReturnWhenEmpty, trackReturnWhenEmptyBox, 2, 1);
		addItem(pReturnWhenEmpty, autoReturnWhenEmptyTrackCheckBox, 3, 1);
		pOptional.add(pReturnWhenEmpty);

		// add load fields
		JPanel pLoad = new JPanel();
		pLoad.setLayout(new GridBagLayout());
		pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
		addItem(pLoad, ignoreLoadCheckBox, 1, 0);
		addItem(pLoad, loadComboBox, 2, 0);
		addItem(pLoad, editLoadButton, 3, 0);
		pOptional.add(pLoad);
		
		// add kernel fields
		JPanel pKernel = new JPanel();
		pKernel.setLayout(new GridBagLayout());
		pKernel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Kernel")));
		addItem(pKernel, ignoreKernelCheckBox, 1, 0);
		addItem(pKernel, kernelComboBox, 2, 0);
		addItem(pKernel, editKernelButton, 3, 0);
		pOptional.add(pKernel);

		// don't show ignore checkboxes
		ignoreRWECheckBox.setVisible(false);
		ignoreLoadCheckBox.setVisible(false);
		ignoreKernelCheckBox.setVisible(false);

		autoReturnWhenEmptyTrackCheckBox.setSelected(autoReturnWhenEmptyTrackCheckBoxSelected);

		// setup combobox
		addComboBoxAction(destReturnWhenEmptyBox);
		addComboBoxAction(loadComboBox);

		// setup button
		addButtonAction(editLoadButton);
		addButtonAction(editKernelButton);

		// setup checkboxes
		addCheckBoxAction(ignoreRWECheckBox);
		addCheckBoxAction(autoReturnWhenEmptyTrackCheckBox);
		addCheckBoxAction(ignoreLoadCheckBox);
		addCheckBoxAction(ignoreKernelCheckBox);

		// tool tips
		ignoreRWECheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
		ignoreLoadCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
		ignoreKernelCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
		outOfServiceCheckBox.setToolTipText(Bundle.getMessage("TipCarOutOfService"));
		autoReturnWhenEmptyTrackCheckBox.setToolTipText(Bundle.getMessage("rsTipAutoTrack"));

		// get notified if combo box gets modified
		CarLoads.instance().addPropertyChangeListener(this);
		carManager.addPropertyChangeListener(this);

		packFrame();
	}

	public void loadCar(Car car) {
		_car = car;
		load(car);
	}

	protected void updateComboBoxes() {
		super.updateComboBoxes();

		finalDestinationBox.setSelectedItem(_car.getFinalDestination());
		locationManager.updateComboBox(destReturnWhenEmptyBox);
		destReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestination());

		updateFinalDestination();
		updateReturnWhenEmpty();
		updateLoadComboBox();
		updateKernelComboBox();
		updateTrainComboBox();
	}

	protected void enableComponents(boolean enabled) {

		// If routing is disable, the RWE and Final Destination fields do not work
		if (!Setup.isCarRoutingEnabled()) {
			ignoreRWECheckBox.setSelected(true);
			ignoreFinalDestinationCheckBox.setSelected(true);
		}

		super.enableComponents(enabled);

		ignoreRWECheckBox.setEnabled(Setup.isCarRoutingEnabled() & enabled);
		destReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() & enabled);
		trackReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() & enabled);
		autoReturnWhenEmptyTrackCheckBox.setEnabled(!ignoreRWECheckBox.isSelected() & enabled);

		ignoreLoadCheckBox.setEnabled(enabled);
		loadComboBox.setEnabled(!ignoreLoadCheckBox.isSelected() & enabled);
		editLoadButton.setEnabled(!ignoreLoadCheckBox.isSelected() & enabled & _car != null);
		
		ignoreKernelCheckBox.setEnabled(enabled);
		kernelComboBox.setEnabled(!ignoreKernelCheckBox.isSelected() & enabled);
		editKernelButton.setEnabled(!ignoreKernelCheckBox.isSelected() & enabled & _car != null);
		
		// if car in a built train, enable destination fields
		boolean enableDest = enableDestination
				|| (_car != null && _car.getTrain() != null && _car.getTrain().isBuilt());

		destinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enableDest & enabled);
		trackDestinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enableDest & enabled);
		autoDestinationTrackCheckBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enableDest
				& enabled);
	}

	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		_disableComboBoxUpdate = true; // stop updates
		super.comboBoxActionPerformed(ae);
		if (ae.getSource() == finalDestinationBox) {
			updateFinalDestination();
		}
		if (ae.getSource() == destReturnWhenEmptyBox) {
			updateReturnWhenEmpty();
		}
		_disableComboBoxUpdate = false;
	}

	private boolean editActive = false;
	CarAttributeEditFrame f;

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		super.buttonActionPerformed(ae);
		if (ae.getSource() == editLoadButton && _car != null) {
			if (lef != null)
				lef.dispose();
			lef = new CarLoadEditFrame();
			lef.setLocationRelativeTo(this);
			lef.initComponents(_car.getTypeName(), (String) loadComboBox.getSelectedItem());
		}
		if (ae.getSource() == editKernelButton) {
			if (editActive) {
				f.dispose();
			}
			f = new CarAttributeEditFrame();
			f.setLocationRelativeTo(this);
			f.addPropertyChangeListener(this);
			editActive = true;
			f.initComponents(Bundle.getMessage("Kernel"), (String) kernelComboBox.getSelectedItem());
		}
	}

	protected boolean save() {
		if (change(_car)) {
			OperationsXml.save();
			return true;
		}
		return false;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	protected boolean change(Car car) {
		// save the auto button
		autoReturnWhenEmptyTrackCheckBoxSelected = autoReturnWhenEmptyTrackCheckBox.isSelected();

		// set final destination fields before destination in case there's a schedule at destination
		if (!ignoreFinalDestinationCheckBox.isSelected()) {
			if (finalDestinationBox.getSelectedItem() == null
					|| finalDestinationBox.getSelectedItem().equals("")) {
				car.setFinalDestination(null);
				car.setFinalDestinationTrack(null);
			} else {
				Track finalDestTrack = null;
				if (finalDestTrackBox.getSelectedItem() != null
						&& !finalDestTrackBox.getSelectedItem().equals(""))
					finalDestTrack = (Track) finalDestTrackBox.getSelectedItem();
				if (finalDestTrack != null && car.getFinalDestinationTrack() != finalDestTrack
						&& finalDestTrack.getLocType().equals(Track.STAGING)) {
					log.debug("Destination track (" + finalDestTrack.getName() + ") is staging");
					JOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"), Bundle
							.getMessage("rsCanNotFinal"), JOptionPane.ERROR_MESSAGE);
					return false;
				}
				String status = car.testDestination((Location) finalDestinationBox.getSelectedItem(),
						finalDestTrack);
				if (!status.equals(Track.OKAY)) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
							.getMessage("rsCanNotFinalMsg"), new Object[] { car.toString(), status }), Bundle
							.getMessage("rsCanNotFinal"), JOptionPane.WARNING_MESSAGE);
				}
				car.setFinalDestination((Location) finalDestinationBox.getSelectedItem());
				car.setFinalDestinationTrack(finalDestTrack);
			}
		}
		// car load
		if (!ignoreLoadCheckBox.isSelected() && loadComboBox.getSelectedItem() != null) {
			String load = (String) loadComboBox.getSelectedItem();
			if (CarLoads.instance().containsName(car.getTypeName(), load))
				car.setLoadName(load);
		}
		// kernel
		if (!ignoreKernelCheckBox.isSelected() && kernelComboBox.getSelectedItem() != null) {
			if (kernelComboBox.getSelectedItem().equals("")) {
				car.setKernel(null);
			} else {
				car.setKernel(carManager.getKernelByName((String) kernelComboBox.getSelectedItem()));
				// if car has FRED make lead
				if (car.hasFred())
					car.getKernel().setLead(car);
			}
		}		
		// save car's track
		Track saveTrack = car.getTrack();
		if (!super.change(car))
			return false;
		// return when empty fields
		if (!ignoreRWECheckBox.isSelected()) {
			if (destReturnWhenEmptyBox.getSelectedItem() == null
					|| destReturnWhenEmptyBox.getSelectedItem().equals("")) {
				car.setReturnWhenEmptyDestination(null);
				car.setReturnWhenEmptyDestTrack(null);
			} else {
				if (trackReturnWhenEmptyBox.getSelectedItem() != null
						&& !trackReturnWhenEmptyBox.getSelectedItem().equals("")) {
					Track rwe = (Track) trackReturnWhenEmptyBox.getSelectedItem();
					if (rwe != null && rwe.getLocType().equals(Track.STAGING)) {
						log.debug("Return when empty track (" + rwe.getName() + ") is staging");
						JOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"), Bundle
								.getMessage("rsCanNotRWE"), JOptionPane.ERROR_MESSAGE);
						return false;
					}
					String status = car.testDestination((Location) destReturnWhenEmptyBox.getSelectedItem(),
							(Track) trackReturnWhenEmptyBox.getSelectedItem());
					if (!status.equals(Track.OKAY)) {
						JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
								.getMessage("rsCanNotRWEMsg"), new Object[] { car.toString(), status }),
								Bundle.getMessage("rsCanNotRWE"), JOptionPane.WARNING_MESSAGE);
					}
					car.setReturnWhenEmptyDestTrack((Track) trackReturnWhenEmptyBox.getSelectedItem());
				} else {
					car.setReturnWhenEmptyDestTrack(null);
				}
				car.setReturnWhenEmptyDestination((Location) destReturnWhenEmptyBox.getSelectedItem());
			}
		}
		// check to see if there's a schedule when placing the car at a spur
		if (!ignoreLocationCheckBox.isSelected() && trackLocationBox.getSelectedItem() != null
				&& !trackLocationBox.getSelectedItem().equals("")
				&& saveTrack != trackLocationBox.getSelectedItem()) {
			Track track = (Track) trackLocationBox.getSelectedItem();
			if (track.getSchedule() != null) {
				if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
						.getMessage("rsDoYouWantSchedule"), new Object[] { car.toString() }), MessageFormat
						.format(Bundle.getMessage("rsSpurHasSchedule"), new Object[] { track.getName(),
								track.getScheduleName() }), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					String results = track.checkSchedule(car);
					if (!results.equals(Track.OKAY)) {
						JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
								.getMessage("rsNotAbleToApplySchedule"), new Object[] { results }), Bundle
								.getMessage("rsApplyingScheduleFailed"), JOptionPane.ERROR_MESSAGE);
						// restore previous location and track so we'll ask to test schedule again
						if (saveTrack != null)
							car.setLocation(saveTrack.getLocation(), saveTrack);
						else
							car.setLocation(null, null);
						return false;
					}
					// now apply schedule to car
					track.scheduleNext(car);
					// change load to ship load
					if (!car.getNextLoadName().equals("")) {
						car.setLoadName(car.getNextLoadName());
						car.setNextLoadName("");
					}
					// change next wait to wait now!
					if (car.getNextWait() > 0) {
						car.setWait(car.getNextWait());
						car.setNextWait(0);
					}
				}
			}
		}
		// determine if train services this car's load
		if (car.getTrain() != null) {
			Train train = car.getTrain();
			if (!train.acceptsLoad(car.getLoadName(), car.getTypeName())) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
						.getMessage("carTrainNotServLoad"), new Object[] { car.getLoadName(), train.getName() }),
						Bundle.getMessage("rsNotMove"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (car.getLocation() != null && car.getDestination() != null && !train.services(car)) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
						.getMessage("carTrainNotService"), new Object[] { train.getName() }), Bundle
						.getMessage("rsNotMove"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		checkTrain(car);
		// is this car part of a kernel?
		if (car.getKernel() != null) {
			List<RollingStock> list = car.getKernel().getGroup();
			if (list.size() > 1) {
				if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
						Bundle.getMessage("carInKernel"), new Object[] { car.toString() }), MessageFormat
						.format(Bundle.getMessage("carPartKernel"), new Object[] { _car.getKernelName() }),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					if (!updateGroup(list))
						return false;
				}
			}
		}
		return true;
	}

	protected boolean updateGroup(List<RollingStock> list) {
		for (int i = 0; i < list.size(); i++) {
			Car car = (Car) list.get(i);
			if (car == _car)
				continue;
			// make all cars in kernel the same
			if (!ignoreRWECheckBox.isSelected()) {
				car.setReturnWhenEmptyDestination(_car.getReturnWhenEmptyDestination());
				car.setReturnWhenEmptyDestTrack(_car.getReturnWhenEmptyDestTrack());
			}
			if (!ignoreFinalDestinationCheckBox.isSelected()) {
				car.setFinalDestination(_car.getFinalDestination());
				car.setFinalDestinationTrack(_car.getFinalDestinationTrack());
			}
			// update car load
			if (!ignoreLoadCheckBox.isSelected()
					&& CarLoads.instance().containsName(car.getTypeName(), _car.getLoadName()))
				car.setLoadName(_car.getLoadName());
			// update kernel
			if (!ignoreKernelCheckBox.isSelected())
				car.setKernel(_car.getKernel());
		}
		return super.updateGroup(list);
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		_disableComboBoxUpdate = true; // stop updates
		super.checkBoxActionPerformed(ae);
		if (ae.getSource() == autoFinalDestTrackCheckBox)
			updateFinalDestination();
		if (ae.getSource() == autoReturnWhenEmptyTrackCheckBox)
			updateReturnWhenEmpty();
		if (ae.getSource() == autoTrainCheckBox)
			updateTrainComboBox();
		if (ae.getSource() == ignoreRWECheckBox) {
			destReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
			trackReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
			autoReturnWhenEmptyTrackCheckBox.setEnabled(!ignoreRWECheckBox.isSelected());
		}
		if (ae.getSource() == ignoreLoadCheckBox) {
			loadComboBox.setEnabled(!ignoreLoadCheckBox.isSelected());
			editLoadButton.setEnabled(!ignoreLoadCheckBox.isSelected() & _car != null);
		}
		if (ae.getSource() == ignoreKernelCheckBox) {
			kernelComboBox.setEnabled(!ignoreKernelCheckBox.isSelected());
			editKernelButton.setEnabled(!ignoreKernelCheckBox.isSelected());
		}
		_disableComboBoxUpdate = false;
	}

	protected void updateReturnWhenEmpty() {
		if (destReturnWhenEmptyBox.getSelectedItem() != null) {
			if (destReturnWhenEmptyBox.getSelectedItem().equals("")) {
				trackReturnWhenEmptyBox.removeAllItems();
			} else {
				log.debug("CarSetFrame sees return when empty: " + destReturnWhenEmptyBox.getSelectedItem());
				Location l = (Location) destReturnWhenEmptyBox.getSelectedItem();
				l.updateComboBox(trackReturnWhenEmptyBox, _car,
						autoReturnWhenEmptyTrackCheckBox.isSelected(), true);
				if (_car != null && _car.getReturnWhenEmptyDestination() != null
						&& _car.getReturnWhenEmptyDestination().equals(l)
						&& _car.getReturnWhenEmptyDestTrack() != null)
					trackReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestTrack());
				packFrame();
			}
		}
	}

	protected void updateFinalDestination() {
		if (finalDestinationBox.getSelectedItem() != null) {
			if (finalDestinationBox.getSelectedItem().equals("")) {
				finalDestTrackBox.removeAllItems();
			} else {
				log.debug("CarSetFrame sees final destination: " + finalDestinationBox.getSelectedItem());
				Location l = (Location) finalDestinationBox.getSelectedItem();
				l.updateComboBox(finalDestTrackBox, _car, autoFinalDestTrackCheckBox.isSelected(), true);
				if (_car != null && _car.getFinalDestination() != null
						&& _car.getFinalDestination().equals(l) && _car.getFinalDestinationTrack() != null)
					finalDestTrackBox.setSelectedItem(_car.getFinalDestinationTrack());
				packFrame();
			}
		}
	}

	protected void updateFinalDestinationComboBoxes() {
		if (_car != null) {
			log.debug("Updating final destinations for car (" + _car.toString() + ")");
			finalDestinationBox.setSelectedItem(_car.getFinalDestination());
		}
		updateFinalDestination();
	}

	protected void updateLoadComboBox() {
		if (_car != null) {
			log.debug("Updating load box for car (" + _car.toString() + ")");
			CarLoads.instance().updateComboBox(_car.getTypeName(), loadComboBox);
			loadComboBox.setSelectedItem(_car.getLoadName());
		}
	}
	
	protected void updateKernelComboBox() {
		carManager.updateKernelComboBox(kernelComboBox);
		if (_car != null) {
			kernelComboBox.setSelectedItem(_car.getKernelName());
		}
	}

	protected void updateTrainComboBox() {
		if (_car != null && autoTrainCheckBox.isSelected()) {
			log.debug("Updating train box for car (" + _car.toString() + ")");
			trainManager.updateComboBox(trainBox, _car);
		} else {
			trainManager.updateComboBox(trainBox);
		}
		if (_car != null)
			trainBox.setSelectedItem(_car.getTrain());
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public void setDestinationEnabled(boolean enable) {
		enableDestination = !enableDestination;
		enableComponents(!locationUnknownCheckBox.isSelected());
	}

	protected void packFrame() {
		super.packFrame();
		if (getHeight() < 650)
			setSize(getWidth(), 650);
	}

	public void dispose() {
		CarLoads.instance().removePropertyChangeListener(this);
		carManager.removePropertyChangeListener(this);
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("PropertyChange " + e.getPropertyName() + " " + e.getNewValue());
		if (_disableComboBoxUpdate) {
			log.debug("Combobox update is disabled");
			return;
		}
		super.propertyChange(e);
		if (e.getPropertyName().equals(Car.FINAL_DESTINATION_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Car.FINAL_DESTINATION_TRACK_CHANGED_PROPERTY))
			updateFinalDestinationComboBoxes();
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)) {
			updateLoadComboBox();
		}
		if (e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY)) {
			updateKernelComboBox();
		}
		if (e.getPropertyName().equals(CarAttributeEditFrame.DISPOSE)) {
			editActive = false;
		}
	}

	static Logger log = LoggerFactory.getLogger(CarSetFrame.class.getName());
}

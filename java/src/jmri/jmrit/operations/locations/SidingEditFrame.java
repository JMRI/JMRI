// SidingEditFrame.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.operations.setup.Control;

/**
 * Frame for user edit of a spur.
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011
 * @version $Revision$
 */

public class SidingEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	// labels, buttons, etc. for spurs
	JLabel textSchedule = new JLabel(Bundle.getString("DeliverySchedule"));
	JLabel textSchError = new JLabel();
	JButton editScheduleButton = new JButton(Bundle.getString("Edit"));
	JComboBox comboBoxSchedules = ScheduleManager.instance().getComboBox();

	JPanel panelSchedule = panelOpt4;

	public SidingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.SIDING;

		// setup the optional panel with schedule stuff
		panelSchedule.setLayout(new GridBagLayout());
		panelSchedule.setBorder(BorderFactory.createTitledBorder(Bundle
				.getString("DeliverySchedule")));
		addItem(panelSchedule, comboBoxSchedules, 0, 0);
		addItem(panelSchedule, editScheduleButton, 1, 0);
		addItem(panelSchedule, textSchError, 2, 0);

		super.initComponents(location, track);

		_toolMenu.add(new AlternateTrackAction(this));
		_toolMenu.add(new ChangeTrackTypeAction(this));
		_toolMenu.add(new IgnoreUsedTrackAction(this));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Sidings", true); // NOI18N

		// override text strings for tracks
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getString("TrainSiding")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getString("TypesSiding")));
		deleteTrackButton.setText(Bundle.getString("DeleteSiding"));
		addTrackButton.setText(Bundle.getString("AddSiding"));
		saveTrackButton.setText(Bundle.getString("SaveSiding"));

		// Select the spur's Schedule
		updateScheduleComboBox();

		ScheduleManager.instance().addPropertyChangeListener(this);

		// setup buttons
		addButtonAction(editScheduleButton);
		// finish
		panelOrder.setVisible(false); // Car order out of spurs is not available
		packFrame();
		setVisible(true);
	}

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == editScheduleButton) {
			editAddSchedule();
		}
		super.buttonActionPerformed(ae);
	}

	ScheduleEditFrame sef = null;

	private void editAddSchedule() {
		log.debug("Edit/add schedule");
		if (sef != null)
			sef.dispose();
		sef = new ScheduleEditFrame();
		Object selected = comboBoxSchedules.getSelectedItem();
		if (selected != null && !selected.equals("")) {
			Schedule schedule = (Schedule) selected;
			sef.setTitle(MessageFormat.format(Bundle.getString("TitleScheduleEdit"),
					new Object[] { _track.getName() }));
			sef.initComponents(schedule, _location, _track);
		} else {
			sef.setTitle(MessageFormat.format(Bundle.getString("TitleScheduleAdd"),
					new Object[] { _track.getName() }));
			sef.initComponents(null, _location, _track);
		}
	}

	protected void enableButtons(boolean enabled) {
		editScheduleButton.setEnabled(enabled);
		comboBoxSchedules.setEnabled(enabled);
		if (!enabled)
			comboBoxSchedules.setSelectedItem("");
		super.enableButtons(enabled);
	}

	protected void saveTrack(Track track) {
		// save the schedule
		Object selected = comboBoxSchedules.getSelectedItem();
		if (selected == null || selected.equals("")) {
			track.setScheduleId("");
		} else {
			Schedule sch = (Schedule) selected;
			// update only if the schedule has changed
			track.setScheduleId(sch.getId());
		}
		textSchError.setText(track.checkScheduleValid());
		super.saveTrack(track);
	}

	protected void addNewTrack() {
		super.addNewTrack();
		updateScheduleComboBox(); // reset schedule and error text
	}

	private void updateScheduleComboBox() {
		ScheduleManager.instance().updateComboBox(comboBoxSchedules);
		if (_track != null) {
			Schedule s = ScheduleManager.instance().getScheduleById(_track.getScheduleId());
			comboBoxSchedules.setSelectedItem(s);
			textSchError.setText(_track.checkScheduleValid());
		}
	}

	public void dispose() {
		ScheduleManager.instance().removePropertyChangeListener(this);
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());
		if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.SCHEDULE_CHANGED_PROPERTY)) {
			updateScheduleComboBox();
		}
		super.propertyChange(e);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SidingEditFrame.class
			.getName());
}

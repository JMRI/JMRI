// SidingEditFrame.java

package jmri.jmrit.operations.locations;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.operations.setup.Control;


/**
 * Frame for user edit of a location sidings
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.20 $
 */

public class SidingEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// labels, buttons, etc. for sidings
	JLabel textSchedule = new JLabel(rb.getString("DeliverySchedule"));
	JLabel textSchError = new JLabel();
	JButton editScheduleButton = new JButton(rb.getString("Edit"));
	JComboBox comboBoxSchedules = ScheduleManager.instance().getComboBox();
	
	JPanel panelSchedule = panelOpt1;
	
	public SidingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.SIDING;
		
		// setup the optional panel with schedule stuff
		panelSchedule.setLayout(new GridBagLayout());
		panelSchedule.setBorder(BorderFactory.createTitledBorder(rb.getString("DeliverySchedule")));
		addItem(panelSchedule, comboBoxSchedules, 0, 0);
		addItem(panelSchedule, editScheduleButton, 1, 0);
		addItem(panelSchedule, textSchError, 2, 0);
		
		super.initComponents(location, track);
		
		_toolMenu.add(new ChangeTrackTypeAction (this));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Sidings", true);
		
		// override text strings for tracks
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainSiding")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(rb.getString("TypesSiding")));
		deleteTrackButton.setText(rb.getString("DeleteSiding"));
		addTrackButton.setText(rb.getString("AddSiding"));
		saveTrackButton.setText(rb.getString("SaveSiding"));
		
		// Select the siding's Schedule
		if (_track !=null){
			Schedule s = ScheduleManager.instance().getScheduleByName(_track.getScheduleName());
			comboBoxSchedules.setSelectedItem(s);
			textSchError.setText(_track.checkScheduleValid());
		}
		ScheduleManager.instance().addPropertyChangeListener(this);
		
		// setup buttons
		addButtonAction(editScheduleButton);
		// finish
		packFrame();
		setVisible(true);
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == editScheduleButton){
			editAddSchedule();
		}
		super.buttonActionPerformed(ae);
	}
	
	ScheduleEditFrame sef = null;
	private void editAddSchedule(){
		log.debug("Edit/add route");
		if (sef != null)
			sef.dispose();
		sef = new ScheduleEditFrame();			
		Object selected =  comboBoxSchedules.getSelectedItem();
		if (selected != null && !selected.equals("")){
			Schedule schedule = (Schedule)selected;
			sef.setTitle(MessageFormat.format(rb.getString("TitleScheduleEdit"), new Object[]{_track.getName()}));
			sef.initComponents(schedule, _location, _track);
		} else {
			sef.setTitle(MessageFormat.format(rb.getString("TitleScheduleAdd"), new Object[]{_track.getName()}));
			sef.initComponents(null, _location, _track);
		}
	}
	
	protected void enableButtons(boolean enabled){
		editScheduleButton.setEnabled(enabled);
		super.enableButtons(enabled);
	}
	
	@SuppressWarnings("null")
	protected void saveTrack (Track track){
		// save the schedule
		Object selected =  comboBoxSchedules.getSelectedItem();	
		if (selected == null || selected.equals("")){
			track.setScheduleName("");
			textSchError.setText("");
		} else {
			Schedule sch = (Schedule)selected;
			// update only if the schedule has changed
			if (sch != null){
				List<String> l = sch.getItemsBySequenceList();	
				//	must have at least one item in schedule
				if(l.size()>0){
					if (track.getScheduleName().equals("") ||
							!track.getScheduleName().equals(sch.getName())){
						track.setScheduleName(sch.getName());
					} else {
					// check to see if user deleted the current item for track
						ScheduleItem currentSi = sch.getItemById(track.getScheduleItemId());
						if (currentSi == null){
							track.setScheduleItemId(l.get(0));
							track.setScheduleCount(0);
						}
					}
					textSchError.setText(track.checkScheduleValid());
				} else {
					// no items in schedule so disable
					track.setScheduleName("");
					textSchError.setText(rb.getString("empty"));
				}
			}
		}
		super.saveTrack(track);
	}
	
	private void updateScheduleComboBox(){
		ScheduleManager.instance().updateComboBox(comboBoxSchedules);
		if (_track != null){
			Schedule s = ScheduleManager.instance().getScheduleByName(_track.getScheduleName());
			comboBoxSchedules.setSelectedItem(s);
		}
	}

	public void dispose() {
		ScheduleManager.instance().removePropertyChangeListener(this);
		super.dispose();
	}


	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateScheduleComboBox();
		}
		super.propertyChange(e);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(SidingEditFrame.class.getName());
}

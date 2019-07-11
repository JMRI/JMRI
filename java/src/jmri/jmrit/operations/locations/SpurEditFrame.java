package jmri.jmrit.operations.locations;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleEditFrame;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.locations.tools.AlternateTrackAction;
import jmri.jmrit.operations.locations.tools.ChangeTrackTypeAction;
import jmri.jmrit.operations.locations.tools.IgnoreUsedTrackAction;
import jmri.jmrit.operations.locations.tools.ShowCarsByLocationAction;
import jmri.jmrit.operations.locations.tools.ShowTrainsServingLocationAction;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for user edit of a spur.
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011
 */
public class SpurEditFrame extends TrackEditFrame {

    // labels, buttons, etc. for spurs
    JLabel textSchedule = new JLabel(Bundle.getMessage("DeliverySchedule"));
    JLabel textSchError = new JLabel();
    JButton editScheduleButton = new JButton();
    JComboBox<Schedule> comboBoxSchedules = InstanceManager.getDefault(ScheduleManager.class).getComboBox();

    JPanel panelSchedule = panelOpt4;

    public SpurEditFrame() {
        super();
    }

    @Override
    public void initComponents(Location location, Track track) {
        _type = Track.SPUR;

        // setup the optional panel with schedule stuff
        panelSchedule.setLayout(new GridBagLayout());
        panelSchedule.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("DeliverySchedule")));
        addItem(panelSchedule, comboBoxSchedules, 0, 0);
        addItem(panelSchedule, editScheduleButton, 1, 0);
        addItem(panelSchedule, textSchError, 2, 0);
        textSchError.setForeground(Color.RED);

        super.initComponents(location, track);

        _toolMenu.add(new AlternateTrackAction(this));
        _toolMenu.add(new IgnoreUsedTrackAction(this));
        _toolMenu.add(new ChangeTrackTypeAction(this));
        _toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsTrack"), _location, _track));
        _toolMenu.add(new ShowCarsByLocationAction(false, _location, _track));
        addHelpMenu("package.jmri.jmrit.operations.Operations_Sidings", true); // NOI18N

        // override text strings for tracks
        panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainSpur")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesSpur")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteSpur"));
        addTrackButton.setText(Bundle.getMessage("AddSpur"));
        saveTrackButton.setText(Bundle.getMessage("SaveSpur"));

        // setup buttons
        addButtonAction(editScheduleButton);
        addComboBoxAction(comboBoxSchedules);
        
        // Select the spur's Schedule
        updateScheduleComboBox();

        InstanceManager.getDefault(ScheduleManager.class).addPropertyChangeListener(this);
        
        // finish
        panelOrder.setVisible(false); // Car order out of spurs is not available
        pack();
        setVisible(true);
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == editScheduleButton) {
            editAddSchedule();
        }
        super.buttonActionPerformed(ae);
    }
    
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        updateScheduleButtonText();
    }
    
    private void updateScheduleButtonText() {
        if (comboBoxSchedules.getSelectedItem() == null) {
            editScheduleButton.setText(Bundle.getMessage("Add"));
        } else {
            editScheduleButton.setText(Bundle.getMessage("ButtonEdit"));
        }
    }

    ScheduleEditFrame sef = null;

    private void editAddSchedule() {
        log.debug("Edit/add schedule");
        if (sef != null) {
            sef.dispose();
        }
        Schedule schedule = (Schedule) comboBoxSchedules.getSelectedItem();
        sef = new ScheduleEditFrame(schedule, _track);
    }

    @Override
    protected void enableButtons(boolean enabled) {
        editScheduleButton.setEnabled(enabled);
        comboBoxSchedules.setEnabled(enabled);
        if (!enabled) {
            comboBoxSchedules.setSelectedItem(null);
        }
        super.enableButtons(enabled);
    }

    @Override
    protected void saveTrack(Track track) {
        // save the schedule
        Schedule schedule = (Schedule) comboBoxSchedules.getSelectedItem();
        track.setSchedule(schedule);
        textSchError.setText(track.checkScheduleValid());
        super.saveTrack(track);
    }

    @Override
    protected void addNewTrack() {
        super.addNewTrack();
        updateScheduleComboBox(); // reset schedule and error text
    }

    private void updateScheduleComboBox() {
        InstanceManager.getDefault(ScheduleManager.class).updateComboBox(comboBoxSchedules);
        if (_track != null) {
            Schedule sch = InstanceManager.getDefault(ScheduleManager.class).getScheduleById(_track.getScheduleId());
            comboBoxSchedules.setSelectedItem(sch);
            textSchError.setText(_track.checkScheduleValid());
            if (sch != null) {
                sch.removePropertyChangeListener(this);
                sch.addPropertyChangeListener(this);
            }
        }
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(ScheduleManager.class).removePropertyChangeListener(this);
        if (_track != null) {
            Schedule sch = InstanceManager.getDefault(ScheduleManager.class).getScheduleById(_track.getScheduleId());
            if (sch != null)
                sch.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.SCHEDULE_ID_CHANGED_PROPERTY)) {
            updateScheduleComboBox();
        }
        if (e.getSource().getClass().equals(Schedule.class)) {
            textSchError.setText(_track.checkScheduleValid());
        }
        super.propertyChange(e);
    }

    private final static Logger log = LoggerFactory.getLogger(SpurEditFrame.class);
}

package jmri.jmrit.operations.locations.gui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.*;
import jmri.jmrit.operations.locations.tools.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of a spur.
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2023, 2025
 */
public class SpurEditFrame extends TrackEditFrame {

    ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);

    // labels, buttons, etc. for spurs
    JLabel textSchedule = new JLabel(Bundle.getMessage("DeliverySchedule"));
    JLabel textSchError = new JLabel();
    JButton editScheduleButton = new JButton();
    JComboBox<Schedule> comboBoxSchedules = scheduleManager.getComboBox();

    JPanel panelSchedule = panelOpt4;

    public SpurEditFrame() {
        super(Bundle.getMessage("AddSpur"));
    }
    
    @Override
    public void initComponents(Track track) {
        setTitle(Bundle.getMessage("EditSpur", track.getLocation().getName()));
        initComponents(track.getLocation(), track);
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

        _toolMenu.insert(new AlternateTrackAction(this), 0);
        _toolMenu.insert(new TrackPriorityAction(_track), 1);
        _toolMenu.insert(new ChangeTrackTypeAction(this), TOOL_MENU_OFFSET + 2);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Spurs", true); // NOI18N

        // override text strings for tracks
        panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainSpur")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesSpur")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteSpur"));
        addTrackButton.setText(Bundle.getMessage("AddSpur"));
        saveTrackButton.setText(Bundle.getMessage("SaveSpur"));
        
        // tool tips
        autoSelectButton.setToolTipText(Bundle.getMessage("TipAutoSelectSchedule"));

        // setup buttons
        addButtonAction(autoSelectButton);
        addButtonAction(editScheduleButton);
        addComboBoxAction(comboBoxSchedules);
        
        // Select the spur's Schedule
        updateScheduleComboBox();

        scheduleManager.addPropertyChangeListener(this);
        
        // finish
        panelOrder.setVisible(false); // Car order out of spurs is not available
        pack();
        setVisible(true);
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == autoSelectButton) {
            autoSelectCheckboxes();
        }
        if (ae.getSource() == editScheduleButton) {
            editAddSchedule();
        }
        super.buttonActionPerformed(ae);
    }
    
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        removeSchedulePropertyListener();
        updateScheduleButtonText();
    }
    
    private void updateScheduleButtonText() {
        if (comboBoxSchedules.getSelectedItem() == null) {
            editScheduleButton.setText(Bundle.getMessage("Add"));
        } else {
            editScheduleButton.setText(Bundle.getMessage("ButtonEdit"));
        }
    }
    
    private void autoSelectCheckboxes() {
        if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("TipAutoSelectSchedule"),
                Bundle.getMessage("AutoSelect"), JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
            for (int i = 0; i < checkBoxes.size(); i++) {
                JCheckBox checkBox = checkBoxes.get(i);
                if (_track != null && _track.getSchedule() != null) {
                    Schedule schedule = _track.getSchedule();
                    if (schedule.checkScheduleAttribute(Track.TYPE, checkBox.getText(), null)) {
                        _track.addTypeName(checkBox.getText());
                    } else {
                        _track.deleteTypeName(checkBox.getText());
                    }
                }
            }
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
        autoSelectButton.setEnabled(enabled && _track.getSchedule() != null);
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
    
    @Override
    protected void deleteTrack() {
        removeSchedulePropertyListener();
        super.deleteTrack();
    }

    private void updateScheduleComboBox() {
        scheduleManager.updateComboBox(comboBoxSchedules);
        if (_track != null) {
            Schedule sch = scheduleManager.getScheduleById(_track.getScheduleId());
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
        scheduleManager.removePropertyChangeListener(this);
        removeSchedulePropertyListener();
        super.dispose();
    }
    
    private void removeSchedulePropertyListener() {
        if (_track != null) {
            Schedule sch = scheduleManager.getScheduleById(_track.getScheduleId());
            if (sch != null)
                sch.removePropertyChangeListener(this);
        }
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
        if (e.getSource().getClass().equals(Schedule.class) && _track != null) {
            textSchError.setText(_track.checkScheduleValid());
        }
        super.propertyChange(e);
    }

    private final static Logger log = LoggerFactory.getLogger(SpurEditFrame.class);
}

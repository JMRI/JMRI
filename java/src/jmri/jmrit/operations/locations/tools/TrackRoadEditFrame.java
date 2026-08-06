package jmri.jmrit.operations.locations.tools;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of track roads
 *
 * @author Dan Boudreau Copyright (C) 2013, 2014, 2026
 */
public class TrackRoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Location _location = null;
    Track _track = null;
    private static boolean roadAndLoadType = false;

    // panels
    JPanel pRoadControls = new JPanel();
    JPanel panelRoads = new JPanel();
    JScrollPane paneRoads = new JScrollPane(panelRoads);

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton addRoadButton = new JButton(Bundle.getMessage("AddRoad"));
    JButton deleteRoadButton = new JButton(Bundle.getMessage("DeleteRoad"));
    JButton deleteAllRoadsButton = new JButton(Bundle.getMessage("DeleteAll"));

    // radio buttons
    JRadioButton roadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton roadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton roadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    // combo box
    JComboBox<String> comboBoxRoads = InstanceManager.getDefault(CarRoads.class).getComboBox();
    JComboBox<String> comboBoxLoadTypes = InstanceManager.getDefault(CarLoads.class).getLoadTypesComboBox();

    // check boxes
    JCheckBox roadAndLoadTypeCheckBox = new JCheckBox(Bundle.getMessage("RoadAndLoadType"));

    // labels
    JLabel trackName = new JLabel();

    public static final String DISPOSE = "dispose"; // NOI18N
    public static final int MAX_NAME_LENGTH = Control.max_len_string_track_name;

    public TrackRoadEditFrame() {
        super(Bundle.getMessage("TitleEditTrackRoads"));
    }

    public void initComponents(Location location, Track track) {
        _location = location;
        _track = track;

        // property changes
        // listen for car road name changes
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        // Layout the panel by rows
        // row 1
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setMaximumSize(new Dimension(2000, 250));

        // row 1a
        JPanel pTrackName = new JPanel();
        pTrackName.setLayout(new GridBagLayout());
        pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        addItem(pTrackName, trackName, 0, 0);

        // row 1b
        JPanel pLocationName = new JPanel();
        pLocationName.setLayout(new GridBagLayout());
        pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        addItem(pLocationName, new JLabel(_location.getName()), 0, 0);

        p1.add(pTrackName);
        p1.add(pLocationName);

        // row 3
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        JScrollPane pane3 = new JScrollPane(p3);
        pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RoadsTrack")));
        pane3.setMaximumSize(new Dimension(2000, 400));

        JPanel pRoadRadioButtons = new JPanel();
        pRoadRadioButtons.setLayout(new FlowLayout());

        pRoadRadioButtons.add(roadNameAll);
        pRoadRadioButtons.add(roadNameInclude);
        pRoadRadioButtons.add(roadNameExclude);
        pRoadRadioButtons.add(roadAndLoadTypeCheckBox);

        pRoadControls.setLayout(new FlowLayout());

        pRoadControls.add(comboBoxRoads);
        pRoadControls.add(comboBoxLoadTypes);
        pRoadControls.add(addRoadButton);
        pRoadControls.add(deleteRoadButton);
        pRoadControls.add(deleteAllRoadsButton);

        pRoadControls.setVisible(false);
        comboBoxLoadTypes.setVisible(false);
        roadAndLoadTypeCheckBox.setSelected(roadAndLoadType);

        p3.add(pRoadRadioButtons);
        p3.add(pRoadControls);

        // row 4
        panelRoads.setLayout(new GridBagLayout());
        paneRoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Roads")));

        ButtonGroup roadGroup = new ButtonGroup();
        roadGroup.add(roadNameAll);
        roadGroup.add(roadNameInclude);
        roadGroup.add(roadNameExclude);

        // row 12
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        panelButtons.setBorder(BorderFactory.createTitledBorder(""));
        panelButtons.setMaximumSize(new Dimension(2000, 200));

        // row 13
        addItem(panelButtons, saveButton, 0, 0);

        getContentPane().add(p1);
        getContentPane().add(pane3);
        getContentPane().add(paneRoads);
        getContentPane().add(panelButtons);

        // setup buttons
        addButtonAction(saveButton);

        addButtonAction(deleteRoadButton);
        addButtonAction(deleteAllRoadsButton);
        addButtonAction(addRoadButton);

        addRadioButtonAction(roadNameAll);
        addRadioButtonAction(roadNameInclude);
        addRadioButtonAction(roadNameExclude);

        addCheckBoxAction(roadAndLoadTypeCheckBox);

        // road fields and enable buttons
        if (_track != null) {
            _track.addPropertyChangeListener(this);
            trackName.setText(_track.getName());
            enableButtons(true);
        } else {
            enableButtons(false);
        }

        updateRoadComboBox();
        updateRoadNames();

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_RoadOptions", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight400));
    }

    // Save, Delete, Add
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        if (_track == null) {
            return;
        }
        if (ae.getSource() == saveButton) {
            log.debug("track save button activated");
            if (!checkForErrors()) {
                roadAndLoadType = roadAndLoadTypeCheckBox.isSelected();
                OperationsXml.save();
                if (Setup.isCloseWindowOnSaveEnabled()) {
                    dispose();
                }
            }
        }
        if (ae.getSource() == addRoadButton) {
            String roadName = (String) comboBoxRoads.getSelectedItem();
            if (roadAndLoadTypeCheckBox.isSelected()) {
                roadName = roadName + CarRoads.SPLIT_CHAR + comboBoxLoadTypes.getSelectedItem();
            }
            _track.addRoadName(roadName);
            selectNextItemComboBox(comboBoxRoads);
        }
        if (ae.getSource() == deleteRoadButton) {
            String roadName = (String) comboBoxRoads.getSelectedItem();
            if (roadAndLoadTypeCheckBox.isSelected()) {
                roadName = roadName + CarRoads.SPLIT_CHAR + comboBoxLoadTypes.getSelectedItem();
            }
            _track.deleteRoadName(roadName);
            selectNextItemComboBox(comboBoxRoads);
        }
        if (ae.getSource() == deleteAllRoadsButton) {
            deleteAllRoads();
        }
    }

    protected void enableButtons(boolean enabled) {
        saveButton.setEnabled(enabled);
        roadNameAll.setEnabled(enabled);
        roadNameInclude.setEnabled(enabled);
        roadNameExclude.setEnabled(enabled);
    }

    @Override
    public void radioButtonActionPerformed(ActionEvent ae) {
        log.debug("radio button activated");
        if (ae.getSource() == roadNameAll) {
            _track.setRoadOption(Track.ALL_ROADS);
        }
        if (ae.getSource() == roadNameInclude) {
            _track.setRoadOption(Track.INCLUDE_ROADS);
        }
        if (ae.getSource() == roadNameExclude) {
            _track.setRoadOption(Track.EXCLUDE_ROADS);
        }
    }

    private void updateRoadComboBox() {
        InstanceManager.getDefault(CarRoads.class).updateComboBox(comboBoxRoads);
    }

    private void updateRoadNames() {
        log.debug("Update road names");
        panelRoads.removeAll();
        if (_track != null) {
            // set radio button
            roadNameAll.setSelected(_track.getRoadOption().equals(Track.ALL_ROADS));
            roadNameInclude.setSelected(_track.getRoadOption().equals(Track.INCLUDE_ROADS));
            roadNameExclude.setSelected(_track.getRoadOption().equals(Track.EXCLUDE_ROADS));

            pRoadControls.setVisible(!roadNameAll.isSelected());

            if (!roadNameAll.isSelected()) {
                int x = 0;
                int y = 0; // vertical position in panel

                int numberOfRoads = getNumberOfCheckboxesPerLine();
                for (String roadName : _track.getRoadNames()) {
                    JLabel road = new JLabel();
                    road.setText(roadName);
                    addItemTop(panelRoads, road, x++, y);
                    // limit the number of roads per line
                    if (x > numberOfRoads) {
                        y++;
                        x = 0;
                    }
                }
                revalidate();
            }
        } else {
            roadNameAll.setSelected(true);
        }
        panelRoads.repaint();
        panelRoads.revalidate();
    }

    private void deleteAllRoads() {
        if (_track != null) {
            for (String roadName : _track.getRoadNames()) {
                _track.deleteRoadName(roadName);
            }
        }
    }

    @Override
    public void checkBoxActionPerformed(ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        comboBoxLoadTypes.setVisible(b.isSelected());
    }

    private boolean checkForErrors() {
        if (_track.getRoadOption().equals(Track.INCLUDE_ROADS) && _track.getRoadNames().length == 0) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorNeedRoads"),
                    Bundle.getMessage("ErrorNoRoads"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            updateRoadComboBox();
            updateRoadNames();
        }
        if (e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY)) {
            updateRoadNames();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackRoadEditFrame.class);
}

package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of track roads
 *
 * @author Dan Boudreau Copyright (C) 2013, 2014
 * 
 */
public class TrackRoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Location _location = null;
    Track _track = null;

    // panels
    JPanel pRoadControls = new JPanel();
    JPanel panelRoads = new JPanel();
    JScrollPane paneRoads = new JScrollPane(panelRoads);

    // major buttons
    JButton saveTrackButton = new JButton(Bundle.getMessage("SaveTrack"));
    JButton addRoadButton = new JButton(Bundle.getMessage("AddRoad"));
    JButton deleteRoadButton = new JButton(Bundle.getMessage("DeleteRoad"));
    JButton deleteAllRoadsButton = new JButton(Bundle.getMessage("DeleteAll"));

    // radio buttons
    JRadioButton roadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton roadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton roadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    // combo box
    JComboBox<String> comboBoxRoads = InstanceManager.getDefault(CarRoads.class).getComboBox();

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

        pRoadControls.setLayout(new FlowLayout());

        pRoadControls.add(comboBoxRoads);
        pRoadControls.add(addRoadButton);
        pRoadControls.add(deleteRoadButton);
        pRoadControls.add(deleteAllRoadsButton);

        pRoadControls.setVisible(false);

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
        addItem(panelButtons, saveTrackButton, 0, 0);

        getContentPane().add(p1);
        getContentPane().add(pane3);
        getContentPane().add(paneRoads);
        getContentPane().add(panelButtons);

        // setup buttons
        addButtonAction(saveTrackButton);

        addButtonAction(deleteRoadButton);
        addButtonAction(deleteAllRoadsButton);
        addButtonAction(addRoadButton);

        addRadioButtonAction(roadNameAll);
        addRadioButtonAction(roadNameInclude);
        addRadioButtonAction(roadNameExclude);

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

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight400));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (_track == null) {
            return;
        }
        if (ae.getSource() == saveTrackButton) {
            log.debug("track save button activated");
            checkForErrors();
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == addRoadButton) {
            _track.addRoadName((String) comboBoxRoads.getSelectedItem());
            selectNextItemComboBox(comboBoxRoads);
        }
        if (ae.getSource() == deleteRoadButton) {
            _track.deleteRoadName((String) comboBoxRoads.getSelectedItem());
            selectNextItemComboBox(comboBoxRoads);
        }
        if (ae.getSource() == deleteAllRoadsButton) {
            deleteAllRoads();
        }
    }

    protected void enableButtons(boolean enabled) {
        saveTrackButton.setEnabled(enabled);
        roadNameAll.setEnabled(enabled);
        roadNameInclude.setEnabled(enabled);
        roadNameExclude.setEnabled(enabled);
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
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
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change {}", b.getText());
        if (_location == null) {
            return;
        }
        if (b.isSelected()) {
            _track.addTypeName(b.getText());
        } else {
            _track.deleteTypeName(b.getText());
        }
    }

    private void checkForErrors() {
        if (_track.getRoadOption().equals(Track.INCLUDE_ROADS) && _track.getRoadNames().length == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorNeedRoads"), Bundle.getMessage("ErrorNoRoads"),
                    JOptionPane.ERROR_MESSAGE);
        }
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

    private final static Logger log = LoggerFactory.getLogger(TrackRoadEditFrame.class);
}

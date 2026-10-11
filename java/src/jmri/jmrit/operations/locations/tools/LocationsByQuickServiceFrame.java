package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame to display and modify which locations have quick service tracks
 *
 * @author Dan Boudreau Copyright (C) 2026
 */
public class LocationsByQuickServiceFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    // checkboxes have the location id or track id as the checkbox name
    ArrayList<JCheckBox> locationCheckBoxList = new ArrayList<>();
    ArrayList<JCheckBox> trackCheckBoxList = new ArrayList<>();
    JPanel locationCheckBoxes = new JPanel();

    // panels
    JPanel pLocations;

    // major buttons
    JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    JButton setButton = new JButton(Bundle.getMessage("SelectAll"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // selected location
    Location _location;

    public LocationsByQuickServiceFrame() {
        super();
    }

    public void initComponents(Location location) {
        this._location = location;
        initComponents();
    }

    @Override
    public void initComponents() {

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel pCarType = new JPanel();
        pCarType.setLayout(new GridBagLayout());
        pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));

        pLocations = new JPanel();
        pLocations.setLayout(new GridBagLayout());
        JScrollPane locationPane = new JScrollPane(pLocations);
        locationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        locationPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Locations")));
        updateLocations();

        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        pButtons.setBorder(BorderFactory.createTitledBorder(""));

        addItem(pButtons, clearButton, 0, 0);
        addItem(pButtons, setButton, 1, 0);
        addItem(pButtons, saveButton, 2, 0);

        getContentPane().add(pCarType);
        getContentPane().add(locationPane);
        getContentPane().add(pButtons);

        // setup buttons
        addButtonAction(setButton);
        addButtonAction(clearButton);
        addButtonAction(saveButton);

        locationManager.addPropertyChangeListener(this);

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ModifyLocationsByQuickService", true); // NOI18N

        if (_location != null) {
            setTitle(Bundle.getMessage("TitleModifyLocQuickService"));
        } else {
            setTitle(Bundle.getMessage("TitleModifyLocsQuickService"));
        }
        
        setPreferredSize(null); // we need to resize this frame
        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight250));
        setSize(getWidth() + 25, getHeight()); // make a bit wider to eliminate scroll bar
    }
    
    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox cb = (JCheckBox) ae.getSource();
        log.debug("Checkbox {} text: {}", cb.getName(), cb.getText());
        if (locationCheckBoxList.contains(cb)) {
            log.debug("Checkbox location {}", cb.getText());
            // update all tracks at location{
                String locId = cb.getName();
                for (JCheckBox cbt : new ArrayList<>(trackCheckBoxList)) {
                    String[] id = cbt.getName().split(Location.LOC_TRACK_REGIX);
                    if (locId.equals(id[0])) {
                        cbt.setSelected(cb.isSelected());
                }
            }
        }
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            save();
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
    }

    /**
     * Update the car types that locations and tracks service. Note that the
     * checkbox name is the id of the location or track.
     */
    private void save() {
        log.debug("save {} locations", locationCheckBoxList.size());
        removePropertyChangeLocations();
        for (JCheckBox cb : new ArrayList<>(locationCheckBoxList)) {
            Location loc = locationManager.getLocationById(cb.getName());
            // save tracks that have the same id as the location
            for (JCheckBox cbt : new ArrayList<>(trackCheckBoxList)) {
                String[] id = cbt.getName().split(Location.LOC_TRACK_REGIX);
                if (loc.getId().equals(id[0])) {
                    Track track = loc.getTrackById(cbt.getName());
                    track.setQuickServiceEnabled(cb.isSelected() && cbt.isSelected());
                }
            }
        }
        OperationsXml.save();
        updateLocations();
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    private void updateLocations() {
        log.debug("update checkboxes");
        removePropertyChangeLocations();
        locationCheckBoxList.clear();
        trackCheckBoxList.clear();
        int x = 0;
        pLocations.removeAll();
        // did the location get deleted?
        if (_location != null && locationManager.getLocationByName(_location.getName()) == null) {
            _location = null;
        }
        List<Location> locations = locationManager.getLocationsByNameList();
        for (Location loc : locations) {
            // show only one location?
            if (_location != null && _location != loc) {
                continue;
            }
            loc.addPropertyChangeListener(this);
            JCheckBox cb = new JCheckBox(loc.getName());
            cb.setName(loc.getId());
            addCheckBoxAction(cb);
            locationCheckBoxList.add(cb);
            addItemLeft(pLocations, cb, 0, x++);
            cb.setSelected(loc.hasQuickService());
            List<Track> tracks = loc.getTracksByNameList(null);
            for (Track track : tracks) {
                track.addPropertyChangeListener(this);
                JCheckBox cbt = new JCheckBox(track.getName());
                cbt.setName(track.getId());
                trackCheckBoxList.add(cbt);
                cbt.setSelected(track.isQuickServiceEnabled());
                addItemLeft(pLocations, cbt, 1, x++);
            }
        }
        pLocations.revalidate();
        repaint();
    }

    private void selectCheckboxes(boolean select) {
        for (JCheckBox cb : new ArrayList<>(locationCheckBoxList)) {
            cb.setSelected(select);
        }
        for (JCheckBox cb : new ArrayList<>(trackCheckBoxList)) {
            cb.setSelected(select);
        }
    }

    private void removePropertyChangeLocations() {
        for (JCheckBox cb : new ArrayList<>(locationCheckBoxList)) {
            // checkbox name is the location id
            Location loc = locationManager.getLocationById(cb.getName());
            if (loc != null) {
                loc.removePropertyChangeListener(this);
                List<Track> tracks = loc.getTracksList();
                for (Track track : tracks) {
                    track.removePropertyChangeListener(this);
                }
            }
        }
    }

    @Override
    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        removePropertyChangeLocations();
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                .getNewValue());
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.LOAD_OPTIONS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.NAME_CHANGED_PROPERTY)) {
            updateLocations();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocationsByQuickServiceFrame.class);
}

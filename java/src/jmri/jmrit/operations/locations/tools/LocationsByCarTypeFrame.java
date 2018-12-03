package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to display which locations service certain car types
 *
 * @author Dan Boudreau Copyright (C) 2009, 2011
 */
public class LocationsByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    LocationManager manager;
    static final String EMPTY = "            ";

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

    // check boxes
    JCheckBox copyCheckBox = new JCheckBox(Bundle.getMessage("ButtonCopy"));

    // text field
    JLabel textCarType = new JLabel(EMPTY);

    // combo boxes
    JComboBox<String> typeComboBox = InstanceManager.getDefault(CarTypes.class).getComboBox();

    // selected location
    Location _location;

    public LocationsByCarTypeFrame() {
        super();
    }

    @Override
    public void initComponents() {
        initComponents(NONE);
    }

    public void initComponents(Location location) {
        this._location = location;
        initComponents(NONE);
    }

    public void initComponents(Location location, String carType) {
        this._location = location;
        initComponents(carType);
    }

    public void initComponents(String carType) {

        // load managers
        manager = InstanceManager.getDefault(LocationManager.class);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel pCarType = new JPanel();
        pCarType.setLayout(new GridBagLayout());
        pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));

        addItem(pCarType, typeComboBox, 0, 0);
        addItem(pCarType, copyCheckBox, 1, 0);
        addItem(pCarType, textCarType, 2, 0);
        typeComboBox.setSelectedItem(carType);
        copyCheckBox.setToolTipText(Bundle.getMessage("TipCopyCarType"));

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

        // setup combo box
        addComboBoxAction(typeComboBox);

        // setup buttons
        addButtonAction(setButton);
        addButtonAction(clearButton);
        addButtonAction(saveButton);

        // setup checkbox
        addCheckBoxAction(copyCheckBox);

        manager.addPropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new PrintLocationsByCarTypesAction(Bundle.getMessage("MenuItemPrintByType"), new Frame(), false,
                this));
        toolMenu.add(new PrintLocationsByCarTypesAction(Bundle.getMessage("MenuItemPreviewByType"), new Frame(), true,
                this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_ModifyLocationsByCarType", true); // NOI18N

        setPreferredSize(null); // we need to resize this frame
        pack();
        setMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight250));
        setSize(getWidth() + 25, getHeight()); // make a bit wider to eliminate scroll bar
        if (_location != null) {
            setTitle(Bundle.getMessage("TitleModifyLocation"));
        } else {
            setTitle(Bundle.getMessage("TitleModifyLocations"));
        }
        setVisible(true);
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("combo box action");
        updateLocations();
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
        if (copyCheckBox.isSelected() &&
                JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("CopyCarType"),
                        new Object[]{typeComboBox.getSelectedItem(), textCarType.getText()}),
                        Bundle.getMessage("CopyCarTypeTitle"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        log.debug("save {} locations", locationCheckBoxList.size());
        removePropertyChangeLocations();
        for (JCheckBox cb : locationCheckBoxList) {
            Location loc = manager.getLocationById(cb.getName());
            if (cb.isSelected()) {
                loc.addTypeName((String) typeComboBox.getSelectedItem());
                // save tracks that have the same id as the location
                for (JCheckBox cbt : trackCheckBoxList) {
                    String[] id = cbt.getName().split(Location.LOC_TRACK_REGIX);
                    if (loc.getId().equals(id[0])) {
                        Track track = loc.getTrackById(cbt.getName());
                        if (cbt.isSelected()) {
                            track.addTypeName((String) typeComboBox.getSelectedItem());
                        } else {
                            track.deleteTypeName((String) typeComboBox.getSelectedItem());
                        }
                    }
                }
            } else {
                loc.deleteTypeName((String) typeComboBox.getSelectedItem());
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
        String carType = (String) typeComboBox.getSelectedItem();
        if (copyCheckBox.isSelected()) {
            carType = textCarType.getText();
        }
        // did the location get deleted?
        if (_location != null && manager.getLocationByName(_location.getName()) == null) {
            _location = null;
        }
        List<Location> locations = manager.getLocationsByNameList();
        for (Location loc : locations) {
            // show only one location?
            if (_location != null && _location != loc) {
                continue;
            }
            loc.addPropertyChangeListener(this);
            JCheckBox cb = new JCheckBox(loc.getName());
            cb.setName(loc.getId());
            cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipLocCarType"), new Object[]{carType}));
            addCheckBoxAction(cb);
            locationCheckBoxList.add(cb);
            boolean locAcceptsType = loc.acceptsTypeName(carType);
            cb.setSelected(locAcceptsType);
            addItemLeft(pLocations, cb, 0, x++);
            List<Track> tracks = loc.getTrackByNameList(null);
            for (Track track : tracks) {
                track.addPropertyChangeListener(this);
                cb = new JCheckBox(track.getName());
                cb.setName(track.getId());
                cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipTrackCarType"), new Object[]{carType}));
                addCheckBoxAction(cb);
                trackCheckBoxList.add(cb);
                cb.setSelected(track.acceptsTypeName(carType));
                addItemLeft(pLocations, cb, 1, x++);
            }
        }
        pLocations.revalidate();
        repaint();
    }

    private void updateComboBox() {
        log.debug("update combobox");
        InstanceManager.getDefault(CarTypes.class).updateComboBox(typeComboBox);
    }

    private void selectCheckboxes(boolean select) {
        for (int i = 0; i < locationCheckBoxList.size(); i++) {
            locationCheckBoxList.get(i).setSelected(select);
        }
        for (int i = 0; i < trackCheckBoxList.size(); i++) {
            trackCheckBoxList.get(i).setSelected(select);
        }
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        // copy checkbox?
        if (ae.getSource() == copyCheckBox) {
            if (copyCheckBox.isSelected()) {
                textCarType.setText((String) typeComboBox.getSelectedItem());
            } else {
                textCarType.setText(EMPTY);
                updateLocations();
            }
        } else {
            JCheckBox cb = (JCheckBox) ae.getSource();
            log.debug("Checkbox {} text: {}", cb.getName(), cb.getText());
            if (locationCheckBoxList.contains(cb)) {
                log.debug("Checkbox location {}", cb.getText());
                // must deselect tracks if location is deselect
                if (!cb.isSelected()) {
                    String locId = cb.getName();
                    for (int i = 0; i < trackCheckBoxList.size(); i++) {
                        cb = trackCheckBoxList.get(i);
                        String[] id = cb.getName().split(Location.LOC_TRACK_REGIX);
                        if (locId.equals(id[0])) {
                            cb.setSelected(false);
                        }
                    }
                }

            } else if (trackCheckBoxList.contains(cb)) {
                log.debug("Checkbox track {}", cb.getText());
                // Must select location if track is selected
                if (cb.isSelected()) {
                    String[] loc = cb.getName().split(Location.LOC_TRACK_REGIX);
                    for (int i = 0; i < locationCheckBoxList.size(); i++) {
                        cb = locationCheckBoxList.get(i);
                        if (cb.getName().equals(loc[0])) {
                            cb.setSelected(true);
                            break;
                        }
                    }
                }
            } else {
                log.error("Error checkbox not found");
            }
        }
    }

    private void removePropertyChangeLocations() {
        if (locationCheckBoxList != null) {
            for (int i = 0; i < locationCheckBoxList.size(); i++) {
                // checkbox name is the location id
                Location loc = manager.getLocationById(locationCheckBoxList.get(i).getName());
                if (loc != null) {
                    loc.removePropertyChangeListener(this);
                    List<Track> tracks = loc.getTrackList();
                    for (Track track : tracks) {
                        track.removePropertyChangeListener(this);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        manager.removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        removePropertyChangeLocations();
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                .getNewValue());
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.NAME_CHANGED_PROPERTY)) {
            updateLocations();
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)) {
            updateComboBox();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocationsByCarTypeFrame.class);
}

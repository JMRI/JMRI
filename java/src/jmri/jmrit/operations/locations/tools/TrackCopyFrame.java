package jmri.jmrit.operations.locations.tools;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationEditFrame;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for copying a track for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 */
public class TrackCopyFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // text field
    JTextField trackNameTextField = new javax.swing.JTextField(Control.max_len_string_track_name);

    // major buttons
    JButton copyButton = new javax.swing.JButton(Bundle.getMessage("ButtonCopy"));
    JButton saveButton = new javax.swing.JButton(Bundle.getMessage("ButtonSave"));

    // combo boxes
    JComboBox<Location> locationBox = InstanceManager.getDefault(LocationManager.class).getComboBox();
    JComboBox<Track> trackBox = new JComboBox<>();
    JComboBox<Location> destinationBox = InstanceManager.getDefault(LocationManager.class).getComboBox();

    // checkboxes
    JCheckBox sameNameCheckBox = new JCheckBox(Bundle.getMessage("SameName"));
    JCheckBox moveRollingStockCheckBox = new JCheckBox(Bundle.getMessage("MoveRollingStock"));
    JCheckBox deleteTrackCheckBox = new JCheckBox(Bundle.getMessage("DeleteCopiedTrack"));

    Location _location;     // copy from this location
    Location _destination;  // copy the track to this location

    // remember state of checkboxes during a session
    static boolean sameName = false;
    static boolean moveRollingStock = false;
    static boolean deleteTrack = false;

    public TrackCopyFrame(LocationEditFrame lef) {
        if (lef != null) {
            _destination = lef._location;
        }

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        // Layout the panel by rows
        // row 1
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrackName")));
        addItem(pName, trackNameTextField, 0, 0);

        // row 2
        JPanel pCopy = new JPanel();
        pCopy.setLayout(new GridBagLayout());
        pCopy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectTrackToCopy")));
        addItem(pCopy, locationBox, 0, 0);
        addItem(pCopy, trackBox, 1, 0);
        
        // row 3
        JPanel pCopyTo = new JPanel();
        pCopyTo.setLayout(new GridBagLayout());
        pCopyTo.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectCopyToLocation")));
        addItem(pCopyTo, destinationBox, 0, 0);

        // row 4
        JPanel pOptions = new JPanel();
        pOptions.setLayout(new GridBagLayout());
        pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        addItemLeft(pOptions, sameNameCheckBox, 0, 0);
        addItemLeft(pOptions, moveRollingStockCheckBox, 0, 1);
        addItemLeft(pOptions, deleteTrackCheckBox, 0, 2);

        // row 5
        JPanel pButton = new JPanel();
        pButton.setLayout(new GridBagLayout());
        addItem(pButton, copyButton, 0, 0);
        addItem(pButton, saveButton, 1, 0);

        getContentPane().add(pName);
        getContentPane().add(pCopy);
        getContentPane().add(pCopyTo);
        getContentPane().add(pOptions);
        getContentPane().add(pButton);

        addComboBoxAction(locationBox);
        addComboBoxAction(trackBox);
        
        addComboBoxAction(destinationBox);

        // set the checkbox states
        sameNameCheckBox.setSelected(sameName);
        moveRollingStockCheckBox.setSelected(moveRollingStock);
        deleteTrackCheckBox.setSelected(deleteTrack);
        deleteTrackCheckBox.setEnabled(moveRollingStockCheckBox.isSelected());

        // get notified if combo box gets modified
        InstanceManager.getDefault(LocationManager.class).addPropertyChangeListener(this);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_CopyTrack", true); // NOI18N

        pack();
        setMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight400));

        if (_destination != null) {
            setTitle(MessageFormat.format(Bundle.getMessage("TitleCopyTrack"), new Object[]{_destination.getName()}));
            pCopyTo.setVisible(false);
            _destination.addPropertyChangeListener(this);
        } else {
            setTitle(Bundle.getMessage("MenuItemCopyTrack"));
            copyButton.setEnabled(false);
        }

        // setup buttons
        addButtonAction(copyButton);
        addButtonAction(saveButton);

        addCheckBoxAction(sameNameCheckBox);
        addCheckBoxAction(moveRollingStockCheckBox);
    }

    // location combo box
    @Override
    protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == locationBox) {
            updateTrackComboBox();
        }
        if (ae.getSource() == trackBox) {
            updateTrackName();
        }
        if (ae.getSource() == destinationBox) {
            _destination = (Location) destinationBox.getSelectedItem();
            copyButton.setEnabled(_destination != null);
        }
    }

    protected void updateTrackComboBox() {
        log.debug("update track combobox");
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        if (locationBox.getSelectedItem() == null) {
            trackBox.removeAllItems();
        } else {
            log.debug("copy from location: {}", locationBox.getSelectedItem());
            _location = (Location) locationBox.getSelectedItem();
            Track track = (Track)trackBox.getSelectedItem();
            _location.updateComboBox(trackBox);
            trackBox.setSelectedItem(track);
            _location.addPropertyChangeListener(this);
        }
    }

    protected void updateTrackName() {
        if (sameNameCheckBox.isSelected() && trackBox.getSelectedItem() != null) {
            trackNameTextField.setText(trackBox.getSelectedItem().toString());
        }
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == copyButton) {
            log.debug("copy track button activated");
            if (!checkName()) {
                return;
            }
            if (trackBox.getSelectedItem() == null || trackBox.getSelectedItem().equals(Location.NONE) || _destination == null) {
                // tell user that they need to select a track to copy
                JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectLocationAndTrack"), Bundle
                        .getMessage("SelectTrackToCopy"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Track fromTrack = (Track) trackBox.getSelectedItem();
            if (moveRollingStockCheckBox.isSelected() && fromTrack.getPickupRS() > 0) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("FoundRollingStockPickUp"),
                        new Object[]{fromTrack.getPickupRS()}), MessageFormat.format(Bundle
                                .getMessage("TrainsServicingTrack"), new Object[]{fromTrack.getName()}),
                        JOptionPane.WARNING_MESSAGE);
                return; // failed
            }
            if (moveRollingStockCheckBox.isSelected() && fromTrack.getDropRS() > 0) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("FoundRollingStockDrop"),
                        new Object[]{fromTrack.getDropRS()}), MessageFormat.format(Bundle
                                .getMessage("TrainsServicingTrack"), new Object[]{fromTrack.getName()}),
                        JOptionPane.WARNING_MESSAGE);
                return; // failed
            }
            // only copy tracks that are okay with the location
            if (fromTrack.isStaging() ^ _destination.isStaging()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrackTypeWrong"),
                        new Object[]{fromTrack.getTrackType(), _destination.getName()}), MessageFormat.format(Bundle
                                .getMessage("CanNotCopy"), new Object[]{fromTrack.getName()}), JOptionPane.ERROR_MESSAGE);
                return;
            }
            Track toTrack = fromTrack.copyTrack(trackNameTextField.getText(), _destination);
            if (moveRollingStockCheckBox.isSelected()) {
                // move rolling stock
                moveRollingStock(fromTrack, toTrack);
                if (deleteTrackCheckBox.isSelected()) {
                    InstanceManager.getDefault(ScheduleManager.class).replaceTrack(fromTrack, toTrack);
                    fromTrack.getLocation().deleteTrack(fromTrack);
                }
            }
        }
        if (ae.getSource() == saveButton) {
            log.debug("save track button activated");
            // save checkbox states
            sameName = sameNameCheckBox.isSelected();
            moveRollingStock = moveRollingStockCheckBox.isSelected();
            deleteTrack = deleteTrackCheckBox.isSelected();
            // save location file
            OperationsXml.save();
        }
    }

    @Override
    protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == sameNameCheckBox) {
            updateTrackName();
        }
        if (ae.getSource() == moveRollingStockCheckBox) {
            deleteTrackCheckBox.setEnabled(moveRollingStockCheckBox.isSelected());
            deleteTrackCheckBox.setSelected(false);
        }
    }

    protected void updateComboBoxes() {
        log.debug("update location combobox");
        InstanceManager.getDefault(LocationManager.class).updateComboBox(locationBox);
        InstanceManager.getDefault(LocationManager.class).updateComboBox(destinationBox);
    }

    /**
     *
     * @return true if name entered OK and isn't too long
     */
    protected boolean checkName() {
        if (trackNameTextField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
                    .getMessage("CanNotTrack"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (TrainCommon.splitString(trackNameTextField.getText()).length() > Control.max_len_string_track_name) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrackNameLengthMax"),
                    new Object[]{Integer.toString(Control.max_len_string_track_name + 1)}), MessageFormat.format(Bundle
                            .getMessage("CanNotTrack"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // check to see if track already exists
        if (_destination == null) {
            return false;
        }
        Track check = _destination.getTrackByName(trackNameTextField.getText(), null);
        if (check != null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("TrackAlreadyExists"), MessageFormat.format(Bundle
                    .getMessage("CanNotTrack"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    protected void moveRollingStock(Track fromTrack, Track toTrack) {
        moveRollingStock(fromTrack, toTrack, InstanceManager.getDefault(CarManager.class));
        moveRollingStock(fromTrack, toTrack, InstanceManager.getDefault(EngineManager.class));
    }

    private void moveRollingStock(Track fromTrack, Track toTrack, RollingStockManager<? extends RollingStock> manager) {
        for (RollingStock rs : manager.getByIdList()) {
            if (rs.getTrack() == fromTrack) {
                rs.setLocation(toTrack.getLocation(), toTrack, RollingStock.FORCE);
            }
        }
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(LocationManager.class).removePropertyChangeListener(this);
        if (_destination != null) {
            _destination.removePropertyChangeListener(this);
        }
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("PropertyChange ({}) old ({}) new ({})", e.getPropertyName(), e.getOldValue(), e.getNewValue());
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateComboBoxes();
        }
        if (e.getSource() == _location) {
            updateTrackComboBox();
        }
        if (e.getSource() == _destination && e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
            dispose();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrackCopyFrame.class);
}

// LocationCopyFrame.java
package jmri.jmrit.operations.locations;

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
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for copying a location for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class LocationCopyFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 5798102907541807915L;

    LocationManager locationManager = LocationManager.instance();

    // text field
    JTextField loctionNameTextField = new javax.swing.JTextField(Control.max_len_string_location_name);

    // major buttons
    JButton copyButton = new javax.swing.JButton(Bundle.getMessage("Copy"));
    JButton saveButton = new javax.swing.JButton(Bundle.getMessage("Save"));

    // combo boxes
    JComboBox<Location> locationBox = locationManager.getComboBox();

    // checkboxes
    JCheckBox moveRollingStockCheckBox = new JCheckBox(Bundle.getMessage("MoveRollingStock"));
    JCheckBox deleteTrackCheckBox = new JCheckBox(Bundle.getMessage("DeleteCopiedTrack"));

    // remember state of checkboxes during a session
    static boolean moveRollingStock = false;
    static boolean deleteTrack = false;

    public LocationCopyFrame() {

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        // Layout the panel by rows
        // row 1
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationName")));
        addItem(pName, loctionNameTextField, 0, 0);

        // row 2
        JPanel pCopy = new JPanel();
        pCopy.setLayout(new GridBagLayout());
        pCopy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectLocationToCopy")));
        addItem(pCopy, locationBox, 0, 0);

        // row 3
        JPanel pOptions = new JPanel();
        pOptions.setLayout(new GridBagLayout());
        pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        addItemLeft(pOptions, moveRollingStockCheckBox, 0, 1);
        addItemLeft(pOptions, deleteTrackCheckBox, 0, 2);

        // row 4
        JPanel pButton = new JPanel();
        pButton.setLayout(new GridBagLayout());
        addItem(pButton, copyButton, 0, 0);
        addItem(pButton, saveButton, 1, 0);

        getContentPane().add(pName);
        getContentPane().add(pCopy);
        getContentPane().add(pOptions);
        getContentPane().add(pButton);

        // set the checkbox states
        moveRollingStockCheckBox.setSelected(moveRollingStock);
        deleteTrackCheckBox.setSelected(deleteTrack);
        deleteTrackCheckBox.setEnabled(moveRollingStockCheckBox.isSelected());

        // get notified if combo box gets modified
        locationManager.addPropertyChangeListener(this);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_CopyLocation", true); // NOI18N

        pack();
        setMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight400));

        // setup buttons
        addButtonAction(copyButton);
        addButtonAction(saveButton);

        addCheckBoxAction(moveRollingStockCheckBox);

        setTitle(Bundle.getMessage("MenuItemCopyLocation"));
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    protected void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == copyButton) {
            log.debug("copy location button activated");
            if (!checkName()) {
                return;
            }

            if (locationBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectLocationToCopy"), MessageFormat.format(Bundle
                        .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("Copy")}), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Location location = (Location) locationBox.getSelectedItem();
            // check to see if there are cars scheduled for pickup or set out
            if (moveRollingStockCheckBox.isSelected()) {
                for (Track track : location.getTrackList()) {
                    if (track.getPickupRS() > 0) {
                        JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
                                .getMessage("FoundRollingStockPickUp"), new Object[]{track.getPickupRS()}),
                                MessageFormat.format(Bundle.getMessage("TrainsServicingTrack"), new Object[]{track
                                    .getName()}), JOptionPane.WARNING_MESSAGE);
                        return; // can't move rolling stock, some are scheduled for a pick up
                    }
                    if (track.getDropRS() > 0) {
                        JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
                                .getMessage("FoundRollingStockDrop"), new Object[]{track.getDropRS()}),
                                MessageFormat.format(Bundle.getMessage("TrainsServicingTrack"), new Object[]{track
                                    .getName()}), JOptionPane.WARNING_MESSAGE);
                        return; // can't move rolling stock, some are scheduled for drops
                    }
                }
            }
            // now copy all of the tracks
            Location newLocation = locationManager.newLocation(loctionNameTextField.getText());
            location.copyLocation(newLocation);

            // does the user want the cars to also move to the new tracks?
            if (moveRollingStockCheckBox.isSelected()) {
                for (Track track : location.getTrackList()) {
                    moveRollingStock(track, newLocation.getTrackByName(track.getName(), null));
                    if (deleteTrackCheckBox.isSelected()) {
                        location.deleteTrack(track);
                    }
                }
            }
        }
        if (ae.getSource() == saveButton) {
            log.debug("save track button activated");
            // save checkbox states
            moveRollingStock = moveRollingStockCheckBox.isSelected();
            deleteTrack = deleteTrackCheckBox.isSelected();
            // save location file
            OperationsXml.save();
        }
    }

    protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == moveRollingStockCheckBox) {
            deleteTrackCheckBox.setEnabled(moveRollingStockCheckBox.isSelected());
            deleteTrackCheckBox.setSelected(false);
        }
    }

    protected void updateComboBoxes() {
        log.debug("update location combobox");
        Object item = locationBox.getSelectedItem(); // remember which object was selected
        locationManager.updateComboBox(locationBox);
        locationBox.setSelectedItem(item);
    }

    /**
     *
     * @return true if name entered OK and isn't too long
     */
    protected boolean checkName() {
        if (loctionNameTextField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
                    .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("Copy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (TrainCommon.splitString(loctionNameTextField.getText()).length() > Control.max_len_string_location_name) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("LocationNameLengthMax"),
                    new Object[]{Integer.toString(Control.max_len_string_location_name + 1)}), MessageFormat.format(Bundle
                            .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("Copy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Location check = locationManager.getLocationByName(loctionNameTextField.getText());
        if (check != null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("LocationAlreadyExists"), MessageFormat.format(Bundle
                    .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("Copy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    protected void moveRollingStock(Track fromTrack, Track toTrack) {
        moveRollingStock(fromTrack, toTrack, CarManager.instance());
        moveRollingStock(fromTrack, toTrack, EngineManager.instance());
    }

    private void moveRollingStock(Track fromTrack, Track toTrack, RollingStockManager manager) {
        for (RollingStock rs : manager.getByIdList()) {
            if (rs.getTrack() == fromTrack) {
                rs.setLocation(toTrack.getLocation(), toTrack, true);
            }
        }
    }

    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        super.dispose();
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("PropertyChange ({}) new: ({})", e.getPropertyName(), e.getNewValue());
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateComboBoxes();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocationCopyFrame.class.getName());
}

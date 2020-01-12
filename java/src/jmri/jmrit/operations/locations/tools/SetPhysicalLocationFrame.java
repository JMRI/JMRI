package jmri.jmrit.operations.locations.tools;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.PhysicalLocation;
import jmri.util.PhysicalLocationPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for setting train physical location coordinates for a location.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2011
 */
public class SetPhysicalLocationFrame extends OperationsFrame {

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    Location _location;

    // labels
    // text field
    // check boxes
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton closeButton = new JButton(Bundle.getMessage("Close"));

    // combo boxes
    JComboBox<Location> locationBox = InstanceManager.getDefault(LocationManager.class).getComboBox();

    // Spinners
    PhysicalLocationPanel physicalLocation;

    public SetPhysicalLocationFrame(Location l) {
        super(Bundle.getMessage("MenuSetPhysicalLocation"));

        // Store the location (null if called from the list view)
        _location = l;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // set tool tips
        saveButton.setToolTipText(Bundle.getMessage("TipSaveButton"));
        closeButton.setToolTipText(Bundle.getMessage("TipCloseButton"));

        // Set up the panels
        JPanel pLocation = new JPanel();
        pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        pLocation.add(locationBox);

        physicalLocation = new PhysicalLocationPanel(Bundle.getMessage("PhysicalLocation"));
        physicalLocation.setToolTipText(Bundle.getMessage("PhysicalLocationToolTip"));
        physicalLocation.setVisible(true);

        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pControl, saveButton, 1, 0);
        addItem(pControl, closeButton, 2, 0);

        getContentPane().add(pLocation);
        getContentPane().add(physicalLocation);
        getContentPane().add(pControl);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_SetTrainIconCoordinates", true); // fix this // NOI18N
        // later

        // setup buttons
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveButtonActionPerformed(e);
            }
        });
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeButtonActionPerformed(e);
            }
        });

        // setup combo box
        addComboBoxAction(locationBox);

        if (_location != null) {
            locationBox.setSelectedItem(_location);
        }

        pack();
        setVisible(true);
    }

    /**
     * Close button action
     * 
     * @param ae The ActionEvent.
     */
    public void closeButtonActionPerformed(java.awt.event.ActionEvent ae) {
        dispose();
    }

    /**
     * Save button action {@literal ->} save this Reporter's location
     * 
     * @param ae The ActionEvent.
     */
    public void saveButtonActionPerformed(java.awt.event.ActionEvent ae) {
        // check to see if a location has been selected
        if (locationBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("SelectLocationToEdit"),
                    Bundle.getMessage("NoLocationSelected"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        Location l = (Location) locationBox.getSelectedItem();
        if (l == null) {
            return;
        }
        if (ae.getSource() == saveButton) {
            int value = JOptionPane.showConfirmDialog(null, MessageFormat.format(
                    Bundle.getMessage("UpdatePhysicalLocation"), new Object[]{l.getName()}),
                    Bundle.getMessage("UpdateDefaults"), JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION) {
                saveSpinnerValues(l);
            }
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (locationBox.getSelectedItem() == null) {
            resetSpinners();
        } else {
            Location l = (Location) locationBox.getSelectedItem();
            loadSpinners(l);
        }
    }

    @Override
    public void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
        if (ae.getSource() == physicalLocation) {
            Location l = (Location) locationBox.getSelectedItem();
            if (l != null) {
                l.setPhysicalLocation(physicalLocation.getValue());
            }
        }
    }

    private void resetSpinners() {
        // Reset spinners to zero.
        physicalLocation.setValue(new PhysicalLocation());
    }

    private void loadSpinners(Location l) {
        log.debug("Load spinners location {}", l.getName());
        physicalLocation.setValue(l.getPhysicalLocation());
    }

    // Unused. Carried over from SetTrainIconPosition or whatever it was
    // called...
    /*
     * private void spinnersEnable(boolean enable){
     * physicalLocation.setEnabled(enable); }
     */
    private void saveSpinnerValues(Location l) {
        log.debug("Save physical coordinates for location {}", l.getName());
        l.setPhysicalLocation(physicalLocation.getValue());
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationFrame.class);
}

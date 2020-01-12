package jmri.jmrit.beantable;

import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsPanel;
import jmri.util.PhysicalLocation;
import jmri.util.PhysicalLocationPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create a SetPhysicalLocation dialog.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2011
 */
public class SetPhysicalLocationAction extends AbstractAction {

    Reporter _reporter;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.JmritBeantablePhysicalLocationBundle");

    /**
     * Constructor.
     *
     * @param s title of the action
     * @param reporter {@link Reporter} to use
     */
    public SetPhysicalLocationAction(String s, Reporter reporter) {
        super(s);
        _reporter = reporter;
    }

    SetPhysicalLocationFrame f = null;

    /**
     * Action method.
     *
     * @param e the associated {@link ActionEvent} that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new SetPhysicalLocationFrame(_reporter);
            f.setVisible(true);
        }
        f.setExtendedState(Frame.NORMAL);
    }

    /**
     * Frame for setting train physical location coordinates for a Reporter.
     *
     * @author Bob Jacobsen Copyright (C) 2001
     * @author Daniel Boudreau Copyright (C) 2010
     * @author Mark Underwood Copyright (C) 2011
     * 
     */
    private static class SetPhysicalLocationFrame extends OperationsFrame {

        /**
         * Frame Constructor.
         */
        public SetPhysicalLocationFrame(Reporter reporter) {
            super(rb.getString("MenuSetPhysicalLocation"), new SetPhysicalLocationPanel(reporter));

            // add help menu to window
            addHelpMenu("package.jmri.jmrit.operations.Operations_SetTrainIconCoordinates", true); // fix this later

            pack();
        }
    }

    private static class SetPhysicalLocationPanel extends OperationsPanel {

        Reporter _reporter;

        String emptyReporterString = "(No Reporters)";

        List<Reporter> _reporterList = new ArrayList<>();

        // labels
        // text field
        // check boxes
        // major buttons
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));

        // combo boxes
        JComboBox<String> reporterBox = getReporterComboBox();

        // Spinners
        PhysicalLocationPanel physicalLocation;

        public SetPhysicalLocationPanel(Reporter l) {

            // Store the location (null if called from the list view)
            _reporter = l;

            // general GUI config
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // set tool tips
            saveButton.setToolTipText(rb.getString("TipSaveButton"));
            closeButton.setToolTipText(rb.getString("TipCloseButton"));

            // Set up the panels
            JPanel pLocation = new JPanel();
            pLocation.setBorder(BorderFactory.createTitledBorder(rb.getString("ReporterName")));
            pLocation.setToolTipText(rb.getString("TipSelectReporter"));
            pLocation.add(reporterBox);

            physicalLocation = new PhysicalLocationPanel(rb.getString("PhysicalLocation"));
            physicalLocation.setToolTipText(rb.getString("TipPhysicalLocation"));
            physicalLocation.setVisible(true);

            JPanel pControl = new JPanel();
            pControl.setLayout(new GridBagLayout());
            pControl.setBorder(BorderFactory.createTitledBorder(""));
            addItem(pControl, saveButton, 1, 0);
            addItem(pControl, closeButton, 2, 0);

            add(pLocation);
            add(physicalLocation);
            add(pControl);

            // setup buttons
            saveButton.addActionListener(this::saveButtonActionPerformed);
            closeButton.addActionListener(this::closeButtonActionPerformed);

            // setup combo box
            addComboBoxAction(reporterBox);
            reporterBox.setSelectedIndex(0);

            if (_reporter != null) {
                reporterBox.setSelectedItem(_reporter);
            }

            setVisible(true);
        }

        /**
         * Construct the combo box with the list of available Reporters.
         */
        protected JComboBox<String> getReporterComboBox() {
            ReporterManager mgr = InstanceManager.getDefault(jmri.ReporterManager.class);
            List<String> displayList = new ArrayList<>();
            for (Reporter r : mgr.getNamedBeanSet()) {
                if (r != null) {
                    _reporterList.add(r);
                    displayList.add(r.getDisplayName());
                }
            }
            if (displayList.isEmpty()) {
                displayList.add(emptyReporterString);
                saveButton.setEnabled(false);
            }
            String[] sa = new String[displayList.size()];
            displayList.toArray(sa);
            JComboBox<String> retv = new JComboBox<String>(sa);
            return (retv);

        }

        /**
         * Close button action.
         */
        public void closeButtonActionPerformed(ActionEvent ae) {
            JOptionPane.showMessageDialog(null,
                    rb.getString("CloseButtonSaveWarning"),
                    rb.getString("CloseButtonSaveWarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            dispose();
        }

        /**
         * Save button action -> save this Reporter's location.
         */
        public void saveButtonActionPerformed(ActionEvent ae) {
            // check to see if a location has been selected
            if (reporterBox.getSelectedItem() == null
                    || reporterBox.getSelectedItem().equals("")) {
                JOptionPane.showMessageDialog(null,
                        rb.getString("SelectLocationToEdit"),
                        rb.getString("NoLocationSelected"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Reporter l = getReporterFromList();
            if (l == null) {
                return;
            }
            int value = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("UpdatePhysicalLocation"),
                    new Object[]{l.getDisplayName()}), rb.getString("SaveLocation?"), JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION) {
                saveSpinnerValues(l);
            }
        }

        /**
         * Get a Reporter from its name in the combo box.
         */
        private Reporter getReporterFromList() {
            String s = (String) reporterBox.getSelectedItem();
            // Since we don't have "getByDisplayName()" we need to do this in two steps
            Reporter r = InstanceManager.getDefault(jmri.ReporterManager.class).getByDisplayName(s);
            return (r);
        }

        /**
         * Combo box action.
         */
        @Override
        public void comboBoxActionPerformed(ActionEvent ae) {
            if (reporterBox.getSelectedItem() != null) {
                if (reporterBox.getSelectedItem().equals("") || reporterBox.getSelectedItem().equals(emptyReporterString)) {
                    resetSpinners();
                } else {
                    Reporter l = getReporterFromList();
                    loadSpinners(l);
                }
            }
        }

        /**
         * Spinner change event.
         */
        @Override
        public void spinnerChangeEvent(ChangeEvent ae) {
            if (ae.getSource() == physicalLocation) {
                Reporter l = getReporterFromList();
                if (l != null) {
                    PhysicalLocation.setBeanPhysicalLocation(physicalLocation.getValue(), l);
                }
            }
        }

        /**
         * Reset spinners to zero.
         */
        private void resetSpinners() {
            // Reset spinners to zero.
            physicalLocation.setValue(new PhysicalLocation());
        }

        /**
         * Load spinners from an existing Reporter
         */
        private void loadSpinners(Reporter r) {
            log.debug("Load spinners Reporter location " + r.getSystemName());
            physicalLocation.setValue(PhysicalLocation.getBeanPhysicalLocation(r));
        }

        // Unused. Carried over from SetTrainIconPosition or whatever it was
        // called...
 /*
         * private void spinnersEnable(boolean enable){
         * physicalLocation.setEnabled(enable); }
         */
        /**
         * Write spinner values to a Reporter.
         */
        private void saveSpinnerValues(Reporter r) {
            log.debug("Save train icons coordinates for location " + r.getSystemName());
            PhysicalLocation.setBeanPhysicalLocation(physicalLocation.getValue(), r);
        }

    }

    private static final Logger log = LoggerFactory.getLogger(SetPhysicalLocationAction.class);

}

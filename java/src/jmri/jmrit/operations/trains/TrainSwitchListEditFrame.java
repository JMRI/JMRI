// TrainSwitchListEditFrame.java
package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
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
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.excel.SetupExcelProgramSwitchListFrameAction;
import jmri.jmrit.operations.trains.excel.TrainCustomSwitchList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user selection of switch lists
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012, 2013, 2014
 * @version $Revision$
 */
public class TrainSwitchListEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    JScrollPane switchPane;

    // load managers
    LocationManager locationManager = LocationManager.instance();
    List<JCheckBox> locationCheckBoxes = new ArrayList<JCheckBox>();
    List<JComboBox<String>> locationComboBoxes = new ArrayList<JComboBox<String>>();
    JPanel locationPanelCheckBoxes = new JPanel();

    // checkboxes
    JCheckBox switchListRealTimeCheckBox = new JCheckBox(Bundle.getMessage("SwitchListRealTime"));
    JCheckBox switchListAllTrainsCheckBox = new JCheckBox(Bundle.getMessage("SwitchListAllTrains"));

    // major buttons
    JButton clearButton = new JButton(Bundle.getMessage("Clear"));
    JButton setButton = new JButton(Bundle.getMessage("Select"));
    JButton printButton = new JButton(Bundle.getMessage("PrintSwitchLists"));
    JButton previewButton = new JButton(Bundle.getMessage("PreviewSwitchLists"));
    JButton printChangesButton = new JButton(Bundle.getMessage("PrintChanges"));
    JButton runButton = new JButton(Bundle.getMessage("RunFile"));
    JButton runChangeButton = new JButton(Bundle.getMessage("RunFileChanges"));
    JButton csvGenerateButton = new JButton(Bundle.getMessage("CsvGenerate"));
    JButton csvChangeButton = new JButton(Bundle.getMessage("CsvChanges"));
    JButton updateButton = new JButton(Bundle.getMessage("Update"));
    JButton resetButton = new JButton(Bundle.getMessage("ResetSwitchLists"));
    JButton saveButton = new JButton(Bundle.getMessage("Save"));

    JComboBox<String> switchListPageComboBox = Setup.getSwitchListPageFormatComboBox();

    // panels
    JPanel customPanel;

    public TrainSwitchListEditFrame() {
        super(Bundle.getMessage("TitleSwitchLists"));
    }

    public void initComponents() {
        // listen for any changes in the number of locations
        locationManager.addPropertyChangeListener(this);

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // tool tips
        switchListRealTimeCheckBox.setToolTipText(Bundle.getMessage("RealTimeTip"));
        switchListAllTrainsCheckBox.setToolTipText(Bundle.getMessage("AllTrainsTip"));
        switchListPageComboBox.setToolTipText(Bundle.getMessage("PageTrainTip"));
        csvChangeButton.setToolTipText(Bundle.getMessage("CsvChangesTip"));
        printChangesButton.setToolTipText(Bundle.getMessage("PrintChangesTip"));
        resetButton.setToolTipText(Bundle.getMessage("ResetSwitchListTip"));

        switchPane = new JScrollPane(locationPanelCheckBoxes);
        switchPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        switchPane.setBorder(BorderFactory.createTitledBorder(""));

        // Layout the panel by rows
        locationPanelCheckBoxes.setLayout(new GridBagLayout());
        updateLocationCheckboxes();
        enableChangeButtons();

        // Clear and set buttons
        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        pButtons.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pButtons, clearButton, 0, 1);
        addItem(pButtons, setButton, 1, 1);

        // options
        JPanel pSwitchListOptions = new JPanel();
        pSwitchListOptions.setLayout(new GridBagLayout());
        pSwitchListOptions.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutSwitchListOptions")));

        JPanel pSwitchListPageFormat = new JPanel();
        pSwitchListPageFormat.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutSwitchListPageFormat")));
        pSwitchListPageFormat.add(switchListPageComboBox);

        addItem(pSwitchListOptions, switchListAllTrainsCheckBox, 1, 0);
        addItem(pSwitchListOptions, pSwitchListPageFormat, 2, 0);
        addItem(pSwitchListOptions, switchListRealTimeCheckBox, 3, 0);
        addItem(pSwitchListOptions, saveButton, 4, 0);

        // buttons
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder(""));

        // row 3
        addItem(controlPanel, previewButton, 0, 2);
        addItem(controlPanel, printButton, 1, 2);
        addItem(controlPanel, printChangesButton, 2, 2);
        // row 4
        addItem(controlPanel, updateButton, 0, 3);
        addItem(controlPanel, resetButton, 1, 3);

        // row 5
        customPanel = new JPanel();
        customPanel.setLayout(new GridBagLayout());
        customPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutCustomSwitchLists")));

        addItem(customPanel, csvGenerateButton, 1, 4);
        addItem(customPanel, csvChangeButton, 2, 4);
        addItem(customPanel, runButton, 1, 5);
        addItem(customPanel, runChangeButton, 2, 5);

        getContentPane().add(switchPane);
        getContentPane().add(pButtons);
        getContentPane().add(pSwitchListOptions);
        getContentPane().add(controlPanel);
        getContentPane().add(customPanel);

        customPanel.setVisible(Setup.isGenerateCsvSwitchListEnabled());

        // Set the state
        switchListRealTimeCheckBox.setSelected(Setup.isSwitchListRealTime());
        switchListAllTrainsCheckBox.setSelected(Setup.isSwitchListAllTrainsEnabled());
        switchListPageComboBox.setSelectedItem(Setup.getSwitchListPageFormat());

        updateButton.setVisible(!switchListRealTimeCheckBox.isSelected());
        resetButton.setVisible(!switchListRealTimeCheckBox.isSelected());
        saveButton.setEnabled(false);

        addButtonAction(clearButton);
        addButtonAction(setButton);
        addButtonAction(printButton);
        addButtonAction(previewButton);
        addButtonAction(printChangesButton);
        addButtonAction(runButton);
        addButtonAction(runChangeButton);
        addButtonAction(csvGenerateButton);
        addButtonAction(csvChangeButton);
        addButtonAction(updateButton);
        addButtonAction(resetButton);
        addButtonAction(saveButton);

        addCheckBoxAction(switchListRealTimeCheckBox);
        addCheckBoxAction(switchListAllTrainsCheckBox);

        addComboBoxAction(switchListPageComboBox);

        Setup.addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
        toolMenu.add(new SetupExcelProgramSwitchListFrameAction(Bundle.getMessage("MenuItemSetupExcelProgramSwitchList")));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_SwitchList", true); // NOI18N
        // set frame size and train for display
        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }
    
    private static final boolean IS_PREVIEW = true;
    private static final boolean IS_CHANGED = true;
    private static final boolean IS_CSV = true;
    private static final boolean IS_UPDATE = true;

    // Buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == previewButton) {
            buildSwitchList(IS_PREVIEW, !IS_CHANGED, !IS_CSV, !IS_UPDATE);
        }
        if (ae.getSource() == printButton) {
            buildSwitchList(!IS_PREVIEW, !IS_CHANGED, !IS_CSV, !IS_UPDATE);
        }
        if (ae.getSource() == printChangesButton) {
            buildSwitchList(!IS_PREVIEW, IS_CHANGED, !IS_CSV, !IS_UPDATE);
        }
        if (ae.getSource() == csvGenerateButton) {
            buildSwitchList(!IS_PREVIEW, !IS_CHANGED, IS_CSV, !IS_UPDATE);
        }
        if (ae.getSource() == csvChangeButton) {
            buildSwitchList(!IS_PREVIEW, IS_CHANGED, IS_CSV, !IS_UPDATE);
        }
        if (ae.getSource() == updateButton) {
            buildSwitchList(IS_PREVIEW, !IS_CHANGED, !IS_CSV, IS_UPDATE);
        }
        if (ae.getSource() == runButton) {
            runCustomSwitchLists(!IS_CHANGED);
        }
        if (ae.getSource() == runChangeButton) {
            runCustomSwitchLists(IS_CHANGED);
        }
        if (ae.getSource() == resetButton) {
            reset();
        }
        if (ae.getSource() == saveButton) {
            save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == switchListRealTimeCheckBox) {
            updateButton.setVisible(!switchListRealTimeCheckBox.isSelected());
            resetButton.setVisible(!switchListRealTimeCheckBox.isSelected());
        }
        // enable the save button whenever a checkbox is changed
        enableSaveButton(true);
    }

    // Remove all terminated or reset trains from the switch lists for selected locations
    private void reset() {
        for (int i = 0; i < locationCheckBoxes.size(); i++) {
            String locationName = locationCheckBoxes.get(i).getName();
            Location location = locationManager.getLocationByName(locationName);
            if (location.isSwitchListEnabled()) {
                // new switch lists will now be created for the location
                location.setSwitchListState(Location.SW_CREATE);
                location.setStatus(Location.MODIFIED);
            }
        }
        // set trains switch lists unknown, any built trains should remain on the switch lists
        TrainManager.instance().setTrainsSwitchListStatus(Train.UNKNOWN);
    }

    // save printer selection
    private void save() {
        for (int i = 0; i < locationCheckBoxes.size(); i++) {
            String locationName = locationCheckBoxes.get(i).getName();
            Location location = locationManager.getLocationByName(locationName);
            JComboBox<?> comboBox = locationComboBoxes.get(i);
            String printerName = (String) comboBox.getSelectedItem();
            if (printerName == null || printerName.equals(TrainPrintUtilities.getDefaultPrinterName())) {
                location.setDefaultPrinterName(Location.NONE);
            } else {
                log.debug("Location " + location.getName() + " has selected printer " + printerName);
                location.setDefaultPrinterName(printerName);
            }
        }
        Setup.setSwitchListRealTime(switchListRealTimeCheckBox.isSelected());
        Setup.setSwitchListAllTrainsEnabled(switchListAllTrainsCheckBox.isSelected());
        Setup.setSwitchListPageFormat((String) switchListPageComboBox.getSelectedItem());
        // save location file
        OperationsXml.save();
        enableSaveButton(false);
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    /**
     * Print = all false;
     *
     * @param isPreview true if print preview
     * @param isChanged true if only print changes was requested
     * @param isCsv     true if building a CSV switch list files
     * @param isUpdate  true if only updating switch lists (no printing or preview)
     */
    private void buildSwitchList(boolean isPreview, boolean isChanged, boolean isCsv, boolean isUpdate) {
        TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
        for (Location location : LocationManager.instance().getLocationsByNameList()) {
            if (location.isSwitchListEnabled()) {
                if (!isCsv) {
                    // update switch list
                    trainSwitchLists.buildSwitchList(location);
                    // print or only print changes
                    if (!isUpdate && (!isChanged || isChanged && location.getStatus().equals(Location.MODIFIED))) {
                        trainSwitchLists.printSwitchList(location, isPreview);
                    }
                } else if (Setup.isGenerateCsvSwitchListEnabled()
                        && (!isChanged || isChanged && location.getStatus().equals(Location.MODIFIED))) {
                    TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
                    trainCsvSwitchLists.buildSwitchList(location);
                }
            }
        }
        // set trains switch lists printed
        TrainManager.instance().setTrainsSwitchListStatus(Train.PRINTED);
    }

    private void selectCheckboxes(boolean enable) {
        for (int i = 0; i < locationCheckBoxes.size(); i++) {
            String locationName = locationCheckBoxes.get(i).getName();
            Location l = locationManager.getLocationByName(locationName);
            l.setSwitchListEnabled(enable);
        }
        // enable the save button whenever a checkbox is changed
        saveButton.setEnabled(true);
    }

    // name change or number of locations has changed
    private void updateLocationCheckboxes() {
        List<Location> locations = locationManager.getLocationsByNameList();
        synchronized (this) {
            for (Location location : locations) {
                location.removePropertyChangeListener(this);
            }
        }

        locationCheckBoxes.clear();
        locationComboBoxes.clear(); // remove printer selection
        locationPanelCheckBoxes.removeAll();

        // create header
        addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Location")), 0, 0);
        addItem(locationPanelCheckBoxes, new JLabel("        "), 1, 0);
        addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Status")), 2, 0);
        addItem(locationPanelCheckBoxes, new JLabel("        "), 3, 0);
        addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Comment")), 4, 0);
        addItem(locationPanelCheckBoxes, new JLabel("        "), 5, 0);
        addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Printer")), 6, 0);

        int y = 1; // vertical position in panel

        Location mainLocation = null; // user can have multiple locations with the "same" name.

        for (Location location : locations) {
            String name = TrainCommon.splitString(location.getName());
            if (mainLocation != null && TrainCommon.splitString(mainLocation.getName()).equals(name)) {
                location.setSwitchListEnabled(mainLocation.isSwitchListEnabled());
                if (mainLocation.isSwitchListEnabled() && location.getStatus().equals(Location.MODIFIED)) {
                    mainLocation.setStatus(Location.MODIFIED); // we need to update the primary location
                    location.setStatus(Location.UPDATED); // and clear the secondaries
                }
                continue;
            }
            mainLocation = location;

            JCheckBox checkBox = new JCheckBox();
            locationCheckBoxes.add(checkBox);
            checkBox.setSelected(location.isSwitchListEnabled());
            checkBox.setText(name);
            checkBox.setName(location.getName());
            addLocationCheckBoxAction(checkBox);
            addItemLeft(locationPanelCheckBoxes, checkBox, 0, y);

            JLabel status = new JLabel(location.getStatus());
            addItem(locationPanelCheckBoxes, status, 2, y);

            JButton button = new JButton(Bundle.getMessage("Add"));
            if (!location.getSwitchListComment().equals(Location.NONE)) {
                button.setText(Bundle.getMessage("Edit"));
            }
            button.setName(location.getName());
            addCommentButtonAction(button);
            addItem(locationPanelCheckBoxes, button, 4, y);

            JComboBox<String> comboBox = TrainPrintUtilities.getPrinterJComboBox();
            locationComboBoxes.add(comboBox);
            comboBox.setSelectedItem(location.getDefaultPrinterName());
            addComboBoxAction(comboBox);
            addItem(locationPanelCheckBoxes, comboBox, 6, y++);

        }

        // restore listeners
        synchronized (this) {
            for (Location location : locations) {
                location.addPropertyChangeListener(this);
            }
        }

        locationPanelCheckBoxes.revalidate();
        pack();
        repaint();
    }

    private void runCustomSwitchLists(boolean isChanged) {
        if (!Setup.isGenerateCsvSwitchListEnabled()) {
            return;
        }
        log.debug("run custom switch lists");
        TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
        TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
        for (int i = 0; i < locationCheckBoxes.size(); i++) {
            String locationName = locationCheckBoxes.get(i).getName();
            Location location = locationManager.getLocationByName(locationName);
            if (location.isSwitchListEnabled()
                    && (!isChanged || isChanged && location.getStatus().equals(Location.MODIFIED))) {
                // also build the regular switch lists so they can be used
                if (!switchListRealTimeCheckBox.isSelected()) {
                    trainSwitchLists.buildSwitchList(location);
                }
                File csvFile = trainCsvSwitchLists.buildSwitchList(location);
                if (csvFile == null || !csvFile.exists()) {
                    log.error("CSV switch list file was not created for location {}", locationName);
                    return;
                }

                TrainCustomSwitchList.addCVSFile(csvFile);
            }
        }
        // Processes the CSV Manifest files using an external custom program.
        if (!TrainCustomSwitchList.manifestCreatorFileExists()) {
            log.warn("Manifest creator file not found!, directory name: {}, file name: {}", TrainCustomSwitchList
                    .getDirectoryName(), TrainCustomSwitchList.getFileName());
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("LoadDirectoryNameFileName"),
                    new Object[]{TrainCustomSwitchList.getDirectoryName(), TrainCustomSwitchList.getFileName()}),
                    Bundle.getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Now run the user specified custom Switch List processor program
        TrainCustomSwitchList.process();
        // set trains switch lists printed
        TrainManager.instance().setTrainsSwitchListStatus(Train.PRINTED);
    }

    private void enableSaveButton(boolean enable) {
        saveButton.setEnabled(enable);
        // these get the inverse
        previewButton.setEnabled(!enable);
        printButton.setEnabled(!enable);
        updateButton.setEnabled(!enable);
        resetButton.setEnabled(!enable);
    }

    private void enableChangeButtons() {
        printChangesButton.setEnabled(false);
        csvChangeButton.setEnabled(false);
        runChangeButton.setEnabled(false);
        for (Location location : locationManager.getLocationsByNameList()) {
            if (location.getStatus().equals(Location.MODIFIED) && location.isSwitchListEnabled()) {
                printChangesButton.setEnabled(true);
                csvChangeButton.setEnabled(true);
                runChangeButton.setEnabled(true);
            }
        }
    }

    // The print switch list for a location has changed
    private void changeLocationCheckboxes(PropertyChangeEvent e) {
        Location l = (Location) e.getSource();
        for (int i = 0; i < locationCheckBoxes.size(); i++) {
            JCheckBox checkBox = locationCheckBoxes.get(i);
            if (checkBox.getName().equals(l.getName())) {
                checkBox.setSelected(l.isSwitchListEnabled());
                break;
            }
        }
    }

    private void addLocationCheckBoxAction(JCheckBox b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                locationCheckBoxActionPerformed(e);
            }
        });
    }

    public void locationCheckBoxActionPerformed(ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change " + b.getName());
        Location l = locationManager.getLocationByName(b.getName());
        l.setSwitchListEnabled(b.isSelected());
        // enable the save button whenever a checkbox is changed
        saveButton.setEnabled(true);
    }

    private void addCommentButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                commentButtonActionPerformed(e);
            }
        });
    }

    public void commentButtonActionPerformed(ActionEvent ae) {
        JButton b = (JButton) ae.getSource();
        log.debug("button action " + b.getName());
        Location l = locationManager.getLocationByName(b.getName());
        new TrainSwitchListCommentFrame(l);
    }

    protected void comboBoxActionPerformed(ActionEvent ae) {
        log.debug("combo box action");
        enableSaveButton(true);
    }

    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        Setup.removePropertyChangeListener(this);
        for (Location location : locationManager.getLocationsByNameList()) {
            location.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) {
            changeLocationCheckboxes(e);
            enableChangeButtons();
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Location.SWITCHLIST_COMMENT_CHANGED_PROPERTY)) {
            updateLocationCheckboxes();
            enableChangeButtons();
        }
        if (e.getPropertyName().equals(Setup.SWITCH_LIST_CSV_PROPERTY_CHANGE)) {
            customPanel.setVisible(Setup.isGenerateCsvSwitchListEnabled());
        }
    }

    private static class TrainSwitchListCommentFrame extends OperationsFrame {

        /**
         *
         */
        private static final long serialVersionUID = 4880037349897207594L;
        // text area
        JTextArea commentTextArea = new JTextArea(10, 90);
        JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension minScrollerDim = new Dimension(1200, 500);
        JButton saveButton = new JButton(Bundle.getMessage("Save"));

        Location _location;

        private TrainSwitchListCommentFrame(Location location) {
            super();
            initComponents(location);
        }

        private void initComponents(Location location) {
            _location = location;
            // the following code sets the frame's initial state
            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            JPanel pC = new JPanel();
            pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
            pC.setLayout(new GridBagLayout());
            commentScroller.setMinimumSize(minScrollerDim);
            addItem(pC, commentScroller, 1, 0);

            commentTextArea.setText(location.getSwitchListComment());

            JPanel pB = new JPanel();
            pB.setLayout(new GridBagLayout());
            addItem(pB, saveButton, 0, 0);

            getContentPane().add(pC);
            getContentPane().add(pB);

            addButtonAction(saveButton);

            pack();
            setTitle(location.getName());
            setVisible(true);
        }

        // Buttons
        public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
            if (ae.getSource() == saveButton) {
                _location.setSwitchListComment(commentTextArea.getText());
                // save location file
                OperationsXml.save();
                if (Setup.isCloseWindowOnSaveEnabled()) {
                    super.dispose();
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainSwitchListEditFrame.class.getName());
}

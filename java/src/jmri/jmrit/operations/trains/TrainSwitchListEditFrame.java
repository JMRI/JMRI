package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.excel.SetupExcelProgramSwitchListFrameAction;
import jmri.jmrit.operations.trains.excel.TrainCustomSwitchList;

/**
 * Frame for user selection of switch lists
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012, 2013, 2014, 2021
 */
public class TrainSwitchListEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    JScrollPane switchPane;

    // load managers
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    List<JCheckBox> locationCheckBoxes = new ArrayList<>();
    List<JComboBox<String>> locationComboBoxes = new ArrayList<>();
    JPanel locationPanelCheckBoxes = new JPanel();

    // checkboxes
    JCheckBox switchListRealTimeCheckBox = new JCheckBox(Bundle.getMessage("SwitchListRealTime"));
    JCheckBox switchListAllTrainsCheckBox = new JCheckBox(Bundle.getMessage("SwitchListAllTrains"));

    // major buttons
    JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    JButton setButton = new JButton(Bundle.getMessage("SelectAll"));
    JButton printButton = new JButton(Bundle.getMessage("PrintSwitchLists"));
    JButton previewButton = new JButton(Bundle.getMessage("PreviewSwitchLists"));
    JButton printChangesButton = new JButton(Bundle.getMessage("PrintChanges"));
    JButton runButton = new JButton(Bundle.getMessage("RunFile"));
    JButton runChangeButton = new JButton(Bundle.getMessage("RunFileChanges"));
    JButton openFileButton = new JButton(Bundle.getMessage("OpenFile"));
    JButton updateButton = new JButton(Bundle.getMessage("Update"));
    JButton resetButton = new JButton(Bundle.getMessage("ResetSwitchLists"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    JComboBox<String> switchListPageComboBox = Setup.getSwitchListPageFormatComboBox();

    // panels
    JPanel customPanel;

    public TrainSwitchListEditFrame() {
        super(Bundle.getMessage("TitleSwitchLists"));
    }

    @Override
    public void initComponents() {
        // listen for any changes in the number of locations
        locationManager.addPropertyChangeListener(this);

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // tool tips
        switchListRealTimeCheckBox.setToolTipText(Bundle.getMessage("RealTimeTip"));
        switchListAllTrainsCheckBox.setToolTipText(Bundle.getMessage("AllTrainsTip"));
        switchListPageComboBox.setToolTipText(Bundle.getMessage("PageTrainTip"));
        printChangesButton.setToolTipText(Bundle.getMessage("PrintChangesTip"));
        resetButton.setToolTipText(Bundle.getMessage("ResetSwitchListTip"));

        switchPane = new JScrollPane(locationPanelCheckBoxes);
        switchPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        switchPane.setBorder(BorderFactory.createTitledBorder(""));

        // Layout the panel by rows
        locationPanelCheckBoxes.setLayout(new GridBagLayout());
        updateLocationCheckboxes();
        enableSaveButton(false);

        // Clear and set buttons
        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        pButtons.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pButtons, clearButton, 0, 1);
        addItem(pButtons, setButton, 1, 1);

        // options
        JPanel pSwitchListOptions = new JPanel();
        pSwitchListOptions.setLayout(new GridBagLayout());
        pSwitchListOptions
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListOptions")));

        JPanel pSwitchListPageFormat = new JPanel();
        pSwitchListPageFormat
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListPageFormat")));
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

        addItem(customPanel, runButton, 1, 5);
        addItem(customPanel, runChangeButton, 2, 5);
        addItem(customPanel, openFileButton, 3, 5);

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
        addButtonAction(openFileButton);
        addButtonAction(updateButton);
        addButtonAction(resetButton);
        addButtonAction(saveButton);

        addCheckBoxAction(switchListRealTimeCheckBox);
        addCheckBoxAction(switchListAllTrainsCheckBox);

        addComboBoxAction(switchListPageComboBox);

        Setup.getDefault().addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new SetupExcelProgramSwitchListFrameAction());
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_SwitchList", true); // NOI18N
        // set frame size and train for display
        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    private static final boolean IS_CHANGED = true;
    private static final boolean IS_PRINT = true; // print or preview
    private static final boolean IS_PREVIEW = true;

    // Buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == previewButton) {
            buildSwitchList(!IS_CHANGED, IS_PRINT, IS_PREVIEW);
        }
        if (ae.getSource() == printButton) {
            buildSwitchList(!IS_CHANGED, IS_PRINT, !IS_PREVIEW);
        }
        if (ae.getSource() == printChangesButton) {
            buildSwitchList(IS_CHANGED, IS_PRINT, !IS_PREVIEW);
        }
        if (ae.getSource() == updateButton) {
            buildSwitchList(!IS_CHANGED, !IS_PRINT, IS_PREVIEW);
        }
        if (ae.getSource() == runButton) {
            runCustomSwitchLists(!IS_CHANGED);
        }
        if (ae.getSource() == runChangeButton) {
            runCustomSwitchLists(IS_CHANGED);
        }
        if (ae.getSource() == openFileButton) {
            buildSwitchList(IS_CHANGED, !IS_PRINT, IS_PREVIEW);
            openCsvSwitchList();
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

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == switchListRealTimeCheckBox) {
            updateButton.setVisible(!switchListRealTimeCheckBox.isSelected());
            resetButton.setVisible(!switchListRealTimeCheckBox.isSelected());
        }
        // enable the save button whenever a checkbox is changed
        enableSaveButton(true);
    }

    // Remove all terminated or reset trains from the switch lists for selected
    // locations
    private void reset() {
        // Confirm that user really wants to delete all terminated and reset trains from
        // the switch lists
        if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("DoYouReallyWantDeleteSwitchListData"),
                Bundle.getMessage("ResetSwitchLists"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        // this for loop prevents ConcurrentModificationException when printing and
        // status changes
        for (JCheckBox checkbox : new ArrayList<>(locationCheckBoxes)) {
            String locationName = checkbox.getName();
            Location location = locationManager.getLocationByName(locationName);
            if (location.isSwitchListEnabled()) {
                // new switch lists will now be created for the location
                location.setSwitchListState(Location.SW_CREATE);
                location.setStatus(Location.MODIFIED);
            }
        }
        // set trains switch lists unknown, any built trains should remain on the switch
        // lists
        InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.UNKNOWN);
    }

    // save printer selection
    private void save() {
        // this for loop prevents ConcurrentModificationException when printing and
        // status changes
        for (int i = 0; i < locationCheckBoxes.size(); i++) {
            String locationName = locationCheckBoxes.get(i).getName();
            Location location = locationManager.getLocationByName(locationName);
            JComboBox<String> comboBox = locationComboBoxes.get(i);
            String printerName = (String) comboBox.getSelectedItem();
            if (printerName == null || printerName.equals(TrainPrintUtilities.getDefaultPrinterName())) {
                location.setDefaultPrinterName(Location.NONE);
            } else {
                log.debug("Location {} has selected printer {}", location.getName(), printerName);
                location.setDefaultPrinterName(printerName);
            }
        }
        // reset if changing to consolidation mode
        if (Setup.isSwitchListRealTime() && !switchListRealTimeCheckBox.isSelected()) {
            reset();
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
     *
     * @param isPreview true if print preview
     * @param isChanged true if only print changes was requested
     * @param isPrint   true if printing or preview
     */
    private void buildSwitchList(boolean isChanged, boolean isPrint, boolean isPreview) {
        TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
        TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
        // this for loop prevents ConcurrentModificationException when printing and
        // status changes
        for (JCheckBox checkbox : new ArrayList<>(locationCheckBoxes)) {
            String locationName = checkbox.getName();
            Location location = locationManager.getLocationByName(locationName);
            if (location.isSwitchListEnabled() && (!isChanged || location.getStatus().equals(Location.MODIFIED))) {
                // update switch lists
                trainCsvSwitchLists.buildSwitchList(location);
                trainSwitchLists.buildSwitchList(location);
                // print or only print changes
                if (isPrint) {
                    trainSwitchLists.printSwitchList(location, isPreview);
                }
            }
        }
        // set trains switch lists printed
        InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.PRINTED);
    }

    private void selectCheckboxes(boolean enable) {
        for (JCheckBox checkbox : new ArrayList<>(locationCheckBoxes)) {
            String locationName = checkbox.getName();
            Location location = locationManager.getLocationByName(locationName);
            location.setSwitchListEnabled(enable);
        }
        // enable the save button whenever a checkbox is changed
        saveButton.setEnabled(true);
    }

    // TODO there's a ConcurrentModificationException when the printer status
    // changes
    // when printing. This routine rebuilds the locationCheckBoxes during the
    // update.
    // A better solution would only update the status for a location.
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
        }

        mainLocation = null;

        for (Location location : locations) {
            String name = TrainCommon.splitString(location.getName());
            if (mainLocation != null && TrainCommon.splitString(mainLocation.getName()).equals(name)) {
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
            if (!location.getSwitchListCommentWithColor().isEmpty()) {
                button.setText(Bundle.getMessage("ButtonEdit"));
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

    /**
     * Creates custom switch lists using an external program like MS Excel. Switch
     * lists are created for locations that have switch lists enabled.
     * 
     * @param isChanged when true, only create custom switch list for enabled
     *                  locations that have changes. When isChanged is false, create
     *                  custom switch lists for all enabled locations.
     */
    private void runCustomSwitchLists(boolean isChanged) {
        if (!Setup.isGenerateCsvSwitchListEnabled()) {
            return;
        }
        log.debug("run custom switch lists");
        TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
        TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
        // this for loop prevents ConcurrentModificationException when printing and
        // status changes
        for (JCheckBox checkbox : new ArrayList<>(locationCheckBoxes)) {
            String locationName = checkbox.getName();
            Location location = locationManager.getLocationByName(locationName);
            if (location.isSwitchListEnabled() && (!isChanged || location.getStatus().equals(Location.MODIFIED))) {
                File csvFile = trainCsvSwitchLists.buildSwitchList(location);
                // also build the regular switch lists so they can be used
                trainSwitchLists.buildSwitchList(location);
                if (csvFile == null || !csvFile.exists()) {
                    log.error("CSV switch list file was not created for location {}", locationName);
                    return;
                }
                InstanceManager.getDefault(TrainCustomSwitchList.class).addCsvFile(csvFile);
            }
        }
        // Processes the CSV Manifest files using an external custom program.
        if (!InstanceManager.getDefault(TrainCustomSwitchList.class).excelFileExists()) {
            log.warn("Manifest creator file not found!, directory name: {}, file name: {}",
                    InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName(),
                    InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName());
            JOptionPane.showMessageDialog(this,
                    MessageFormat.format(Bundle.getMessage("LoadDirectoryNameFileName"),
                            new Object[] { InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName(),
                                    InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName() }),
                    Bundle.getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Now run the user specified custom Switch List processor program
        InstanceManager.getDefault(TrainCustomSwitchList.class).process();
        // set trains switch lists printed
        InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.PRINTED);
    }

    private void openCsvSwitchList() {
        File file = selectFile();
        if (file != null) {
            TrainUtilities.openDesktop(file);
        }
    }

    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    JFileChooser fc;

    private File selectFile() {
        if (fc == null) {
            fc = new JFileChooser(
                    InstanceManager.getDefault(TrainManagerXml.class).getDefaultCsvSwitchListDirectoryName());
            fc.setFileFilter(new FileNameExtensionFilter("Comma Separated Values", "csv")); // NOI18N
            fc.setDialogTitle(Bundle.getMessage("TitleSwitchLists"));
        }
        // when reusing the chooser, make sure new files are included
        fc.rescanCurrentDirectory();
        int retVal = fc.showOpenDialog(this);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        return null;
    }

    private void enableSaveButton(boolean enable) {
        saveButton.setEnabled(enable);
        // these get the inverse
        previewButton.setEnabled(!enable);
        printButton
                .setEnabled(!enable && (!Control.disablePrintingIfCustom || !Setup.isGenerateCsvSwitchListEnabled()));
        resetButton.setEnabled(!enable);
        runButton.setEnabled(!enable);
        openFileButton.setEnabled(!enable);
        // disable the following, and turn then back on if needed
        printChangesButton.setEnabled(false);
        runChangeButton.setEnabled(false);
        updateButton.setEnabled(false);
        if (!enable) {
            enableChangeButtons();
        }
    }

    private void enableChangeButtons() {
        printChangesButton.setEnabled(false);
        runChangeButton.setEnabled(false);
        updateButton.setEnabled(false);
        for (Location location : locationManager.getLocationsByNameList()) {
            if (location.getStatus().equals(Location.MODIFIED) && location.isSwitchListEnabled()) {
                printChangesButton
                        .setEnabled((!Control.disablePrintingIfCustom || !Setup.isGenerateCsvSwitchListEnabled()));
                runChangeButton.setEnabled(true);
                updateButton.setEnabled(true);
            }
        }
    }

    // The print switch list for a location has changed
    private void changeLocationCheckboxes(PropertyChangeEvent e) {
        Location l = (Location) e.getSource();
        for (JCheckBox checkbox : new ArrayList<>(locationCheckBoxes)) {
            if (checkbox.getName().equals(l.getName())) {
                checkbox.setSelected(l.isSwitchListEnabled());
                break;
            }
        }
    }

    private void addLocationCheckBoxAction(JCheckBox b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                locationCheckBoxActionPerformed(e);
            }
        });
    }

    public void locationCheckBoxActionPerformed(ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change {}", b.getName());
        Location l = locationManager.getLocationByName(b.getName());
        l.setSwitchListEnabled(b.isSelected());
        // enable the save button whenever a checkbox is changed
        saveButton.setEnabled(true);
    }

    private void addCommentButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                commentButtonActionPerformed(e);
            }
        });
    }

    public void commentButtonActionPerformed(ActionEvent ae) {
        JButton b = (JButton) ae.getSource();
        log.debug("button action {}", b.getName());
        Location l = locationManager.getLocationByName(b.getName());
        new TrainSwitchListCommentFrame(l);
    }

    @Override
    protected void comboBoxActionPerformed(ActionEvent ae) {
        log.debug("combo box action");
        enableSaveButton(true);
    }

    @Override
    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        Setup.getDefault().removePropertyChangeListener(this);
        for (Location location : locationManager.getLocationsByNameList()) {
            location.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) {
            changeLocationCheckboxes(e);
            enableChangeButtons();
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.SWITCHLIST_COMMENT_CHANGED_PROPERTY)) {
            updateLocationCheckboxes();
            enableChangeButtons();
        }
        if (e.getPropertyName().equals(Setup.SWITCH_LIST_CSV_PROPERTY_CHANGE)) {
            enableSaveButton(false);
            customPanel.setVisible(Setup.isGenerateCsvSwitchListEnabled());
        }
    }

    public static class TrainSwitchListCommentFrame extends OperationsFrame {

        // text area
        JTextArea commentTextArea = new JTextArea(10, 90);
        JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension minScrollerDim = new Dimension(1200, 500);

        // text color chooser
        JColorChooser commentColorChooser = new JColorChooser();

        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));

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
            addItem(pC, OperationsPanel.getColorChooserPanel(location.getSwitchListCommentWithColor(), commentColorChooser), 2, 0);
            JScrollPane panelPane = new JScrollPane(pC);

            commentTextArea.setText(TrainCommon.getTextColorString(location.getSwitchListCommentWithColor()));

            JPanel pB = new JPanel();
            pB.setLayout(new GridBagLayout());
            addItem(pB, cancelButton, 0, 0);
            addItem(pB, saveButton, 1, 0);

            getContentPane().add(panelPane);
            getContentPane().add(pB);

            addButtonAction(saveButton);
            addButtonAction(cancelButton);

            setTitle(location.getName());
            initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight200));
        }

        // Buttons
        @Override
        public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
            if (ae.getSource() == saveButton) {
                _location.setSwitchListComment(
                        TrainCommon.formatColorString(commentTextArea.getText(), commentColorChooser.getColor()));
                // save location file
                OperationsXml.save();
                if (Setup.isCloseWindowOnSaveEnabled()) {
                    super.dispose();
                }
            }
            if (ae.getSource() == cancelButton) {
                super.dispose();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainSwitchListEditFrame.class);
}

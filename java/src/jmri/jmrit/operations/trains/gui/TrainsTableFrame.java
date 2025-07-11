package jmri.jmrit.operations.trains.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.automation.gui.AutomationsTableFrameAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.backup.AutoSave;
import jmri.jmrit.operations.setup.gui.*;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.excel.SetupExcelProgramFrameAction;
import jmri.jmrit.operations.trains.excel.TrainCustomManifest;
import jmri.jmrit.operations.trains.schedules.*;
import jmri.jmrit.operations.trains.tools.*;
import jmri.swing.JTablePersistenceManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for adding and editing the train roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 *         2014
 */
public class TrainsTableFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    public static final String MOVE = Bundle.getMessage("Move");
    public static final String TERMINATE = Bundle.getMessage("Terminate");
    public static final String RESET = Bundle.getMessage("Reset");
    public static final String CONDUCTOR = Bundle.getMessage("Conductor");

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    public TrainsTableModel trainsModel;
    JTable trainsTable;
    JScrollPane trainsPane;

    // labels
    JLabel numTrains = new JLabel();
    JLabel textTrains = new JLabel(Bundle.getMessage("trains"));
    JLabel textSep1 = new JLabel("      ");

    // radio buttons
    JRadioButton showTime = new JRadioButton(Bundle.getMessage("Time"));
    JRadioButton showId = new JRadioButton(Bundle.getMessage("Id"));

    JRadioButton moveRB = new JRadioButton(MOVE);
    JRadioButton terminateRB = new JRadioButton(TERMINATE);
    JRadioButton resetRB = new JRadioButton(RESET);
    JRadioButton conductorRB = new JRadioButton(CONDUCTOR);

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("AddTrain"));
    JButton buildButton = new JButton(Bundle.getMessage("Build"));
    JButton printButton = new JButton(Bundle.getMessage("Print"));
    JButton openFileButton = new JButton(Bundle.getMessage("OpenFile"));
    JButton runFileButton = new JButton(Bundle.getMessage("RunFile"));
    JButton switchListsButton = new JButton(Bundle.getMessage("SwitchLists"));
    JButton terminateButton = new JButton(Bundle.getMessage("Terminate"));
    JButton saveButton = new JButton(Bundle.getMessage("SaveBuilds"));

    // check boxes
    JCheckBox buildMsgBox = new JCheckBox(Bundle.getMessage("BuildMessages"));
    JCheckBox buildReportBox = new JCheckBox(Bundle.getMessage("BuildReport"));
    JCheckBox printPreviewBox = new JCheckBox(Bundle.getMessage("Preview"));
    JCheckBox openFileBox = new JCheckBox(Bundle.getMessage("OpenFile"));
    JCheckBox runFileBox = new JCheckBox(Bundle.getMessage("RunFile"));
    public JCheckBox showAllBox = new JCheckBox(Bundle.getMessage("ShowAllTrains"));

    public TrainsTableFrame() {
        super();

        updateTitle();

        // create ShutDownTasks
        createShutDownTask();
        // always check for dirty operations files
        setModifiedFlag(true);

        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        trainsModel = new TrainsTableModel();
        trainsTable = new JTable(trainsModel);
        trainsPane = new JScrollPane(trainsTable);
        trainsModel.initTable(trainsTable, this);

        // Set up the control panel
        // row 1
        JPanel cp1 = new JPanel();
        cp1.setLayout(new BoxLayout(cp1, BoxLayout.X_AXIS));

        JPanel show = new JPanel();
        show.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ShowClickToSort")));
        show.add(showTime);
        show.add(showId);

        JPanel build = new JPanel();
        build.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Build")));
        build.add(showAllBox);

        JPanel function = new JPanel();
        function.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Function")));
        function.add(printPreviewBox);
        function.add(openFileBox);
        function.add(runFileBox);

        JPanel options = new JPanel();
        options.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        options.add(buildMsgBox);
        options.add(buildReportBox);

        JPanel action = new JPanel();
        action.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Action")));
        action.add(moveRB);
        action.add(conductorRB);
        action.add(terminateRB);
        action.add(resetRB);

        cp1.add(show);
        cp1.add(build);
        cp1.add(function);
        cp1.add(options);
        cp1.add(action);

        // tool tips, see setPrintButtonText() for more tool tips
        addButton.setToolTipText(Bundle.getMessage("AddTrainTip"));
        buildButton.setToolTipText(Bundle.getMessage("BuildSelectedTip"));
        switchListsButton.setToolTipText(Bundle.getMessage("PreviewPrintSwitchListsTip"));

        terminateButton.setToolTipText(Bundle.getMessage("TerminateSelectedTip"));
        saveButton.setToolTipText(Bundle.getMessage("SaveBuildsTip"));
        openFileButton.setToolTipText(Bundle.getMessage("OpenFileButtonTip"));
        runFileButton.setToolTipText(Bundle.getMessage("RunFileButtonTip"));
        buildMsgBox.setToolTipText(Bundle.getMessage("BuildMessagesTip"));
        printPreviewBox.setToolTipText(Bundle.getMessage("PreviewTip"));
        openFileBox.setToolTipText(Bundle.getMessage("OpenFileTip"));
        runFileBox.setToolTipText(Bundle.getMessage("RunFileTip"));
        showAllBox.setToolTipText(Bundle.getMessage("ShowAllTrainsTip"));

        moveRB.setToolTipText(Bundle.getMessage("MoveTip"));
        terminateRB.setToolTipText(Bundle.getMessage("TerminateTip"));
        resetRB.setToolTipText(Bundle.getMessage("ResetTip"));
        conductorRB.setToolTipText(Bundle.getMessage("ConductorTip"));

        // row 2
        JPanel addTrain = new JPanel();
        addTrain.setBorder(BorderFactory.createTitledBorder(""));
        addTrain.add(numTrains);
        addTrain.add(textTrains);
        addTrain.add(textSep1);
        addTrain.add(addButton);

        numTrains.setText(Integer.toString(trainManager.getNumEntries()));

        JPanel select = new JPanel();
        select.setBorder(BorderFactory.createTitledBorder(""));
        select.add(buildButton);
        select.add(printButton);
        select.add(openFileButton);
        select.add(runFileButton);
        select.add(switchListsButton);
        select.add(terminateButton);

        JPanel save = new JPanel();
        save.setBorder(BorderFactory.createTitledBorder(""));
        save.add(saveButton);

        JPanel cp2 = new JPanel();
        cp2.setLayout(new BoxLayout(cp2, BoxLayout.X_AXIS));
        cp2.add(addTrain);
        cp2.add(select);
        cp2.add(save);

        // place controls in scroll pane
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(cp1);
        controlPanel.add(cp2);

        JScrollPane controlPane = new JScrollPane(controlPanel);

        getContentPane().add(trainsPane);
        getContentPane().add(controlPane);

        // setup buttons
        addButtonAction(addButton);
        addButtonAction(buildButton);
        addButtonAction(printButton);
        addButtonAction(openFileButton);
        addButtonAction(runFileButton);
        addButtonAction(switchListsButton);
        addButtonAction(terminateButton);
        addButtonAction(saveButton);

        ButtonGroup showGroup = new ButtonGroup();
        showGroup.add(showTime);
        showGroup.add(showId);
        showTime.setSelected(true);

        ButtonGroup actionGroup = new ButtonGroup();
        actionGroup.add(moveRB);
        actionGroup.add(conductorRB);
        actionGroup.add(terminateRB);
        actionGroup.add(resetRB);

        addRadioButtonAction(showTime);
        addRadioButtonAction(showId);

        addRadioButtonAction(moveRB);
        addRadioButtonAction(terminateRB);
        addRadioButtonAction(resetRB);
        addRadioButtonAction(conductorRB);

        buildMsgBox.setSelected(trainManager.isBuildMessagesEnabled());
        buildReportBox.setSelected(trainManager.isBuildReportEnabled());
        printPreviewBox.setSelected(trainManager.isPrintPreviewEnabled());
        openFileBox.setSelected(trainManager.isOpenFileEnabled());
        runFileBox.setSelected(trainManager.isRunFileEnabled());
        showAllBox.setSelected(trainsModel.isShowAll());

        // show open files only if create csv is enabled
        updateRunAndOpenButtons();

        addCheckBoxAction(buildMsgBox);
        addCheckBoxAction(buildReportBox);
        addCheckBoxAction(printPreviewBox);
        addCheckBoxAction(showAllBox);
        addCheckBoxAction(openFileBox);
        addCheckBoxAction(runFileBox);

        // Set the button text to Print or Preview
        setPrintButtonText();
        // Set the train action button text to Move or Terminate
        setTrainActionButton();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new OptionAction());
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.addSeparator();
        toolMenu.add(new TrainsByCarTypeAction());
        toolMenu.add(new ChangeDepartureTimesAction());
        toolMenu.add(new TrainsScheduleAction());
        toolMenu.add(new TrainsTableSetColorAction());
        toolMenu.add(new TrainCopyAction());
        toolMenu.addSeparator();
        toolMenu.add(new TrainsScriptAction(this));
        toolMenu.add(new AutomationsTableFrameAction());
        toolMenu.add(new SetupExcelProgramFrameAction());
        toolMenu.addSeparator();
        toolMenu.add(new ExportTrainRosterAction());
        toolMenu.add(new ExportTimetableAction());
        toolMenu.add(new ExportTrainLineupsAction());
        toolMenu.addSeparator();
        toolMenu.add(new TrainByCarTypeAction(null));
        toolMenu.addSeparator();
        toolMenu.add(new PrintTrainsAction(false, this));
        toolMenu.add(new PrintTrainsAction(true, this));
        toolMenu.add(new PrintSavedTrainManifestAction(false, null));
        toolMenu.add(new PrintSavedTrainManifestAction(true, null));

        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight250));

        addHorizontalScrollBarKludgeFix(controlPane, controlPanel);

        // listen for train schedule changes
        InstanceManager.getDefault(TrainScheduleManager.class).addPropertyChangeListener(this);
        // listen for changes in the number of trains
        trainManager.addPropertyChangeListener(this);
        Setup.getDefault().addPropertyChangeListener(this);
        // listen for location switch list changes
        addPropertyChangeLocations();

        // auto save
        AutoSave.start();
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(trainsTable);
        if (ae.getSource() == showId) {
            trainsModel.setSort(trainsModel.SORTBYID);
        }
        if (ae.getSource() == showTime) {
            trainsModel.setSort(trainsModel.SORTBYTIME);
        }
        if (ae.getSource() == moveRB) {
            trainManager.setTrainsFrameTrainAction(MOVE);
        }
        if (ae.getSource() == terminateRB) {
            trainManager.setTrainsFrameTrainAction(TERMINATE);
        }
        if (ae.getSource() == resetRB) {
            trainManager.setTrainsFrameTrainAction(RESET);
        }
        if (ae.getSource() == conductorRB) {
            trainManager.setTrainsFrameTrainAction(CONDUCTOR);
        }
    }

    TrainSwitchListEditFrame tslef;

    // add, build, print, switch lists, terminate, and save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // log.debug("train button activated");
        if (ae.getSource() == addButton) {
            new TrainEditFrame(null);
        }
        if (ae.getSource() == buildButton) {
            runFileButton.setEnabled(false);
            // uses a thread which allows table updates during build
            trainManager.buildSelectedTrains(getSortByList());
        }
        if (ae.getSource() == printButton) {
            trainManager.printSelectedTrains(getSortByList());
        }
        if (ae.getSource() == openFileButton) {
            // open the csv files
            List<Train> trains = getSortByList();
            for (Train train : trains) {
                if (train.isBuildEnabled()) {
                    if (!train.isBuilt() && trainManager.isBuildMessagesEnabled()) {
                        int response = JmriJOptionPane.showConfirmDialog(this,
                                Bundle.getMessage("NeedToBuildBeforeOpenFile",
                                        train.getName()),
                                Bundle.getMessage("ErrorTitle"), JmriJOptionPane.OK_CANCEL_OPTION);
                        if (response != JmriJOptionPane.OK_OPTION ) {
                            break;
                        }
                    } else if (train.isBuilt()) {
                        train.openFile();
                    }
                }
            }
        }
        if (ae.getSource() == runFileButton) {
            // Processes the CSV Manifest files using an external custom program.
            TrainCustomManifest tcm = InstanceManager.getDefault(TrainCustomManifest.class);
            if (!tcm.excelFileExists()) {
                log.warn("Manifest creator file not found!, directory path: {}, file name: {}", tcm.getDirectoryPathName(),
                        tcm.getFileName());
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("LoadDirectoryNameFileName",
                                tcm.getDirectoryPathName(), tcm.getFileName()),
                        Bundle.getMessage("ManifestCreatorNotFound"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            List<Train> trains = getSortByList();
            for (Train train : trains) {
                if (train.isBuildEnabled()) {
                    if (!train.isBuilt() && trainManager.isBuildMessagesEnabled()) {
                        int response = JmriJOptionPane.showConfirmDialog(this,
                                Bundle.getMessage("NeedToBuildBeforeRunFile",
                                        train.getName()),
                                Bundle.getMessage("ErrorTitle"), JmriJOptionPane.OK_CANCEL_OPTION);
                        if (response != JmriJOptionPane.OK_OPTION ) {
                            break;
                        }
                    } else if (train.isBuilt()) {
                        // Add csv manifest file to our collection to be processed.
                        tcm.addCsvFile(train.createCsvManifestFile());
                        train.setPrinted(true);
                    }
                }
            }
            // Now run the user specified custom Manifest processor program
            tcm.process();
        }
        if (ae.getSource() == switchListsButton) {
            if (tslef != null) {
                tslef.dispose();
            }
            tslef = new TrainSwitchListEditFrame();
            tslef.initComponents();
        }
        if (ae.getSource() == terminateButton) {
            trainManager.terminateSelectedTrains(getSortByList());
        }
        if (ae.getSource() == saveButton) {
            storeValues();
        }
    }

    SortOrder _status = SortOrder.ASCENDING;

    public String getSortBy() {
        // set the defaults
        String sortBy = TrainsTableModel.TIMECOLUMNNAME;
        _status = SortOrder.ASCENDING;
        // now look to see if a sort is active
        for (RowSorter.SortKey key : trainsTable.getRowSorter().getSortKeys()) {
            String name = trainsModel.getColumnName(key.getColumn());
            SortOrder status = key.getSortOrder();
            // log.debug("Column {} status {}", name, status);
            if (!status.equals(SortOrder.UNSORTED) && !name.isEmpty()) {
                sortBy = name;
                _status = status;
                break;
            }
        }
        return sortBy;
    }

    public List<Train> getSortByList() {
        return getSortByList(getSortBy());
    }

    public List<Train> getSortByList(String sortBy) {
        List<Train> sysList;

        if (sortBy.equals(TrainsTableModel.IDCOLUMNNAME)) {
            sysList = trainManager.getTrainsByIdList();
        } else if (sortBy.equals(TrainsTableModel.TIMECOLUMNNAME)) {
            sysList = trainManager.getTrainsByTimeList();
        } else if (sortBy.equals(TrainsTableModel.DEPARTSCOLUMNNAME)) {
            sysList = trainManager.getTrainsByDepartureList();
        } else if (sortBy.equals(TrainsTableModel.TERMINATESCOLUMNNAME)) {
            sysList = trainManager.getTrainsByTerminatesList();
        } else if (sortBy.equals(TrainsTableModel.ROUTECOLUMNNAME)) {
            sysList = trainManager.getTrainsByRouteList();
        } else if (sortBy.equals(TrainsTableModel.STATUSCOLUMNNAME)) {
            sysList = trainManager.getTrainsByStatusList();
        } else if (sortBy.equals(TrainsTableModel.DESCRIPTIONCOLUMNNAME)) {
            sysList = trainManager.getTrainsByDescriptionList();
        } else {
            sysList = trainManager.getTrainsByNameList();
        }
        return sysList;
    }

    // Modifies button text and tool tips
    private void setPrintButtonText() {
        if (printPreviewBox.isSelected()) {
            printButton.setText(Bundle.getMessage("Preview"));
            printButton.setToolTipText(Bundle.getMessage("PreviewSelectedTip"));
            buildReportBox.setToolTipText(Bundle.getMessage("BuildReportPreviewTip"));
        } else {
            printButton.setText(Bundle.getMessage("Print"));
            printButton.setToolTipText(Bundle.getMessage("PrintSelectedTip"));
            buildReportBox.setToolTipText(Bundle.getMessage("BuildReportPrintTip"));
        }
    }

    private void setTrainActionButton() {
        moveRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE));
        terminateRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.TERMINATE));
        resetRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.RESET));
        conductorRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.CONDUCTOR));
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == buildMsgBox) {
            trainManager.setBuildMessagesEnabled(buildMsgBox.isSelected());
        }
        if (ae.getSource() == buildReportBox) {
            trainManager.setBuildReportEnabled(buildReportBox.isSelected());
        }
        if (ae.getSource() == printPreviewBox) {
            trainManager.setPrintPreviewEnabled(printPreviewBox.isSelected());
            setPrintButtonText(); // set the button text for Print or Preview
        }
        if (ae.getSource() == openFileBox) {
            trainManager.setOpenFileEnabled(openFileBox.isSelected());
            runFileBox.setSelected(false);
            trainManager.setRunFileEnabled(false);
        }
        if (ae.getSource() == runFileBox) {
            trainManager.setRunFileEnabled(runFileBox.isSelected());
            openFileBox.setSelected(false);
            trainManager.setOpenFileEnabled(false);
        }
        if (ae.getSource() == showAllBox) {
            trainsModel.setShowAll(showAllBox.isSelected());
        }
    }

    private void updateTitle() {
        String title = Bundle.getMessage("TitleTrainsTable");
        TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule();
        if (sch != null) {
            title = title + " " + sch.getName();
        }
        setTitle(title);
    }

    private void updateSwitchListButton() {
        List<Location> locations = locationManager.getList();
        for (Location location : locations) {
            if (location != null && location.isSwitchListEnabled() && location.getStatus().equals(Location.MODIFIED)) {
                switchListsButton.setBackground(Color.RED);
                return;
            }
        }
        switchListsButton.setBackground(Color.GREEN);
    }

    // show open files only if create csv is enabled
    private void updateRunAndOpenButtons() {
        openFileBox.setVisible(Setup.isGenerateCsvManifestEnabled());
        openFileButton.setVisible(Setup.isGenerateCsvManifestEnabled());
        runFileBox.setVisible(Setup.isGenerateCsvManifestEnabled());
        runFileButton.setVisible(Setup.isGenerateCsvManifestEnabled());
    }

    private synchronized void addPropertyChangeLocations() {
        List<Location> locations = locationManager.getList();
        for (Location location : locations) {
            location.addPropertyChangeListener(this);
        }
    }

    private synchronized void removePropertyChangeLocations() {
        List<Location> locations = locationManager.getList();
        for (Location location : locations) {
            location.removePropertyChangeListener(this);
        }
    }

    @Override
    public void dispose() {
        trainsModel.dispose();
        trainManager.runShutDownScripts();
        trainManager.removePropertyChangeListener(this);
        InstanceManager.getDefault(TrainScheduleManager.class).removePropertyChangeListener(this);
        Setup.getDefault().removePropertyChangeListener(this);
        removePropertyChangeLocations();
        setModifiedFlag(false);
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(trainsTable);
        });
        super.dispose();
    }

    @Override
    protected void handleModified() {
        if (!getModifiedFlag()) {
            return;
        }
        if (Setup.isAutoSaveEnabled()) {
            storeValues();
            return;
        }
        if (OperationsXml.areFilesDirty()) {
            int result = JmriJOptionPane.showOptionDialog(this, Bundle.getMessage("PromptQuitWindowNotWritten"),
                    Bundle.getMessage("PromptSaveQuit"), JmriJOptionPane.YES_NO_OPTION,
                    JmriJOptionPane.WARNING_MESSAGE, null,
                    new String[] { ResourceBundle.getBundle("jmri.util.UtilBundle").getString("WarnYesSave"), // NOI18N
                            ResourceBundle.getBundle("jmri.util.UtilBundle").getString("WarnNoClose") }, // NOI18N
                    ResourceBundle.getBundle("jmri.util.UtilBundle").getString("WarnYesSave"));
            if (result == JmriJOptionPane.YES_OPTION) {
                // user wants to save
                storeValues();
            }
        }
    }

    @Override
    protected void storeValues() {
        super.storeValues();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(TrainScheduleManager.SCHEDULE_ID_CHANGED_PROPERTY)) {
            updateTitle();
        }
        if (e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) {
            log.debug("update switch list button location ({})", e.getSource());
            updateSwitchListButton();
        }
        if (e.getPropertyName().equals(Setup.MANIFEST_CSV_PROPERTY_CHANGE)) {
            updateRunAndOpenButtons();
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            numTrains.setText(Integer.toString(trainManager.getNumEntries()));
        }
        if (e.getPropertyName().equals(TrainManager.TRAINS_BUILT_CHANGED_PROPERTY)) {
            runFileButton.setEnabled(true);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainsTableFrame.class);
}

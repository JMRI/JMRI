// TrainsScheduleTableFrame.java
package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing train schedules (Timetable) for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010, 2012
 * @version $Revision$
 */
public class TrainsScheduleTableFrame extends OperationsFrame implements PropertyChangeListener {

    // public static SwingShutDownTask trainDirtyTask;

    public static final String NAME = Bundle.getMessage("Name"); // Sort by choices
    public static final String TIME = Bundle.getMessage("Time");

    TrainManager trainManager = TrainManager.instance();
    TrainScheduleManager trainScheduleManager = TrainScheduleManager.instance();
    LocationManager locationManager = LocationManager.instance();

    TrainsScheduleTableModel trainsScheduleModel = new TrainsScheduleTableModel();
    javax.swing.JTable trainsScheduleTable = new javax.swing.JTable(trainsScheduleModel);
    JScrollPane trainsPane;

    // labels
    JLabel textSort = new JLabel(Bundle.getMessage("SortBy"));

    // radio buttons
    JRadioButton sortByName = new JRadioButton(NAME);
    JRadioButton sortByTime = new JRadioButton(TIME);

    JRadioButton noneButton = new JRadioButton(Bundle.getMessage("None"));

    // radio button groups
    ButtonGroup schGroup = new ButtonGroup();

    // major buttons
    JButton selectButton = new JButton(Bundle.getMessage("Select"));
    JButton clearButton = new JButton(Bundle.getMessage("Clear"));

    JButton applyButton = new JButton(Bundle.getMessage("Apply"));
    JButton buildButton = new JButton(Bundle.getMessage("Build"));
    JButton printButton = new JButton(Bundle.getMessage("Print"));
    JButton switchListsButton = new JButton();
    JButton terminateButton = new JButton(Bundle.getMessage("Terminate"));

    JButton activateButton = new JButton(Bundle.getMessage("Activate"));
    JButton saveButton = new JButton(Bundle.getMessage("Save"));

    // check boxes
    // panel
    JPanel schedule = new JPanel();
    
    // text area
    JTextArea commentTextArea = new JTextArea(2, 70);
    JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    public TrainsScheduleTableFrame() {

        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        trainsPane = new JScrollPane(trainsScheduleTable);
        trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        trainsScheduleModel.initTable(trainsScheduleTable, this);
        
        // row comment
        JPanel pC = new JPanel();
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        pC.setLayout(new GridBagLayout());
        addItem(pC, commentScroller, 1, 0);
        
        // adjust text area width based on window size
        adjustTextAreaColumnWidth(commentScroller, commentTextArea);

        // Set up the control panel
        // row 1
        JPanel cp1 = new JPanel();
        cp1.setLayout(new BoxLayout(cp1, BoxLayout.X_AXIS));

        // row 1
        JPanel sortBy = new JPanel();
        sortBy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));
        sortBy.add(sortByTime);
        sortBy.add(sortByName);

        // row 2
        schedule.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Active")));
        updateControlPanel();

        cp1.add(sortBy);
        cp1.add(schedule);

        JPanel pButtons = new JPanel();
        pButtons.setLayout(new BoxLayout(pButtons, BoxLayout.X_AXIS));

        JPanel cp3 = new JPanel();
        cp3.setBorder(BorderFactory.createTitledBorder(""));
        cp3.add(clearButton);
        cp3.add(selectButton);

        JPanel cp4 = new JPanel();
        cp4.setBorder(BorderFactory.createTitledBorder(""));
        cp4.add(applyButton);
        cp4.add(buildButton);
        cp4.add(printButton);
        cp4.add(switchListsButton);
        cp4.add(terminateButton);

        JPanel cp5 = new JPanel();
        cp5.setBorder(BorderFactory.createTitledBorder(""));
        cp5.add(activateButton);
        cp5.add(saveButton);

        pButtons.add(cp3);
        pButtons.add(cp4);
        pButtons.add(cp5);

        // tool tips
        selectButton.setToolTipText(Bundle.getMessage("SelectAllButtonTip"));
        clearButton.setToolTipText(Bundle.getMessage("ClearAllButtonTip"));
        applyButton.setToolTipText(Bundle.getMessage("ApplyButtonTip"));
        buildButton.setToolTipText(Bundle.getMessage("BuildSelectedTip"));
        activateButton.setToolTipText(Bundle.getMessage("ActivateButtonTip"));
        terminateButton.setToolTipText(Bundle.getMessage("TerminateSelectedTip"));

        setPrintButtonText();
        setSwitchListButtonText();

        // place controls in scroll pane
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(pC);
        controlPanel.add(cp1);
        controlPanel.add(pButtons);

        JScrollPane controlPane = new JScrollPane(controlPanel);
        // make sure control panel is the right size
        controlPane.setMinimumSize(new Dimension(500, 480));
        controlPane.setMaximumSize(new Dimension(2000, 500));
        controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        getContentPane().add(trainsPane);
        getContentPane().add(controlPane);

        // setup buttons
        addButtonAction(clearButton);
        addButtonAction(selectButton);
        addButtonAction(applyButton);
        addButtonAction(buildButton);
        addButtonAction(printButton);
        addButtonAction(switchListsButton);
        addButtonAction(terminateButton);
        addButtonAction(activateButton);
        addButtonAction(saveButton);

        ButtonGroup sortGroup = new ButtonGroup();
        sortGroup.add(sortByTime);
        sortGroup.add(sortByName);
        sortByTime.setSelected(true);

        addRadioButtonAction(sortByTime);
        addRadioButtonAction(sortByName);

        addRadioButtonAction(noneButton);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
        toolMenu.add(new TrainsScheduleEditAction());
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Timetable", true); // NOI18N

        setTitle(Bundle.getMessage("TitleTimeTableTrains"));

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight500));

        addHorizontalScrollBarKludgeFix(controlPane, controlPanel);

        Setup.addPropertyChangeListener(this);
        trainManager.addPropertyChangeListener(this);
        trainScheduleManager.addPropertyChangeListener(this);
        addPropertyChangeLocations();
        addPropertyChangeTrainSchedules();
    }

    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (ae.getSource() == sortByName) {
            trainsScheduleModel.setSort(trainsScheduleModel.SORTBYNAME);
        } else if (ae.getSource() == sortByTime) {
            trainsScheduleModel.setSort(trainsScheduleModel.SORTBYTIME);
        } else if (ae.getSource() == noneButton) {
            enableButtons(false);
            commentTextArea.setText(""); // no text for the noneButton
            // must be one of the schedule radio buttons
        } else {
            enableButtons(true);
            // update comment field
            TrainSchedule ts = trainScheduleManager.getScheduleById(getSelectedScheduleId());
            commentTextArea.setText(ts.getComment());
        }
    }

    // add, build, print, switch lists, terminate, and save buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("button activated");
        if (ae.getSource() == clearButton) {
            updateCheckboxes(false);
        }
        if (ae.getSource() == selectButton) {
            updateCheckboxes(true);
        }
        if (ae.getSource() == applyButton) {
            applySchedule();
        }
        if (ae.getSource() == buildButton) {
            switchListsButton.setEnabled(false);
            // uses a thread which allows table updates during build
            trainManager.buildSelectedTrains(getSortByList());
        }
        if (ae.getSource() == printButton) {
            trainManager.printSelectedTrains(getSortByList());
        }
        if (ae.getSource() == switchListsButton) {
            trainScheduleManager.buildSwitchLists();
        }
        if (ae.getSource() == terminateButton) {
            trainManager.terminateSelectedTrains(getSortByList());
        }
        if (ae.getSource() == activateButton) {
            trainManager.setTrainSecheduleActiveId(getSelectedScheduleId());
            activateButton.setEnabled(false);
        }
        if (ae.getSource() == saveButton) {
            storeValues();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    /*
     * Update radio button names in the same order as the table
     */
    private void updateControlPanel() {
        schedule.removeAll();
        noneButton.setName(""); // Name holds schedule id for the selected radio button
        noneButton.setSelected(true);
        commentTextArea.setText(""); // no text for the noneButton
        enableButtons(false);
        schedule.add(noneButton);
        schGroup.add(noneButton);

        for (int i = trainsScheduleModel.getFixedColumn(); i < trainsScheduleModel.getColumnCount(); i++) {
            log.debug("Column name: {}", trainsScheduleTable.getColumnName(i));
            TrainSchedule ts = trainScheduleManager.getScheduleByName(trainsScheduleTable.getColumnName(i));
            if (ts != null) {
                JRadioButton b = new JRadioButton();
                b.setText(ts.getName());
                b.setName(ts.getId());
                schedule.add(b);
                schGroup.add(b);
                addRadioButtonAction(b);
                if (b.getName().equals(trainManager.getTrainScheduleActiveId())) {
                    b.setSelected(true);
                    enableButtons(true);
                    // update comment field
                    commentTextArea.setText(ts.getComment());
                }
            }
        }
        schedule.revalidate();
    }

    private void updateCheckboxes(boolean selected) {
        TrainSchedule ts = trainScheduleManager.getScheduleById(getSelectedScheduleId());
        if (ts != null) {
            for (Train train : trainManager.getTrainsByIdList()) {
                if (selected) {
                    ts.addTrainId(train.getId());
                } else {
                    ts.removeTrainId(train.getId());
                }
            }
        }
    }

    private void applySchedule() {
        TrainSchedule ts = trainScheduleManager.getScheduleById(getSelectedScheduleId());
        if (ts != null) {
            for (Train train : trainManager.getTrainsByIdList()) {
                train.setBuildEnabled(ts.containsTrainId(train.getId()));
            }
        }
    }

    private String getSelectedScheduleId() {
        AbstractButton b;
        Enumeration<AbstractButton> en = schGroup.getElements();
        while (en.hasMoreElements()) {
            b = en.nextElement();
            if (b.isSelected()) {
                log.debug("schedule radio button " + b.getText());
                return b.getName();
            }
        }
        return null;
    }

    private void enableButtons(boolean enable) {
        selectButton.setEnabled(enable);
        clearButton.setEnabled(enable);
        applyButton.setEnabled(enable);
        buildButton.setEnabled(enable);
        printButton.setEnabled(enable);
        switchListsButton.setEnabled(enable);
        terminateButton.setEnabled(enable);

        log.debug("Selected id: {}, Active id: {}", getSelectedScheduleId(), trainManager.getTrainScheduleActiveId());

        activateButton.setEnabled(getSelectedScheduleId() != null
                && !getSelectedScheduleId().equals(trainManager.getTrainScheduleActiveId()));
        
        commentTextArea.setEnabled(enable);
    }

    private List<Train> getSortByList() {
        if (sortByTime.isSelected()) {
            return trainManager.getTrainsByTimeList();
        } else {
            return trainManager.getTrainsByNameList();
        }
    }

    private void setSwitchListButtonText() {
        if (!Setup.isSwitchListRealTime()) {
            switchListsButton.setText(Bundle.getMessage("Update"));
        } else if (trainManager.isPrintPreviewEnabled()) {
            switchListsButton.setText(Bundle.getMessage("PreviewSwitchLists"));
        } else {
            switchListsButton.setText(Bundle.getMessage("PrintSwitchLists"));
        }
    }

    // Modifies button text and tool tips
    private void setPrintButtonText() {
        if (trainManager.isPrintPreviewEnabled()) {
            printButton.setText(Bundle.getMessage("Preview"));
            printButton.setToolTipText(Bundle.getMessage("PreviewSelectedTip"));
        } else {
            printButton.setText(Bundle.getMessage("Print"));
            printButton.setToolTipText(Bundle.getMessage("PrintSelectedTip"));
        }
    }

//    private void buildSwitchList() {
//        TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
//        for (Location location : locationManager.getLocationsByNameList()) {
//            if (location.isSwitchListEnabled()) {
//                trainSwitchLists.buildSwitchList(location);
//                // // print or print changes
//                if (Setup.isSwitchListRealTime() && !location.getStatus().equals(Location.PRINTED)) {
//                    trainSwitchLists.printSwitchList(location, trainManager.isPrintPreviewEnabled());
//                }
//            }
//        }
//        // set trains switch lists printed
//        trainManager.setTrainsSwitchListStatus(Train.PRINTED);
//    }

    private void updateSwitchListButton() {
        log.debug("update switch list button");
        List<Location> locations = locationManager.getList();
        for (Location location : locations) {
            if (location != null && location.isSwitchListEnabled() && location.getStatus().equals(Location.MODIFIED)) {
                switchListsButton.setBackground(Color.RED);
                return;
            }
        }
        switchListsButton.setBackground(Color.GREEN);
    }

    protected void storeValues() {
        // Save comment
        TrainSchedule ts = trainScheduleManager.getScheduleById(getSelectedScheduleId());
        if (ts != null) {
            ts.setComment(commentTextArea.getText());
        }
//        updateControlPanel();
        saveTableDetails(trainsScheduleTable);
        OperationsXml.save();
    }

    public void dispose() {
        Setup.removePropertyChangeListener(this);
        trainManager.removePropertyChangeListener(this);
        trainScheduleManager.removePropertyChangeListener(this);
        removePropertyChangeTrainSchedules();
        removePropertyChangeLocations();
        trainsScheduleModel.dispose();
        super.dispose();
    }

    private void addPropertyChangeLocations() {
        for (Location location : locationManager.getList()) {
            location.addPropertyChangeListener(this);
        }
    }

    private void removePropertyChangeLocations() {
        for (Location location : locationManager.getList()) {
            location.removePropertyChangeListener(this);
        }
    }

    private void addPropertyChangeTrainSchedules() {
        List<TrainSchedule> trainSchedules = trainScheduleManager.getSchedulesByIdList();
        for (TrainSchedule ts : trainSchedules) {
            ts.addPropertyChangeListener(this);
        }
    }

    private void removePropertyChangeTrainSchedules() {
        List<TrainSchedule> trainSchedules = trainScheduleManager.getSchedulesByIdList();
        for (TrainSchedule ts : trainSchedules) {
            ts.removePropertyChangeListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (Control.showProperty)
            log.debug("Property change {} old: {} new: {}", e.getPropertyName(), e.getOldValue(), e.getNewValue());
        if (e.getPropertyName().equals(TrainScheduleManager.LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(TrainSchedule.NAME_CHANGED_PROPERTY)) {
            updateControlPanel();
        }
        if (e.getPropertyName().equals(TrainManager.PRINTPREVIEW_CHANGED_PROPERTY)) {
            setPrintButtonText();
            setSwitchListButtonText();
        }
        if (e.getPropertyName().equals(TrainManager.TRAINS_BUILT_CHANGED_PROPERTY)) {
            switchListsButton.setEnabled(true);
        }
        if (e.getPropertyName().equals(Setup.REAL_TIME_PROPERTY_CHANGE)) {
            setSwitchListButtonText();
        }
        if (e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) {
            updateSwitchListButton();
        }
    }

    static Logger log = LoggerFactory.getLogger(TrainsScheduleTableFrame.class.getName());
}

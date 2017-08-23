package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainSwitchListText;

/**
 * Frame for user edit of switch list text strings
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class EditSwitchListTextPanel extends OperationsPreferencesPanel {

//    private static final Logger log = LoggerFactory.getLogger(EditSwitchListTextPanel.class);

    protected static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton resetButton = new JButton(rb.getString("Reset"));

    // text fields
    JTextField switchListForTextField = new JTextField(60);
    JTextField scheduledWorkTextField = new JTextField(60);

    JTextField departsAtTextField = new JTextField(60);
    JTextField departsAtExpectedArrivalTextField = new JTextField(60);
    JTextField departedExpectedTextField = new JTextField(60);

    JTextField visitNumberTextField = new JTextField(60);
    JTextField visitNumberDepartedTextField = new JTextField(60);
    JTextField visitNumberTerminatesTextField = new JTextField(60);
    JTextField visitNumberTerminatesDepartedTextField = new JTextField(60);
    JTextField visitNumberDoneTextField = new JTextField(60);

    JTextField trainDirectionChangeTextField = new JTextField(60);
    JTextField noCarPickUpsTextField = new JTextField(60);
    JTextField noCarDropsTextField = new JTextField(60);
    JTextField trainDoneTextField = new JTextField(60);
    JTextField trainDepartsCarsTextField = new JTextField(60);
    JTextField trainDepartsLoadsTextField = new JTextField(60);
    
    JTextField switchListByTrackTextField = new JTextField(60);
    JTextField holdCarTextField = new JTextField(60);

    public EditSwitchListTextPanel() {

        // the following code sets the frame's initial state
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // manifest text fields
        JPanel pSwitchList = new JPanel();
        JScrollPane pSwitchListPane = new JScrollPane(pSwitchList);
        pSwitchListPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchList")));
        pSwitchList.setLayout(new BoxLayout(pSwitchList, BoxLayout.Y_AXIS));

        JPanel pSwitchListForTextField = new JPanel();
        pSwitchListForTextField.setBorder(BorderFactory.createTitledBorder(rb
                .getString("SwitchListFor")));
        pSwitchListForTextField.add(switchListForTextField);
        switchListForTextField.setText(TrainSwitchListText.getStringSwitchListFor());
        switchListForTextField.setToolTipText(rb.getString("ToolTipSwitchListFor"));
        pSwitchList.add(pSwitchListForTextField);

        JPanel pScheduledWorkTextField = new JPanel();
        pScheduledWorkTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduledWork")));
        pScheduledWorkTextField.add(scheduledWorkTextField);
        scheduledWorkTextField.setText(TrainSwitchListText.getStringScheduledWork());
        scheduledWorkTextField.setToolTipText(rb.getString("ToolTipManifestForTrain"));
        pSwitchList.add(pScheduledWorkTextField);

        JPanel pDepartsAtTextField = new JPanel();
        pDepartsAtTextField.setBorder(BorderFactory.createTitledBorder(rb
                .getString("DepartsAt")));
        pDepartsAtTextField.add(departsAtTextField);
        departsAtTextField.setText(TrainSwitchListText.getStringDepartsAt());
        departsAtTextField.setToolTipText(rb.getString("ToolTipDepartsAt"));
        pSwitchList.add(pDepartsAtTextField);

        JPanel pDepartsAtExpectedArrivalTextField = new JPanel();
        pDepartsAtExpectedArrivalTextField
                .setBorder(BorderFactory.createTitledBorder(rb.getString("DepartsAtExpectedArrival")));
        pDepartsAtExpectedArrivalTextField.add(departsAtExpectedArrivalTextField);
        departsAtExpectedArrivalTextField.setText(TrainSwitchListText.getStringDepartsAtExpectedArrival());
        departsAtExpectedArrivalTextField.setToolTipText(rb.getString("ToolTipArrives"));
        pSwitchList.add(pDepartsAtExpectedArrivalTextField);

        JPanel pDepartedExpectedTextField = new JPanel();
        pDepartedExpectedTextField.setBorder(BorderFactory.createTitledBorder(rb
                .getString("DepartedExpected")));
        pDepartedExpectedTextField.add(departedExpectedTextField);
        departedExpectedTextField.setText(TrainSwitchListText.getStringDepartedExpected());
        departedExpectedTextField.setToolTipText(rb.getString("ToolTipDeparted"));
        pSwitchList.add(pDepartedExpectedTextField);

        JPanel pVisitNumber = new JPanel();
        pVisitNumber.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumber")));
        pVisitNumber.add(visitNumberTextField);
        visitNumberTextField.setText(TrainSwitchListText.getStringVisitNumber());
        visitNumberTextField.setToolTipText(rb.getString("ToolTipVisitNumber"));
        pSwitchList.add(pVisitNumber);

        JPanel pVisitNumberDeparted = new JPanel();
        pVisitNumberDeparted.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumberDeparted")));
        pVisitNumberDeparted.add(visitNumberDepartedTextField);
        visitNumberDepartedTextField.setText(TrainSwitchListText.getStringVisitNumberDeparted());
        visitNumberDepartedTextField.setToolTipText(rb.getString("ToolTipVisitNumber"));
        pSwitchList.add(pVisitNumberDeparted);

        JPanel pVisitNumberTerminates = new JPanel();
        pVisitNumberTerminates.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumberTerminates")));
        pVisitNumberTerminates.add(visitNumberTerminatesTextField);
        visitNumberTerminatesTextField.setText(TrainSwitchListText.getStringVisitNumberTerminates());
        visitNumberTerminatesTextField.setToolTipText(rb.getString("ToolTipVisitNumberTerminates"));
        pSwitchList.add(pVisitNumberTerminates);

        JPanel pVisitNumberTerminatesDepartedTextField = new JPanel();
        pVisitNumberTerminatesDepartedTextField.setBorder(BorderFactory.createTitledBorder(rb
                .getString("VisitNumberTerminatesDeparted")));
        pVisitNumberTerminatesDepartedTextField.add(visitNumberTerminatesDepartedTextField);
        visitNumberTerminatesDepartedTextField.setText(TrainSwitchListText.getStringVisitNumberTerminatesDeparted());
        visitNumberTerminatesDepartedTextField.setToolTipText(rb.getString("ToolTipVisitNumberTerminates"));
        pSwitchList.add(pVisitNumberTerminatesDepartedTextField);

        JPanel pVisitNumberDone = new JPanel();
        pVisitNumberDone.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumberDone")));
        pVisitNumberDone.add(visitNumberDoneTextField);
        visitNumberDoneTextField.setText(TrainSwitchListText.getStringVisitNumberDone());
        visitNumberDoneTextField.setToolTipText(rb.getString("ToolTipVisitNumberDone"));
        pSwitchList.add(pVisitNumberDone);

        JPanel pTrainDirectionChange = new JPanel();
        pTrainDirectionChange.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDirectionChange")));
        pTrainDirectionChange.add(trainDirectionChangeTextField);
        trainDirectionChangeTextField.setText(TrainSwitchListText.getStringTrainDirectionChange());
        trainDirectionChangeTextField.setToolTipText(rb.getString("ToolTipDirectionChange"));
        pSwitchList.add(pTrainDirectionChange);

        JPanel pNoCarPickUps = new JPanel();
        pNoCarPickUps.setBorder(BorderFactory.createTitledBorder(rb.getString("NoCarPickUps")));
        pNoCarPickUps.add(noCarPickUpsTextField);
        noCarPickUpsTextField.setText(TrainSwitchListText.getStringNoCarPickUps());
        noCarPickUpsTextField.setToolTipText(rb.getString("ToolTipTrainDone"));
        pSwitchList.add(pNoCarPickUps);

        JPanel pNoCarDrops = new JPanel();
        pNoCarDrops.setBorder(BorderFactory.createTitledBorder(rb.getString("NoCarDrops")));
        pNoCarDrops.add(noCarDropsTextField);
        noCarDropsTextField.setText(TrainSwitchListText.getStringNoCarDrops());
        noCarDropsTextField.setToolTipText(rb.getString("ToolTipTrainDone"));
        pSwitchList.add(pNoCarDrops);

        JPanel pTrainDone = new JPanel();
        pTrainDone.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDone")));
        pTrainDone.add(trainDoneTextField);
        trainDoneTextField.setText(TrainSwitchListText.getStringTrainDone());
        trainDoneTextField.setToolTipText(rb.getString("ToolTipTrainDone"));
        pSwitchList.add(pTrainDone);

        JPanel pTrainDepartsCars = new JPanel();
        pTrainDepartsCars.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDepartsCars")));
        pTrainDepartsCars.add(trainDepartsCarsTextField);
        trainDepartsCarsTextField.setText(TrainSwitchListText.getStringTrainDepartsCars());
        trainDepartsCarsTextField.setToolTipText(rb.getString("ToolTipTrainDepartsCars"));
        pSwitchList.add(pTrainDepartsCars);

        JPanel pTrainDepartsLoadsTextField = new JPanel();
        pTrainDepartsLoadsTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDepartsLoads")));
        pTrainDepartsLoadsTextField.add(trainDepartsLoadsTextField);
        trainDepartsLoadsTextField.setText(TrainSwitchListText.getStringTrainDepartsLoads());
        trainDepartsLoadsTextField.setToolTipText(rb.getString("ToolTipTrainDepartsLoads"));
        pSwitchList.add(pTrainDepartsLoadsTextField);
        
        JPanel pSwitchListByTrackTextField = new JPanel();
        pSwitchListByTrackTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("SwitchListByTrack")));
        pSwitchListByTrackTextField.add(switchListByTrackTextField);
        switchListByTrackTextField.setText(TrainSwitchListText.getStringSwitchListByTrack());
        switchListByTrackTextField.setToolTipText(rb.getString("ToolTipSwitchListFor"));
        pSwitchList.add(pSwitchListByTrackTextField);
        
        JPanel pHoldCarTextField = new JPanel();
        pHoldCarTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("HoldCar")));
        pHoldCarTextField.add(holdCarTextField);
        holdCarTextField.setText(TrainSwitchListText.getStringHoldCar());
        holdCarTextField.setToolTipText(rb.getString("ToolTipHoldCar"));
        pSwitchList.add(pHoldCarTextField);

        // add tool tips
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));

        // row 11
        JPanel pControl = new JPanel();
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, resetButton, 0, 0);
        addItem(pControl, saveButton, 1, 0);

        add(pSwitchListPane);
        add(pControl);

        // setup buttons
        addButtonAction(resetButton);
        addButtonAction(saveButton);

        initMinimumSize();
    }

    // Save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == resetButton) {
            switchListForTextField.setText(rb.getString("SwitchListFor"));
            scheduledWorkTextField.setText(rb.getString("ScheduledWork"));

            departsAtTextField.setText(rb.getString("DepartsAt"));
            departsAtExpectedArrivalTextField.setText(rb.getString("DepartsAtExpectedArrival"));
            departedExpectedTextField.setText(rb.getString("DepartedExpected"));

            visitNumberTextField.setText(rb.getString("VisitNumber"));
            visitNumberDepartedTextField.setText(rb.getString("VisitNumberDeparted"));
            visitNumberTerminatesTextField.setText(rb.getString("VisitNumberTerminates"));
            visitNumberTerminatesDepartedTextField.setText(rb.getString("VisitNumberTerminatesDeparted"));
            visitNumberDoneTextField.setText(rb.getString("VisitNumberDone"));

            trainDirectionChangeTextField.setText(rb.getString("TrainDirectionChange"));
            noCarPickUpsTextField.setText(rb.getString("NoCarPickUps"));
            noCarDropsTextField.setText(rb.getString("NoCarDrops"));
            trainDoneTextField.setText(rb.getString("TrainDone"));
            trainDepartsCarsTextField.setText(rb.getString("TrainDepartsCars"));
            trainDepartsLoadsTextField.setText(rb.getString("TrainDepartsLoads"));
            
            switchListByTrackTextField.setText(rb.getString("SwitchListByTrack"));
            holdCarTextField.setText(rb.getString("HoldCar"));
        }
        if (ae.getSource() == saveButton) {
            this.savePreferences();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitleSwitchListText");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        TrainSwitchListText.setStringSwitchListFor(switchListForTextField.getText());
        TrainSwitchListText.setStringScheduledWork(scheduledWorkTextField.getText());

        TrainSwitchListText.setStringDepartsAt(departsAtTextField.getText());
        TrainSwitchListText.setStringDepartsAtExpectedArrival(departsAtExpectedArrivalTextField.getText());
        TrainSwitchListText.setStringDepartedExpected(departedExpectedTextField.getText());

        TrainSwitchListText.setStringVisitNumber(visitNumberTextField.getText());
        TrainSwitchListText.setStringVisitNumberDeparted(visitNumberDepartedTextField.getText());
        TrainSwitchListText.setStringVisitNumberTerminates(visitNumberTerminatesTextField.getText());
        TrainSwitchListText.setStringVisitNumberTerminatesDeparted(visitNumberTerminatesDepartedTextField.getText());
        TrainSwitchListText.setStringVisitNumberDone(visitNumberDoneTextField.getText());

        TrainSwitchListText.setStringTrainDirectionChange(trainDirectionChangeTextField.getText());
        TrainSwitchListText.setStringNoCarPickUps(noCarPickUpsTextField.getText());
        TrainSwitchListText.setStringNoCarDrops(noCarDropsTextField.getText());
        TrainSwitchListText.setStringTrainDone(trainDoneTextField.getText());
        TrainSwitchListText.setStringTrainDepartsCars(trainDepartsCarsTextField.getText());
        TrainSwitchListText.setStringTrainDepartsLoads(trainDepartsLoadsTextField.getText());
        
        TrainSwitchListText.setStringSwitchListByTrack(switchListByTrackTextField.getText());
        TrainSwitchListText.setStringHoldCar(holdCarTextField.getText());

        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();
    }

    @Override
    public boolean isDirty() {
        return (TrainSwitchListText.getStringSwitchListFor().equals(switchListForTextField.getText())
                || TrainSwitchListText.getStringScheduledWork().equals(scheduledWorkTextField.getText())
                || TrainSwitchListText.getStringDepartsAt().equals(departsAtTextField.getText())
                || TrainSwitchListText.getStringDepartsAtExpectedArrival().equals(departsAtExpectedArrivalTextField.getText())
                || TrainSwitchListText.getStringDepartedExpected().equals(departedExpectedTextField.getText())
                || TrainSwitchListText.getStringVisitNumber().equals(visitNumberTextField.getText())
                || TrainSwitchListText.getStringVisitNumberDeparted().equals(visitNumberDepartedTextField.getText())
                || TrainSwitchListText.getStringVisitNumberTerminates().equals(visitNumberTerminatesTextField.getText())
                || TrainSwitchListText.getStringVisitNumberTerminatesDeparted().equals(visitNumberTerminatesDepartedTextField.getText())
                || TrainSwitchListText.getStringVisitNumberDone().equals(visitNumberDoneTextField.getText())
                || TrainSwitchListText.getStringTrainDirectionChange().equals(trainDirectionChangeTextField.getText())
                || TrainSwitchListText.getStringNoCarPickUps().equals(noCarPickUpsTextField.getText())
                || TrainSwitchListText.getStringNoCarDrops().equals(noCarDropsTextField.getText())
                || TrainSwitchListText.getStringTrainDone().equals(trainDoneTextField.getText()))
                || TrainSwitchListText.getStringTrainDepartsCars().equals(trainDepartsCarsTextField.getText())
                || TrainSwitchListText.getStringTrainDepartsLoads().equals(trainDepartsLoadsTextField.getText())
                || TrainSwitchListText.getStringSwitchListByTrack().equals(switchListByTrackTextField.getText())
                || TrainSwitchListText.getStringHoldCar().equals(holdCarTextField.getText())
                ;
    }
}

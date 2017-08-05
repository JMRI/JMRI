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
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManifestText;

/**
 * Frame for user edit of manifest text strings
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class EditManifestTextPanel extends OperationsPreferencesPanel {

//    private static final Logger log = LoggerFactory.getLogger(OperationsSetupPanel.class);

    protected static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton resetButton = new JButton(Bundle.getMessage("Reset"));

    // text field
    JTextField manifestForTrainTextField = new JTextField(60);
    JTextField validTextField = new JTextField(60);
    JTextField scheduledWorkAtTextField = new JTextField(60);
    JTextField scheduledWorkDepartureTextField = new JTextField(60);
    JTextField scheduledWorkArrivalTextField = new JTextField(60);
    JTextField noScheduledWorkAtTextField = new JTextField(60);
    JTextField noScheduledWorkAtWithRouteCommentTextField = new JTextField(60);
    JTextField departTimeTextField = new JTextField(60);
    JTextField trainDepartsCarsTextField = new JTextField(60);
    JTextField trainDepartsLoadsTextField = new JTextField(60);
    JTextField trainTerminatesInTextField = new JTextField(60);

    JTextField destinationTextField = new JTextField(60);
    JTextField toTextField = new JTextField(25);
    JTextField fromTextField = new JTextField(25);
    JTextField destTextField = new JTextField(25);
    JTextField finalDestinationTextField = new JTextField(25);

    JTextField addHelpersAtTextField = new JTextField(60);
    JTextField removeHelpersAtTextField = new JTextField(60);
    JTextField locoChangeAtTextField = new JTextField(60);
    JTextField cabooseChangeAtTextField = new JTextField(60);
    JTextField locoAndCabooseChangeAtTextField = new JTextField(60);

    public EditManifestTextPanel() {

        // the following code sets the frame's initial state
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // manifest text fields
        JPanel pManifest = new JPanel();
        JScrollPane pManifestPane = new JScrollPane(pManifest);
        pManifestPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutManifest")));
        pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));

        JPanel pManifestForTrainTextField = new JPanel();
        pManifestForTrainTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("ManifestForTrain")));
        pManifestForTrainTextField.add(manifestForTrainTextField);
        manifestForTrainTextField.setText(TrainManifestText.getStringManifestForTrain());
        manifestForTrainTextField.setToolTipText(rb.getString("ToolTipManifestForTrain"));
        pManifest.add(pManifestForTrainTextField);

        JPanel pValidTextField = new JPanel();
        pValidTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("Valid")));
        pValidTextField.add(validTextField);
        validTextField.setText(TrainManifestText.getStringValid());
        validTextField.setToolTipText(rb.getString("ToolTipValid"));
        pManifest.add(pValidTextField);

        JPanel pScheduledWorkAtTextField = new JPanel();
        pScheduledWorkAtTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduledWorkAt")));
        pScheduledWorkAtTextField.add(scheduledWorkAtTextField);
        scheduledWorkAtTextField.setText(TrainManifestText.getStringScheduledWork());
        scheduledWorkAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pScheduledWorkAtTextField);

        JPanel pScheduledWorkDepartureTextField = new JPanel();
        pScheduledWorkDepartureTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("WorkDepartureTime")));
        pScheduledWorkDepartureTextField.add(scheduledWorkDepartureTextField);
        scheduledWorkDepartureTextField.setText(TrainManifestText.getStringWorkDepartureTime());
        scheduledWorkDepartureTextField.setToolTipText(rb.getString("ToolTipWorkDepartureTime"));
        pManifest.add(pScheduledWorkDepartureTextField);

        JPanel pScheduledWorkArrivalTextField = new JPanel();
        pScheduledWorkArrivalTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("WorkArrivalTime")));
        pScheduledWorkArrivalTextField.add(scheduledWorkArrivalTextField);
        scheduledWorkArrivalTextField.setText(TrainManifestText.getStringWorkArrivalTime());
        scheduledWorkArrivalTextField.setToolTipText(rb.getString("ToolTipWorkDepartureTime"));
        pManifest.add(pScheduledWorkArrivalTextField);

        JPanel pNoScheduledWorkAt = new JPanel();
        pNoScheduledWorkAt.setBorder(BorderFactory.createTitledBorder(rb.getString("NoScheduledWorkAt")));
        pNoScheduledWorkAt.add(noScheduledWorkAtTextField);
        noScheduledWorkAtTextField.setText(TrainManifestText.getStringNoScheduledWork());
        noScheduledWorkAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pNoScheduledWorkAt);

        JPanel pNoScheduledWorkAtWithRouteComment = new JPanel();
        pNoScheduledWorkAtWithRouteComment.setBorder(BorderFactory.createTitledBorder(rb
                .getString("NoScheduledWorkAtWithRouteComment")));
        pNoScheduledWorkAtWithRouteComment.add(noScheduledWorkAtWithRouteCommentTextField);
        noScheduledWorkAtWithRouteCommentTextField
                .setText(TrainManifestText.getStringNoScheduledWorkWithRouteComment());
        noScheduledWorkAtWithRouteCommentTextField.setToolTipText(rb
                .getString("ToolTipNoScheduledWorkAtWithRouteComment"));
        pManifest.add(pNoScheduledWorkAtWithRouteComment);

        JPanel pDepartTime = new JPanel();
        pDepartTime.setBorder(BorderFactory.createTitledBorder(rb.getString("departureTime")));
        pDepartTime.add(departTimeTextField);
        departTimeTextField.setText(TrainManifestText.getStringDepartTime());
        departTimeTextField.setToolTipText(rb.getString("ToolTipNoScheduledWorkShowTime"));
        pManifest.add(pDepartTime);

        JPanel pTrainDepartsCars = new JPanel();
        pTrainDepartsCars.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDepartsCars")));
        pTrainDepartsCars.add(trainDepartsCarsTextField);
        trainDepartsCarsTextField.setText(TrainManifestText.getStringTrainDepartsCars());
        trainDepartsCarsTextField.setToolTipText(rb.getString("ToolTipTrainDepartsCars"));
        pManifest.add(pTrainDepartsCars);

        JPanel pTrainDepartsLoadsTextField = new JPanel();
        pTrainDepartsLoadsTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDepartsLoads")));
        pTrainDepartsLoadsTextField.add(trainDepartsLoadsTextField);
        trainDepartsLoadsTextField.setText(TrainManifestText.getStringTrainDepartsLoads());
        trainDepartsLoadsTextField.setToolTipText(rb.getString("ToolTipTrainDepartsLoads"));
        pManifest.add(pTrainDepartsLoadsTextField);

        JPanel pTrainTerminatesIn = new JPanel();
        pTrainTerminatesIn.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainTerminatesIn")));
        pTrainTerminatesIn.add(trainTerminatesInTextField);
        trainTerminatesInTextField.setText(TrainManifestText.getStringTrainTerminates());
        trainTerminatesInTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pTrainTerminatesIn);

        JPanel pDestination = new JPanel();
        pDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("destination")));
        pDestination.add(destinationTextField);
        destinationTextField.setText(TrainManifestText.getStringDestination());
        pManifest.add(pDestination);

        JPanel pToFrom = new JPanel();
        pToFrom.setLayout(new BoxLayout(pToFrom, BoxLayout.X_AXIS));

        JPanel pTo = new JPanel();
        pTo.setBorder(BorderFactory.createTitledBorder(rb.getString("to")));
        pTo.add(toTextField);
        toTextField.setText(TrainManifestText.getStringTo());
        pToFrom.add(pTo);

        JPanel pFrom = new JPanel();
        pFrom.setBorder(BorderFactory.createTitledBorder(rb.getString("from")));
        pFrom.add(fromTextField);
        fromTextField.setText(TrainManifestText.getStringFrom());
        pToFrom.add(pFrom);

        pManifest.add(pToFrom);

        JPanel pDestFinalDest = new JPanel();
        pDestFinalDest.setLayout(new BoxLayout(pDestFinalDest, BoxLayout.X_AXIS));

        JPanel pDest = new JPanel();
        pDest.setBorder(BorderFactory.createTitledBorder(rb.getString("dest")));
        pDest.add(destTextField);
        destTextField.setText(TrainManifestText.getStringDest());
        pDestFinalDest.add(pDest);

        JPanel pFinalDestination = new JPanel();
        pFinalDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("FD") + " ("
                + Bundle.getMessage("FinalDestination") + ")"));
        pFinalDestination.add(finalDestinationTextField);
        finalDestinationTextField.setText(TrainManifestText.getStringFinalDestination());
        pDestFinalDest.add(pFinalDestination);

        pManifest.add(pDestFinalDest);

        JPanel pAddHelpersAt = new JPanel();
        pAddHelpersAt.setBorder(BorderFactory.createTitledBorder(rb.getString("AddHelpersAt")));
        pAddHelpersAt.add(addHelpersAtTextField);
        addHelpersAtTextField.setText(TrainManifestText.getStringAddHelpers());
        addHelpersAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pAddHelpersAt);

        JPanel pRemoveHelpersAt = new JPanel();
        pRemoveHelpersAt.setBorder(BorderFactory.createTitledBorder(rb.getString("RemoveHelpersAt")));
        pRemoveHelpersAt.add(removeHelpersAtTextField);
        removeHelpersAtTextField.setText(TrainManifestText.getStringRemoveHelpers());
        removeHelpersAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pRemoveHelpersAt);

        JPanel pLocoChangeAt = new JPanel();
        pLocoChangeAt.setBorder(BorderFactory.createTitledBorder(rb.getString("LocoChangeAt")));
        pLocoChangeAt.add(locoChangeAtTextField);
        locoChangeAtTextField.setText(TrainManifestText.getStringLocoChange());
        locoChangeAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pLocoChangeAt);

        JPanel pCabooseChangeAt = new JPanel();
        pCabooseChangeAt.setBorder(BorderFactory.createTitledBorder(rb.getString("CabooseChangeAt")));
        pCabooseChangeAt.add(cabooseChangeAtTextField);
        cabooseChangeAtTextField.setText(TrainManifestText.getStringCabooseChange());
        cabooseChangeAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pCabooseChangeAt);

        JPanel pLocoAndCabooseChangeAt = new JPanel();
        pLocoAndCabooseChangeAt.setBorder(BorderFactory.createTitledBorder(rb.getString("LocoAndCabooseChangeAt")));
        pLocoAndCabooseChangeAt.add(locoAndCabooseChangeAtTextField);
        locoAndCabooseChangeAtTextField.setText(TrainManifestText.getStringLocoAndCabooseChange());
        locoAndCabooseChangeAtTextField.setToolTipText(rb.getString("ToolTipScheduledWorkAt"));
        pManifest.add(pLocoAndCabooseChangeAt);

        // add tool tips
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));

        // row 11
        JPanel pControl = new JPanel();
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, resetButton, 0, 0);
        addItem(pControl, saveButton, 1, 0);

        add(pManifestPane);
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
            manifestForTrainTextField.setText(rb.getString("ManifestForTrain"));
            validTextField.setText(rb.getString("Valid"));
            scheduledWorkAtTextField.setText(rb.getString("ScheduledWorkAt"));
            scheduledWorkDepartureTextField.setText(rb.getString("WorkDepartureTime"));
            scheduledWorkArrivalTextField.setText(rb.getString("WorkArrivalTime"));
            noScheduledWorkAtTextField.setText(rb.getString("NoScheduledWorkAt"));
            noScheduledWorkAtWithRouteCommentTextField.setText(rb.getString("NoScheduledWorkAtWithRouteComment"));
            departTimeTextField.setText(rb.getString("departureTime"));
            trainDepartsCarsTextField.setText(rb.getString("TrainDepartsCars"));
            trainDepartsLoadsTextField.setText(rb.getString("TrainDepartsLoads"));
            trainTerminatesInTextField.setText(rb.getString("TrainTerminatesIn"));

            destinationTextField.setText(rb.getString("destination"));
            toTextField.setText(rb.getString("to"));
            fromTextField.setText(rb.getString("from"));
            destTextField.setText(rb.getString("dest"));
            finalDestinationTextField.setText(rb.getString("FD"));

            addHelpersAtTextField.setText(rb.getString("AddHelpersAt"));
            removeHelpersAtTextField.setText(rb.getString("RemoveHelpersAt"));
            locoChangeAtTextField.setText(rb.getString("LocoChangeAt"));
            cabooseChangeAtTextField.setText(rb.getString("CabooseChangeAt"));
            locoAndCabooseChangeAtTextField.setText(rb.getString("LocoAndCabooseChangeAt"));
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
        return Bundle.getMessage("TitleManifestText");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        TrainManifestText.setStringManifestForTrain(manifestForTrainTextField.getText());
        TrainManifestText.setStringValid(validTextField.getText());
        TrainManifestText.setStringScheduledWork(scheduledWorkAtTextField.getText());
        TrainManifestText.setStringWorkDepartureTime(scheduledWorkDepartureTextField.getText());
        TrainManifestText.setStringWorkArrivalTime(scheduledWorkArrivalTextField.getText());
        TrainManifestText.setStringNoScheduledWork(noScheduledWorkAtTextField.getText());
        TrainManifestText.setStringNoScheduledWorkWithRouteComment(noScheduledWorkAtWithRouteCommentTextField.getText());
        TrainManifestText.setStringDepartTime(departTimeTextField.getText());
        TrainManifestText.setStringTrainDepartsCars(trainDepartsCarsTextField.getText());
        TrainManifestText.setStringTrainDepartsLoads(trainDepartsLoadsTextField.getText());
        TrainManifestText.setStringTrainTerminates(trainTerminatesInTextField.getText());

        TrainManifestText.setStringDestination(destinationTextField.getText());
        TrainManifestText.setStringTo(toTextField.getText());
        TrainManifestText.setStringFrom(fromTextField.getText());
        TrainManifestText.setStringDest(destTextField.getText());
        TrainManifestText.setStringFinalDestination(finalDestinationTextField.getText());

        TrainManifestText.setStringAddHelpers(addHelpersAtTextField.getText());
        TrainManifestText.setStringRemoveHelpers(removeHelpersAtTextField.getText());
        TrainManifestText.setStringLocoChange(locoChangeAtTextField.getText());
        TrainManifestText.setStringCabooseChange(cabooseChangeAtTextField.getText());
        TrainManifestText.setStringLocoAndCabooseChange(locoAndCabooseChangeAtTextField.getText());

        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();

        // recreate all train manifests
        InstanceManager.getDefault(TrainManager.class).setTrainsModified();
    }

    @Override
    public boolean isDirty() {
        return (TrainManifestText.getStringManifestForTrain().equals(manifestForTrainTextField.getText())
                || TrainManifestText.getStringValid().equals(validTextField.getText())
                || TrainManifestText.getStringScheduledWork().equals(scheduledWorkAtTextField.getText())
                || TrainManifestText.getStringWorkDepartureTime().equals(scheduledWorkDepartureTextField.getText())
                || TrainManifestText.getStringWorkArrivalTime().equals(scheduledWorkArrivalTextField.getText())
                || TrainManifestText.getStringNoScheduledWork().equals(noScheduledWorkAtTextField.getText())
                || TrainManifestText.getStringNoScheduledWorkWithRouteComment().equals(noScheduledWorkAtWithRouteCommentTextField.getText())
                || TrainManifestText.getStringDepartTime().equals(departTimeTextField.getText())
                || TrainManifestText.getStringTrainDepartsCars().equals(trainDepartsCarsTextField.getText())
                || TrainManifestText.getStringTrainDepartsLoads().equals(trainDepartsLoadsTextField.getText())
                || TrainManifestText.getStringTrainTerminates().equals(trainTerminatesInTextField.getText())
                || TrainManifestText.getStringDestination().equals(destinationTextField.getText())
                || TrainManifestText.getStringTo().equals(toTextField.getText())
                || TrainManifestText.getStringFrom().equals(fromTextField.getText())
                || TrainManifestText.getStringDest().equals(destTextField.getText())
                || TrainManifestText.getStringFinalDestination().equals(finalDestinationTextField.getText())
                || TrainManifestText.getStringAddHelpers().equals(addHelpersAtTextField.getText())
                || TrainManifestText.getStringRemoveHelpers().equals(removeHelpersAtTextField.getText())
                || TrainManifestText.getStringLocoChange().equals(locoChangeAtTextField.getText())
                || TrainManifestText.getStringCabooseChange().equals(cabooseChangeAtTextField.getText())
                || TrainManifestText.getStringLocoAndCabooseChange().equals(locoAndCabooseChangeAtTextField.getText()));
    }
}

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of setup options
 *
 * @author Dan Boudreau Copyright (C) 2010, 2011, 2012, 2013, 2015
 */
public class OptionPanel extends OperationsPreferencesPanel {

    // labels
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // radio buttons
    JRadioButton buildNormal = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton buildAggressive = new JRadioButton(Bundle.getMessage("Aggressive"));

    // check boxes
    JCheckBox routerCheckBox = new JCheckBox(Bundle.getMessage("EnableCarRouting"));
    JCheckBox routerYardCheckBox = new JCheckBox(Bundle.getMessage("EnableCarRoutingYard"));
    JCheckBox routerStagingCheckBox = new JCheckBox(Bundle.getMessage("EnableCarRoutingStaging"));
    JCheckBox routerAllTrainsBox = new JCheckBox(Bundle.getMessage("AllTrains"));
    JCheckBox routerRestrictBox = new JCheckBox(Bundle.getMessage("EnableTrackDestinationRestrictions"));

    JCheckBox valueCheckBox = new JCheckBox(Bundle.getMessage("EnableValue"));
    JCheckBox rfidCheckBox = new JCheckBox(Bundle.getMessage("EnableRfid"));
    JCheckBox carLoggerCheckBox = new JCheckBox(Bundle.getMessage("EnableCarLogging"));
    JCheckBox engineLoggerCheckBox = new JCheckBox(Bundle.getMessage("EnableEngineLogging"));
    JCheckBox trainLoggerCheckBox = new JCheckBox(Bundle.getMessage("EnableTrainLogging"));

    JCheckBox localInterchangeCheckBox = new JCheckBox(Bundle.getMessage("AllowLocalInterchange"));
    JCheckBox localSpurCheckBox = new JCheckBox(Bundle.getMessage("AllowLocalSpur"));
    JCheckBox localYardCheckBox = new JCheckBox(Bundle.getMessage("AllowLocalYard"));

    JCheckBox trainIntoStagingCheckBox = new JCheckBox(Bundle.getMessage("TrainIntoStaging"));
    JCheckBox stagingAvailCheckBox = new JCheckBox(Bundle.getMessage("StagingAvailable"));
    JCheckBox stagingTurnCheckBox = new JCheckBox(Bundle.getMessage("AllowCarsToReturn"));
    JCheckBox promptFromTrackStagingCheckBox = new JCheckBox(Bundle.getMessage("PromptFromStaging"));
    JCheckBox promptToTrackStagingCheckBox = new JCheckBox(Bundle.getMessage("PromptToStaging"));
    JCheckBox tryNormalStagingCheckBox = new JCheckBox(Bundle.getMessage("TryNormalStaging"));

    JCheckBox generateCvsManifestCheckBox = new JCheckBox(Bundle.getMessage("GenerateCsvManifest"));
    JCheckBox generateCvsSwitchListCheckBox = new JCheckBox(Bundle.getMessage("GenerateCsvSwitchList"));

    JCheckBox enableVsdCheckBox = new JCheckBox(Bundle.getMessage("EnableVSD"));
    JCheckBox saveTrainManifestCheckBox = new JCheckBox(Bundle.getMessage("SaveManifests"));

    // text field
    JTextField rfidTextField = new JTextField(10);
    JTextField valueTextField = new JTextField(10);

    // combo boxes
    JComboBox<Integer> numberPassesComboBox = new JComboBox<>();

    public OptionPanel() {

        // load checkboxes
        localInterchangeCheckBox.setSelected(Setup.isLocalInterchangeMovesEnabled());
        localSpurCheckBox.setSelected(Setup.isLocalSpurMovesEnabled());
        localYardCheckBox.setSelected(Setup.isLocalYardMovesEnabled());
        // staging options
        trainIntoStagingCheckBox.setSelected(Setup.isStagingTrainCheckEnabled());
        stagingAvailCheckBox.setSelected(Setup.isStagingTrackImmediatelyAvail());
        stagingTurnCheckBox.setSelected(Setup.isStagingAllowReturnEnabled());
        promptToTrackStagingCheckBox.setSelected(Setup.isStagingPromptToEnabled());
        promptFromTrackStagingCheckBox.setSelected(Setup.isStagingPromptFromEnabled());
        tryNormalStagingCheckBox.setSelected(Setup.isStagingTryNormalBuildEnabled());
        // router
        routerCheckBox.setSelected(Setup.isCarRoutingEnabled());
        routerYardCheckBox.setSelected(Setup.isCarRoutingViaYardsEnabled());
        routerStagingCheckBox.setSelected(Setup.isCarRoutingViaStagingEnabled());
        routerAllTrainsBox.setSelected(!Setup.isOnlyActiveTrainsEnabled());
        routerRestrictBox.setSelected(Setup.isCheckCarDestinationEnabled());
        // logging options
        carLoggerCheckBox.setSelected(Setup.isCarLoggerEnabled());
        engineLoggerCheckBox.setSelected(Setup.isEngineLoggerEnabled());
        trainLoggerCheckBox.setSelected(Setup.isTrainLoggerEnabled());
        // save manifests
        saveTrainManifestCheckBox.setSelected(Setup.isSaveTrainManifestsEnabled());

        generateCvsManifestCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());
        generateCvsSwitchListCheckBox.setSelected(Setup.isGenerateCsvSwitchListEnabled());
        valueCheckBox.setSelected(Setup.isValueEnabled());
        rfidCheckBox.setSelected(Setup.isRfidEnabled());
        enableVsdCheckBox.setSelected(Setup.isVsdPhysicalLocationEnabled());

        // load text fields
        rfidTextField.setText(Setup.getRfidLabel());
        valueTextField.setText(Setup.getValueLabel());

        // add tool tips
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
        rfidTextField.setToolTipText(Bundle.getMessage("EnterNameRfidTip"));
        valueTextField.setToolTipText(Bundle.getMessage("EnterNameValueTip"));
        stagingTurnCheckBox.setToolTipText(Bundle.getMessage("AlsoAvailablePerTrain"));

        // load combobox, allow 2 to 4 passes
        for (int x = 2; x < 5; x++) {
            numberPassesComboBox.addItem(x);
        }

        numberPassesComboBox.setSelectedItem(Setup.getNumberPasses());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane panelPane = new JScrollPane(panel);

        // Build Options panel
        JPanel pBuild = new JPanel();
        pBuild.setLayout(new BoxLayout(pBuild, BoxLayout.Y_AXIS));
        pBuild.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutBuildOptions")));

        JPanel pOpt = new JPanel();
        pOpt.setLayout(new GridBagLayout());

        addItem(pOpt, buildNormal, 1, 0);
        addItem(pOpt, buildAggressive, 2, 0);
        pBuild.add(pOpt);

        JPanel pPasses = new JPanel();
        pPasses.setLayout(new GridBagLayout());
        pPasses.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutNumberPasses")));
        addItem(pPasses, numberPassesComboBox, 0, 0);
        pBuild.add(pPasses);

        // Switcher Service
        JPanel pSwitcher = new JPanel();
        pSwitcher.setLayout(new GridBagLayout());
        pSwitcher.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitcherService")));

        addItemLeft(pSwitcher, localInterchangeCheckBox, 1, 1);
        addItemLeft(pSwitcher, localSpurCheckBox, 1, 2);
        addItemLeft(pSwitcher, localYardCheckBox, 1, 3);

        // Staging
        JPanel pStaging = new JPanel();
        pStaging.setLayout(new GridBagLayout());
        pStaging.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutStaging")));

        addItemLeft(pStaging, trainIntoStagingCheckBox, 1, 4);
        addItemLeft(pStaging, stagingAvailCheckBox, 1, 5);
        addItemLeft(pStaging, stagingTurnCheckBox, 1, 6);
        addItemLeft(pStaging, promptFromTrackStagingCheckBox, 1, 7);
        addItemLeft(pStaging, promptToTrackStagingCheckBox, 1, 8);
        addItemLeft(pStaging, tryNormalStagingCheckBox, 1, 9);

        // Router panel
        JPanel pRouter = new JPanel();
        pRouter.setLayout(new GridBagLayout());
        pRouter.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutRouterOptions")));
        addItemLeft(pRouter, routerCheckBox, 1, 0);
        addItemLeft(pRouter, routerYardCheckBox, 1, 1);
        addItemLeft(pRouter, routerStagingCheckBox, 1, 2);
        addItemLeft(pRouter, routerAllTrainsBox, 1, 3);
        addItemLeft(pRouter, routerRestrictBox, 1, 4);

        // Logger panel
        JPanel pLogger = new JPanel();
        pLogger.setLayout(new GridBagLayout());
        pLogger.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutLoggerOptions")));
        addItemLeft(pLogger, engineLoggerCheckBox, 1, 0);
        addItemLeft(pLogger, carLoggerCheckBox, 1, 1);
        addItemLeft(pLogger, trainLoggerCheckBox, 1, 2);

        // Custom Manifests and Switch Lists
        JPanel pCustom = new JPanel();
        pCustom.setLayout(new GridBagLayout());
        pCustom.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutCustomManifests")));
        addItemLeft(pCustom, generateCvsManifestCheckBox, 1, 0);
        addItemLeft(pCustom, generateCvsSwitchListCheckBox, 1, 1);

        // Options
        JPanel pOption = new JPanel();
        pOption.setLayout(new GridBagLayout());
        pOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptions")));
        addItemLeft(pOption, saveTrainManifestCheckBox, 1, 1);
        addItemLeft(pOption, valueCheckBox, 1, 2);
        addItemLeft(pOption, valueTextField, 2, 2);
        addItemLeft(pOption, rfidCheckBox, 1, 3);
        addItemLeft(pOption, rfidTextField, 2, 3);
        addItemLeft(pOption, enableVsdCheckBox, 1, 4);

        // row 11
        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, saveButton, 3, 9);

        panel.add(pBuild);
        panel.add(pSwitcher);
        panel.add(pStaging);
        panel.add(pRouter);
        panel.add(pLogger);
        panel.add(pCustom);
        panel.add(pOption);

        add(panelPane);
        add(pControl);

        // setup buttons
        addButtonAction(saveButton);

        // radio buttons
        ButtonGroup buildGroup = new ButtonGroup();
        buildGroup.add(buildNormal);
        buildGroup.add(buildAggressive);
        addRadioButtonAction(buildNormal);
        addRadioButtonAction(buildAggressive);

        // check boxes
        addCheckBoxAction(routerCheckBox);
        addCheckBoxAction(routerRestrictBox);
        setRouterCheckBoxesEnabled();

        setBuildOption();
        enableComponents();
    }

    private void setBuildOption() {
        buildNormal.setSelected(!Setup.isBuildAggressive());
        buildAggressive.setSelected(Setup.isBuildAggressive());
    }

    private void enableComponents() {
        // disable staging option if normal mode
        stagingAvailCheckBox.setEnabled(buildAggressive.isSelected());
        numberPassesComboBox.setEnabled(buildAggressive.isSelected());
        tryNormalStagingCheckBox.setEnabled(buildAggressive.isSelected());
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button selected");
        // can't change the build option if there are trains built
        if (InstanceManager.getDefault(TrainManager.class).isAnyTrainBuilt()) {
            setBuildOption(); // restore the correct setting
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotChangeBuild"),
                    Bundle.getMessage("MustTerminateOrReset"), JmriJOptionPane.ERROR_MESSAGE);
        }
        enableComponents();
        // Special case where there are train build failures that created work.
        // Track reserve needs to cleaned up by reseting build failed trains
        if (buildAggressive.isSelected() != Setup.isBuildAggressive() &&
                InstanceManager.getDefault(LocationManager.class).hasWork()) {
            InstanceManager.getDefault(TrainManager.class).resetBuildFailedTrains();
        }
    }

    // Save button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            this.savePreferences();
            var topLevelAncestor = getTopLevelAncestor();
            if (Setup.isCloseWindowOnSaveEnabled() && topLevelAncestor instanceof OptionFrame) {
                ((OptionFrame) topLevelAncestor).dispose();
            }
        }
    }

    @Override
    protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == routerCheckBox) {
            setRouterCheckBoxesEnabled();
        }
        if (ae.getSource() == routerRestrictBox && routerRestrictBox.isSelected()) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("WarnExtremeTrackDest"),
                    Bundle.getMessage("WarnExtremeTitle"), JmriJOptionPane.WARNING_MESSAGE);
        }
    }

    private void setRouterCheckBoxesEnabled() {
        routerYardCheckBox.setEnabled(routerCheckBox.isSelected());
        routerStagingCheckBox.setEnabled(routerCheckBox.isSelected());
        routerAllTrainsBox.setEnabled(routerCheckBox.isSelected());
        routerRestrictBox.setEnabled(routerCheckBox.isSelected());
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitleOptions");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        // build option
        Setup.setBuildAggressive(buildAggressive.isSelected());
        Setup.setNumberPasses((Integer) numberPassesComboBox.getSelectedItem());
        // Local moves?
        Setup.setLocalInterchangeMovesEnabled(localInterchangeCheckBox.isSelected());
        Setup.setLocalSpurMovesEnabled(localSpurCheckBox.isSelected());
        Setup.setLocalYardMovesEnabled(localYardCheckBox.isSelected());
        // Staging options
        Setup.setStagingTrainCheckEnabled(trainIntoStagingCheckBox.isSelected());
        Setup.setStagingTrackImmediatelyAvail(stagingAvailCheckBox.isSelected());
        Setup.setStagingAllowReturnEnabled(stagingTurnCheckBox.isSelected());
        Setup.setStagingPromptFromEnabled(promptFromTrackStagingCheckBox.isSelected());
        Setup.setStagingPromptToEnabled(promptToTrackStagingCheckBox.isSelected());
        Setup.setStagingTryNormalBuildEnabled(tryNormalStagingCheckBox.isSelected());
        // Car routing enabled?
        Setup.setCarRoutingEnabled(routerCheckBox.isSelected());
        Setup.setCarRoutingViaYardsEnabled(routerYardCheckBox.isSelected());
        Setup.setCarRoutingViaStagingEnabled(routerStagingCheckBox.isSelected());
        Setup.setOnlyActiveTrainsEnabled(!routerAllTrainsBox.isSelected());
        Setup.setCheckCarDestinationEnabled(routerRestrictBox.isSelected());
        // Options
        Setup.setGenerateCsvManifestEnabled(generateCvsManifestCheckBox.isSelected());
        Setup.setGenerateCsvSwitchListEnabled(generateCvsSwitchListCheckBox.isSelected());
        Setup.setSaveTrainManifestsEnabled(saveTrainManifestCheckBox.isSelected());
        Setup.setValueEnabled(valueCheckBox.isSelected());
        Setup.setValueLabel(valueTextField.getText());
        Setup.setRfidEnabled(rfidCheckBox.isSelected());
        Setup.setRfidLabel(rfidTextField.getText());
        // Logging enabled?
        Setup.setEngineLoggerEnabled(engineLoggerCheckBox.isSelected());
        Setup.setCarLoggerEnabled(carLoggerCheckBox.isSelected());
        Setup.setTrainLoggerEnabled(trainLoggerCheckBox.isSelected());
        // VSD
        Setup.setVsdPhysicalLocationEnabled(enableVsdCheckBox.isSelected());
        // write the file
        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();
    }

    @Override
    public boolean isDirty() {
        return !( // build option
        Setup.isBuildAggressive() == buildAggressive.isSelected() &&
                Setup.getNumberPasses() == (int) numberPassesComboBox.getSelectedItem()
                // Local moves?
                &&
                Setup.isLocalInterchangeMovesEnabled() == localInterchangeCheckBox.isSelected() &&
                Setup.isLocalSpurMovesEnabled() == localSpurCheckBox.isSelected() &&
                Setup.isLocalYardMovesEnabled() == localYardCheckBox.isSelected()
                // Staging options
                &&
                Setup.isStagingTrainCheckEnabled() == trainIntoStagingCheckBox.isSelected() &&
                Setup.isStagingTrackImmediatelyAvail() == stagingAvailCheckBox.isSelected() &&
                Setup.isStagingAllowReturnEnabled() == stagingTurnCheckBox.isSelected() &&
                Setup.isStagingPromptFromEnabled() == promptFromTrackStagingCheckBox.isSelected() &&
                Setup.isStagingPromptToEnabled() == promptToTrackStagingCheckBox.isSelected() &&
                Setup.isStagingTryNormalBuildEnabled() == tryNormalStagingCheckBox.isSelected()
                // Car routing enabled?
                &&
                Setup.isCarRoutingEnabled() == routerCheckBox.isSelected() &&
                Setup.isCarRoutingViaYardsEnabled() == routerYardCheckBox.isSelected() &&
                Setup.isCarRoutingViaStagingEnabled() == routerStagingCheckBox.isSelected() &&
                Setup.isOnlyActiveTrainsEnabled() == !routerAllTrainsBox.isSelected() &&
                Setup.isCheckCarDestinationEnabled() == routerRestrictBox.isSelected()
                // Options
                &&
                Setup.isGenerateCsvManifestEnabled() == generateCvsManifestCheckBox.isSelected() &&
                Setup.isGenerateCsvSwitchListEnabled() == generateCvsSwitchListCheckBox.isSelected() &&
                Setup.isValueEnabled() == valueCheckBox.isSelected() &&
                Setup.getValueLabel().equals(valueTextField.getText()) &&
                Setup.isRfidEnabled() == rfidCheckBox.isSelected() &&
                Setup.getRfidLabel().equals(rfidTextField.getText()) &&
                Setup.isSaveTrainManifestsEnabled() == saveTrainManifestCheckBox.isSelected()
                // Logging enabled?
                &&
                Setup.isEngineLoggerEnabled() == engineLoggerCheckBox.isSelected() &&
                Setup.isCarLoggerEnabled() == carLoggerCheckBox.isSelected() &&
                Setup.isTrainLoggerEnabled() == trainLoggerCheckBox.isSelected()
                // VSD
                &&
                Setup.isVsdPhysicalLocationEnabled() == enableVsdCheckBox.isSelected());
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OptionPanel.class);

}

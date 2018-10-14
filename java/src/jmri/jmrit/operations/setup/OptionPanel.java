package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        trainIntoStagingCheckBox.setSelected(Setup.isTrainIntoStagingCheckEnabled());
        stagingAvailCheckBox.setSelected(Setup.isStagingTrackImmediatelyAvail());
        stagingTurnCheckBox.setSelected(Setup.isAllowReturnToStagingEnabled());
        promptToTrackStagingCheckBox.setSelected(Setup.isPromptToStagingEnabled());
        promptFromTrackStagingCheckBox.setSelected(Setup.isPromptFromStagingEnabled());
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
        addItem(pBuild, pOpt, 1, 0);

        JPanel pPasses = new JPanel();
        pPasses.setLayout(new GridBagLayout());
        pPasses.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutNumberPasses")));
        addItem(pPasses, numberPassesComboBox, 0, 0);
        addItem(pBuild, pPasses, 1, 1);

        // Switcher Service
        JPanel pSwitcher = new JPanel();
        pSwitcher.setLayout(new GridBagLayout());
        pSwitcher.setBorder(BorderFactory
                .createTitledBorder(Bundle.getMessage("BorderLayoutSwitcherService")));

        addItemLeft(pSwitcher, localInterchangeCheckBox, 1, 1);
        addItemLeft(pSwitcher, localSpurCheckBox, 1, 2);
        addItemLeft(pSwitcher, localYardCheckBox, 1, 3);
        addItemLeft(pBuild, pSwitcher, 1, 2);

        // Staging
        JPanel pStaging = new JPanel();
        pStaging.setLayout(new GridBagLayout());
        pStaging.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutStaging")));

        addItemLeft(pStaging, trainIntoStagingCheckBox, 1, 4);
        addItemLeft(pStaging, stagingAvailCheckBox, 1, 5);
        addItemLeft(pStaging, stagingTurnCheckBox, 1, 6);
        addItemLeft(pStaging, promptFromTrackStagingCheckBox, 1, 7);
        addItemLeft(pStaging, promptToTrackStagingCheckBox, 1, 8);
        addItemLeft(pBuild, pStaging, 1, 3);

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

        // disable staging option if normal mode
        stagingAvailCheckBox.setEnabled(buildAggressive.isSelected());
        numberPassesComboBox.setEnabled(buildAggressive.isSelected());

        initMinimumSize();
    }

    private void setBuildOption() {
        buildNormal.setSelected(!Setup.isBuildAggressive());
        buildAggressive.setSelected(Setup.isBuildAggressive());
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button selected");
        // can't change the build option if there are trains built
        if (InstanceManager.getDefault(TrainManager.class).isAnyTrainBuilt()) {
            setBuildOption(); // restore the correct setting
            JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotChangeBuild"), Bundle
                    .getMessage("MustTerminateOrReset"), JOptionPane.ERROR_MESSAGE);
        }
        // disable staging option if normal mode
        stagingAvailCheckBox.setEnabled(buildAggressive.isSelected());
        numberPassesComboBox.setEnabled(buildAggressive.isSelected());
    }

    // Save button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            this.savePreferences();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == routerCheckBox) {
            setRouterCheckBoxesEnabled();
        }
        if (ae.getSource() == routerRestrictBox && routerRestrictBox.isSelected()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("WarnExtremeTrackDest"), Bundle
                    .getMessage("WarnExtremeTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setRouterCheckBoxesEnabled() {
        routerYardCheckBox.setEnabled(routerCheckBox.isSelected());
        routerStagingCheckBox.setEnabled(routerCheckBox.isSelected());
        routerAllTrainsBox.setEnabled(routerCheckBox.isSelected());
        routerRestrictBox.setEnabled(routerCheckBox.isSelected());
    }

    private static final Logger log = LoggerFactory.getLogger(OptionPanel.class);

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
        Setup.setTrainIntoStagingCheckEnabled(trainIntoStagingCheckBox.isSelected());
        Setup.setStagingTrackImmediatelyAvail(stagingAvailCheckBox.isSelected());
        Setup.setAllowReturnToStagingEnabled(stagingTurnCheckBox.isSelected());
        Setup.setPromptFromStagingEnabled(promptFromTrackStagingCheckBox.isSelected());
        Setup.setPromptToStagingEnabled(promptToTrackStagingCheckBox.isSelected());
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
        return ( // build option
                Setup.isBuildAggressive() != buildAggressive.isSelected()
                || Setup.getNumberPasses() != (int) numberPassesComboBox.getSelectedItem()
                // Local moves?
                || Setup.isLocalInterchangeMovesEnabled() != localInterchangeCheckBox.isSelected()
                || Setup.isLocalSpurMovesEnabled() != localSpurCheckBox.isSelected()
                || Setup.isLocalYardMovesEnabled() != localYardCheckBox.isSelected()
                // Staging options
                || Setup.isTrainIntoStagingCheckEnabled() != trainIntoStagingCheckBox.isSelected()
                || Setup.isStagingTrackImmediatelyAvail() != stagingAvailCheckBox.isSelected()
                || Setup.isAllowReturnToStagingEnabled() != stagingTurnCheckBox.isSelected()
                || Setup.isPromptFromStagingEnabled() != promptFromTrackStagingCheckBox.isSelected()
                || Setup.isPromptToStagingEnabled() != promptToTrackStagingCheckBox.isSelected()
                // Car routing enabled?
                || Setup.isCarRoutingEnabled() != routerCheckBox.isSelected()
                || Setup.isCarRoutingViaYardsEnabled() != routerYardCheckBox.isSelected()
                || Setup.isCarRoutingViaStagingEnabled() != routerStagingCheckBox.isSelected()
                || Setup.isOnlyActiveTrainsEnabled() != !routerAllTrainsBox.isSelected()
                || Setup.isCheckCarDestinationEnabled() != routerRestrictBox.isSelected()
                // Options
                || Setup.isGenerateCsvManifestEnabled() != generateCvsManifestCheckBox.isSelected()
                || Setup.isGenerateCsvSwitchListEnabled() != generateCvsSwitchListCheckBox.isSelected()
                || Setup.isValueEnabled() != valueCheckBox.isSelected()
                || !Setup.getValueLabel().equals(valueTextField.getText())
                || Setup.isRfidEnabled() != rfidCheckBox.isSelected()
                || !Setup.getRfidLabel().equals(rfidTextField.getText())
                || Setup.isSaveTrainManifestsEnabled() != saveTrainManifestCheckBox.isSelected()
                // Logging enabled?
                || Setup.isEngineLoggerEnabled() != engineLoggerCheckBox.isSelected()
                || Setup.isCarLoggerEnabled() != carLoggerCheckBox.isSelected()
                || Setup.isTrainLoggerEnabled() != trainLoggerCheckBox.isSelected()
                // VSD
                || Setup.isVsdPhysicalLocationEnabled() != enableVsdCheckBox.isSelected());
    }
}

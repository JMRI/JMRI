// OptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user edit of setup options
 * 
 * @author Dan Boudreau Copyright (C) 2010, 2011, 2012
 * @version $Revision$
 */

public class OptionFrame extends OperationsFrame{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));

	// radio buttons
	JRadioButton buildNormal = new JRadioButton(rb.getString("Normal"));
	JRadioButton buildAggressive = new JRadioButton(rb.getString("Aggressive"));
 
    // check boxes
	JCheckBox routerCheckBox = new JCheckBox(rb.getString("EnableCarRouting"));
	JCheckBox valueCheckBox = new JCheckBox(rb.getString("EnableValue"));
	JCheckBox rfidCheckBox = new JCheckBox(rb.getString("EnableRfid"));
	JCheckBox carLoggerCheckBox = new JCheckBox(rb.getString("EnableCarLogging"));
	JCheckBox engineLoggerCheckBox = new JCheckBox(rb.getString("EnableEngineLogging"));
	JCheckBox trainLoggerCheckBox = new JCheckBox(rb.getString("EnableTrainLogging"));
	
	JCheckBox localInterchangeCheckBox = new JCheckBox(rb.getString("AllowLocalInterchange"));
	JCheckBox localSidingCheckBox = new JCheckBox(rb.getString("AllowLocalSiding"));
	JCheckBox localYardCheckBox = new JCheckBox(rb.getString("AllowLocalYard"));
	
	JCheckBox trainIntoStagingCheckBox = new JCheckBox(rb.getString("TrainIntoStaging"));
	JCheckBox stagingAvailCheckBox = new JCheckBox(rb.getString("StagingAvailable"));
	JCheckBox stagingTurnCheckBox = new JCheckBox(rb.getString("AllowCarsToReturn"));
	JCheckBox promptFromTrackStagingCheckBox = new JCheckBox(rb.getString("PromptFromStaging"));
	JCheckBox promptToTrackStagingCheckBox = new JCheckBox(rb.getString("PromptToStaging"));
	
	JCheckBox generateCvsManifestCheckBox = new JCheckBox(rb.getString("GenerateCsvManifest"));
	JCheckBox generateCvsSwitchListCheckBox = new JCheckBox(rb.getString("GenerateCsvSwitchList"));
	
	JCheckBox enableVsdCheckBox = new JCheckBox(rb.getString("EnableVSD"));

	// text field
	JTextField rfidTextField = new JTextField(10);
	JTextField valueTextField = new JTextField(10);
	
	// combo boxes

	public OptionFrame() {
		super(rb.getString("TitleOptions"));
	}

	public void initComponents() {

		// load checkboxes			
		localInterchangeCheckBox.setSelected(Setup.isLocalInterchangeMovesEnabled());
		localSidingCheckBox.setSelected(Setup.isLocalSidingMovesEnabled());
		localYardCheckBox.setSelected(Setup.isLocalYardMovesEnabled());
		// staging options
		trainIntoStagingCheckBox.setSelected(Setup.isTrainIntoStagingCheckEnabled());
		stagingAvailCheckBox.setSelected(Setup.isStagingTrackImmediatelyAvail());
		stagingTurnCheckBox.setSelected(Setup.isAllowReturnToStagingEnabled());
		promptToTrackStagingCheckBox.setSelected(Setup.isPromptToStagingEnabled());
		promptFromTrackStagingCheckBox.setSelected(Setup.isPromptFromStagingEnabled());
		// router
		routerCheckBox.setSelected(Setup.isCarRoutingEnabled());
		// logging options
		carLoggerCheckBox.setSelected(Setup.isCarLoggerEnabled());
		engineLoggerCheckBox.setSelected(Setup.isEngineLoggerEnabled());
		trainLoggerCheckBox.setSelected(Setup.isTrainLoggerEnabled());

		generateCvsManifestCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());
		generateCvsSwitchListCheckBox.setSelected(Setup.isGenerateCsvSwitchListEnabled());
		valueCheckBox.setSelected(Setup.isValueEnabled());
		rfidCheckBox.setSelected(Setup.isRfidEnabled());
		enableVsdCheckBox.setSelected(Setup.isVsdPhysicalLocationEnabled());
		
		// load text fields
		rfidTextField.setText(Setup.getRfidLabel());
		valueTextField.setText(Setup.getValueLabel());

		// add tool tips
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
		rfidTextField.setToolTipText(rb.getString("EnterNameRfidTip"));
		valueTextField.setToolTipText(rb.getString("EnterNameValueTip"));
			
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		JScrollPane panelPane = new JScrollPane(panel);
		
		// Build Options panel
		JPanel pBuild = new JPanel();
		pBuild.setLayout(new BoxLayout(pBuild,BoxLayout.Y_AXIS));
		pBuild.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutBuildOptions")));
		
		JPanel pOpt = new JPanel();
		pOpt.setLayout(new GridBagLayout());
		
		addItem(pOpt, buildNormal, 1, 0);
		addItem(pOpt, buildAggressive, 2, 0);
		addItem(pBuild, pOpt, 1, 0);
		
		// Switcher Service
		JPanel pSwitcher = new JPanel();
		pSwitcher.setLayout(new GridBagLayout());
		pSwitcher.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutSwitcherService")));
		
		addItemLeft(pSwitcher, localInterchangeCheckBox, 1,1);
		addItemLeft(pSwitcher, localSidingCheckBox, 1,2);
		addItemLeft(pSwitcher, localYardCheckBox, 1,3);
		addItemLeft(pBuild, pSwitcher, 1, 1);
		
		// Staging
		JPanel pStaging = new JPanel();
		pStaging.setLayout(new GridBagLayout());
		pStaging.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStaging")));
		
		addItemLeft(pStaging, trainIntoStagingCheckBox, 1,4);
		addItemLeft(pStaging, stagingAvailCheckBox, 1,5);
		addItemLeft(pStaging, stagingTurnCheckBox, 1,6);
		addItemLeft(pStaging, promptFromTrackStagingCheckBox, 1,7);
		addItemLeft(pStaging, promptToTrackStagingCheckBox, 1,8);
		addItemLeft(pBuild, pStaging, 1, 2);
		
		// Router panel
		JPanel pRouter = new JPanel();
		pRouter.setLayout(new GridBagLayout());
		pRouter.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutRouterOptions")));	
		addItemLeft (pRouter, routerCheckBox, 1,0);
		
		// Logger panel
		JPanel pLogger = new JPanel();
		pLogger.setLayout(new GridBagLayout());
		pLogger.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLoggerOptions")));		
		addItemLeft (pLogger, engineLoggerCheckBox, 1,0);
		addItemLeft (pLogger, carLoggerCheckBox, 1,1);
		addItemLeft (pLogger, trainLoggerCheckBox, 1,2);
		
		JPanel pOption = new JPanel();
		pOption.setLayout(new GridBagLayout());
		pOption.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptions")));
		addItemLeft (pOption, generateCvsManifestCheckBox, 1,0);
		addItemLeft (pOption, generateCvsSwitchListCheckBox, 1,1);
		addItemLeft (pOption, valueCheckBox, 1,2);
		addItemLeft (pOption, valueTextField, 2,2);
		addItemLeft (pOption, rfidCheckBox, 1,3);
		addItemLeft (pOption, rfidTextField, 2,3);
		addItemLeft (pOption, enableVsdCheckBox, 1,4);
		
		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 3, 9);
		
		panel.add(pBuild);
		panel.add(pRouter);
		panel.add(pLogger);
		panel.add(pOption);
		panel.add(pControl);
		
		getContentPane().add(panelPane);

		// setup buttons
		addButtonAction(saveButton);
		
		// radio buttons
		ButtonGroup buildGroup = new ButtonGroup();
		buildGroup.add(buildNormal);
		buildGroup.add(buildAggressive);
		addRadioButtonAction(buildNormal);
		addRadioButtonAction(buildAggressive);
		
		setBuildOption();
		
		// disable staging option if normal mode
		stagingAvailCheckBox.setEnabled(buildAggressive.isSelected());

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_SettingsOptions", true);

		pack();
		if (getWidth()<450)
			setSize(450, getHeight());
		if (getHeight()<Control.panelHeight)		
			setSize(getWidth(), Control.panelHeight);
		setVisible(true);
	}
	
	private void setBuildOption(){
		buildNormal.setSelected(!Setup.isBuildAggressive());
		buildAggressive.setSelected(Setup.isBuildAggressive());
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae){
		log.debug("radio button selected");
		// can't change the build option if there are trains built
		if (TrainManager.instance().getAnyTrainBuilt()){
			setBuildOption();	// restore the correct setting
			JOptionPane.showMessageDialog(this, rb.getString("CanNotChangeBuild"),
					rb.getString("MustTerminateOrReset"),
					JOptionPane.ERROR_MESSAGE);
		}
		// disable staging option if normal mode
		stagingAvailCheckBox.setEnabled(buildAggressive.isSelected());
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// build option
			Setup.setBuildAggressive(buildAggressive.isSelected());
			// Local moves?
			Setup.setLocalInterchangeMovesEnabled(localInterchangeCheckBox.isSelected());
			Setup.setLocalSidingMovesEnabled(localSidingCheckBox.isSelected());
			Setup.setLocalYardMovesEnabled(localYardCheckBox.isSelected());
			// Staging options
			Setup.setTrainIntoStagingCheckEnabled(trainIntoStagingCheckBox.isSelected());
			Setup.setStagingTrackImmediatelyAvail(stagingAvailCheckBox.isSelected());
			Setup.setAllowReturnToStagingEnabled(stagingTurnCheckBox.isSelected());
			Setup.setPromptFromStagingEnabled(promptFromTrackStagingCheckBox.isSelected());
			Setup.setPromptToStagingEnabled(promptToTrackStagingCheckBox.isSelected());
			// Car routing enabled?
			Setup.setCarRoutingEnabled(routerCheckBox.isSelected());
			// Options
			Setup.setGenerateCsvManifestEnabled(generateCvsManifestCheckBox.isSelected());
			Setup.setGenerateCsvSwitchListEnabled(generateCvsSwitchListCheckBox.isSelected());
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
			OperationsSetupXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OptionFrame.class.getName());
}

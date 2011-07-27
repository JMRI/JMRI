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

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user edit of setup options
 * 
 * @author Dan Boudreau Copyright (C) 2010, 2011
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
	JCheckBox promptFromTrackStagingCheckBox = new JCheckBox(rb.getString("PromptFromStaging"));
	JCheckBox promptToTrackStagingCheckBox = new JCheckBox(rb.getString("PromptToStaging"));
	JCheckBox generateCvsManifestCheckBox = new JCheckBox(rb.getString("GenerateCsvManifest"));
	JCheckBox generateCvsSwitchListCheckBox = new JCheckBox(rb.getString("GenerateCsvSwitchList"));
	
	// text field
	
	// combo boxes

	public OptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOptions"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		// load checkboxes	
		valueCheckBox.setSelected(Setup.isValueEnabled());
		rfidCheckBox.setSelected(Setup.isRfidEnabled());
		routerCheckBox.setSelected(Setup.isCarRoutingEnabled());
		carLoggerCheckBox.setSelected(Setup.isCarLoggerEnabled());
		engineLoggerCheckBox.setSelected(Setup.isEngineLoggerEnabled());
		trainLoggerCheckBox.setSelected(Setup.isTrainLoggerEnabled());
		localInterchangeCheckBox.setSelected(Setup.isLocalInterchangeMovesEnabled());
		localSidingCheckBox.setSelected(Setup.isLocalSidingMovesEnabled());
		localYardCheckBox.setSelected(Setup.isLocalYardMovesEnabled());
		trainIntoStagingCheckBox.setSelected(Setup.isTrainIntoStagingCheckEnabled());
		promptToTrackStagingCheckBox.setSelected(Setup.isPromptToStagingEnabled());
		promptFromTrackStagingCheckBox.setSelected(Setup.isPromptFromStagingEnabled());
		generateCvsManifestCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());
		generateCvsSwitchListCheckBox.setSelected(Setup.isGenerateCsvSwitchListEnabled());

		// add tool tips
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
			
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
		// Build Options panel
		JPanel pBuild = new JPanel();
		pBuild.setLayout(new GridBagLayout());
		pBuild.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutBuildOptions")));
		
		JPanel pOpt = new JPanel();
		pOpt.setLayout(new GridBagLayout());
		
		addItem(pOpt, buildNormal, 1, 0);
		addItem(pOpt, buildAggressive, 2, 0);
		
		addItem(pBuild, pOpt, 1, 0);
		addItemLeft(pBuild, localInterchangeCheckBox, 1,1);
		addItemLeft(pBuild, localSidingCheckBox, 1,2);
		addItemLeft(pBuild, localYardCheckBox, 1,3);
		addItemLeft(pBuild, trainIntoStagingCheckBox, 1,4);
		addItemLeft(pBuild, promptFromTrackStagingCheckBox, 1,5);
		addItemLeft(pBuild, promptToTrackStagingCheckBox, 1,6);
		
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
		addItemLeft (pOption, rfidCheckBox, 1,3);
		
		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(pBuild);
		getContentPane().add(pRouter);
		getContentPane().add(pLogger);
		getContentPane().add(pOption);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(saveButton);
		
		// radio buttons
		ButtonGroup buildGroup = new ButtonGroup();
		buildGroup.add(buildNormal);
		buildGroup.add(buildAggressive);
		addRadioButtonAction(buildNormal);
		addRadioButtonAction(buildAggressive);
		
		setBuildOption();

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_SettingsOptions", true);

		pack();
		if (getWidth()<400)
			setSize(400, getHeight());
		if (getHeight()<550)		
			setSize(getWidth(), 550);
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
			// Staging restriction?
			Setup.setTrainIntoStagingCheckEnabled(trainIntoStagingCheckBox.isSelected());
			Setup.setPromptFromStagingEnabled(promptFromTrackStagingCheckBox.isSelected());
			Setup.setPromptToStagingEnabled(promptToTrackStagingCheckBox.isSelected());
			// Car routing enabled?
			Setup.setCarRoutingEnabled(routerCheckBox.isSelected());
			// Options
			Setup.setGenerateCsvManifestEnabled(generateCvsManifestCheckBox.isSelected());
			Setup.setGenerateCsvSwitchListEnabled(generateCvsSwitchListCheckBox.isSelected());
			Setup.setValueEnabled(valueCheckBox.isSelected());
			Setup.setRfidEnabled(rfidCheckBox.isSelected());
			// Logging enabled?		
			Setup.setEngineLoggerEnabled(engineLoggerCheckBox.isSelected());
			Setup.setCarLoggerEnabled(carLoggerCheckBox.isSelected());
			Setup.setTrainLoggerEnabled(trainLoggerCheckBox.isSelected());
			OperationsSetupXml.instance().writeOperationsFile();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OptionFrame.class.getName());
}

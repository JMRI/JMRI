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
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision: 1.11 $
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
	JCheckBox rfidCheckBox = new JCheckBox(rb.getString("EnableRfid"));
	JCheckBox carLoggerCheckBox = new JCheckBox(rb.getString("EnableCarLogging"));
	JCheckBox engineLoggerCheckBox = new JCheckBox(rb.getString("EnableEngineLogging"));
	
	JCheckBox localInterchangeCheckBox = new JCheckBox(rb.getString("AllowLocalInterchange"));
	JCheckBox localSidingCheckBox = new JCheckBox(rb.getString("AllowLocalSiding"));
	JCheckBox localYardCheckBox = new JCheckBox(rb.getString("AllowLocalYard"));
	JCheckBox trainIntoStagingCheckBox = new JCheckBox(rb.getString("TrainIntoStaging"));
	JCheckBox promptTrackStagingCheckBox = new JCheckBox(rb.getString("PromptFromStaging"));
	JCheckBox generateCvsManifestCheckBox = new JCheckBox(rb.getString("GenerateCsvManifest"));
	
	// text field
	
	// combo boxes

	public OptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOptions"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		// load checkboxes	
		rfidCheckBox.setSelected(Setup.isRfidEnabled());
		routerCheckBox.setSelected(Setup.isCarRoutingEnabled());
		carLoggerCheckBox.setSelected(Setup.isCarLoggerEnabled());
		engineLoggerCheckBox.setSelected(Setup.isEngineLoggerEnabled());
		localInterchangeCheckBox.setSelected(Setup.isLocalInterchangeMovesEnabled());
		localSidingCheckBox.setSelected(Setup.isLocalSidingMovesEnabled());
		localYardCheckBox.setSelected(Setup.isLocalYardMovesEnabled());
		trainIntoStagingCheckBox.setSelected(Setup.isTrainIntoStagingCheckEnabled());
		promptTrackStagingCheckBox.setSelected(Setup.isPromptFromStagingEnabled());
		generateCvsManifestCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());

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
		addItemLeft(pBuild, promptTrackStagingCheckBox, 1,5);
		
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
		
		JPanel pOption = new JPanel();
		pOption.setLayout(new GridBagLayout());
		pOption.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptions")));
		addItemLeft (pOption, generateCvsManifestCheckBox, 1,0);
		addItemLeft (pOption, rfidCheckBox, 1,1);
		
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
		if (getHeight()<450)		
			setSize(getWidth(), 450);
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
			Setup.setPromptFromStagingEnabled(promptTrackStagingCheckBox.isSelected());
			// Car routing enabled?
			Setup.setCarRoutingEnabled(routerCheckBox.isSelected());
			// Options
			Setup.setGenerateCsvManifestEnabled(generateCvsManifestCheckBox.isSelected());
			Setup.setRfidEnabled(rfidCheckBox.isSelected());
			// Logging enabled?		
			Setup.setEngineLoggerEnabled(engineLoggerCheckBox.isSelected());
			Setup.setCarLoggerEnabled(carLoggerCheckBox.isSelected());
			OperationsSetupXml.instance().writeOperationsFile();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OptionFrame.class.getName());
}

// BuildReportOptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of print options
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 * @version $Revision: 21643 $
 */

public class BuildReportOptionFrame extends OperationsFrame{

	// major buttons	
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// radio buttons		    
    JRadioButton buildReportMin = new JRadioButton(Bundle.getMessage("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton buildReportMax = new JRadioButton(Bundle.getMessage("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(Bundle.getMessage("VeryDetailed"));
    
    // check boxes
	JCheckBox buildReportCheckBox = new JCheckBox(Bundle.getMessage("BuildReportEdit"));
	JCheckBox buildReportIndentCheckBox = new JCheckBox(Bundle.getMessage("BuildReportIndent"));
	
	// combo boxes
	JComboBox fontSizeComboBox = new JComboBox();

	public BuildReportOptionFrame() {
		super(Bundle.getMessage("TitleBuildReportOptions"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state

		// add tool tips
		saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
		buildReportCheckBox.setToolTipText(Bundle.getMessage("CreatesTextFileTip"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			
		// build report
		JPanel pReport = new JPanel();
		pReport.setLayout(new GridBagLayout());		
		pReport.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutReportOptions")));
		
		// build report options
		addItemWidth (pReport, buildReportCheckBox, 3, 1, 1);	
		addItemWidth (pReport, buildReportIndentCheckBox, 3, 1, 2);

		JPanel pFontSize = new JPanel();
		pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
		pFontSize.add(fontSizeComboBox);
		
		JPanel pLevel = new JPanel();
		pLevel.setLayout(new GridBagLayout());
		pLevel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BuildReport")));
		
		// build report level radio buttons
		addItemLeft (pLevel, buildReportMin, 1, 0);
		addItemLeft (pLevel, buildReportNor, 2, 0);
		addItemLeft (pLevel, buildReportMax, 3, 0);
		addItemLeft (pLevel, buildReportVD, 4, 0);

		// controls
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);
		
		getContentPane().add(pReport);
		getContentPane().add(pLevel);
		getContentPane().add(pFontSize);
		getContentPane().add(pControl);
		
		buildReportCheckBox.setSelected(Setup.isBuildReportEditorEnabled());
		buildReportIndentCheckBox.setSelected(Setup.isBuildReportIndentEnabled());
		
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		setBuildReportRadioButton();
		
		// load font sizes 7 through 14
		for (int i = 7; i < 15; i++)
			fontSizeComboBox.addItem(i);
		fontSizeComboBox.setSelectedItem(Setup.getBuildReportFontSize());
		
		addButtonAction(saveButton);

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_BuildReportDetails", true); // NOI18N

		pack();
		setVisible(true);
	}
	
	// Add Remove Logo and Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {		
		if (ae.getSource() == saveButton){
			// font size
			Setup.setBuildReportFontSize((Integer) fontSizeComboBox.getSelectedItem());
			// build report level
			if (buildReportMin.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_MINIMAL);
			else if (buildReportNor.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
			else if (buildReportMax.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
			else if (buildReportVD.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
			Setup.setBuildReportEditorEnabled(buildReportCheckBox.isSelected());
			Setup.setBuildReportIndentEnabled(buildReportIndentCheckBox.isSelected());
			
			OperationsSetupXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}
	
	private void setBuildReportRadioButton(){
		buildReportMin.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL));
		buildReportNor.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL));
		buildReportMax.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED));
		buildReportVD.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED));
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OperationsSetupFrame.class.getName());
}

// BuildReportOptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of print options
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 * @version $Revision: 21643 $
 */

public class BuildReportOptionFrame extends OperationsFrame{
	
	// labels
	JLabel textBuildReport = new JLabel(Bundle.getString("BuildReport"));

	// major buttons	
	JButton saveButton = new JButton(Bundle.getString("Save"));

	// radio buttons		    
    JRadioButton buildReportMin = new JRadioButton(Bundle.getString("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(Bundle.getString("Normal"));
    JRadioButton buildReportMax = new JRadioButton(Bundle.getString("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(Bundle.getString("VeryDetailed"));
    
    // check boxes
	JCheckBox buildReportCheckBox = new JCheckBox(Bundle.getString("BuildReportEdit"));
	JCheckBox buildReportIndentCheckBox = new JCheckBox(Bundle.getString("BuildReportIndent"));
	
	

	public BuildReportOptionFrame() {
		super(Bundle.getString("TitleBuildReportOptions"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state

		// add tool tips
		saveButton.setToolTipText(Bundle.getString("SaveToolTip"));
		buildReportCheckBox.setToolTipText(Bundle.getString("CreatesTextFileTip"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			
		// build report
		JPanel pReport = new JPanel();
		pReport.setLayout(new GridBagLayout());		
		pReport.setBorder(BorderFactory.createTitledBorder(Bundle.getString("BorderLayoutReportOptions")));
		// build report options
		addItem (pReport, textBuildReport, 0, 0);
		addItemLeft (pReport, buildReportMin, 1, 0);
		addItemLeft (pReport, buildReportNor, 2, 0);
		addItemLeft (pReport, buildReportMax, 3, 0);
		addItemLeft (pReport, buildReportVD, 4, 0);
		addItemWidth (pReport, buildReportCheckBox, 3, 1, 1);	
		addItemWidth (pReport, buildReportIndentCheckBox, 3, 1, 2);

		// controls
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);
		
		getContentPane().add(pReport);
		getContentPane().add(pControl);
		
		buildReportCheckBox.setSelected(Setup.isBuildReportEditorEnabled());
		buildReportIndentCheckBox.setSelected(Setup.isBuildReportIndentEnabled());
		
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		setBuildReportRadioButton();
		
		addButtonAction(saveButton);

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_BuildReportDetails", true);

		pack();
		setVisible(true);
	}
	
	// Add Remove Logo and Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {		
		if (ae.getSource() == saveButton){
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

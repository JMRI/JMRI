// BuildReportOptionFrame.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of the build report options
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 * @version $Revision: 21643 $
 */

public class BuildReportOptionFrame extends OperationsFrame {

	// major buttons
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// radio buttons
	JRadioButton buildReportMin = new JRadioButton(Bundle.getMessage("Minimal"));
	JRadioButton buildReportNor = new JRadioButton(Bundle.getMessage("Normal"));
	JRadioButton buildReportMax = new JRadioButton(Bundle.getMessage("Detailed"));
	JRadioButton buildReportVD = new JRadioButton(Bundle.getMessage("VeryDetailed"));

	JRadioButton buildReportRouterNor = new JRadioButton(Bundle.getMessage("Normal"));
	JRadioButton buildReportRouterMax = new JRadioButton(Bundle.getMessage("Detailed"));
	JRadioButton buildReportRouterVD = new JRadioButton(Bundle.getMessage("VeryDetailed"));

	// check boxes
	JCheckBox buildReportCheckBox = new JCheckBox(Bundle.getMessage("BuildReportEdit"));
	JCheckBox buildReportIndentCheckBox = new JCheckBox(Bundle.getMessage("BuildReportIndent"));
	JCheckBox buildReportAlwaysPreviewCheckBox = new JCheckBox(Bundle.getMessage("BuildReportAlwaysPreview"));

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
		addItemWidth(pReport, buildReportCheckBox, 3, 1, 1);
		addItemWidth(pReport, buildReportIndentCheckBox, 3, 1, 2);
		addItemWidth(pReport, buildReportAlwaysPreviewCheckBox, 3, 1, 3);

		JPanel pFontSize = new JPanel();
		pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
		pFontSize.add(fontSizeComboBox);

		JPanel pLevel = new JPanel();
		pLevel.setLayout(new GridBagLayout());
		pLevel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BuildReport")));

		// build report level radio buttons
		addItemLeft(pLevel, buildReportMin, 1, 0);
		addItemLeft(pLevel, buildReportNor, 2, 0);
		addItemLeft(pLevel, buildReportMax, 3, 0);
		addItemLeft(pLevel, buildReportVD, 4, 0);

		JPanel pRouterLevel = new JPanel();
		pRouterLevel.setLayout(new GridBagLayout());
		pRouterLevel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BuildReportRouter")));

		// build report level radio buttons
		addItemLeft(pRouterLevel, buildReportRouterNor, 2, 0);
		addItemLeft(pRouterLevel, buildReportRouterMax, 3, 0);
		addItemLeft(pRouterLevel, buildReportRouterVD, 4, 0);

		// controls
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);

		getContentPane().add(pReport);
		getContentPane().add(pLevel);
		getContentPane().add(pRouterLevel);
		getContentPane().add(pFontSize);
		getContentPane().add(pControl);

		buildReportCheckBox.setSelected(Setup.isBuildReportEditorEnabled());
		buildReportIndentCheckBox.setSelected(Setup.isBuildReportIndentEnabled());
		buildReportIndentCheckBox.setEnabled(buildReportCheckBox.isSelected());
		buildReportAlwaysPreviewCheckBox.setSelected(Setup.isBuildReportAlwaysPreviewEnabled());

		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);

		ButtonGroup buildReportRouterGroup = new ButtonGroup();
		buildReportRouterGroup.add(buildReportRouterNor);
		buildReportRouterGroup.add(buildReportRouterMax);
		buildReportRouterGroup.add(buildReportRouterVD);

		setBuildReportRadioButton();
		setBuildReportRouterRadioButton();

		// load font sizes 5 through 14
		for (int i = 5; i < 15; i++)
			fontSizeComboBox.addItem(i);
		fontSizeComboBox.setSelectedItem(Setup.getBuildReportFontSize());

		addButtonAction(saveButton);
		addCheckBoxAction(buildReportCheckBox);
		
		addRadioButtonAction(buildReportMin);
		addRadioButtonAction(buildReportNor);
		addRadioButtonAction(buildReportMax);
		addRadioButtonAction(buildReportVD);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_BuildReportDetails", true); // NOI18N

		initMinimumSize();
	}

	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			
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

			// router build report level
			String oldReportLevel = Setup.getRouterBuildReportLevel();			
			if (buildReportRouterNor.isSelected())
				Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
			else if (buildReportRouterMax.isSelected())
				Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
			else if (buildReportRouterVD.isSelected())
				Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
			
			if (!oldReportLevel.equals(Setup.getRouterBuildReportLevel())) {
				JOptionPane.showMessageDialog(this, Bundle.getMessage("buildReportRouter"), Bundle
						.getMessage("buildReportRouterTitle"), JOptionPane.INFORMATION_MESSAGE);
			}

			Setup.setBuildReportEditorEnabled(buildReportCheckBox.isSelected());
			Setup.setBuildReportIndentEnabled(buildReportIndentCheckBox.isSelected());
			Setup.setBuildReportAlwaysPreviewEnabled(buildReportAlwaysPreviewCheckBox.isSelected());

			OperationsSetupXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		buildReportIndentCheckBox.setEnabled(buildReportCheckBox.isSelected());
	}
	
	protected void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		setBuildReportRouterRadioButton();	// enable detailed and very detailed if needed
	}

	private void setBuildReportRadioButton() {
		buildReportMin.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL));
		buildReportNor.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL));
		buildReportMax.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED));
		buildReportVD.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED));
	}

	private void setBuildReportRouterRadioButton() {		
		// Enabled for the router only if the build report is very detailed
		buildReportRouterNor.setEnabled(buildReportVD.isSelected());
		buildReportRouterMax.setEnabled(buildReportVD.isSelected());
		buildReportRouterVD.setEnabled(buildReportVD.isSelected());

		buildReportRouterMax.setSelected(Setup.getRouterBuildReportLevel()
				.equals(Setup.BUILD_REPORT_DETAILED));
		buildReportRouterVD.setSelected(Setup.getRouterBuildReportLevel().equals(
				Setup.BUILD_REPORT_VERY_DETAILED));
		buildReportRouterNor.setSelected(Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)
				|| !buildReportVD.isSelected());
	}

	static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class.getName());
}

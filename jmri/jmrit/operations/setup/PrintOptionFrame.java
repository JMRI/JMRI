// PrintOptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of print options
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.2 $
 */

public class PrintOptionFrame extends OperationsFrame{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	JLabel textFont = new JLabel(rb.getString("Font"));
	JLabel showCar = new JLabel(rb.getString("ShowCar"));
	JLabel textDropColor = new JLabel(rb.getString("DropColor"));
	JLabel textPickupColor = new JLabel(rb.getString("PickupColor"));
	JLabel textBuildReport = new JLabel(rb.getString("BuildReport"));
	JLabel textManifest = new JLabel(rb.getString("Manifest"));
	JLabel textPad = new JLabel("   ");

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));

	// radio buttons		
    JRadioButton mono = new JRadioButton(rb.getString("Monospaced"));
    JRadioButton sanSerif = new JRadioButton(rb.getString("SansSerif"));
    
    JRadioButton buildReportMin = new JRadioButton(rb.getString("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(rb.getString("Normal"));
    JRadioButton buildReportMax = new JRadioButton(rb.getString("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(rb.getString("VeryDetailed"));
    
    // check boxes
	JCheckBox showLengthCheckBox = new JCheckBox(rb.getString("Length"));
	JCheckBox showLoadCheckBox = new JCheckBox(rb.getString("Load"));
	JCheckBox showColorCheckBox = new JCheckBox(rb.getString("Color"));
	JCheckBox showDestinationCheckBox = new JCheckBox(rb.getString("Destination"));
	JCheckBox appendCommentCheckBox = new JCheckBox(rb.getString("Comment"));
	
	// text field
	
	// combo boxes
	JComboBox pickupComboBox = Setup.getPrintColorComboBox();
	JComboBox dropComboBox = Setup.getPrintColorComboBox();

	public PrintOptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitlePrintOptions"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		// load checkboxes		
		showLengthCheckBox.setSelected(Setup.isShowCarLengthEnabled());
		showLoadCheckBox.setSelected(Setup.isShowCarLoadEnabled());
		showColorCheckBox.setSelected(Setup.isShowCarColorEnabled());
		showDestinationCheckBox.setSelected(Setup.isShowCarDestinationEnabled());		
		appendCommentCheckBox.setSelected(Setup.isAppendCarCommentEnabled());

		// add tool tips
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
			
		// Manifest panel
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel pManifest = new JPanel();
		pManifest.setLayout(new GridBagLayout());
		JScrollPane pManifestPane = new JScrollPane(pManifest);
		pManifestPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutManifestOptions")));
		ButtonGroup printerGroup = new ButtonGroup();
		printerGroup.add(mono);
		printerGroup.add(sanSerif);
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		// manifest options
		JPanel pReport = new JPanel();
		pReport.setLayout(new GridBagLayout());
		JScrollPane pReportPane = new JScrollPane(pReport);
		pReportPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutReportOptions")));
		addItem (pManifest, textFont, 0, 8);
		addItem (pManifest, textPad, 1, 8);
		addItemLeft (pManifest, mono, 2, 8);
		addItemLeft (pManifest, sanSerif, 3, 8);
		addItem (pManifest, showCar, 0, 12);
		addItemLeft (pManifest, showLengthCheckBox, 2, 12);
		addItemLeft (pManifest, showLoadCheckBox, 3, 12);
		addItemLeft (pManifest, showColorCheckBox, 4, 12);
		addItemLeft (pManifest, showDestinationCheckBox, 5, 12);
		addItemLeft (pManifest, appendCommentCheckBox, 6, 12);
		// drop and pickup color options
		addItemLeft (pManifest, textDropColor, 0, 14);
		addItemLeft (pManifest, dropComboBox, 2, 14);
		addItemLeft (pManifest, textPickupColor, 0, 16);		
		addItemLeft (pManifest, pickupComboBox, 2, 16);
		dropComboBox.setSelectedItem(Setup.getDropTextColor());
		pickupComboBox.setSelectedItem(Setup.getPickupTextColor());
		
		// build report options
		addItem (pReport, textBuildReport, 0, 16);
		addItemLeft (pReport, buildReportMin, 1, 16);
		addItemLeft (pReport, buildReportNor, 2, 16);
		addItemLeft (pReport, buildReportMax, 3, 16);
		addItemLeft (pReport, buildReportVD, 4, 16);
		
		setPrinterFontRadioButton();
		setBuildReportRadioButton();

		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(pManifestPane);
		getContentPane().add(pReportPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(saveButton);


		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true);

		pack();
		setSize(getWidth(), getHeight()+25);	// pad out a bit
		setVisible(true);
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			if (mono.isSelected())
				Setup.setFontName(Setup.MONOSPACED);
			else
				Setup.setFontName(Setup.SANSERIF);
			// drop and pickup color option
			Setup.setDropTextColor((String)dropComboBox.getSelectedItem());
			Setup.setPickupTextColor((String)pickupComboBox.getSelectedItem());
			// show car attributes
			Setup.setShowCarLengthEnabled(showLengthCheckBox.isSelected());
			Setup.setShowCarLoadEnabled(showLoadCheckBox.isSelected());
			Setup.setShowCarColorEnabled(showColorCheckBox.isSelected());
			Setup.setShowCarDestinationEnabled(showDestinationCheckBox.isSelected());
			// append car comment
			Setup.setAppendCarCommentEnabled(appendCommentCheckBox.isSelected());
			// build report level
			if (buildReportMin.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_MINIMAL);
			else if (buildReportNor.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
			else if (buildReportMax.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
			else if (buildReportVD.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
			OperationsXml.instance().writeOperationsFile();
		}
	}
	
	private void setPrinterFontRadioButton(){
		mono.setSelected(Setup.getFontName().equals(Setup.MONOSPACED));
		sanSerif.setSelected(Setup.getFontName().equals(Setup.SANSERIF));
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

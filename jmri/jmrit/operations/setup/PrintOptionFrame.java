// PrintOptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of print options
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.16 $
 */

public class PrintOptionFrame extends OperationsFrame{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	JLabel textBuildReport = new JLabel(rb.getString("BuildReport"));
	JLabel logoURL = new JLabel("");

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));
	JButton addLogoButton = new JButton(rb.getString("AddLogo"));
	JButton removeLogoButton = new JButton(rb.getString("RemoveLogo"));

	// radio buttons		
    
    JRadioButton buildReportMin = new JRadioButton(rb.getString("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(rb.getString("Normal"));
    JRadioButton buildReportMax = new JRadioButton(rb.getString("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(rb.getString("VeryDetailed"));
    
    // check boxes
    JCheckBox tabFormatCheckBox = new JCheckBox(rb.getString("TabFormat"));
	JCheckBox buildReportCheckBox = new JCheckBox(rb.getString("BuildReportEdit"));
	JCheckBox printLocCommentsCheckBox = new JCheckBox(rb.getString("PrintLocationComments"));
	JCheckBox printLoadsEmptiesCheckBox = new JCheckBox(rb.getString("PrintLoadsEmpties"));
	
	// text field
	
	// text area
	JTextArea commentTextArea	= new JTextArea(2,90);
	JScrollPane commentScroller = new JScrollPane(commentTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	Dimension minScrollerDim = new Dimension(700,60);
	
	// combo boxes
	JComboBox fontComboBox = Setup.getFontComboBox();
	JComboBox fontSizeComboBox = new JComboBox();
	JComboBox pickupComboBox = Setup.getPrintColorComboBox();
	JComboBox dropComboBox = Setup.getPrintColorComboBox();
	
	// message formats
	List<JComboBox> enginePickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> engineDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> carPickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> carDropMessageList = new ArrayList<JComboBox>();

	public PrintOptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitlePrintOptions"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state

		// add tool tips
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
		addLogoButton.setToolTipText(rb.getString("AddLogoToolTip"));
		removeLogoButton.setToolTipText(rb.getString("RemoveLogoToolTip"));
		tabFormatCheckBox.setToolTipText(rb.getString("TabComment"));
		printLocCommentsCheckBox.setToolTipText(rb.getString("AddLocationComments"));
		printLoadsEmptiesCheckBox.setToolTipText(rb.getString("LoadsEmptiesComment"));
		buildReportCheckBox.setToolTipText(rb.getString("CreatesTextFile"));
		
		// Manifest panel
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JPanel pManifest = new JPanel();
		pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));
		JScrollPane pManifestPane = new JScrollPane(pManifest);
		pManifestPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutManifestOptions")));
		
		// manifest options
		JPanel pReport = new JPanel();
		pReport.setLayout(new GridBagLayout());
		JScrollPane pReportPane = new JScrollPane(pReport);
		pReportPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutReportOptions")));
		
		// row 1 font type and size
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		JPanel pFont = new JPanel();
		pFont.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFont")));
		pFont.add(fontComboBox);
		JPanel pFontSize = new JPanel();
		pFontSize.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFontSize")));
		pFontSize.add(fontSizeComboBox);

		JPanel pPickupColor = new JPanel();
		pPickupColor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPickupColor")));
		pPickupColor.add( pickupComboBox);
		JPanel pDropColor = new JPanel();
		pDropColor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDropColor")));
		pDropColor.add(dropComboBox);
		
		JPanel pFormat = new JPanel();
		pFormat.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFormat")));
		pFormat.add(tabFormatCheckBox);

		p1.add(pFont);
		p1.add(pFontSize);
		p1.add(pPickupColor);
		p1.add(pDropColor);
		p1.add(pFormat);
		
		// engine message format
		JPanel pEngPickup = new JPanel();
		pEngPickup.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPickupEngine")));
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			JComboBox b = Setup.getEngineMessageComboBox();
			b.setSelectedItem(format[i]);
			pEngPickup.add(b);
			enginePickupMessageList.add(b);
		}
		
		JPanel pEngDrop = new JPanel();
		pEngDrop.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDropEngine")));
		format = Setup.getDropEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			JComboBox b = Setup.getEngineMessageComboBox();
			b.setSelectedItem(format[i]);
			pEngDrop.add(b);
			engineDropMessageList.add(b);
		}
		
		// car pickup message format
		JPanel pPickup = new JPanel();
		pPickup.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPickupCar")));
		String[] pickFormat = Setup.getPickupCarMessageFormat();
		for (int i=0; i<pickFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(pickFormat[i]);
			pPickup.add(b);
			carPickupMessageList.add(b);
		}
			
		// car drop message format
		JPanel pDrop = new JPanel();
		pDrop.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDropCar")));
		String[] dropFormat = Setup.getDropCarMessageFormat();
		for (int i=0; i<dropFormat.length; i++){
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(dropFormat[i]);
			pDrop.add(b);
			carDropMessageList.add(b);
		}
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
		
		// Manifest comments
		JPanel pManifestComment = new JPanel();
		pManifestComment.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutComments")));
		pManifestComment.add(printLocCommentsCheckBox);
		pManifestComment.add(printLoadsEmptiesCheckBox);
		
		// manifest logo
		JPanel pLogo = new JPanel();
		pLogo.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLogo")));
		pLogo.add(removeLogoButton);
		pLogo.add(addLogoButton);
		pLogo.add(logoURL);
		p2.add(pManifestComment);
		p2.add(pLogo);
		
		pManifest.add(p1);
		pManifest.add(pEngPickup);
		pManifest.add(pEngDrop);
		pManifest.add(pPickup);
		pManifest.add(pDrop);
		pManifest.add(p2);
			
		// build report options
		addItem (pReport, textBuildReport, 0, 16);
		addItemLeft (pReport, buildReportMin, 1, 16);
		addItemLeft (pReport, buildReportNor, 2, 16);
		addItemLeft (pReport, buildReportMax, 3, 16);
		addItemLeft (pReport, buildReportVD, 4, 16);
		addItemWidth (pReport, buildReportCheckBox, 3, 1, 17);
		
		// manifest options
		JPanel pComment = new JPanel();
		pComment.setLayout(new GridBagLayout());
		JScrollPane pCommentPane = new JScrollPane(pComment);
		pCommentPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutCommentOptions")));
		addItem (pComment, commentScroller, 0, 0);
				
		// row 11
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);
		
		getContentPane().add(pManifestPane);	
		getContentPane().add(pCommentPane);
		getContentPane().add(pReportPane);
		getContentPane().add(pControl);
		
		tabFormatCheckBox.setSelected(Setup.isTabEnabled());
		printLocCommentsCheckBox.setSelected(Setup.isPrintLocationCommentsEnabled());
		printLoadsEmptiesCheckBox.setSelected(Setup.isPrintLoadsAndEmptiesEnabled());
		buildReportCheckBox.setSelected(Setup.isBuildReportEditorEnabled());
		
		updateLogoButtons();
		dropComboBox.setSelectedItem(Setup.getDropTextColor());
		pickupComboBox.setSelectedItem(Setup.getPickupTextColor());		

		commentTextArea.setText(Setup.getMiaComment());
		
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		// load font sizes 7 through 12
		fontSizeComboBox.addItem(7);
		fontSizeComboBox.addItem(8);
		fontSizeComboBox.addItem(9);
		fontSizeComboBox.addItem(10);
		fontSizeComboBox.addItem(11);
		fontSizeComboBox.addItem(12);
		fontSizeComboBox.setSelectedItem(Setup.getFontSize());
		fontComboBox.setSelectedItem(Setup.getFontName());

		// setup buttons
		addButtonAction(addLogoButton);
		addButtonAction(removeLogoButton);
		addButtonAction(saveButton);
		
		setBuildReportRadioButton();

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_PrintOptions", true);

		pack();
		//setSize(getWidth(), getHeight()+55);	// pad out a bit
		setVisible(true);
	}
	
	// Add Remove Logo and Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLogoButton){
			log.debug("add logo button pressed");
			File f = selectFile();
			if (f != null)
				Setup.setManifestLogoURL(f.getAbsolutePath());
			updateLogoButtons();
		}
		if (ae.getSource() == removeLogoButton){
			log.debug("remove logo button pressed");
			Setup.setManifestLogoURL("");
			updateLogoButtons();
		}
		if (ae.getSource() == saveButton){
			// font name
			Setup.setFontName((String)fontComboBox.getSelectedItem());
			// font size
			Setup.setFontSize((Integer)fontSizeComboBox.getSelectedItem());
			// drop and pickup color option
			Setup.setDropTextColor((String)dropComboBox.getSelectedItem());
			Setup.setPickupTextColor((String)pickupComboBox.getSelectedItem());
			// save engine message format
			String[] format = new String[enginePickupMessageList.size()];
			for (int i=0; i<enginePickupMessageList.size(); i++){
				JComboBox b = enginePickupMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setPickupEngineMessageFormat(format);
			format = new String[engineDropMessageList.size()];
			for (int i=0; i<engineDropMessageList.size(); i++){
				JComboBox b = engineDropMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setDropEngineMessageFormat(format);
			// save car pickup message format
			format = new String[carPickupMessageList.size()];
			for (int i=0; i<carPickupMessageList.size(); i++){
				JComboBox b = carPickupMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setPickupCarMessageFormat(format);
			// save car drop message format
			format = new String[carDropMessageList.size()];
			for (int i=0; i<carDropMessageList.size(); i++){
				JComboBox b = carDropMessageList.get(i);
				format[i] = (String)b.getSelectedItem();
			}
			Setup.setDropCarMessageFormat(format);
			// misplaced car comment
			Setup.setMiaComment(commentTextArea.getText());
			// build report level
			if (buildReportMin.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_MINIMAL);
			else if (buildReportNor.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
			else if (buildReportMax.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
			else if (buildReportVD.isSelected())
				Setup.setBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
			Setup.setTabEnabled(tabFormatCheckBox.isSelected());
			Setup.setPrintLocationCommentsEnabled(printLocCommentsCheckBox.isSelected());
			Setup.setPrintLoadsAndEmptiesEnabled(printLoadsEmptiesCheckBox.isSelected());
			Setup.setBuildReportEditorEnabled(buildReportCheckBox.isSelected());
			OperationsSetupXml.instance().writeOperationsFile();
			// Check font if user selected tab output
			if (Setup.isTabEnabled() && (!Setup.getFontName().equals(Setup.COURIER) && !Setup.getFontName().equals(Setup.MONOSPACED))){
				JOptionPane.showMessageDialog(this,
						rb.getString("TabWorksBest"), rb.getString("ChangeFont"),
						JOptionPane.WARNING_MESSAGE);				
			}
		}
	}

	/**
	 * We always use the same file chooser in this class, so that the user's
	 * last-accessed directory remains available.
	 */
	JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("Images");

	private File selectFile() {
		if (fc==null) {
			log.error("Could not find user directory");
		} else {
			fc.setDialogTitle("Find desired image");
			// when reusing the chooser, make sure new files are included
			fc.rescanCurrentDirectory();
		}

		int retVal = fc.showOpenDialog(null);
		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		return null;
	}

	private void updateLogoButtons(){
		boolean flag = Setup.getManifestLogoURL().equals("");
		addLogoButton.setVisible(flag);
		removeLogoButton.setVisible(!flag);
		logoURL.setText(Setup.getManifestLogoURL());
		pack();
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

// OperationsSetupFrame.java

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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;


/**
 * Frame for user edit of operation parameters
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.19 $
 */

public class OperationsSetupFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	JLabel textScale = new JLabel();
	JLabel textCarType = new JLabel();
	JLabel textRailroadName = new JLabel();
	JLabel textDirection = new JLabel();
	JLabel textMaxTrain = new JLabel();
	JLabel textMaxEngine = new JLabel();
	JLabel textMoveTime = new JLabel();
	JLabel textTravelTime = new JLabel();
	JLabel textOwner = new JLabel();
	JLabel textPrinter = new JLabel();
	JLabel textBuildReport = new JLabel();
	JLabel textPanel = new JLabel();
	JLabel textIconNorth = new JLabel();
	JLabel textIconSouth = new JLabel();
	JLabel textIconEast = new JLabel();
	JLabel textIconWest = new JLabel();
	JLabel textIconLocal = new JLabel();
	JLabel textIconTerminate = new JLabel();
	JLabel textComment = new JLabel();

	// major buttons
	
	JButton backupButton = new JButton();
	JButton restoreButton = new JButton();
	JButton saveButton = new JButton();

	// radio buttons
	
    JRadioButton scaleZ = new JRadioButton("Z");
    JRadioButton scaleN = new JRadioButton("N");
    JRadioButton scaleTT = new JRadioButton("TT");
    JRadioButton scaleHOn3 = new JRadioButton("HOn3");
    JRadioButton scaleOO = new JRadioButton("OO");
    JRadioButton scaleHO = new JRadioButton("HO");
    JRadioButton scaleSn3 = new JRadioButton("Sn3");
    JRadioButton scaleS = new JRadioButton("S");
    JRadioButton scaleOn3 = new JRadioButton("On3");
    JRadioButton scaleO = new JRadioButton("O");
    JRadioButton scaleG = new JRadioButton("G");
    
    JRadioButton typeDesc = new JRadioButton(rb.getString("Descriptive"));
    JRadioButton typeAAR = new JRadioButton(rb.getString("AAR"));
		
    JRadioButton mono = new JRadioButton(rb.getString("Monospaced"));
    JRadioButton sanSerif = new JRadioButton(rb.getString("SansSerif"));
    
    JRadioButton buildReportMin = new JRadioButton(rb.getString("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(rb.getString("Normal"));
    JRadioButton buildReportMax = new JRadioButton(rb.getString("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(rb.getString("VeryDetailed"));
    
    // check boxes
    JCheckBox eastCheckBox = new JCheckBox();
	JCheckBox northCheckBox = new JCheckBox();
	JCheckBox appendCommentCheckBox = new JCheckBox();
	JCheckBox iconCheckBox = new JCheckBox();
	JCheckBox appendCheckBox = new JCheckBox();
	
	// text field
	JTextField ownerTextField = new JTextField(10);
	JTextField panelTextField = new JTextField(35);
	JTextField railroadNameTextField = new JTextField(35);
	JTextField maxLengthTextField = new JTextField(10);
	JTextField maxEngineSizeTextField = new JTextField(3);
	JTextField switchTimeTextField = new JTextField(3);
	JTextField travelTimeTextField = new JTextField(3);
	JTextField commentTextField = new JTextField(35);

	// for padding out panel
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();
	
	// combo boxes
	JComboBox northComboBox = new JComboBox();
	JComboBox southComboBox = new JComboBox();
	JComboBox eastComboBox = new JComboBox();
	JComboBox westComboBox = new JComboBox();
	JComboBox localComboBox = new JComboBox();
	JComboBox terminateComboBox = new JComboBox();

	public OperationsSetupFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOperationsSetup"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state
		
    	// create manager to load operation settings
		OperationsXml.instance();
		
		textScale.setText(" "+ rb.getString("Scale"));
		textScale.setVisible(true);
		
		textCarType.setText(" "+ rb.getString("CarTypes"));
		textCarType.setVisible(true);
		
		textRailroadName.setText(" " + rb.getString("RailroadName") + " ");
		textRailroadName.setVisible(true);
		railroadNameTextField.setText(Setup.getRailroadName());
		
		textDirection.setText(rb.getString("direction"));
		textDirection.setVisible(true);
		eastCheckBox.setText(rb.getString("eastwest"));
		northCheckBox.setText(rb.getString("northsouth"));
				
		textMaxTrain.setText(rb.getString("MaxLength"));
		textMaxTrain.setVisible(true);
		maxLengthTextField.setText(Integer.toString(Setup.getTrainLength()));
		
		textMaxEngine.setText(rb.getString("MaxEngine"));
		textMaxEngine.setVisible(true);
		maxEngineSizeTextField.setText(Integer.toString(Setup.getEngineSize()));
		
		textMoveTime.setText(rb.getString("MoveTime"));
		textMoveTime.setVisible(true);
		switchTimeTextField.setText(Integer.toString(Setup.getSwitchTime()));
		textTravelTime.setText(rb.getString("TravelTime"));
		textTravelTime.setVisible(true);
		travelTimeTextField.setText(Integer.toString(Setup.getTravelTime()));
		
		textPanel.setText(" "+rb.getString("Panel"));
		textPanel.setVisible(true);
		panelTextField.setText(Setup.getPanelName());
		iconCheckBox.setText(rb.getString("trainIcon"));
		iconCheckBox.setSelected(Setup.isTrainIconCordEnabled());
		appendCheckBox.setText(rb.getString("trainIconAppend"));
		appendCheckBox.setSelected(Setup.isTrainIconAppendEnabled());
		
		textOwner.setText(" "+rb.getString("Owner"));
		textOwner.setVisible(true);
		ownerTextField.setText(Setup.getOwnerName());
		
		textPrinter.setText(rb.getString("PrinterFont"));
		textPrinter.setVisible(true);
		appendCommentCheckBox.setText(rb.getString("CarComment"));
		appendCommentCheckBox.setSelected(Setup.isAppendCarCommentEnabled());
		textBuildReport.setText(rb.getString("BuildReport"));
		textBuildReport.setVisible(true);
				
		textIconNorth.setText(rb.getString("IconNorth"));
		textIconSouth.setText(rb.getString("IconSouth"));
		textIconEast.setText(rb.getString("IconEast"));
		textIconWest.setText(rb.getString("IconWest"));
		textIconLocal.setText(rb.getString("IconLocal"));
		textIconTerminate.setText(rb.getString("IconTerminate"));
		
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		space1.setText("      ");
		space1.setVisible(true);

		backupButton.setText(rb.getString("Backup"));
		backupButton.setToolTipText(rb.getString("BackupToolTip"));
		backupButton.setVisible(true);
		restoreButton.setText(rb.getString("Restore"));
		restoreButton.setToolTipText(rb.getString("RestoreToolTip"));
		restoreButton.setVisible(true);
		saveButton.setText(rb.getString("Save"));
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
		saveButton.setVisible(true);

		// Layout the panel by rows
		// row 1
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		addItem (panel, textRailroadName, 0, 1);
		addItemWidth (panel, railroadNameTextField, 3, 1, 1);
		
		// row 2
		addItem (panel, textDirection, 0, 2);
		addItemLeft (panel, northCheckBox, 1, 2);
		addItemLeft (panel, eastCheckBox, 2, 2);
		setDirectionCheckBox(Setup.getTrainDirection());
		
		// row 3
		addItem (panel, textMaxTrain, 0, 3);
		addItemLeft (panel, maxLengthTextField, 1, 3);
		
		// row 4
		addItem (panel, textMaxEngine, 0, 4);
		addItemLeft (panel, maxEngineSizeTextField, 1, 4);
		
		// row 5
		addItem (panel, textMoveTime, 0, 5);
		addItemLeft (panel, switchTimeTextField, 1, 5);
		
		// row 6
		addItem (panel, textTravelTime, 0, 6);
		addItemLeft (panel, travelTimeTextField, 1, 6);
		
		// row 7
		JPanel p = new JPanel();

		ButtonGroup scaleGroup = new ButtonGroup();
		scaleGroup.add(scaleZ);
		scaleGroup.add(scaleN);
		scaleGroup.add(scaleTT);
		scaleGroup.add(scaleHOn3);
		scaleGroup.add(scaleOO);
		scaleGroup.add(scaleHO);
		scaleGroup.add(scaleSn3);
		scaleGroup.add(scaleS);
		scaleGroup.add(scaleOn3);
		scaleGroup.add(scaleO);
		scaleGroup.add(scaleG);
		
		p.add(scaleZ);
		p.add(scaleN);
		p.add(scaleTT);
		p.add(scaleHOn3);
		p.add(scaleOO);
		p.add(scaleHO);
		p.add(scaleSn3);
		p.add(scaleS);
		p.add(scaleOn3);
		p.add(scaleO);
		p.add(scaleG);
		addItem(panel, textScale, 0, 7);
		addItemWidth(panel, p, 3, 1, 7);
		setScale();
		
		// row 9
		JPanel carTypeButtons = new JPanel();
		ButtonGroup carTypeGroup = new ButtonGroup();
		carTypeGroup.add(typeDesc);
		carTypeGroup.add(typeAAR);
		carTypeButtons.add(typeDesc);
		carTypeButtons.add(typeAAR);
		addItem (panel, textCarType, 0, 9);
		addItemWidth(panel, carTypeButtons, 3, 1, 9);
		setCarTypes();
		
		// row 10
		addItem (panel, textOwner, 0, 10);
		addItemLeft (panel, ownerTextField, 1, 10);
		
		Border border = BorderFactory.createEtchedBorder();
		
		// Printer panel
		JPanel pPrinter = new JPanel();
		pPrinter.setLayout(new GridBagLayout());
		ButtonGroup printerGroup = new ButtonGroup();
		printerGroup.add(mono);
		printerGroup.add(sanSerif);
		ButtonGroup buildReportGroup = new ButtonGroup();
		buildReportGroup.add(buildReportMin);
		buildReportGroup.add(buildReportNor);
		buildReportGroup.add(buildReportMax);
		buildReportGroup.add(buildReportVD);
		
		addItem (pPrinter, textPrinter, 0, 8);
		addItemLeft (pPrinter, mono, 1, 8);
		addItemLeft (pPrinter, sanSerif, 2, 8);
		addItem (pPrinter, textBuildReport, 0, 10);
		addItemLeft (pPrinter, buildReportMin, 1, 10);
		addItemLeft (pPrinter, buildReportNor, 2, 10);
		addItemLeft (pPrinter, buildReportMax, 3, 10);
		addItemLeft (pPrinter, buildReportVD, 4, 10);
		addItemWidth (pPrinter, appendCommentCheckBox, 3, 1, 12);
		pPrinter.setBorder(border);
		setPrinterFontRadioButton();
		setBuildReportRadioButton();

		// Icon panel
		JPanel pIcon = new JPanel();
		pIcon.setLayout(new GridBagLayout());
		pIcon.setBorder(border);
		addItem (pIcon, textPanel, 0, 1);
		addItemLeft (pIcon, panelTextField, 1, 1);
		addItem (pIcon, iconCheckBox, 0, 2);
		addItem (pIcon, appendCheckBox, 0, 3);
		addItem (pIcon, textIconNorth, 0, 4);
		addItemLeft (pIcon, northComboBox, 1, 4);
		addItem (pIcon, textIconSouth, 0, 5);
		addItemLeft (pIcon, southComboBox, 1, 5);
		addItem (pIcon, textIconEast, 0, 8);
		addItemLeft (pIcon, eastComboBox, 1, 8);
		addItem (pIcon, textIconWest, 0, 9);
		addItemLeft (pIcon, westComboBox, 1, 9);
		addItem (pIcon, textIconLocal, 0, 10);
		addItemLeft (pIcon, localComboBox, 1, 10);
		addItem (pIcon, textIconTerminate, 0, 11);
		addItemLeft (pIcon, terminateComboBox, 1, 11);
		loadIconComboBox(northComboBox);
		loadIconComboBox(southComboBox);
		loadIconComboBox(eastComboBox);
		loadIconComboBox(westComboBox);
		loadIconComboBox(localComboBox);
		loadIconComboBox(terminateComboBox);
		northComboBox.setSelectedItem(Setup.getTrainIconColorNorth());
		southComboBox.setSelectedItem(Setup.getTrainIconColorSouth());
		eastComboBox.setSelectedItem(Setup.getTrainIconColorEast());
		westComboBox.setSelectedItem(Setup.getTrainIconColorWest());
		localComboBox.setSelectedItem(Setup.getTrainIconColorLocal());
		terminateComboBox.setSelectedItem(Setup.getTrainIconColorTerminate());
				
		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, space1, 0, 8);
		
		// row 13
		addItem(pControl, restoreButton, 0, 9);
		addItem(pControl, backupButton, 1, 9);
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(panel);
		getContentPane().add(pPrinter);
		getContentPane().add(pIcon);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(backupButton);
		addButtonAction(restoreButton);
		addButtonAction(saveButton);
		addCheckBoxAction(eastCheckBox);
		addCheckBoxAction(northCheckBox);

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new LoadDemoAction(rb.getString("LoadDemo")));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true);

		// set frame size and location for display
		pack();
		setSize(getWidth()+30, getHeight()+50);
		setVisible(true);
	}
	
	BackupFrame bf = null;
	RestoreFrame rf = null;
	
	// Save, Delete, Add buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == backupButton){
			if (bf != null)
				bf.dispose();
			bf = new BackupFrame();
			bf.initComponents();
		}
		if (ae.getSource() == restoreButton){
			if(rf != null)
				rf.dispose();
			rf = new RestoreFrame();
			rf.initComponents();
		}
		if (ae.getSource() == saveButton){
			String addOwner = ownerTextField.getText();
			if (addOwner.length() > 13){
				JOptionPane.showMessageDialog(this,rb.getString("OwnerText"),
						rb.getString("CanNotAddOwner"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check input fields
			try {
				Integer.parseInt(maxLengthTextField.getText());
				Integer.parseInt(maxEngineSizeTextField.getText());
				Integer.parseInt(switchTimeTextField.getText());
				Integer.parseInt(travelTimeTextField.getText());
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						rb.getString("CanNotAcceptNumber"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// add owner name to setup
			Setup.setOwnerName(addOwner);
			// add owner name to list
			CarOwners.instance().addName(addOwner);
			// set car types
			if (typeDesc.isSelected()){
				if (!Setup.getCarTypes().equals(Setup.DESCRIPTIVE)){
					CarTypes.instance().changeDefaultNames(Setup.DESCRIPTIVE);
					Setup.setCarTypes(Setup.DESCRIPTIVE);
				}
			} else {
				if (!Setup.getCarTypes().equals(Setup.AAR)){
					CarTypes.instance().changeDefaultNames(Setup.AAR);
					Setup.setCarTypes(Setup.AAR);
				}
			}
			// set printer font
			if (mono.isSelected())
				Setup.setFontName(Setup.MONOSPACED);
			else
				Setup.setFontName(Setup.SANSERIF);
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
			// add panel name to setup
			Setup.setPanelName(panelTextField.getText());
			// train Icon X&Y
			Setup.setTrainIconCordEnabled(iconCheckBox.isSelected());
			Setup.setTrainIconAppendEnabled(appendCheckBox.isSelected());
			// save train icon colors
			Setup.setTrainIconColorNorth((String)northComboBox.getSelectedItem());
			Setup.setTrainIconColorSouth((String)southComboBox.getSelectedItem());
			Setup.setTrainIconColorEast((String)eastComboBox.getSelectedItem());
			Setup.setTrainIconColorWest((String)westComboBox.getSelectedItem());
			Setup.setTrainIconColorLocal((String)localComboBox.getSelectedItem());
			Setup.setTrainIconColorTerminate((String)terminateComboBox.getSelectedItem());
			// set train direction
			int direction = 0;
			if (eastCheckBox.isSelected())
				direction = Setup.EAST + Setup.WEST;
			if (northCheckBox.isSelected())
				direction += Setup.NORTH + Setup.SOUTH;
			Setup.setTrainDirection(direction);
			// set max train length
			Setup.setTrainLength(Integer.parseInt(maxLengthTextField.getText()));
			// set max engine length
			Setup.setEngineSize(Integer.parseInt(maxEngineSizeTextField.getText()));
			// set switch time
			Setup.setSwitchTime(Integer.parseInt(switchTimeTextField.getText()));
			// set travel time
			Setup.setTravelTime(Integer.parseInt(travelTimeTextField.getText()));
			// set scale
			if (scaleZ.isSelected())
				Setup.setScale(Setup.Z_SCALE);
			if (scaleN.isSelected())
				Setup.setScale(Setup.N_SCALE);
			if (scaleTT.isSelected())
				Setup.setScale(Setup.TT_SCALE);
			if (scaleOO.isSelected())
				Setup.setScale(Setup.OO_SCALE);
			if (scaleHOn3.isSelected())
				Setup.setScale(Setup.HOn3_SCALE);
			if (scaleHO.isSelected())
				Setup.setScale(Setup.HO_SCALE);
			if (scaleSn3.isSelected())
				Setup.setScale(Setup.Sn3_SCALE);
			if (scaleS.isSelected())
				Setup.setScale(Setup.S_SCALE);
			if (scaleOn3.isSelected())
				Setup.setScale(Setup.On3_SCALE);
			if (scaleO.isSelected())
				Setup.setScale(Setup.O_SCALE);
			if (scaleG.isSelected())
				Setup.setScale(Setup.G_SCALE);
			Setup.setRailroadName(railroadNameTextField.getText());
			OperationsXml.writeOperationsFile();
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == northCheckBox){
			if (!northCheckBox.isSelected()){
				eastCheckBox.setSelected(true);
			}
		}
		if (ae.getSource() == eastCheckBox){
			if (!eastCheckBox.isSelected()){
				northCheckBox.setSelected(true);
			}
		}
		int direction = 0;
		if(eastCheckBox.isSelected()){
			direction += Setup.EAST;
		}
		if(northCheckBox.isSelected()){
			direction += Setup.NORTH;
		}
		setDirectionCheckBox(direction);
		pack();
		//setSize(getWidth(),getHeight()+getHeight()*1/10);
	}
	
	private void setScale(){
		int scale = Setup.getScale();
		switch (scale){
		case Setup.Z_SCALE:
			scaleZ.setSelected(true);
			break;
		case Setup.N_SCALE:
			scaleN.setSelected(true);
			break;
		case Setup.TT_SCALE:
			scaleTT.setSelected(true);
			break;
		case Setup.HOn3_SCALE:
			scaleHOn3.setSelected(true);
			break;
		case Setup.OO_SCALE:
			scaleOO.setSelected(true);
			break;
		case Setup.HO_SCALE:
			scaleHO.setSelected(true);
			break;
		case Setup.Sn3_SCALE:
			scaleSn3.setSelected(true);
			break;
		case Setup.S_SCALE:
			scaleS.setSelected(true);
			break;
		case Setup.On3_SCALE:
			scaleOn3.setSelected(true);
			break;
		case Setup.O_SCALE:
			scaleO.setSelected(true);
			break;
		case Setup.G_SCALE:
			scaleG.setSelected(true);
			break;
		default:
			log.error ("Unknown scale");
		}
	}
	
	private void setCarTypes(){
		typeDesc.setSelected(Setup.getCarTypes().equals(Setup.DESCRIPTIVE));
		typeAAR.setSelected(Setup.getCarTypes().equals(Setup.AAR));
	}
	
	private void setDirectionCheckBox(int direction){
		eastCheckBox.setSelected((direction & Setup.EAST) >0);
		textIconEast.setVisible((direction & Setup.EAST) >0);
		eastComboBox.setVisible((direction & Setup.EAST) >0);
		textIconWest.setVisible((direction & Setup.EAST) >0);
		westComboBox.setVisible((direction & Setup.EAST) >0);
		northCheckBox.setSelected((direction & Setup.NORTH) >0);
		textIconNorth.setVisible((direction & Setup.NORTH) >0);
		northComboBox.setVisible((direction & Setup.NORTH) >0);
		textIconSouth.setVisible((direction & Setup.NORTH) >0);
		southComboBox.setVisible((direction & Setup.NORTH) >0);
	}
	
	private void loadIconComboBox (JComboBox comboBox){
		LocoIcon li = new LocoIcon();
    	String[] colors = li.getLocoColors();
    	for (int i=0; i<colors.length; i++){
    		comboBox.addItem(colors[i]);
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

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("OperationsSetupFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(OperationsSetupFrame.class.getName());
}

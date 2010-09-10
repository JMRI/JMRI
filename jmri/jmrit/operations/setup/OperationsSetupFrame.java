// OperationsSetupFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user edit of operation parameters
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.44 $
 */

public class OperationsSetupFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	JLabel textScale = new JLabel(" "+ rb.getString("Scale"));
	JLabel textCarType = new JLabel(" "+ rb.getString("CarTypes"));
	JLabel textRailroadName = new JLabel(" " + rb.getString("RailroadName") + " ");
	JLabel textDirection = new JLabel(rb.getString("direction"));
	JLabel textMaxTrain = new JLabel(rb.getString("MaxLength"));
	JLabel textMaxEngine = new JLabel(rb.getString("MaxEngine"));
	JLabel textMoveTime = new JLabel(rb.getString("MoveTime"));
	JLabel textTravelTime = new JLabel(rb.getString("TravelTime"));
	JLabel textOwner = new JLabel(" "+rb.getString("Owner"));
	JLabel textPanel = new JLabel(" "+rb.getString("Panel"));
	JLabel textIconNorth = new JLabel(rb.getString("IconNorth"));
	JLabel textIconSouth = new JLabel(rb.getString("IconSouth"));
	JLabel textIconEast = new JLabel(rb.getString("IconEast"));
	JLabel textIconWest = new JLabel(rb.getString("IconWest"));
	JLabel textIconLocal = new JLabel(rb.getString("IconLocal"));
	JLabel textIconTerminate = new JLabel(rb.getString("IconTerminate"));
	JLabel textComment = new JLabel(rb.getString("Comment"));
	JLabel textBuild = new JLabel(rb.getString("BuildOption"));

	// major buttons	
	JButton backupButton = new JButton(rb.getString("Backup"));
	JButton restoreButton = new JButton(rb.getString("Restore"));
	JButton saveButton = new JButton(rb.getString("Save"));

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
    
    JRadioButton buildNormal = new JRadioButton(rb.getString("Normal"));
    JRadioButton buildAggressive = new JRadioButton(rb.getString("Aggressive"));
		    
    // check boxes
    JCheckBox eastCheckBox = new JCheckBox(rb.getString("eastwest"));
	JCheckBox northCheckBox = new JCheckBox(rb.getString("northsouth"));
	JCheckBox mainMenuCheckBox = new JCheckBox(rb.getString("MainMenu"));
	JCheckBox iconCheckBox = new JCheckBox(rb.getString("trainIcon"));
	JCheckBox appendCheckBox = new JCheckBox(rb.getString("trainIconAppend"));
	//JCheckBox rfidCheckBox = new JCheckBox(rb.getString("EnableRfid"));
	
	// text field
	JTextField ownerTextField = new JTextField(10);
	JTextField panelTextField = new JTextField(35);
	JTextField railroadNameTextField = new JTextField(35);
	JTextField maxLengthTextField = new JTextField(10);
	JTextField maxEngineSizeTextField = new JTextField(3);
	JTextField switchTimeTextField = new JTextField(3);
	JTextField travelTimeTextField = new JTextField(3);
	JTextField commentTextField = new JTextField(35);
	
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
		OperationsSetupXml.instance();
		
		// load fields
		railroadNameTextField.setText(Setup.getRailroadName());				
		maxLengthTextField.setText(Integer.toString(Setup.getTrainLength()));
		maxEngineSizeTextField.setText(Integer.toString(Setup.getEngineSize()));
		switchTimeTextField.setText(Integer.toString(Setup.getSwitchTime()));
		travelTimeTextField.setText(Integer.toString(Setup.getTravelTime()));
		panelTextField.setText(Setup.getPanelName());
		ownerTextField.setText(Setup.getOwnerName());

		// load checkboxes
		mainMenuCheckBox.setSelected(Setup.isMainMenuEnabled());
		//rfidCheckBox.setSelected(Setup.isRfidEnabled());
		iconCheckBox.setSelected(Setup.isTrainIconCordEnabled());
		appendCheckBox.setSelected(Setup.isTrainIconAppendEnabled());		

		// add tool tips
		backupButton.setToolTipText(rb.getString("BackupToolTip"));
		restoreButton.setToolTipText(rb.getString("RestoreToolTip"));
		saveButton.setToolTipText(rb.getString("SaveToolTip"));

		// Layout the panel by rows
		// row 1
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		JScrollPane panelPane = new JScrollPane(panel);
		panel.setLayout(new GridBagLayout());
		panelPane.setBorder(BorderFactory.createTitledBorder(""));
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
		
		// row 12 
		JPanel buildButtons = new JPanel();
		ButtonGroup buildGroup = new ButtonGroup();
		buildGroup.add(buildNormal);
		buildGroup.add(buildAggressive);
		buildButtons.add(buildNormal);
		buildButtons.add(buildAggressive);
		addItem (panel, textBuild, 0, 12);
		addItemWidth(panel, buildButtons, 3, 1, 12);
		setBuildOption();
		
		// Option panel
		JPanel options = new JPanel();
		options.setLayout(new GridBagLayout());
		options.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptions")));
		addItem (options, mainMenuCheckBox, 1,7);
		// addItem (options, rfidCheckBox, 1,8);		

		// Icon panel
		JPanel pIcon = new JPanel();
		pIcon.setLayout(new GridBagLayout());	
		JScrollPane pIconPane = new JScrollPane(pIcon);
		pIconPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutPanelOptions")));

		addItem (pIcon, textPanel, 0, 1);
		addItemLeft (pIcon, panelTextField, 1, 1);
		panelTextField.setToolTipText(rb.getString("EnterPanelName"));
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
				
		// row 15
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, restoreButton, 0, 9);
		addItem(pControl, backupButton, 1, 9);
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(panelPane);
		getContentPane().add(options);
		getContentPane().add(pIconPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(backupButton);
		addButtonAction(restoreButton);
		addButtonAction(saveButton);
		addCheckBoxAction(eastCheckBox);
		addCheckBoxAction(northCheckBox);
		addRadioButtonAction(buildNormal);
		addRadioButtonAction(buildAggressive);

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new OptionAction(rb.getString("TitleOptions")));
		toolMenu.add(new PrintOptionAction(rb.getString("TitlePrintOptions")));
		toolMenu.add(new LoadDemoAction(rb.getString("LoadDemo")));
		toolMenu.add(new ResetAction(rb.getString("ResetOperations")));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true);

		// set frame size and location for display
		if (Setup.getOperationsSetupFramePosition()!= null){
			setLocation(Setup.getOperationsSetupFramePosition());
		}	
		packFrame();
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
			if (addOwner.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("OwnerText"), new Object[]{Integer.toString(Control.MAX_LEN_STRING_ATTRIBUTE)}),
						rb.getString("CanNotAddOwner"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check input fields
			try {
				Integer.parseInt(maxLengthTextField.getText());
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, textMaxTrain.getText(),
						rb.getString("CanNotAcceptNumber"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				Integer.parseInt(maxEngineSizeTextField.getText());
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, textMaxEngine.getText(),
						rb.getString("CanNotAcceptNumber"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				Integer.parseInt(switchTimeTextField.getText());
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, textMoveTime.getText(),
						rb.getString("CanNotAcceptNumber"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				Integer.parseInt(travelTimeTextField.getText());
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, textTravelTime.getText(),
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
					CarManagerXml.instance().writeOperationsFile();
				}
			} else {
				if (!Setup.getCarTypes().equals(Setup.AAR)){
					CarTypes.instance().changeDefaultNames(Setup.AAR);
					Setup.setCarTypes(Setup.AAR);
					CarManagerXml.instance().writeOperationsFile();
				}
			}
			// build option
			Setup.setBuildAggressive(buildAggressive.isSelected());
			// main menu enabled?
			Setup.setMainMenuEnabled(mainMenuCheckBox.isSelected());
			// RFID enabled?
			// Setup.setRfidEnabled(rfidCheckBox.isSelected());
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
			// save panel size and position
			Setup.setOperationsSetupFrame(this);
			OperationsSetupXml.instance().writeOperationsFile();
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
		packFrame();
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
	
	private void setBuildOption(){
		buildNormal.setSelected(!Setup.isBuildAggressive());
		buildAggressive.setSelected(Setup.isBuildAggressive());
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
    	String[] colors = LocoIcon.getLocoColors();
    	for (int i=0; i<colors.length; i++){
    		comboBox.addItem(colors[i]);
    	}
	}
		
	private void packFrame(){
		pack();
		if (Setup.getOperationsSetupFrameSize()!= null){
			setSize(Setup.getOperationsSetupFrameSize());
		} else {
			setSize(getWidth(), getHeight()+20);	// made the panel a bit larger to eliminate scroll bars
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("OperationsSetupFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OperationsSetupFrame.class.getName());
}

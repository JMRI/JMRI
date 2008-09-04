// OperationsSetupFrame.java

package jmri.jmrit.operations.setup;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import java.io.*;
import java.util.ResourceBundle;

import jmri.jmrit.operations.cars.CarManagerXml;
import jmri.jmrit.operations.cars.CarOwners;
import jmri.jmrit.operations.OperationsFrame;

import jmri.jmrit.display.LocoIcon;


/**
 * Frame for user edit of car
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */

public class OperationsSetupFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	javax.swing.JLabel textScale = new javax.swing.JLabel();
	javax.swing.JLabel textRailroadName = new javax.swing.JLabel();
	javax.swing.JLabel textDirection = new javax.swing.JLabel();
	javax.swing.JLabel textMaxTrain = new javax.swing.JLabel();
	javax.swing.JLabel textMaxEngine = new javax.swing.JLabel();
	javax.swing.JLabel textOwner = new javax.swing.JLabel();
	javax.swing.JLabel textPrinter = new javax.swing.JLabel();
	javax.swing.JLabel textPanel = new javax.swing.JLabel();
	javax.swing.JLabel textIconNorth = new javax.swing.JLabel();
	javax.swing.JLabel textIconSouth = new javax.swing.JLabel();
	javax.swing.JLabel textIconEast = new javax.swing.JLabel();
	javax.swing.JLabel textIconWest = new javax.swing.JLabel();
	javax.swing.JLabel textIconLocal = new javax.swing.JLabel();
	javax.swing.JLabel textIconTerminate = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	
	javax.swing.JButton saveButton = new javax.swing.JButton();

	// radio buttons
	
    javax.swing.JRadioButton scaleZ = new javax.swing.JRadioButton("Z");
    javax.swing.JRadioButton scaleN = new javax.swing.JRadioButton("N");
    javax.swing.JRadioButton scaleTT = new javax.swing.JRadioButton("TT");
    javax.swing.JRadioButton scaleHOn3 = new javax.swing.JRadioButton("HOn3");
    javax.swing.JRadioButton scaleOO = new javax.swing.JRadioButton("OO");
    javax.swing.JRadioButton scaleHO = new javax.swing.JRadioButton("HO");
    javax.swing.JRadioButton scaleSn3 = new javax.swing.JRadioButton("Sn3");
    javax.swing.JRadioButton scaleS = new javax.swing.JRadioButton("S");
    javax.swing.JRadioButton scaleOn3 = new javax.swing.JRadioButton("On3");
    javax.swing.JRadioButton scaleO = new javax.swing.JRadioButton("O");
    javax.swing.JRadioButton scaleG = new javax.swing.JRadioButton("G");
		
    javax.swing.JRadioButton mono = new javax.swing.JRadioButton(rb.getString("Monospaced"));
    javax.swing.JRadioButton sanSerif = new javax.swing.JRadioButton(rb.getString("SansSerif"));
    
    // check boxes
    
    javax.swing.JCheckBox eastCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox northCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox iconCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox appendCheckBox = new javax.swing.JCheckBox();
	
	// text field
	javax.swing.JTextField ownerTextField = new javax.swing.JTextField(10);
	javax.swing.JTextField panelTextField = new javax.swing.JTextField(35);
	javax.swing.JTextField railroadNameTextField = new javax.swing.JTextField(35);
	javax.swing.JTextField maxLengthTextField = new javax.swing.JTextField(10);
	javax.swing.JTextField maxEngineSizeTextField = new javax.swing.JTextField(3);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo boxes
	javax.swing.JComboBox northComboBox = new javax.swing.JComboBox();
	javax.swing.JComboBox southComboBox = new javax.swing.JComboBox();
	javax.swing.JComboBox eastComboBox = new javax.swing.JComboBox();
	javax.swing.JComboBox westComboBox = new javax.swing.JComboBox();
	javax.swing.JComboBox localComboBox = new javax.swing.JComboBox();
	javax.swing.JComboBox terminateComboBox = new javax.swing.JComboBox();

	public OperationsSetupFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOperationsSetup"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state
		
    	// create manager to load operation settings
		OperationsXml.instance();
		
		textScale.setText(" "+ rb.getString("Scale"));
		textScale.setVisible(true);
		
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

		saveButton.setText(rb.getString("Save"));
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
		addItem(panel, textScale, 0, 5);
		addItemWidth(panel, p, 3, 1, 5);
		setScale();
		
		// row 6
		addItem (panel, textOwner, 0, 6);
		addItemLeft (panel, ownerTextField, 1, 6);
		
		// Printer panel
		ButtonGroup printerGroup = new ButtonGroup();
		printerGroup.add(mono);
		printerGroup.add(sanSerif);
		addItem (panel, textPrinter, 0, 8);
		addItemLeft (panel, mono, 1, 8);
		addItemLeft (panel, sanSerif, 2, 8);
		setPrinterFontRadioButton();

		// Icon panel
		JPanel pIcon = new JPanel();
		pIcon.setLayout(new GridBagLayout());
		Border border = BorderFactory.createEtchedBorder();
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
		addItem(pControl, space1, 0, 8);
		
		// row 13
		addItem(pControl, saveButton, 2, 9);
		
		getContentPane().add(panel);
		getContentPane().add(pIcon);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(saveButton);
		addCheckBoxAction(eastCheckBox);
		addCheckBoxAction(northCheckBox);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true);

		// set frame size and location for display
		pack();
		//setSize(getWidth(),getHeight()+getHeight()*1/10);
		setVisible(true);
	}
	

	
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	// Save, Delete, Add buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			String addOwner = ownerTextField.getText();
			if (addOwner.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("ownerText"),
						"Can not add owner",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// add owner name to setup
			Setup.setOwnerName(addOwner);
			// add owner name to list
			CarOwners.instance().addName(addOwner);
			// set printer font
			if (mono.isSelected())
				Setup.setFontName(Setup.MONOSPACED);
			else
				Setup.setFontName(Setup.SANSERIF);
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
	
	private void addCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
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

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("OperationsSetupFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(OperationsSetupFrame.class.getName());
}

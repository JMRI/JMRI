/**
 * ConsistToolFrame.java
 *
 * Description:          Frame object for manipulating consists.
 *
 * @author               Paul Bender Copyright (C) 2003
 * @version              $ version 1.00 $
 */


package jmri.jmrit.consisttool;

import jmri.InstanceManager;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.jmrit.roster.*;

import jmri.jmrit.throttle.ThrottleFrameManager;

import java.awt.*;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;

public class ConsistToolFrame extends javax.swing.JFrame {

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(2);
    javax.swing.JComboBox consistAdrBox = new javax.swing.JComboBox();

    javax.swing.JRadioButton isAdvancedConsist = new javax.swing.JRadioButton("Advanced Consist");
    javax.swing.JRadioButton isCSConsist = new javax.swing.JRadioButton("Command Station Consist");

    javax.swing.JButton createButton = new javax.swing.JButton();
    javax.swing.JButton deleteButton = new javax.swing.JButton();
    javax.swing.JButton throttleButton = new javax.swing.JButton();

    javax.swing.JLabel textLoco1Label = new javax.swing.JLabel();
    javax.swing.JTextField loco1TextField = new javax.swing.JTextField(4);
    javax.swing.JComboBox loco1RosterBox;

    javax.swing.JButton addLoco1Button = new javax.swing.JButton();
    javax.swing.JButton removeLoco1Button = new javax.swing.JButton();

    javax.swing.JCheckBox loco1DirectionNormal = new javax.swing.JCheckBox("Direction Normal");

    javax.swing.JLabel textLoco2Label = new javax.swing.JLabel();
    javax.swing.JTextField loco2TextField = new javax.swing.JTextField(4);
    javax.swing.JComboBox loco2RosterBox;

    javax.swing.JButton addLoco2Button = new javax.swing.JButton();
    javax.swing.JButton removeLoco2Button = new javax.swing.JButton();

    javax.swing.JCheckBox loco2DirectionNormal = new javax.swing.JCheckBox("Direction Normal");

    javax.swing.JLabel textLoco3Label = new javax.swing.JLabel();
    javax.swing.JTextField loco3TextField = new javax.swing.JTextField(4);
    javax.swing.JComboBox loco3RosterBox;

    javax.swing.JButton addLoco3Button = new javax.swing.JButton();
    javax.swing.JButton removeLoco3Button = new javax.swing.JButton();

    javax.swing.JCheckBox loco3DirectionNormal = new javax.swing.JCheckBox("Direction Normal");

    javax.swing.JLabel textLoco4Label = new javax.swing.JLabel();
    javax.swing.JTextField loco4TextField = new javax.swing.JTextField(4);
    javax.swing.JComboBox loco4RosterBox;

    javax.swing.JButton addLoco4Button = new javax.swing.JButton();
    javax.swing.JButton removeLoco4Button = new javax.swing.JButton();

    javax.swing.JCheckBox loco4DirectionNormal = new javax.swing.JCheckBox("Direction Normal");

    ConsistManager ConsistMan = null;

    public ConsistToolFrame() {

  	ConsistMan = InstanceManager.consistManagerInstance();

        // configure items for GUI
	

        textAdrLabel.setText("Consist:");
        textAdrLabel.setVisible(true);

        adrTextField.setText("");
        adrTextField.setVisible(true);
        adrTextField.setToolTipText("consist being created or deleted");

	initializeConsistBox();
	
	consistAdrBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			consistSelected();
		}
	});	    

	consistAdrBox.setVisible(false);
	consistAdrBox.setToolTipText("Select an existing Consist");

        isAdvancedConsist.setSelected(true); 
	isAdvancedConsist.setVisible(true);
	isAdvancedConsist.setEnabled(false);
        isCSConsist.setSelected(false);
	isCSConsist.setVisible(true);
	isCSConsist.setEnabled(false);

	if(ConsistMan.isCommandStationConsistPossible())
	{
		isAdvancedConsist.setEnabled(true);
		isCSConsist.setEnabled(true);
	}

        createButton.setText("Create");
        createButton.setVisible(true);
        createButton.setToolTipText("Press to create the consist as specified");
        createButton.addActionListener(new java.awt.event.ActionListener() 
	{
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    createButtonActionPerformed(e);
                }
            });

        deleteButton.setText("Delete");
        deleteButton.setVisible(true);
        deleteButton.setToolTipText("Delete the consist/remove all locomotives");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    deleteButtonActionPerformed(e);
                }
            });

        throttleButton.setText("Throttle");
        throttleButton.setVisible(true);
        throttleButton.setToolTipText("Create the consist AND Start a Throttle for it");
        throttleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    throttleButtonActionPerformed(e);
                }
            });

	// Set up the controlls for the First Locomotive in the consist.

        textLoco1Label.setText("1st locomotive");
        textLoco1Label.setVisible(true);

        loco1TextField.setText("");
        loco1TextField.setVisible(true);
        loco1TextField.setToolTipText("Address of First Locomotive in the consist");

	loco1RosterBox = Roster.instance().matchingComboBox(null,null,null,null,null,null,null);
        loco1RosterBox.insertItemAt("",0);
	loco1RosterBox.setSelectedIndex(0);
	loco1RosterBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			loco1Selected();
		}
	});	    

	loco1RosterBox.setVisible(true);

	loco1DirectionNormal.setToolTipText("Consist Forward is Forward for this locomotive if checked");
	loco1DirectionNormal.setSelected(true);
	loco1DirectionNormal.setVisible(true);
	loco1DirectionNormal.setEnabled(false);

        addLoco1Button.setText("add");
        addLoco1Button.setVisible(true);
        addLoco1Button.setToolTipText("Add to the Consist");
        addLoco1Button.addActionListener(new java.awt.event.ActionListener() 
	{
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addLoco1ButtonActionPerformed(e);
                }
            });

        removeLoco1Button.setText("remove");
        removeLoco1Button.setVisible(true);
        removeLoco1Button.setToolTipText("Remove from the Consist");
        removeLoco1Button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    removeLoco1ButtonActionPerformed(e);
                }
            });

	// Set up the controlls for the Second Locomotive in the consist.

        textLoco2Label.setText("2nd Locomotive");
        textLoco2Label.setVisible(true);

        loco2TextField.setText("");
        loco2TextField.setVisible(true);
        loco2TextField.setToolTipText("Address of Second Locomotive in the consist");

	loco2RosterBox = Roster.instance().matchingComboBox(null,null,null,null,null,null,null);
        loco2RosterBox.insertItemAt("",0);
	loco2RosterBox.setSelectedIndex(0);
	loco2RosterBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			loco2Selected();
		}
	});	    

	loco2RosterBox.setVisible(true);

	loco2DirectionNormal.setToolTipText("Consist Forward is Forward for this locomotive if checked");
	loco2DirectionNormal.setSelected(true);
	loco2DirectionNormal.setVisible(true);

        addLoco2Button.setText("add");
        addLoco2Button.setVisible(true);
        addLoco2Button.setToolTipText("Add to the Consist");
        addLoco2Button.addActionListener(new java.awt.event.ActionListener() 
	{
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addLoco2ButtonActionPerformed(e);
                }
            });

        removeLoco2Button.setText("remove");
        removeLoco2Button.setVisible(true);
        removeLoco2Button.setToolTipText("Remove from the Consist");
        removeLoco2Button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    removeLoco2ButtonActionPerformed(e);
                }
            });

	// Set up the controlls for the Third Locomotive in the consist.

        textLoco3Label.setText("3rd Locomotive");
        textLoco3Label.setVisible(true);

        loco3TextField.setText("");
        loco3TextField.setVisible(true);
        loco3TextField.setToolTipText("Address of Third Locomotive in the consist");

	loco3RosterBox = Roster.instance().matchingComboBox(null,null,null,null,null,null,null);
        loco3RosterBox.insertItemAt("",0);
	loco3RosterBox.setSelectedIndex(0);
	loco3RosterBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			loco3Selected();
		}
	});	    

	loco3RosterBox.setVisible(true);

	loco3DirectionNormal.setToolTipText("Consist Forward is Forward for this locomotive if checked");
	loco3DirectionNormal.setSelected(true);
	loco3DirectionNormal.setVisible(true);

        addLoco3Button.setText("add");
        addLoco3Button.setVisible(true);
        addLoco3Button.setToolTipText("Add to the Consist");
        addLoco3Button.addActionListener(new java.awt.event.ActionListener() 
	{
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addLoco3ButtonActionPerformed(e);
                }
            });

        removeLoco3Button.setText("remove");
        removeLoco3Button.setVisible(true);
        removeLoco3Button.setToolTipText("Remove from the Consist");
        removeLoco3Button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    removeLoco3ButtonActionPerformed(e);
                }
            });

	// Set up the controlls for the Fourth Locomotive in the consist.

        textLoco4Label.setText("4th Locomotive");
        textLoco4Label.setVisible(true);

        loco4TextField.setText("");
        loco4TextField.setVisible(true);
        loco4TextField.setToolTipText("Address of Fourth Locomotive in the consist");

	loco4RosterBox = Roster.instance().matchingComboBox(null,null,null,null,null,null,null);
        loco4RosterBox.insertItemAt("",0);
	loco4RosterBox.setSelectedIndex(0);
	loco4RosterBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			loco4Selected();
		}
	});	    

	loco4RosterBox.setVisible(true);

	loco4DirectionNormal.setToolTipText("Consist Forward is Forward for this locomotive if checked");
	loco4DirectionNormal.setSelected(true);
	loco4DirectionNormal.setVisible(true);

        addLoco4Button.setText("add");
        addLoco4Button.setVisible(true);
        addLoco4Button.setToolTipText("Add to the Consist");
        addLoco4Button.addActionListener(new java.awt.event.ActionListener() 
	{
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addLoco4ButtonActionPerformed(e);
                }
            });

        removeLoco4Button.setText("remove");
        removeLoco4Button.setVisible(true);
        removeLoco4Button.setToolTipText("Remove from the Consist");
        removeLoco4Button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    removeLoco4ButtonActionPerformed(e);
                }
            });


        // general GUI config
        setTitle("Consist Control");
        getContentPane().setLayout(new GridLayout(8,2));

        // install items in GUI
	
	// The address and related buttons are installed in a single pane
	JPanel addressPanel = new JPanel();
	addressPanel.setLayout(new FlowLayout());

        addressPanel.add(textAdrLabel);
        addressPanel.add(adrTextField);
	addressPanel.add(consistAdrBox);
	addressPanel.add(isAdvancedConsist);
	addressPanel.add(isCSConsist);

	getContentPane().add(addressPanel);

	// The address and related buttons for each Locomotive
        // are installed in a single pane


	// Locomotive 1
	JPanel loco1Panel = new JPanel();
	loco1Panel.setLayout(new FlowLayout());
	
        loco1Panel.add(textLoco1Label);
        loco1Panel.add(loco1TextField);
	loco1Panel.add(loco1RosterBox);
	loco1Panel.add(loco1DirectionNormal);

        loco1Panel.add(addLoco1Button);
        loco1Panel.add(removeLoco1Button);

	getContentPane().add(loco1Panel);

	// Locomotive 2
	JPanel loco2Panel = new JPanel();
	loco2Panel.setLayout(new FlowLayout());

        loco2Panel.add(textLoco2Label);
        loco2Panel.add(loco2TextField);
	loco2Panel.add(loco2RosterBox);
	loco2Panel.add(loco2DirectionNormal);

        loco2Panel.add(addLoco2Button);
        loco2Panel.add(removeLoco2Button);

	getContentPane().add(loco2Panel);

	// Locomotive 3
	JPanel loco3Panel = new JPanel();
	loco3Panel.setLayout(new FlowLayout());

        loco3Panel.add(textLoco3Label);
        loco3Panel.add(loco3TextField);
	loco3Panel.add(loco3RosterBox);
	loco3Panel.add(loco3DirectionNormal);

        loco3Panel.add(addLoco3Button);
        loco3Panel.add(removeLoco3Button);

	getContentPane().add(loco3Panel);

	// Locomotive 4
	JPanel loco4Panel = new JPanel();
	loco4Panel.setLayout(new FlowLayout());
        loco4Panel.add(textLoco4Label);
        loco4Panel.add(loco4TextField);
	loco4Panel.add(loco4RosterBox);
	loco4Panel.add(loco4DirectionNormal);

        loco4Panel.add(addLoco4Button);
        loco4Panel.add(removeLoco4Button);

	getContentPane().add(loco4Panel);

	// Set up the Control Button panel
	JPanel controlPanel = new JPanel();
	controlPanel.setLayout(new FlowLayout());

        controlPanel.add(createButton);
        controlPanel.add(deleteButton);
        controlPanel.add(throttleButton);

	getContentPane().add(controlPanel);

        pack();

    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }


    private void initializeConsistBox() {
        consistAdrBox.insertItemAt("",0);
	//consistAdrBox.setSelectedIndex(0);
	//ArrayList existingConsists = ConsistMan.getConsistList();

	//for(int i=0; i<existingConsists.getLength(); i++) {
	//	consistAdrBox.setSelectedIndex(i) = existingConsists[i];
	//}
     }

    public void deleteButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	removeLoco1ButtonActionPerformed(e);
	removeLoco2ButtonActionPerformed(e);
	removeLoco3ButtonActionPerformed(e);
	removeLoco4ButtonActionPerformed(e);
	int address=Integer.parseInt(adrTextField.getText());
	try {
		ConsistMan.delConsist(address);
	    }  catch (Exception ex) { }
	adrTextField.setText("");
        initializeConsistBox();
    	}

    public void createButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	ConsistMan.getConsist(address);
	addLoco1ButtonActionPerformed(e);
	addLoco2ButtonActionPerformed(e);
	addLoco3ButtonActionPerformed(e);
	addLoco4ButtonActionPerformed(e);
	initializeConsistBox();
    }

    public void throttleButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
		}
	  // make sure the consist was created as specified
	  createButtonActionPerformed(e);
	  // Create a throttle object with the selected consist address
	  jmri.jmrit.throttle.ThrottleFrame tf=
		jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
		int address=Integer.parseInt(adrTextField.getText());
		tf.notifyAddressChosen(address);		
		tf.setVisible(true);
    	}

    public void consistSelected() {
	adrTextField.setText("" + consistAdrBox.getSelectedItem());
    }

    public void removeLoco1ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco1TextField.getText().equals("")) { return; }
	int address=Integer.parseInt(adrTextField.getText());
	int loco1address=Integer.parseInt(loco1TextField.getText());
	try {
		ConsistMan.getConsist(address).remove(loco1address);
	    }  catch (Exception ex) { }
	loco1TextField.setText("");
	loco1RosterBox.setSelectedIndex(0);
	}

    public void addLoco1ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco1TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco1address=Integer.parseInt(loco1TextField.getText());
	ConsistMan.getConsist(address).add(loco1address,
					   loco1DirectionNormal.isSelected());
    }

    public void loco1Selected() {
	if (!(loco1RosterBox.getSelectedItem().equals(""))){
           String rosterEntryTitle = loco1RosterBox.getSelectedItem().toString();
           RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		loco1TextField.setText(entry.getDccAddress());
        }

    }

    public void removeLoco2ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco2TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco2address=Integer.parseInt(loco2TextField.getText());
	try {
		ConsistMan.getConsist(address).remove(loco2address);
	    }  catch (Exception ex) { }
	loco2TextField.setText("");
	loco2RosterBox.setSelectedIndex(0);
	}

    public void addLoco2ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco2TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco2address=Integer.parseInt(loco2TextField.getText());
	ConsistMan.getConsist(address).add(loco2address,
					   loco2DirectionNormal.isSelected());
    }

    public void loco2Selected() {
	if (!(loco2RosterBox.getSelectedItem().equals(""))){
           String rosterEntryTitle = loco2RosterBox.getSelectedItem().toString();
           RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		loco2TextField.setText(entry.getDccAddress());
        }

    }

    public void removeLoco3ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco3TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco3address=Integer.parseInt(loco3TextField.getText());
	try {
		ConsistMan.getConsist(address).remove(loco3address);
	    }  catch (Exception ex) { }
	loco3TextField.setText("");
	loco3RosterBox.setSelectedIndex(0);
	}

    public void addLoco3ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco3TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco3address=Integer.parseInt(loco3TextField.getText());
	ConsistMan.getConsist(address).add(loco3address,
					   loco3DirectionNormal.isSelected());
    }

    public void loco3Selected() {
	if (!(loco3RosterBox.getSelectedItem().equals(""))){
           String rosterEntryTitle = loco3RosterBox.getSelectedItem().toString();
           RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		loco3TextField.setText(entry.getDccAddress());
        }

    }

    public void removeLoco4ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco4TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco4address=Integer.parseInt(loco4TextField.getText());
	try {
		ConsistMan.getConsist(address).remove(loco4address);
	    }  catch (Exception ex) { }
	loco4TextField.setText("");
	loco4RosterBox.setSelectedIndex(0);
	}

    public void addLoco4ButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(loco4TextField.getText().equals("")) { return; }
	if(adrTextField.getText().equals("")) { 
           	javax.swing.JOptionPane.showMessageDialog(this, 
						"No Consist Address Selected");
		return; 
	}
	int address=Integer.parseInt(adrTextField.getText());
	int loco4address=Integer.parseInt(loco4TextField.getText());
	ConsistMan.getConsist(address).add(loco4address,
 					   loco4DirectionNormal.isSelected());
    }

    public void loco4Selected() {
	if (!(loco1RosterBox.getSelectedItem().equals(""))){
           String rosterEntryTitle = loco4RosterBox.getSelectedItem().toString();
           RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		loco4TextField.setText(entry.getDccAddress());
        }

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConsistToolFrame.class.getName());

}

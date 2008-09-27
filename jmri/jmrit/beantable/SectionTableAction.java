// SectionTableAction.java

package jmri.jmrit.beantable;

import jmri.Manager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.EntryPoint;
import jmri.Block;
import jmri.BlockManager;
import jmri.Sensor;
import jmri.Path;
import jmri.Transit;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import jmri.util.JmriJFrame;
import jmri.jmrit.display.LayoutEditor;
import java.util.ArrayList;

/**
 * Swing action to create and register a
 * SectionTable GUI.
  <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Dave Duchamp    Copyright (C) 2008
 * @version     $Revision: 1.1 $
 */

public class SectionTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SectionTableAction(String actionName) {
		super(actionName);
		// set manager - no need to use InstanceManager here
		sectionManager = jmri.InstanceManager.sectionManagerInstance();
        // disable ourself if there is no Section manager available
        if (sectionManager==null) {
            setEnabled(false);
        }
    }

    public SectionTableAction() { this(rb.getString("TitleSectionTable"));}
	
	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");


   /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Section objects
     */
    void createModel() {
        m = new BeanTableDataModel() {

			static public final int BEGINBLOCKCOL = NUMCOLUMN;
			static public final int ENDBLOCKCOL = BEGINBLOCKCOL+1;
			static public final int EDITCOL = ENDBLOCKCOL+1;

			public String getValue(String name) {
				return "";
            }
            public Manager getManager() { return jmri.InstanceManager.sectionManagerInstance(); }
            public NamedBean getBySystemName(String name) { 
				return jmri.InstanceManager.sectionManagerInstance().getBySystemName(name);
			}
            public NamedBean getByUserName(String name) { 
				return jmri.InstanceManager.sectionManagerInstance().getByUserName(name);
			}

            public void clickOn(NamedBean t) {
            }

    		public int getColumnCount(){ 
    		    return EDITCOL+1;
     		}

    		public Object getValueAt(int row, int col) {
	   			if (col==BEGINBLOCKCOL) {
            		Section z = (Section)getBySystemName((String)sysNameList.get(row));
                    if (z != null) {
						return z.getBeginBlockName();
                    }
					return "  ";
				}
	   			else if (col==ENDBLOCKCOL) {
            		Section z = (Section)getBySystemName((String)sysNameList.get(row));
                    if (z != null) {
						return z.getEndBlockName();
                    }
					return "  ";
    			}
				else if (col==VALUECOL) {
            		Section z = (Section)getBySystemName((String)sysNameList.get(row));
                    if (z == null) {
						return "";
					}
					else {
						int state = z.getState();
						if (state==Section.FREE) return (rbx.getString("SectionFree"));
						else if (state==Section.FORWARD) return (rbx.getString("SectionForward"));
						else if (state==Section.REVERSE) return (rbx.getString("SectionReverse"));
					}
				}
				else if (col==EDITCOL) return rb.getString("ButtonEdit"); 
				else return super.getValueAt(row, col);
				return null;
			}    		

    		public void setValueAt(Object value, int row, int col) {
        		if ( (col==BEGINBLOCKCOL) || (col==ENDBLOCKCOL) ) {
					return;
				}
				else if (col == EDITCOL) {
					// set up to edit
					String sName = (String) getValueAt(row, SYSNAMECOL);
					editPressed(sName);
				} 
				else super.setValueAt(value, row, col);
    		}

	   		public String getColumnName(int col) {
        		if (col==BEGINBLOCKCOL) return (rbx.getString("SectionFirstBlock"));
        		if (col==ENDBLOCKCOL) return (rbx.getString("SectionLastBlock"));
				if (col==EDITCOL) return "";   // no namne on Edit column
        		return super.getColumnName(col);
        	}

    		public Class getColumnClass(int col) {
				if (col==VALUECOL) return String.class;  // not a button
    			if (col==BEGINBLOCKCOL) return String.class;  // not a button
    			if (col==ENDBLOCKCOL) return String.class;  // not a button
				if (col==EDITCOL) return JButton.class;
    			else return super.getColumnClass(col);
		    }

			public boolean isCellEditable(int row, int col) {
				if (col == BEGINBLOCKCOL) return false;
				if (col == ENDBLOCKCOL) return false;
				if (col == VALUECOL) return false;
				if (col == EDITCOL) return true;
				else return super.isCellEditable(row, col);
			}

    		public int getPreferredWidth(int col) {
				// override default value for SystemName and UserName columns
				if (col == SYSNAMECOL)return new JTextField(9).getPreferredSize().width;
				if (col == USERNAMECOL)return new JTextField(17).getPreferredSize().width;
				if (col == VALUECOL)return new JTextField(6).getPreferredSize().width;
				// new columns
    			if (col==BEGINBLOCKCOL) return new JTextField(15).getPreferredSize().width;
    			if (col==ENDBLOCKCOL) return new JTextField(15).getPreferredSize().width;
    			if (col==EDITCOL) return new JTextField(6).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }

    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }

			boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			    return true;
				// return (e.getPropertyName().indexOf("alue")>=0);
			}
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSectionTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SectionTable";
    }
	
	// instance variables
	ArrayList blockList = new ArrayList();
	BlockTableModel blockTableModel = null;
	EntryPointTableModel entryPointTableModel = null;
	SectionManager sectionManager = null;
	BlockManager blockManager = jmri.InstanceManager.blockManagerInstance();
	boolean editMode = false;
	Section curSection = null;
	boolean addCreateActive = true;
	ArrayList lePanelList = null;
	LayoutEditor curLayoutEditor = null;
	ArrayList blockBoxList = new ArrayList();
	Block beginBlock = null;
	Block endBlock = null;
	Sensor fSensor = null;
	Sensor rSensor = null;
	Sensor fStopSensor = null;
	Sensor rStopSensor = null;
	ArrayList entryPointList = new ArrayList();
	boolean manualEntryPoints = true;
	
	// add/create variables
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JLabel sysNameFixed = new JLabel("");
    JTextField userName = new JTextField(17);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
	JButton create = null;
	JButton update = null;
	JComboBox blockBox = new JComboBox();
	JButton addBlock = null;
	JButton deleteBlocks = null;
	JComboBox layoutEditorBox = new JComboBox();
	JTextField forwardSensorField = new JTextField(12);
	JTextField reverseSensorField = new JTextField(12);
	JTextField forwardStopSensorField = new JTextField(12);
	JTextField reverseStopSensorField = new JTextField(12);
	JRadioButton manually = new JRadioButton(rbx.getString("SetManually"),true);
	JRadioButton automatic = new JRadioButton(rbx.getString("UseConnectivity"),false);
	ButtonGroup entryPointOptions = null;

    /**
	 * Responds to the Add... button and the Edit buttons in Section Table 
	 */
	void addPressed(ActionEvent e) {
		editMode = false;
		if ((blockManager.getSystemNameList().size()) > 0) {
			addEditPressed();
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(null, rbx
					.getString("Message1"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}
	void editPressed(String sName) {
		curSection = sectionManager.getBySystemName(sName);
		if (curSection==null) {
			// no section - should never happen, but protects against a $%^#@ exception
			return;
		}
		sysNameFixed.setText(sName);
		editMode = true;
		addEditPressed();
	}
	void addEditPressed() {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddSection"));   
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SectionAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p = new JPanel(); 
			p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
			p.add(sysNameFixed);
            p.add(sysName);
			sysName.setToolTipText(rbx.getString("SectionSystemNameHint"));
			p.add (new JLabel("     "));
            p.add(userNameLabel);
            p.add(userName);
			userName.setToolTipText(rbx.getString("SectionUserNameHint"));
            addFrame.getContentPane().add(p);
			addFrame.getContentPane().add(new JSeparator());
			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
			JPanel p11 = new JPanel();
			p11.setLayout(new FlowLayout());
			p11.add(new JLabel(rbx.getString("BlockTableMessage")));
			p1.add(p11);
			JPanel p12 = new JPanel();
			// initialize table of blocks
			blockTableModel = new BlockTableModel();
			JTable blockTable = new JTable(blockTableModel);
			blockTable.setRowSelectionAllowed(false);
			blockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(350,100));
			TableColumnModel blockColumnModel = blockTable.getColumnModel();
			TableColumn sNameColumn = blockColumnModel.getColumn(BlockTableModel.SNAME_COLUMN);
			sNameColumn.setResizable(true);
			sNameColumn.setMinWidth(90);
			sNameColumn.setMaxWidth(130);
			TableColumn uNameColumn = blockColumnModel.getColumn(BlockTableModel.UNAME_COLUMN);
			uNameColumn.setResizable(true);
			uNameColumn.setMinWidth(210);
			uNameColumn.setMaxWidth(260);
			JScrollPane blockTableScrollPane = new JScrollPane(blockTable);
			p12.add(blockTableScrollPane, BorderLayout.CENTER);
			p1.add(p12);
			JPanel p13 = new JPanel();
			p13.setLayout(new FlowLayout());
			p13.add (deleteBlocks = new JButton(rbx.getString("DeleteAllBlocksButton")));
            deleteBlocks.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteBlocksPressed(e);
                }
            });
			deleteBlocks.setToolTipText(rbx.getString("DeleteAllBlocksButtonHint"));
			p13.add (new JLabel("     "));
			p13.add (blockBox);
			blockBox.setToolTipText(rbx.getString("BlockBoxHint"));
			p13.add (addBlock = new JButton(rbx.getString("AddBlockButton")));
            addBlock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addBlockPressed(e);
                }
            });
			addBlock.setToolTipText(rbx.getString("AddBlockButtonHint"));			
			p1.add(p13);
			addFrame.getContentPane().add(p1);
			addFrame.getContentPane().add(new JSeparator());
			JPanel p31 = new JPanel();
			p31.setLayout(new FlowLayout());
			p31.add(new JLabel(rbx.getString("EntryPointTable")));
			addFrame.getContentPane().add(p31);
			JPanel p32 = new JPanel();
			p32.setLayout(new FlowLayout());
			entryPointOptions = new ButtonGroup();
			p32.add (manually);
			entryPointOptions.add(manually);
			manually.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					manualEntryPoints = true;
				}
			});
			manually.setToolTipText(rbx.getString("SetManuallyHint"));
			p32.add (new JLabel("   "));
			p32.add (automatic);
			entryPointOptions.add(automatic);
			automatic.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					manualEntryPoints = false;
				}
			});
			automatic.setToolTipText(rbx.getString("SetAutomaticHint"));
			p32.add (layoutEditorBox);
			layoutEditorBox.setToolTipText(rbx.getString("LayoutEditorBoxHint"));
			layoutEditorBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layoutEditorSelectionChanged();
				}
			});
// djd debugging - temporarily hide these items until the automatic setting of entry point direction is ready
//			addFrame.getContentPane().add(p32);
// end djd debugging
			JPanel p33 = new JPanel();
			// initialize table of entry points
			entryPointTableModel = new EntryPointTableModel();
			JTable entryPointTable = new JTable(entryPointTableModel);
			entryPointTable.setRowSelectionAllowed(false);
			entryPointTable.setPreferredScrollableViewportSize(new java.awt.Dimension(400,100));
			TableColumnModel entryPointColumnModel = entryPointTable.getColumnModel();
			TableColumn fromBlockColumn = entryPointColumnModel.getColumn(EntryPointTableModel.BLOCK_COLUMN);
			fromBlockColumn.setResizable(true);
			fromBlockColumn.setMinWidth(250);
			fromBlockColumn.setMaxWidth(310);
			JComboBox directionCombo = new JComboBox();
			directionCombo.addItem(rbx.getString("SectionForward"));
			directionCombo.addItem(rbx.getString("SectionReverse"));
			directionCombo.addItem(rbx.getString("Unknown"));
			TableColumn directionColumn = entryPointColumnModel.getColumn(EntryPointTableModel.DIRECTION_COLUMN);
			directionColumn.setCellEditor(new DefaultCellEditor(directionCombo));
			entryPointTable.setRowHeight(directionCombo.getPreferredSize().height);
			directionColumn.setPreferredWidth(directionCombo.getPreferredSize().width);
			directionColumn.setResizable(false);
			JScrollPane entryPointTableScrollPane = new JScrollPane(entryPointTable);
			p33.add(entryPointTableScrollPane, BorderLayout.CENTER);
			addFrame.getContentPane().add(p33);
			p33.setVisible(true);
			JPanel p34 = new JPanel();
			p34.setLayout(new FlowLayout());
			p34.add(new JLabel(rbx.getString("DirectionNote")));
			addFrame.getContentPane().add(p34);
			addFrame.getContentPane().add(new JSeparator());
			// set up for direction sensors
			JPanel p20 = new JPanel();
			p20.setLayout(new FlowLayout());
			p20.add(new JLabel(rbx.getString("DirectionSensorLabel")));
			addFrame.getContentPane().add(p20);
			JPanel p21 = new JPanel();
			p21.setLayout(new FlowLayout());
			p21.add(new JLabel(rbx.getString("ForwardSensor")));
			p21.add(forwardSensorField);
			forwardSensorField.setToolTipText(rbx.getString("ForwardSensorHint"));
			p21.add (new JLabel("     "));
			p21.add(new JLabel(rbx.getString("ReverseSensor")));
			p21.add(reverseSensorField);
			reverseSensorField.setToolTipText(rbx.getString("ReverseSensorHint"));
			addFrame.getContentPane().add(p21);
			addFrame.getContentPane().add(new JSeparator());
			// set up for stopping sensors
			JPanel p40 = new JPanel();
			p40.setLayout(new FlowLayout());
			p40.add(new JLabel(rbx.getString("StoppingSensorLabel")));
			addFrame.getContentPane().add(p40);
			JPanel p41 = new JPanel();
			p41.setLayout(new FlowLayout());
			p41.add(new JLabel(rbx.getString("ForwardStopSensor")));
			p41.add(forwardStopSensorField);
			forwardStopSensorField.setToolTipText(rbx.getString("ForwardStopSensorHint"));
			p41.add (new JLabel("     "));
			p41.add(new JLabel(rbx.getString("ReverseStopSensor")));
			p41.add(reverseStopSensorField);
			reverseStopSensorField.setToolTipText(rbx.getString("ReverseStopSensorHint"));
			addFrame.getContentPane().add(p41);
			addFrame.getContentPane().add(new JSeparator());
			// set up bottom buttons
			JButton cancel = null;
			JPanel pb = new JPanel();
			pb.setLayout (new FlowLayout());
            pb.add(cancel = new JButton(rb.getString("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
			cancel.setToolTipText(rbx.getString("CancelButtonHint"));
            pb.add(create = new JButton(rb.getString("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
			create.setToolTipText(rbx.getString("SectionCreateButtonHint"));
            pb.add(update = new JButton(rb.getString("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
			update.setToolTipText(rbx.getString("SectionUpdateButtonHint"));
			addFrame.getContentPane().add(pb);
        }
		if (editMode) {
			// setup for edit window
			create.setVisible(false);
			update.setVisible(true);
			sysName.setVisible(false);
			sysNameFixed.setVisible(true);
			initializeEditInformation();
		}
		else {
			// setup for create window
			create.setVisible(true);
			update.setVisible(false);
			sysName.setVisible(true);
			sysNameFixed.setVisible(false);
			clearForCreate();
		}
		// initialize layout editor panels
		if (initializeLayoutEditorCombo()) {
			manually.setVisible(true);
			automatic.setVisible(true);
			layoutEditorBox.setVisible(true);
		}
		else {
			manually.setVisible(false);
			automatic.setVisible(false);
			layoutEditorBox.setVisible(false);
		}
		// initialize block combo - first time
		initializeBlockCombo();
        addFrame.pack();
        addFrame.setVisible(true);
    }
	private void initializeEditInformation() {
		userName.setText(curSection.getUserName());
		deleteBlocksPressed(null);
		int i = 0;
		while (curSection.getBlockBySequenceNumber(i)!=null) {
			Block b = curSection.getBlockBySequenceNumber(i);
			blockList.add((Object)b);
			i++;
			if (blockList.size()==1) {
				beginBlock = b;
			}
			endBlock = b;
		}
		forwardSensorField.setText(curSection.getForwardBlockingSensorName());
		reverseSensorField.setText(curSection.getReverseBlockingSensorName());		
		forwardStopSensorField.setText(curSection.getForwardStoppingSensorName());
		reverseStopSensorField.setText(curSection.getReverseStoppingSensorName());		
		ArrayList list = (ArrayList)curSection.getForwardEntryPointList();
		if (list.size()>0) {
			for (int j = 0; j<list.size(); j++) {
				entryPointList.add(list.get(j));
			}
		}
		list = (ArrayList)curSection.getReverseEntryPointList();
		if (list.size()>0) {
			for (int j = 0; j<list.size(); j++) {
				entryPointList.add(list.get(j));
			}
		}
	}
	private void clearForCreate() {
		deleteBlocksPressed(null);
		curSection =  null;
		forwardSensorField.setText("");
		reverseSensorField.setText("");
		forwardStopSensorField.setText("");
		reverseStopSensorField.setText("");
	}
    void createPressed(ActionEvent e) {
		if (!checkSectionInformation()) {
			return;
		}
        String uName = userName.getText();
        if (uName.equals("")) uName=null;
        String sName = sysName.getText().toUpperCase();
		// attempt to create the new Section
        curSection = sectionManager.createNewSection(sName, uName);
		if (curSection==null) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
					.getString("Message2"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);			
			return;
		}
		sysName.setText(curSection.getSystemName());
		setSectionInformation();
		addFrame.setVisible(false);
    }
	void cancelPressed(ActionEvent e) {
		addFrame.setVisible(false);
	}
    void updatePressed(ActionEvent e) {
		if (!checkSectionInformation()) {
			return;
		}
		// check if user name has been changed
        String uName = userName.getText();
        if (uName.equals("")) uName=null;
		if ( (uName!=null) && (!uName.equals(curSection.getUserName())) ) {
			// check that new user name is unique
			Section tSection = sectionManager.getByUserName(uName);
			if (tSection!=null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message2"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);			
				return;
			}
		}				
		curSection.setUserName(uName);
		if (setSectionInformation()) {
			// successful update
			addFrame.setVisible(false);
		}
    }
	private boolean checkSectionInformation() {
		if (blockList.size()==0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message6"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check entry points
		boolean unknownPresent = false;
		for (int i = 0; i<entryPointList.size(); i++) {
			if ( ((EntryPoint)entryPointList.get(i)).isUnknownType() ) {
				unknownPresent = true;
			}
		}
		if (unknownPresent) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message10"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}			
		// check direction sensors
		String txt = forwardSensorField.getText();
		if ( (txt==null) || (txt.equals("")) ) {
			fSensor = null;
		}
		else {			
			fSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor(txt);
			if (fSensor==null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message7"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else {
				if (!txt.equals(fSensor.getUserName())) {
					forwardSensorField.setText(fSensor.getSystemName());
				}
			}
		}
		txt = reverseSensorField.getText();
		if ( (txt==null) || (txt.equals("")) ) {
			rSensor = null;
		}
		else {			
			rSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor(txt);
			if (rSensor==null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message8"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else {
				if (!txt.equals(rSensor.getUserName())) {
					reverseSensorField.setText(rSensor.getSystemName());
				}
			}
		}
		if ( (fSensor!=null) && (fSensor==rSensor) ) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message9"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check stopping sensors
		txt = forwardStopSensorField.getText();
		if ( (txt==null) || (txt.equals("")) ) {
			fStopSensor = null;
		}
		else {			
			fStopSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor(txt);
			if (fStopSensor==null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message7"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else {
				if (!txt.equals(fStopSensor.getUserName())) {
					forwardStopSensorField.setText(fStopSensor.getSystemName());
				}
			}
		}
		txt = reverseStopSensorField.getText();
		if ( (txt==null) || (txt.equals("")) ) {
			rStopSensor = null;
		}
		else {			
			rStopSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor(txt);
			if (rStopSensor==null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message8"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else {
				if (!txt.equals(rStopSensor.getUserName())) {
					reverseStopSensorField.setText(rSensor.getSystemName());
				}
			}
		}
		return true;
	}
	private boolean setSectionInformation() {
		curSection.removeAllBlocksFromSection();
		for (int i = 0; i<blockList.size(); i++) {
			if (!curSection.addBlock((Block)blockList.get(i))) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message4"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);			
			}
		}
		curSection.setForwardBlockingSensorName(forwardSensorField.getText());		
		curSection.setReverseBlockingSensorName(reverseSensorField.getText());
		curSection.setForwardStoppingSensorName(forwardStopSensorField.getText());		
		curSection.setReverseStoppingSensorName(reverseStopSensorField.getText());
		for (int j = 0; j<entryPointList.size(); j++) {
			EntryPoint ep = (EntryPoint)entryPointList.get(j);
			if (ep.isForwardType()) curSection.addToForwardList(ep);
			else if (ep.isReverseType()) curSection.addToReverseList(ep);
		}
		return true;
	}
	void deleteBlocksPressed(ActionEvent e) {
		for (int j=blockList.size(); j>0; j--) blockList.remove(j-1);
		beginBlock = null;
		endBlock = null;
		initializeBlockCombo();
		initializeEntryPoints();
		blockTableModel.fireTableDataChanged();
	}
	void addBlockPressed(ActionEvent e) {
		if (blockBoxList.size()==0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message5"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);	
			return;		
		}
		int index = blockBox.getSelectedIndex();
		Block b = (Block)blockBoxList.get(index);
		if (b!=null) {
			blockList.add((Object)b);
			if (blockList.size()==1) {
				beginBlock = b;
			}
			endBlock = b;
			initializeBlockCombo();
			initializeEntryPoints();
			blockTableModel.fireTableDataChanged();
		}
	}
	private boolean initializeLayoutEditorCombo() {
		// get list of Layout Editor panels
		lePanelList = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
		if (lePanelList.size()==0) return false;
		layoutEditorBox.removeAllItems();
		layoutEditorBox.addItem(rbx.getString("None"));
		for (int i=0; i<lePanelList.size(); i++) {
			layoutEditorBox.addItem(((LayoutEditor)lePanelList.get(i)).getTitle());
		}
		layoutEditorBox.setSelectedIndex(1);
		return true;
	}
	private void layoutEditorSelectionChanged() {
		int i = layoutEditorBox.getSelectedIndex();
		if ( (i<=0) || (i>lePanelList.size()) ) curLayoutEditor = null;
		else curLayoutEditor = (LayoutEditor)lePanelList.get(i-1);
	}
	private void initializeBlockCombo() {
		ArrayList allBlocks = (ArrayList)blockManager.getSystemNameList();
		blockBox.removeAllItems();
		for (int j=blockBoxList.size(); j>0; j--) blockBoxList.remove(j-1);
		if (blockList.size()==0) {
			// No blocks selected, all blocks are eligible
			for (int i=0; i<allBlocks.size(); i++) {
				String bName = (String)allBlocks.get(i);
				Block b = blockManager.getBySystemName(bName);
				if (b!=null) {
					if ( (b.getUserName()!=null) && (!b.getUserName().equals("")) )
						bName = bName+"( "+b.getUserName()+" )";
					blockBox.addItem(bName);
				    blockBoxList.add((Object)b);
				}
			}
		}
		else {
			// limit to Blocks bonded to the current block that are not already in the Section
			for (int i=0; i<allBlocks.size(); i++) {
				String bName = (String)allBlocks.get(i);
				Block b = blockManager.getBySystemName(bName);
				if (b!=null) {
					if ( (!inSection(b)) && connected(b,endBlock) ) {
						if ( (b.getUserName()!=null) && (!b.getUserName().equals("")) )
							bName = bName+"( "+b.getUserName()+" )";
						blockBox.addItem(bName);
						blockBoxList.add((Object)b);
					}
				}
			}
		}
	}
	private boolean inSection(Block b) {
		for (int i = 0; i<blockList.size(); i++) {
			if (blockList.get(i) == (Object)b) return true;
		}
		return false;
	}
	private boolean connected(Block b1, Block b2) {
		if ( (b1!=null) && (b2!=null) ) {
			ArrayList paths = (ArrayList)b1.getPaths();
			for (int i = 0; i<paths.size(); i++) {
				if (((Path)paths.get(i)).getBlock() == b2) return true;
			}
		}
		return false;
	}
	private void initializeEntryPoints() {
		// Copy old Entry Point List, if there are entries, and clear it.
		ArrayList oldList = new ArrayList();
		for (int i = 0; i<entryPointList.size(); i++) oldList.add(entryPointList.get(i));
		entryPointList.clear();
		if (blockList.size()>0) {
			// cycle through Blocks to find Entry Points
			for (int i = 0; i<blockList.size(); i++) {
				Block sb = (Block)blockList.get(i);
				ArrayList paths = (ArrayList)sb.getPaths();
				for (int j = 0; j<paths.size(); j++) {
					Path p = (Path)paths.get(j);
					if (!inSection(p.getBlock())) {
						// this is path to an outside block, so need an Entry Point
						String pbDir = Path.decodeDirection(p.getFromBlockDirection());
						EntryPoint ep = getEntryPointInList(oldList, sb, p.getBlock(), pbDir);	
						if (ep==null) ep = new EntryPoint(sb, p.getBlock(), pbDir);						
						entryPointList.add(ep);
					}
				}
			}
			// Set directions where possible
			ArrayList epList = getBlockEntryPointsList(beginBlock);
			if ( (epList.size()==2) && (blockList.size()==1) ) {
				if ( (((EntryPoint)epList.get(0)).isUnknownType()) &&
							(((EntryPoint)epList.get(1)).isUnknownType()) ) {
					((EntryPoint)epList.get(0)).setTypeForward();
					((EntryPoint)epList.get(1)).setTypeReverse();
				}
			}
			else if (epList.size()==1) {
				((EntryPoint)epList.get(0)).setTypeForward();
			}
			epList = getBlockEntryPointsList(endBlock);
			if (epList.size()==1) {
				((EntryPoint)epList.get(0)).setTypeReverse();
			}
		}
// debugging		
// here add code to use Layout Editor connectivity		
		entryPointTableModel.fireTableDataChanged();			
	}
	private EntryPoint getEntryPointInList(ArrayList list, Block b, Block pb, String pbDir) {
		for (int i = 0; i<list.size(); i++) {
			EntryPoint ep = (EntryPoint)list.get(i);
			if ( (ep.getBlock()==b) && (ep.getFromBlock()==pb) && 
							(pbDir.equals(ep.getFromBlockDirection())) ) return ep;
		}
		return null;
	}
	private ArrayList getBlockEntryPointsList(Block b) {
		ArrayList list = new ArrayList();
		for (int i = 0; i<entryPointList.size(); i++) {
			EntryPoint ep = (EntryPoint)entryPointList.get(i);
			if (ep.getBlock()==b) list.add((Object)ep);
		}
		return list;
	}
	
    private boolean noWarn = false;
	
	/**
	 * Table model for Blocks in Create/Edit Section window
	 */
	public class BlockTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int SNAME_COLUMN = 0;

		public static final int UNAME_COLUMN = 1;

		public BlockTableModel() {
			super();
			blockManager.addPropertyChangeListener(this);
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				// a new NamedBean is available in the manager
				fireTableDataChanged();
			}
		}

		public Class getColumnClass(int c) {
			return String.class;
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return (blockList.size());
		}

		public boolean isCellEditable(int r, int c) {
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case SNAME_COLUMN:
				return rb.getString("LabelSystemName");
			case UNAME_COLUMN:
				return rb.getString("LabelUserName");
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case SNAME_COLUMN:
				return new JTextField(8).getPreferredSize().width;
			case UNAME_COLUMN:
				return new JTextField(17).getPreferredSize().width;
			default:
				return new JTextField(5).getPreferredSize().width;
			}
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > blockList.size()) {
				return null;
			}
			switch (c) {
				case SNAME_COLUMN:
					return ((Block)(blockList.get(rx))).getSystemName();
				case UNAME_COLUMN: //
					return ((Block)(blockList.get(rx))).getUserName();
				default:
					return rbx.getString("Unknown");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			return;
		}
	}
	
	/**
	 * Table model for Entry Points in Create/Edit Section window
	 */
	public class EntryPointTableModel extends javax.swing.table.AbstractTableModel {

		public static final int BLOCK_COLUMN = 0;

		public static final int DIRECTION_COLUMN = 1;

		public EntryPointTableModel() {
			super();
		}

		public Class getColumnClass(int c) {
			if (c == DIRECTION_COLUMN)
				return JComboBox.class;
			return String.class;
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return (entryPointList.size());
		}

		public boolean isCellEditable(int r, int c) {
			if (c==DIRECTION_COLUMN) {
				if ( !manualEntryPoints )
					return (false);
				else if (r<entryPointList.size()) {
					return (!((EntryPoint)entryPointList.get(r)).isFixed());
				}
				return (true);
			}
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case BLOCK_COLUMN:
				return rbx.getString("FromBlock");
			case DIRECTION_COLUMN:
				return rbx.getString("TravelDirection");
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			if (col == BLOCK_COLUMN)
				return new JTextField(37).getPreferredSize().width;
			if (col == DIRECTION_COLUMN)
				return new JTextField(9).getPreferredSize().width;
			return new JTextField(5).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx >= entryPointList.size()) {
				return null;
			}
			switch (c) {
				case BLOCK_COLUMN:
					return ((EntryPoint)(entryPointList.get(rx))).getFromBlockName();
				case DIRECTION_COLUMN: //
					if ( ((EntryPoint)(entryPointList.get(rx))).isForwardType() ) {
						return rbx.getString("SectionForward");
					}
					else if ( ((EntryPoint)(entryPointList.get(rx))).isReverseType() ) {
						return rbx.getString("SectionReverse");
					}
					else {
						return rbx.getString("Unknown");
					}
			}
			return null;
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==DIRECTION_COLUMN) {
				if (((String)value).equals(rbx.getString("SectionForward"))) {
					((EntryPoint)(entryPointList.get(row))).setTypeForward();
				}
				else if (((String)value).equals(rbx.getString("SectionReverse"))) {
					((EntryPoint)(entryPointList.get(row))).setTypeReverse();
				}
				else if (((String)value).equals(rbx.getString("Unknown"))) {
					((EntryPoint)(entryPointList.get(row))).setTypeUnknown();
				}
			}
			return;
		}
	}

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SectionTableAction.class.getName());
}

/* @(#)SectionTableAction.java */

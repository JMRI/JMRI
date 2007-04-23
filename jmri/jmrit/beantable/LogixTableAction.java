// LogixTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Light;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.Route;
import jmri.Memory;
import jmri.Timebase;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.util.Date;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import com.sun.java.util.collections.List;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a Logix Table.
 * <P>
 * Also contains the windows to create, edit, and delete a Logix. 
 * Also contains the window to define and edit a Conditional.
 * <P>
 * Most of the text used in this GUI is in LogixTableBundle.properties,
 *     accessed via rbx, and the remainder of the text is in 
 *	   BeanTableBundle.properties, accessed via rb.
 *
 * @author	Dave Duchamp    Copyright (C) 2007
 * @version     $Revision: 1.10 $
 */

public class LogixTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public LogixTableAction(String s) {
        super(s);
		// set up managers - no need to use InstanceManager since both managers are 
		//     Default only (internal).  We use InstanceManager to get managers for
		//     compatibility with other facilities.
		logixManager = InstanceManager.logixManagerInstance();
		conditionalManager = InstanceManager.conditionalManagerInstance();
        // disable ourself if there is no Logix manager or no Conditional manager available
        if ( (logixManager==null) || (conditionalManager==null) ) {
            setEnabled(false);
        }
    }
    public LogixTableAction() { this("Logix Table");}

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");
	
	//  *********** Methods for Logix Table Window ********************
    
    /**
     * Create the JTable DataModel, along with the changes (overrides of 
	 *         BeanTableDataModel) for the specific case of a Logix table.
	 * Note: Table Models for the Conditional table in the Edit Logix window,
	 *         and the State Variable table in the Edit Conditional window
	 *         are at the end of this module.	 
     */
    void createModel() {
        m = new BeanTableDataModel() {
            // overlay the state column with the edit column
		    static public final int ENABLECOL = 2;
		    static public final int EDITCOL = NUMCOLUMN;
			protected String enabledString = rb.getString("ColumnHeadEnabled");
    		public int getColumnCount( ){ return NUMCOLUMN+1;}
    		public String getColumnName(int col) {
    			if (col==EDITCOL) return "";    // no heading on "Edit"
    			if (col==ENABLECOL) return enabledString;
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==EDITCOL) return JButton.class;
    			if (col==ENABLECOL) return Boolean.class;
    			else return super.getColumnClass(col);
		    }
			public int getPreferredWidth(int col) {
				// override default value for SystemName and UserName columns
				if (col==SYSNAMECOL) return new JTextField(8).getPreferredSize().width;
				if (col==USERNAMECOL) return new JTextField(17).getPreferredSize().width;
    			if (col==EDITCOL) return new JTextField(5).getPreferredSize().width;
    			if (col==ENABLECOL) return new JTextField(5).getPreferredSize().width;
				else return super.getPreferredWidth(col);
			}
    		public boolean isCellEditable(int row, int col) {
    			if (col==EDITCOL) return true;
    			if (col==ENABLECOL) return true;
    			else return super.isCellEditable(row,col);
			}    		
    		public Object getValueAt(int row, int col) {
    			if (col==EDITCOL) {
					return rb.getString("ButtonEdit");
    			}
    			else if (col==ENABLECOL) {
    				return new Boolean(((Logix)getBySystemName((String)getValueAt(row, SYSNAMECOL))).getEnabled());
    			}
				else return super.getValueAt(row, col);
			}    		
    		public void setValueAt(Object value, int row, int col) {
    			if (col==EDITCOL) {
                    // set up to edit
                    String sName = (String)getValueAt(row, SYSNAMECOL);
                    editPressed(sName); 
    			}
    			else if (col==ENABLECOL) {
                    // alternate
                    Logix x = (Logix)getBySystemName((String)getValueAt(row, SYSNAMECOL));
                    boolean v = x.getEnabled();
                    x.setEnabled(!v);
    			}
    			else super.setValueAt(value, row, col);
    		}
            boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(enabledString)) return true;
                else return super.matchPropertyName(e);
            }

            public Manager getManager() { 
                return InstanceManager.logixManagerInstance(); 
            }
            public NamedBean getBySystemName(String name) { 
                return InstanceManager.logixManagerInstance().getBySystemName(name);
            }
			// Not needed - here for interface compatibility
            public void clickOn(NamedBean t) { }
            public String getValue(String s) {return "";} 
		};
    }

	// set title for Logix table
    void setTitle() {
        f.setTitle(f.rb.getString("TitleLogixTable"));
    }
	
    String helpTarget() {
        return "package.jmri.jmrit.beantable.LogixTable";
    }

	//  *********** variable definitions ********************
    
	// Multi use variables 
	ConditionalManager conditionalManager = null;  // set when LogixAction is created
	LogixManager logixManager = null;  // set when LogixAction is created
    boolean logixCreated = false;
    boolean warnMsg = false;
    boolean noWarn = false;
	boolean reminderActive = false;
	int showReminderCount = 0;
	boolean warnStateVariables = true;
	
	// current focus variables
    Logix curLogix = null;
	int numConditionals = 0;
	int conditionalRowNumber = 0;
	Conditional curConditional = null;
	
	// Add Logix Variables
    JmriJFrame addLogixFrame = null;
    JTextField systemName = new JTextField(10);
    JTextField addUserName = new JTextField(10);
    JButton create;
    JButton cancel;
	
	// Edit Logix Variables
	JmriJFrame editLogixFrame = null;
	boolean inEditMode = false;
	boolean inReorderMode = false;
	int nextInOrder = 0;
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");	
    JTextField editUserName = new JTextField(10);
	ConditionalTableModel conditionalTableModel = null;
	JPanel pct = null;  // conditional table 
    JButton newConditionalButton;
    JButton reorderButton;
	JButton calculateButton;
    JButton done;
	JButton delete;    
	JLabel status = new JLabel(" ");
	
	// Edit Conditional Variables
	boolean inEditConditionalMode = false;
	JmriJFrame editConditionalFrame = null;
	JLabel conditionalSystemName = new JLabel("xxxxxxxxxxx");
    JLabel conditionalSystemNameLabel = new JLabel( rbx.getString("ConditionalSystemName") );
    JTextField conditionalUserName = new JTextField(10);
    JLabel conditionalUserNameLabel = new JLabel( rbx.getString("ConditionalUserName") );
	VariableTableModel variableTableModel = null;
	JPanel vt = null;  // state variables table 
	JLabel cStatus = new JLabel(" ");
	JButton addVariableButton;    
	JButton checkVariableButton;    
    JButton updateConditional;
	JButton cancelConditional;
	JButton deleteConditional; 
	JRadioButton action1OnTrue;
	JRadioButton action1OnFalse;
	JRadioButton action1OnChange;
	ButtonGroup action1Group;
	JComboBox action1TypeBox;
	JTextField action1NameField;
	JTextField action1DataField;
	JComboBox action1TurnoutSetBox;
	JComboBox action1SensorSetBox;
	JComboBox action1SignalSetBox;
	JComboBox action1LightSetBox;
	JButton action1SetButton;
	JRadioButton action2OnTrue;
	JRadioButton action2OnFalse;
	JRadioButton action2OnChange;
	ButtonGroup action2Group;
	JComboBox action2TypeBox;
	JTextField action2NameField;
	JTextField action2DataField;
	JComboBox action2TurnoutSetBox;
	JComboBox action2SensorSetBox;
	JComboBox action2SignalSetBox;
	JComboBox action2LightSetBox;
	JButton action2SetButton;	   
 
	// Current State Variable Information
	int numStateVariables = 0;
	private int[] variableOpern = new int[Conditional.MAX_STATE_VARIABLES];
	private String[] variableNOT = new String[Conditional.MAX_STATE_VARIABLES];
	private int[] variableType = new int[Conditional.MAX_STATE_VARIABLES];
	private String[] variableName = new String[Conditional.MAX_STATE_VARIABLES]; 
	private boolean[] variableNameEditable = new boolean[Conditional.MAX_STATE_VARIABLES];
	private String[] variableData1 = new String[Conditional.MAX_STATE_VARIABLES];
	private boolean[] variableData1Editable = new boolean[Conditional.MAX_STATE_VARIABLES];
	private String[] variableData2 = new String[Conditional.MAX_STATE_VARIABLES];
	private boolean[] variableData2Editable = new boolean[Conditional.MAX_STATE_VARIABLES];
	private String[] variableState = new String[Conditional.MAX_STATE_VARIABLES];
	// the following are not displayed in the State Variable Table. Derived from Data1 and Data2 strings
	private int[] variableNum1 = new int[Conditional.MAX_STATE_VARIABLES];
	private int[] variableNum2 = new int[Conditional.MAX_STATE_VARIABLES];
	private boolean[] variableTriggersCalculation = new boolean[Conditional.MAX_STATE_VARIABLES];
	
	// Current Action Information
	private int[] actionOption = {Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
									Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE};
	private int[] actionDelay = {0,0};
	private int[] actionType = {Conditional.ACTION_NONE,Conditional.ACTION_NONE};
	private String[] actionName = {" "," "};
	private int[] actionData = {0,0};
	private String[] actionString = {" "," "};
	
	//  *********** Methods for Add Logix Window ********************
     
    /**
     * Responds to the Add button in Logix table
	 * Creates and/or initializes the Add Logix window
     */        
    void addPressed(ActionEvent e) {
		if (inEditMode) {
			// cancel Edit and reactivate the edited Logix
			donePressed(null);
		}
		// make an Add Logix Frame
        if (addLogixFrame==null) {
            addLogixFrame = new JmriJFrame( rbx.getString("TitleAddLogix") );
            addLogixFrame.addHelpMenu("package.jmri.jmrit.beantable.LogixAddEdit", true);
            addLogixFrame.setLocation(50,30);
            Container contentPane = addLogixFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel systemNameLabel = new JLabel( rbx.getString("LogixSystemName") );
            panel1.add(systemNameLabel);
            panel1.add(systemName);
            systemName.setToolTipText( rbx.getString("LogixSystemNameHint") );
            contentPane.add(panel1);
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel userNameLabel = new JLabel( rbx.getString("LogixUserName") );
            panel2.add(userNameLabel);
            panel2.add(addUserName);
            addUserName.setToolTipText( rbx.getString("LogixUserNameHint") );
            contentPane.add(panel2);
			// set up message
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel message1 = new JLabel( rbx.getString("AddLogixMessage1") );
            panel31.add(message1);
            JPanel panel32 = new JPanel();
			JLabel message2 = new JLabel( rbx.getString("AddLogixMessage2") );
            panel32.add(message2);
			panel3.add(panel31);
			panel3.add(panel32);
            contentPane.add(panel3);
			// set up create and cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
			// Create Logix
            panel5.add(create = new JButton(rbx.getString("CreateLogixButton")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText( rbx.getString("LogixCreateButtonHint") );
			// Cancel
            panel5.add(cancel = new JButton(rbx.getString("CancelButton")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelAddPressed(e);
                }
            });
            cancel.setToolTipText( rbx.getString("CancelButtonHint") );
            contentPane.add(panel5);
        }

		addLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					cancelAddPressed(null);
				}
			});
        addLogixFrame.pack();
        addLogixFrame.setVisible(true);
    }		
    
    /**
     * Responds to the Cancel button in Add Logix window
	 * Note: Also get there if the user closes the Add Logix window
     */
    void cancelAddPressed(ActionEvent e) {
		addLogixFrame.setVisible(false);
		f.setVisible(true);
	}
		
    /**
     * Responds to the Create Logix button in Add Logix window
     */
    void createPressed(ActionEvent e) {
        String sName = systemName.getText().toUpperCase();
        String uName = addUserName.getText().trim();
        // check validity of Logix system name
        if ( (sName.equals(" ")) || (sName.length()<1) ) {
            // Entered system name is blank or too short
            log.error("Logix system name is invalid: "+sName);
			javax.swing.JOptionPane.showMessageDialog(addLogixFrame,
				rbx.getString("Error8"),
					rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
		if ( (sName.length()<2) || (sName.charAt(0)!='I') || (sName.charAt(1)!='X') ) {
			// System name does not begin with IX, prefix IX to it
			String s = sName;
			sName = "IX"+s;
		}
		systemName.setText(sName);
        // check if a Logix with this name already exists
        Logix x = logixManager.getBySystemName(sName);
        if (x!=null) {
            // Logix already exists
            log.error("Duplicate Logix system name entered: "+sName);
			javax.swing.JOptionPane.showMessageDialog(addLogixFrame,
				rbx.getString("Error1"),
					rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        // check if a Logix with the same user name exists
		if (uName.length() > 0) {
			x = logixManager.getByUserName(uName);
			if (x!=null) {
				// Logix with this user name already exists
				log.error("Duplicate Logix user name entered: "+uName);
				javax.swing.JOptionPane.showMessageDialog(addLogixFrame,
					rbx.getString("Error3"),
						rbx.getString("ErrorTitle"),
							javax.swing.JOptionPane.ERROR_MESSAGE);
				return;
			}
        }
        // Create the new Logix
        curLogix = logixManager.createNewLogix(sName,uName);
        if (curLogix==null) {
            // should never get here unless there is an assignment conflict
            log.error("Failure to create Logix with System Name: "+sName);
            return;
        }
		numConditionals = 0;
		logixCreated = true;
		addLogixFrame.setVisible(false);
		// create the Edit Logix Window
		makeEditLogixWindow();
    }
	
	//  *********** Methods for Edit Logix Window ********************
        
    /**
     * Responds to the Edit button pressed in Logix table
     */
    void editPressed(String sName) {
		if (inEditMode) {
			// Already editing a Logix, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
			    java.text.MessageFormat.format(
				    rbx.getString("Error32"),
				    new String[]{curLogix.getSystemName()}),
					rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}						
        // check if a Logix with this name exists
        Logix x = logixManager.getBySystemName(sName);
        if (x==null) {
			// Logix does not exist, so cannot be edited
			log.error("No Logix with system name: "+sName);
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
				rbx.getString("Error5"),
					rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			if (editLogixFrame!=null) {
				editLogixFrame.setVisible(false);
			}
			return;
        }
        // Logix was found, initialize for edit
        curLogix = x;
		numConditionals = curLogix.getNumConditionals();
        // deactivate this Logix
        curLogix.deActivateLogix();
		// create the Edit Logix Window
		makeEditLogixWindow();
    }

    /**
     * creates and/or initializes the Edit Logix window
     */
	void makeEditLogixWindow () {
        fixedSystemName.setText(curLogix.getSystemName());
        editUserName.setText(curLogix.getUserName());
        // clear conditional table if needed
		if (conditionalTableModel!=null) {
			conditionalTableModel.fireTableStructureChanged();
		}
		inEditMode = true;
        if (editLogixFrame==null) {
            editLogixFrame = new JmriJFrame( rbx.getString("TitleEditLogix") );
            editLogixFrame.addHelpMenu("package.jmri.jmrit.beantable.LogixAddEdit", true);
            editLogixFrame.setLocation(100,30);
            Container contentPane = editLogixFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel systemNameLabel = new JLabel( rbx.getString("LogixSystemName") );
            panel1.add(systemNameLabel);
            panel1.add(fixedSystemName);
            contentPane.add(panel1);
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel userNameLabel = new JLabel( rbx.getString("LogixUserName") );
            panel2.add(userNameLabel);
            panel2.add(editUserName);
            editUserName.setToolTipText( rbx.getString("LogixUserNameHint2") );
            contentPane.add(panel2);			
			// add table of Conditionals
            JPanel pctSpace = new JPanel();
            pctSpace.setLayout(new FlowLayout());
            pctSpace.add(new JLabel("   "));
            contentPane.add(pctSpace);            
            JPanel pTitle = new JPanel();
            pTitle.setLayout(new FlowLayout());
            pTitle.add(new JLabel(rbx.getString("ConditionalTableTitle")));
            contentPane.add(pTitle);
            pct = new JPanel();
			// initialize table of conditionals
            conditionalTableModel = new ConditionalTableModel();
			JTable conditionalTable = new JTable(conditionalTableModel);
            conditionalTable.setRowSelectionAllowed(false);
            conditionalTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(530,250));
            TableColumnModel conditionalColumnModel = conditionalTable.getColumnModel();
            TableColumn sNameColumn = conditionalColumnModel.
                                                getColumn(ConditionalTableModel.SNAME_COLUMN);
            sNameColumn.setResizable(true);
            sNameColumn.setMinWidth(100);
            sNameColumn.setMaxWidth(130);
            TableColumn uNameColumn = conditionalColumnModel.
                                                getColumn(ConditionalTableModel.UNAME_COLUMN);
            uNameColumn.setResizable(true);
            uNameColumn.setMinWidth(210);
            uNameColumn.setMaxWidth(260);
            TableColumn stateColumn = conditionalColumnModel.
                                                getColumn(ConditionalTableModel.STATE_COLUMN);
            stateColumn.setResizable(false);
            stateColumn.setMinWidth(90);
            stateColumn.setMaxWidth(100);
            TableColumn buttonColumn = conditionalColumnModel.
                                                getColumn(ConditionalTableModel.BUTTON_COLUMN);
			// install button renderer and editor
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			conditionalTable.setDefaultRenderer(JButton.class,buttonRenderer);
			TableCellEditor buttonEditor = new ButtonEditor(new JButton());
			conditionalTable.setDefaultEditor(JButton.class,buttonEditor);
			JButton testButton = new JButton("XXXXX");
			conditionalTable.setRowHeight(testButton.getPreferredSize().height);
			buttonColumn.setPreferredWidth(testButton.getPreferredSize().width);
            buttonColumn.setResizable(false);
            JScrollPane conditionalTableScrollPane = new JScrollPane(conditionalTable);
            pct.add(conditionalTableScrollPane,BorderLayout.CENTER);
            contentPane.add(pct);
            pct.setVisible(true);
            // add message area between table and buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(status);
			panel4.add(panel41);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			// Conditional panel buttons - New Conditional
            panel42.add(newConditionalButton = new JButton(rbx.getString("NewConditionalButton")));
            newConditionalButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newConditionalPressed(e);
                }
            });
            newConditionalButton.setToolTipText( rbx.getString("NewConditionalButtonHint") );
			// Conditional panel buttons - Reorder 
            panel42.add(reorderButton = new JButton(rbx.getString("ReorderButton")));
            reorderButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    reorderPressed(e);
                }
            });
            reorderButton.setToolTipText( rbx.getString("ReorderButtonHint") );
			// Conditional panel buttons - Calculate 
            panel42.add(calculateButton = new JButton(rbx.getString("CalculateButton")));
            calculateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    calculatePressed(e);
                }
            });
            calculateButton.setToolTipText( rbx.getString("CalculateButtonHint") );
			panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);
			// add buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
			// Bottom Buttons - Done Logix
            panel5.add(done = new JButton(rbx.getString("DoneButton")));
            done.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    donePressed(e);
                }
            });
            done.setToolTipText( rbx.getString("DoneButtonHint") );
			// Delete Logix
            panel5.add(delete = new JButton(rbx.getString("DeleteLogixButton")));
			delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            delete.setToolTipText( rbx.getString("DeleteLogixButtonHint") );			
            contentPane.add(panel5);
        }

        if (!reminderActive) {
			editLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						// if in Edit mode, complete the Edit and reactivate the Logix
						if (inEditMode) {
							donePressed(null);
						}
						else {
							editLogixFrame.setVisible(false);
							// bring Logix Table to front
							f.setVisible(true);
						}
					}
				});
			reminderActive = true;
		}            
        editLogixFrame.pack();
        editLogixFrame.setVisible(true);
    }
	
	/**
	 * Display reminder to save
	 */
	void showSaveReminder() {
		showReminderCount ++;
		if (showReminderCount<4) {
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
				rbx.getString("Reminder1"),
					rbx.getString("ReminderTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Responds to the Reorder Button in the Edit Logix window
	 */
	void reorderPressed(ActionEvent e) {
		if (checkEditConditional()) return;
		// Check if reorder is reasonable
		if (numConditionals<=1) {
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
				rbx.getString("Error11"),
					rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}			
		// Initialize for reordering Conditionals
		curLogix.initializeReorder();
		nextInOrder = 0;
		inReorderMode = true;
		status.setText(rbx.getString("ReorderMessage"));
		conditionalTableModel.fireTableDataChanged();
	}
	
	/**
	 * Responds to the Calculate Button in the Edit Logix window
	 */
	void calculatePressed(ActionEvent e) {
		if (checkEditConditional()) return;
		// are there Conditionals to calculate?
		if (numConditionals>0) {
			// There are conditionals to calculate
			String cName = "";
			Conditional c = null;
			for (int i = 0;i<numConditionals;i++) {
				cName = curLogix.getConditionalByNumberOrder(i);
				c = conditionalManager.getBySystemName(cName);
				if (c==null) {
					log.error("Invalid conditional system name when calculating - "+cName);
				}
				else {
					// calculate without taking any action
					c.calculate(false);
				}
			}
			// force the table to update 
			conditionalTableModel.fireTableDataChanged();
		}
	}
	
    /**
     * Responds to the Done button in the Edit Logix window
	 *    Note: also get here if the Edit Logix window is dismissed, or
	 *		if the Add button is pressed in the Logic Table with an 
	 *		active Edit Logix window.
     */
    void donePressed(ActionEvent e) {
		if (checkEditConditional()) return;
		if (curLogix!=null) {
			Logix x = curLogix;
			// Check for consistency in suppressing triggering of calculation among
			//		state variables of this Logix.
			String[] varName = new String[Logix.MAX_LISTENERS];
			int[] varListenerType = new int[Logix.MAX_LISTENERS];
			String[] varListenerProperty = new String[Logix.MAX_LISTENERS];
			int[] varAppearance = new int[Logix.MAX_LISTENERS];
			int[] numTriggersCalc = new int[Logix.MAX_LISTENERS];
			int[] numTriggerSuppressed = new int[Logix.MAX_LISTENERS];
			int numVs = x.getStateVariableList(varName, varListenerType, 
						varListenerProperty, varAppearance, numTriggersCalc, 
						numTriggerSuppressed, Logix.MAX_LISTENERS); 
			if (numVs>0) {
				// scan table testing for inconsistencies
				for (int k=0;k<numVs;k++) {
					if ( (numTriggersCalc[k] > 0) && (numTriggerSuppressed[k] > 0) ) {
						// have inconsistency, synthesize a warning message
						log.warn("Triggers calculation inconsistency - "+varName[k]);

                        String msg7 = "";
						if (varListenerProperty[k].equals("Appearance") ) {
							msg7 = java.text.MessageFormat.format(
                                rbx.getString("Warn7"),
                                new String[]{signalAppearanceIndexToString(
								    signalAppearanceToAppearanceIndex(varAppearance[k]))});
						}
                        String msg6 = java.text.MessageFormat.format(
                            rbx.getString("Warn6"),
                            new String[]{varListenerProperty[k], varName[k], msg7});
						javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
							msg6,
							rbx.getString("WarnTitle"),
						    javax.swing.JOptionPane.WARNING_MESSAGE);						
					}
				}
			}
			// Check if the User Name has been changed
			String uName = editUserName.getText();
			if ( !(uName.equals(x.getUserName())) ) {
				// user name has changed - check if already in use
				Logix p = logixManager.getByUserName(uName);
				if (p!=null) {
					// Logix with this user name already exists
					log.error("Failure to update Logix with Duplicate User Name: "+uName);
					javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
						rbx.getString("Error6"),
							rbx.getString("ErrorTitle"),
								javax.swing.JOptionPane.ERROR_MESSAGE);
					return;
				}
				// user name is unique, change it
				x.setUserName(uName);  
				m.fireTableDataChanged();   
			}
			// complete update and activate Logix
			x.activateLogix();
		}
		else {
			log.error("null pointer to curLogix in donePressed method");
		}
        logixCreated = true;
		inEditMode = false;
		showSaveReminder();
		editLogixFrame.setVisible(false);
		// bring Logix Table to front
		f.setVisible(true);
	}

    /**
     * Responds to the Delete button in the Edit Logix window
     */
    void deletePressed(ActionEvent e) {
		if (checkEditConditional()) return;
		showSaveReminder();
        Logix x = curLogix;
		// delete this Logix
		logixManager.deleteLogix(x);
		curLogix = null;
        logixCreated = true;
		inEditMode = false;
		editLogixFrame.setVisible(false);
  		f.setVisible(true);
	}
	
	/**
	 * Responds to the New Conditional Button in Edit Logix Window
	 */
	void newConditionalPressed(ActionEvent e) {
		if (checkEditConditional()) return;
		// make system name for new conditional
		int num = curLogix.getNextConditionalNumber();
		String cName = curLogix.getSystemName()+"C"+Integer.toString(num);
		Conditional c = conditionalManager.createNewConditional(cName,"");
		if (c==null) {
            // should never get here unless there is an assignment conflict
            log.error("Failure to create Conditional with System Name: "+cName);
            return;
        }
		// add to Logix at the end of the calculate order
		if (!curLogix.addConditional(cName,-1)) {
			// too many conditionals
// add too many conditionals message			
		}
		// Conditional successfully added to Logix
		curConditional = c;
		conditionalTableModel.fireTableRowsInserted(numConditionals,numConditionals);
		conditionalRowNumber = numConditionals;
		numConditionals ++;
		numStateVariables = 0;
		// clear action items
		actionOption[0] = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
		actionDelay[0] = 0;
		actionType[0] = Conditional.ACTION_NONE;
		actionName[0] = " ";
		actionData[0] = 0;
		actionString[0] = " ";
		actionOption[1] = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
		actionDelay[1] = 0;
		actionType[1] = Conditional.ACTION_NONE;
		actionName[1] = " ";
		actionData[1] = 0;
		actionString[1] = " ";
		// move to edit this Conditional's information
		makeEditConditionalWindow();
	}
		
	/**
	 * Responds to Edit Button in the Conditional table of the Edit Logix Window
	 */
	void editConditionalPressed(int rx) {
		if (inEditConditionalMode) {
			// Already editing a Conditional, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
			    java.text.MessageFormat.format(
				    rbx.getString("Error34"),
				    new String[]{curConditional.getSystemName()}),
				rbx.getString("ErrorTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}						
		// get Conditional to edit
		curConditional = conditionalManager.
			getBySystemName(curLogix.getConditionalByNumberOrder(rx));
		if (curConditional==null) {
			log.error("Attempted edit of non-existant conditional.");
			return;
		}
		numStateVariables = curConditional.getNumStateVariables();
		conditionalRowNumber = rx;
		// get Conditional information and set up in display variables
		if (numStateVariables > 0) {
			curConditional.getStateVariables(variableOpern,variableType,variableName,
						variableData1,variableNum1,variableNum2,variableTriggersCalculation);
			int saveType = 0;
			String saveName = "";
			String saveData1 = "";
			int saveNum1 = 0;
			int saveNum2 = 0;
			for (int i = 0;i<numStateVariables;i++) {
				if ( (variableOpern[i]==Conditional.OPERATOR_NOT) ||
						(variableOpern[i]==Conditional.OPERATOR_AND_NOT) ) {
					variableNOT[i] = rbx.getString("LogicNOT");
				}
				else {
					variableNOT[i] = " ";
				}
				variableData2[i] = " ";
				// save current values for this state variable
				saveType = variableType[i];
				saveName = variableName[i];
				saveData1 = variableData1[i];
				saveNum1 = variableNum1[i];
				saveNum2 = variableNum2[i];
				// initialize as a new state variable
				variableType[i] = -1;
				variableTypeChanged(i,stateVariableTypeToString(saveType));
				// restore current values for this state variable
				variableName[i] = saveName;
				variableData1[i] = saveData1;
				variableNum1[i] = saveNum1;
				variableNum2[i] = saveNum2;
				// since number variables are not directly displayed, format as needed
				if (variableType[i]==Conditional.TYPE_FAST_CLOCK_RANGE) {
					// need to format strings for display
					variableData1[i] = formatTime(variableNum1[i]/60,variableNum1[i]-
														((variableNum1[i]/60)*60));
					variableData2[i] = formatTime(variableNum2[i]/60,variableNum2[i]-
														((variableNum2[i]/60)*60));
				}
			}
		}
		// get action variables
		curConditional.getAction(actionOption,actionDelay,actionType,
				actionName,actionData,actionString);
		makeEditConditionalWindow();
	}

	/** 
	 * Checks if edit of a conditional is in progress
	 *   Returns true after sending message if this is the case
	 */
	boolean checkEditConditional () {
		if (inEditConditionalMode) {
			// Already editing a Conditional, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
			    java.text.MessageFormat.format(
				    rbx.getString("Error35"),
				    new String[]{curConditional.getSystemName()}),
				rbx.getString("ErrorTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}
	
	//  *********** Methods for Edit Conditional Window ********************

	/**
	 * Creates and/or initializes the Edit Conditional window
	 *  Note: you can get here via the New Conditional button
	 *        (newConditionalPressed) or via an Edit button 
	 *        in the Conditional table of the Edit Logix window.
	 */
	void makeEditConditionalWindow () {
		conditionalSystemName.setText(curConditional.getSystemName());
		conditionalUserName.setText(curConditional.getUserName());
		cStatus.setText(" ");
        if (editConditionalFrame==null) {
            editConditionalFrame = new JmriJFrame( rbx.getString("TitleEditConditional") );
            editConditionalFrame.addHelpMenu("package.jmri.jmrit.beantable.ConditionalAddEdit", true);
            editConditionalFrame.setLocation(50,40);
            Container contentPane = editConditionalFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
            panel1.add(conditionalSystemNameLabel);
            panel1.add(conditionalSystemName);
            contentPane.add(panel1);
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
            panel2.add(conditionalUserNameLabel);
            panel2.add(conditionalUserName);
            conditionalUserName.setToolTipText( rbx.getString("ConditionalUserNameHint") );
            contentPane.add(panel2);
			// add logical expression section
			JPanel logicPanel = new JPanel();
			logicPanel.setLayout(new BoxLayout(logicPanel,BoxLayout.Y_AXIS));
            JPanel varTitle = new JPanel();
            varTitle.setLayout(new FlowLayout());
            varTitle.add(new JLabel(rbx.getString("StateVariableTableTitle")));
            logicPanel.add(varTitle);
			// set up state variables table
            vt = new JPanel();
			// initialize and populate Combo boxes for table of state variables
			JComboBox notCombo = new JComboBox();
			notCombo.addItem (" ");
			notCombo.addItem (rbx.getString("LogicNOT"));
			JComboBox typeCombo = new JComboBox();
			for (int i=1;i<=Conditional.NUM_STATE_VARIABLE_TYPES;i++) {
				typeCombo.addItem(stateVariableTypeToString(i));
			}
			// initialize table of state variables
            variableTableModel = new VariableTableModel();
			JTable variableTable = new JTable(variableTableModel);
            variableTable.setRowSelectionAllowed(false);
            variableTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(720,150));
            TableColumnModel variableColumnModel = variableTable.getColumnModel();
            TableColumn andColumn = variableColumnModel.
                                                getColumn(VariableTableModel.AND_COLUMN);
            andColumn.setResizable(false);
			andColumn.setPreferredWidth(new JTextField(3).getPreferredSize().width);
            TableColumn notColumn = variableColumnModel.
                                                getColumn(VariableTableModel.NOT_COLUMN);
			notColumn.setCellEditor(new DefaultCellEditor(notCombo));
			variableTable.setRowHeight(notCombo.getPreferredSize().height);
			notColumn.setPreferredWidth(notCombo.getPreferredSize().width);
            notColumn.setResizable(false);
            TableColumn typeColumn = variableColumnModel.
                                                getColumn(VariableTableModel.TYPE_COLUMN);
			typeColumn.setCellEditor(new DefaultCellEditor(typeCombo));
			typeColumn.setPreferredWidth(typeCombo.getPreferredSize().width);
            typeColumn.setResizable(false);
            TableColumn sysNameColumn = variableColumnModel.
                                                getColumn(VariableTableModel.SNAME_COLUMN);
            sysNameColumn.setResizable(true);
            sysNameColumn.setMinWidth(105);
            sysNameColumn.setMaxWidth(160);
            TableColumn data1Column = variableColumnModel.
                                                getColumn(VariableTableModel.DATA1_COLUMN);
            data1Column.setResizable(true);
            data1Column.setMinWidth(65);
            data1Column.setMaxWidth(90);
            TableColumn data2Column = variableColumnModel.
                                                getColumn(VariableTableModel.DATA2_COLUMN);
            data2Column.setResizable(true);
            data2Column.setMinWidth(65);
            data2Column.setMaxWidth(90);
            TableColumn stateColumn = variableColumnModel.
                                                getColumn(VariableTableModel.STATE_COLUMN);
            stateColumn.setResizable(false);
			stateColumn.setPreferredWidth(60);
            TableColumn triggersColumn = variableColumnModel.
                                                getColumn(VariableTableModel.TRIGGERS_COLUMN);
            triggersColumn.setResizable(true);
            triggersColumn.setMinWidth(65);
            triggersColumn.setMaxWidth(200);
            TableColumn deleteColumn = variableColumnModel.
                                                getColumn(VariableTableModel.DELETE_COLUMN);
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			variableTable.setDefaultRenderer(JButton.class,buttonRenderer);
			TableCellEditor buttonEditor = new ButtonEditor(new JButton());
			variableTable.setDefaultEditor(JButton.class,buttonEditor);
			JButton testButton = new JButton("Delete");
			variableTable.setRowHeight(testButton.getPreferredSize().height);
			deleteColumn.setPreferredWidth(testButton.getPreferredSize().width+5);
            deleteColumn.setResizable(false);
			// add a scroll pane
            JScrollPane variableTableScrollPane = new JScrollPane(variableTable);
            vt.add(variableTableScrollPane,BorderLayout.CENTER);
            logicPanel.add(vt);
            vt.setVisible(true);
			// set up state variable message area and buttons
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(cStatus);
			logicPanel.add(panel41);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			// State Variable panel buttons - Add State Variable
            panel42.add(addVariableButton = new JButton(rbx.getString("AddVariableButton")));
            addVariableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addVariablePressed(e);
                }
            });
            addVariableButton.setToolTipText( rbx.getString("AddVariableButtonHint") );
			// State Variable panel buttons - Check State Variables 
            panel42.add(checkVariableButton = new JButton(rbx.getString("CheckVariableButton")));
            checkVariableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    checkVariablePressed(e);
                }
            });
            checkVariableButton.setToolTipText( rbx.getString("CheckVariableButtonHint") );			
			logicPanel.add(panel42);
            Border logicPanelBorder = BorderFactory.createEtchedBorder();
			Border logicPanelTitled = BorderFactory.createTitledBorder(logicPanelBorder,
				rbx.getString("TitleLogicalExpression") );
            logicPanel.setBorder(logicPanelTitled);
            contentPane.add(logicPanel);
			// set up Action information area - Action 1
			JPanel actionPanel = new JPanel();
			actionPanel.setLayout(new BoxLayout(actionPanel,BoxLayout.Y_AXIS));
            JPanel action1Title = new JPanel();
            action1Title.setLayout(new FlowLayout());
            action1Title.add(new JLabel(rbx.getString("Action1Title")));
			action1Group = new ButtonGroup();
			action1OnTrue = new JRadioButton(rbx.getString("OnChangeToTrue"),true);
			action1OnTrue.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionOption[0] = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
					}
				});
			action1Group.add(action1OnTrue);
			action1Title.add(action1OnTrue);
			action1OnFalse = new JRadioButton(rbx.getString("OnChangeToFalse"),false);
			action1OnFalse.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionOption[0] = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
					}
				});
			action1Group.add(action1OnFalse);
			action1Title.add(action1OnFalse);
			action1OnChange = new JRadioButton(rbx.getString("OnChange"),false);
			action1OnChange.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionOption[0] = Conditional.ACTION_OPTION_ON_CHANGE;
					}
				});
			action1Group.add(action1OnChange);
			action1Title.add(action1OnChange);			
            actionPanel.add(action1Title);
			JPanel action1Data = new JPanel();
			action1Data.setLayout(new FlowLayout());
			action1Data.add(new JLabel(rbx.getString("Action1Type")));
			action1TypeBox = new JComboBox();
			for (int i=1;i<=Conditional.NUM_ACTION_TYPES;i++) {
				action1TypeBox.addItem(actionTypeToString(i));
			}
            action1TypeBox.setToolTipText(rbx.getString("ActionTypeHint") );
			action1Data.add(action1TypeBox);
			action1TypeBox.setSelectedIndex(0);
			action1NameField = new JTextField(8);
			action1Data.add(action1NameField);
			action1NameField.setVisible(false);
			action1TurnoutSetBox = new JComboBox(new String[]{rbx.getString("TurnoutClosed"),
												rbx.getString("TurnoutThrown")});
			action1Data.add(action1TurnoutSetBox);
			action1TurnoutSetBox.setToolTipText(rbx.getString("TurnoutSetHint"));
			action1TurnoutSetBox.setVisible(false);
			action1SensorSetBox = new JComboBox(new String[]{rbx.getString("SensorActive"),
												rbx.getString("SensorInactive")});
			action1Data.add(action1SensorSetBox);
			action1SensorSetBox.setToolTipText(rbx.getString("SensorSetHint"));
			action1SensorSetBox.setVisible(false);
			action1LightSetBox = new JComboBox(new String[]{rbx.getString("LightOn"),
												rbx.getString("LightOff")});
			action1Data.add(action1LightSetBox);
			action1LightSetBox.setToolTipText(rbx.getString("LightSetHint"));
			action1LightSetBox.setVisible(false);
			action1SignalSetBox = new JComboBox();
			for (int i=0;i<7;i++) {
				action1SignalSetBox.addItem(signalAppearanceIndexToString(i));
			}			
			action1Data.add(action1SignalSetBox);
			action1SignalSetBox.setToolTipText(rbx.getString("SignalSetHint"));
			action1SignalSetBox.setVisible(false);
			action1SetButton = new JButton("Set");
			action1SetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setFileLocation(1);
					}
				});
			action1Data.add(action1SetButton);
			action1SetButton.setVisible(false);
			action1DataField = new JTextField(20);
			action1Data.add(action1DataField);
			action1DataField.setVisible(false);
			// note - this listener cannot be added before other action 1 items have been created
			action1TypeBox.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						actionTypeChanged(1);
					}
				});
			actionPanel.add(action1Data);
			// Space between the two Actions
            JPanel actionSpace = new JPanel();
            actionSpace.setLayout(new FlowLayout());
            actionSpace.add(new JLabel("   "));
			actionPanel.add(actionSpace);
			// Action 2 items
            JPanel action2Title = new JPanel();
            action2Title.setLayout(new FlowLayout());
            action2Title.add(new JLabel(rbx.getString("Action2Title")));
			action2Group = new ButtonGroup();
			action2OnTrue = new JRadioButton(rbx.getString("OnChangeToTrue"),true);
			action2OnTrue.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionOption[1] = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
					}
				});
			action2Group.add(action2OnTrue);
			action2Title.add(action2OnTrue);
			action2OnFalse = new JRadioButton(rbx.getString("OnChangeToFalse"),false);
			action2OnFalse.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionOption[1] = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
					}
				});
			action2Group.add(action2OnFalse);
			action2Title.add(action2OnFalse);
			action2OnChange = new JRadioButton(rbx.getString("OnChange"),false);
			action2OnChange.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionOption[1] = Conditional.ACTION_OPTION_ON_CHANGE;
					}
				});
			action2Group.add(action2OnChange);
			action2Title.add(action2OnChange);			
            actionPanel.add(action2Title);
			JPanel action2Data = new JPanel();
			action2Data.setLayout(new FlowLayout());
			action2Data.add(new JLabel(rbx.getString("Action2Type")));
			action2TypeBox = new JComboBox();
			for (int i=1;i<=Conditional.NUM_ACTION_TYPES;i++) {
				action2TypeBox.addItem(actionTypeToString(i));
			}
            action2TypeBox.setToolTipText(rbx.getString("ActionTypeHint") );
			action2Data.add(action2TypeBox);
			action2TypeBox.setSelectedIndex(0);
			action2NameField = new JTextField(8);
			action2Data.add(action2NameField);
			action2NameField.setVisible(false);
			action2TurnoutSetBox = new JComboBox(new String[]{rbx.getString("TurnoutClosed"),
												rbx.getString("TurnoutThrown")});
			action2Data.add(action2TurnoutSetBox);
			action2TurnoutSetBox.setToolTipText(rbx.getString("TurnoutSetHint"));
			action2TurnoutSetBox.setVisible(false);
			action2SensorSetBox = new JComboBox(new String[]{rbx.getString("SensorActive"),
												rbx.getString("SensorInactive")});
			action2Data.add(action2SensorSetBox);
			action2SensorSetBox.setToolTipText(rbx.getString("SensorSetHint"));
			action2SensorSetBox.setVisible(false);
			action2LightSetBox = new JComboBox(new String[]{rbx.getString("LightOn"),
												rbx.getString("LightOff")});
			action2Data.add(action2LightSetBox);
			action2LightSetBox.setToolTipText(rbx.getString("LightSetHint"));
			action2LightSetBox.setVisible(false);
			action2SignalSetBox = new JComboBox();
			for (int i=0;i<7;i++) {
				action2SignalSetBox.addItem(signalAppearanceIndexToString(i));
			}			
			action2Data.add(action2SignalSetBox);
			action2SignalSetBox.setToolTipText(rbx.getString("SignalSetHint"));
			action2SignalSetBox.setVisible(false);
			action2SetButton = new JButton("Set");
			action2SetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setFileLocation(2);
					}
				});
			action2Data.add(action2SetButton);
			action2SetButton.setVisible(false);
			action2DataField = new JTextField(20);
			action2Data.add(action2DataField);
			action2DataField.setVisible(false);
			// note - this listener cannot be added before other action 2 items have been created
			action2TypeBox.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						actionTypeChanged(2);
					}
				});
			actionPanel.add(action2Data);
			// Complete Action data section
            Border actionPanelBorder = BorderFactory.createEtchedBorder();
			Border actionPanelTitled = BorderFactory.createTitledBorder(actionPanelBorder,
				rbx.getString("TitleAction") );
            actionPanel.setBorder(actionPanelTitled);
            contentPane.add(actionPanel);
			// Bottom Buttons - Update Conditional
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(updateConditional = new JButton(rbx.getString("UpdateConditionalButton")));
            updateConditional.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateConditionalPressed(e);
                }
            });
            updateConditional.setToolTipText( rbx.getString("UpdateConditionalButtonHint") );
			// Cancel
            panel5.add(cancelConditional = new JButton(rbx.getString("CancelButton")));
            cancelConditional.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelConditionalPressed(e);
                }
            });
            cancelConditional.setToolTipText( rbx.getString("CancelConditionalButtonHint") );
			// Delete Conditional
            panel5.add(deleteConditional = new JButton(rbx.getString("DeleteConditionalButton")));
			deleteConditional.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteConditionalPressed(e);
                }
            });
            deleteConditional.setToolTipText( rbx.getString("DeleteConditionalButtonHint") );
			
            contentPane.add(panel5);
        }
		// setup window closing listener
		editConditionalFrame.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						if (inEditConditionalMode)
							cancelConditionalPressed(null);
					}
				});
		// initialize state variable table 
		variableTableModel.fireTableDataChanged();
		// initialize action variables
		initializeActionVariables();
        editConditionalFrame.pack();
        editConditionalFrame.setVisible(true);
		inEditConditionalMode = true;
    }
	
	/**
	 * Initializes new set of action variables when display changes
	 */
	void initializeActionVariables() {
		// initialize for the type of actions
		action1TypeBox.setSelectedIndex(actionType[0]-1);
		actionType[0] = 0;
		actionTypeChanged(1);
		action2TypeBox.setSelectedIndex(actionType[1]-1);
		actionType[1] = 0;
		actionTypeChanged(2);
		// initialize action options
		if (actionOption[0] == Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE) {
			action1OnTrue.setSelected(true);
		}
		else if (actionOption[0] == Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE) {
			action1OnFalse.setSelected(true);
		}
		else if (actionOption[0] == Conditional.ACTION_OPTION_ON_CHANGE) {
			action1OnChange.setSelected(true);
		}
		if (actionOption[1] == Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE) {
			action2OnTrue.setSelected(true);
		}
		else if (actionOption[1] == Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE) {
			action2OnFalse.setSelected(true);
		}
		else if (actionOption[1] == Conditional.ACTION_OPTION_ON_CHANGE) {
			action2OnChange.setSelected(true);
		}
		// set variables specific to action type
		action1NameField.setText(actionName[0]);
		action2NameField.setText(actionName[1]);
		switch (actionType[0]) {
			case Conditional.ACTION_SET_TURNOUT:
				if (actionData[0]==Turnout.CLOSED) {
					action1TurnoutSetBox.setSelectedIndex(0);
				}
				else {
					action1TurnoutSetBox.setSelectedIndex(1);
				}
				break;
			case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
				action1SignalSetBox.setSelectedIndex(
						signalAppearanceToAppearanceIndex(actionData[0]));
				break;
			case Conditional.ACTION_SET_SENSOR:
				if (actionData[0]==Sensor.ACTIVE) {
					action1SensorSetBox.setSelectedIndex(0);
				}
				else {
					action1SensorSetBox.setSelectedIndex(1);
				}
				break;
			case Conditional.ACTION_DELAYED_SENSOR:
				if (actionData[0]==Sensor.ACTIVE) {
					action1SensorSetBox.setSelectedIndex(0);
				}
				else {
					action1SensorSetBox.setSelectedIndex(1);
				}
				action1DataField.setText(Integer.toString(actionDelay[0]));
				break;
			case Conditional.ACTION_SET_LIGHT:
				if (actionData[0]==Light.ON) {
					action1LightSetBox.setSelectedIndex(0);
				}
				else {
					action1LightSetBox.setSelectedIndex(1);
				}
				break;
			case Conditional.ACTION_SET_MEMORY:
			case Conditional.ACTION_PLAY_SOUND:
			case Conditional.ACTION_RUN_SCRIPT:
				action1DataField.setText(actionString[0]);
				break;
		}
		switch (actionType[1]) {
			case Conditional.ACTION_SET_TURNOUT:
				if (actionData[1]==Turnout.CLOSED) {
					action2TurnoutSetBox.setSelectedIndex(0);
				}
				else {
					action2TurnoutSetBox.setSelectedIndex(1);
				}
				break;
			case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
				action2SignalSetBox.setSelectedIndex(
						signalAppearanceToAppearanceIndex(actionData[1]));
				break;
			case Conditional.ACTION_SET_SENSOR:
				if (actionData[1]==Sensor.ACTIVE) {
					action2SensorSetBox.setSelectedIndex(0);
				}
				else {
					action2SensorSetBox.setSelectedIndex(1);
				}
				break;
			case Conditional.ACTION_DELAYED_SENSOR:
				if (actionData[1]==Sensor.ACTIVE) {
					action2SensorSetBox.setSelectedIndex(0);
				}
				else {
					action2SensorSetBox.setSelectedIndex(1);
				}
				action2DataField.setText(Integer.toString(actionDelay[1]));
				break;
			case Conditional.ACTION_SET_LIGHT:
				if (actionData[1]==Light.ON) {
					action2LightSetBox.setSelectedIndex(0);
				}
				else {
					action2LightSetBox.setSelectedIndex(1);
				}
				break;
			case Conditional.ACTION_SET_MEMORY:
			case Conditional.ACTION_PLAY_SOUND:
			case Conditional.ACTION_RUN_SCRIPT:
				action2DataField.setText(actionString[1]);
				break;
		}
	}
		
	/**
	 * Responds to the Add State Variable Button in the Edit Conditional window
	 */
	void addVariablePressed(ActionEvent e) {
		variableNOT[numStateVariables] = " ";
		variableType[numStateVariables] = 0;
		variableName[numStateVariables] = " ";
		variableName[numStateVariables] = " ";
		variableNameEditable[numStateVariables] = false;
		variableData1[numStateVariables] = " ";
		variableData1Editable[numStateVariables] = false;
		variableData2[numStateVariables] = " ";
		variableNum1[numStateVariables] = 0;
		variableNum2[numStateVariables] = 0;
		variableTriggersCalculation[numStateVariables] = true;
		variableData2Editable[numStateVariables] = false;
		variableState[numStateVariables] = rbx.getString("Unknown");
		cStatus.setText(rbx.getString("TypeSelectMessage"));		
		numStateVariables ++;
		variableTableModel.fireTableRowsInserted(numStateVariables,numStateVariables);
	}
	
	/**
	 * Responds to the Check State Variables button in the Edit Conditional window
	 * Note: If the user is in process of editting a cell, the value of that cell may
	 *        not be entered into arrays.  To prevent this, it is recommended in the
	 *		  hints and error messages, that the State column be clicked before 
	 *        checking State Variables. 
	 */
	boolean checkVariablePressed(ActionEvent e) {
		if (numStateVariables==0) {
			return(true);
		}
		// check individual state variables
		boolean result = true;
		for (int i = 0;(i<numStateVariables); i++) {
			result = checkStateVariable(i);
			if (!result) {
			    if (log.isDebugEnabled()) log.debug("checkStateVariable("+i+") failed, stopping");
			    break;
            }
		}
		if (result)
			cStatus.setText(rbx.getString("VariableOKMessage"));
		else
			cStatus.setText(rbx.getString("VariableErrorMessage"));
		variableTableModel.fireTableDataChanged();	
		return (result);
	}
	
	/**
	 * Responds to the Delete Button in the State Variable Table of the Edit Conditional window
	 */
	void deleteVariablePressed(int r) {
		if (numStateVariables==0) {
			log.error("attempt to delete state variables when none are present");
			return;
		}
		numStateVariables --;
		if (numStateVariables==0) {
			if (warnStateVariables) {
				// warning message - last State Variable deleted
				javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
					rbx.getString("Warn3"),
							rbx.getString("WarnTitle"),
								javax.swing.JOptionPane.WARNING_MESSAGE);
				warnStateVariables = false;				
			}
		}
		// move remaining state variables if needed		
		if (r<numStateVariables) {
			for (int i = r;i<numStateVariables;i++) {
				variableOpern[i] = variableOpern[i+1];
				variableNOT[i] = variableNOT[i+1];
				variableType[i] = variableType[i+1];
				variableName[i] = variableName[i+1];
				variableNameEditable[i] = variableNameEditable[i+1];
				variableData1[i] = variableData1[i+1];
				variableData1Editable[i] = variableData1Editable[i+1];
				variableData2[i] = variableData2[i+1];
				variableData2Editable[i] = variableData2Editable[i+1];
				variableState[i] = variableState[i+1];
				variableNum1[i] = variableNum1[i+1];
				variableNum2[i] = variableNum2[i+1];
				variableTriggersCalculation[i] = variableTriggersCalculation[i+1];
			}
		}
		variableTableModel.fireTableDataChanged();
	}
	
	/**
	 * Responds to the Update Conditional Button in the Edit Conditional window
	 */
	void updateConditionalPressed(ActionEvent e) {
        Conditional c = curConditional;
        // Check if the User Name has been changed
        String uName = conditionalUserName.getText().trim();
        if ( !(uName.equals(c.getUserName())) ) {
            // user name has changed - check if already in use
			if ( (uName!=null) && (!(uName.equals(""))) ) {
				Conditional p = conditionalManager.getByUserName(curLogix,uName);
				if (p!=null) {
					// Conditional with this user name already exists
					log.error("Failure to update Conditional with Duplicate User Name: "+uName);
					javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
						rbx.getString("Error10"),
							rbx.getString("ErrorTitle"),
								javax.swing.JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
            // user name is unique or blank, change it
            c.setUserName(uName);  
			conditionalTableModel.fireTableDataChanged();   
        }
		if (numStateVariables<=0) {
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
			    java.text.MessageFormat.format(
				    rbx.getString("Warn5"),
				    new String[]{c.getSystemName()}),
				rbx.getString("WarnTitle"),
				javax.swing.JOptionPane.WARNING_MESSAGE);
		}
		else {
			// validate and update state variables
			if (!checkVariablePressed(null)) {
				// State Variables are not OK - don't save
				return;
			}
		}
		// validate and update action variables
		if (!validateActionVariables(0,action1NameField,action1DataField,
					action1TurnoutSetBox,action1SensorSetBox,action1SignalSetBox,
					action1LightSetBox) ) {
			return;
		}
		if (!validateActionVariables(1,action2NameField,action2DataField,
					action2TurnoutSetBox,action2SensorSetBox,action2SignalSetBox,
					action2LightSetBox) ) {
			return;
		}		
		// complete update
		curConditional.setStateVariables(variableOpern,variableType,variableName,
					variableData1,variableNum1,variableNum2,variableTriggersCalculation,
					numStateVariables);
		curConditional.setAction(actionOption,actionDelay,actionType,
							actionName,actionData,actionString);
		inEditConditionalMode = false;
		editConditionalFrame.setVisible(false);	
		editLogixFrame.setVisible(true);	
	}	    
	
	/** 
	 * Responds to the Cancel button in the Edit Conditional frame
	 *    Also activated if the user dismisses the Edit Conditional window.
	 */
	void cancelConditionalPressed(ActionEvent e) {
		inEditConditionalMode = false;
		editConditionalFrame.setVisible(false);
		editLogixFrame.setVisible(true);			
	}
	
	/**
	 * Responds to the Delete Conditional Button in the Edit Conditional window
	 */
	void deleteConditionalPressed(ActionEvent e) {
        Conditional c = curConditional;
		// delete this Conditional - this is done by the parent Logix
		curConditional = null;
		curLogix.deleteConditional(c.getSystemName());
		numConditionals --;
		// complete deletion
        logixCreated = true;  // mark the parent Logix as modified
		inEditConditionalMode = false;
		editConditionalFrame.setVisible(false);
		conditionalTableModel.fireTableRowsDeleted(conditionalRowNumber,conditionalRowNumber);
		if (numConditionals<1) {
			// warning message - last Conditional deleted
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
				rbx.getString("Warn1"),
						rbx.getString("WarnTitle"),
							javax.swing.JOptionPane.WARNING_MESSAGE);
		}
		editLogixFrame.setVisible(true);			
	}
	
    JFileChooser fileChooser = null;
	/** 
	 * Responds to the Set button in the Edit Conditional window action section.
	 * @param actionNum - equal to 1 or 2, depending upon which action button is clicked
	 */
	void setFileLocation(int actionNum) {
		if (fileChooser == null) fileChooser = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
			// set selected file location in data string
            try {
				if (actionNum==1) {
					action1DataField.setText(fileChooser.getSelectedFile().getCanonicalPath());
				}
				else if (actionNum==2) {
					action2DataField.setText(fileChooser.getSelectedFile().getCanonicalPath());
				}
            } catch (java.io.IOException e) {
                log.error("exception setting file location: "+e);
				if (actionNum==1) action1DataField.setText("");
				else if (actionNum==2) action2DataField.setText("");
            }
        }
    }
	
	/** 
	 * Responds to a change in an Action Type Box in the Edit Conditional window.
	 * Also used to initialize window for edit
	 * @param actionNum - equal to 1 or 2, depending upon which action has changed type
	 */
	void actionTypeChanged(int actionNum) {
		int type = 1;
		// get Action type
		if (actionNum == 1) {
			// get new Action type
			type = action1TypeBox.getSelectedIndex() + 1;
			if (type==actionType[0]) {
				// no change - simply return
				return;
			}
			// set up for new type
			actionType[0] = type;
			switch (type) {
				case Conditional.ACTION_NONE:
					action1NameField.setVisible(false);
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);
					break;
				case Conditional.ACTION_SET_TURNOUT:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(true);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);
					action1NameField.setToolTipText(rbx.getString("NameHintTurnout"));
					break;
				case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(true);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSignal"));
					break;
				case Conditional.ACTION_SET_SIGNAL_HELD:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_CLEAR_SIGNAL_HELD:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_SET_SIGNAL_DARK:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_SET_SIGNAL_LIT:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_TRIGGER_ROUTE:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintRoute"));			
					break;
				case Conditional.ACTION_SET_SENSOR:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(true);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSensor"));						
					break;
				case Conditional.ACTION_DELAYED_SENSOR:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(true);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(true);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintSensor"));						
					action1DataField.setToolTipText(rbx.getString("DataHintDelayedSensor"));						
					break;
				case Conditional.ACTION_SET_LIGHT:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(true);			
					action1NameField.setToolTipText(rbx.getString("NameHintLight"));						
					break;
				case Conditional.ACTION_SET_MEMORY:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(true);
					action1DataField.setText("");
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintMemory"));			
					action1DataField.setToolTipText(rbx.getString("DataHintMemory"));			
					break;
				case Conditional.ACTION_ENABLE_LOGIX:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintLogix"));			
					break;
				case Conditional.ACTION_DISABLE_LOGIX:
					action1NameField.setVisible(true);
					action1NameField.setText("");
					action1DataField.setVisible(false);
					action1SetButton.setVisible(false);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1NameField.setToolTipText(rbx.getString("NameHintLogix"));			
					break;
				case Conditional.ACTION_PLAY_SOUND:
					action1NameField.setVisible(false);
					action1DataField.setVisible(true);
					action1DataField.setText("");
					action1SetButton.setVisible(true);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1DataField.setToolTipText(rbx.getString("DataHintSound"));
					action1SetButton.setToolTipText(rbx.getString("SetHintSound"));						
					break;
				case Conditional.ACTION_RUN_SCRIPT:
					action1NameField.setVisible(false);
					action1DataField.setVisible(true);
					action1DataField.setText("");
					action1SetButton.setVisible(true);
					action1TurnoutSetBox.setVisible(false);
					action1SensorSetBox.setVisible(false);
					action1SignalSetBox.setVisible(false);
					action1LightSetBox.setVisible(false);			
					action1DataField.setToolTipText(rbx.getString("DataHintScript"));						
					action1SetButton.setToolTipText(rbx.getString("SetHintScript"));						
					break;
			}
		}
		else if (actionNum == 2) {
			// get new Action type
			type = action2TypeBox.getSelectedIndex() + 1;
			if (type==actionType[1]) {
				// no change - simply return
				return;
			}
			// set up for new type
			actionType[1] = type;
			switch (type) {
				case Conditional.ACTION_NONE:
					action2NameField.setVisible(false);
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);
					break;
				case Conditional.ACTION_SET_TURNOUT:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(true);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);
					action2NameField.setToolTipText(rbx.getString("NameHintTurnout"));
					break;
				case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(true);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSignal"));
					break;
				case Conditional.ACTION_SET_SIGNAL_HELD:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_CLEAR_SIGNAL_HELD:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_SET_SIGNAL_DARK:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_SET_SIGNAL_LIT:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSignal"));			
					break;
				case Conditional.ACTION_TRIGGER_ROUTE:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintRoute"));			
					break;
				case Conditional.ACTION_SET_SENSOR:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(true);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSensor"));						
					break;
				case Conditional.ACTION_DELAYED_SENSOR:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(true);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(true);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintSensor"));						
					action2DataField.setToolTipText(rbx.getString("DataHintDelayedSensor"));						
					break;
				case Conditional.ACTION_SET_LIGHT:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(true);			
					action2NameField.setToolTipText(rbx.getString("NameHintLight"));						
					break;
				case Conditional.ACTION_SET_MEMORY:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(true);
					action2DataField.setText("");
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintMemory"));			
					action2DataField.setToolTipText(rbx.getString("DataHintMemory"));			
					break;
				case Conditional.ACTION_ENABLE_LOGIX:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintLogix"));			
					break;
				case Conditional.ACTION_DISABLE_LOGIX:
					action2NameField.setVisible(true);
					action2NameField.setText("");
					action2DataField.setVisible(false);
					action2SetButton.setVisible(false);
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2NameField.setToolTipText(rbx.getString("NameHintLogix"));			
					break;
				case Conditional.ACTION_PLAY_SOUND:
					action2NameField.setVisible(false);
					action2SetButton.setVisible(true);
					action2DataField.setVisible(true);
					action2DataField.setText("");
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2DataField.setToolTipText(rbx.getString("DataHintSound"));						
					action2SetButton.setToolTipText(rbx.getString("SetHintSound"));						
					break;
				case Conditional.ACTION_RUN_SCRIPT:
					action2NameField.setVisible(false);
					action2SetButton.setVisible(true);
					action2DataField.setVisible(true);
					action2DataField.setText("");
					action2TurnoutSetBox.setVisible(false);
					action2SensorSetBox.setVisible(false);
					action2SignalSetBox.setVisible(false);
					action2LightSetBox.setVisible(false);			
					action2DataField.setToolTipText(rbx.getString("DataHintScript"));						
					action2SetButton.setToolTipText(rbx.getString("SetHintScript"));						
					break;
			}
		}
		// Ask window to reconfigure itself
		editConditionalFrame.pack();
	}
	
	/**
	 * Responds to change in variable type in State Variable Table in the Edit Conditional window
	 *   Also used to set up for Edit of a Conditional with state variables.
	 * @param r = row number in state variable table == subscript of state variables arrays
	 */
	void variableTypeChanged(int r,String typeString) {
		int oldType = variableType[r];
		// get new variable type
		int type = stringToStateVariableType(typeString);
		if (type!=oldType) {
			// set defaults needed by most types, specific types should override as needed
			variableNameEditable[r] = true;
			variableData1[r] = rbx.getString("NotApplicableAbbreviation");
			variableData1Editable[r] = false;
			variableData2[r] = rbx.getString("NotApplicableAbbreviation");
			variableData2Editable[r] = false;
			// set up table row by type
			switch (type) {
				case Conditional.TYPE_SENSOR_ACTIVE:
					if (oldType!=Conditional.TYPE_SENSOR_INACTIVE) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSensorMessage"));
					break;
				case Conditional.TYPE_SENSOR_INACTIVE:
					if (oldType!=Conditional.TYPE_SENSOR_ACTIVE) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSensorMessage"));
					break;
				case Conditional.TYPE_TURNOUT_THROWN:
					if (oldType!=Conditional.TYPE_TURNOUT_CLOSED) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeTurnoutMessage"));
					break;
				case Conditional.TYPE_TURNOUT_CLOSED:
					if (oldType!=Conditional.TYPE_TURNOUT_THROWN) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeTurnoutMessage"));
					break;
				case Conditional.TYPE_CONDITIONAL_TRUE:
					if (oldType!=Conditional.TYPE_CONDITIONAL_FALSE) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeConditionalMessage"));
					break;
				case Conditional.TYPE_CONDITIONAL_FALSE:
					if (oldType!=Conditional.TYPE_CONDITIONAL_TRUE) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeConditionalMessage"));
					break;
				case Conditional.TYPE_LIGHT_ON:
					if (oldType!=Conditional.TYPE_LIGHT_OFF) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeLightMessage"));
					break;
				case Conditional.TYPE_LIGHT_OFF:
					if (oldType!=Conditional.TYPE_LIGHT_ON) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeLightMessage"));
					break;
				case Conditional.TYPE_MEMORY_EQUALS:
					variableName[r] = "";
					variableData1[r] = "";
					variableData1Editable[r] = true;
					cStatus.setText(rbx.getString("TypeMemoryMessage"));
					break;
				case Conditional.TYPE_FAST_CLOCK_RANGE:
					variableNameEditable[r] = false;
					variableName[r] = rbx.getString("NotApplicableAbbreviation");
					variableData1[r] = "00:00";
					variableData1Editable[r] = true;
					variableData2[r] = "00:00";
					variableData2Editable[r] = true;
					variableNum1[r] = 0;
					variableNum2[r] = 0;
					cStatus.setText(rbx.getString("TypeClockMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_RED:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_GREEN:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_DARK:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				case Conditional.TYPE_SIGNAL_HEAD_HELD:
					if (!( (oldType>=Conditional.TYPE_SIGNAL_HEAD_RED) &&
							(oldType<=Conditional.TYPE_SIGNAL_HEAD_HELD) ) ) {
						variableName[r] = "";
					}
					cStatus.setText(rbx.getString("TypeSignalMessage"));
					break;
				default:
					break;
			}				
		}
		// complete processing this type change
		variableType[r] = type;
		if (variableTableModel!=null) {
			variableTableModel.fireTableRowsUpdated(r,r);
		}
	}
	
	/**
	 *  Checks one State Variable in State Variable table of the Edit Conditional Window
	 *  <P>
	 *  Returns true if all data checks out OK, otherwise false. 
	 *  <P> 
	 *  Messages are sent to the user for any errors found. This routine returns
	 *    false immediately after finding an error, even if there might be more errors.
	 *  <P>
	 *  If data checks out, this method returns the current state of the state variable
	 */
	boolean checkStateVariable(int index) {
		// check vNOT and translate to the proper Conditional operator designation
		boolean isNOT = false;
		if (variableNOT[index].equals(rbx.getString("LogicNOT"))) {
			isNOT = true;
			if (index == 0) {
				variableOpern[index] = Conditional.OPERATOR_NOT;
			}
			else {
				variableOpern[index] = Conditional.OPERATOR_AND_NOT;
			}
		}
		else {
			if (index == 0) {
				variableOpern[index] = Conditional.OPERATOR_NONE;
			}
			else {
				variableOpern[index] = Conditional.OPERATOR_AND;
			}
		}
		// check according to state variable type
		String vUName = variableName[index].trim();
		String sTemp = variableName[index].toUpperCase();
		String vSName = sTemp.trim();
		variableName[index] = vSName;
		Sensor sn = null;
		Turnout t = null;
		SignalHead h = null;
		Conditional c = null;
		Light lgt = null;
		Memory m = null;
		variableNum1[index] = 0;
		variableNum2[index] = 0;
		boolean result = false;
		switch (variableType[index]) {
			case Conditional.TYPE_SENSOR_ACTIVE:
				sn = InstanceManager.sensorManagerInstance().getByUserName(vUName);
				if (sn != null) {
					variableName[index] = vUName;
				}
				else {
					sn = InstanceManager.sensorManagerInstance().getBySystemName(vSName);
					if (sn == null) {
						variableName[index] = vUName;
						messageInvalidSensorName(vUName,true);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Sensor "+vSName+" not found");
						return (false);
					}
				}	
				if (sn.getState() == Sensor.ACTIVE) result = true;
				break;
			case Conditional.TYPE_SENSOR_INACTIVE:
				sn = InstanceManager.sensorManagerInstance().getByUserName(vUName);
				if (sn != null) {
					variableName[index] = vUName;
				}
				else {
					sn = InstanceManager.sensorManagerInstance().getBySystemName(vSName);
					if (sn == null) {
						variableName[index] = vUName;
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Sensor "+vSName+" not found");
						messageInvalidSensorName(vUName,true);
						return (false);
					}
				}
				if (sn.getState() == Sensor.INACTIVE) result = true;
				break;
			case Conditional.TYPE_TURNOUT_THROWN:
				t = InstanceManager.turnoutManagerInstance().getByUserName(vUName);
				if (t != null) {
					variableName[index] = vUName;
				}
				else {
					t = InstanceManager.turnoutManagerInstance().getBySystemName(vSName);
					if (t == null) {
						variableName[index] = vUName;
						messageInvalidTurnoutName(vUName,true);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Turnout "+vSName+" not found");
						return (false);
					}
				}
				if (t.getState() == Turnout.THROWN) result = true; 
				break;
			case Conditional.TYPE_TURNOUT_CLOSED:
				t = InstanceManager.turnoutManagerInstance().getByUserName(vUName);
				if (t != null) {
					variableName[index] = vUName;
				}
				else {
					t = InstanceManager.turnoutManagerInstance().getBySystemName(vSName);
					if (t == null) {
						variableName[index] = vUName;
						messageInvalidTurnoutName(vUName,true);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Turnout "+vSName+" not found");
						return (false);
					}
				}
				if (t.getState() == Turnout.CLOSED) result = true;
				break;
			case Conditional.TYPE_CONDITIONAL_TRUE:
				c = InstanceManager.conditionalManagerInstance().getByUserName(curLogix,vUName);
				if (c != null) {
					variableName[index] = vUName;
				}
				else {
					c = InstanceManager.conditionalManagerInstance().getBySystemName(vSName);
					if (c == null) {
						variableName[index] = vUName;
						messageInvalidConditionalName(vUName);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Conditional "+vSName+" not found");
						return (false);
					}
				}
				if (c.getState() == Conditional.TRUE) result = true;
				break;
			case Conditional.TYPE_CONDITIONAL_FALSE:
				c = InstanceManager.conditionalManagerInstance().getByUserName(curLogix,vUName);
				if (c != null) {
					variableName[index] = vUName;
				}
				else {
					c = InstanceManager.conditionalManagerInstance().getBySystemName(vSName);
					if (c == null) {
						variableName[index] = vUName;
						messageInvalidConditionalName(vUName);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Conditional "+vSName+" not found");
						return (false);
					}
				}
				if (c.getState() == Conditional.FALSE) result = true;
				break;
			case Conditional.TYPE_LIGHT_ON:
				lgt = InstanceManager.lightManagerInstance().getByUserName(vUName);
				if (lgt != null) {
					variableName[index] = vUName;
				}
				else {
					lgt = InstanceManager.lightManagerInstance().getBySystemName(vSName);
					if (lgt == null) {
						variableName[index] = vUName;
						messageInvalidLightName(vUName,true);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Light "+vSName+" not found");
						return (false);
					}
				}
				if (lgt.getState() == Light.ON) result = true;
				break;
			case Conditional.TYPE_LIGHT_OFF:
				lgt = InstanceManager.lightManagerInstance().getByUserName(vUName);
				if (lgt != null) {
					variableName[index] = vUName;
				}
				else {
					lgt = InstanceManager.lightManagerInstance().getBySystemName(vSName);
					if (lgt == null) {
						variableName[index] = vUName;
						messageInvalidLightName(vUName,true);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Light "+vSName+" not found");
						return (false);
					}
				}
				if (lgt.getState() == Light.OFF) result = true; 
				break;
			case Conditional.TYPE_MEMORY_EQUALS:
				m = InstanceManager.memoryManagerInstance().getByUserName(vUName);
				if (m != null) {
					variableName[index] = vUName;
				}
				else {
					m = InstanceManager.memoryManagerInstance().getBySystemName(vSName);
					if (m == null) {
						variableName[index] = vUName;
						messageInvalidMemoryName(vUName,true);
						if (log.isDebugEnabled()) log.debug("checkStateVariable failed: Memory "+vSName+" not found");
						return (false);
					}
				}
				String s = (String)m.getValue();
				if (s==null) {
				    return (true);  // result is false, because null doesn't match content
			    }
				if (s.equals(variableData1[index])) result = true; 
				break;
			case Conditional.TYPE_FAST_CLOCK_RANGE:
				int beginTime = parseTime(variableData1[index]);
				if (beginTime < 0) {
					// parse error occurred - message has been sent
					return (false);
				}
				int endTime = parseTime(variableData2[index]);
				if (endTime < 0) {
					return (false);
				}
				// set beginning and end time (minutes since midnight)
				variableNum1[index] = beginTime;
				variableNum2[index] = endTime;
				// get current fast clock time
				Timebase fastClock = InstanceManager.timebaseInstance();
				Date currentTime = fastClock.getTime();
				int currentMinutes = (currentTime.getHours()*60) + currentTime.getMinutes();
				// check if current time is within range specified
				if (endTime>beginTime) {
					// range is entirely within one day
					if ( (currentMinutes<endTime) && (currentMinutes>=beginTime) ) result = true;
				}
				else {
					// range includes midnight
					if (currentMinutes>=beginTime) result = true;
					else if (currentMinutes<endTime) result = true;
				}
				break;
			case Conditional.TYPE_SIGNAL_HEAD_RED:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.RED) result = true; 
				break;
			case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.YELLOW) result = true; 
				break;
			case Conditional.TYPE_SIGNAL_HEAD_GREEN:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.GREEN) result = true;
				break;
			case Conditional.TYPE_SIGNAL_HEAD_DARK:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.DARK) result = true; 
				break;
			case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.FLASHRED) result = true; 
				break;
			case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.FLASHYELLOW) result = true; 
				break;
			case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getAppearance() == SignalHead.FLASHGREEN) result = true;
				break;
			case Conditional.TYPE_SIGNAL_HEAD_LIT:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getLit()) result = true;
				break;
			case Conditional.TYPE_SIGNAL_HEAD_HELD:
				h = InstanceManager.signalHeadManagerInstance().getByUserName(vUName);
				if (h != null) {
					variableName[index] = vUName;
				}
				else {
					h = InstanceManager.signalHeadManagerInstance().getBySystemName(vSName);
					if (h == null) {
						variableName[index] = vUName;
						messageInvalidSignalHeadName(vUName,true);
						return (false);
					}
				}
				if (h.getHeld()) result = true;
				break;
		}
		// set state variable
		if (isNOT) result = !result;
		if (result) variableState[index] = rbx.getString("True");
		else variableState[index] = rbx.getString("False");
		return (true);
	}
	
	/**
	*  Validates Action data from Edit Conditional Window, and transfers it to
	*    current action variables as appropriate
	*  <P>
	*  Returns true if all data checks out OK, otherwise false. 
	*  <P> 
	*  Messages are sent to the user for any errors found. This routine returns
	*    false immediately after finding an error, even if there might be more errors.
	*/
	boolean validateActionVariables(int index,
			JTextField systemNameField,JTextField dataField,JComboBox turnoutSetBox,
			JComboBox sensorSetBox,JComboBox signalSetBox,JComboBox lightSetBox) {
		// get possible user name
		String uName = systemNameField.getText().trim();	
		// get possible system name, make upper case, and tentatively initialize	
		String sName = systemNameField.getText().toUpperCase().trim();
		// initialize to system name, changing to user name later if needed
		actionName[index] = sName;
		systemNameField.setText(sName);
		// validate according to action type
		actionDelay[index] = 0;
		switch (actionType[index]) {
			case Conditional.ACTION_NONE:
				actionName[index] = " ";
				actionData[index] = 0;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_SET_TURNOUT:
				Turnout t = null;
				// check if turnout user name was entered
				if ((uName!=null) && (uName!="")) {
					t = InstanceManager.turnoutManagerInstance().getByUserName(uName);
					if (t != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check turnout system name
						t = InstanceManager.turnoutManagerInstance().getBySystemName(sName);
					}
				}
				if (t == null) {
					systemNameField.setText(uName);
					messageInvalidTurnoutName(uName,false);
					return (false);
				}
				if (turnoutSetBox.getSelectedIndex()==0)
					actionData[index] = Turnout.CLOSED;
				else
					actionData[index] = Turnout.THROWN;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
				SignalHead hx = null;
				// check if signal head user name was entered
				if ((uName!=null) && (uName!="")) {
					hx = InstanceManager.signalHeadManagerInstance().getByUserName(uName);
					if (hx != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check signal head system name
						hx = InstanceManager.signalHeadManagerInstance().getBySystemName(sName);
					}
				}
				if (hx == null) {
					systemNameField.setText(uName);
					messageInvalidSignalHeadName(uName,false);
					return (false);
				}
				actionData[index] = signalAppearanceIndexToAppearance(signalSetBox.getSelectedIndex());
				actionString[index] = " ";
				break;
			case Conditional.ACTION_SET_SIGNAL_HELD:
			case Conditional.ACTION_CLEAR_SIGNAL_HELD:
			case Conditional.ACTION_SET_SIGNAL_DARK:
			case Conditional.ACTION_SET_SIGNAL_LIT:
				SignalHead h = null;
				// check if signal head user name was entered
				if ((uName!=null) && (uName!="")) {
					h = InstanceManager.signalHeadManagerInstance().getByUserName(uName);
					if (h != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check signal head system name
						h = InstanceManager.signalHeadManagerInstance().getBySystemName(sName);
					}
				}
				if (h == null) {
					systemNameField.setText(uName);
					messageInvalidSignalHeadName(uName,false);
					return (false);
				}
				actionData[index] = 0;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_TRIGGER_ROUTE:
				Route r = null;
				// check if route user name was entered
				if ((uName!=null) && (uName!="")) {
					r = InstanceManager.routeManagerInstance().getByUserName(uName);
					if (r != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check route system name
						r = InstanceManager.routeManagerInstance().getBySystemName(sName);
					}
				}
				if (r == null) {
					systemNameField.setText(uName);
					messageInvalidRouteName(uName);
					return (false);
				}
				actionData[index] = 0;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_SET_SENSOR:
				Sensor sn = null;
				// check if sensor user name was entered
				if ((uName!=null) && (uName!="")) {
					sn = InstanceManager.sensorManagerInstance().getByUserName(uName);
					if (sn != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check sensor system name
						sn = InstanceManager.sensorManagerInstance().getBySystemName(sName);
					}
				}
				if (sn == null) {
					systemNameField.setText(uName);
					messageInvalidSensorName(uName,false);
					return (false);
				}
				if (sensorSetBox.getSelectedIndex()==0)
					actionData[index] = Sensor.ACTIVE;
				else
					actionData[index] = Sensor.INACTIVE;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_DELAYED_SENSOR:
				Sensor snx = null;
				// check if sensor user name was entered
				if ((uName!=null) && (uName!="")) {
					snx = InstanceManager.sensorManagerInstance().getByUserName(uName);
					if (snx != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check sensor system name
						snx = InstanceManager.sensorManagerInstance().getBySystemName(sName);
					}
				}
				if (snx == null) {
					systemNameField.setText(uName);
					messageInvalidSensorName(uName,false);
					return (false);
				}
				if (sensorSetBox.getSelectedIndex()==0)
					actionData[index] = Sensor.ACTIVE;
				else
					actionData[index] = Sensor.INACTIVE;
				try {
                    actionDelay[index] = Integer.valueOf(dataField.getText()).intValue();
                    if (actionDelay[index]<=0) {
						javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
			                java.text.MessageFormat.format(
				                rbx.getString("Error25"),
				                new String[]{dataField.getText()}),
							rbx.getString("ErrorTitle"),
							javax.swing.JOptionPane.ERROR_MESSAGE);
						return (false);
					}
                }
                catch (Exception e) {
					javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
			            java.text.MessageFormat.format(
				            rbx.getString("Error23"),
				            new String[]{dataField.getText()}),
						rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
					return (false);
                }
				actionString[index] = " ";
				break;
			case Conditional.ACTION_SET_LIGHT:
				Light lgt = null;
				// check if light user name was entered
				if ((uName!=null) && (uName!="")) {
					lgt = InstanceManager.lightManagerInstance().getByUserName(uName);
					if (lgt != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check light system name
						lgt = InstanceManager.lightManagerInstance().getBySystemName(sName);
					}
				}
				if (lgt == null) {
					systemNameField.setText(uName);
					messageInvalidLightName(uName,false);
					return (false);
				}
				if (lightSetBox.getSelectedIndex()==0)
					actionData[index] = Light.ON;
				else
					actionData[index] = Light.OFF;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_SET_MEMORY:
				Memory m = null;
				// check if memory user name was entered
				if ((uName!=null) && (uName!="")) {
					m = InstanceManager.memoryManagerInstance().getByUserName(uName);
					if (m != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check memory system name
						m = InstanceManager.memoryManagerInstance().getBySystemName(sName);
					}
				}
				if (m == null) {
					systemNameField.setText(uName);
					messageInvalidMemoryName(uName,false);
					return (false);
				}
				actionData[index] = 0;
				actionString[index] = dataField.getText();
				break;
			case Conditional.ACTION_ENABLE_LOGIX:
			case Conditional.ACTION_DISABLE_LOGIX:
				Logix x = null;
				// check if logix user name was entered
				if ((uName!=null) && (uName!="")) {
					x = InstanceManager.logixManagerInstance().getByUserName(uName);
					if (x != null) {
						actionName[index] = uName;
						systemNameField.setText(uName);
					}
					else {
						// check logix system name
						x = InstanceManager.logixManagerInstance().getBySystemName(sName);
					}
				}
				if (x == null) {
					systemNameField.setText(uName);
					messageInvalidLogixName(uName);
					return (false);
				}
				actionData[index] = 0;
				actionString[index] = " ";
				break;
			case Conditional.ACTION_PLAY_SOUND:
				actionName[index] = " ";
				actionData[index] = 0;
				actionString[index] = dataField.getText();
				break;
			case Conditional.ACTION_RUN_SCRIPT:
				actionName[index] = " ";
				actionData[index] = 0;
				actionString[index] = dataField.getText();
				break;
		}		
		return (true);
	}
	
	//  *********** Utility Methods ********************
	
	/**
	 *  Identifies State Variable Type from Text String
	 *  Note: if string does not correspond to a state variable type
	 *		as defined in stateVariableTypeToString, returns 0.
	 */
	int stringToStateVariableType (String s) {
		int type = 0;
		for(int i = 1;i<=Conditional.MAX_STATE_VARIABLES;i++) {
			if (s.equals(stateVariableTypeToString(i))) {
				type = i;
				return (type);
			}
		}
		return type;
	}
	
	/**
	 *  State Variable Type to Text String
	 */
	String stateVariableTypeToString (int type) {
		switch (type) {
			case Conditional.TYPE_SENSOR_ACTIVE:
				return (rbx.getString ("TypeSensorActive"));
			case Conditional.TYPE_SENSOR_INACTIVE:
				return (rbx.getString ("TypeSensorInactive"));
			case Conditional.TYPE_TURNOUT_THROWN:
				return (rbx.getString ("TypeTurnoutThrown"));
			case Conditional.TYPE_TURNOUT_CLOSED:
				return (rbx.getString ("TypeTurnoutClosed"));
			case Conditional.TYPE_CONDITIONAL_TRUE:
				return (rbx.getString ("TypeConditionalTrue"));
			case Conditional.TYPE_CONDITIONAL_FALSE:
				return (rbx.getString ("TypeConditionalFalse"));
			case Conditional.TYPE_LIGHT_ON:
				return (rbx.getString ("TypeLightOn"));
			case Conditional.TYPE_LIGHT_OFF:
				return (rbx.getString ("TypeLightOff"));
			case Conditional.TYPE_MEMORY_EQUALS:
				return (rbx.getString ("TypeMemoryEquals"));
			case Conditional.TYPE_FAST_CLOCK_RANGE:
				return (rbx.getString ("TypeFastClockRange"));
			case Conditional.TYPE_SIGNAL_HEAD_RED:
				return (rbx.getString ("TypeSignalHeadRed"));
			case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
				return (rbx.getString ("TypeSignalHeadYellow"));
			case Conditional.TYPE_SIGNAL_HEAD_GREEN:
				return (rbx.getString ("TypeSignalHeadGreen"));
			case Conditional.TYPE_SIGNAL_HEAD_DARK:
				return (rbx.getString ("TypeSignalHeadDark"));
			case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
				return (rbx.getString ("TypeSignalHeadFlashRed"));
			case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
				return (rbx.getString ("TypeSignalHeadFlashYellow"));
			case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
				return (rbx.getString ("TypeSignalHeadFlashGreen"));
			case Conditional.TYPE_SIGNAL_HEAD_LIT:
				return (rbx.getString ("TypeSignalHeadLit"));
			case Conditional.TYPE_SIGNAL_HEAD_HELD:
				return (rbx.getString ("TypeSignalHeadHeld"));
		}
		return ("");
	}	
	
	/**
	 *  State Variable Type to Text String
	 */
	String actionTypeToString (int type) {
		switch (type) {
			case Conditional.ACTION_NONE:
				return (rbx.getString ("ActionNone"));
			case Conditional.ACTION_SET_TURNOUT:
				return (rbx.getString ("ActionSetTurnout"));
			case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
				return (rbx.getString ("ActionSetSignal"));
			case Conditional.ACTION_SET_SIGNAL_HELD:
				return (rbx.getString ("ActionSetSignalHeld"));
			case Conditional.ACTION_CLEAR_SIGNAL_HELD:
				return (rbx.getString ("ActionClearSignalHeld"));
			case Conditional.ACTION_SET_SIGNAL_DARK:
				return (rbx.getString ("ActionSetSignalDark"));
			case Conditional.ACTION_SET_SIGNAL_LIT:
				return (rbx.getString ("ActionSetSignalLit"));
			case Conditional.ACTION_TRIGGER_ROUTE:
				return (rbx.getString ("ActionTriggerRoute"));
			case Conditional.ACTION_SET_SENSOR:
				return (rbx.getString ("ActionSetSensor"));
			case Conditional.ACTION_DELAYED_SENSOR:
				return (rbx.getString ("ActionDelayedSensor"));
			case Conditional.ACTION_SET_LIGHT:
				return (rbx.getString ("ActionSetLight"));
			case Conditional.ACTION_SET_MEMORY:
				return (rbx.getString ("ActionSetMemory"));
			case Conditional.ACTION_ENABLE_LOGIX:
				return (rbx.getString ("ActionEnableLogix"));
			case Conditional.ACTION_DISABLE_LOGIX:
				return (rbx.getString ("ActionDisableLogix"));
			case Conditional.ACTION_PLAY_SOUND:
				return (rbx.getString ("ActionPlaySound"));
			case Conditional.ACTION_RUN_SCRIPT:
				return (rbx.getString ("ActionRunScript"));
		}
		return ("");
	}	
	
	/**
	 *  Signal Appearance Index to Text String
	 */
	String signalAppearanceIndexToString (int appearanceIndex) {
		switch (appearanceIndex) {
			case 0:
				return (rbx.getString("AppearanceRed"));
			case 1:
				return (rbx.getString("AppearanceYellow"));
			case 2:
				return (rbx.getString("AppearanceGreen"));
			case 3:
				return (rbx.getString("AppearanceDark"));
			case 4:
				return (rbx.getString("AppearanceFlashRed"));
			case 5:
				return (rbx.getString("AppearanceFlashYellow"));
			case 6:
				return (rbx.getString("AppearanceFlashGreen"));
		}
		return ("");
	}
	
	/**
	 *  Signal Appearance Index to Signal Appearance
	 */
	int signalAppearanceIndexToAppearance (int appearanceIndex) {
		switch (appearanceIndex) {
			case 0:
				return (SignalHead.RED);
			case 1:
				return (SignalHead.YELLOW);
			case 2:
				return (SignalHead.GREEN);
			case 3:
				return (SignalHead.DARK);
			case 4:
				return (SignalHead.FLASHRED);
			case 5:
				return (SignalHead.FLASHYELLOW);
			case 6:
				return (SignalHead.FLASHGREEN);
		}
		return (0);
	}	
	
	/**
	 *  Signal Appearance to Signal Appearance Index 
	 */
	int signalAppearanceToAppearanceIndex (int appearance) {
		switch (appearance) {
			case SignalHead.RED:
				return (0);
			case SignalHead.YELLOW:
				return (1);
			case SignalHead.GREEN:
				return (2);
			case SignalHead.DARK:
				return (3);
			case SignalHead.FLASHRED:
				return (4);
			case SignalHead.FLASHYELLOW:
				return (5);
			case SignalHead.FLASHGREEN:
				return (6);
		}
		return (0);
	}	

    /** 
     *  Parses time in hh:mm format given a string in the correct format
	 * <P>
	 * Returns integer = hh*60 + mm (minutes since midnight) if parse is successful, 
	 *      else returns -1.
	 * <P>
	 * If errors in format are found, an error message is sent to the user and logged
	 * @param s - string with time in hh:mm format
     */
    int parseTime (String s) {
		int nHour = 0;
		int nMin = 0;
		boolean error = false;
		// check length and : in the 3rd character
		if ( (s.length() != 5) || (s.charAt(2) != ':') ) {
			error = true;
		}
		if (!error) {
			// get hour and check if it is within range
			try {
				nHour = Integer.valueOf(s.substring(0,2)).intValue();
				if ( (nHour < 0) || (nHour > 24) ) {
					error = true;
				}
			}
			catch (Exception e) {
				error = true;
			}
		}
		if (!error) {
			// get miinutes and check if it is within range
			try {
				nMin = Integer.valueOf(s.substring(3,5)).intValue();
				if ( (nMin < 0) || (nMin > 59) ) {
					error = true;
				}
			}
			catch (Exception e) {
				error = true;
			}
		}
		if (error) {
			// if unsuccessful, print error message
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
			    java.text.MessageFormat.format(
				    rbx.getString("Error26"),
				    new String[]{s}),
		        rbx.getString("ErrorTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return (-1);
		}
		// here if successful
		return ( (nHour*60) + nMin );		
	}

    /** 
     *  Formats time to hh:mm given integer hour and minute
     */
    String formatTime (int hour,int minute) {
        String s = "";
        String t = Integer.toString(hour);
        if (t.length() == 2) {
            s = t + ":";
        }
        else if (t.length() == 1) {
            s = "0" + t + ":";
        } 
        t = Integer.toString(minute);
        if (t.length() == 2) {
            s = s + t;
        }
        else if (t.length() == 1) {
            s = s + "0" + t;
        }
        if (s.length() != 5) {
            // input error
            s = "00:00";
        }
        return s;
    }
	
	
	/**
	 * Utility routine for formatting up
	 * the various "does not match an existing" error 
	 * messages.
     * @param msg Index of the message string from the properties file
     * @param name Bad/not found bean name
     * @param table If true, display message Error21 about editing in the table
	 */
	void messageGeneralInvalidBean(String msg, String name, boolean table) {
		javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
		    java.text.MessageFormat.format(
			    rbx.getString(msg)+(table?rbx.getString("Error21"):""),
				new String[]{name}),
			rbx.getString("ErrorTitle"),
			javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	
	
	/**
	 * Sends an invalid turnout name error message for Edit Conditional window
	 */
	void messageInvalidTurnoutName(String name,boolean table) {
	    messageGeneralInvalidBean("Error13", name, table);
	}
	
	/**
	 * Sends an invalid signal head name error message for Edit Conditional window
	 */
	void messageInvalidSignalHeadName(String name,boolean table) {
	    messageGeneralInvalidBean("Error14", name, table);
	}
	
	/**
	 * Sends an invalid sensor name error message for Edit Conditional window
	 */
	void messageInvalidSensorName(String name,boolean table) {
	    messageGeneralInvalidBean("Error15", name, table);
	}
	
	/**
	 * Sends an invalid light name error message for Edit Conditional window
	 */
	void messageInvalidLightName(String name,boolean table) {
	    messageGeneralInvalidBean("Error16", name, table);
	}
	
	/**
	 * Sends an invalid memory system name error message for Edit Conditional window
	 */
	void messageInvalidMemoryName(String name,boolean table) {
	    messageGeneralInvalidBean("Error17", name, table);
	}
	
	/**
	 * Sends an invalid route name error message for Edit Conditional window
	 */
	void messageInvalidRouteName(String name) {
	    messageGeneralInvalidBean("Error18", name, true);
	}
	
	/**
	 * Sends an invalid conditional name error message for Edit Conditional window
	 */
	void messageInvalidConditionalName(String name) {
	    messageGeneralInvalidBean("Error22", name, true);
	}
	
	/**
	 * Sends an invalid logix name error message for Edit Conditional window
	 */
	void messageInvalidLogixName(String name) {
	    messageGeneralInvalidBean("Error22", name, true);
	}
	
	/**
	 * Sends a duplicate Conditional user name message for Edit Logix window
	 */
	void messageDuplicateConditionalUserName(String svName) {
	    messageGeneralInvalidBean("Error30", svName, false);
	}
	
	//  *********** Special Table Models ********************
	
    /**
     * Table model for Conditionals in Edit Logix window
     */
    public class ConditionalTableModel extends AbstractTableModel
				implements PropertyChangeListener {
		
        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int STATE_COLUMN = 2;
        public static final int BUTTON_COLUMN = 3;
		
		public ConditionalTableModel() {
			super();
			conditionalManager.addPropertyChangeListener(this);
			updateConditionalListeners();
		}

		synchronized void updateConditionalListeners() {
			// first, remove listeners from the individual objects
			String sNam = "";
			Conditional c = null;
			if (numConditionals > 0) {
				for (int i = 0; i< numConditionals; i++) {
					// if object has been deleted, it's not here; ignore it
					sNam = curLogix.getConditionalByNumberOrder(i);
					c = conditionalManager.getBySystemName(sNam);
					if (c!=null)
						c.removePropertyChangeListener(this);
				}
			}
			// and add them back in
			for (int i = 0; i< numConditionals; i++) {
				sNam = curLogix.getConditionalByNumberOrder(i);
				c = conditionalManager.getBySystemName(sNam);
				if (c!=null)
					addPropertyChangeListener(this);					
			}
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				// a new NamedBean is available in the manager
				updateConditionalListeners();
				fireTableDataChanged();
			} 
			else if (matchPropertyName(e)) {
				// a value changed.          
				fireTableDataChanged();
			}
		}

		/**
		* Is this property event announcing a change this table should display?
		* <P>
		* Note that events will come both from the NamedBeans and also from the manager
		*/
		boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			return (e.getPropertyName().indexOf("State")>=0 || e.getPropertyName().indexOf("Appearance")>=0);
		}
		
        public Class getColumnClass(int c) {
            if (c == BUTTON_COLUMN) {
                return JButton.class;
            }
            else {
                return String.class;
            }
        }

        public int getColumnCount () {
			return 4;
		}

        public int getRowCount () {
			return (numConditionals);
        }

        public boolean isCellEditable(int r,int c) {
			if (!inReorderMode) {
				return ( (c==UNAME_COLUMN) || (c==BUTTON_COLUMN) );
			}
			else if (c==BUTTON_COLUMN) {
				if (r >= nextInOrder) return (true);
			}
			return (false);
        }

        public String getColumnName(int col) {
			switch (col) {
				case SNAME_COLUMN:
					return rbx.getString("ColumnLabelSystemName");
				case UNAME_COLUMN:
					return rbx.getString("ColumnLabelUserName");
				case BUTTON_COLUMN:
					return "";  // no label
				case STATE_COLUMN:
					return rbx.getString("ColumnLabelState");
				default:
					return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case SNAME_COLUMN:
				return new JTextField(6).getPreferredSize().width;
			case UNAME_COLUMN:
				return new JTextField(17).getPreferredSize().width;
			case BUTTON_COLUMN:
				return new JTextField(6).getPreferredSize().width;
			case STATE_COLUMN:
				return new JTextField(12).getPreferredSize().width;
			default:
				return new JTextField(5).getPreferredSize().width;
			}
		}

        public Object getValueAt (int r,int c) {
            int rx = r;
            if ( (rx > numConditionals) || (curLogix==null) ) {
                return null;
            }
            switch (c) {
				case BUTTON_COLUMN:
					if (!inReorderMode) {
						return rb.getString("ButtonEdit");
					}
					else if (nextInOrder==0) {
						return rbx.getString("ButtonFirst");
					}
					else if (nextInOrder <= r) {
						return rbx.getString("ButtonNext");
					}
					else return Integer.toString(rx+1);
				case SNAME_COLUMN:  
					return curLogix.getConditionalByNumberOrder(rx);
				case UNAME_COLUMN:  //
					return conditionalManager.
						getBySystemName(curLogix.getConditionalByNumberOrder(rx)).getUserName();
				case STATE_COLUMN:  //
					int curState = conditionalManager.
						getBySystemName(curLogix.getConditionalByNumberOrder(rx)).getState();
					if (curState == Conditional.TRUE) return rbx.getString("True");
					if (curState == Conditional.FALSE) return rbx.getString("False");
					return rbx.getString("Unknown");
				default:
					return rbx.getString("Unknown");
            }
        }
		
        public void setValueAt(Object value,int row,int col) {
            int rx = row;
            if ( (rx > numConditionals) || (curLogix==null) ) {
                return;
            }
			if (col == BUTTON_COLUMN) {
				// button fired
				if (inReorderMode) {
					if (curLogix.nextConditionalInOrder(rx)) {
						// reordering done
						status.setText("");
						inReorderMode = false;
					}
					else {
						nextInOrder ++;
					}
					fireTableDataChanged();
				}
				else {
					editConditionalPressed(rx);
				} 			
			}
			else if (col == UNAME_COLUMN) {
				String uName = (String)value;
				if (curLogix!=null) {
					Conditional cn = conditionalManager.getByUserName(curLogix,uName.trim());
					if (cn==null) {
						conditionalManager.getBySystemName(
							curLogix.getConditionalByNumberOrder(rx)).setUserName(uName.trim());
						fireTableRowsUpdated(rx,rx);
					}
					else {
						String svName = curLogix.getConditionalByNumberOrder(rx);
						if (cn != conditionalManager.getBySystemName(svName)) {
							messageDuplicateConditionalUserName(cn.getSystemName());
						}
					}
				}
			}
        }
    } 
	   
    /**
     * Table model for State Variables in Edit Conditional window
     */
    public class VariableTableModel extends AbstractTableModel {
		
        public static final int AND_COLUMN = 0;
        public static final int NOT_COLUMN = 1;
        public static final int TYPE_COLUMN = 2;
        public static final int SNAME_COLUMN = 3;
		public static final int DATA1_COLUMN = 4;
		public static final int DATA2_COLUMN = 5;
		public static final int STATE_COLUMN = 6;
		public static final int TRIGGERS_COLUMN = 7;
		public static final int DELETE_COLUMN = 8;
				
        public Class getColumnClass(int c) {
			if (c == NOT_COLUMN) return JComboBox.class;
			if (c == TYPE_COLUMN) return JComboBox.class;
			if (c == TRIGGERS_COLUMN) return Boolean.class;
			if (c == DELETE_COLUMN) return JButton.class;
			return String.class;
        }

        public int getColumnCount () {
			return 9;
		}

        public int getRowCount () {
			return (numStateVariables);
        }

        public boolean isCellEditable(int r,int c) {
			switch (c) {
				case AND_COLUMN:
					return (false);
				case NOT_COLUMN:
					return (true);
				case TYPE_COLUMN:
					return (true);
				case SNAME_COLUMN:
					return (variableNameEditable[r]);
				case DATA1_COLUMN:
					return (variableData1Editable[r]);
				case DATA2_COLUMN:
					return (variableData2Editable[r]);
				case STATE_COLUMN:
					return (false);
				case TRIGGERS_COLUMN:
					return (true);
				case DELETE_COLUMN:
					return (true);
			}
			return (false);
        }

        public String getColumnName(int col) {
			switch (col) {
				case AND_COLUMN:
					return ("  ");
				case NOT_COLUMN:
					return ("  ");
				case TYPE_COLUMN:
					return (rbx.getString("ColumnLabelVariableType"));
				case SNAME_COLUMN:
					return (rbx.getString("ColumnLabelName"));
				case DATA1_COLUMN:
					return (rbx.getString("ColumnLabelData1"));
				case DATA2_COLUMN:
					return (rbx.getString("ColumnLabelData2"));
				case STATE_COLUMN:
					return (rbx.getString("ColumnLabelState"));
				case TRIGGERS_COLUMN:
					return (rbx.getString("ColumnLabelTriggersCalculation"));
				case DELETE_COLUMN:
					return ("  ");
			}
			return "";
		}

		public int getPreferredWidth(int col) {
			switch (col) {
				case AND_COLUMN:
					return new JTextField(3).getPreferredSize().width;
				case NOT_COLUMN:
					return new JTextField(4).getPreferredSize().width;
				case TYPE_COLUMN:
					return new JTextField(14).getPreferredSize().width;
				case SNAME_COLUMN:
					return new JTextField(5).getPreferredSize().width;
				case DATA1_COLUMN:
					return new JTextField(4).getPreferredSize().width;
				case DATA2_COLUMN:
					return new JTextField(4).getPreferredSize().width;
				case STATE_COLUMN:
					return new JTextField(4).getPreferredSize().width;
				case TRIGGERS_COLUMN:
					return new JTextField(4).getPreferredSize().width;
				case DELETE_COLUMN:
					return new JTextField(4).getPreferredSize().width;
			}
			return new JTextField(5).getPreferredSize().width;
		}

        public Object getValueAt (int r,int c) {
            int rx = r;
            if (rx >= numStateVariables) {
                return null;
            }
			switch (c) {
				case AND_COLUMN:
					if (r==0) return ("");
					return rbx.getString("LogicAND");
				case NOT_COLUMN:
					return variableNOT[rx];
				case TYPE_COLUMN:
					return stateVariableTypeToString(variableType[rx]);
				case SNAME_COLUMN:
					return variableName[rx];
				case DATA1_COLUMN:
					return variableData1[rx];
				case DATA2_COLUMN:
					return variableData2[rx];
				case STATE_COLUMN:
					return variableState[rx];
				case TRIGGERS_COLUMN:
					return new Boolean(variableTriggersCalculation[rx]);
				case DELETE_COLUMN:
					return rbx.getString("ButtonDelete");
			}
			return null;
        }
		
        public void setValueAt(Object value,int row,int col) {
            int rx = row;
            if (rx > numStateVariables) {
                return;
            }
			switch (col) {
				case NOT_COLUMN:
					variableNOT[rx] = (String)value;
					break;
				case TYPE_COLUMN:
					variableTypeChanged(rx,(String)value);
					break;
				case SNAME_COLUMN:
					variableName[rx] = (String)value;
					break;
				case DATA1_COLUMN:
					variableData1[rx] = (String)value;
					break;
				case DATA2_COLUMN:
					variableData2[rx] = (String)value;
					break;
				case TRIGGERS_COLUMN:
					variableTriggersCalculation[rx] = !variableTriggersCalculation[rx];
					break;
				case DELETE_COLUMN:
					deleteVariablePressed(row);
					break;
			}
        }
    }    
	
	static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LogixTableAction.class.getName());
}
/* @(#)LogixTableAction.java */

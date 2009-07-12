// LogixTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Light;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.implementation.DefaultConditionalAction;
import jmri.ConditionalVariable;
import jmri.ConditionalManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.implementation.DefaultSignalHead;
import jmri.Route;
import jmri.Memory;
import jmri.Timebase;
import jmri.jmrit.sensorgroup.SensorGroupFrame;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.util.ArrayList;
import java.util.ResourceBundle;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.util.Date;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.table.*;

//import java.util.List;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a Logix Table.
 * <P>
 * Also contains the windows to create, edit, and delete a Logix. Also contains
 * the window to define and edit a Conditional.
 * <P>
 * Most of the text used in this GUI is in LogixTableBundle.properties, accessed
 * via rbx, and the remainder of the text is in BeanTableBundle.properties,
 * accessed via rb.
 *
 * Methods and Members for 'state variables' and 'actions' removed to become their
 * own objects - 'ConditionalVariable' and 'ConditionalAction' in jmri package.
 * Two more types of logic for a Conditional to use in its antecedent have been added
 * to the original 'AND'ing all statevariables - 'OR' (i.e. all OR's) and 'MIXED' 
 * (i.e. general boolean statement with any mixture of boolean operations). 
 * The 'OR's an 'AND's types are unambiguous and do not require parentheses.
 * The 'Mixed' type uses a TextField for the user to insert parenthees. 
 * Jan 22, 2009 - Pete Cressman
 * 
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009
 * @version $Revision: 1.40.2.1 $
 */

public class LogixTableAction extends AbstractTableAction {

	/**
	 * Create an action with a specific title.
	 * <P>
	 * Note that the argument is the Action title, not the title of the
	 * resulting frame. Perhaps this should be changed?
	 * 
	 * @param s
	 */
	public LogixTableAction(String s) {
		super(s);
		// set up managers - no need to use InstanceManager since both managers are
		// Default only (internal). We use InstanceManager to get managers for
		// compatibility with other facilities.
		_logixManager = InstanceManager.logixManagerInstance();
		_conditionalManager = InstanceManager.conditionalManagerInstance();
		// disable ourself if there is no Logix manager or no Conditional manager available
		if ((_logixManager == null) || (_conditionalManager == null)) {
			setEnabled(false);
		}
	}

	public LogixTableAction() {
		this("Logix Table");
	}

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

	// *********** Methods for Logix Table Window ********************

	/**
	 * Create the JTable DataModel, along with the changes (overrides of
	 * BeanTableDataModel) for the specific case of a Logix table. Note: Table
	 * Models for the Conditional table in the Edit Logix window, and the State
	 * Variable table in the Edit Conditional window are at the end of this
	 * module.
	 */
	void createModel() {
		m = new BeanTableDataModel() {
			// overlay the state column with the edit column
			static public final int ENABLECOL = VALUECOL;
			static public final int EDITCOL = DELETECOL;
			protected String enabledString = rb.getString("ColumnHeadEnabled");

			public String getColumnName(int col) {
				if (col == EDITCOL)
					return ""; // no heading on "Edit"
				if (col == ENABLECOL)
					return enabledString;
				else
					return super.getColumnName(col);
			}

			public Class<?> getColumnClass(int col) {
				if (col == EDITCOL)
					return String.class;
				if (col == ENABLECOL)
					return Boolean.class;
				else
					return super.getColumnClass(col);
			}

			public int getPreferredWidth(int col) {
				// override default value for SystemName and UserName columns
				if (col == SYSNAMECOL)
					return new JTextField(12).getPreferredSize().width;
				if (col == USERNAMECOL)
					return new JTextField(17).getPreferredSize().width;
				if (col == EDITCOL)
					return new JTextField(12).getPreferredSize().width;
				if (col == ENABLECOL)
					return new JTextField(5).getPreferredSize().width;
				else
					return super.getPreferredWidth(col);
			}

			public boolean isCellEditable(int row, int col) {
				if (col == EDITCOL)
					return true;
				if (col == ENABLECOL)
					return true;
				else
					return super.isCellEditable(row, col);
			}

			public Object getValueAt(int row, int col) {
				if (col == EDITCOL) {
					return rbx.getString("ButtonSelect");
				} else if (col == ENABLECOL) {
				    Logix logix = (Logix) getBySystemName((String) getValueAt(row,
									SYSNAMECOL));
			        if (logix == null) return null;
					return new Boolean(
							logix.getEnabled());
				} else
					return super.getValueAt(row, col);
			}

			public void setValueAt(Object value, int row, int col) {
				if (col == EDITCOL) {
					// set up to edit
					String sName = (String) getValueAt(row, SYSNAMECOL);
                    if ( rbx.getString("ButtonEdit").equals(value) ) {
                        editPressed(sName);
                    } else if (rbx.getString("ButtonCopy").equals(value) ) {
                        copyPressed(sName);
                    } else if ( rbx.getString("ButtonDelete").equals(value) ) {
                        deletePressed(sName);
                    } else
                        log.debug("Logix table setValueAt column "+EDITCOL+" = "+value);
				} else if (col == ENABLECOL) {
					// alternate
					Logix x = (Logix) getBySystemName((String) getValueAt(row,
							SYSNAMECOL));
					boolean v = x.getEnabled();
					x.setEnabled(!v);
				} else
					super.setValueAt(value, row, col);
			}

			/**
			 * Delete the bean after all the checking has been done.
			 * <P>
			 * Deactivate the Logix and remove it's conditionals
			 */
			void doDelete(NamedBean bean) {
				Logix l = (Logix) bean;
				l.deActivateLogix();
				// delete the Logix and all its Conditionals
				_logixManager.deleteLogix(l);
			}

			boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
				if (e.getPropertyName().equals(enabledString))
					return true;
				else
					return super.matchPropertyName(e);
			}

			public Manager getManager() {
				return InstanceManager.logixManagerInstance();
			}

			public NamedBean getBySystemName(String name) {
				return InstanceManager.logixManagerInstance().getBySystemName(
						name);
			}
			
			public NamedBean getByUserName(String name) {
				return InstanceManager.logixManagerInstance().getByUserName(
						name);
			}

            public void configureTable(JTable table) {
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                super.configureTable(table);
            }

            /**
            * Replace delete button with comboBox
            */
            void configDeleteColumn(JTable table) {
                JComboBox editCombo = new JComboBox();
                editCombo.addItem(rbx.getString("ButtonSelect"));
                editCombo.addItem(rbx.getString("ButtonEdit"));
                editCombo.addItem(rbx.getString("ButtonCopy"));
                editCombo.addItem(rbx.getString("ButtonDelete"));
                TableColumn col = table.getColumnModel().getColumn(BeanTableDataModel.DELETECOL);
                col.setCellEditor(new DefaultCellEditor(editCombo));
            }


			// Not needed - here for interface compatibility
			public void clickOn(NamedBean t) {
			}

			public String getValue(String s) {
				return "";
			}
		};
	}

	// set title for Logix table
	void setTitle() {
		f.setTitle(f.rb.getString("TitleLogixTable"));

        // Hack into Logix frame to add my junk. (pwc)
        _devNameField = new JTextField(30);
        JPanel panel = makeEditPanel(_devNameField, "ElementName", "ElementNameHint");
        JButton referenceButton = new JButton(rbx.getString("ReferenceButton"));
        panel.add(referenceButton);
        referenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deviceReportPressed(e);
            }
        });
        panel.add(referenceButton);
        panel.setVisible(true);
        f.addToBottomBox(panel);
        JButton orphanButton = new JButton(rbx.getString("OrphanButton"));
        orphanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findOrphansPressed(e);
            }
        });
        f.addToBottomBox(orphanButton);
        JButton emptyButton = new JButton(rbx.getString("EmptyButton"));
        emptyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findEmptyPressed(e);
            }
        });
        f.addToBottomBox(emptyButton);

        JMenu menu = new JMenu(rbx.getString("OptionsMenu"));
        menu.setMnemonic(KeyEvent.VK_O);

        menu.addSeparator();
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(rbx.getString("SuppressWithDisable"));
        cbMenuItem.setSelected(_suppressReminder);
        cbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SuppressReminder(false);
            }
        });
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem(rbx.getString("SuppressWithEnable"));
        cbMenuItem.setSelected(_suppressDisable);
        cbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SuppressReminder(true);
            }
        });
        menu.add(cbMenuItem);

        javax.swing.JMenuBar menuBar = f.getJMenuBar();
        menuBar.add(menu);
	}

	String helpTarget() {
		return "package.jmri.jmrit.beantable.LogixTable";
	}

    void SuppressReminder(boolean suppressDisable) {
        if (suppressDisable)   {
            _suppressDisable = !_suppressDisable;
        } else {
            _suppressReminder = !_suppressReminder;
        }
    }

	// *********** variable definitions ********************

	// Multi use variables
	ConditionalManager _conditionalManager = null; // set when LogixAction is created

	LogixManager _logixManager = null; // set when LogixAction is created
	boolean _showReminder = false;
    boolean _suppressReminder = false;
    boolean _suppressDisable = false;

	// current focus variables
	Logix _curLogix = null;
	int numConditionals = 0;
	int conditionalRowNumber = 0;
	Conditional _curConditional = null;

	// Add Logix Variables
	JmriJFrame addLogixFrame = null;
	JTextField _systemName = new JTextField(10);
	JTextField _addUserName = new JTextField(10);
    JTextField _devNameField;

	// Edit Logix Variables
	JmriJFrame editLogixFrame = null;
	boolean inEditMode = false;
	boolean inCopyMode = false;
	boolean _inReorderMode = false;
	int _nextInOrder = 0;
	JTextField editUserName = new JTextField(20);
	ConditionalTableModel conditionalTableModel = null;
	JLabel status = new JLabel(" ");

	// Edit Conditional Variables
	boolean inEditConditionalMode = false;
	JmriJFrame editConditionalFrame = null;
	JTextField conditionalUserName = new JTextField(20);
   
	private ActionTableModel _actionTableModel = null;
	private VariableTableModel _variableTableModel = null;
    private JComboBox _operatorBox;
    private JComboBox _andOperatorBox;
    private JComboBox _notOperatorBox;
    private JTextField _antecedentField;
    private JPanel _antecedentPanel;
    private int _logicType = Conditional.ALL_AND;
    private String _antecedent = null;
    private boolean _newItem = false;   // marks a new Action or Variable object added

    /***  Conponents of Edit Variable Windows */
	JmriJFrame _editVariableFrame = null;
    JComboBox _variableTypeBox;
	JTextField _variableNameField;
	JTextField _variableData1Field;
	JTextField _variableData2Field;
    JPanel _variableNamePanel;
    JPanel _variableData1Panel;
    JPanel _variableData2Panel;

    /***  Conponents of Edit Action Windows */
	JmriJFrame _editActionFrame = null;
    JComboBox _actionOptionBox;
    JComboBox _actionTypeBox;
	JTextField _actionNameField;
	JTextField _actionStringField;
	JComboBox _actionTurnoutSetBox;
	JComboBox _actionSensorSetBox;
	JComboBox _actionSignalSetBox;
	JComboBox _actionLightSetBox;
	JComboBox _actionLockSetBox;
	JButton _actionSetButton;
    JPanel _optionPanel;
    JPanel _namePanel;
    JPanel _turnoutPanel;
    JPanel _sensorPanel;
    JPanel _lightPanel;
    JPanel _signalPanel;
    JPanel _lockPanel;
    JPanel _setPanel;
    JPanel _textPanel;

	// Current Variable Information
    private ArrayList <ConditionalVariable> _variableList;
    private ConditionalVariable _curVariable;
    private int _curVariableRowNumber;

	// Current Action Information
    private ArrayList <ConditionalAction> _actionList;
    private ConditionalAction _curAction;
    private int _curActionRowNumber;

    void findEmptyPressed(ActionEvent e) {
        Maintenance.findEmptyPressed(f);
    }

    void findOrphansPressed(ActionEvent e) {
        Maintenance.findOrphansPressed(f);
    }

    void deviceReportPressed(ActionEvent e) {
        Maintenance.deviceReportPressed(_devNameField.getText(), f);
    }

	// *********** Methods for Add Logix Window ********************

	/**
	 * Responds to the Add button in Logix table Creates and/or initializes the
	 * Add Logix window
	 */
	void addPressed(ActionEvent e) {
		// possible change
        if (!checkFlags(null)) {
            return;
        }
		_showReminder = true;
		// make an Add Logix Frame
		if (addLogixFrame == null) {
            JPanel panel5 = makeAddLogixFrame("TitleAddLogix", "AddLogixMessage");
			// Create Logix
            JButton create = new JButton(rbx.getString("CreateLogixButton"));
			panel5.add(create);
			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createPressed(e);
				}
			});
			create.setToolTipText(rbx.getString("LogixCreateButtonHint"));
        }
		addLogixFrame.pack();
		addLogixFrame.setVisible(true);
	}

    /**
    *  shared method for window to create or copy Logix
    * Returns the button panel
    */
    JPanel makeAddLogixFrame(String titleId, String messageId) {
        addLogixFrame = new JmriJFrame(rbx.getString(titleId));
        addLogixFrame.addHelpMenu(
                "package.jmri.jmrit.beantable.LogixAddEdit", true);
        addLogixFrame.setLocation(50, 30);
        Container contentPane = addLogixFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        JLabel systemNameLabel = new JLabel(rbx.getString("LogixSystemName"));
        panel1.add(systemNameLabel);
        panel1.add(_systemName);
        _systemName.setToolTipText(rbx.getString("LogixSystemNameHint"));
        contentPane.add(panel1);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JLabel userNameLabel = new JLabel(rbx.getString("LogixUserName"));
        panel2.add(userNameLabel);
        panel2.add(_addUserName);
        _addUserName.setToolTipText(rbx.getString("LogixUserNameHint"));
        contentPane.add(panel2);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        JLabel message1 = new JLabel(rbx.getString(messageId+"1"));
        panel31.add(message1);
        JPanel panel32 = new JPanel();
        JLabel message2 = new JLabel(rbx.getString(messageId+"2"));
        panel32.add(message2);
        panel3.add(panel31);
        panel3.add(panel32);
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(rbx.getString("CancelButton")); 
        panel5.add(cancel);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelAddPressed(e);
            }
        });
        cancel.setToolTipText(rbx.getString("CancelLogixButtonHint"));

        addLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelAddPressed(null);
            }
        });
        contentPane.add(panel5);
        return panel5;
    }

	/**
	 * Responds to the Cancel button in Add Logix window Note: Also get there if
	 * the user closes the Add Logix window
	 */
	void cancelAddPressed(ActionEvent e) {
		addLogixFrame.setVisible(false);
		addLogixFrame.dispose();
		addLogixFrame = null;
        inCopyMode = false;
		f.setVisible(true);
	}

    void copyPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        // Use separate Thread so window is created on top
        Runnable t = new Runnable() {
                public void run() {
                    //Thread.yield();
                    JPanel panel5 = makeAddLogixFrame("TitleCopyLogix", "CopyLogixMessage");
                    // Create Logix
                    JButton create = new JButton(rbx.getString("ButtonCopy"));
                    panel5.add(create);
                    create.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            copyLogixPressed(e);
                        }
                    });
                    addLogixFrame.pack();
                    addLogixFrame.setVisible(true);
                    }
                };
        log.debug("copyPressed Thread started for " + sName);
        javax.swing.SwingUtilities.invokeLater(t);
        inCopyMode = true;
        _logixSysName = sName;
    }

    String _logixSysName;

	void copyLogixPressed(ActionEvent e) {
		String uName = _addUserName.getText().trim();
        if (!checkLogixSysName()) {
            return;
        }
		String sName = _systemName.getText().toUpperCase().trim();
		// check if a Logix with this name already exists
        boolean createLogix = true;
		Logix targetLogix = _logixManager.getBySystemName(sName);
        if (targetLogix != null) {
            int result = JOptionPane.showConfirmDialog(f, java.text.MessageFormat.format(
                                                rbx.getString("ConfirmLogixDuplicate"),  
                                                new Object[] {sName, _logixSysName}),
                                                rbx.getString("ConfirmTitle"), JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
            if (JOptionPane.NO_OPTION == result) {
                return;
            }
            createLogix = false;
            String userName = targetLogix.getUserName();
            if (userName.length() > 0) {
                _addUserName.setText(userName);
                uName = userName;
            }
        } else if (!checkLogixUserName(uName)) {
            return;
        }
        if (createLogix) {
            // Create the new Logix
            targetLogix = _logixManager.createNewLogix(sName, uName);
            if (targetLogix == null) {
                // should never get here unless there is an assignment conflict
                log.error("Failure to create Logix with System Name: " + sName);
                return;
            }
        } else if (targetLogix == null) {
            log.error("Error targetLogix is null!");
            return;
        } else {
            targetLogix.setUserName(uName);
        }
        Logix srcLogic = _logixManager.getBySystemName(_logixSysName);
        for (int i=0; i<srcLogic.getNumConditionals(); i++) {
            String cSysName = srcLogic.getConditionalByNumberOrder(i);
            copyConditionalToLogix(cSysName, srcLogic, targetLogix);
        }
        cancelAddPressed(null);
    }

    void copyConditionalToLogix(String cSysName, Logix srcLogix, Logix targetLogix) {
        Conditional cOld = _conditionalManager.getBySystemName(cSysName);
        if (cOld == null) {
            log.error("Failure to find Conditional with System Name: " + cSysName);
            return;
        }
        String cOldSysName = cOld.getSystemName();
        String cOldUserName = cOld.getUserName();

		// make system name for new conditional
		int num = targetLogix.getNumConditionals()+1;
		String cNewSysName = targetLogix.getSystemName() + "C" + Integer.toString(num);
		// add to Logix at the end of the calculate order
        String cNewUserName = java.text.MessageFormat.format(rbx.getString("CopyOf"), cOldUserName);
        if (cOldUserName.length() == 0){
            cNewUserName += "C"+Integer.toString(num);
        }
        do {
            cNewUserName = JOptionPane.showInputDialog(f, java.text.MessageFormat.format(
                                                    rbx.getString("NameConditionalCopy"), new Object[] {
                                                    cOldUserName, cOldSysName, _logixSysName, 
                                                    targetLogix.getUserName(), targetLogix.getSystemName()}),
                                                    cNewUserName);
            if (cNewUserName == null || cNewUserName.length()==0) {
                return;
            }
        } while (!checkConditionalUserName(cNewUserName, targetLogix) );

		Conditional cNew = _conditionalManager.createNewConditional(cNewSysName, cNewUserName);
		if (cNew == null) {
			// should never get here unless there is an assignment conflict
			log.error("Failure to create Conditional with System Name: \""
					+ cNewSysName+"\" and User Name: \""+ cNewUserName+"\"");
			return;
		}
        cNew.setLogicType(cOld.getLogicType(), cOld.getAntecedentExpression());
        cNew.setStateVariables(cOld.getCopyOfStateVariables());
        cNew.setAction(cOld.getCopyOfActions());
		targetLogix.addConditional(cNewSysName, -1);
    }

    boolean checkLogixUserName(String uName) {
		// check if a Logix with the same user name exists
		if (uName.length() > 0) {
			Logix x = _logixManager.getByUserName(uName);
			if (x != null) {
				// Logix with this user name already exists
				javax.swing.JOptionPane.showMessageDialog(addLogixFrame, rbx
						.getString("Error3"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
        return true;
    }

    boolean checkLogixSysName() {
		// check validity of Logix system name
		String sName = _systemName.getText().toUpperCase().trim();
		if ( (sName.length() < 1)) {
			// Entered system name is blank or too short
			javax.swing.JOptionPane.showMessageDialog(addLogixFrame, rbx
					.getString("Error8"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ((sName.length() < 2) || (sName.charAt(0) != 'I')
				|| (sName.charAt(1) != 'X')) {
			// System name does not begin with IX, prefix IX to it
			String s = sName;
			sName = "IX" + s;
		}
		_systemName.setText(sName);
        return true;
    }

    boolean checkFlags(String sName) {
		if (inEditMode) {
			// Already editing a Logix, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
					java.text.MessageFormat.format(rbx.getString("Error32"),
					new Object[] { _curLogix.getSystemName() }), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (inCopyMode) {
			// Already editing a Logix, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
					java.text.MessageFormat.format(rbx.getString("Error31"),
					new Object[] { _logixSysName }), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
        if (sName != null) {
            // check if a Logix with this name exists
            Logix x = _logixManager.getBySystemName(sName);
            if (x == null) {
                // Logix does not exist, so cannot be edited
                log.error("No Logix with system name: " + sName);
                javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
                        .getString("Error5"), rbx.getString("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                if (editLogixFrame != null) {
                    editLogixFrame.setVisible(false);
                }
                return false;
            }
        }
        return true;
    }

	/**
	 * Responds to the Create Logix button in Add Logix window
	 */
	void createPressed(ActionEvent e) {
		// possible change
		_showReminder = true;
		String uName = _addUserName.getText().trim();
        if (!checkLogixSysName()) {
            return;
        }
		String sName = _systemName.getText().toUpperCase().trim();
		// check if a Logix with this name already exists
		Logix x = _logixManager.getBySystemName(sName);
		if (x != null) {
			// Logix already exists
			javax.swing.JOptionPane.showMessageDialog(addLogixFrame, rbx
					.getString("Error1"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
        if (!checkLogixUserName(uName)) {
            return;
        }
		// Create the new Logix
		_curLogix = _logixManager.createNewLogix(sName, uName);
		if (_curLogix == null) {
			// should never get here unless there is an assignment conflict
			log.error("Failure to create Logix with System Name: " + sName);
			return;
		}
		numConditionals = 0;
        cancelAddPressed(null);
		// create the Edit Logix Window
        makeEditLogixWindow();
	}

	// *********** Methods for Edit Logix Window ********************

	/**
	 * Responds to the Edit button pressed in Logix table
	 */
	void editPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
		// Logix was found, initialize for edit
		_curLogix =  _logixManager.getBySystemName(sName);
		numConditionals = _curLogix.getNumConditionals();
		// create the Edit Logix Window
        // Use separate Thread so window is created on top
        Runnable t = new Runnable() {
                public void run() {
                    //Thread.yield();
                    makeEditLogixWindow();
                    }
                };
        log.debug("editPressed Thread started for " + sName);
        javax.swing.SwingUtilities.invokeLater(t);
	}

	/**
	 * creates and/or initializes the Edit Logix window
	 */
	void makeEditLogixWindow() {
        //log.debug("makeEditLogixWindow ");
		editUserName.setText(_curLogix.getUserName());
		// clear conditional table if needed
		if (conditionalTableModel != null) {
			conditionalTableModel.fireTableStructureChanged();
		}
		inEditMode = true;
		if (editLogixFrame == null) {
			editLogixFrame = new JmriJFrame(rbx.getString("TitleEditLogix"));
			editLogixFrame.addHelpMenu(
					"package.jmri.jmrit.beantable.LogixAddEdit", true);
			editLogixFrame.setLocation(100, 30);
			Container contentPane = editLogixFrame.getContentPane();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel();
			panel1.setLayout(new FlowLayout());
			JLabel systemNameLabel = new JLabel(rbx
					.getString("LogixSystemName"));
			panel1.add(systemNameLabel);
            JLabel fixedSystemName = new JLabel(_curLogix.getSystemName());
			panel1.add(fixedSystemName);
			contentPane.add(panel1);
			JPanel panel2 = new JPanel();
			panel2.setLayout(new FlowLayout());
			JLabel userNameLabel = new JLabel(rbx.getString("LogixUserName"));
			panel2.add(userNameLabel);
			panel2.add(editUserName);
			editUserName.setToolTipText(rbx.getString("LogixUserNameHint2"));
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
			JPanel pct = new JPanel();
			// initialize table of conditionals
			conditionalTableModel = new ConditionalTableModel();
			JTable conditionalTable = new JTable(conditionalTableModel);
			conditionalTable.setRowSelectionAllowed(false);
			conditionalTable.setPreferredScrollableViewportSize(
							new java.awt.Dimension(530, 450));
			TableColumnModel conditionalColumnModel = conditionalTable
					.getColumnModel();
			TableColumn sNameColumn = conditionalColumnModel
					.getColumn(ConditionalTableModel.SNAME_COLUMN);
			sNameColumn.setResizable(true);
			sNameColumn.setMinWidth(100);
			sNameColumn.setMaxWidth(130);
			TableColumn uNameColumn = conditionalColumnModel
					.getColumn(ConditionalTableModel.UNAME_COLUMN);
			uNameColumn.setResizable(true);
			uNameColumn.setMinWidth(210);
			uNameColumn.setMaxWidth(260);
			TableColumn stateColumn = conditionalColumnModel
					.getColumn(ConditionalTableModel.STATE_COLUMN);
			stateColumn.setResizable(false);
			stateColumn.setMinWidth(90);
			stateColumn.setMaxWidth(100);
			TableColumn buttonColumn = conditionalColumnModel
					.getColumn(ConditionalTableModel.BUTTON_COLUMN);

			// install button renderer and editor
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			conditionalTable.setDefaultRenderer(JButton.class, buttonRenderer);
			TableCellEditor buttonEditor = new ButtonEditor(new JButton());
			conditionalTable.setDefaultEditor(JButton.class, buttonEditor);
			JButton testButton = new JButton("XXXXX");
			conditionalTable.setRowHeight(testButton.getPreferredSize().height);
			buttonColumn.setMinWidth(testButton.getPreferredSize().width);
			buttonColumn.setResizable(false);

			JScrollPane conditionalTableScrollPane = new JScrollPane(
					conditionalTable);
			pct.add(conditionalTableScrollPane, BorderLayout.CENTER);
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
            JButton newConditionalButton = new JButton(rbx.getString("NewConditionalButton"));
			panel42.add(newConditionalButton);
			newConditionalButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newConditionalPressed(e);
				}
			});
			newConditionalButton.setToolTipText(rbx.getString("NewConditionalButtonHint"));
			// Conditional panel buttons - Reorder
            JButton reorderButton = new JButton(rbx.getString("ReorderButton"));
			panel42.add(reorderButton);
			reorderButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					reorderPressed(e);
				}
			});
			reorderButton.setToolTipText(rbx.getString("ReorderButtonHint"));
			// Conditional panel buttons - Calculate
            JButton calculateButton = new JButton(rbx.getString("CalculateButton"));
			panel42.add(calculateButton);
			calculateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					calculatePressed(e);
				}
			});
			calculateButton.setToolTipText(rbx.getString("CalculateButtonHint"));
			panel4.add(panel42);
			Border panel4Border = BorderFactory.createEtchedBorder();
			panel4.setBorder(panel4Border);
			contentPane.add(panel4);
			// add buttons at bottom of window
			JPanel panel5 = new JPanel();
			panel5.setLayout(new FlowLayout());
			// Bottom Buttons - Done Logix
            JButton done = new JButton(rbx.getString("DoneButton"));
			panel5.add(done);
			done.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					donePressed(e);
				}
			});
			done.setToolTipText(rbx.getString("DoneButtonHint"));
			// Delete Logix
            JButton delete = new JButton(rbx.getString("DeleteLogixButton"));
			panel5.add(delete);
			delete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deletePressed(e);
				}
			});
			delete.setToolTipText(rbx.getString("DeleteLogixButtonHint"));
			contentPane.add(panel5);
		}

        editLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        showSaveReminder();
                        if (inEditMode) {
                            donePressed(null);
                        } else {
                            editLogixFrame.setVisible(false);
                            // bring Logix Table to front
                            f.setVisible(true);
                        }
                    }
                });
		editLogixFrame.pack();
		editLogixFrame.setVisible(true);
	}

	/**
	 * Display reminder to save
	 */
	void showSaveReminder() {
		if (_showReminder && !_suppressReminder) {
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
                    .getString("Reminder1"),
                    rbx.getString("ReminderTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Responds to the Reorder Button in the Edit Logix window
	 */
	void reorderPressed(ActionEvent e) {
		if (checkEditConditional())
			return;
		// Check if reorder is reasonable
		_showReminder = true;
		_nextInOrder = 0;
		_inReorderMode = true;
		status.setText(rbx.getString("ReorderMessage"));
		conditionalTableModel.fireTableDataChanged();
    }

	/**
	 * Responds to the First/Next (Delete) Button in the Edit Logix window
	 */
    void swapConditional(int row) {
        _curLogix.swapConditional(_nextInOrder, row);
        _nextInOrder++;
        if (_nextInOrder >= numConditionals)
        {
            _inReorderMode = false;
        }
        //status.setText("");
        conditionalTableModel.fireTableDataChanged();
    } 

	/**
	 * Responds to the Calculate Button in the Edit Logix window
	 */
	void calculatePressed(ActionEvent e) {
		if (checkEditConditional())
			return;
		// are there Conditionals to calculate?
		if (numConditionals > 0) {
			// There are conditionals to calculate
			String cName = "";
			Conditional c = null;
			for (int i = 0; i < numConditionals; i++) {
				cName = _curLogix.getConditionalByNumberOrder(i);
                if (cName != null) {
                    c = _conditionalManager.getBySystemName(cName);
                    if (c == null) {
                        log.error("Invalid conditional system name when calculating - "
                                        + cName);
                    } else {
                        // calculate without taking any action
                        c.calculate(false, null);
                    }
                } else {
                    log.error("null conditional system name when calculating");
                }
			}
			// force the table to update
			conditionalTableModel.fireTableDataChanged();
		}
	}

	/**
	 * Responds to the Done button in the Edit Logix window Note: also get here
	 * if the Edit Logix window is dismissed, or if the Add button is pressed in
	 * the Logic Table with an active Edit Logix window.
	 */
	void donePressed(ActionEvent e) {
		if (_curLogix == null) {
			log.error("null pointer to _curLogix in donePressed method");
            finishDone();
            return;
		}
		if (checkEditConditional()) {
            return;
        }
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            finishDone();
			return;
        }
        // Check if the User Name has been changed
        String uName = editUserName.getText();
        if (!(uName.equals(_curLogix.getUserName()))) {
            // user name has changed - check if already in use
            Logix p = _logixManager.getByUserName(uName);
            if (p != null) {
                // Logix with this user name already exists
                log.error("Failure to update Logix with Duplicate User Name: "
                                + uName);
                javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                        rbx.getString("Error6"), rbx
                                .getString("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);

            } else {
                // user name is unique, change it
                _curLogix.setUserName(uName);
                m.fireTableDataChanged();
            }
        }
        /* check for possible loop situation
        THIS RAISES MORE FALSE ALARMS THAN ACTUAL HAZARDS
        if (!_suppressDisable)  {
            // user does not want any change to enabling -REGARDLESS!
            boolean disabled = _curLogix.checkLoopCondition();
            int response = 0;
            if (disabled && !_suppressReminder) {
                // loop condition is present - warn user and give options
                ArrayList <String[]> list = _curLogix.getLoopGremlins();
                String msg = "";
                int k = 0;
                for (int i=0; i<list.size(); i++) {
                    String[] str = list.get(i);
                    if (k==7) {
                        msg = msg + "\n";
                        k=0;
                    } else if (i>0) {
                        msg = msg + ", ";
                    }
                    k++;
                    msg = msg + rbx.getString(str[0]) +": "+ str[1];
                }
                response = JOptionPane.showOptionDialog(editLogixFrame, rbx.getString("Warn9")
                        +msg, rbx.getString("WarnTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
                        null, new Object[] {rbx.getString("ButtonDisabled"),
                                rbx.getString("ButtonEnabled") }, rbx.getString("ButtonDisabled"));
            }
            if (disabled && response == 0) {
                if (!_suppressReminder) {
                    // user elected to disable the Logix
                    JOptionPane.showMessageDialog(editLogixFrame, rbx.getString("Logix")
                            + " " + _curLogix.getSystemName() + "( "+ _curLogix.getUserName()
                            + " ) " + rbx.getString("Warn10"), "", JOptionPane.INFORMATION_MESSAGE);
                }
                _curLogix.setEnabled(false);
            }

        } */
        // complete update and activate Logix
        finishDone();
	}  /* donePressed */

    void finishDone() {
		inEditMode = false;
		showSaveReminder();
		editLogixFrame.setVisible(false);
		editLogixFrame.dispose();
		editLogixFrame = null;
		// bring Logix Table to front
		f.setVisible(true);
    }

	/**
	 * Responds to the Delete combo selection Logix window
	 */
	void deletePressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(f, java.text.MessageFormat.format(
                                                rbx.getString("ConfirmLogixDelete"), sName),
                                                rbx.getString("ConfirmTitle"), JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE) )
        {
            Logix x = _logixManager.getBySystemName(sName);
            if (x != null) {
                _logixManager.deleteLogix(x);
            }
        }
		f.setVisible(true);
	}

	/**
	 * Responds to the Delete button in the Edit Logix window
	 */
	void deletePressed(ActionEvent e) {
		if (checkEditConditional())
			return;
		_showReminder = true;
		Logix x = _curLogix;
		// delete this Logix
		_logixManager.deleteLogix(x);
		_curLogix = null;
		inEditMode = false;
		editLogixFrame.setVisible(false);
		editLogixFrame.dispose();
		editLogixFrame = null;
		f.setVisible(true);
	}

	/**
	 * Responds to the New Conditional Button in Edit Logix Window
	 */
	void newConditionalPressed(ActionEvent e) {
		if (checkEditConditional())
			return;
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            javax.swing.JOptionPane.showMessageDialog(
                    editLogixFrame, java.text.MessageFormat.format(rbx.getString("Warn8"),
                        new Object[] {SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName }),
                    rbx .getString("WarnTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
		// make system name for new conditional
		int num = _curLogix.getNumConditionals()+1;
        _curConditional = null;
        String cName = null;
        while (_curConditional == null)  {
            cName = _curLogix.getSystemName() + "C" + Integer.toString(num);
            _curConditional = _conditionalManager.createNewConditional(cName, "");
            num++;
            if (num==1000) break;
        }
		if (_curConditional == null) {
			// should never get here unless there is an assignment conflict
			log.error("Failure to create Conditional with System Name: "
					+ cName);
			return;
		}
		// add to Logix at the end of the calculate order
		_curLogix.addConditional(cName, -1);
		conditionalTableModel.fireTableRowsInserted(numConditionals, numConditionals);
		conditionalRowNumber = numConditionals;
		numConditionals++;
		_showReminder = true;
		// clear action items
        _actionList = new ArrayList <ConditionalAction>();
        _variableList = new  ArrayList <ConditionalVariable>();
		makeEditConditionalWindow();
	}

	/**
	 * Responds to Edit Button in the Conditional table of the Edit Logix Window
	 */
	void editConditionalPressed(int rx) {
		if (inEditConditionalMode) {
			// Already editing a Conditional, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
					java.text.MessageFormat.format(rbx.getString("Error34"),
							new Object[] { _curConditional.getSystemName() }),
					rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		// get Conditional to edit
		_curConditional = _conditionalManager.getBySystemName(_curLogix.getConditionalByNumberOrder(rx));
		if (_curConditional == null) {
			log.error("Attempted edit of non-existant conditional.");
			return;
		}
        _variableList = _curConditional.getCopyOfStateVariables();
		conditionalRowNumber = rx;
		// get action variables
       	_actionList = _curConditional.getCopyOfActions();
		makeEditConditionalWindow();
	}  /* editConditionalPressed */

	/**
	 * Checks if edit of a conditional is in progress Returns true after sending
	 * message if this is the case
	 */
	boolean checkEditConditional() {
		if (inEditConditionalMode) {
			// Already editing a Conditional, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
					java.text.MessageFormat.format(rbx.getString("Error35"),
							new Object[] { _curConditional.getSystemName() }),
					rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}

    boolean checkConditionalUserName(String uName, Logix logix) {
        if ((uName != null) && (!(uName.equals("")))) {
            Conditional p = _conditionalManager.getByUserName(logix, uName);
            if (p != null) {
                // Conditional with this user name already exists
                log.error("Failure to update Conditional with Duplicate User Name: "
                                + uName);
                javax.swing.JOptionPane.showMessageDialog(
                        editConditionalFrame, rbx.getString("Error10"), rbx
                                .getString("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
/*********************** Edit Conditional Window and Methods********************/

	/**
	 * Creates and/or initializes the Edit Conditional window Note: you can get
	 * here via the New Conditional button (newConditionalPressed) or via an
	 * Edit button in the Conditional table of the Edit Logix window.
	 */
	void makeEditConditionalWindow() {
		// deactivate this Logix
		_curLogix.deActivateLogix();
		conditionalUserName.setText(_curConditional.getUserName());
		if (editConditionalFrame == null) {
			editConditionalFrame = new JmriJFrame(rbx.getString("TitleEditConditional"));
			editConditionalFrame.addHelpMenu(
					"package.jmri.jmrit.beantable.ConditionalAddEdit", true);
			//editConditionalFrame.setLocation(50, 5);
			Container contentPane = editConditionalFrame.getContentPane();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel();
			panel1.setLayout(new FlowLayout());
			panel1.add(new JLabel(rbx.getString("ConditionalSystemName")));
			panel1.add(new JLabel(_curConditional.getSystemName()));
			contentPane.add(panel1);
			JPanel panel2 = new JPanel();
			panel2.setLayout(new FlowLayout());
			panel2.add(new JLabel(rbx.getString("ConditionalUserName")));
			panel2.add(conditionalUserName);
			conditionalUserName.setToolTipText(rbx.getString("ConditionalUserNameHint"));
			contentPane.add(panel2);

			// add Logical Expression Section
			JPanel logicPanel = new JPanel();
			logicPanel.setLayout(new BoxLayout(logicPanel, BoxLayout.Y_AXIS));
            
            // add Antecedent Expression Panel -ONLY appears for MIXED operator statements
            _antecedent = _curConditional.getAntecedentExpression();
            _logicType = _curConditional.getLogicType();
            _antecedentField = new JTextField(65);
            _antecedentField.setFont(new Font("SansSerif", Font.BOLD, 14));
            _antecedentField.setText(_antecedent);
            _antecedentPanel = makeEditPanel(_antecedentField, "LabelAntecedent", "LabelAntecedentHint");

            JButton helpButton = new JButton(rbx.getString("HelpButton"));
			_antecedentPanel.add(helpButton);
			helpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					helpPressed(e);
				}
			});
            _antecedentPanel.add(helpButton);
            _antecedentPanel.setVisible(_logicType == Conditional.MIXED);
            logicPanel.add(_antecedentPanel);

            // add state variable table title
			JPanel varTitle = new JPanel();
			varTitle.setLayout(new FlowLayout());
			varTitle.add(new JLabel(rbx.getString("StateVariableTableTitle")));
			logicPanel.add(varTitle);
			// set up state variables table
			JPanel vt = new JPanel();
			// initialize and populate Combo boxes for table of state variables
			_notOperatorBox = new JComboBox();
			_notOperatorBox.addItem(" ");
			_notOperatorBox.addItem(rbx.getString("LogicNOT"));

			_andOperatorBox = new JComboBox();
			_andOperatorBox.addItem(rbx.getString("LogicAND"));
			_andOperatorBox.addItem(rbx.getString("LogicOR"));
			// initialize table of state variables
			_variableTableModel = new VariableTableModel();
			JTable variableTable = new JTable(_variableTableModel);
			variableTable.setRowHeight(_notOperatorBox.getPreferredSize().height);
			variableTable.setRowSelectionAllowed(false);
            int rowHeight = variableTable.getRowHeight();
			variableTable.setPreferredScrollableViewportSize(new java.awt.Dimension(700, 7*rowHeight));

			TableColumnModel variableColumnModel = variableTable.getColumnModel();

			TableColumn rowColumn = variableColumnModel.getColumn(VariableTableModel.ROWNUM_COLUMN);
			rowColumn.setResizable(false);
			rowColumn.setMaxWidth(new JTextField(3).getPreferredSize().width);
            
			TableColumn andColumn = variableColumnModel.getColumn(VariableTableModel.AND_COLUMN);
			andColumn.setResizable(false);
			andColumn.setCellEditor(new DefaultCellEditor(_andOperatorBox));
			andColumn.setMaxWidth(_andOperatorBox.getPreferredSize().width - 5);
            
			TableColumn notColumn = variableColumnModel.getColumn(VariableTableModel.NOT_COLUMN);
			notColumn.setCellEditor(new DefaultCellEditor(_notOperatorBox));
			notColumn.setMaxWidth(_notOperatorBox.getPreferredSize().width - 5);
			notColumn.setResizable(false);
            
			TableColumn descColumn = variableColumnModel.getColumn(VariableTableModel.DESCRIPTION_COLUMN);
			descColumn.setPreferredWidth(300);
			descColumn.setMinWidth(200);
			descColumn.setMaxWidth(600);
			descColumn.setResizable(true);

			TableColumn stateColumn = variableColumnModel.getColumn(VariableTableModel.STATE_COLUMN);
			stateColumn.setResizable(false);
			stateColumn.setMaxWidth(new JTextField(7).getPreferredSize().width);

			TableColumn triggerColumn = variableColumnModel.getColumn(VariableTableModel.TRIGGERS_COLUMN);
			triggerColumn.setResizable(false);
			triggerColumn.setMinWidth(30);
			triggerColumn.setMaxWidth(60);

			TableColumn editColumn = variableColumnModel.getColumn(VariableTableModel.EDIT_COLUMN);
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			variableTable.setDefaultRenderer(JButton.class, buttonRenderer);
			TableCellEditor buttonEditor = new ButtonEditor(new JButton());
			variableTable.setDefaultEditor(JButton.class, buttonEditor);
			JButton testButton = new JButton("XXXXX");
			variableTable.setRowHeight(testButton.getPreferredSize().height);
			editColumn.setMinWidth(testButton.getPreferredSize().width);
			editColumn.setResizable(false);

			TableColumn deleteColumn = variableColumnModel.getColumn(VariableTableModel.DELETE_COLUMN);
            // ButtonRenderer and TableCellEditor already set
			deleteColumn.setMinWidth(testButton.getPreferredSize().width);
			deleteColumn.setResizable(false);
			// add a scroll pane
			JScrollPane variableTableScrollPane = new JScrollPane(variableTable);
			vt.add(variableTableScrollPane, BorderLayout.CENTER);
			logicPanel.add(vt);
			vt.setVisible(true);

			// set up state variable buttons and logic
			JPanel panel42 = new JPanel();
			panel42.setLayout(new FlowLayout());
			        //  Add State Variable
            JButton addVariableButton = new JButton(rbx.getString("AddVariableButton"));
			panel42.add(addVariableButton);
			addVariableButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addVariablePressed(e);
				}
			});
			addVariableButton.setToolTipText(rbx.getString("AddVariableButtonHint"));

            JButton checkVariableButton = new JButton(rbx.getString("CheckVariableButton"));
			panel42.add(checkVariableButton);
			checkVariableButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkVariablePressed(e);
				}
			});
			checkVariableButton.setToolTipText(rbx.getString("CheckVariableButtonHint"));
			logicPanel.add(panel42);
                    
            // logic type area
			_operatorBox = new JComboBox(new String[] {
                    rbx.getString("LogicAND"),
                    rbx.getString("LogicOR"),
                    rbx.getString("LogicMixed") });
            JPanel typePanel = makeEditPanel(_operatorBox, "LabelLogicType", "TypeLogicHint");
			_operatorBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logicTypeChanged(e);
				}
			});
            _operatorBox.setSelectedIndex(_logicType-1);
            logicPanel.add(typePanel);
            logicPanel.add(Box.createHorizontalStrut(10));

			Border logicPanelBorder = BorderFactory.createEtchedBorder();
			Border logicPanelTitled = BorderFactory.createTitledBorder(
					logicPanelBorder, rbx.getString("TitleLogicalExpression"));
			logicPanel.setBorder(logicPanelTitled);
			contentPane.add(logicPanel);
            // End of Logic Expression Section

			// add Action Consequents Section
			JPanel conseqentPanel = new JPanel();
			conseqentPanel.setLayout(new BoxLayout(conseqentPanel, BoxLayout.Y_AXIS));
            
			JPanel actTitle = new JPanel();
			actTitle.setLayout(new FlowLayout());
			actTitle.add(new JLabel(rbx.getString("ActionTableTitle")));
			conseqentPanel.add(actTitle);

            // set up action consequents table
			_actionTableModel = new ActionTableModel();
			JTable actionTable = new JTable(_actionTableModel);
			actionTable.setRowSelectionAllowed(false);
			actionTable.setRowHeight(testButton.getPreferredSize().height);
			actionTable.setPreferredScrollableViewportSize(new java.awt.Dimension(700, 7*rowHeight));
			JPanel actionPanel = new JPanel();
			actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
			JPanel actionTitle = new JPanel();
			actionTitle.setLayout(new FlowLayout());
			conseqentPanel.add(actionPanel);

			TableColumnModel actionColumnModel = actionTable.getColumnModel();

			TableColumn descriptionColumn = actionColumnModel.getColumn(
                ActionTableModel.DESCRIPTION_COLUMN);
			descriptionColumn.setResizable(true);
            descriptionColumn.setPreferredWidth(600);
			descriptionColumn.setMinWidth(300);
			descriptionColumn.setMaxWidth(760);

			TableColumn actionEditColumn = actionColumnModel.getColumn(ActionTableModel.EDIT_COLUMN);
			// ButtonRenderer already exists
			actionTable.setDefaultRenderer(JButton.class, buttonRenderer);
			TableCellEditor editButEditor = new ButtonEditor(new JButton());
			actionTable.setDefaultEditor(JButton.class, editButEditor);
			actionEditColumn.setMinWidth(testButton.getPreferredSize().width);
			actionEditColumn.setResizable(false);

			TableColumn actionDeleteColumn = actionColumnModel.getColumn(ActionTableModel.DELETE_COLUMN);
            // ButtonRenderer and TableCellEditor already set
			actionDeleteColumn.setMinWidth(testButton.getPreferredSize().width);
			actionDeleteColumn.setResizable(false);
			// add a scroll pane
			JScrollPane actionTableScrollPane = new JScrollPane(actionTable);
			JPanel at = new JPanel();
			at.add(actionTableScrollPane, BorderLayout.CENTER);
			conseqentPanel.add(at);
			at.setVisible(true);

            // add action buttons to Action Section
			JPanel panel43 = new JPanel();
			panel43.setLayout(new FlowLayout());
            JButton addActionButton = new JButton(rbx.getString("addActionButton"));
			panel43.add(addActionButton);
			addActionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addActionPressed(e);
				}
			});

			addActionButton.setToolTipText(rbx.getString("addActionButtonHint"));
			conseqentPanel.add(panel43);
			//  - Reorder action button
            JButton reorderButton = new JButton(rbx.getString("ReorderButton"));
			panel43.add(reorderButton);
			reorderButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					reorderActionPressed(e);
				}
			});
			reorderButton.setToolTipText(rbx.getString("ReorderButtonHint"));
			conseqentPanel.add(panel43);

			Border conseqentPanelBorder = BorderFactory.createEtchedBorder();
			Border conseqentPanelTitled = BorderFactory.createTitledBorder(
					conseqentPanelBorder, rbx.getString("TitleAction"));
			conseqentPanel.setBorder(conseqentPanelTitled);
			contentPane.add(conseqentPanel);
            // End of Action Consequents Section

			// Bottom Buttons - Update Conditional
			JPanel panel5 = new JPanel();
			panel5.setLayout(new FlowLayout());
            JButton updateConditional = new JButton(rbx.getString("UpdateConditionalButton"));
			panel5.add(updateConditional);
			updateConditional.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateConditionalPressed(e);
				}
			});
			updateConditional.setToolTipText(rbx.getString("UpdateConditionalButtonHint"));
			// Cancel
            JButton cancelConditional = new JButton(rbx.getString("CancelButton"));
			panel5.add(cancelConditional);
			cancelConditional.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelConditionalPressed(e);
				}
			});
			cancelConditional.setToolTipText(rbx.getString("CancelConditionalButtonHint"));
			// Delete Conditional
            JButton deleteConditional = new JButton(rbx.getString("DeleteConditionalButton"));
			panel5.add(deleteConditional);
			deleteConditional.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deleteConditionalPressed(null);
				}
			});
			deleteConditional.setToolTipText(rbx.getString("DeleteConditionalButtonHint"));

			contentPane.add(panel5);
		}
		// setup window closing listener
		editConditionalFrame.addWindowListener(
            new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						if (inEditConditionalMode)
							cancelConditionalPressed(null);
					}
				});
		// initialize state variable table
		_variableTableModel.fireTableDataChanged();
		// initialize action variables
		_actionTableModel.fireTableDataChanged();
		editConditionalFrame.pack();
		editConditionalFrame.setVisible(true);
		inEditConditionalMode = true;
        checkVariablePressed(null);     // update variables to their current states
	}   /* makeEditConditionalWindow */

	/**
	 * Responds to the Add State Variable Button in the Edit Conditional window
	 */
	void addVariablePressed(ActionEvent e) {
		if (alreadyEditingActionOrVariable()) {
            return;
		}
		_showReminder = true;
        ConditionalVariable variable = new ConditionalVariable();
		_variableList.add(variable);
        _newItem = true;
        int size = _variableList.size();
        // default of operator for postion 0 (row 1) is Conditional.OPERATOR_NONE
        if (size > 1)
        {
            if (_logicType == Conditional.ALL_OR)
                variable.setOpern(Conditional.OPERATOR_OR);
            else
                variable.setOpern(Conditional.OPERATOR_AND);
        }
        size--;
		_variableTableModel.fireTableRowsInserted(size, size);
        makeEditVariableWindow(size);
        appendToAntecedent(variable);
	}

	/**
	 * Responds to the Check State Variable Button in the Edit Conditional window
	 */
    void checkVariablePressed(ActionEvent e) {
        for (int i=0; i<_variableList.size(); i++)
        {
            _variableList.get(i).evaluate();
        }
        _variableTableModel.fireTableDataChanged();
    }

	/**
	 * Responds to the Negation column in the Edit Conditional window
	 */
    void variableNegationChanged(int row, String oper) {
        ConditionalVariable variable = _variableList.get(row);
        boolean state = variable.isNegated();
        if (oper == null)
            variable.setNegation(false);
        else
            variable.setNegation(oper.equals(rbx.getString("LogicNOT")));
        if (variable.isNegated() != state )
            makeAntecedent();
    }

	/**
	 * Responds to the Operator column in the Edit Conditional window
	 */
    void variableOperatorChanged(int row, String oper) {
        ConditionalVariable variable = _variableList.get(row);
        int oldOper = variable.getOpern();
        if (row > 0)
        {
            if (oper.equals(rbx.getString("LogicOR")))
                variable.setOpern(Conditional.OPERATOR_OR);
            else
                variable.setOpern(Conditional.OPERATOR_AND);
        }
        else
            variable.setOpern(Conditional.OPERATOR_NONE);
        if (variable.getOpern() != oldOper )
            makeAntecedent();

    }

    /*
    * Responds to Add action button in the EditConditional window
    */
	void addActionPressed(ActionEvent e) {
		if (alreadyEditingActionOrVariable()) {
            return;
		}
		_showReminder = true;
        _actionList.add(new DefaultConditionalAction());
        _newItem = true;
		_actionTableModel.fireTableRowsInserted(_actionList.size(),
				_actionList.size());
        makeEditActionWindow(_actionList.size() - 1);
    }

	/**
	 * Responds to the Reorder Button in the Edit Conditional window
	 */
	void reorderActionPressed(ActionEvent e) {
		if (alreadyEditingActionOrVariable()) {
            return;
		}
		_showReminder = true;
		// Check if reorder is reasonable
		if (_actionList.size() <= 1) {
			javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
					.getString("Error46"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		_nextInOrder = 0;
		_inReorderMode = true;
		//status.setText(rbx.getString("ReorderMessage"));
		_actionTableModel.fireTableDataChanged();
	}

	/**
	 * Responds to the First/Next (Delete) Button in the Edit Conditional window
	 */
    void swapActions(int row) {
        ConditionalAction temp = _actionList.get(row);
        for (int i = row; i > _nextInOrder; i--)
        {
            _actionList.set(i, _actionList.get(i-1));
        }
        _actionList.set(_nextInOrder, temp);
        _nextInOrder++;
        if (_nextInOrder >= _actionList.size())
        {
            _inReorderMode = false;
        }
        //status.setText("");
        _actionTableModel.fireTableDataChanged();
    } 

    /**
    * Responds to the Update Conditional Button in the Edit Conditional window
    */
    void updateConditionalPressed(ActionEvent e) {
		if (alreadyEditingActionOrVariable()) {
            return;
		}
        Conditional c = _curConditional;
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            javax.swing.JOptionPane.showMessageDialog(
                    editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Warn8"),
                                new Object[] {SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName})+
                                java.text.MessageFormat.format(rbx.getString("Warn11"),
                                new Object[] {c.getUserName(), c.getSystemName() }), rbx .getString("WarnTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            cancelConditionalPressed(null);
            return;
        }
        // Check if the User Name has been changed
        String uName = conditionalUserName.getText().trim();
        if (!(uName.equals(c.getUserName()))) {
            // user name has changed - check if already in use
            if (!checkConditionalUserName(uName, _curLogix)) {
                return;
            }
            // user name is unique or blank, change it
            c.setUserName(uName);
            conditionalTableModel.fireTableDataChanged();
        }
        // clean up empty variable and actions
        for (int i=0; i<_variableList.size(); i++) {
            if (_variableList.get(i).getType() == Conditional.TYPE_NONE) {
                _variableList.remove(i);
                _variableTableModel.fireTableRowsDeleted(i, i);
            }
        }
        for (int i=0; i<_actionList.size(); i++) {
            if (_actionList.get(i).getType() == Conditional.ACTION_NONE) {
                _actionList.remove(i);
                _actionTableModel.fireTableRowsDeleted(i, i);
            }
        }
        if (_variableList.size() <= 0 && !_suppressReminder) {
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    java.text.MessageFormat.format(rbx.getString("Warn5"),
                            new Object[] { c.getUserName(), c.getSystemName() }), rbx
                            .getString("WarnTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        /*
        THIS RAISES MORE FALSE ALARMS THAN ACTUAL HAZARDS
        // Check for consistency in suppressing triggering of calculation
        // among state variables of this Logix.
        // Move this to a button to be used at the user's descretion.
        ArrayList <int[]> triggerPair = new ArrayList<int[]>(_variableList.size());
        for (int i=0; i<_variableList.size(); i++) {
            triggerPair.add(new int[2]);
        }
        _curLogix.getStateVariableList(_variableList, triggerPair);
        for (int i=0; i<_variableList.size(); i++)
        {
            ConditionalVariable variable = _variableList.get(i);
            int[] trigPair = triggerPair.get(i);  // 1st is triggerCalc, 2nd is triggerSupress
            if ( (trigPair[0] > 0) && (trigPair[1] > 0) ) {
                // have inconsistency, synthesize a warning message
                log.warn("Triggers calculation inconsistency - " + variable.getName());

                String msg7 = "";
                int type = variable.getType();
                if (type == Conditional.TYPE_SIGNAL_HEAD_RED ||
                    type == Conditional.TYPE_SIGNAL_HEAD_YELLOW ||
                    type == Conditional.TYPE_SIGNAL_HEAD_GREEN ||
                    type == Conditional.TYPE_SIGNAL_HEAD_DARK ||
                    type == Conditional.TYPE_SIGNAL_HEAD_FLASHRED ||
                    type == Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW ||
                    type == Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN )
                {
                    msg7 = java.text.MessageFormat.format(rbx.getString("Warn7"),
                         DefaultSignalHead.getAppearanceString(variable.getType()));
                }
                if (!_suppressReminder) {
                    String msg6 = java.text.MessageFormat.format(rbx.getString("Warn6"), 
                             new Object[] {"Appearance", variable.getName(), msg7 });
                    javax.swing.JOptionPane.showMessageDialog(editLogixFrame, msg6,
                             rbx.getString("WarnTitle"),javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            }
        }  */
        if (!validateAntecedent()) {
            return;
        } 
        // complete update
        _curConditional.setStateVariables(_variableList);
        _curConditional.setAction(_actionList);
        _curConditional.setLogicType(_logicType, _antecedent);
        cancelConditionalPressed(null);
    }

    /**
     * Responds to the Cancel button in the Edit Conditional frame
     * Does the cleanup from deleteConditionalPressed, updateConditionalPressed
     * and editConditionalFrame window closer.
     */
    void cancelConditionalPressed(ActionEvent e) {
        if (_editActionFrame != null) {
            cleanUpAction();
        }
        if (_editVariableFrame != null) {
            cleanUpVariable();
            }
        try {
            _curLogix.activateLogix();
        } catch (NumberFormatException nfe) {
            log.error("NumberFormatException on activation of Logix "+nfe);
            //nfe.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    rbx.getString("Error4")+nfe.toString()+rbx.getString("Error7"),
                    rbx.getString("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        inEditConditionalMode = false;
        editConditionalFrame.setVisible(false);
        editConditionalFrame.dispose();
        editConditionalFrame = null;
        editLogixFrame.setVisible(true);
    }

    /**
     * Responds to the Delete Conditional Button in the Edit Conditional window
     */

    void deleteConditionalPressed(String sName) {
        // delete this Conditional - this is done by the parent Logix
        if (sName == null)
        {
            sName = _curConditional.getSystemName();
        }
        if (sName == null) {
            log.error("Unable to delete Conditional, null system name");
            return;
        }
		_showReminder = true;
        _curConditional = null;
        numConditionals--;
        String[] msgs = _curLogix.deleteConditional(sName);
        if (msgs != null) {
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    java.text.MessageFormat.format(rbx.getString("Error11"), (Object[])msgs), 
                    rbx.getString("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        // complete deletion
        cancelConditionalPressed(null);
        conditionalTableModel.fireTableRowsDeleted(conditionalRowNumber,
                conditionalRowNumber);
        if (numConditionals < 1 && !_suppressReminder) {
            // warning message - last Conditional deleted
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
                    .getString("Warn1"), rbx.getString("WarnTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }


    @SuppressWarnings("fallthrough")
	boolean logicTypeChanged(ActionEvent e) {
        int type = _operatorBox.getSelectedIndex() + 1;
        if (type == _logicType) {
                return false;
        }
        makeAntecedent();
        int oper = Conditional.OPERATOR_OR;
        switch (type) {
            case Conditional.ALL_AND:
                oper = Conditional.OPERATOR_AND;
                // fall through
            case Conditional.ALL_OR:
                for (int i=1; i<_variableList.size(); i++)
                {
                    _variableList.get(i).setOpern(oper);
                }
                _antecedentPanel.setVisible(false);
                break;
            case Conditional.MIXED:
                _antecedentPanel.setVisible(true);
        }
        _logicType = type;
        _variableTableModel.fireTableDataChanged();
        editConditionalFrame.repaint();
        return true;
    }

    void helpPressed(ActionEvent e) {
        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                new String[] {
                    rbx.getString("LogicHelpText1"),
                    rbx.getString("LogicHelpText2"),
                    rbx.getString("LogicHelpText3"),
                    rbx.getString("LogicHelpText4"),
                    rbx.getString("LogicHelpText5"),
                    rbx.getString("LogicHelpText6"),
                    rbx.getString("LogicHelpText7")
                },
        rbx.getString("HelpButton"), javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    /**
    * build the antecedent statement
    */
    void makeAntecedent() {
        String not = rbx.getString("LogicNOT").toLowerCase();
        String row = rbx.getString("rowAbrev");
        String and = " " + rbx.getString("LogicAND").toLowerCase() + " ";
        String or = " " + rbx.getString("LogicOR").toLowerCase() + " ";
        String str = new String("");
        if (_variableList.get(0).isNegated())
        {
            str = not+ " ";
        }
        str = str + row + "1";
        for (int i=1; i<_variableList.size(); i++) {
            ConditionalVariable variable = _variableList.get(i);
            switch (variable.getOpern() ) {
                case Conditional.OPERATOR_AND:
                    str = str + and;
                    break;
                case Conditional.OPERATOR_OR:
                    str = str + or;
                    break;
            }
            if (variable.isNegated())
            {
                str = str + not;
            }
            str = str + row + (i+1);
            if (i>0 && i+1<_variableList.size()) {
                str = "(" + str  + ")";
            }
        }
        _antecedent = str;
        _antecedentField.setText(_antecedent);
		_showReminder = true;
    }

    void appendToAntecedent(ConditionalVariable variable) {
        if (_variableList.size() > 1) {
            if (_logicType == Conditional.OPERATOR_OR) {
                _antecedent = _antecedent + " " + rbx.getString("LogicOR").toLowerCase() + " ";
            } else {
                _antecedent = _antecedent + " " + rbx.getString("LogicAND").toLowerCase() + " ";
            }
        }
        _antecedent = _antecedent + rbx.getString("rowAbrev") + _variableList.size();
        _antecedentField.setText(_antecedent);
    }

    /**
    *  Check the antecedent and logic type
    */
    boolean validateAntecedent() {
        if (_logicType !=Conditional.MIXED) {
            return true;
        }
        _antecedent = _antecedentField.getText();
        if (_antecedent == null || _antecedent.trim().length() == 0)
        {
            makeAntecedent();
        }
        String message = _curConditional.validateAntecedent(_antecedent, _variableList);
        if (message != null)
        {
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
					message+rbx.getString("ParseError8"),  rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

/************************* Methods for Edit Variable Window ********************/

    boolean alreadyEditingActionOrVariable() {
        if (_editActionFrame != null) {
			// Already editing an Action, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(_editActionFrame,
                    rbx.getString("Error48"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
            _editActionFrame.setVisible(true);
			return true;
        }
        if (_editVariableFrame != null) {
			// Already editing a state variable, ask for completion of that edit
			javax.swing.JOptionPane.showMessageDialog(_editVariableFrame,
                    rbx.getString("Error47"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
            _editVariableFrame.setVisible(true);
			return true;
        }
        return false;
    }

	/**
	 * Creates and/or initializes the Edit a Variable window Note: you can get
	 * here via the New Variable button (addVariablePressed) or via an
	 * Edit button in the Variable table of the EditConditional window.
	 */
    void makeEditVariableWindow(int row) {
		if (alreadyEditingActionOrVariable()) {
            return;
		}
        _curVariableRowNumber = row;
        _curVariable = _variableList.get(row);
        _editVariableFrame = new JmriJFrame(rbx.getString("TitleEditVariable"));
        _editVariableFrame.setLocation(10, 100);
        JPanel topPanel = makeTopPanel(_editVariableFrame, "TitleAntecedentPhrase", 500, 160);

        Box panel1 = Box.createHorizontalBox();
        panel1.add(Box.createHorizontalGlue());

        _variableTypeBox = new JComboBox();
        for (int i = 1; i <= Conditional.NUM_STATE_VARIABLE_TYPES; i++) {
            _variableTypeBox.addItem(ConditionalVariable.getTypeString(i));
        }
        JPanel typePanel = makeEditPanel(_variableTypeBox, "LabelVariableType", "TypeVariableHint");
        panel1.add(typePanel);
        panel1.add(Box.createHorizontalStrut(10));

        _variableNameField = new JTextField(30);
        _variableNamePanel = makeEditPanel(_variableNameField, "LabelActionName", null);
        _variableNamePanel.setMaximumSize(
                    new Dimension(50, _variableNamePanel.getPreferredSize().height));
        _variableNamePanel.setVisible(false);
        panel1.add(_variableNamePanel);
        panel1.add(Box.createHorizontalStrut(10));

        _variableData1Field = new JTextField(30);
        _variableData1Panel = makeEditPanel(_variableData1Field, "LabelVariableData", null);
        _variableData1Panel.setMaximumSize(
                    new Dimension(45, _variableData1Panel.getPreferredSize().height));
        _variableData1Panel.setVisible(false);
        panel1.add(_variableData1Panel);
        panel1.add(Box.createHorizontalStrut(10));

        _variableData2Field = new JTextField(30);
        _variableData2Panel = makeEditPanel(_variableData2Field, "LabelVariableData", null);
        _variableData2Panel.setMaximumSize(
                    new Dimension(45, _variableData2Panel.getPreferredSize().height));
        _variableData2Panel.setVisible(false);
        panel1.add(_variableData2Panel);
        panel1.add(Box.createHorizontalStrut(10));
        topPanel.add(panel1);

        ActionListener updateListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateVariablePressed();
                }
            };
       ActionListener cancelListener = new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   cancelEditVariablePressed();
               }
           };
       ActionListener deleteListener = new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   deleteVariablePressed();
               }
           };
        JPanel panel = makeButtonPanel(updateListener, cancelListener, deleteListener);
        topPanel.add(panel);
        topPanel.add(Box.createVerticalGlue());

        Container contentPane = _editVariableFrame.getContentPane();
        contentPane.add(topPanel);
        // note - this listener cannot be added before other action items
        // have been created
        _variableTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                variableTypeChanged(true);
            }
        });
        // setup window closing listener
        _editVariableFrame.addWindowListener(
            new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        cancelEditVariablePressed();
                    }
                });
        initializeStateVariables();
        _editVariableFrame.pack();
        _editVariableFrame.setVisible(true);
    }

/***************************** Edit Action Window and methods ***********************/

	/**
	 * Creates and/or initializes the Edit Action window Note: you can get
	 * here via the New Action button (addActionPressed) or via an
	 * Edit button in the Action table of the EditConditional window.
	 */
	void makeEditActionWindow(int row) {
		if (alreadyEditingActionOrVariable()) {
            return;
		}
        _curActionRowNumber = row;
        _curAction = _actionList.get(row);
        _editActionFrame = new JmriJFrame(rbx.getString("TitleEditAction"));
        _editActionFrame.setLocation(10, 300);
        JPanel topPanel = makeTopPanel(_editActionFrame, "TitleConsequentPhrase", 600, 160);

        Box panel1 = Box.createHorizontalBox();
        panel1.add(Box.createHorizontalGlue());

        _actionTypeBox = new JComboBox();
        for (int i = 1; i <= Conditional.NUM_ACTION_TYPES; i++) {
            _actionTypeBox.addItem(DefaultConditionalAction.getTypeString(i));
        }
        JPanel typePanel = makeEditPanel(_actionTypeBox, "LabelActionType", "ActionTypeHint");
        panel1.add(typePanel);
        panel1.add(Box.createHorizontalStrut(10));

        _actionOptionBox = new JComboBox();
        for (int i = 1; i <= Conditional.NUM_ACTION_OPTIONS; i++) {
            _actionOptionBox.addItem(DefaultConditionalAction.getOptionString(i));
        }
        _optionPanel = makeEditPanel(_actionOptionBox, "LabelActionOption", "ActionOptionHint");
        _optionPanel.setVisible(false);
        panel1.add(_optionPanel);
        panel1.add(Box.createHorizontalStrut(10));

        _actionNameField = new JTextField(30);
        _namePanel = makeEditPanel(_actionNameField, "LabelActionName", null);
        _namePanel.setMaximumSize(
                    new Dimension(50, _namePanel.getPreferredSize().height));
        _namePanel.setVisible(false);
        panel1.add(_namePanel);
        panel1.add(Box.createHorizontalStrut(10));

        _actionTurnoutSetBox = new JComboBox(new String[] {
                rbx.getString("TurnoutClosed"),
                rbx.getString("TurnoutThrown"),
                rbx.getString("Toggle") });
        _turnoutPanel = makeEditPanel(_actionTurnoutSetBox, "LabelActionTurnout", "TurnoutSetHint");
        _turnoutPanel.setVisible(false);
        panel1.add(_turnoutPanel);

        _actionSensorSetBox = new JComboBox(new String[] {
                rbx.getString("SensorActive"),
                rbx.getString("SensorInactive"),
                rbx.getString("Toggle") });
        _sensorPanel = makeEditPanel(_actionSensorSetBox, "LabelActionSensor", "SensorSetHint");
        _sensorPanel.setVisible(false);
        panel1.add(_sensorPanel);

        _actionLightSetBox = new JComboBox(new String[] {
                rbx.getString("LightOn"),
                rbx.getString("LightOff"),
                rbx.getString("Toggle") });
        _lightPanel = makeEditPanel(_actionLightSetBox, "LabelActionLight", "LightSetHint");
        _lightPanel.setVisible(false);
        panel1.add(_lightPanel);

        _actionSignalSetBox = new JComboBox();
        for (int i = 0; i < 7; i++) {
            _actionSignalSetBox.addItem(
                DefaultSignalHead.getAppearanceString(signalAppearanceIndexToAppearance(i)));
        }
        _signalPanel = makeEditPanel(_actionSignalSetBox, "LabelActionSignal", "SignalSetHint");
        _signalPanel.setVisible(false);
        panel1.add(_signalPanel);

        _actionLockSetBox = new JComboBox(new String[] {
                rbx.getString("TurnoutUnlock"),
                rbx.getString("TurnoutLock"),
                rbx.getString("Toggle") });
        _lockPanel = makeEditPanel(_actionLockSetBox, "LabelActionLock", "LockSetHint");
        _lockPanel.setVisible(false);
        panel1.add(_lockPanel);

        panel1.add(Box.createHorizontalGlue());
        topPanel.add(panel1);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(Box.createVerticalGlue());

        Box panel2 = Box.createHorizontalBox();
        panel2.add(Box.createHorizontalGlue());

        _setPanel = new JPanel();
        _setPanel.setLayout(new BoxLayout(_setPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(new JLabel(rbx.getString("LabelActionFile")));
        _setPanel.add(p);
        _actionSetButton = new JButton(rbx.getString("FileButton"));
        _actionSetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    validateAction();
                    setFileLocation(e);
                }
            });
        _actionSetButton.setMaximumSize(_actionSetButton.getPreferredSize());
        _setPanel.add(_actionSetButton);
        _setPanel.add(Box.createVerticalGlue());
        _setPanel.setVisible(false);
        panel2.add(_setPanel);
        panel2.add(Box.createHorizontalStrut(5));

        _actionStringField = new JTextField(50);
        _textPanel = makeEditPanel(_actionStringField, "LabelActionText", null);
        _textPanel.setMaximumSize(
                    new Dimension(80, _textPanel.getPreferredSize().height));
        _textPanel.add(Box.createVerticalGlue());
        _textPanel.setVisible(false);
        panel2.add(_textPanel);
        panel2.add(Box.createHorizontalGlue());
        topPanel.add(panel2);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(Box.createVerticalGlue());

        ActionListener updateListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateActionPressed();
                }
            };
       ActionListener cancelListener = new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   cancelEditActionPressed();
               }
           };
       ActionListener deleteListener = new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   deleteActionPressed();
               }
           };
        JPanel panel = makeButtonPanel(updateListener, cancelListener, deleteListener);
        topPanel.add(panel);
        topPanel.add(Box.createVerticalGlue());

        Container contentPane = _editActionFrame.getContentPane();
        contentPane.add(topPanel);
        // note - this listener cannot be added before other action items
        // have been created
        _actionTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionTypeChanged(true);
            }
        });
        // setup window closing listener
        _editActionFrame.addWindowListener(
            new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        cancelEditActionPressed();
                    }
                });
        initializeActionVariables();
        _editActionFrame.pack();
        _editActionFrame.setVisible(true);
    } /* makeEditActionWindow */

    /******* Methods shared by Edit Variable and Edit Action Windows **********/

    /**
    * Utility for making Variable and Action editing Windows
    */
    JPanel makeTopPanel(JFrame frame, String title, int width, int height) {
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(new Dimension(0, height)));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        Border panelBorder = BorderFactory.createEtchedBorder();
        Border panelTitled = BorderFactory.createTitledBorder(panelBorder, rbx.getString(title));
        topPanel.setBorder(panelTitled);
        topPanel.add(Box.createRigidArea(new Dimension(width, 0)));
        topPanel.add(Box.createVerticalGlue());
        return topPanel;
    }

    /**
    * Utility for making Variable and Action editing Windows
    */
    JPanel makeEditPanel(JComponent comp, String label, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(new JLabel(rbx.getString(label)));
        panel.add(p);
        if (hint != null)
        {
            comp.setToolTipText(rbx.getString(hint));
        }
        comp.setMaximumSize(comp.getPreferredSize());  // override for  text fields
        panel.add(comp);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
    * Utility for making Variable and Action editing Windows
    */
    JPanel makeButtonPanel(ActionListener updateListener, 
                           ActionListener cancelListener,
                           ActionListener deleteListener) {
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
        JButton updateAction = new JButton(rbx.getString("UpdateButton"));
        panel3.add(updateAction);
        panel3.add(Box.createHorizontalStrut(10));
        updateAction.addActionListener(updateListener);
        updateAction.setToolTipText(rbx.getString("UpdateButtonHint"));

        JButton cancelAction = new JButton(rbx.getString("CancelButton"));
        panel3.add(cancelAction);
        panel3.add(Box.createHorizontalStrut(10));
        cancelAction.addActionListener(cancelListener);
        cancelAction.setToolTipText(rbx.getString("CancelButtonHint"));

        JButton deleteAction = new JButton(rbx.getString("DeleteButton"));
        panel3.add(deleteAction);
        deleteAction.addActionListener(deleteListener);
        deleteAction.setToolTipText(rbx.getString("DeleteButtonHint"));
        return panel3;
    }

    /************* Responses for Edit Action and Edit Variable Buttons ***********/
    /*
    * Responds to Update action button in the Edit Action window 
    */
    void updateActionPressed() {
        if (!validateAction() ) {
            _editActionFrame.toFront();
            return;
        }
        _actionTableModel.fireTableRowsUpdated(_curActionRowNumber, _curActionRowNumber);
        cleanUpAction();
    }

    /*
    * Responds to Update action button in the Edit Action window 
    */
    void updateVariablePressed() {
        if (!validateVariable() ) {
            _editVariableFrame.toFront();
            return;
        }
        _curVariable.evaluate();
        _variableTableModel.fireTableRowsUpdated(_curVariableRowNumber, _curVariableRowNumber);
        cleanUpVariable();
    }

    /*
    * Responds to Cancel action button and window closer of the 
    * Edit Action window.  Also does cleanup of Update and Delete
    * buttons.  
    */
    void cancelEditActionPressed() {
        if (_newItem) {
            deleteActionPressed(_curActionRowNumber);
        } else {
            cleanUpAction();
        }
    }

    void cleanUpAction() {
        _newItem = false;
        if (_editActionFrame != null) {
            _editActionFrame.setVisible(false);
            _editActionFrame.dispose();
            _editActionFrame = null;
            _curActionRowNumber = -1;
        }
		editConditionalFrame.setVisible(true);
    }

    /*
    * Responds to Cancel action button and window closer of the 
    * Edit Variable window.  Also does cleanup of Update and Delete
    * buttons.  
    */
    void cancelEditVariablePressed() {
        if (_newItem) {
            deleteVariablePressed(_curVariableRowNumber);
        } else {
            cleanUpVariable();
        }
    }

    void cleanUpVariable() {
        _newItem = false;
        if (_editVariableFrame != null) {
            _editVariableFrame.setVisible(false);
            _editVariableFrame.dispose();
            _editVariableFrame = null;
        }
        _curVariableRowNumber = -1;
        editConditionalFrame.setVisible(true);
    }

    /*
    * Responds to Delete action button in the Edit Action window 
    */
	void deleteActionPressed() {
        deleteActionPressed(_curActionRowNumber);
    }

    /*
    * Responds to Delete action button in an action row of the
    * Edit Conditional window 
    */
	void deleteActionPressed(int row) {
		if (row != _curActionRowNumber && alreadyEditingActionOrVariable()) {
            return;
		}
        _actionList.remove(row);
		_actionTableModel.fireTableRowsDeleted(row, row);
        cleanUpAction();
		_showReminder = true;
    }

    /*
    * Responds to Delete action button in the Edit Variable window 
    */
	void deleteVariablePressed() {
        deleteVariablePressed(_curVariableRowNumber);
    }

	/**
	 * Responds to the Delete Button in the State Variable Table of the Edit
	 * Conditional window
	 */
	void deleteVariablePressed(int row) {
		if (row != _curVariableRowNumber && alreadyEditingActionOrVariable()) {
            return;
		}
		if (_variableList.size() < 1 && !_suppressReminder) {
            // warning message - last State Variable deleted
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    rbx.getString("Warn3"), rbx.getString("WarnTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
		}
		// move remaining state variables if needed
		_variableList.remove(row);
        _variableTableModel.fireTableRowsDeleted(row, row);
        makeAntecedent();
        cleanUpVariable();
		_showReminder = true;
    }

    /**
    * set display to show current state variable (curVariable) parameters
    */
    @SuppressWarnings("fallthrough")
	void initializeStateVariables() {
        int type = _curVariable.getType();
        switch (type)  {
            case Conditional.TYPE_MEMORY_EQUALS:
                _variableData2Field.setText(_curVariable.getDataString());
                // fall through
            case Conditional.TYPE_SENSOR_ACTIVE:
            case Conditional.TYPE_SENSOR_INACTIVE:
            case Conditional.TYPE_TURNOUT_THROWN:
            case Conditional.TYPE_TURNOUT_CLOSED:
            case Conditional.TYPE_LIGHT_ON:
            case Conditional.TYPE_LIGHT_OFF:
            case Conditional.TYPE_SIGNAL_HEAD_RED:
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                _variableNameField.setText(_curVariable.getName());
                break;
            case Conditional.TYPE_CONDITIONAL_TRUE:
            case Conditional.TYPE_CONDITIONAL_FALSE:
                _variableNameField.setText(getConditionalUserName(_curVariable.getName()));
                break;
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                int time = _curVariable.getNum1();
                _variableData1Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                time = _curVariable.getNum2();
                _variableData2Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                _variableNameField.setText("");
                break;
        }
		variableTypeChanged(false);
        // set type after call to variableTypeChanged
		_variableTypeBox.setSelectedIndex(type - 1);
        _editVariableFrame.transferFocusBackward();
    }

    String getConditionalUserName(String name) {
        Conditional c = _conditionalManager.getBySystemName(name.toUpperCase());
        if (c != null) {
            return c.getUserName();
        }
        return name;
    }

    /**
    * set display to show current action (curAction) parameters
    */
	@SuppressWarnings("fallthrough")
	void initializeActionVariables() {
        _actionOptionBox.setSelectedIndex(_curAction.getOption() - 1);
        _actionNameField.setText(_curAction.getDeviceName());
        int type = _curAction.getType();
        switch (type)  {
            case Conditional.ACTION_DELAYED_TURNOUT:
            case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                _actionStringField.setText(_curAction.getActionString());
                // fall through
            case Conditional.ACTION_SET_TURNOUT:
                if (_curAction.getActionData() == Turnout.CLOSED) {
                    _actionTurnoutSetBox.setSelectedIndex(0);
                } else if (_curAction.getActionData() == Turnout.THROWN) {
                    _actionTurnoutSetBox.setSelectedIndex(1);
                } else {
                    _actionTurnoutSetBox.setSelectedIndex(2);
                }
                break;
            case Conditional.ACTION_DELAYED_SENSOR:
            case Conditional.ACTION_RESET_DELAYED_SENSOR:
                _actionStringField.setText(_curAction.getActionString());
                // fall through
            case Conditional.ACTION_SET_SENSOR:
                if (_curAction.getActionData() == Sensor.ACTIVE) {
                    _actionSensorSetBox.setSelectedIndex(0);
                } else if (_curAction.getActionData() == Sensor.INACTIVE) {
                    _actionSensorSetBox.setSelectedIndex(1);
                } else {
                    _actionSensorSetBox.setSelectedIndex(2);
                }
                break;
            case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                _actionSignalSetBox.setSelectedIndex(
                    signalAppearanceToAppearanceIndex(_curAction.getActionData()));
                break;
            case Conditional.ACTION_SET_LIGHT:
                if (_curAction.getActionData() == Light.ON) {
                    _actionLightSetBox.setSelectedIndex(0);
                } else if (_curAction.getActionData() == Light.OFF) {
                    _actionLightSetBox.setSelectedIndex(1);
                } else {
                    _actionLightSetBox.setSelectedIndex(2);
                }
                break;
            case Conditional.ACTION_LOCK_TURNOUT:
                if (_curAction.getActionData() == Turnout.UNLOCKED) {
                    _actionLockSetBox.setSelectedIndex(0);
                } else if (_curAction.getActionData() == Turnout.LOCKED) {
                    _actionLockSetBox.setSelectedIndex(1);
                } else {
                    _actionLockSetBox.setSelectedIndex(2);
                }
                break;
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                _actionStringField.setText(_curAction.getActionString());
                break;		
            case Conditional.ACTION_PLAY_SOUND:
            case Conditional.ACTION_RUN_SCRIPT:
                _actionNameField.setText("");
                // fall through
            case Conditional.ACTION_SET_MEMORY:
            case Conditional.ACTION_COPY_MEMORY:
                _actionStringField.setText(_curAction.getActionString());
                break;
            case Conditional.ACTION_SET_FAST_CLOCK_TIME:
                int time = _curAction.getActionData();
                _actionStringField.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                _actionNameField.setText("");
                break;
        }
		actionTypeChanged(false);
        // set type after call to actionTypeChanged
		_actionTypeBox.setSelectedIndex(type - 1);
        _editActionFrame.transferFocusBackward();
    }   /* initializeActionVariables */


	JFileChooser sndFileChooser = null;
	JFileChooser scriptFileChooser = null;
	JFileChooser defaultFileChooser = null;

	/**
	 * Responds to the Set button in the Edit Action window action section.
	 */
	void setFileLocation(ActionEvent e) {
        ConditionalAction action = _actionList.get(_curActionRowNumber);
        JFileChooser currentChooser;
        int actionType = action.getType();
        if (actionType == Conditional.ACTION_PLAY_SOUND) {
            if (sndFileChooser == null) {
                sndFileChooser = new JFileChooser(System.getProperty("user.dir")+
                                                  java.io.File.separator+"resources"+
                                                  java.io.File.separator+"sounds");
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("wav sound files");
                filt.addExtension("wav");
                sndFileChooser.setFileFilter(filt);
            }
            currentChooser = sndFileChooser;
        } else if (actionType == Conditional.ACTION_RUN_SCRIPT) {
            if (scriptFileChooser == null) {
                scriptFileChooser = new JFileChooser(System.getProperty("user.dir")+
                                                     java.io.File.separator+"jython");
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");
                filt.addExtension("py");
                scriptFileChooser.setFileFilter(filt);
            }
            currentChooser = scriptFileChooser;
        } else {
            log.warn("Unexpected actionType["+actionType+"] = "+DefaultConditionalAction.getTypeString(actionType));
            if (defaultFileChooser == null) {
                defaultFileChooser = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());
                defaultFileChooser.setFileFilter(new jmri.util.NoArchiveFileFilter());
            }
            currentChooser = defaultFileChooser;
        }
        
        currentChooser.rescanCurrentDirectory();
        int retVal = currentChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
        // set selected file location in data string
            try {
                _actionStringField.setText(currentChooser.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException ex) {
                log.error("exception setting file location: " + ex);
                _actionStringField.setText("");
            }
        }
	}

	/**
	 * Responds to a change in an Action Type Box of Edit Action Window
	 * Set components visible for the selected type
	 */
	@SuppressWarnings("fallthrough")
	void actionTypeChanged(boolean fromTypeBox) {
        int type = _actionTypeBox.getSelectedIndex() + 1;
        if (fromTypeBox) {
            if (type == _curAction.getType()) {
                // no change - simply return
                return;
            }
        }
        else {
            type = _curAction.getType();

        }
        _setPanel.setVisible(false);
        _turnoutPanel.setVisible(false);
        _sensorPanel.setVisible(false);
        _signalPanel.setVisible(false);
        _lightPanel.setVisible(false);
        _lockPanel.setVisible(false);
        _textPanel.setVisible(false);
        _namePanel.setVisible(false);
        if (type == Conditional.ACTION_NONE) {
            _optionPanel.setVisible(false);
        }
        else {
            _optionPanel.setVisible(true);
        }
        switch (type)  {
            case Conditional.ACTION_DELAYED_TURNOUT:
            case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                _actionStringField.setToolTipText(rbx.getString("DataHintDelayedTurnout"));
                _textPanel.setVisible(true);
                // fall through
            case Conditional.ACTION_SET_TURNOUT:
                _turnoutPanel.setVisible(true);
                // fall through
            case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
                _actionNameField.setToolTipText(rbx.getString("NameHintTurnout"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_DELAYED_SENSOR:
            case Conditional.ACTION_RESET_DELAYED_SENSOR:
                _actionStringField.setToolTipText(rbx.getString("DataHintDelayedSensor"));
                _textPanel.setVisible(true);
                // fall through
            case Conditional.ACTION_SET_SENSOR:
                _sensorPanel.setVisible(true);
                // fall through
            case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
                _actionNameField.setToolTipText(rbx.getString("NameHintSensor"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                _signalPanel.setVisible(true);
                // fall through
			case Conditional.ACTION_SET_SIGNAL_HELD:
			case Conditional.ACTION_CLEAR_SIGNAL_HELD:
			case Conditional.ACTION_SET_SIGNAL_DARK:
			case Conditional.ACTION_SET_SIGNAL_LIT:
			case Conditional.ACTION_TRIGGER_ROUTE:
			case Conditional.ACTION_ENABLE_LOGIX:
			case Conditional.ACTION_DISABLE_LOGIX:
                _actionNameField.setToolTipText(rbx.getString("NameHintSignal"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
                _actionStringField.setToolTipText(rbx.getString("DataHintLightIntensity"));
                _textPanel.setVisible(true);
                _actionNameField.setToolTipText(rbx.getString("NameHintLight"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                _actionStringField.setToolTipText(rbx.getString("DataHintLightTransitionTime"));
                _textPanel.setVisible(true);
                _actionNameField.setToolTipText(rbx.getString("NameHintLight"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_SET_LIGHT:
                _lightPanel.setVisible(true);
                _actionNameField.setToolTipText(rbx.getString("NameHintLight"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_LOCK_TURNOUT:
                _lockPanel.setVisible(true);
                _actionNameField.setToolTipText(rbx.getString("NameHintTurnout"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_SET_MEMORY:
                _actionStringField.setToolTipText(rbx.getString("DataHintMemory"));
                _textPanel.setVisible(true);
                _actionNameField.setToolTipText(rbx.getString("NameHintMemory"));
                _namePanel.setVisible(true);
                break;
            case Conditional.ACTION_COPY_MEMORY:
                _actionStringField.setToolTipText(rbx.getString("DataHintToMemory"));
                _textPanel.setVisible(true);
                _actionNameField.setToolTipText(rbx.getString("NameHintMemory"));
                _namePanel.setVisible(true);
                break;
			case Conditional.ACTION_SET_FAST_CLOCK_TIME:
                _actionStringField.setToolTipText(rbx.getString("DataHintTime"));
                _textPanel.setVisible(true);
                break;
            case Conditional.ACTION_PLAY_SOUND:
                _actionStringField.setToolTipText(rbx.getString("SetHintSound"));
                _textPanel.setVisible(true);
                _setPanel.setVisible(true);
                break;
            case Conditional.ACTION_RUN_SCRIPT:
                _actionStringField.setToolTipText(rbx.getString("SetHintScript"));
                _textPanel.setVisible(true);
                _setPanel.setVisible(true);
                break;
			case Conditional.ACTION_START_FAST_CLOCK:
			case Conditional.ACTION_STOP_FAST_CLOCK:
				break;
        }
	} /* actionTypeChanged */

	/**
	 * Responds to change in variable type in State Variable Table in the Edit
	 * Conditional window Also used to set up for Edit of a Conditional with
	 * state variables.
	 */
	void variableTypeChanged(boolean fromTypeBox) {
        int type = _variableTypeBox.getSelectedIndex() + 1;
        if (fromTypeBox) {
            if (type == _curVariable.getType()) {
                // no change - simply return
                return;
            }
        }
        else {
            type = _curVariable.getType();

        }
        _variableNamePanel.setVisible(false);
        _variableData1Panel.setVisible(false);
        _variableData2Panel.setVisible(false);
		switch (type) {
            case Conditional.TYPE_SENSOR_ACTIVE:
            case Conditional.TYPE_SENSOR_INACTIVE:
                _variableNameField.setToolTipText(rbx.getString("NameHintSensor"));
                _variableNamePanel.setVisible(true);
                break;
            case Conditional.TYPE_TURNOUT_THROWN:
            case Conditional.TYPE_TURNOUT_CLOSED:
                _variableNameField.setToolTipText(rbx.getString("NameHintTurnout"));
                _variableNamePanel.setVisible(true);
                break;
            case Conditional.TYPE_CONDITIONAL_TRUE:
            case Conditional.TYPE_CONDITIONAL_FALSE:
                _variableNameField.setToolTipText(rbx.getString("NameHintConditional"));
                _variableNamePanel.setVisible(true);
                break;
            case Conditional.TYPE_LIGHT_ON:
            case Conditional.TYPE_LIGHT_OFF:
                _variableNameField.setToolTipText(rbx.getString("NameHintLight"));
                _variableNamePanel.setVisible(true);
                break;
            case Conditional.TYPE_MEMORY_EQUALS:
                _variableData1Field.setToolTipText(rbx.getString("DataHintMemory"));
                _variableData1Panel.setVisible(true);
                _variableNameField.setToolTipText(rbx.getString("NameHintMemory"));
                _variableNamePanel.setVisible(true);
                break;
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                _variableData1Field.setToolTipText(rbx.getString("DataHintTime"));
                _variableData1Panel.setVisible(true);
                _variableData2Field.setToolTipText(rbx.getString("DataHintTime"));
                _variableData2Panel.setVisible(true);
                break;
            case Conditional.TYPE_SIGNAL_HEAD_RED:
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                _variableNameField.setToolTipText(rbx.getString("NameHintSignal"));
                _variableNamePanel.setVisible(true);
                break;
        }
    } /* variableTypeChanged */

	/**
	 * Validates Variable data from Edit Variable Window, and transfers it to
	 * current action object as appropriate
	 * <P>
	 * Returns true if all data checks out OK, otherwise false.
	 * <P>
	 * Messages are sent to the user for any errors found. This routine returns
	 * false immediately after finding an error, even if there might be more
	 * errors.
	 */
	@SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    boolean validateVariable() {
        int type = _variableTypeBox.getSelectedIndex() + 1;
        if (type != _curVariable.getType()) {
            _curVariable.setType(type);
        }
        String name = _variableNameField.getText().trim();
		_variableNameField.setText(name);
        _curVariable.setDataString("");
        _curVariable.setNum1(0);
        _curVariable.setNum2(0);
		// validate according to action type
		Sensor sn = null;
		Turnout t = null;
		SignalHead h = null;
		Conditional c = null;
		Light lgt = null;
		Memory m = null;
		boolean result = false;
		switch ( type ) {
            case Conditional.TYPE_SENSOR_ACTIVE:
                name = validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                sn = getSensor(name);
                if (sn.getState() == Sensor.ACTIVE)
                    result = true;
                break;
            case Conditional.TYPE_SENSOR_INACTIVE:
                name = validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                sn = getSensor(name);
                if (sn.getState() == Sensor.INACTIVE)
                    result = true;
                break;
            case Conditional.TYPE_TURNOUT_THROWN:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                t = getTurnout(name);
                if (t.getState() == Turnout.THROWN)
                    result = true;
                break;
            case Conditional.TYPE_TURNOUT_CLOSED:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                t = getTurnout(name);
                if (t.getState() == Turnout.CLOSED)
                    result = true;
                break;
            case Conditional.TYPE_CONDITIONAL_TRUE:
                name = validateConditionalReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                c = getConditional(name);
                if (c.getState() == Conditional.TRUE)
                    result = true;
                break;
            case Conditional.TYPE_CONDITIONAL_FALSE:
                name = validateConditionalReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                c = getConditional(name);
                if (c.getState() == Conditional.FALSE)
                    result = true;
                break;
            case Conditional.TYPE_LIGHT_ON:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                lgt = getLight(name);
                if (lgt.getState() == Light.ON)
                    result = true;
                break;
            case Conditional.TYPE_LIGHT_OFF:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                lgt = getLight(name);
                if (lgt.getState() == Light.OFF)
                    result = true;
                break;
            case Conditional.TYPE_MEMORY_EQUALS:
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                String str = _variableData1Field.getText();
                _curVariable.setDataString(str);
                m = getMemory(name);
                String s = (String) m.getValue();
                if (s == null) {
                    return (true); // result is false, because null doesn't match content
                }
                if ( s.equals(str) ) {
                    result = true;
                }
                break;
            case Conditional.TYPE_FAST_CLOCK_RANGE:
                int beginTime = parseTime(_variableData1Field.getText());
                if (beginTime < 0) {
                    // parse error occurred - message has been sent
                    return (false);
                }
                int endTime = parseTime(_variableData2Field.getText());
                if (endTime < 0) {
                    return (false);
                }
                // set beginning and end time (minutes since midnight)
                _curVariable.setNum1(beginTime);
                _curVariable.setNum2(endTime);
                // get current fast clock time
                Timebase fastClock = InstanceManager.timebaseInstance();
                Date currentTime = fastClock.getTime();
                int currentMinutes = (currentTime.getHours() * 60)
                        + currentTime.getMinutes();
                // check if current time is within range specified
                if (endTime > beginTime) {
                    // range is entirely within one day
                    if ((currentMinutes < endTime) && (currentMinutes >= beginTime))
                        result = true;
                } else {
                    // range includes midnight
                    if (currentMinutes >= beginTime)
                        result = true;
                    else if (currentMinutes < endTime)
                        result = true;
                }
                break;
            case Conditional.TYPE_SIGNAL_HEAD_RED:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.RED)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_YELLOW:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.YELLOW)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_GREEN:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.GREEN)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_DARK:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.DARK)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_FLASHRED:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.FLASHRED)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.FLASHYELLOW)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getAppearance() == SignalHead.FLASHGREEN)
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_LIT:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getLit())
                    result = true;
                break;
            case Conditional.TYPE_SIGNAL_HEAD_HELD:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                h = getSignalHead(name);
                if (h.getHeld())
                    result = true;
                break;
		}
		// set state variable
		if (_curVariable.isNegated()) {
			result = !result;
        }
        _curVariable.setState(result);
		return (true);
	}   /* validateVariable */

	/**
	 * Validates Action data from Edit Action Window, and transfers it to
	 * current action object as appropriate
	 * <P>
	 * Returns true if all data checks out OK, otherwise false.
	 * <P>
	 * Messages are sent to the user for any errors found. This routine returns
	 * false immediately after finding an error, even if there might be more
	 * errors.
	 */
	@SuppressWarnings("fallthrough")
	boolean validateAction() {
        int type = _actionTypeBox.getSelectedIndex() + 1;
        if (type != _curAction.getType()) {
            _curAction.setType(type);
        }
        if (type != Conditional.ACTION_NONE) {
            _curAction.setOption(_actionOptionBox.getSelectedIndex() + 1);
        }
        else {
            _curAction.setOption(0);
        }
        String name = _actionNameField.getText().trim();
		_actionNameField.setText(name);
        String actionString = _actionStringField.getText().trim();
        _curAction.setActionString("");
        _curAction.setActionData(-1);
		// validate according to action type
		switch (type) {
            case Conditional.ACTION_NONE:
                _curAction.setDeviceName("");
                break;
            case Conditional.ACTION_DELAYED_TURNOUT:
            case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                if (!validateIntegerReference(Conditional.ACTION_RESET_DELAYED_TURNOUT, actionString)) 
                {
                    return (false);
                }
                _curAction.setActionString(actionString);
                // fall through
            case Conditional.ACTION_SET_TURNOUT:
                if (_actionTurnoutSetBox.getSelectedIndex() == 0)
                    _curAction.setActionData(Turnout.CLOSED);
                else if (_actionTurnoutSetBox.getSelectedIndex() == 1)
                    _curAction.setActionData(Turnout.THROWN);
                else
                    _curAction.setActionData(Route.TOGGLE);
                // fall through
            case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ACTION_LOCK_TURNOUT:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (_actionLockSetBox.getSelectedIndex() == 0)
                    _curAction.setActionData(Turnout.UNLOCKED);
                else if (_actionLockSetBox.getSelectedIndex() == 1)
                    _curAction.setActionData(Turnout.LOCKED);
                else
                    _curAction.setActionData(Route.TOGGLE);
                break;
            case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                _curAction.setActionData(signalAppearanceIndexToAppearance(
                        _actionSignalSetBox.getSelectedIndex()));
                // fall through;
            case Conditional.ACTION_SET_SIGNAL_HELD:
            case Conditional.ACTION_CLEAR_SIGNAL_HELD:
            case Conditional.ACTION_SET_SIGNAL_DARK:
            case Conditional.ACTION_SET_SIGNAL_LIT:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ACTION_TRIGGER_ROUTE:  
                name = validateRouteReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ACTION_DELAYED_SENSOR:
            case Conditional.ACTION_RESET_DELAYED_SENSOR:
                if (!validateIntegerReference(Conditional.ACTION_RESET_DELAYED_TURNOUT, actionString)) 
                {
                    return (false);
                }
                _curAction.setActionString(actionString);
                // fall through
            case Conditional.ACTION_SET_SENSOR:
                if (_actionSensorSetBox.getSelectedIndex() == 0)
                    _curAction.setActionData(Sensor.ACTIVE);
                else if (_actionSensorSetBox.getSelectedIndex() == 1)
                    _curAction.setActionData(Sensor.INACTIVE);
                else
                    _curAction.setActionData(Route.TOGGLE);
                // fall through
            case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
                name = validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ACTION_SET_LIGHT:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (_actionLightSetBox.getSelectedIndex() == 0)
                    _curAction.setActionData(Light.ON);
                else if (_actionLightSetBox.getSelectedIndex() == 1)
                    _curAction.setActionData(Light.OFF);
                else
                    _curAction.setActionData(Route.TOGGLE);
                break;
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                Light lgtx = getLight(name);
                // check if light user name was entered
                if (lgtx == null) {
                    return false;
                }
                if (!lgtx.isIntensityVariable()) {
                    javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                            java.text.MessageFormat.format(
                            rbx.getString("Error45"), new Object[] { name }), 
                            rbx.getString("ErrorTitle"),  javax.swing.JOptionPane.ERROR_MESSAGE);
                    return (false);				
                }
                if (!validateIntegerReference(type, actionString)) 
                {
                    return (false);
                }
                _curAction.setActionString(actionString);
                break;
            case Conditional.ACTION_SET_MEMORY:
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                _curAction.setActionString(actionString);
                break;
            case Conditional.ACTION_COPY_MEMORY:
                // check "from" Memory
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                actionString = validateMemoryReference(actionString);
                if (actionString == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _actionStringField.setText(actionString);
                _curAction.setDeviceName(name);
                _curAction.setActionString(actionString);
                break;
            case Conditional.ACTION_ENABLE_LOGIX:
            case Conditional.ACTION_DISABLE_LOGIX:
                name = validateLogixReference(name);
                if (name == null) {
                    return false;
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ACTION_PLAY_SOUND:
            case Conditional.ACTION_RUN_SCRIPT:
                _curAction.setActionString(actionString);
                break;
            case Conditional.ACTION_SET_FAST_CLOCK_TIME:
                int time = parseTime(actionString);
                if ( time<0 ) {
                    return (false);
                }
                _curAction.setActionData(time);
                break;
            case Conditional.ACTION_START_FAST_CLOCK:
            case Conditional.ACTION_STOP_FAST_CLOCK:
                break;
		}
		return (true);
	}

	// *********** Utility Methods ********************

    /**
    * Checks if String is an integer or references an integer
    */
    boolean validateIntegerReference(int actionType, String intReference) {
        intReference = intReference.trim();
        if (intReference == null || intReference.length() == 0)
        {
            displayBadIntegerFormat(actionType, intReference);
            return false;
        }
        try {
            return validateInteger(actionType, Integer.valueOf(intReference).intValue());
        } catch (NumberFormatException e) {
            intReference = validateMemoryReference(intReference);
            if (intReference != null)
            {
                Memory m = getMemory(intReference);
                try {
                    return validateInteger(actionType, Integer.valueOf((String)m.getValue()).intValue());
                } catch (NumberFormatException ex) {
                    displayBadIntegerFormat(actionType, intReference);
                }
            }
        }
        return false;
    }

    /**
    * Checks text represents an integer suitable for timing
    * throws NumberFormatException
    */
    boolean validateInteger(int actionType, int time) {
        int maxTime = 3600;
        if (actionType == Conditional.ACTION_SET_LIGHT_INTENSITY)
        {
            maxTime = 100;

        }
        if (time <= 0 || time > maxTime) {
            String errorNum = " ";
            switch(actionType) {
                case Conditional.ACTION_DELAYED_TURNOUT:
                    errorNum = "Error38";
                    break;
                case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                    errorNum = "Error40";
                    break;
                case Conditional.ACTION_DELAYED_SENSOR:
                    errorNum = "Error25";
                    break;
                case Conditional.ACTION_RESET_DELAYED_SENSOR:
                    errorNum = "Error28";
                    break;
                case Conditional.ACTION_SET_LIGHT_INTENSITY:
                    errorNum = "Error42";
                    break;
                case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                    errorNum = "Error44";
                    break;
            }
            javax.swing.JOptionPane.showMessageDialog(
                    editConditionalFrame, java.text.MessageFormat.format(rbx.getString(errorNum),
                    time), rbx.getString("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    void displayBadIntegerFormat(int actionType, String intReference)
    {
        String errorNum = " ";
        switch(actionType) {
            case Conditional.ACTION_DELAYED_TURNOUT:
                errorNum = "Error39";
                break;
            case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                errorNum = "Error41";
                break;
            case Conditional.ACTION_DELAYED_SENSOR:
                errorNum = "Error23";
                break;
            case Conditional.ACTION_RESET_DELAYED_SENSOR:
                errorNum = "Error27";
                break;
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
                errorNum = "Error43";
                break;
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                errorNum = "Error45";
                break;
        }
        javax.swing.JOptionPane.showMessageDialog(
                editConditionalFrame, java.text.MessageFormat.format(rbx.getString(errorNum),
                intReference), rbx.getString("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
    * Checks Memory reference of text.  Upshifts text of field if it is a system name
    */
    String validateMemoryReference(String name) {
        Memory m = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            m = InstanceManager.memoryManagerInstance().getByUserName(name);
            if (m != null) {
                //return m.getSystemName();
                return name;
            }
            // check memory system name
            name = name.toUpperCase().trim();
            m = InstanceManager.memoryManagerInstance().getBySystemName(name);
        }
        if (m == null) {
            messageInvalidMemoryName(name, false);
            return null;
        }
        return name;
    }

    /**
    * Checks Turnout reference of text.  Upshifts text of field if it is a system name
    */
    String validateTurnoutReference(String name) {
        Turnout t = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            t = InstanceManager.turnoutManagerInstance().getByUserName(name);
            if (t != null) {
                //return t.getSystemName();
                return name;
            }
            name = name.toUpperCase().trim();
            t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
        }
        if (t == null) {
            messageInvalidTurnoutName(name, false);
            return null;
        }
        return name;
    }

    /**
    * Checks SignalHead reference of text.  Upshifts text of field if it is a system name
    */
    String validateSignalHeadReference(String name) {
        SignalHead h = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            h = InstanceManager.signalHeadManagerInstance().getByUserName(name);
            if (h != null) {
                //return h.getSystemName();
                return name;
            }
            name = name.toUpperCase().trim();
            h = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
        }
        if (h == null) {
            messageInvalidSignalHeadName(name, false);
            return null;
        }
        return name;
    }

    /**
    * Checks Sensor reference of text.  Upshifts text of field if it is a system name
    */
    String validateSensorReference(String name) {
        Sensor s = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            s = InstanceManager.sensorManagerInstance().getByUserName(name);
            if (s != null) {
                //return s.getSystemName();
                return name;
            }
            name = name.toUpperCase().trim();
            s = InstanceManager.sensorManagerInstance().getBySystemName(name);
        }
        if (s == null) {
            messageInvalidSensorName(name, false);
            return null;
        }
        return name;
    }

    /**
    * Checks Light reference of text.  Upshifts text of field if it is a system name
    */
    String validateLightReference(String name) {
        Light l = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            l = InstanceManager.lightManagerInstance().getByUserName(name);
            if (l != null) {
                //return l.getSystemName();
                return name;
            }
            name = name.toUpperCase().trim();
            l = InstanceManager.lightManagerInstance().getBySystemName(name);
        }
        if (l == null) {
            messageInvalidLightName(name, false);
            return null;
        }
        return name;
    }

    /**
    * Checks Conditional reference of text.  Upshifts text of field if it is a system name
    * Forces name to System name
    */
    String validateConditionalReference(String name) {
        Conditional c = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            c = _conditionalManager.getByUserName(_curLogix, name);
            if (c != null) {
                return c.getSystemName();
            }
            c = _conditionalManager.getByUserName(name);
            if (c != null) {
                return c.getSystemName();
            }
            name = name.toUpperCase().trim();
            c = _conditionalManager.getBySystemName(name);
        }
        if (c == null) {
            messageInvalidConditionalName(name);
            return null;
        }
        return name;
    }

    /**
    * Checks Logix reference of text.  Upshifts text of field if it is a system name
    */
    String validateLogixReference(String name) {
        Logix l = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            l = _logixManager.getByUserName(name);
            if (l != null) {
                return name;
            }
            // check memory system name
            name = name.toUpperCase().trim();
            l = _logixManager.getBySystemName(name);
        }
        if (l == null) {
            messageInvalidLogixName(name);
            return null;
        }
        return name;
    }
    /**
    * Checks Route reference of text.  Upshifts text of field if it is a system name
    */
    String validateRouteReference(String name) {
        Route r = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            r = InstanceManager.routeManagerInstance().getByUserName(name);
            if (r != null) {
                return name;
            }
            // check memory system name
            name = name.toUpperCase().trim();
            r = InstanceManager.routeManagerInstance().getBySystemName(name);
        }
        if (r == null) {
            messageInvalidRouteName(name);
            return null;
        }
        return name;
    }


    /**
    * get Coditional instance.  Upshifts text of string if it is a system name
    */
    Conditional getConditional(String name) {
        Conditional c = null;

        name = name.trim();
        if ((name != null) && (name != "")) {
            c = _conditionalManager.getByUserName(_curLogix, name);
            if (c != null) {
                return c;
            }
            c = _conditionalManager.getByUserName(name);
            if (c != null) {
                return c;
            }
            name = name.toUpperCase().trim();
            c = _conditionalManager.getBySystemName(name);
        }
        if (c == null) {
            messageInvalidLightName(name, false);
        }
        return c;
    }

    /**
    * get Memory instance.  Upshifts text of string if it is a system name
    */
    Memory getMemory(String name) {
        Memory m = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            m = InstanceManager.memoryManagerInstance().getByUserName(name);
            if (m != null) {
                return m;
            }
            // check memory system name
            name = name.toUpperCase().trim();
            m = InstanceManager.memoryManagerInstance().getBySystemName(name);
        }
        if (m == null) {
            messageInvalidMemoryName(name, false);
        }
        return m;
    }

    /**
    * get Light instance.  Upshifts text of string if it is a system name
    */
    Light getLight(String name) {
        Light l = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            l = InstanceManager.lightManagerInstance().getByUserName(name);
            if (l != null) {
                return l;
            }
            name = name.toUpperCase().trim();
            l = InstanceManager.lightManagerInstance().getBySystemName(name);
        }
        if (l == null) {
            messageInvalidLightName(name, false);
        }
        return l;
    }

    /**
    * get sensor instance.  Upshifts text of string if it is a system name
    */
    Sensor getSensor(String name) {
        Sensor s = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            s = InstanceManager.sensorManagerInstance().getByUserName(name);
            if (s != null) {
                return s;
            }
            name = name.toUpperCase().trim();
            s = InstanceManager.sensorManagerInstance().getBySystemName(name);
        }
        if (s == null) {
            messageInvalidSensorName(name, false);
        }
        return s;
    }

    /**
    * get Turnout instance.  Upshifts text of string if it is a system name
    */
    Turnout getTurnout(String name) {
        Turnout t = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            t = InstanceManager.turnoutManagerInstance().getByUserName(name);
            if (t != null) {
                return t;
            }
            name = name.toUpperCase().trim();
            t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
        }
        if (t == null) {
            messageInvalidTurnoutName(name, false);
        }
        return t;
    }

    /**
    * get SignalHead instance.  Upshifts text of string if it is a system name
    */
    SignalHead getSignalHead(String name) {
        SignalHead h = null;
        name = name.trim();
        if ((name != null) && (name != "")) {
            h = InstanceManager.signalHeadManagerInstance().getByUserName(name);
            if (h != null) {
                return h;
            }
            name = name.toUpperCase().trim();
            h = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
        }
        if (h == null) {
            messageInvalidSignalHeadName(name, false);
        }
        return h;
    }

	/**
	 * Identifies State Variable Type from Text String Note: if string does not
	 * correspond to a state variable type as defined in
	 * stateVariableTypeToString, returns 0.
	 */
	int stringToStateVariableType(String s) {
		int type = 0;
		for (int i = 1; i <= Conditional.MAX_STATE_VARIABLES; i++) {
			if (s.equals(ConditionalVariable.getTypeString(i))) {
				type = i;
				return (type);
			}
		}
		return type;
	}

	/**
	 * Signal Appearance to Signal Appearance Index
	 */
	int signalAppearanceToAppearanceIndex(int appearance) {
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
	 * Signal Appearance Index to Signal Appearance
	 */
	int signalAppearanceIndexToAppearance(int appearanceIndex) {
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
	 * Parses time in hh:mm format given a string in the correct format
	 * <P>
	 * Returns integer = hh*60 + mm (minutes since midnight) if parse is
	 * successful, else returns -1.
	 * <P>
	 * If errors in format are found, an error message is sent to the user and
	 * logged
	 * 
	 * @param s -
	 *            string with time in hh:mm format
	 */
	int parseTime(String s) {
		int nHour = 0;
		int nMin = 0;
		boolean error = false;
        int index = s.indexOf(':');
        String hour = null;
        String minute = null;
        try {
            if (index > 0)
            {
                hour = s.substring(0, index);
                if (index > 1)

                    minute = s.substring(index+1);
                else
                    minute = "0";
            } else if (index == 0)
            {
                hour = "0";
                minute = s.substring(index+1);
            } else {
                hour = s;
                minute = "0";
            }
        } catch (IndexOutOfBoundsException ioob ) {
            error = true;
        }
        if (!error)  {
            try {
                nHour = Integer.valueOf(hour);
                if ((nHour < 0) || (nHour > 24)) {
                    error = true;
                }
                nMin = Integer.valueOf(minute);
                if ((nMin < 0) || (nMin > 59)) {
                    error = true;
                }
            } catch (NumberFormatException e) {
                error = true;
            }
        }
		if (error) {
			// if unsuccessful, print error message
			javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
					java.text.MessageFormat.format(rbx.getString("Error26"),
					new Object[] { s }), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return (-1);
		}
		// here if successful
		return ((nHour * 60) + nMin);
	}

	/**
	 * Formats time to hh:mm given integer hour and minute
	 */
	public static String formatTime(int hour, int minute) {
		String s = "";
		String t = Integer.toString(hour);
		if (t.length() == 2) {
			s = t + ":";
		} else if (t.length() == 1) {
			s = "0" + t + ":";
		}
		t = Integer.toString(minute);
		if (t.length() == 2) {
			s = s + t;
		} else if (t.length() == 1) {
			s = s + "0" + t;
		}
		if (s.length() != 5) {
			// input error
			s = "00:00";
		}
		return s;
	}

	/**
	 * Utility routine for formatting up the various "does not match an
	 * existing" error messages.
	 * 
	 * @param msg
	 *            Index of the message string from the properties file
	 * @param name
	 *            Bad/not found bean name
	 * @param table
	 *            If true, display message Error21 about editing in the table
	 */
	void messageGeneralInvalidBean(String msg, String name, boolean table) {
		javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
				java.text.MessageFormat.format(rbx.getString(msg)
						+ (table ? rbx.getString("Error21") : ""),
						new Object[] { name }), rbx.getString("ErrorTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Sends an invalid turnout name error message for Edit Conditional window
	 */
	void messageInvalidTurnoutName(String name, boolean table) {
		messageGeneralInvalidBean("Error13", name, table);
	}

	/**
	 * Sends an invalid signal head name error message for Edit Conditional
	 * window
	 */
	void messageInvalidSignalHeadName(String name, boolean table) {
		messageGeneralInvalidBean("Error14", name, table);
	}

	/**
	 * Sends an invalid sensor name error message for Edit Conditional window
	 */
	void messageInvalidSensorName(String name, boolean table) {
		messageGeneralInvalidBean("Error15", name, table);
	}

	/**
	 * Sends an invalid light name error message for Edit Conditional window
	 */
	void messageInvalidLightName(String name, boolean table) {
		messageGeneralInvalidBean("Error16", name, table);
	}

	/**
	 * Sends an invalid memory system name error message for Edit Conditional
	 * window
	 */
	void messageInvalidMemoryName(String name, boolean table) {
		messageGeneralInvalidBean("Error17", name, table);
	}

	/**
	 * Sends an invalid route name error message for Edit Conditional window
	 */
	void messageInvalidRouteName(String name) {
		messageGeneralInvalidBean("Error18", name, true);
	}

	/**
	 * Sends an invalid conditional name error message for Edit Conditional
	 * window
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

	// *********** Special Table Models ********************

	/**
	 * Table model for Conditionals in Edit Logix window
	 */
	public class ConditionalTableModel extends AbstractTableModel implements
			PropertyChangeListener {

		public static final int SNAME_COLUMN = 0;

		public static final int UNAME_COLUMN = 1;

		public static final int STATE_COLUMN = 2;

		public static final int BUTTON_COLUMN = 3;

		public ConditionalTableModel() {
			super();
			_conditionalManager.addPropertyChangeListener(this);
			updateConditionalListeners();
		}

		synchronized void updateConditionalListeners() {
			// first, remove listeners from the individual objects
			String sNam = "";
			Conditional c = null;
            numConditionals = _curLogix.getNumConditionals();
            for (int i = 0; i < numConditionals; i++) {
                // if object has been deleted, it's not here; ignore it
                sNam = _curLogix.getConditionalByNumberOrder(i);
                c = _conditionalManager.getBySystemName(sNam);
                if (c != null)
                    c.removePropertyChangeListener(this);
            }
			// and add them back in
			for (int i = 0; i < numConditionals; i++) {
                sNam = _curLogix.getConditionalByNumberOrder(i);
                c = _conditionalManager.getBySystemName(sNam);
                if (c != null)
                    addPropertyChangeListener(this);
			}
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				// a new NamedBean is available in the manager
				updateConditionalListeners();
				fireTableDataChanged();
			} else if (matchPropertyName(e)) {
				// a value changed.
				fireTableDataChanged();
			}
		}

		/**
		 * Is this property event announcing a change this table should display?
		 * <P>
		 * Note that events will come both from the NamedBeans and also from the
		 * manager
		 */
		boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			return (e.getPropertyName().indexOf("State") >= 0 || e
					.getPropertyName().indexOf("Appearance") >= 0);
		}

		public Class<?> getColumnClass(int c) {
			if (c == BUTTON_COLUMN) {
				return JButton.class;
			} else {
				return String.class;
			}
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return (numConditionals);
		}

		public boolean isCellEditable(int r, int c) {
			if (!_inReorderMode) {
				return ((c == UNAME_COLUMN) || (c == BUTTON_COLUMN));
			} else if (c == BUTTON_COLUMN) {
				if (r >= _nextInOrder)
					return (true);
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
				return ""; // no label
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

		public Object getValueAt(int r, int col) {
			int rx = r;
			if ((rx > numConditionals) || (_curLogix == null)) {
				return null;
			}
			switch (col) {
			case BUTTON_COLUMN:
				if (!_inReorderMode) {
					return rb.getString("ButtonEdit");
				} else if (_nextInOrder == 0) {
					return rbx.getString("ButtonFirst");
				} else if (_nextInOrder <= r) {
					return rbx.getString("ButtonNext");
				} else
					return Integer.toString(rx + 1);
			case SNAME_COLUMN:
				return _curLogix.getConditionalByNumberOrder(rx);
            case UNAME_COLUMN: {
                    //log.debug("ConditionalTableModel: "+_curLogix.getConditionalByNumberOrder(rx));
                    Conditional c = _conditionalManager.getBySystemName(
                        _curLogix.getConditionalByNumberOrder(rx));
                    if (c!=null) return c.getUserName();
                    else return "";
                }
            case STATE_COLUMN:
                Conditional c = _conditionalManager.getBySystemName(
                    _curLogix.getConditionalByNumberOrder(rx));
                if (c != null) {
                    int curState = c.getState();
                    if (curState == Conditional.TRUE)
                        return rbx.getString("True");
                    if (curState == Conditional.FALSE)
                        return rbx.getString("False");
                }
                return rbx.getString("Unknown");
			default:
				return rbx.getString("Unknown");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			int rx = row;
			if ((rx > numConditionals) || (_curLogix == null)) {
				return;
			}
			if (col == BUTTON_COLUMN) {
                if (_inReorderMode) {
					swapConditional(row);
				} 
                else if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
                    javax.swing.JOptionPane.showMessageDialog(
                                editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Warn8"),
                                new Object[] {SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName}),
                                rbx .getString("WarnTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
                } 
                else {
                    // Use separate Thread so window is created on top
                    class WindowMaker implements Runnable {
                        int row;
                        WindowMaker(int r){
                            row = r;
                        }
                        public void run() {
                                //Thread.yield();
                                editConditionalPressed(row);
                            }
                        }
                    WindowMaker t = new WindowMaker(rx);
					javax.swing.SwingUtilities.invokeLater(t);
				}
			} 
            else if (col == UNAME_COLUMN) {
				String uName = (String) value;
				if (_curLogix != null) {
					Conditional cn = _conditionalManager.getByUserName(_curLogix,
							uName.trim());
					if (cn == null) {
						_conditionalManager.getBySystemName(
								_curLogix.getConditionalByNumberOrder(rx))
								.setUserName(uName.trim());
						fireTableRowsUpdated(rx, rx);
					} else {
						String svName = _curLogix.getConditionalByNumberOrder(rx);
						if (cn != _conditionalManager.getBySystemName(svName)) {
							messageDuplicateConditionalUserName(cn
									.getSystemName());
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

		public static final int ROWNUM_COLUMN = 0;

		public static final int AND_COLUMN = 1;

		public static final int NOT_COLUMN = 2;

		public static final int DESCRIPTION_COLUMN = 3;

		public static final int STATE_COLUMN = 4;

		public static final int TRIGGERS_COLUMN = 5;

		public static final int EDIT_COLUMN = 6;

		public static final int DELETE_COLUMN = 7;

		public Class<?> getColumnClass(int c) {
            switch (c)
            {
                case ROWNUM_COLUMN:
                    return String.class;
                case AND_COLUMN:
                    return JComboBox.class;
                case NOT_COLUMN:
                    return JComboBox.class;
                case DESCRIPTION_COLUMN:
                    return String.class;
                case STATE_COLUMN:
                    return String.class;
                case TRIGGERS_COLUMN:
                    return Boolean.class;
                case EDIT_COLUMN:
                    return JButton.class;
                case DELETE_COLUMN:
                    return JButton.class;
            }
			return String.class;
		}

		public int getColumnCount() {
			return 8;
		}

		public int getRowCount() {
			return _variableList.size();
		}

		public boolean isCellEditable(int r, int c) {
			switch (c) {
                case ROWNUM_COLUMN:
                    return (false);
                case AND_COLUMN:
                    return (_logicType == Conditional.MIXED );
                case NOT_COLUMN:
                    return (true);
                case DESCRIPTION_COLUMN:
                    return (false);
                case STATE_COLUMN:
                    return (false);
                case TRIGGERS_COLUMN:
                    return (true);
                case EDIT_COLUMN:
                    return (true);
                case DELETE_COLUMN:
                    return (true);
			}
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
                case ROWNUM_COLUMN:
                    return (rbx.getString("ColumnLabelRow"));
                case AND_COLUMN:
                    return (rbx.getString("ColumnLabelOperator"));
                case NOT_COLUMN:
                    return (rbx.getString("ColumnLabelNot"));
                case DESCRIPTION_COLUMN:
                    return (rbx.getString("ColumnLabelDescription"));
                case STATE_COLUMN:
                    return (rbx.getString("ColumnLabelState"));
                case TRIGGERS_COLUMN:
                    return (rbx.getString("ColumnLabelTriggersCalculation"));
                case EDIT_COLUMN:
                    return "";
                case DELETE_COLUMN:
                    return "";
 			}
			return "";
		}

		public int getPreferredWidth(int col) {
			if (col == DESCRIPTION_COLUMN) {
                return 500;
            }
            return 10;
		}

		public Object getValueAt(int r, int c) {
			if ( r >= _variableList.size() ) {
				return null;
			}
            ConditionalVariable variable = _variableList.get(r);
			switch (c) {
                case ROWNUM_COLUMN:
                    return (rbx.getString("rowAbrev") + (r + 1));
                case AND_COLUMN:
                    if (r==0) {
                        return "";
                    }
                    return variable.getOpernString();
                case NOT_COLUMN:
                    if (variable.isNegated())
                        return rbx.getString("LogicNOT");
                    break;
                case DESCRIPTION_COLUMN:
                    return variable.toString();
                case STATE_COLUMN:
                    switch (variable.getState()) {
                        case Conditional.TRUE:
                            return rbx.getString("True"); 
                        case Conditional.FALSE:
                            return rbx.getString("False"); 
                        case Conditional.UNKNOWN:
                            return rbx.getString("Unknown"); 
                    }
                    break;
                case TRIGGERS_COLUMN:
                    return new Boolean(variable.doTriggerActions());
                case EDIT_COLUMN:
                    return rbx.getString("ButtonEdit");
                case DELETE_COLUMN:
                    return rbx.getString("ButtonDelete");
			}
			return null;
		}

		public void setValueAt(Object value, int r, int c) {
			if ( r >= _variableList.size() ) {
				return;
			}
            ConditionalVariable variable = _variableList.get(r);
			switch (c) {
                case AND_COLUMN:
                    variableOperatorChanged(r, (String)value);
                    break;
                case NOT_COLUMN:
                    variableNegationChanged(r, (String)value);
                    break;
                case STATE_COLUMN:
                    String state = ((String)value).toUpperCase().trim();
                    if ( state.equals(rbx.getString("True").toUpperCase().trim()) ) {
                        variable.setState(Conditional.TRUE);
                    } else  if ( state.equals(rbx.getString("False").toUpperCase().trim()) )  {
                        variable.setState(Conditional.FALSE);
                    } else {
                        variable.setState(Conditional.UNKNOWN);
                    }
                    break;
                case TRIGGERS_COLUMN:
                    variable.setTriggerActions(!variable.doTriggerActions());
                    break;
                case EDIT_COLUMN:
                    // Use separate Thread so window is created on top
                    class WindowMaker implements Runnable {
                        int row;
                        WindowMaker(int r){
                            row = r;
                        }
                        public void run() {
                                //Thread.yield();
                                makeEditVariableWindow(row);
                            }
                        }
                    WindowMaker t = new WindowMaker(r);
                    javax.swing.SwingUtilities.invokeLater(t);
                    break;
                case DELETE_COLUMN:
                    deleteVariablePressed(r);
                    break;
            }
		}
	}

	/**
	 * Table model for Actions in Edit Conditional window
	 */
	public class ActionTableModel extends AbstractTableModel {

		public static final int DESCRIPTION_COLUMN = 0;

		public static final int EDIT_COLUMN = 1;

		public static final int DELETE_COLUMN = 2;

		public Class<?> getColumnClass(int c) {
            if (c == EDIT_COLUMN || c ==DELETE_COLUMN )
            {
                return JButton.class;
            }
			return super.getColumnClass(c);
		}

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return _actionList.size();
		}

		public boolean isCellEditable(int r, int c) {
            if (c == DESCRIPTION_COLUMN)  {
                return false;
            }
			if ( _inReorderMode && (c ==EDIT_COLUMN || r < _nextInOrder) ) {
                return false;
			}
            return true;
        }

		public String getColumnName(int col) {
			if ( col == DESCRIPTION_COLUMN)
            {
                return rbx.getString("LabelActionDescription");
            }
			return "";
		}

		public int getPreferredWidth(int col) {
            if (col == DESCRIPTION_COLUMN)
            {
                return 680;
            }
            return 20;
        }

        public Object getValueAt(int row, int col) {
            if (row >= _actionList.size()) {
                return null;
            }
			switch (col) {
                case DESCRIPTION_COLUMN:
                    ConditionalAction action = _actionList.get(row);
                    return action.toString();
                case EDIT_COLUMN:
                    return rbx.getString("ButtonEdit");
                case DELETE_COLUMN:
                    if (!_inReorderMode) {
                        return rb.getString("ButtonDelete");
                    } else if (_nextInOrder == 0) {
                        return rbx.getString("ButtonFirst");
                    } else if (_nextInOrder <= row) {
                        return rbx.getString("ButtonNext");
                    }
                    return Integer.toString(row + 1);
			}
            return null;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == EDIT_COLUMN) {
                // Use separate Thread so window is created on top
                class WindowMaker implements Runnable {
                    int row;
                    WindowMaker(int r){
                        row = r;
                    }
                    public void run() {
                            //Thread.yield();
                            makeEditActionWindow(row);
                        }
                    }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
            }
            else if (col == DELETE_COLUMN) {
				if (_inReorderMode) 
					swapActions(row);
				else
                    deleteActionPressed(row);
            }
        }
    }

	static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(LogixTableAction.class.getName());
}
/* @(#)LogixTableAction.java */

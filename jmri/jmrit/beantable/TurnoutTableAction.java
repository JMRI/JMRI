// TurnoutTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperationManager;
import jmri.TurnoutOperation;
import jmri.Sensor;
import jmri.jmrit.turnoutoperations.TurnoutOperationFrame;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Swing action to create and register a
 * TurnoutTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2004
 * @version     $Revision: 1.23 $
 */

public class TurnoutTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public TurnoutTableAction(String actionName) { 
	super(actionName);

        // disable ourself if there is no primary turnout manager available
        if (jmri.InstanceManager.turnoutManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .turnoutManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }

    }

    public TurnoutTableAction() { this("Turnout Table");}
	
	String closedText = InstanceManager.turnoutManagerInstance().getClosedText();
	String thrownText = InstanceManager.turnoutManagerInstance().getThrownText();

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Turnouts
     */
    void createModel() {
        m = new BeanTableDataModel() {
		    static public final int KNOWNCOL = 3;
		    static public final int MODECOL = 4;
		    static public final int SENSOR1COL = 5;
		    static public final int SENSOR2COL = 6;
		    static public final int OPSONOFFCOL = 7;
    		public int getColumnCount( ){ 
    		    if (showFeedback)
    		        return NUMCOLUMN+5;
    		    else
    		        return NUMCOLUMN;
     		}
    		
    		public String getColumnName(int col) {
    			if (col==KNOWNCOL) return "Feedback";
    			else if (col==MODECOL) return "Mode";
    			else if (col==SENSOR1COL) return "Sensor 1";
    			else if (col==SENSOR2COL) return "Sensor 2";
    			else if (col==OPSONOFFCOL) return "Automate";
    			
    			else if (col==VALUECOL) return "Cmd";  // override default title
    			
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==KNOWNCOL) return String.class;
    			else if (col==MODECOL) return JComboBox.class;
    			else if (col==SENSOR1COL) return String.class;
    			else if (col==SENSOR2COL) return String.class;
    			else if (col==OPSONOFFCOL) return JComboBox.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==KNOWNCOL) return new JTextField(6).getPreferredSize().width;
    			else if (col==MODECOL) return new JTextField(10).getPreferredSize().width;
    			else if (col==SENSOR1COL) return new JTextField(5).getPreferredSize().width;
    			else if (col==SENSOR2COL) return new JTextField(5).getPreferredSize().width;
    			else if (col==OPSONOFFCOL) return new JTextField(14).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==KNOWNCOL) return false;
    			else if (col==MODECOL) return true;
    			else if (col==SENSOR1COL) return true;
    			else if (col==SENSOR2COL) return true;
    			else if (col==OPSONOFFCOL) return true;
    			else return super.isCellEditable(row,col);
			}    		

    		public Object getValueAt(int row, int col) {
				String name = (String)sysNameList.get(row);
				TurnoutManager manager = InstanceManager.turnoutManagerInstance();
				Turnout t = manager.getBySystemName(name);
    			if (col==KNOWNCOL) {
                    if (t.getKnownState()==Turnout.CLOSED) return closedText;
                    if (t.getKnownState()==Turnout.THROWN) return thrownText;
                    if (t.getKnownState()==Turnout.INCONSISTENT) return "Inconsistent";
                    else return "Unknown";
    			} else if (col==MODECOL) {
					JComboBox c = new JComboBox(t.getValidFeedbackNames());
					c.setSelectedItem(t.getFeedbackModeName());
					return c;
    			} else if (col==SENSOR1COL) {
                    Sensor s = t.getFirstSensor();
                    if (s!=null) return s.getSystemName();
                    else return "";
    			} else if (col==SENSOR2COL) {
                    Sensor s = t.getSecondSensor();
                    if (s!=null) return s.getSystemName();
                    else return "";
    			} else if (col==OPSONOFFCOL) {
    				return makeAutomationBox(t);
    			} else return super.getValueAt(row, col);
			}    		
			
    		public void setValueAt(Object value, int row, int col) {
				String name = (String)sysNameList.get(row);
				TurnoutManager manager = InstanceManager.turnoutManagerInstance();
				Turnout t = manager.getBySystemName(name);
    			if (col==MODECOL) {
                    String modeName = (String)((JComboBox)value).getSelectedItem();
    				t.setFeedbackMode(modeName);
    			} else if (col==SENSOR1COL) {
                    String sname = (String)value;
                    Sensor s;
                    if (!sname.equals("")) s = InstanceManager.sensorManagerInstance().provideSensor((String)value);
                    else s = null;
                    t.provideFirstFeedbackSensor(s);
    			} else if (col==SENSOR2COL) {
                    String sname = (String)value;
                    Sensor s;
                    if (!sname.equals("")) s = InstanceManager.sensorManagerInstance().provideSensor((String)value);
                    else s = null;
                    t.provideSecondFeedbackSensor(s);
    			} else if (col==OPSONOFFCOL) {
    									// do nothing as this is handled by the combo box listener
    			} else super.setValueAt(value, row, col);
    		}

            public String getValue(String name) {
                int val = InstanceManager.turnoutManagerInstance().getBySystemName(name).getCommandedState();
                switch (val) {
                case Turnout.CLOSED: return closedText;
                case Turnout.THROWN: return thrownText;
                case Turnout.UNKNOWN: return rbean.getString("BeanStateUnknown");
                case Turnout.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.turnoutManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.turnoutManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                int state = ((Turnout)t).getCommandedState();
                if (state==Turnout.CLOSED) ((Turnout)t).setCommandedState(Turnout.THROWN);
                else ((Turnout)t).setCommandedState(Turnout.CLOSED);
            }
            public JButton configureButton() {
				return new JButton(thrownText);
            }

            public void configureTable(JTable table) {
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                super.configureTable(table);
            }

        };  // end of custom data model
    }
    
    void setTitle() {
        f.setTitle(f.rb.getString("TitleTurnoutTable"));
    }
    JFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddTurnout"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.show();
    }

    boolean showFeedback = false;
    void showFeedbackChanged() {
        showFeedback = showFeedbackBox.isSelected();
        m.fireTableStructureChanged(); // update view
    }
    
    /**
     * Create a JComboBox containing all the options for turnout automation parameters for
     * this turnout
     * @param t	the turnout
     * @return	the JComboBox
     */
    protected JComboBox makeAutomationBox(Turnout t) {
    	String[] str = new String[]{"empty"};
    	final JComboBox cb = new JComboBox(str);
    	final Turnout myTurnout = t;
    	updateAutomationBox(t, cb);
    	cb.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			setTurnoutOperation(myTurnout, cb);
    			cb.removeActionListener(this);		// avoid recursion
    			updateAutomationBox(myTurnout, cb);
    			cb.addActionListener(this);
    		}
    	});
    	return cb;    	
    }
    
    /**
     * Add the content and make the appropriate selection to a combox box for a turnout's
     * automation choices
     * @param t	turnout
     * @param cb	the JComboBox
     */
    public static void updateAutomationBox(Turnout t, JComboBox cb) {
    	TurnoutOperation[] ops = TurnoutOperationManager.getInstance().getTurnoutOperations();
    	cb.removeAllItems();
    	Vector strings = new Vector(20);
    	Vector defStrings = new Vector(20);
    	for (int i=0; i<ops.length; ++i) {
    		if (!ops[i].isDefinitive()
    				&& ops[i].matchFeedbackMode(t.getFeedbackMode())
    				&& !ops[i].isNonce()) {
    			strings.add(ops[i].getName());
    		}
    	}
    	for (int i=0; i<ops.length; ++i) {
    		if (ops[i].isDefinitive()
    				&& ops[i].matchFeedbackMode(t.getFeedbackMode())) {
    			defStrings.add(ops[i].getName());
    		}
    	}
    	java.util.Collections.sort(strings);
    	java.util.Collections.sort(defStrings);
    	strings.add(0, new String("Off"));
    	strings.add(1, new String("Use Global Default"));
    	strings.add(2, new String("Edit..."));
    	for (int i=0; i<defStrings.size(); ++i) {
    		strings.add(i+3, defStrings.get(i));
    	}
    	for (int i=0; i<strings.size(); ++i) {
    		cb.addItem(strings.get(i));
    	}
    	if (t.getInhibitOperation()) {
    		cb.setSelectedIndex(0);
    	} else if (t.getTurnoutOperation() == null) {
    		cb.setSelectedIndex(1);
    	} else if (t.getTurnoutOperation().isNonce()) {
    		cb.setSelectedIndex(2);
    	} else {
    		cb.setSelectedItem(t.getTurnoutOperation().getName());
    	}
    }
    
    /**
     * set the turnout's operation info based on the contents of the combo box
     * @param t	turnout
     * @param cb JComboBox
     */
    private void setTurnoutOperation(Turnout t, JComboBox cb) {
		switch (cb.getSelectedIndex())
		{
		case 0:			// Off
			t.setInhibitOperation(true);
			t.setTurnoutOperation(null);
			break;
		case 1:			// Default
			t.setInhibitOperation(false);
			t.setTurnoutOperation(null);
			break;
		case 2:			// Edit... (use nonce)
			t.setInhibitOperation(false);
			editTurnoutOperation(t, cb);
			break;
		default:		// named operation
			t.setInhibitOperation(false);
			t.setTurnoutOperation(TurnoutOperationManager.getInstance().
							getOperation(((String)cb.getSelectedItem())));	
			break;
		}
	}
    
    /**
     * pop up a TurnoutOperationConfig for the turnout 
     * @param t turnout
     * @param box JComboBox that triggered the edit
     */
    protected void editTurnoutOperation(Turnout t, JComboBox box) {
    	TurnoutOperation op = t.getTurnoutOperation();
    	if (op==null) {
    		TurnoutOperation proto = TurnoutOperationManager.getInstance().getMatchingOperationAlways(t);
    		if (proto != null) {
    			op = proto.makeNonce(t);
    			t.setTurnoutOperation(op);
    		}
    	}
    	if (op != null) {
    		TurnoutOperationEditor dialog = new TurnoutOperationEditor(this, f, op, t, box);
    		dialog.show();
    	} else {
			JOptionPane.showMessageDialog(f, new String("There is no operation type suitable for this turnout"),
					"No operation type", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    protected class TurnoutOperationEditor extends JDialog {
    	TurnoutOperationConfig config;
    	TurnoutOperation myOp;
    	Turnout myTurnout;
    	
    	TurnoutOperationEditor(TurnoutTableAction tta, JFrame parent, TurnoutOperation op, Turnout t, JComboBox box) {
    		super(parent);
    		final TurnoutOperationEditor self = this;
    		myOp = op;
    		myOp.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
    			public void propertyChange(java.beans.PropertyChangeEvent evt) {
    				if (evt.getPropertyName().equals("Deleted")) {
    					hide();
    				}
    			}
    		});
    		myTurnout = t;
        	config = TurnoutOperationConfig.getConfigPanel(op);
        	setSize(300,150);
        	setTitle();
        	if (config != null) {
        		Box outerBox = Box.createVerticalBox();
        		outerBox.add(config);
        		Box buttonBox = Box.createHorizontalBox();
        		JButton nameButton = new JButton("Give name to this setting");
        		nameButton.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				String newName = JOptionPane.showInputDialog("New name for this parameter setting:");
        				if (newName != null && !newName.equals("")) {
        					if (!myOp.rename(newName)) {
        						JOptionPane.showMessageDialog(self, new String("This name is already in use"),
        								"Name already in use", JOptionPane.ERROR_MESSAGE);
        					}
        					setTitle();
        					myTurnout.setTurnoutOperation(null);
        					myTurnout.setTurnoutOperation(myOp);	// no-op but updates display - have to <i>change</i> value
        				}
        			}
        		});
        		JButton okButton = new JButton("OK");
        		okButton.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				config.endConfigure();
        				if (myOp.isNonce() && myOp.equivalentTo(myOp.getDefinitive())) {
        					myTurnout.setTurnoutOperation(null);
        					myOp.dispose();
        					myOp = null;
        				}
        				self.hide();
        			}
        		});
        		JButton cancelButton = new JButton("Cancel");
        		cancelButton.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				self.hide();
        			}
        		});
        		buttonBox.add(Box.createHorizontalGlue());
        		if (!op.isDefinitive()) {
        			buttonBox.add(nameButton);
        		}
        		buttonBox.add(okButton);
        		buttonBox.add(cancelButton);
        		outerBox.add(buttonBox);
        		getContentPane().add(outerBox);
        		show();
        	}
    	}    
    	private void setTitle() {
    		String title = "Turnout Operation \"" + myOp.getName() + "\"";
    		if (myOp.isNonce()) {
    			title = "Turnout operation for turnout " + myTurnout.getSystemName();
    		}
    		setTitle(title);    		
    	}
    }
    
    JCheckBox showFeedbackBox = new JCheckBox("Show feedback information");
    JCheckBox doAutomationBox = new JCheckBox("Automatic retry");
    
    /**
     * Add the check box and Operations menu item
     */
    public void addToFrame(BeanTableFrame f) {
    	final BeanTableFrame finalF = f;			// needed for anonymous ActionListener class
        f.addToBottomBox(showFeedbackBox);
        showFeedbackBox.setToolTipText("Show extra columns for configuring turnout feedback?");
        showFeedbackBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFeedbackChanged();
            }
        });
        f.addToBottomBox(doAutomationBox);
        doAutomationBox.setSelected(TurnoutOperationManager.getInstance().getDoOperations());
        doAutomationBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
        	}
        });
        // add save menu item
        JMenuBar menuBar = f.getJMenuBar();
        JMenu opsMenu = new JMenu("Automation");
        menuBar.add(opsMenu);
        JMenuItem item = new JMenuItem("Edit...");
        opsMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		TurnoutOperationFrame tof = new TurnoutOperationFrame(finalF);
        		tof.show();
        	}
        });
    }


    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        // Test if bit already in use as a light
        String sName = sysName.getText().toUpperCase();
        if (sName.charAt(1)=='T') {
            // probably standard format turnout system name
            String testSN = sName.substring(0,1)+"L"+sName.substring(2,sName.length());
            jmri.Light testLight = InstanceManager.lightManagerInstance().
                                        getBySystemName(testSN);
            if (testLight != null) {
                // Address is already used as a Light
                log.warn("Requested Turnout "+sName+" uses same address as Light "+testSN);
                if (!noWarn) {
                    int selectedValue = JOptionPane.showOptionDialog(addFrame,
                        rb.getString("TurnoutWarn1")+" "+sName+" "+rb.getString("TurnoutWarn2")+" "+
                        testSN+".\n   "+rb.getString("TurnoutWarn3"),rb.getString("WarningTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
                        new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
                        rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
                    if (selectedValue == 1) return;   // return without creating if "No" response
                    if (selectedValue == 2) {
                        // Suppress future warnings, and continue
                        noWarn = true;
                    }
                }
            }
        }
        InstanceManager.turnoutManagerInstance().newTurnout(sName, user);
    }
    private boolean noWarn = false;

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutTableAction.class.getName());
}

/* @(#)TurnoutTableAction.java */

// LightTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Light;
import jmri.Sensor;
import jmri.Turnout;

import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * LightTable GUI.
 * <P>
 * Based on SignalHeadTableAction.java
 *
 * @author	Dave Duchamp    Copyright (C) 2004
 * @version     $Revision: 1.22 $
 */

public class LightTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public LightTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Light manager available
        if (jmri.InstanceManager.lightManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .lightManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }
    }
    public LightTableAction() { this("Light Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Lights
     */
    void createModel() {
        m = new BeanTableDataModel() {
		    static public final int ENABLECOL = NUMCOLUMN;
		    static public final int EDITCOL = ENABLECOL+1;
			protected String enabledString = rb.getString("ColumnHeadEnabled");
    		public int getColumnCount( ){ return NUMCOLUMN+2;}
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
				// override default value for UserName column
				if (col==USERNAMECOL) return new JTextField(16).getPreferredSize().width;
    			if (col==EDITCOL) return new JTextField(6).getPreferredSize().width;
    			if (col==ENABLECOL) return new JTextField(6).getPreferredSize().width;
				else return super.getPreferredWidth(col);
			}
    		public boolean isCellEditable(int row, int col) {
    			if (col==EDITCOL) return true;
    			if (col==ENABLECOL) return true;
    			else return super.isCellEditable(row,col);
			}    		
            public String getValue(String name) {
                int val = InstanceManager.lightManagerInstance().getBySystemName(name).getState();
                switch (val) {
                case Light.ON: return rbean.getString("LightStateOn");
                case Light.OFF: return rbean.getString("LightStateOff");
                default: return "Unexpected value: "+val;
                }
            }
    		public Object getValueAt(int row, int col) {
    			if (col==EDITCOL) {
					return rb.getString("ButtonEdit");
    			}
    			else if (col==ENABLECOL) {
    				return new Boolean(((Light)getBySystemName((String)getValueAt(row, SYSNAMECOL))).getEnabled());
    			}
				else return super.getValueAt(row, col);
			}    		
    		public void setValueAt(Object value, int row, int col) {
    			if (col==EDITCOL) {
                    // set up to edit
                    addPressed(null);
                    systemName.setText((String)getValueAt(row, SYSNAMECOL));
                    editPressed(null); // don't really want to stop Light w/o user action
    			}
    			else if (col==ENABLECOL) {
                    // alternate
                    Light l = (Light)getBySystemName((String)getValueAt(row, SYSNAMECOL));
                    boolean v = l.getEnabled();
                    l.setEnabled(!v);
    			}
				else if (col==DELETECOL) {
					Light g = (Light)getBySystemName((String)getValueAt(row, SYSNAMECOL));
					// deactivate this light
					g.deactivateLight();
					// continue using generic deletion code	
					super.setValueAt(value, row, col);
				}
    			else super.setValueAt(value, row, col);
    		}
            boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(enabledString)) return true;
                else return super.matchPropertyName(e);
            }

            public Manager getManager() { 
                return InstanceManager.lightManagerInstance(); 
            }
            public NamedBean getBySystemName(String name) { 
                return InstanceManager.lightManagerInstance().getBySystemName(name);
            }
            public void clickOn(NamedBean t) {
                int oldState = ((Light)t).getState();
                int newState;
                switch (oldState) {
                    case Light.ON: 
                        newState = Light.OFF; 
                        break;
                    case Light.OFF: 
                        newState = Light.ON; 
                        break;
                    default: 
                        newState = Light.OFF; 
                        this.log.warn("Unexpected Light state "+oldState+" becomes OFF");
                        break;
                }
               ((Light)t).setState(newState);
            }
            public JButton configureButton() {
                return new JButton(" "+rbean.getString("LightStateOff")+" ");
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleLightTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }

    JmriJFrame addFrame = null;
    Light curLight = null;
    boolean lightCreated = false;
    boolean warnMsg = false;
    boolean noWarn = false;
	boolean inEditMode = false;
	boolean reminderActive = false;

    String sensorControl = rb.getString("LightSensorControl");
    String fastClockControl = rb.getString("LightFastClockControl");
    String turnoutStatusControl = rb.getString("LightTurnoutStatusControl");
    String timedOnControl = rb.getString("LightTimedOnControl");
	String noControl = rb.getString("LightNoControl");

    // fixed part of add frame
    JTextField systemName = new JTextField(10);
    JLabel systemNameLabel = new JLabel( rb.getString("LightSystemName") );
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");
    JTextField userName = new JTextField(10);
    JLabel userNameLabel = new JLabel( rb.getString("LightUserName") );
    JComboBox typeBox;
    JLabel typeBoxLabel = new JLabel( rb.getString("LightControlType") );
    int sensorControlIndex;
    int fastClockControlIndex;
    int turnoutStatusControlIndex;
	int timedOnControlIndex;
	int noControlIndex;
    JButton create;
    JButton edit;
    JButton update;
    JButton cancel;
    
    // variable part of add frame
    JTextField field1a = new JTextField(10);  // Sensor 
    JTextField field1b = new JTextField(8);  // Fast Clock
    JTextField field1c = new JTextField(10);  // Turnout
	JTextField field1d = new JTextField(10);  // Timed ON
    JLabel f1Label = new JLabel( rb.getString("LightSensor") );
    JTextField field2a = new JTextField(8);  // Fast Clock
    JTextField field2b = new JTextField(8); // Timed ON
    JLabel f2Label = new JLabel( rb.getString("LightSensorSense") );
    JComboBox stateBox;
    int sensorActiveIndex;
    int sensorInactiveIndex;
    int turnoutClosedIndex;
    int turnoutThrownIndex;
    JLabel stateBoxLabel = new JLabel( rb.getString("LightSensorSense") );

    JLabel status1 = new JLabel( rb.getString("LightCreateInst") );
    JLabel status2 = new JLabel( rb.getString("LightEditInst") );
        
    void addPressed(ActionEvent e) {
		if (inEditMode) {
			// cancel Edit and reactivate the editted light
			cancelPressed(null);
		}
        if (addFrame==null) {
            addFrame = new JmriJFrame( rb.getString("TitleAddLight") );
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.LightAddEdit", true);
            addFrame.setLocation(100,30);
            Container contentPane = addFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
            panel1.add(systemNameLabel);
            panel1.add(systemName);
            panel1.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            systemName.setToolTipText( rb.getString("LightSystemNameHint") );
            contentPane.add(panel1);
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
            panel2.add(userNameLabel);
            panel2.add(userName);
            userName.setToolTipText( rb.getString("LightUserNameHint") );
            contentPane.add(panel2);
            
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            panel31.add(typeBoxLabel);
            panel31.add(typeBox = new JComboBox(new String[]{noControl,
                    sensorControl,fastClockControl,turnoutStatusControl,timedOnControl
            }));
			noControlIndex = 0;
            sensorControlIndex = 1;
            fastClockControlIndex = 2;
            turnoutStatusControlIndex = 3;   
			timedOnControlIndex = 4;
            typeBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    controlTypeChanged();
                }
            });
            typeBox.setToolTipText( rb.getString("LightControlTypeHint") );
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            panel32.add(f1Label);
            panel32.add(field1a);
            panel32.add(field1b);
            panel32.add(field1c);
            panel32.add(field1d);
            field1a.setText("");
            field1b.setText("00:00");
            field1c.setText("");
			field1d.setText("");
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field1a.setToolTipText( rb.getString("LightSensorHint") );
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            panel33.add(f2Label);
            panel33.add(stateBox = new JComboBox(new String[]{
                rbean.getString("SensorStateActive"),rbean.getString("SensorStateInactive"),
            }));
            stateBox.setToolTipText( rb.getString("LightSensorSenseHint") );
            panel33.add(field2a);
            panel33.add(field2b);
            field2a.setText("00:00");
            field2a.setVisible(false);
            field2b.setText("0");
            field2b.setVisible(false);
            panel3.add(panel31);
            panel3.add(panel32);
            panel3.add(panel33);
            Border panel3Border = BorderFactory.createEtchedBorder();
            Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                rb.getString("LightControlBorder") );
            panel3.setBorder(panel3Titled);                
            contentPane.add(panel3);
            
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(status1);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            panel42.add(status2);
            panel4.add(panel41);
            panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);

            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(create = new JButton(rb.getString("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText( rb.getString("LightCreateButtonHint") );
            panel5.add(edit = new JButton(rb.getString("ButtonEdit")));
            edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editPressed(e);
                }
            });
            edit.setToolTipText( rb.getString("LightEditButtonHint") );
            panel5.add(update = new JButton(rb.getString("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            update.setToolTipText( rb.getString("LightUpdateButtonHint") );
            panel5.add(cancel = new JButton(rb.getString("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancel.setToolTipText( rb.getString("LightCancelButtonHint") );
            cancel.setVisible(false);
            update.setVisible(false);
            edit.setVisible(true);
            create.setVisible(true);
            contentPane.add(panel5);
        }
        typeBox.setSelectedIndex(0);  // force GUI status consistent

		if (!reminderActive) {
			addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						// if in Edit mode, cancel the Edit and reactivate the Light
						if (inEditMode) {
							cancelPressed(null);
						}
						// remind to save, if Light was created or edited
						if (lightCreated) {
							javax.swing.JOptionPane.showMessageDialog(addFrame,
								rb.getString("Reminder1")+"\n"+rb.getString("Reminder2"),
									rb.getString("ReminderTitle"),
										javax.swing.JOptionPane.INFORMATION_MESSAGE);
						}
						addFrame.setVisible(false);
						addFrame.dispose();
					}
				});
			reminderActive = true;
		}            
        addFrame.pack();
        addFrame.setVisible(true);
    }

    /**
     * Reacts to a control type change
     */
    void controlTypeChanged() {
        setUpControlType( (String)typeBox.getSelectedItem() );
		// set window size - try to maintain user preferences
		Dimension sz = addFrame.getSize();
		addFrame.pack();
		Dimension nsz = addFrame.getSize();
		if (nsz.width>sz.width) sz.width = nsz.width;
		if (nsz.height>sz.height) sz.height = nsz.height;
		addFrame.setSize(sz);
    }

    /**
     * Sets the Control Information according to control type
     */
    void setUpControlType(String ctype) {
        if ( sensorControl.equals(ctype) ) {
            // set up window for sensor control
            f1Label.setText( rb.getString("LightSensor") );
            field1a.setToolTipText( rb.getString("LightSensorHint") );
            f2Label.setText( rb.getString("LightSensorSense") );
            stateBox.removeAllItems();
            stateBox.addItem( rbean.getString("SensorStateActive") );
            sensorActiveIndex = 0;
            stateBox.addItem( rbean.getString("SensorStateInactive") );
            sensorInactiveIndex = 1;
            stateBox.setToolTipText( rb.getString("LightSensorSenseHint") );
            f2Label.setVisible(true);
            field1a.setVisible(true);
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(true);
        } 
        else if (fastClockControl.equals(ctype) ) {
            // set up window for fast clock control
            f1Label.setText( rb.getString("LightScheduleOn") );
            field1b.setToolTipText( rb.getString("LightScheduleHint") );
            f2Label.setText( rb.getString("LightScheduleOff") );
            field2a.setToolTipText( rb.getString("LightScheduleHint") );
            f2Label.setVisible(true);
            field1a.setVisible(false);
            field1b.setVisible(true);
            field1c.setVisible(false);
			field1d.setVisible(false);
            field2a.setVisible(true);
			field2b.setVisible(false);
            stateBox.setVisible(false);
        }
        else if (turnoutStatusControl.equals(ctype) ) {
            // set up window for turnout status control
            f1Label.setText( rb.getString("LightTurnout") );
            field1c.setToolTipText( rb.getString("LightTurnoutHint") );
            f2Label.setText( rb.getString("LightTurnoutSense") );
            stateBox.removeAllItems();
			stateBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
            turnoutClosedIndex = 0;
			stateBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
            turnoutThrownIndex = 1;
            stateBox.setToolTipText( rb.getString("LightTurnoutSenseHint") );
            f2Label.setVisible(true);
            field1a.setVisible(false);
            field1b.setVisible(false);
            field1c.setVisible(true);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(true);
        }
        else if ( timedOnControl.equals(ctype) ) {
            // set up window for sensor control
            f1Label.setText( rb.getString("LightTimedSensor") );
            field1d.setToolTipText( rb.getString("LightTimedSensorHint") );
            f2Label.setText( rb.getString("LightTimedDurationOn") );
            field2b.setToolTipText( rb.getString("LightTimedDurationOnHint") );
            f2Label.setVisible(true);
            field1a.setVisible(false);
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(true);
            field2a.setVisible(false);
            field2b.setVisible(true);
            stateBox.setVisible(false);
        }
		else if (noControl.equals(ctype) ) {
			// set up window for no control 
            f1Label.setText( rb.getString("LightNoneSelected") );
            f2Label.setVisible(false);
            field1a.setVisible(false);
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(false);
		}
        else log.error("Unexpected control type in controlTypeChanged: "+ctype);
    }
    
    /**
     * Responds to the Create button
     */
    void createPressed(ActionEvent e) {
        String suName = (systemName.getText().toUpperCase()).trim();
        String uName = userName.getText();
        // Does System Name have a valid format
        if (!InstanceManager.lightManagerInstance().validSystemNameFormat(suName)) {
            // Invalid System Name format
            log.warn("Invalid Light system name format entered: "+suName);
            status1.setText( rb.getString("LightError3") );
            status2.setText( rb.getString("LightError6") );
            status2.setVisible(true);
            return;
        }
        // Format is valid, normalize it
        String sName = InstanceManager.lightManagerInstance().normalizeSystemName(suName);
        // check if a Light with this name already exists
        Light g = InstanceManager.lightManagerInstance().getBySystemName(sName);
        if (g!=null) {
            // Light already exists
            status1.setText( rb.getString("LightError1") );
            status2.setText( rb.getString("LightError2") );
            status2.setVisible(true);
            return;
        }
        // check if Light exists under an alternate name if an alternate name exists
        String altName = InstanceManager.lightManagerInstance().convertSystemNameToAlternate(suName);
        if (altName != "") {
            g = InstanceManager.lightManagerInstance().getBySystemName(altName);
            if (g!=null) {
                // Light already exists
                status1.setText( rb.getString("LightError10")+" '"+altName+"' "+
                                    rb.getString("LightError11") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return;
            }
        }
        // check if a Light with the same user name exists
        g = InstanceManager.lightManagerInstance().getByUserName(uName);
        if (g!=null) {
            // Light with this user name already exists
            status1.setText( rb.getString("LightError8") );
            status2.setText( rb.getString("LightError9") );
            status2.setVisible(true);
            return;
        }
        // Does System Name correspond to configured hardware
        if (!InstanceManager.lightManagerInstance().validSystemNameConfig(sName)) {
            // System Name not in configured hardware
            status1.setText( rb.getString("LightError5") );
            status2.setText( rb.getString("LightError6") );
            status2.setVisible(true);
            return;
        }
        // check if requested Light uses the same address as a Turnout
        String testSN = sName.substring(0,1)+"T"+
                                            sName.substring(2,sName.length());
        Turnout testT = InstanceManager.turnoutManagerInstance().
                                                    getBySystemName(testSN);
        if (testT != null) {
            // Address is already used as a Turnout
            log.warn("Requested Light "+sName+" uses same address as Turnout "+testT);
            if (!noWarn) {
                int selectedValue = JOptionPane.showOptionDialog(addFrame,
                    rb.getString("LightWarn5")+" "+sName+" "+rb.getString("LightWarn6")+" "+
                    testSN+".\n   "+rb.getString("LightWarn7"),rb.getString("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
                    new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
                    rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
                if (selectedValue == 1) return;   // return without creating if "No" response
                if (selectedValue == 2) {
                    // Suppress future warnings, and continue
                    noWarn = true;
                }
            }
            // Light with this system name already exists as a turnout
            status2.setText( rb.getString("LightWarn4")+" "+testSN+"." );
            warnMsg = true;
        }
        // Create the new Light
        g = InstanceManager.lightManagerInstance().newLight(sName,uName);
        if (g==null) {
            // should never get here unless there is an assignment conflict
            log.error("Failure to create Light with System Name: "+sName);
            return;
        }
		// update the system name since it may have changed
		systemName.setText(g.getSystemName());
        // Get control information 
        if (setControlInformation(g)) {
            // successful, provide feedback to user
            status1.setText( rb.getString("LightCreateFeedback")+" "+sName+", "+uName);
            // change messages and activate Light
            if (!warnMsg) {
                status2.setText( rb.getString("LightEditInst") );
            }
            warnMsg = false;
            status2.setVisible(true);
            g.activateLight();
            lightCreated = true;
        }
    }
    
    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // check if a Light with this name already exists
        String suName = systemName.getText().toUpperCase();
        String sName = InstanceManager.lightManagerInstance().normalizeSystemName(suName);
        if (sName.equals("")) {
            // Entered system name has invalid format
            status1.setText( rb.getString("LightError3") );
            status2.setText( rb.getString("LightError6") );
            status2.setVisible(true);
            return;
        }            
        Light g = InstanceManager.lightManagerInstance().getBySystemName(sName);
        if (g==null) {
            // check if Light exists under an alternate name if an alternate name exists
            String altName = InstanceManager.lightManagerInstance().convertSystemNameToAlternate(sName);
            if (altName != "") {
                g = InstanceManager.lightManagerInstance().getBySystemName(altName);
                if (g!=null) {
                    sName = altName;
                }
            }
            if (g==null) {
                // Light does not exist, so cannot be edited
                status1.setText( rb.getString("LightError7") );
                status2.setText( rb.getString("LightError6") );
                status2.setVisible(true);
                return;
            }
        }
        // Light was found, make its system name not changeable
        curLight = g;
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        systemName.setVisible(false);
        // deactivate this light
        curLight.deactivateLight();
		inEditMode = true;
        // get information for this Light
        userName.setText(g.getUserName());
        int ctType = g.getControlType();
        switch (ctType) {
            case Light.SENSOR_CONTROL:
                setUpControlType(sensorControl);
                typeBox.setSelectedIndex(sensorControlIndex);
                field1a.setText(g.getControlSensorName());
                stateBox.setSelectedIndex(sensorActiveIndex);
                if (g.getControlSensorSense()==Sensor.INACTIVE) {
                    stateBox.setSelectedIndex(sensorInactiveIndex);
                }
                break;
            case Light.FAST_CLOCK_CONTROL:
                setUpControlType(fastClockControl);
                typeBox.setSelectedIndex(fastClockControlIndex);
                int onHour = g.getFastClockOnHour();
                int onMin = g.getFastClockOnMin();
                int offHour = g.getFastClockOffHour();
                int offMin = g.getFastClockOffMin();
                field1b.setText(formatTime(onHour,onMin));
                field2a.setText(formatTime(offHour,offMin));
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                setUpControlType(turnoutStatusControl);
                typeBox.setSelectedIndex(turnoutStatusControlIndex);
                field1c.setText(g.getControlTurnoutName());
                stateBox.setSelectedIndex(turnoutClosedIndex);
                if (g.getControlTurnoutState()==Turnout.THROWN) {
                    stateBox.setSelectedIndex(turnoutThrownIndex);
                }
                break;
            case Light.TIMED_ON_CONTROL:
                setUpControlType(timedOnControl);
                typeBox.setSelectedIndex(timedOnControlIndex);
                int duration = g.getTimedOnDuration();
                field1d.setText(g.getControlTimedOnSensorName());
                field2b.setText(Integer.toString(duration));
                break;
            case Light.NO_CONTROL:
                // Set up as "None"
                setUpControlType(noControl);
                typeBox.setSelectedIndex(noControlIndex);
                field1a.setText("");
                stateBox.setSelectedIndex(sensorActiveIndex);
                break;
        }
        cancel.setVisible(true);
        update.setVisible(true);
        edit.setVisible(false);
        create.setVisible(false);
        status1.setText( rb.getString("LightUpdateInst") );
        status2.setText( "" );
    }

    /**
     * Responds to the Update button
     */
    void updatePressed(ActionEvent e) {
        Light g = curLight;
        // Check if the User Name has been changed
        String uName = userName.getText();
        if ( !(uName.equals(g.getUserName())) ) {
            // user name has changed - check if already in use
            Light p = InstanceManager.lightManagerInstance().getByUserName(uName);
            if (p!=null) {
                // Light with this user name already exists
                status1.setText( rb.getString("LightError8") );
                status2.setText( rb.getString("LightError9") );
                status2.setVisible(true);
                return;
            }
            // user name is unique, change it
            g.setUserName(uName);     
        }
        if (setControlInformation(g)) {
            // provide feedback to user
            status1.setText( rb.getString("LightUpdateFeedback")+
                                                    " "+g.getSystemName()+", "+uName);
            status2.setText( rb.getString("LightEditInst") );
            status2.setVisible(true);
        }
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        systemName.setVisible(true);
        g.activateLight();
        lightCreated = true;
		inEditMode = false;
    }

    /**
     * Retrieve control information from window and update Light
     *    Returns 'true' if no errors or warnings.
     */
    private boolean setControlInformation(Light g) {
        // Get control information
        if (sensorControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.SENSOR_CONTROL);
            // Get sensor control information
            String sensorName = field1a.getText().trim();
			Sensor s = null;
			if (sensorName.length() < 1) {
				// no sensor name entered
				g.setControlType(Light.NO_CONTROL);
			}
			else {
				// name was entered, check for user name first
				s = InstanceManager.sensorManagerInstance().
                                   getByUserName(sensorName);
				if (s==null) {
					// not user name, try system name
					s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
					if (s!=null) {
						// update sensor system name in case it changed
						sensorName = s.getSystemName();
						field1a.setText(sensorName);
					}
				}
			}
            int sState = Sensor.ACTIVE;
            if ( stateBox.getSelectedItem().equals(rbean.getString
                                                    ("SensorStateInactive")) ) {
                sState = Sensor.INACTIVE;
            }
            g.setControlSensor(sensorName);
            g.setControlSensorSense(sState);
            if (s==null) {
                status1.setText( rb.getString("LightWarn1") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return (false);
            }
        }
        else if (fastClockControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.FAST_CLOCK_CONTROL);
            // read and parse the hours and minutes in the two fields
            boolean error = false;
            int onHour = 0;
            int onMin = 0;
            int offHour = 0;
            int offMin = 0;
            String s = field1b.getText();
            if ( (s.length() != 5) || (s.charAt(2) != ':') ) {
                status1.setText( rb.getString("LightError12") );
                error = true;
            }
            if (!error) {
                try {
                    onHour = Integer.valueOf(s.substring(0,2)).intValue();
                    if ( (onHour < 0) || (onHour > 24) ) {
                        status1.setText( rb.getString("LightError13") );
                        error = true;
                    }
                }
                catch (Exception e) {
                    status1.setText( rb.getString("LightError14") );
                    error = true;
                }
            }
            if (!error) {
                try {
                    onMin = Integer.valueOf(s.substring(3,5)).intValue();
                    if ( (onMin < 0) || (onMin > 59) ) {
                        status1.setText( rb.getString("LightError13") );
                        error = true;
                    }
                }
                catch (Exception e) {
                    status1.setText( rb.getString("LightError14") );
                    error = true;
                }
            }
            s = field2a.getText();
            if ( (s.length() != 5) || (s.charAt(2) != ':') ) {
                status1.setText( rb.getString("LightError12") );
                error = true;
            }
            if (!error) {
                try {
                    offHour = Integer.valueOf(s.substring(0,2)).intValue();
                    if ( (offHour < 0) || (offHour > 24) ) {
                        status1.setText( rb.getString("LightError13") );
                        error = true;
                    }
                }
                catch (Exception e) {
                    status1.setText( rb.getString("LightError14") );
                    error = true;
                }
            }
            if (!error) {
                try {
                    offMin = Integer.valueOf(s.substring(3,5)).intValue();
                    if ( (offMin < 0) || (offMin > 59) ) {
                        status1.setText( rb.getString("LightError13") );
                        error = true;
                    }
                }
                catch (Exception e) {
                    status1.setText( rb.getString("LightError14") );
                    error = true;
                }
            }
                    
            if (error) {
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return (false);
            }
            g.setFastClockControlSchedule(onHour,onMin,offHour,offMin);
        }
        else if (turnoutStatusControl.equals(typeBox.getSelectedItem())) {
            boolean error = false;
            Turnout t = null;
            // Set type of control
            g.setControlType(Light.TURNOUT_STATUS_CONTROL);
            // Get turnout control information
            String turnoutName = field1c.getText().trim();
			if (turnoutName.length() < 1) {
				// valid turnout system name was not entered
				g.setControlType(Light.NO_CONTROL);
			}
			else {
				// Ensure that this Turnout is not already a Light
				if (turnoutName.charAt(1)=='T') {
					// must be a standard format name (not just a number)
					String testSN = turnoutName.substring(0,1)+"L"+
						turnoutName.substring(2,turnoutName.length());
					Light testLight = InstanceManager.lightManagerInstance().
                                        getBySystemName(testSN);
					if (testLight != null) {
						// Requested turnout bit is already assigned to a Light
						status2.setText( rb.getString("LightWarn3")+" "+testSN+"." );
						error = true;
					}
				}
				if (!error) {
					// Requested turnout bit is not assigned to a Light
					t = InstanceManager.turnoutManagerInstance().
                                    getByUserName(turnoutName);
					if (t==null) {
						// not user name, try system name
						t = InstanceManager.turnoutManagerInstance().
									getBySystemName(turnoutName.toUpperCase());
						if (t!=null) {
							// update turnout system name in case it changed
							turnoutName = t.getSystemName();
							field1c.setText(turnoutName);
						}
					}
				}
			}
            // Initialize the requested Turnout State
            int tState = Turnout.CLOSED;
            if ( stateBox.getSelectedItem().equals(InstanceManager.
				turnoutManagerInstance().getThrownText()) ) {
                tState = Turnout.THROWN;
            }
            g.setControlTurnout(turnoutName);
            g.setControlTurnoutState(tState);
            if (t==null) {
                status1.setText( rb.getString("LightWarn2") );
                if (!error) {
                    status2.setText( rb.getString("LightEditInst") );
                }
                status2.setVisible(true);
                return (false);
            }
        }
        else if (timedOnControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.TIMED_ON_CONTROL);
            // Get trigger sensor control information
			Sensor s = null;          
			String triggerSensorName = field1d.getText();
			if (triggerSensorName.length() < 1) {
				// Trigger sensor not entered, or invalidly entered
				g.setControlType(Light.NO_CONTROL);
			}
			else {
				// name entered, try user name first
				s = InstanceManager.sensorManagerInstance().
                            getByUserName(triggerSensorName);
				if (s==null) {
					// not user name, try system name
					s = InstanceManager.sensorManagerInstance().
								getBySystemName(triggerSensorName);
					if (s!=null) {
						// update sensor system name in case it changed
						triggerSensorName = s.getSystemName();
						field1d.setText(triggerSensorName);
					}
				}
			}
            g.setControlTimedOnSensor(triggerSensorName);
			int dur = 0;       
			try 
			{
				dur = Integer.parseInt(field2b.getText());
			}
			catch (Exception e)
			{
				if (s!=null) {
					status1.setText(rb.getString("LightWarn9") );
					status2.setText( rb.getString("LightEditInst") );
					status2.setVisible(true);
					return (false);
				}
			}
			g.setTimedOnDuration(dur);
            if (s==null) {
                status1.setText( rb.getString("LightWarn8") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return (false);
            }
        }
        else if (noControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.NO_CONTROL);
		}
        else {
            log.error("Unexpected control type: "+typeBox.getSelectedItem());
        }
        return (true);
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
     * Responds to the Cancel button
     */
    void cancelPressed(ActionEvent e) {
        status1.setText( rb.getString("LightCreateInst") );
        status2.setText( rb.getString("LightEditInst") );
        status2.setVisible(true);
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        systemName.setVisible(true);
        // reactivate the light
        curLight.activateLight();
		inEditMode = false;
    }
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LightTableAction.class.getName());
}
/* @(#)LightTableAction.java */

// TurnoutTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.Sensor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;

/**
 * Swing action to create and register a
 * TurnoutTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2004
 * @version     $Revision: 1.17 $
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
    		public int getColumnCount( ){ return NUMCOLUMN+4;}
    		
    		public String getColumnName(int col) {
    			if (col==KNOWNCOL) return "Feedback";
    			else if (col==MODECOL) return "Mode";
    			else if (col==SENSOR1COL) return "Sensor 1";
    			else if (col==SENSOR2COL) return "Sensor 2";
    			
    			else if (col==VALUECOL) return "Cmd";  // override default title
    			
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==KNOWNCOL) return String.class;
    			else if (col==MODECOL) return JComboBox.class;
    			else if (col==SENSOR1COL) return String.class;
    			else if (col==SENSOR2COL) return String.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==KNOWNCOL) return new JTextField(6).getPreferredSize().width;
    			else if (col==MODECOL) return new JTextField(10).getPreferredSize().width;
    			else if (col==SENSOR1COL) return new JTextField(5).getPreferredSize().width;
    			else if (col==SENSOR2COL) return new JTextField(5).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==KNOWNCOL) return false;
    			else if (col==MODECOL) return true;
    			else if (col==SENSOR1COL) return true;
    			else if (col==SENSOR2COL) return true;
    			else return super.isCellEditable(row,col);
			}    		

    		public Object getValueAt(int row, int col) {
    			if (col==KNOWNCOL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                    if (t.getKnownState()==Turnout.CLOSED) return "Closed";
                    if (t.getKnownState()==Turnout.THROWN) return "Thrown";
                    if (t.getKnownState()==Turnout.INCONSISTENT) return "Inconsistent";
                    else return "Unknown";
    			} else if (col==MODECOL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
					JComboBox c = new JComboBox(t.getValidFeedbackNames());
					c.setSelectedItem(t.getFeedbackModeName());
					return c;
    			} else if (col==SENSOR1COL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                    Sensor s = t.getFirstSensor();
                    if (s!=null) return s.getSystemName();
                    else return "";
    			} else if (col==SENSOR2COL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                    Sensor s = t.getSecondSensor();
                    if (s!=null) return s.getSystemName();
                    else return "";
    			} else return super.getValueAt(row, col);
			}    		
			
    		public void setValueAt(Object value, int row, int col) {
    			if (col==MODECOL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                    t.setFeedbackMode((String)((JComboBox)value).getSelectedItem());
    			} else if (col==SENSOR1COL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                    String sname = (String)value;
                    Sensor s;
                    if (!sname.equals("")) s = InstanceManager.sensorManagerInstance().provideSensor((String)value);
                    else s = null;
                    t.provideFirstFeedbackSensor(s);
    			} else if (col==SENSOR2COL) {
    				String name = (String)sysNameList.get(row);
    				Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                    String sname = (String)value;
                    Sensor s;
                    if (!sname.equals("")) s = InstanceManager.sensorManagerInstance().provideSensor((String)value);
                    else s = null;
                    t.provideSecondFeedbackSensor(s);
    			} else super.setValueAt(value, row, col);
    		}

            public String getValue(String name) {
                int val = InstanceManager.turnoutManagerInstance().getBySystemName(name).getCommandedState();
                switch (val) {
                case Turnout.CLOSED: return rbean.getString("TurnoutStateClosed");
                case Turnout.THROWN: return rbean.getString("TurnoutStateThrown");
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
                return new JButton("Thrown");
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

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        // Test if bit already in use as a light
        String sName = sysName.getText();
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

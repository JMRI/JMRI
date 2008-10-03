// SignalHeadTableAction.java

// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under
// the terms of version 2 of the GNU General Public License as published
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
//
// JMRI is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.DoubleTurnoutSignalHead;
import jmri.TripleTurnoutSignalHead;
import jmri.Turnout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JSeparator;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003,2006,2007, 2008
 * @author	Petr Koud'a     Copyright (C) 2007
 * @version     $Revision: 1.29 $
 */

public class SignalHeadTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public SignalHeadTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (jmri.InstanceManager.signalHeadManagerInstance()==null) {
            setEnabled(false);
        }
    }
    public SignalHeadTableAction() { this("Signal Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of SignalHeads
     */
    void createModel() {
        m = new BeanTableDataModel() {
		    static public final int LITCOL = NUMCOLUMN;
		    static public final int HELDCOL = LITCOL+1;
			static public final int EDITCOL = HELDCOL+1;
    		public int getColumnCount( ){ return NUMCOLUMN+3;}
    		public String getColumnName(int col) {
    			if (col==LITCOL) return rb.getString("ColumnHeadLit");
    			else if (col==HELDCOL) return rb.getString("ColumnHeadHeld");
				else if (col==EDITCOL) return ""; // no heading on "Edit"
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==LITCOL) return Boolean.class;
    			else if (col==HELDCOL) return Boolean.class;
				else if (col==EDITCOL) return JButton.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==LITCOL) return new JTextField(4).getPreferredSize().width;
    			else if (col==HELDCOL) return new JTextField(4).getPreferredSize().width;
    			else if (col==EDITCOL) return new JTextField(7).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==LITCOL) return true;
    			else if (col==HELDCOL) return true;
				else if (col==EDITCOL) return true;
    			else return super.isCellEditable(row,col);
			}
    		public Object getValueAt(int row, int col) {
    		    String name = (String)sysNameList.get(row);
                SignalHead s = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
                if (s==null) return new Boolean(false); // if due to race condition, the device is going away
    			if (col==LITCOL) {
    				boolean val = s.getLit();
					return new Boolean(val);
    			}
    			else if (col==HELDCOL) {
    				boolean val = s.getHeld();
					return new Boolean(val);
    			}
				else if (col==EDITCOL) return rb.getString("ButtonEdit");
				else return super.getValueAt(row, col);
			}
    		public void setValueAt(Object value, int row, int col) {
    			String name = (String)sysNameList.get(row);
                SignalHead s = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
                if (s==null) return;  // device is going away anyway
    			if (col==LITCOL) {
    				boolean b = ((Boolean)value).booleanValue();
    				s.setLit(b);
    			}
    			else if (col==HELDCOL) {
    				boolean b = ((Boolean)value).booleanValue();
    				s.setHeld(b);
				}
				else if (col==EDITCOL) {
					// button clicked - edit
					editSignal(row);
    			}
    			else super.setValueAt(value, row, col);
    		}

            public String getValue(String name) {
                SignalHead s = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
                if (s==null) return "<lost>"; // if due to race condition, the device is going away
                int val = s.getAppearance();
                switch (val) {
                case SignalHead.RED: return rbean.getString("SignalHeadStateRed");
                case SignalHead.YELLOW: return rbean.getString("SignalHeadStateYellow");
                case SignalHead.GREEN: return rbean.getString("SignalHeadStateGreen");
                case SignalHead.FLASHRED: return rbean.getString("SignalHeadStateFlashingRed");
                case SignalHead.FLASHYELLOW: return rbean.getString("SignalHeadStateFlashingYellow");
                case SignalHead.FLASHGREEN: return rbean.getString("SignalHeadStateFlashingGreen");
                case SignalHead.DARK: return rbean.getString("SignalHeadStateDark");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.signalHeadManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.signalHeadManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.signalHeadManagerInstance().getByUserName(name);}
            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead)t).getAppearance();
                int newState;
                switch (oldState) {
                case SignalHead.RED: newState = SignalHead.YELLOW; break;
                case SignalHead.YELLOW: newState = SignalHead.GREEN; break;
                case SignalHead.GREEN: newState = SignalHead.FLASHRED; break;
                case SignalHead.FLASHRED: newState = SignalHead.FLASHYELLOW; break;
                case SignalHead.FLASHYELLOW: newState = SignalHead.FLASHGREEN; break;
                case SignalHead.FLASHGREEN: newState = SignalHead.DARK; break;
                case SignalHead.DARK: newState = SignalHead.RED; break;
                default: newState = SignalHead.DARK; this.log.warn("Unexpected state "+oldState+" becomes DARK");break;
                }
               ((SignalHead)t).setAppearance(newState);
            }
            public JButton configureButton() {
                return new JButton(rbean.getString("SignalHeadStateYellow"));
            }
            public boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
		        if (e.getPropertyName().indexOf("Lit")>=0 || e.getPropertyName().indexOf("Held")>=0) return true;
                else return super.matchPropertyName(e);
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSignalTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalTable";
    }

    String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    String[] turnoutStates = new String[]{stateClosed, stateThrown};
    int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

    JmriJFrame addFrame = null;
    JComboBox typeBox;
    JTextField name = new JTextField(5);
    JTextField to1 = new JTextField(5);
    JTextField to2 = new JTextField(5);
    JTextField to3 = new JTextField(5);
    JTextField to4 = new JTextField(5);
    JTextField to5 = new JTextField(5);
    JTextField to6 = new JTextField(5);
    JTextField to7 = new JTextField(5);
    JLabel nameLabel = new JLabel("");
    JLabel v1Label = new JLabel("");
    JLabel v2Label = new JLabel("");
    JLabel v3Label = new JLabel("");
    JLabel v4Label = new JLabel("");
    JLabel v5Label = new JLabel("");
    JLabel v6Label = new JLabel("");
    JLabel v7Label = new JLabel("");
    JComboBox s1Box = new JComboBox(turnoutStates);
    JComboBox s2Box = new JComboBox(turnoutStates);
    JComboBox s3Box = new JComboBox(turnoutStates);
    JComboBox s4Box = new JComboBox(turnoutStates);
    JComboBox s5Box = new JComboBox(turnoutStates);
    JComboBox s6Box = new JComboBox(turnoutStates);
    JComboBox s7Box = new JComboBox(turnoutStates);

    String se8c4Aspect = rb.getString("StringSE8c4aspect");
    String tripleTurnout = rb.getString("StringTripleTurnout");
    String doubleTurnout = rb.getString("StringDoubleTurnout");
    String virtualHead = rb.getString("StringVirtual");
    String grapevine = rb.getString("StringGrapevine");
    String lsDec = rb.getString("StringLsDec");
    String dccSignalDecoder = rb.getString("StringDccSigDec");

    int turnoutStateFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutStateValues, turnoutStates);

        if (result<0) {
            log.warn("unexpected mode string in turnoutMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
	void setTurnoutStateInBox (JComboBox box, int state, int[] iTurnoutStates) {
		if (state==iTurnoutStates[0]) box.setSelectedIndex(0);
		else if (state==iTurnoutStates[1]) box.setSelectedIndex(1);
		else log.error("unexpected  turnout state value: "+state);
	}		

    /**
     * Provide GUI for adding a new SignalHead.
     * <P>
     * Because there are multiple options,
     * each of which requires different inputs,
     * we directly manipulate which parts of the
     * GUI are displayed when the selected type is
     * changed.
     */
    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddSignal"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            addFrame.getContentPane().add(typeBox = new JComboBox(new String[]{
                tripleTurnout, doubleTurnout, virtualHead,
                se8c4Aspect, lsDec, dccSignalDecoder
            }));
            if (jmri.jmrix.grapevine.ActiveFlag.isActive()) typeBox.addItem(grapevine);
            typeBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    typeChanged();
                }
            });
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(nameLabel);
            p.add(name);
            addFrame.getContentPane().add(p);

            // create seven boxes for input information, and put into pane

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v1Label);
            p.add(to1);
            p.add(s1Box);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v2Label);
            p.add(to2);
            p.add(s2Box);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v3Label);
            p.add(to3);
            p.add(s3Box);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v4Label);
            p.add(to4);
            p.add(s4Box);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v5Label);
            p.add(to5);
            p.add(s5Box);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v6Label);
            p.add(to6);
            p.add(s6Box);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v7Label);
            p.add(to7);
            p.add(s7Box);
            addFrame.getContentPane().add(p);
            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        typeBox.setSelectedIndex(1);  // force GUI status consistent
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void typeChanged() {
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            s2Box.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            v4Label.setVisible(false);
            to4.setVisible(false);
            s4Box.setVisible(false);
            v5Label.setVisible(false);
            to5.setVisible(false);
            s5Box.setVisible(false);
            v6Label.setVisible(false);
            to6.setVisible(false);
            s6Box.setVisible(false);
            v7Label.setVisible(false);
            to7.setVisible(false);
            s7Box.setVisible(false);

        } else if (grapevine.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setText(rb.getString("LabelUserName"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            s2Box.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            v4Label.setVisible(false);
            to4.setVisible(false);
            s4Box.setVisible(false);
            v5Label.setVisible(false);
            to5.setVisible(false);
            s5Box.setVisible(false);
            v6Label.setVisible(false);
            to6.setVisible(false);
            s6Box.setVisible(false);
            v7Label.setVisible(false);
            to7.setVisible(false);
            s7Box.setVisible(false);

        } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            v2Label.setVisible(true);
            s2Box.setVisible(false);
            to2.setVisible(true);
            v3Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v3Label.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(false);
            v4Label.setVisible(false);
            to4.setVisible(false);
            s4Box.setVisible(false);
            v5Label.setVisible(false);
            to5.setVisible(false);
            s5Box.setVisible(false);
            v6Label.setVisible(false);
            to6.setVisible(false);
            s6Box.setVisible(false);
            v7Label.setVisible(false);
            to7.setVisible(false);
            s7Box.setVisible(false);

        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v2Label.setVisible(true);
            to2.setVisible(true);
            s2Box.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            v4Label.setVisible(false);
            to4.setVisible(false);
            s4Box.setVisible(false);
            v5Label.setVisible(false);
            to5.setVisible(false);
            s5Box.setVisible(false);
            v6Label.setVisible(false);
            to6.setVisible(false);
            s6Box.setVisible(false);
            v7Label.setVisible(false);
            to7.setVisible(false);
            s7Box.setVisible(false);

        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setVisible(false);
            to1.setVisible(false);
            s1Box.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            s2Box.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            v4Label.setVisible(false);
            to4.setVisible(false);
            s4Box.setVisible(false);
            v5Label.setVisible(false);
            to5.setVisible(false);
            s5Box.setVisible(false);
            v6Label.setVisible(false);
            to6.setVisible(false);
            s6Box.setVisible(false);
            v7Label.setVisible(false);
            to7.setVisible(false);
            s7Box.setVisible(false);

        } else if (lsDec.equals(typeBox.getSelectedItem())) {
            nameLabel.setText(rb.getString("LabelSystemName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(true);
            v2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            v2Label.setVisible(true);
            to2.setVisible(true);
            s2Box.setVisible(true);
            v3Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v3Label.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(true);
            v4Label.setText(rb.getString("LabelFlashGreenTurnoutNumber"));
            v4Label.setVisible(true);
            to4.setVisible(true);
            s4Box.setVisible(true);
            v5Label.setText(rb.getString("LabelFlashYellowTurnoutNumber"));
            v5Label.setVisible(true);
            to5.setVisible(true);
            s5Box.setVisible(true);
            v6Label.setText(rb.getString("LabelFlashRedTurnoutNumber"));
            v6Label.setVisible(true);
            to6.setVisible(true);
            s6Box.setVisible(true);
            v7Label.setText(rb.getString("LabelDarkTurnoutNumber"));
            v7Label.setVisible(true);
            to7.setVisible(true);
            s7Box.setVisible(true);
          } else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
              nameLabel.setText(rb.getString("LabelSystemName"));
              v1Label.setVisible(false);
              to1.setVisible(false);
              s1Box.setVisible(false);
              v2Label.setVisible(false);
              to2.setVisible(false);
              s2Box.setVisible(false);
              v3Label.setVisible(false);
              to3.setVisible(false);
              s3Box.setVisible(false);
              v4Label.setVisible(false);
              to4.setVisible(false);
              s4Box.setVisible(false);
              v5Label.setVisible(false);
              to5.setVisible(false);
              s5Box.setVisible(false);
              v6Label.setVisible(false);
              to6.setVisible(false);
              s6Box.setVisible(false);
              v7Label.setVisible(false);
              to7.setVisible(false);
              s7Box.setVisible(false);

        } else log.error("Unexpected type in typeChanged: "+typeBox.getSelectedItem());

        // make sure size OK
        addFrame.pack();
    }
	
	boolean checkBeforeCreating(String sysName) {
		String sName = sysName.toUpperCase();
		if ( (sName.length()<3) || (!sName.substring(1,2).equals("H")) ) {
			String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("InvalidSignalSystemName"), new Object[] { sName });
			JOptionPane.showMessageDialog(addFrame, msg,
					AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
			return false;			
		}	
		// check for pre-existing signal head with same system name
		SignalHead s = InstanceManager.signalHeadManagerInstance().getBySystemName(sName);
		// return true if signal head does not exist
		if (s==null) return true;
		// inform the user if signal head already exists, and return false so creation can be bypassed
		log.warn("Attempt to create signal with duplicate system name "+sName);
		String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("DuplicateSignalSystemName"), new Object[] { sName });
		JOptionPane.showMessageDialog(addFrame, msg,
				AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	void addTurnoutMessage(String s1, String s2) {
		log.warn("Could not provide turnout "+s2);
		String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("AddNoTurnout"), new Object[] { s1, s2 });
		JOptionPane.showMessageDialog(addFrame, msg,
				AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
	}

    void okPressed(ActionEvent e) {
        SignalHead s;
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            // the turnout field can hold either a NNN number or a system name
            String num = to1.getText();
            int number;
            if (num.substring(0,2).equals("LT"))
                number = Integer.parseInt(num.substring(2,num.length()));
            else if (num.substring(0,2).equals("lt"))
                number = Integer.parseInt(num.substring(2,num.length()));
            else
                number = Integer.parseInt(num);
			if (checkBeforeCreating("LH"+number)) {
				s = new jmri.jmrix.loconet.SE8cSignalHead(number,name.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (grapevine.equals(typeBox.getSelectedItem())) {
            // the turnout field must hold a GH system name
            if (!name.getText().substring(0,2).toUpperCase().equals("GH")) {
                log.warn("skipping creation of signal, "+name.getText()+" does not start with GH");
				String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("GrapevineSkippingCreation"), new Object[] { name.getText() });
				JOptionPane.showMessageDialog(addFrame, msg,
					AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);				
                return;
            }
			if (checkBeforeCreating(name.getText())) {
				// check user name, if one was entered
				if (to1.getText().length()>=1) {
					NamedBean nb = InstanceManager.signalHeadManagerInstance().getByUserName(to1.getText());
					if (nb!=null) {
						//user name already in use
						String msg = java.text.MessageFormat.format(AbstractTableAction.rb
								.getString("WarningUserName"), new Object[] { (to1.getText()) });
						JOptionPane.showMessageDialog(addFrame, msg,
								AbstractTableAction.rb.getString("WarningTitle"),JOptionPane.ERROR_MESSAGE);
						return;
					}
				}	
				s = new jmri.jmrix.grapevine.SerialSignalHead(name.getText(),to1.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(to3.getText());
            if (t1==null) addTurnoutMessage(v1Label.getText(), to1.getText());
            if (t2==null) addTurnoutMessage(v2Label.getText(), to2.getText());
            if (t3==null) addTurnoutMessage(v3Label.getText(), to3.getText());
            if (t3==null || t2==null || t1==null) {
                log.warn("skipping creation of signal "+name.getText()+" due to error");
                return;
            }
			if (checkBeforeCreating(name.getText())) {			
				s = new jmri.TripleTurnoutSignalHead(name.getText(),t1, t2, t3);
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            if (t1==null) addTurnoutMessage(v1Label.getText(), to1.getText());
            if (t2==null) addTurnoutMessage(v2Label.getText(), to2.getText());
            if (t2==null || t1==null) {
                log.warn("skipping creation of signal "+name.getText()+" due to error");
                return;
            }
			if (checkBeforeCreating(name.getText())) {			
				s = new jmri.DoubleTurnoutSignalHead(name.getText(),t1, t2);
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            if (checkBeforeCreating(name.getText())) {
				s = new jmri.VirtualSignalHead(name.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (lsDec.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(to3.getText());
            Turnout t4 = InstanceManager.turnoutManagerInstance().provideTurnout(to4.getText());
            Turnout t5 = InstanceManager.turnoutManagerInstance().provideTurnout(to5.getText());
            Turnout t6 = InstanceManager.turnoutManagerInstance().provideTurnout(to6.getText());
            Turnout t7 = InstanceManager.turnoutManagerInstance().provideTurnout(to7.getText());
            int s1 = turnoutStateFromBox(s1Box);
            int s2 = turnoutStateFromBox(s2Box);
            int s3 = turnoutStateFromBox(s3Box);
            int s4 = turnoutStateFromBox(s4Box);
            int s5 = turnoutStateFromBox(s5Box);
            int s6 = turnoutStateFromBox(s6Box);
            int s7 = turnoutStateFromBox(s7Box);
            if (t1==null) addTurnoutMessage(v1Label.getText(), to1.getText());
            if (t2==null) addTurnoutMessage(v2Label.getText(), to2.getText());
            if (t3==null) addTurnoutMessage(v3Label.getText(), to3.getText());
            if (t4==null) addTurnoutMessage(v4Label.getText(), to4.getText());
            if (t5==null) addTurnoutMessage(v5Label.getText(), to5.getText());
            if (t6==null) addTurnoutMessage(v6Label.getText(), to6.getText());
            if (t7==null) addTurnoutMessage(v7Label.getText(), to7.getText());
            if (t7==null || t6==null || t5==null || t4==null || t3==null || t2==null || t1==null) {
                log.warn("skipping creation of signal "+name.getText()+" due to error");
                return;
            }
            if (checkBeforeCreating(name.getText())) {
				s = new jmri.LsDecSignalHead(name.getText(), t1, s1, t2, s2, t3, s3, t4, s4, t5, s5, t6, s6, t7, s7);
				InstanceManager.signalHeadManagerInstance().register(s);
			}
		} else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
			String addrStr = name.getText() ;
			int number ;
			if (checkBeforeCreating(name.getText())) {            
				s = new jmri.DccSignalHead(name.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else log.error("Unexpected type: "+typeBox.getSelectedItem());
    }
	
	// variables for edit of signal heads
	boolean editingHead = false;
	String editSysName = "";
	JmriJFrame editFrame = null;
	JLabel signalType = new JLabel("XXXX");
	SignalHead curS = null;
	String className = "";
    JTextField eName = new JTextField(5);
    JTextField eto1 = new JTextField(5);
    JTextField eto2 = new JTextField(5);
    JTextField eto3 = new JTextField(5);
    JTextField eto4 = new JTextField(5);
    JTextField eto5 = new JTextField(5);
    JTextField eto6 = new JTextField(5);
    JTextField eto7 = new JTextField(5);
    JLabel eNameLabel = new JLabel("");
	JLabel eSysNameLabel = new JLabel ("");
	JLabel eNumLabel = new JLabel ("");
    JLabel ev1Label = new JLabel("");
    JLabel ev2Label = new JLabel("");
    JLabel ev3Label = new JLabel("");
    JLabel ev4Label = new JLabel("");
    JLabel ev5Label = new JLabel("");
    JLabel ev6Label = new JLabel("");
    JLabel ev7Label = new JLabel("");
    JComboBox es1Box = new JComboBox(turnoutStates);
    JComboBox es2Box = new JComboBox(turnoutStates);
    JComboBox es3Box = new JComboBox(turnoutStates);
    JComboBox es4Box = new JComboBox(turnoutStates);
    JComboBox es5Box = new JComboBox(turnoutStates);
    JComboBox es6Box = new JComboBox(turnoutStates);
    JComboBox es7Box = new JComboBox(turnoutStates);
	
	void editSignal(int row) {
		String eSName = (String)m.getValueAt(row,BeanTableDataModel.SYSNAMECOL);
		if (editingHead) {
			if (eSName.equals(editSysName)) {
				editFrame.setVisible(true);
			}
			else {
				log.error("Attempt to edit two signal heads at the same time-"+editSysName+"-and-"+eSName+"-");
				String msg = java.text.MessageFormat.format(AbstractTableAction.rb
								.getString("WarningEdit"), new Object[] { editSysName, eSName });
				JOptionPane.showMessageDialog(editFrame, msg,
							AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
				editFrame.setVisible(true);
				return;
			}
		}
		// not currently editing a signal head

		editSysName = eSName;
		editingHead = true;
		curS = InstanceManager.signalHeadManagerInstance().getBySystemName(editSysName);
		if (editFrame == null) {
			// set up a new edit window
            editFrame = new JmriJFrame(rb.getString("TitleEditSignal"));
            editFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);
            editFrame.getContentPane().setLayout(new BoxLayout(editFrame.getContentPane(), BoxLayout.Y_AXIS));
			JPanel p;
			p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(signalType);
            editFrame.getContentPane().add(p);
			editFrame.getContentPane().add(new JSeparator());
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(eNameLabel);
            p.add(eName);
			p.add(eSysNameLabel);
            editFrame.getContentPane().add(p);
            // create seven boxes for input information, and put into pane
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev1Label);
            p.add(eto1);
			p.add(eNumLabel);
            p.add(es1Box);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev2Label);
            p.add(eto2);
            p.add(es2Box);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev3Label);
            p.add(eto3);
            p.add(es3Box);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev4Label);
            p.add(eto4);
            p.add(es4Box);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev5Label);
            p.add(eto5);
            p.add(es5Box);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev6Label);
            p.add(eto6);
            p.add(es6Box);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev7Label);
            p.add(eto7);
            p.add(es7Box);
            editFrame.getContentPane().add(p);
			editFrame.getContentPane().add(new JSeparator());
			// add buttons
            p = new JPanel(); p.setLayout(new FlowLayout());			
			JButton cancel;
			p.add(cancel = new JButton(rb.getString("ButtonCancel")));
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelPressed(e);
				}
			});
			JButton update;
			p.add(update = new JButton(rb.getString("ButtonUpdate")));
			update.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updatePressed(e);
				}
			});
            editFrame.getContentPane().add(p);
			editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					cancelPressed(null);
				}
			});
			
		}
		// default the seven optional items to hidden, and system name to visible
		eName.setVisible(false);
		eSysNameLabel.setVisible(true);
		ev1Label.setVisible(false);
		eto1.setVisible(false);
		eNumLabel.setVisible(false);
		es1Box.setVisible(false);
		ev2Label.setVisible(false);
		eto2.setVisible(false);
		es2Box.setVisible(false);
		ev3Label.setVisible(false);
		eto3.setVisible(false);
		es3Box.setVisible(false);
		ev4Label.setVisible(false);
		eto4.setVisible(false);
		es4Box.setVisible(false);
		ev5Label.setVisible(false);
		eto5.setVisible(false);
		es5Box.setVisible(false);
		ev6Label.setVisible(false);
		eto6.setVisible(false);
		es6Box.setVisible(false);
		ev7Label.setVisible(false);
		eto7.setVisible(false);
		es7Box.setVisible(false);		
		// determine class name of signal head and initialize this class of signal
		className = curS.getClass().getName();
		if (className.equals("jmri.TripleTurnoutSignalHead")) {
			signalType.setText(tripleTurnout);
            eNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((TripleTurnoutSignalHead)curS).getGreen().getSystemName());
            ev2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((TripleTurnoutSignalHead)curS).getYellow().getSystemName());
            ev3Label.setText(rb.getString("LabelRedTurnoutNumber"));
			ev3Label.setVisible(true);
            eto3.setVisible(true);
			eto3.setText(((TripleTurnoutSignalHead)curS).getRed().getSystemName());
		}
		else if (className.equals("jmri.DoubleTurnoutSignalHead")) {
			signalType.setText(doubleTurnout);
            eNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((DoubleTurnoutSignalHead)curS).getGreen().getSystemName());
            ev2Label.setText(rb.getString("LabelRedTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((DoubleTurnoutSignalHead)curS).getRed().getSystemName());
 		}
		else if (className.equals("jmri.VirtualSignalHead")) {
			signalType.setText(virtualHead);
			eNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
		}
		else if (className.equals("jmri.LsDecSignalHead")) {
			signalType.setText(lsDec);
            eNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((jmri.LsDecSignalHead)curS).getGreen().getSystemName());
            es1Box.setVisible(true);
			setTurnoutStateInBox(es1Box, ((jmri.LsDecSignalHead)curS).getGreenState(), turnoutStateValues);
            ev2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((jmri.LsDecSignalHead)curS).getYellow().getSystemName());
            es2Box.setVisible(true);
			setTurnoutStateInBox(es2Box, ((jmri.LsDecSignalHead)curS).getYellowState(), turnoutStateValues);
            ev3Label.setText(rb.getString("LabelRedTurnoutNumber"));
			ev3Label.setVisible(true);
            eto3.setVisible(true);
			eto3.setText(((jmri.LsDecSignalHead)curS).getRed().getSystemName());
            es3Box.setVisible(true);
			setTurnoutStateInBox(es3Box, ((jmri.LsDecSignalHead)curS).getRedState(), turnoutStateValues);
            ev4Label.setText(rb.getString("LabelFlashGreenTurnoutNumber"));
            ev4Label.setVisible(true);
            eto4.setVisible(true);
			eto4.setText(((jmri.LsDecSignalHead)curS).getFlashGreen().getSystemName());
            es4Box.setVisible(true);
			setTurnoutStateInBox(es4Box, ((jmri.LsDecSignalHead)curS).getFlashGreenState(), turnoutStateValues);
            ev5Label.setText(rb.getString("LabelFlashYellowTurnoutNumber"));
            ev5Label.setVisible(true);
            eto5.setVisible(true);
			eto5.setText(((jmri.LsDecSignalHead)curS).getFlashYellow().getSystemName());
            es5Box.setVisible(true);
			setTurnoutStateInBox(es5Box, ((jmri.LsDecSignalHead)curS).getFlashYellowState(), turnoutStateValues);
			ev6Label.setText(rb.getString("LabelFlashRedTurnoutNumber"));
            ev6Label.setVisible(true);
            eto6.setVisible(true);
 			eto6.setText(((jmri.LsDecSignalHead)curS).getFlashRed().getSystemName());
			es6Box.setVisible(true);
			setTurnoutStateInBox(es6Box, ((jmri.LsDecSignalHead)curS).getFlashRedState(), turnoutStateValues);
			ev7Label.setText(rb.getString("LabelDarkTurnoutNumber"));
            ev7Label.setVisible(true);
            eto7.setVisible(true);
			eto7.setText(((jmri.LsDecSignalHead)curS).getDark().getSystemName());
            es7Box.setVisible(true);
			setTurnoutStateInBox(es7Box, ((jmri.LsDecSignalHead)curS).getDarkState(), turnoutStateValues);
		}
		else if (className.equals("jmri.jmrix.loconet.SE8cSignalHead")) {
			signalType.setText(se8c4Aspect);
            eNameLabel.setText(rb.getString("LabelUserName"));
			eSysNameLabel.setVisible(false);
			eName.setVisible(true);
			eName.setText(curS.getUserName());
            ev1Label.setText(rb.getString("LabelTurnoutNumber"));
            ev1Label.setVisible(true);
            eNumLabel.setVisible(true);
			eNumLabel.setText(" "+((jmri.jmrix.loconet.SE8cSignalHead)curS).getNumber());
		}
		else if (className.equals("jmri.jmrix.grapevine.SerialSignalHead")) {
			signalType.setText(grapevine);
			eNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            ev1Label.setText(rb.getString("LabelUserName"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(curS.getUserName());
		}
		else if (className.equals("jmri.DccSignalHead")) {
			signalType.setText(dccSignalDecoder);
            eNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
		}			
		// finish up
		editFrame.pack();
		editFrame.setVisible(true);
	}
			
	void cancelPressed(ActionEvent e) {
		editFrame.setVisible(false);
		editingHead = false;
	}
	
	void updatePressed(ActionEvent e) {
		// update according to class of signal head
		if (className.equals("jmri.TripleTurnoutSignalHead")) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(eto3.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			else ((TripleTurnoutSignalHead)curS).setGreen(t1);
            if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((TripleTurnoutSignalHead)curS).setYellow(t2);
            if (t3==null) {
				noTurnoutMessage(ev3Label.getText(), eto3.getText());
				return;
			}
			else ((TripleTurnoutSignalHead)curS).setRed(t3);
		}
		else if (className.equals("jmri.DoubleTurnoutSignalHead")) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto2.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			else ((DoubleTurnoutSignalHead)curS).setGreen(t1);
            if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((DoubleTurnoutSignalHead)curS).setRed(t2);
		}
		else if (className.equals("jmri.LsDecSignalHead")) {
			Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(eto3.getText());
            Turnout t4 = InstanceManager.turnoutManagerInstance().provideTurnout(eto4.getText());
            Turnout t5 = InstanceManager.turnoutManagerInstance().provideTurnout(eto5.getText());
            Turnout t6 = InstanceManager.turnoutManagerInstance().provideTurnout(eto6.getText());
            Turnout t7 = InstanceManager.turnoutManagerInstance().provideTurnout(eto7.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setGreen(t1);
			if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setYellow(t2);
            if (t3==null) {
				noTurnoutMessage(ev3Label.getText(), eto3.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setRed(t3);
            if (t4==null) {
				noTurnoutMessage(ev4Label.getText(), eto4.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setFlashGreen(t4);
            if (t5==null) {
				noTurnoutMessage(ev5Label.getText(), eto5.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setFlashYellow(t5);
            if (t6==null) {
				noTurnoutMessage(ev6Label.getText(), eto6.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setFlashRed(t6);
            if (t7==null) {
				noTurnoutMessage(ev7Label.getText(), eto7.getText());
				return;
			}
			else ((jmri.LsDecSignalHead)curS).setDark(t7);
			((jmri.LsDecSignalHead)curS).setGreenState(turnoutStateFromBox(es1Box));
			((jmri.LsDecSignalHead)curS).setYellowState(turnoutStateFromBox(es2Box));
			((jmri.LsDecSignalHead)curS).setRedState(turnoutStateFromBox(es3Box));
			((jmri.LsDecSignalHead)curS).setFlashGreenState(turnoutStateFromBox(es4Box));
			((jmri.LsDecSignalHead)curS).setFlashYellowState(turnoutStateFromBox(es5Box));
			((jmri.LsDecSignalHead)curS).setFlashRedState(turnoutStateFromBox(es6Box));
			((jmri.LsDecSignalHead)curS).setDarkState(turnoutStateFromBox(es7Box));    
		}
		else if (className.equals("jmri.jmrix.loconet.SE8cSignalHead")) {
			String nam = eName.getText();
			// check if user name changed
			if (!(curS.getUserName().equals(nam)) ) {
				// check for null name
				if (!((nam==null) || (nam==""))) {
					// user name changed, check if new name already exists
					NamedBean nB = InstanceManager.signalHeadManagerInstance().getByUserName(nam);
					if (nB != null) {
						log.error("User name is not unique " + nam);
						String msg = java.text.MessageFormat.format(AbstractTableAction.rb
								.getString("WarningUserName"), new Object[] { ("" + nam) });
						JOptionPane.showMessageDialog(editFrame, msg,
									AbstractTableAction.rb.getString("WarningTitle"),
										JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				curS.setUserName(nam);
			}
		}
		else if (className.equals("jmri.jmrix.grapevine.SerialSignalHead")) {
			String nam = eto1.getText();
			// check if user name changed
			if (!(curS.getUserName().equals(nam)) ) {
				// check for null name
				if (!((nam==null) || (nam==""))) {
					// user name changed, check if new name already exists
					NamedBean nB = InstanceManager.signalHeadManagerInstance().getByUserName(nam);
					if (nB != null) {
						log.error("User name is not unique " + nam);
						String msg = java.text.MessageFormat.format(AbstractTableAction.rb
								.getString("WarningUserName"), new Object[] { ("" + nam) });
						JOptionPane.showMessageDialog(editFrame, msg,
									AbstractTableAction.rb.getString("WarningTitle"),
										JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				curS.setUserName(nam);
			}
		}
		// successful
		editFrame.setVisible(false);
		editingHead = false;
	}
	void noTurnoutMessage(String s1, String s2) {
		log.warn("Could not provide turnout "+s2);
		String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("WarningNoTurnout"), new Object[] { s1, s2 });
		JOptionPane.showMessageDialog(editFrame, msg,
				AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
	}


    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadTableAction.class.getName());
}
/* @(#)SignalHeadTableAction.java */

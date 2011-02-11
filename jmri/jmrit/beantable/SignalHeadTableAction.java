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
import jmri.implementation.SingleTurnoutSignalHead;
import jmri.implementation.DoubleTurnoutSignalHead;
import jmri.implementation.TripleTurnoutSignalHead;
import jmri.implementation.QuadOutputSignalHead;
import jmri.Turnout;

import jmri.util.JmriJFrame;
import jmri.util.NamedBeanHandle;

import jmri.jmrix.acela.AcelaAddress;
import jmri.jmrix.acela.AcelaNode;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JSeparator;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003,2006,2007, 2008, 2009
 * @author	Petr Koud'a     Copyright (C) 2007
 * @version     $Revision: 1.64 $
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
    protected void createModel() {
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
    		public Class<?> getColumnClass(int col) {
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
     			// some error checking
    			if (row >= sysNameList.size()){
    				log.debug("row is greater than name list");
    				return "error";
    			}
    		    String name = sysNameList.get(row);
                SignalHead s = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
                if (s==null) return Boolean.valueOf(false); // if due to race condition, the device is going away
    			if (col==LITCOL) {
    				boolean val = s.getLit();
					return Boolean.valueOf(val);
    			}
    			else if (col==HELDCOL) {
    				boolean val = s.getHeld();
					return Boolean.valueOf(val);
    			}
				else if (col==EDITCOL) return rb.getString("ButtonEdit");
				else return super.getValueAt(row, col);
			}
    		public void setValueAt(Object value, int row, int col) {
    			String name = sysNameList.get(row);
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
                String val = s.getAppearanceName();
                if (val != null) return val;
                else return "Unexpected null value";
            }
            public Manager getManager() { return InstanceManager.signalHeadManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.signalHeadManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.signalHeadManagerInstance().getByUserName(name);}
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"delete"); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "delete", boo); }*/
            protected String getMasterClassName() { return getClassName(); }

            
            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead)t).getAppearance();
                int newState = SignalHead.DARK;
                int[] stateList = ((SignalHead)t).getValidStates();
                for (int i = 0; i < stateList.length; i++) {
                    if (oldState == stateList[i] ) { 
                        if (i < stateList.length-1) {
                            newState = stateList[i+1];
                            break;
                        } else {
                            newState = stateList[0];
                            break;
                        }
                    }
                }
                log.debug("was "+oldState+" becomes "+newState);
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

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleSignalTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalHeadTable";
    }

    final int[] signalStatesValues = new int[]{
        SignalHead.DARK, 
        SignalHead.RED, 
        SignalHead.LUNAR,
        SignalHead.YELLOW,
        SignalHead.GREEN
    };
    
    String[] signalStates = new String[]{
        rbean.getString("SignalHeadStateDark"),
        rbean.getString("SignalHeadStateRed"),
        rbean.getString("SignalHeadStateLunar"),
        rbean.getString("SignalHeadStateYellow"),
        rbean.getString("SignalHeadStateGreen")
    };
    
    String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    String[] turnoutStates = new String[]{stateClosed, stateThrown};
    int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

    String signalheadSingle = rb.getString("StringSignalheadSingle");
    String signalheadDouble = rb.getString("StringSignalheadDouble");
    String signalheadTriple = rb.getString("StringSignalheadTriple");
    String signalheadBiPolar = rb.getString("StringSignalheadBiPolar");
    String signalheadWigwag = rb.getString("StringSignalheadWigwag");
    String[] signalheadTypes = new String[]{signalheadDouble, signalheadTriple, 
                                            signalheadBiPolar, signalheadWigwag};
    int[] signalheadTypeValues = new int[]{AcelaNode.DOUBLE, AcelaNode.TRIPLE,
                                           AcelaNode.BPOLAR, AcelaNode.WIGWAG};
                                           
    String[] ukSignalAspects = new String[]{"2","3","4"};
    String[] ukSignalType = new String[]{"Home", "Distant"};

    JmriJFrame addFrame = null;
    JComboBox typeBox;
    
    // we share input fields across boxes so that 
    // entries in one don't disappear when the user switches
    // to a different type
    
    JTextField systemName = new JTextField(5);
    JTextField userName = new JTextField(10);
    JTextField to1 = new JTextField(5);
    JTextField to2 = new JTextField(5);
    JTextField to3 = new JTextField(5);
    JTextField to4 = new JTextField(5);
    JTextField to5 = new JTextField(5);
    JTextField to6 = new JTextField(5);
    JTextField to7 = new JTextField(5);
    JLabel systemNameLabel = new JLabel("");
    JLabel userNameLabel = new JLabel("");
    JLabel v1Label = new JLabel("");
    JLabel v2Label = new JLabel("");
    JLabel v3Label = new JLabel("");
    JLabel v4Label = new JLabel("");
    JLabel v5Label = new JLabel("");
    JLabel v6Label = new JLabel("");
    JLabel v7Label = new JLabel("");
    JLabel vtLabel = new JLabel("");
    JComboBox s1Box = new JComboBox(turnoutStates);
    JComboBox s2Box = new JComboBox(turnoutStates);
    JComboBox s2aBox = new JComboBox(signalStates);
    JComboBox s3Box = new JComboBox(turnoutStates);
    JComboBox s3aBox = new JComboBox(signalStates);
    JComboBox s4Box = new JComboBox(turnoutStates);
    JComboBox s5Box = new JComboBox(turnoutStates);
    JComboBox s6Box = new JComboBox(turnoutStates);
    JComboBox s7Box = new JComboBox(turnoutStates);
    JComboBox stBox = new JComboBox(signalheadTypes); // Acela signal types
    JComboBox mstBox = new JComboBox(ukSignalType);
    JComboBox msaBox = new JComboBox(ukSignalAspects);

    String acelaAspect = rb.getString("StringAcelaaspect");
    String se8c4Aspect = rb.getString("StringSE8c4aspect");
    String quadOutput = rb.getString("StringQuadOutput");
    String tripleTurnout = rb.getString("StringTripleTurnout");
    String doubleTurnout = rb.getString("StringDoubleTurnout");
    String virtualHead = rb.getString("StringVirtual");
    String grapevine = rb.getString("StringGrapevine");
    String acela = rb.getString("StringAcelaaspect");
    String lsDec = rb.getString("StringLsDec");
    String dccSignalDecoder = rb.getString("StringDccSigDec");
    String mergSignalDriver = rb.getString("StringMerg");
    String singleTurnout = rb.getString("StringSingle");

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
    
    int signalStateFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);

        if (result<0) {
            log.warn("unexpected mode string in signalMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
    
    void setSignalStateInBox (JComboBox box, int state) {
		
        switch(state){
            case SignalHead.DARK : box.setSelectedIndex(0);
                                    break;
            case SignalHead.RED : box.setSelectedIndex(1);
                                    break;
            case SignalHead.LUNAR : box.setSelectedIndex(2);
                                    break;
            case SignalHead.YELLOW : box.setSelectedIndex(3);
                                    break;
            case SignalHead.GREEN : box.setSelectedIndex(4);
                                    break;
            case SignalHead.FLASHRED : box.setSelectedIndex(5);
                                    break;
            case SignalHead.FLASHLUNAR : box.setSelectedIndex(6);
                                    break;
            case SignalHead.FLASHYELLOW : box.setSelectedIndex(7);
                                    break;
            case SignalHead.FLASHGREEN : box.setSelectedIndex(8);
                                    break;
            default : log.error("unexpected Signal state value: "+state);
        }
        
        /*if (state==iSignalStates[0]) box.setSelectedIndex(0);
		else if (state==iSignalStates[1]) box.setSelectedIndex(1);
		else log.error("unexpected  Signal state value: "+state);*/
	}
    
    int signalheadTypeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalheadTypeValues, signalheadTypes);

        if (result<0) {
            log.warn("unexpected mode string in signalhead aspect type: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
	void setSignalheadTypeInBox (JComboBox box, int state, int[] iSignalheadTypes) {
		if (state==iSignalheadTypes[0]) box.setSelectedIndex(0);
		else if (state==iSignalheadTypes[1]) box.setSelectedIndex(1);
		else if (state==iSignalheadTypes[2]) box.setSelectedIndex(2);
		else if (state==iSignalheadTypes[3]) box.setSelectedIndex(3);
		else log.error("unexpected signalhead type value: "+state);
	}		
    
    int ukSignalAspectsFromBox(JComboBox box){
        //String mode = (String)box.getSelectedItem();
        if(box.getSelectedIndex()==0) return 2;
        else if(box.getSelectedIndex()==1) return 3;
        else if(box.getSelectedIndex()==2) return 4;
        else {
            log.warn("unexpected aspect" + box.getSelectedItem());
            throw new IllegalArgumentException();
        }
    }
    
    void setUkSignalAspectsFromBox(JComboBox box, int val){
        if (val==2) box.setSelectedIndex(0);
        else if (val==3) box.setSelectedIndex(1);
        else if (val==4) box.setSelectedIndex(2);
        else log.error("Unexpected Signal Aspect" + val);
    }
    
    String ukSignalTypeFromBox(JComboBox box){
        //String mode = (String)box.getSelectedItem();
        if(box.getSelectedIndex()==0) return "Home";
        else if(box.getSelectedIndex()==1) return "Distant";
        else {
            log.warn("unexpected aspect" + box.getSelectedItem());
            throw new IllegalArgumentException();
        }
    }
    
    void setUkSignalType(JComboBox box, String val){
        if (val.equals(ukSignalType[0])) box.setSelectedIndex(0);
        else if (val.equals(ukSignalType[1])) box.setSelectedIndex(1);
        else log.error("Unexpected Signal Type " + val);
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
    protected void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddSignal"));
            addFrame.setSaveFrameSize(false);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            addFrame.getContentPane().add(typeBox = new JComboBox(new String[]{
                acelaAspect, dccSignalDecoder, doubleTurnout, lsDec, mergSignalDriver, quadOutput, 
                singleTurnout, se8c4Aspect, tripleTurnout, virtualHead
            }));
            if (jmri.jmrix.grapevine.ActiveFlag.isActive()) typeBox.addItem(grapevine);
            typeBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    typeChanged();
                }
            });
            //typeBox.setSelectedIndex(7);
            //typeChanged();
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(systemNameLabel);
            p.add(systemName);
            addFrame.getContentPane().add(p);
            
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            // create seven boxes for input information, and put into pane
            
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v1Label);
            p.add(to1);
            p.add(s1Box);
            p.add(msaBox);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v2Label);
            p.add(to2);
            p.add(s2Box);
            p.add(s2aBox);
            p.add(mstBox);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v3Label);
            p.add(to3);
            p.add(s3Box);
            p.add(s3aBox);
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

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(vtLabel);
            p.add(stBox);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        typeBox.setSelectedIndex(2);  // force GUI status consistent Default set to Double Head
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void typeChanged() {
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            handleSE8cTypeChanged();
        } else if (grapevine.equals(typeBox.getSelectedItem())) {  //Need to see how this works with username
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            userNameLabel.setVisible(true);
            userName.setVisible(true);
            v1Label.setVisible(false);
            to1.setVisible(false);
            s1Box.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);

        } else if (acelaAspect.equals(typeBox.getSelectedItem())) {
            userNameLabel.setText(rb.getString("LabelUserName"));
            userNameLabel.setVisible(true);
            userName.setVisible(true);
            systemNameLabel.setVisible(false);
            systemName.setVisible(false);
            v1Label.setText(rb.getString("LabelSignalheadNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
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
            vtLabel.setText(rb.getString("LabelAspectType"));
            vtLabel.setVisible(true);
            stBox.setVisible(true);
            mstBox.setVisible(false);
            msaBox.setVisible(false);

        } else if (quadOutput.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            v2Label.setVisible(true);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            to2.setVisible(true);
            v3Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v3Label.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
            v4Label.setText(rb.getString("LabelLunarTurnoutNumber"));
            v4Label.setVisible(true);
            to4.setVisible(true);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);
            
        } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            v2Label.setVisible(true);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            to2.setVisible(true);
            v3Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v3Label.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);
            
        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v2Label.setVisible(true);
            to2.setVisible(true);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);
            
        } else if (singleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(false);
            v2Label.setText(rb.getString("LabelTurnoutThrownAppearance"));
            v2Label.setVisible(true);
            to2.setVisible(false);
            s2Box.setVisible(false);
            s2aBox.setVisible(true);
            v3Label.setText(rb.getString("LabelTurnoutClosedAppearance"));
            v3Label.setVisible(true);
            to3.setVisible(false);
            s3aBox.setVisible(true);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setVisible(false);
            to1.setVisible(false);
            s1Box.setVisible(false);
            v2Label.setVisible(false);
            to2.setVisible(false);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            v3Label.setVisible(false);
            to3.setVisible(false);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);
            
        } else if (lsDec.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            v1Label.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(true);
            v2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            v2Label.setVisible(true);
            to2.setVisible(true);
            s2Box.setVisible(true);
            s2aBox.setVisible(false);
            v3Label.setText(rb.getString("LabelRedTurnoutNumber"));
            v3Label.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(true);
            s3aBox.setVisible(false);
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
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            mstBox.setVisible(false);
            msaBox.setVisible(false);
            
        } else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
              systemNameLabel.setText(rb.getString("LabelSystemName"));
              systemNameLabel.setVisible(true);
              systemName.setVisible(true);
              userNameLabel.setText(rb.getString("LabelUserName"));
              v1Label.setVisible(false);
              to1.setVisible(false);
              s1Box.setVisible(false);
              v2Label.setVisible(false);
              to2.setVisible(false);
              s2Box.setVisible(false);
              s2aBox.setVisible(false);
              v3Label.setVisible(false);
              to3.setVisible(false);
              s3Box.setVisible(false);
              s3aBox.setVisible(false);
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
              vtLabel.setVisible(false);
              stBox.setVisible(false);
              mstBox.setVisible(false);
              msaBox.setVisible(false);
            
        } else if (mergSignalDriver.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(rb.getString("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(rb.getString("LabelUserName"));
            v1Label.setText("Aspects");
            v1Label.setVisible(true);
            to1.setVisible(false);
            s1Box.setVisible(false);
            v2Label.setText("Home");
            v2Label.setVisible(true);
            to2.setVisible(false);
            s2Box.setVisible(false);
            s2aBox.setVisible(false);
            mstBox.setVisible(true);
            msaBox.setVisible(true);
            setUkSignalAspectsFromBox(msaBox, 2);
            v3Label.setText("Input1");
            v3Label.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(false);
            s3aBox.setVisible(false);
            v4Label.setText("Input2");
            v4Label.setVisible(false);
            to4.setVisible(false);
            s4Box.setVisible(false);
            v5Label.setText("Input3");
            v5Label.setVisible(false);
            to5.setVisible(false);
            s5Box.setVisible(false);
            v6Label.setVisible(false);
            to6.setVisible(false);
            s6Box.setVisible(false);
            v7Label.setVisible(false);
            to7.setVisible(false);
            s7Box.setVisible(false);
            vtLabel.setVisible(false);
            stBox.setVisible(false);
            msaBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ukAspectChange(false);
                }
            });
        
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
		if (s==null){
            //Need to check that the Systemname doesn't already exists as a UserName
            NamedBean nB = InstanceManager.signalHeadManagerInstance().getByUserName(sName);
            if (nB!=null){
                log.error("System name is not unique " + sName + " It already exists as a User name");
                String msg = java.text.MessageFormat.format(AbstractTableAction.rb
                        .getString("WarningSystemNameAsUser"), new Object[] { ("" + sName) });
                JOptionPane.showMessageDialog(editFrame, msg,
                            AbstractTableAction.rb.getString("WarningTitle"),
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }
		// inform the user if signal head already exists, and return false so creation can be bypassed
		log.warn("Attempt to create signal with duplicate system name "+sName);
		String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("DuplicateSignalSystemName"), new Object[] { sName });
		JOptionPane.showMessageDialog(addFrame, msg,
				AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
		return false;
	}
    
    public boolean checkIntegerOnly(String s) {  
        String allowed = "0123456789";
        boolean result=true;
        //String result = "";
        for ( int i = 0; i < s.length(); i++ ) {
            if ( allowed.indexOf(s.charAt(i)) == -1 )
                result=false;
        }
        return result;
    }
	
	void addTurnoutMessage(String s1, String s2) {
		log.warn("Could not provide turnout "+s2);
		String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("AddNoTurnout"), new Object[] { s1, s2 });
		JOptionPane.showMessageDialog(addFrame, msg,
				AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
	}
    //@TODO We could do with checking the to make sure that the user has entered a turnout into a turnout field if it has been presented. Otherwise an error is recorded in the console window
    void okPressed(ActionEvent e) {
        if (!checkUserName(userName.getText()))
            return;
        SignalHead s;
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            handleSE8cOkPressed();
        } else if (acelaAspect.equals(typeBox.getSelectedItem())) {
            String inputusername = userName.getText();
            String inputsysname = to1.getText().toUpperCase();
            int headnumber;
            //int aspecttype;

            if (inputsysname.length() == 0) {
                log.warn("must supply a signalhead number (i.e. AH23)");
                return;
            }
            if(inputsysname.length()>2){
                if (inputsysname.substring(0,2).equals("AH"))
                    headnumber = Integer.parseInt(inputsysname.substring(2,inputsysname.length()));
                else if(checkIntegerOnly(inputsysname))
                    headnumber = Integer.parseInt(inputsysname);
                else{
                    String msg = java.text.MessageFormat.format(AbstractTableAction.rb
                        .getString("acelaSkippingCreation"), new Object[] { to1.getText() });
                    JOptionPane.showMessageDialog(addFrame, msg,
                        AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else
                headnumber = Integer.parseInt(inputsysname);
            if (checkBeforeCreating("AH"+headnumber)) {
                if (inputusername.length() == 0) {
                    s = new jmri.jmrix.acela.AcelaSignalHead("AH"+headnumber);
                } else {
                    s = new jmri.jmrix.acela.AcelaSignalHead("AH"+headnumber, inputusername);
                }
                InstanceManager.signalHeadManagerInstance().register(s);
            }

            int st = signalheadTypeFromBox(stBox);
            //This bit returns null i think, will need to check through
            AcelaNode sh = AcelaAddress.getNodeFromSystemName("AH"+headnumber);
            switch (st) {
                case 1: sh.setOutputSignalHeadType(headnumber, AcelaNode.DOUBLE); break;
                case 2: sh.setOutputSignalHeadType(headnumber, AcelaNode.TRIPLE); break;
                case 3: sh.setOutputSignalHeadType(headnumber, AcelaNode.BPOLAR); break;
                case 4: sh.setOutputSignalHeadType(headnumber, AcelaNode.WIGWAG); break;
                default:
                    log.warn("Unexpected Acela Aspect type: "+st);
                    sh.setOutputSignalHeadType(headnumber, AcelaNode.UKNOWN); break;  // default to triple
            }

        } else if (grapevine.equals(typeBox.getSelectedItem())) {
            // the turnout field must hold a GH system name
            if (systemName.getText().length() == 0) {
                log.warn("must supply a signalhead number (i.e. GH23)");
                return;
            }
            String inputsysname = systemName.getText().toUpperCase();
            if (!inputsysname.substring(0,2).equals("GH")) {
                log.warn("skipping creation of signal, "+inputsysname+" does not start with GH");
				String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("GrapevineSkippingCreation"), new Object[] { inputsysname });
				JOptionPane.showMessageDialog(addFrame, msg,
					AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);				
                return;
            }
			if (checkBeforeCreating(inputsysname)) {
				s = new jmri.jmrix.grapevine.SerialSignalHead(inputsysname,userName.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (quadOutput.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(to3.getText());
            Turnout t4 = InstanceManager.turnoutManagerInstance().provideTurnout(to4.getText());
            if (t1==null) addTurnoutMessage(v1Label.getText(), to1.getText());
            if (t2==null) addTurnoutMessage(v2Label.getText(), to2.getText());
            if (t3==null) addTurnoutMessage(v3Label.getText(), to3.getText());
            if (t4==null) addTurnoutMessage(v4Label.getText(), to4.getText());
            if (t4==null || t3==null || t2==null || t1==null) {
                log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                return;
            }

            if (checkBeforeCreating(systemName.getText())) {
                s = new jmri.implementation.QuadOutputSignalHead(systemName.getText(),userName.getText(),
            	    new NamedBeanHandle<Turnout>(to1.getText(),t1), 
            	    new NamedBeanHandle<Turnout>(to2.getText(),t2), 
            	    new NamedBeanHandle<Turnout>(to3.getText(),t3), 
            	    new NamedBeanHandle<Turnout>(to4.getText(),t4));
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
                log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                return;
            }
            if (checkBeforeCreating(systemName.getText())) {
                s = new jmri.implementation.TripleTurnoutSignalHead(systemName.getText(),userName.getText(),
            	    new NamedBeanHandle<Turnout>(to1.getText(),t1), 
            	    new NamedBeanHandle<Turnout>(to2.getText(),t2), 
            	    new NamedBeanHandle<Turnout>(to3.getText(),t3));

                InstanceManager.signalHeadManagerInstance().register(s);
            }
        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to2.getText());
            if (t1==null) addTurnoutMessage(v1Label.getText(), to1.getText());
            if (t2==null) addTurnoutMessage(v2Label.getText(), to2.getText());
            if (t2==null || t1==null) {
                log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                return;
            }
            if (checkBeforeCreating(systemName.getText())) {			
            	s = new jmri.implementation.DoubleTurnoutSignalHead(systemName.getText(),userName.getText(),
            	    new NamedBeanHandle<Turnout>(to1.getText(),t1), 
            	    new NamedBeanHandle<Turnout>(to2.getText(),t2));
                s.setUserName(userName.getText());
                InstanceManager.signalHeadManagerInstance().register(s);
            }
        } else if (singleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to1.getText());
            int on = signalStateFromBox(s2aBox);
            int off = signalStateFromBox(s3aBox);
            if (t1==null) addTurnoutMessage(v1Label.getText(), to1.getText());
            if (t1==null) {
                log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                return;
            }
            if (checkBeforeCreating(systemName.getText())) {			
            	s = new jmri.implementation.SingleTurnoutSignalHead(systemName.getText(),userName.getText(),
            	    new NamedBeanHandle<Turnout>(to1.getText(),t1), on, off);
                InstanceManager.signalHeadManagerInstance().register(s);
            }
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            if (checkBeforeCreating(systemName.getText())) {
				s = new jmri.implementation.VirtualSignalHead(systemName.getText(),userName.getText());
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
                log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                return;
            }
            if (checkBeforeCreating(systemName.getText())) {
				s = new jmri.implementation.LsDecSignalHead(systemName.getText(), t1, s1, t2, s2, t3, s3, t4, s4, t5, s5, t6, s6, t7, s7);
                s.setUserName(userName.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
		} else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
			if (checkBeforeCreating(systemName.getText())) {            
				s = new jmri.implementation.DccSignalHead(systemName.getText());
                s.setUserName(userName.getText());
				InstanceManager.signalHeadManagerInstance().register(s);
			}
        } else if (mergSignalDriver.equals(typeBox.getSelectedItem())){
            handleMergSignalDriverOkPressed();
        }else log.error("Unexpected type: "+typeBox.getSelectedItem());
    }
	
	void handleSE8cOkPressed() {
        SignalHead s;
        // the turnout field can hold either a NNN number or a system name
        String num1 = to1.getText().toUpperCase();
        String num2 = to2.getText().toUpperCase();
        if (checkIntegerOnly(num1)) {
            // input is number, handle that way
            int number = Integer.parseInt(num1);
            num2 = ""+(number+1);
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(num1);
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(num2);
            s = new jmri.implementation.SE8cSignalHead(
                new NamedBeanHandle<Turnout>(num1, t1),
                new NamedBeanHandle<Turnout>(num2, t2),
                userName.getText());
            InstanceManager.signalHeadManagerInstance().register(s);
        } else {
            // hopefully, this is a turnout name, as is 2nd field
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(num1);
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(num2);
            // check validity
            if (t1 != null && t2 != null) {
                // OK process
                s = new jmri.implementation.SE8cSignalHead(
                    new NamedBeanHandle<Turnout>(num1, t1),
                    new NamedBeanHandle<Turnout>(num2, t2),
                    userName.getText());
                InstanceManager.signalHeadManagerInstance().register(s);
            } else {
                // couldn't create turnouts, error
                String msg = java.text.MessageFormat.format(AbstractTableAction.rb
                    .getString("se8c4SkippingCreation"), new Object[] { to1.getText() });
                JOptionPane.showMessageDialog(addFrame, msg,
                    AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
	}
	
    void handleSE8cTypeChanged() {
        systemNameLabel.setVisible(false);
        systemName.setVisible(false);
        userNameLabel.setText(rb.getString("LabelUserName"));
        v1Label.setText(rb.getString("LabelTurnoutNumber"));
        v1Label.setVisible(true);
        to1.setVisible(true);
        s1Box.setVisible(false);
        v2Label.setVisible(true);
        v2Label.setText(rb.getString("LabelSecondNumber"));
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
        vtLabel.setVisible(false);
        stBox.setVisible(false);
        mstBox.setVisible(false);
        msaBox.setVisible(false);
    }
    
	void handleSE8cEditSignal() {
        signalType.setText(se8c4Aspect);
        eSystemNameLabel.setText(rb.getString("LabelSystemName"));
        eSysNameLabel.setText(curS.getSystemName());
        eUserNameLabel.setText(rb.getString("LabelUserName"));
        eUserNameLabel.setVisible(true);
        eUserName.setVisible(true);
        eUserName.setText(curS.getUserName());
        eSystemNameLabel.setText(rb.getString("LabelSystemName"));
        eSysNameLabel.setText(curS.getSystemName());
        //eSysNameLabel.setVisible(true);
	}
	
	void handleSE8cUpdatePressed() {
        // user name handled by common code; notthing else to change
	}
	
    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SF_SWITCH_FALLTHROUGH")
	void handleMergSignalDriverOkPressed() {
        SignalHead s;
        // Adding Merg Signal Driver.
        Turnout t3 = null;
        Turnout t2 = null;
        Turnout t1 = null;
        NamedBeanHandle <Turnout> nbt1 = null;
        NamedBeanHandle <Turnout> nbt2 = null;
        NamedBeanHandle <Turnout> nbt3 = null;

        switch(ukSignalAspectsFromBox(msaBox)){
            case 4: t3 = InstanceManager.turnoutManagerInstance().provideTurnout(to5.getText());
                    if (t3==null) {
                        addTurnoutMessage(v5Label.getText(), to5.getText());
                        log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                        return;
                    } else
                        nbt3 = new NamedBeanHandle<Turnout>(to5.getText(),t3);

                    // fall through
            case 3: t2 = InstanceManager.turnoutManagerInstance().provideTurnout(to4.getText());
                    if (t2==null) {
                        addTurnoutMessage(v4Label.getText(), eto4.getText());
                        log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                        return;
                    } else
                        nbt2 = new NamedBeanHandle<Turnout>(to4.getText(),t2);
                    // fall through
            case 2: t1 = InstanceManager.turnoutManagerInstance().provideTurnout(to3.getText());
                    if (t1==null) {
                        addTurnoutMessage(v3Label.getText(), eto3.getText());
                        log.warn("skipping creation of signal "+systemName.getText()+" due to error");
                        return;
                    } else
                        nbt1 = new NamedBeanHandle<Turnout>(to3.getText(),t1);
        }
        if (checkBeforeCreating(systemName.getText())) {
            boolean home;
            if(ukSignalTypeFromBox(mstBox).equals("Distant")) home=false;
            else home=true;

            s = new jmri.implementation.MergSD2SignalHead(systemName.getText(), ukSignalAspectsFromBox(msaBox), nbt1, nbt2, nbt3, false, home);
            s.setUserName(userName.getText());
            InstanceManager.signalHeadManagerInstance().register(s);

        }
	}
	
    // variables for edit of signal heads
    boolean editingHead = false;
    String editSysName = "";
    JmriJFrame editFrame = null;
    JLabel signalType = new JLabel("XXXX");
    SignalHead curS = null;
    String className = "";
    
    JTextField eSystemName = new JTextField(5);
    JTextField eUserName = new JTextField(10);
    JTextField eto1 = new JTextField(5);
    JTextField eto2 = new JTextField(5);
    JTextField eto3 = new JTextField(5);
    JTextField eto4 = new JTextField(5);
    JTextField eto5 = new JTextField(5);
    JTextField eto6 = new JTextField(5);
    JTextField eto7 = new JTextField(5);
    JTextField etot = new JTextField(5);
    JLabel eSystemNameLabel = new JLabel("");
    JLabel eUserNameLabel = new JLabel("");
    JLabel eSysNameLabel = new JLabel ("");
    JLabel ev1Label = new JLabel("");
    JLabel ev2Label = new JLabel("");
    JLabel ev3Label = new JLabel("");
    JLabel ev4Label = new JLabel("");
    JLabel ev5Label = new JLabel("");
    JLabel ev6Label = new JLabel("");
    JLabel ev7Label = new JLabel("");
    JLabel evtLabel = new JLabel("");
    JComboBox es1Box = new JComboBox(turnoutStates);
    JComboBox es2Box = new JComboBox(turnoutStates);
    JComboBox es2aBox = new JComboBox(signalStates);
    JComboBox es3Box = new JComboBox(turnoutStates);
    JComboBox es3aBox = new JComboBox(signalStates);
    JComboBox es4Box = new JComboBox(turnoutStates);
    JComboBox es5Box = new JComboBox(turnoutStates);
    JComboBox es6Box = new JComboBox(turnoutStates);
    JComboBox es7Box = new JComboBox(turnoutStates);
    JComboBox estBox = new JComboBox(signalheadTypes);
    JComboBox emstBox = new JComboBox(ukSignalType);
    JComboBox emsaBox = new JComboBox(ukSignalAspects);
    
	
    void editSignal(int row) {
		// Logix was found, initialize for edit
        String eSName = (String)m.getValueAt(row,BeanTableDataModel.SYSNAMECOL);
		_curSignal = InstanceManager.signalHeadManagerInstance().getBySystemName(eSName);
		//numConditionals = _curLogix.getNumConditionals();
		// create the Edit Logix Window
        // Use separate Thread so window is created on top
        Thread t = new Thread() {
                public void run() {
                    //Thread.yield();
                    makeEditSignalWindow();
                    }
                };
        if (log.isDebugEnabled()) log.debug("editPressed Thread started for " + eSName);
        javax.swing.SwingUtilities.invokeLater(t);
	}
    
    SignalHead _curSignal = null;
    
	void makeEditSignalWindow() {
        String eSName = _curSignal.getSystemName();
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
            editFrame.setSaveFrameSize(false);
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
            p.add(eSystemNameLabel);
            p.add(eSystemName);
			p.add(eSysNameLabel);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(eUserNameLabel);
            p.add(eUserName);
            editFrame.getContentPane().add(p);
            // create seven boxes for input information, and put into pane
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev1Label);
            p.add(eto1);
            p.add(es1Box);
            p.add(emsaBox);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev2Label);
            p.add(eto2);
            p.add(es2Box);
            p.add(es2aBox);
            p.add(emstBox);
            editFrame.getContentPane().add(p);
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(ev3Label);
            p.add(eto3);
            p.add(es3Box);
            p.add(es3aBox);
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
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(evtLabel);
            p.add(etot);
            p.add(estBox);
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
		eSystemName.setVisible(false);
		eSysNameLabel.setVisible(true);
        eUserNameLabel.setVisible(true);
        eUserName.setVisible(true);
		ev1Label.setVisible(false);
		eto1.setVisible(false);
		es1Box.setVisible(false);
		ev2Label.setVisible(false);
		eto2.setVisible(false);
		es2Box.setVisible(false);
        es2aBox.setVisible(false);
		ev3Label.setVisible(false);
		eto3.setVisible(false);
		es3Box.setVisible(false);
        es3aBox.setVisible(false);
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
		evtLabel.setVisible(false);
		etot.setVisible(false);
		estBox.setVisible(false);		
        emstBox.setVisible(false);
        emsaBox.setVisible(false);
		// determine class name of signal head and initialize this class of signal
		className = curS.getClass().getName();
		if (className.equals("jmri.implementation.QuadOutputSignalHead")) {
			signalType.setText(quadOutput);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((TripleTurnoutSignalHead)curS).getGreen().getName());
            ev2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((TripleTurnoutSignalHead)curS).getYellow().getName());
            ev3Label.setText(rb.getString("LabelRedTurnoutNumber"));
			ev3Label.setVisible(true);
            eto3.setVisible(true);
			eto3.setText(((TripleTurnoutSignalHead)curS).getRed().getName());
            ev4Label.setText(rb.getString("LabelLunarTurnoutNumber"));
			ev4Label.setVisible(true);
            eto4.setVisible(true);
			eto4.setText(((QuadOutputSignalHead)curS).getLunar().getName());
		}
		else if (className.equals("jmri.implementation.TripleTurnoutSignalHead")) {
			signalType.setText(tripleTurnout);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((TripleTurnoutSignalHead)curS).getGreen().getName());
            ev2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((TripleTurnoutSignalHead)curS).getYellow().getName());
            ev3Label.setText(rb.getString("LabelRedTurnoutNumber"));
			ev3Label.setVisible(true);
            eto3.setVisible(true);
			eto3.setText(((TripleTurnoutSignalHead)curS).getRed().getName());
		}
		else if (className.equals("jmri.implementation.DoubleTurnoutSignalHead")) {
			signalType.setText(doubleTurnout);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((DoubleTurnoutSignalHead)curS).getGreen().getName());
            ev2Label.setText(rb.getString("LabelRedTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((DoubleTurnoutSignalHead)curS).getRed().getName());
 		}
		else if (className.equals("jmri.implementation.SingleTurnoutSignalHead")) {
			signalType.setText(singleTurnout);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Label.setText(rb.getString("LabelTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((SingleTurnoutSignalHead)curS).getOutput().getName());
            ev2Label.setText("On Appearance");
            ev2Label.setVisible(true);
            es2aBox.setVisible(true);
            setSignalStateInBox(es2aBox, ((SingleTurnoutSignalHead)curS).getOnAppearance());
            ev3Label.setText("Off Appearance");
            ev3Label.setVisible(true);
            es3aBox.setVisible(true);
            setSignalStateInBox(es3aBox, ((SingleTurnoutSignalHead)curS).getOffAppearance());
 		}
		else if (className.equals("jmri.implementation.VirtualSignalHead")) {
			signalType.setText(virtualHead);
			eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
		}
		else if (className.equals("jmri.implementation.LsDecSignalHead")) {
			signalType.setText(lsDec);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Label.setText(rb.getString("LabelGreenTurnoutNumber"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(((jmri.implementation.LsDecSignalHead)curS).getGreen().getSystemName());
            es1Box.setVisible(true);
			setTurnoutStateInBox(es1Box, ((jmri.implementation.LsDecSignalHead)curS).getGreenState(), turnoutStateValues);
            ev2Label.setText(rb.getString("LabelYellowTurnoutNumber"));
            ev2Label.setVisible(true);
            eto2.setVisible(true);
			eto2.setText(((jmri.implementation.LsDecSignalHead)curS).getYellow().getSystemName());
            es2Box.setVisible(true);
			setTurnoutStateInBox(es2Box, ((jmri.implementation.LsDecSignalHead)curS).getYellowState(), turnoutStateValues);
            ev3Label.setText(rb.getString("LabelRedTurnoutNumber"));
			ev3Label.setVisible(true);
            eto3.setVisible(true);
			eto3.setText(((jmri.implementation.LsDecSignalHead)curS).getRed().getSystemName());
            es3Box.setVisible(true);
			setTurnoutStateInBox(es3Box, ((jmri.implementation.LsDecSignalHead)curS).getRedState(), turnoutStateValues);
            ev4Label.setText(rb.getString("LabelFlashGreenTurnoutNumber"));
            ev4Label.setVisible(true);
            eto4.setVisible(true);
			eto4.setText(((jmri.implementation.LsDecSignalHead)curS).getFlashGreen().getSystemName());
            es4Box.setVisible(true);
			setTurnoutStateInBox(es4Box, ((jmri.implementation.LsDecSignalHead)curS).getFlashGreenState(), turnoutStateValues);
            ev5Label.setText(rb.getString("LabelFlashYellowTurnoutNumber"));
            ev5Label.setVisible(true);
            eto5.setVisible(true);
			eto5.setText(((jmri.implementation.LsDecSignalHead)curS).getFlashYellow().getSystemName());
            es5Box.setVisible(true);
			setTurnoutStateInBox(es5Box, ((jmri.implementation.LsDecSignalHead)curS).getFlashYellowState(), turnoutStateValues);
			ev6Label.setText(rb.getString("LabelFlashRedTurnoutNumber"));
            ev6Label.setVisible(true);
            eto6.setVisible(true);
 			eto6.setText(((jmri.implementation.LsDecSignalHead)curS).getFlashRed().getSystemName());
			es6Box.setVisible(true);
			setTurnoutStateInBox(es6Box, ((jmri.implementation.LsDecSignalHead)curS).getFlashRedState(), turnoutStateValues);
			ev7Label.setText(rb.getString("LabelDarkTurnoutNumber"));
            ev7Label.setVisible(true);
            eto7.setVisible(true);
			eto7.setText(((jmri.implementation.LsDecSignalHead)curS).getDark().getSystemName());
            es7Box.setVisible(true);
			setTurnoutStateInBox(es7Box, ((jmri.implementation.LsDecSignalHead)curS).getDarkState(), turnoutStateValues);
		}
		else if (className.equals("jmri.implementation.SE8cSignalHead")) {
            handleSE8cEditSignal();
		}
		else if (className.equals("jmri.jmrix.grapevine.SerialSignalHead")) {
			signalType.setText(grapevine);
			eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            /*ev1Label.setText(rb.getString("LabelUserName"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(curS.getUserName());*/
		}
		else if (className.equals("jmri.jmrix.acela.AcelaSignalHead")) {
			signalType.setText(acela);
			eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            /*ev1Label.setText(rb.getString("LabelUserName"));
            ev1Label.setVisible(true);
            eto1.setVisible(true);
			eto1.setText(curS.getUserName());*/
            evtLabel.setText(rb.getString("LabelAspectType"));
            evtLabel.setVisible(true);
            etot.setVisible(false);
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(curS.getSystemName());
            if (tNode == null) {
                // node does not exist, ignore call
                log.error("Can't find new Acela Signal with name '"+curS.getSystemName());
                return;
            }
            int headnumber = Integer.parseInt(curS.getSystemName().substring(2,curS.getSystemName().length()));

            estBox.setVisible(true);
            setSignalheadTypeInBox(estBox, tNode.getOutputSignalHeadType(headnumber), signalheadTypeValues);
        }
		else if (className.equals("jmri.implementation.DccSignalHead")) {
			signalType.setText(dccSignalDecoder);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
        } 
        else if (className.equals("jmri.implementation.MergSD2SignalHead")) {
        //Edit signal stuff to go here!
            signalType.setText(mergSignalDriver);
            eSystemNameLabel.setText(rb.getString("LabelSystemName"));
			eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(rb.getString("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Label.setText("Aspects");
            ev1Label.setVisible(true);
            setUkSignalAspectsFromBox(emsaBox, ((jmri.implementation.MergSD2SignalHead)curS).getAspects());
            eto1.setVisible(false);
            emsaBox.setVisible(true);
            ev2Label.setText("Signal Type");
            ev2Label.setVisible(true);
            eto2.setVisible(false);
            emstBox.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead)curS).getHome())
                setUkSignalType(emstBox, "Home");
            else
                setUkSignalType(emstBox, "Distant");
            //setUKSignalTypeFromBox(emstBox, ((jmri.implementation.MergSD2SignalHead)curS).getAspects());
            ev3Label.setText("Input1");
            ev3Label.setVisible(true);
            eto3.setVisible(true);
            eto3.setText(((jmri.implementation.MergSD2SignalHead)curS).getInput1().getName());
            ev4Label.setText("Input2");
            ev4Label.setVisible(true);
            eto4.setVisible(true);
            if(((jmri.implementation.MergSD2SignalHead)curS).getInput2()!=null)
                eto4.setText(((jmri.implementation.MergSD2SignalHead)curS).getInput2().getName());
            ev5Label.setText("Input3");
            ev5Label.setVisible(true);
            eto5.setVisible(true);
            if(((jmri.implementation.MergSD2SignalHead)curS).getInput3()!=null)
                eto5.setText(((jmri.implementation.MergSD2SignalHead)curS).getInput3().getName());
            emsaBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ukAspectChange(true);
                }
            });
            ukAspectChange(true);
        }
        else log.error("Cannot edit SignalHead of unrecognized type: "+className);			
		// finish up
		editFrame.pack();
		editFrame.setVisible(true);
	}
			
	void cancelPressed(ActionEvent e) {
		editFrame.setVisible(false);
		editingHead = false;
	}
	
    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SF_SWITCH_FALLTHROUGH")
	void updatePressed(ActionEvent e) {
    	String nam = eUserName.getText();
		// check if user name changed
		if (!((curS.getUserName()!=null) && (curS.getUserName().equals(nam)))) {
            if(checkUserName(nam))
                curS.setUserName(nam);
            else
                return;
		}
		// update according to class of signal head
		if (className.equals("jmri.implementation.QuadOutputSignalHead")) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(eto3.getText());
            Turnout t4 = InstanceManager.turnoutManagerInstance().provideTurnout(eto4.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			else ((QuadOutputSignalHead)curS).setGreen(new NamedBeanHandle<Turnout>(eto1.getText(),t1));
            if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((QuadOutputSignalHead)curS).setYellow(new NamedBeanHandle<Turnout>(eto2.getText(),t2));
            if (t3==null) {
				noTurnoutMessage(ev3Label.getText(), eto3.getText());
				return;
			}
			else ((QuadOutputSignalHead)curS).setRed(new NamedBeanHandle<Turnout>(eto3.getText(),t3));
            if (t4==null) {
				noTurnoutMessage(ev4Label.getText(), eto4.getText());
				return;
			}
			else ((QuadOutputSignalHead)curS).setLunar(new NamedBeanHandle<Turnout>(eto4.getText(),t4));
		}
		else if (className.equals("jmri.implementation.TripleTurnoutSignalHead")) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(eto3.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			else ((TripleTurnoutSignalHead)curS).setGreen(new NamedBeanHandle<Turnout>(eto1.getText(),t1));
            if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((TripleTurnoutSignalHead)curS).setYellow(new NamedBeanHandle<Turnout>(eto2.getText(),t2));
            if (t3==null) {
				noTurnoutMessage(ev3Label.getText(), eto3.getText());
				return;
			}
			else ((TripleTurnoutSignalHead)curS).setRed(new NamedBeanHandle<Turnout>(eto3.getText(),t3));
		}
		else if (className.equals("jmri.implementation.DoubleTurnoutSignalHead")) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto2.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			else ((DoubleTurnoutSignalHead)curS).setGreen(new NamedBeanHandle<Turnout>(eto1.getText(),t1));
            if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((DoubleTurnoutSignalHead)curS).setRed(new NamedBeanHandle<Turnout>(eto2.getText(),t2));
		}
		else if (className.equals("jmri.implementation.SingleTurnoutSignalHead")) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto1.getText());
            if (t1==null) {
				noTurnoutMessage(ev1Label.getText(), eto1.getText());
				return;
			}
			((SingleTurnoutSignalHead)curS).setOutput(new NamedBeanHandle<Turnout>(eto1.getText(),t1));
            ((SingleTurnoutSignalHead)curS).setOnAppearance(signalStateFromBox(es2aBox));
            ((SingleTurnoutSignalHead)curS).setOffAppearance(signalStateFromBox(es3aBox));
		}
		else if (className.equals("jmri.implementation.LsDecSignalHead")) {
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
			else ((jmri.implementation.LsDecSignalHead)curS).setGreen(t1);
			if (t2==null) {
				noTurnoutMessage(ev2Label.getText(), eto2.getText());
				return;
			}
			else ((jmri.implementation.LsDecSignalHead)curS).setYellow(t2);
            if (t3==null) {
				noTurnoutMessage(ev3Label.getText(), eto3.getText());
				return;
			}
			else ((jmri.implementation.LsDecSignalHead)curS).setRed(t3);
            if (t4==null) {
				noTurnoutMessage(ev4Label.getText(), eto4.getText());
				return;
			}
			else ((jmri.implementation.LsDecSignalHead)curS).setFlashGreen(t4);
            if (t5==null) {
				noTurnoutMessage(ev5Label.getText(), eto5.getText());
				return;
			}
			else ((jmri.implementation.LsDecSignalHead)curS).setFlashYellow(t5);
            if (t6==null) {
				noTurnoutMessage(ev6Label.getText(), eto6.getText());
				return;
			}
			else ((jmri.implementation.LsDecSignalHead)curS).setFlashRed(t6);
            if (t7==null) {
				noTurnoutMessage(ev7Label.getText(), eto7.getText());
				return;
			}
			else ((jmri.implementation.LsDecSignalHead)curS).setDark(t7);
			((jmri.implementation.LsDecSignalHead)curS).setGreenState(turnoutStateFromBox(es1Box));
			((jmri.implementation.LsDecSignalHead)curS).setYellowState(turnoutStateFromBox(es2Box));
			((jmri.implementation.LsDecSignalHead)curS).setRedState(turnoutStateFromBox(es3Box));
			((jmri.implementation.LsDecSignalHead)curS).setFlashGreenState(turnoutStateFromBox(es4Box));
			((jmri.implementation.LsDecSignalHead)curS).setFlashYellowState(turnoutStateFromBox(es5Box));
			((jmri.implementation.LsDecSignalHead)curS).setFlashRedState(turnoutStateFromBox(es6Box));
			((jmri.implementation.LsDecSignalHead)curS).setDarkState(turnoutStateFromBox(es7Box));    
		}
		else if (className.equals("jmri.implementation.SE8cSignalHead")) {
            handleSE8cUpdatePressed();
		}
		else if (className.equals("jmri.jmrix.grapevine.SerialSignalHead")) {
			/*String nam = eUserName.getText();
			// check if user name changed
			if (!((curS.getUserName()!=null) && (curS.getUserName().equals(nam)))) {
                if(checkUserName(nam))
                    curS.setUserName(nam);
			}*/
		}
		else if (className.equals("jmri.jmrix.acela.AcelaSignalHead")) {
            /*String nam = eUserName.getText();
                // check if user name changed
            if (!((curS.getUserName()!=null) && (curS.getUserName().equals(nam)))) {
                if(checkUserName(nam))
                    curS.setUserName(nam);
            
            }*/
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(curS.getSystemName());
            if (tNode == null) {
                // node does not exist, ignore call
                log.error("Can't find new Acela Signal with name '"+curS.getSystemName());
                return;
            }
            int headnumber = Integer.parseInt(curS.getSystemName().substring(2,curS.getSystemName().length()));
            tNode.setOutputSignalHeadTypeString(headnumber, estBox.getSelectedItem().toString());
//          setSignalheadTypeInBox(estBox, tNode.getOutputSignalHeadType(headnumber), signalheadTypeValues);
//          ((jmri.AcelaSignalHead)curS).setDarkState(signalheadTypeFromBox(estBox));    
		}
        else if (className.equals("jmri.implementation.MergSD2SignalHead")){
            switch(ukSignalAspectsFromBox(emsaBox)){
                case 4: Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout(eto5.getText());
                        if (t3==null) {
                            noTurnoutMessage(ev5Label.getText(), eto5.getText());
                            return;
                            }
                        else ((jmri.implementation.MergSD2SignalHead)curS).setInput3(new NamedBeanHandle<Turnout>(eto5.getText(),t3));
                        // fall through
                case 3: Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout(eto4.getText());
                        if (t2==null) {
                            noTurnoutMessage(ev4Label.getText(), eto4.getText());
                            return;
                            }
                        else ((jmri.implementation.MergSD2SignalHead)curS).setInput2(new NamedBeanHandle<Turnout>(eto4.getText(),t2));
                        // fall through
                case 2: Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(eto3.getText());
                        if (t1==null) {
                            noTurnoutMessage(ev3Label.getText(), eto3.getText());
                            return;
                            }
                        else ((jmri.implementation.MergSD2SignalHead)curS).setInput1(new NamedBeanHandle<Turnout>(eto3.getText(),t1));
                        ((jmri.implementation.MergSD2SignalHead)curS).setAspects(ukSignalAspectsFromBox(emsaBox));
                        if(ukSignalTypeFromBox(emstBox)=="Distant") ((jmri.implementation.MergSD2SignalHead)curS).setHome(false);
                        else ((jmri.implementation.MergSD2SignalHead)curS).setHome(true);
            }
            //Need to add the code here for update!
        }
		else {
		    log.error("Internal error - cannot update signal of type "+className);
		}
		// successful
		editFrame.setVisible(false);
		editingHead = false;
	}
    
    boolean checkUserName(String nam){
        if (!((nam==null) || (nam.equals("")))) {
            // user name changed, check if new name already exists
            NamedBean nB = InstanceManager.signalHeadManagerInstance().getByUserName(nam);
            if (nB != null) {
                log.error("User name is not unique " + nam);
                String msg = java.text.MessageFormat.format(AbstractTableAction.rb
                        .getString("WarningUserName"), new Object[] { ("" + nam) });
                JOptionPane.showMessageDialog(editFrame, msg,
                            AbstractTableAction.rb.getString("WarningTitle"),
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
            //Check to ensure that the username doesn't exist as a systemname.
            nB = InstanceManager.signalHeadManagerInstance().getBySystemName(nam);
            if (nB!=null){
                log.error("User name is not unique " + nam + " It already exists as a System name");
                String msg = java.text.MessageFormat.format(AbstractTableAction.rb
                        .getString("WarningUserNameAsSystem"), new Object[] { ("" + nam) });
                JOptionPane.showMessageDialog(editFrame, msg,
                            AbstractTableAction.rb.getString("WarningTitle"),
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    
    }
	void noTurnoutMessage(String s1, String s2) {
		log.warn("Could not provide turnout "+s2);
		String msg = java.text.MessageFormat.format(AbstractTableAction.rb
					.getString("WarningNoTurnout"), new Object[] { s1, s2 });
		JOptionPane.showMessageDialog(editFrame, msg,
				AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
	}
    
    void ukAspectChange(boolean edit){
        if(edit){
            switch (ukSignalAspectsFromBox(emsaBox)){
                case 2 : ev4Label.setVisible(false);
                         eto4.setVisible(false);
                         ev5Label.setVisible(false);
                         eto5.setVisible(false);
                         ev2Label.setVisible(true);
                         emstBox.setVisible(true);
                         break;
                case 3 : ev4Label.setVisible(true);
                         eto4.setVisible(true);
                         ev5Label.setVisible(false);
                         eto5.setVisible(false);
                         ev2Label.setVisible(false);
                         emstBox.setVisible(false);
                         setUkSignalType(emstBox, "Home");
                         break;
                case 4 : ev4Label.setVisible(true);
                         eto4.setVisible(true);
                         ev5Label.setVisible(true);
                         eto5.setVisible(true);
                         ev2Label.setVisible(false);
                         emstBox.setVisible(false);
                         setUkSignalType(emstBox, "Home");
            }
            editFrame.pack();
        
        } else {
            switch (ukSignalAspectsFromBox(msaBox)){
                case 2 : v4Label.setVisible(false);
                         to4.setVisible(false);
                         v5Label.setVisible(false);
                         to5.setVisible(false);
                         v2Label.setVisible(true);
                         mstBox.setVisible(true);
                         break;
                case 3 : v4Label.setVisible(true);
                         to4.setVisible(true);
                         v5Label.setVisible(false);
                         to5.setVisible(false);
                         v2Label.setVisible(false);
                         mstBox.setVisible(false);
                         setUkSignalType(mstBox, "Home");
                         break;
                case 4 : v4Label.setVisible(true);
                         to4.setVisible(true);
                         v5Label.setVisible(true);
                         to5.setVisible(true);
                         v2Label.setVisible(false);
                         mstBox.setVisible(false);
                         setUkSignalType(mstBox, "Home");
            }
            addFrame.pack();
        }
    
    }

    protected String getClassName() { return SignalHeadTableAction.class.getName(); }
    
    public String getClassDescription() { return rb.getString("TitleSignalTable"); }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadTableAction.class.getName());
}
/* @(#)SignalHeadTableAction.java */

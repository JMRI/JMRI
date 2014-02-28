package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jmri.DccLocoAddress;

/**
 * Frame for defining and launching an entry/exit warrant.  An NX warrant is a warrant that
 * can be defined on the run without a pre-recorded learn mode session using a set script for
 * ramping startup and stop throttle settings.
 * <P>
 * The route can be defined in a form or by mouse clicking on the OBlock IndicatorTrack icons.
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Pete Cressman  Copyright (C) 2009, 2010
 */
public class NXFrame extends WarrantRoute {
	WarrantTableFrame 	_parent;
    JTextField  _dccNumBox = new JTextField();
    JTextField  _trainNameBox = new JTextField();
    JTextField  _speedBox = new JTextField();
    JCheckBox	_forward = new JCheckBox();
    JCheckBox	_stageEStop = new JCheckBox();    
    JCheckBox	_haltStart = new JCheckBox();    
    JTextField _rampInterval = new JTextField();
    JTextField _searchDepth = new JTextField();
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
//    int _clickCount;

    private static NXFrame _instance;
    
    static NXFrame getInstance() {
    	if (_instance==null) {
    		_instance = new NXFrame();
    	}
    	_instance.setVisible(true);
    	_instance._dccNumBox.setText(null);
    	_instance._trainNameBox.setText(null);
    	_instance.clearRoute();
    	return _instance;
    }

    private NXFrame() {
		super();
		_parent = WarrantTableFrame.getInstance();
		setTitle(Bundle.getMessage("AutoWarrant"));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10,10));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue());
        panel.add(makeBlockPanels());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup bg = new ButtonGroup();
        bg.add(_runAuto);
        bg.add(_runManual);
        _runAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                enableAuto(true);
            }
        });
        _runManual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                enableAuto(false);
            }
        });
        _runAuto.setSelected(true);
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runAuto);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runManual);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
//        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _trainNameBox, "TrainName", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
//        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _speedBox, "Speed", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeBoxPanel(false, _forward, "forward"));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeBoxPanel(false, _stageEStop, "StageEStop"));
        ppp.add(WarrantFrame.makeBoxPanel(false, _haltStart, "HaltStart"));
        pp.add(ppp);
        pp.add(WarrantFrame.makeTextBoxPanel(false, _rampInterval, "rampInterval", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _searchDepth, "SearchDepth", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        _forward.setSelected(true);
        _stageEStop.setSelected(WarrantTableFrame._defaultEStop);
        _haltStart.setSelected(WarrantTableFrame._defaultHaltStart);
        _speedBox.setText(WarrantTableFrame._defaultSpeed);
        _rampInterval.setText(WarrantTableFrame._defaultIntervalTime);
        _searchDepth.setText(WarrantTableFrame._defaultSearchdepth);
        JPanel p = new JPanel();
        JButton button = new JButton(Bundle.getMessage("ButtonRunNX"));
        button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    	makeAndRunWarrant();
                    }
                });
        p.add(button);
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    	dispose();
                    	_parent.closeNXFrame();
                    }
                });
        p.add(button);
        panel.add(p);
        mainPanel.add(panel);
        getContentPane().add(mainPanel);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                _parent.closeNXFrame();
            }
        });
        setLocation(_parent.getLocation().x+200, _parent.getLocation().y+100);
        setAlwaysOnTop(true);
        pack();
        setVisible(true);      		
	}
    
    private void enableAuto(boolean enable) {
		_dccNumBox.setEnabled(enable);
	    _speedBox.setEnabled(enable);
	    _forward.setEnabled(enable);
	    _stageEStop.setEnabled(enable);    
	    _haltStart.setEnabled(enable);    
	    _rampInterval.setEnabled(enable);    		
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
//        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
//                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
//                                            " source= "+e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
        	doAction(e.getSource());
        }
    }

    /**
     * Callback from RouteFinder.findRoute()
     */
    public void selectedRoute(ArrayList<BlockOrder> orders) {
    	String msg =null;
    	Warrant warrant = null;
    	if (_runManual.isSelected()) {
    		runManual();
    		return;
    	} else if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
            msg = Bundle.getMessage("NoLoco");
        }
        if (msg==null){
            String addr = _dccNumBox.getText();
            if (addr!= null && addr.length() != 0) {
                boolean isLong = false;
                int dccNum = 0;
            	addr = addr.toUpperCase().trim();
        		Character ch = addr.charAt(addr.length()-1);
        		try {
            		if (!Character.isDigit(ch)) {
            			if (ch!='S' && ch!='L' && ch!=')') {
            				msg = Bundle.getMessage("BadDccAddress", addr);
            			}
            			if (ch==')') {
                        	dccNum = Integer.parseInt(addr.substring(0, addr.length()-3));
                        	ch = addr.charAt(addr.length()-2);
                        	isLong = (ch=='L');
            			} else {
                        	dccNum = Integer.parseInt(addr.substring(0, addr.length()-1));        				
                        	isLong = (ch=='L');
            			}
            		} else {
                		dccNum = Integer.parseInt(addr);
                		ch = addr.charAt(0);
                        isLong = (ch=='0' || dccNum>127);  // leading zero means long
                        addr = addr + (isLong?"L":"S");
            		}
            		if (msg==null) {
                    	String name =_trainNameBox.getText();
                    	if (name==null || name.trim().length()==0) {
                    		name = addr;
                    	}
                    	String s = (""+Math.random()).substring(2);
                    	warrant = new Warrant("IW"+s, "NX("+addr+")");
                    	warrant.setDccAddress( new DccLocoAddress(dccNum, isLong));
                    	warrant.setTrainName(name);            			
            		}
                } catch (NumberFormatException nfe) {
                    msg = Bundle.getMessage("BadDccAddress", addr);
                }
            } else {
            	msg = Bundle.getMessage("BadDccAddress", addr);
            }
            if (msg==null) {
            	msg = makeCommands(warrant);           	
            }
            if (msg==null) {
                warrant.setBlockOrders(getOrders());
            }
        }
        if (msg==null) {
        	msg = _parent.runTrain(warrant);           	
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            warrant = null;
        } else {
        	_parent.getModel().addNXWarrant(warrant);
        	_parent.getModel().fireTableDataChanged();
        	if (_haltStart.isSelected()) {
        		WarrantTableFrame._defaultHaltStart = true;
            	class Halter implements Runnable {
            		Warrant war;
            		Halter (Warrant w) {
            			war = w;
            		}
            		public void run() {
                    	int limit = 0;  
                    	try {
                        	while (!war.controlRunTrain(Warrant.HALT) && limit<3000) {
                        		Thread.sleep(200);
                        		limit += 200;
                        	}            		
                    	} catch (InterruptedException e) {
                    		war.controlRunTrain(Warrant.HALT);
                    	}           			
            		}
            	}
            	Halter h = new Halter(warrant);
            	new Thread(h).start();
         	} else {
        		WarrantTableFrame._defaultHaltStart = false;         		
         	}
        	_parent.scrollTable();
        	dispose();
        	_parent.closeNXFrame();           	
        }
    }
    private void runManual() {
    	String name =_trainNameBox.getText();
    	if (name==null || name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("noTrainName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
    	}
    	String s = (""+Math.random()).substring(2);
    	Warrant warrant = new Warrant("IW"+s, name);
    	warrant.setTrainName(name);            			
        warrant.setBlockOrders(getOrders());
    	_parent.getModel().addNXWarrant(warrant);
    	_parent.getModel().fireTableDataChanged();    	
    	_parent.scrollTable();
    	dispose();
    	_parent.closeNXFrame();           	
    }
    
    private String makeCommands(Warrant w) {
        String speed = _speedBox.getText();
        WarrantTableFrame._defaultSpeed = speed;
        String interval = _rampInterval.getText();
    	float f = 0; 
    	int time = 4000;
        try {
        	f = Float.parseFloat(speed);
        	if (f>1.0 || f<0) {
                return Bundle.getMessage("badSpeed");            	            		
        	}
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("badSpeed");            	
        }
        try {
        	float t = Float.parseFloat(interval)*1000;
        	if (t>60000 || t<0) {
                return Bundle.getMessage("invalidNumber");            	            		
        	}
        	time = Math.round(t);
        } catch (NumberFormatException nfe) {
        	time = 4000;            	
        }
        WarrantTableFrame._defaultIntervalTime = interval;
    	List<BlockOrder> orders = getOrders();
    	String blockName = orders.get(0).getBlock().getDisplayName();
    	w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(500, "Forward", 
    										(_forward.isSelected()?"true":"false"), blockName));
    	w.addThrottleCommand(new ThrottleSetting(0, "Speed", Float.toString(f/4), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/8), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(f/2), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/4), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(7*f/8), blockName));        		
    	if (orders.size() > 2) {
        	blockName = orders.get(1).getBlock().getDisplayName();
    		w.addThrottleCommand(new ThrottleSetting(1000, "NoOp", "Enter Block", blockName));
        	if (orders.size() > 3) {
            	w.addThrottleCommand(new ThrottleSetting(0, "Speed", speed, blockName));            		
            	for (int i=2; i<orders.size()-2; i++) {
            		w.addThrottleCommand(new ThrottleSetting(5000, "NoOp", "Enter Block", 
            									orders.get(i).getBlock().getDisplayName()));        		
            	}
            	blockName = orders.get(orders.size()-2).getBlock().getDisplayName();
        		w.addThrottleCommand(new ThrottleSetting(5000, "NoOp", "Enter Block", blockName));        		            		
        	} else {
            	blockName = orders.get(orders.size()-2).getBlock().getDisplayName();
        	}
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", Float.toString(7*f/8), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/4), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(5*f/8), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(f/2), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/8), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(f/3), blockName));        		
     	}
    	// Last block
    	OBlock block = orders.get(orders.size()-1).getBlock();
    	blockName = block.getDisplayName();
		w.addThrottleCommand(new ThrottleSetting(0, "NoOp", "Enter Block", blockName));        		
    	if (_stageEStop.isSelected()) {
    		WarrantTableFrame._defaultEStop = true;
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
        	f = 0;
    	} else {
    		WarrantTableFrame._defaultEStop = false;
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", Float.toString(f/4), blockName));
        	f = Math.max(block.getLengthIn()-4, 0)*200;
     	}
    	w.addThrottleCommand(new ThrottleSetting((int)f, "Speed", "0.0", blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(500, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(500, "F0", "false", blockName));
       	return null;    		
   }
 
	boolean makeAndRunWarrant() {
        int depth = 10;
        String msg = null;
        try {
        	WarrantTableFrame._defaultSearchdepth = _searchDepth.getText();
            depth = Integer.parseInt(_searchDepth.getText());
        } catch (NumberFormatException nfe) {
        	depth = 10;
        }
        msg = findRoute(depth);
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
	}
	
    static Logger log = LoggerFactory.getLogger(NXFrame.class.getName());
}

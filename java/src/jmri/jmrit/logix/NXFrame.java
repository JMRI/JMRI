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

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    JTextField  _speedBox = new JTextField();
    JCheckBox	_forward = new JCheckBox();
     JCheckBox	_stageEStop = new JCheckBox();    
    JTextField _rampInterval = new JTextField();
    JTextField _searchDepth = new JTextField();
    int _clickCount;

    NXFrame(WarrantTableFrame parent) {
		super();
		_parent = parent;
		_clickCount = 0;
		setTitle(Bundle.getMessage("AutoWarrant"));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10,10));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        doSize(_dccNumBox, 50, 20);
//        doSize(_speedBox, 50, 20);
//        doSize(_searchDepth, 50, 20);
        panel.add(Box.createVerticalGlue());
        panel.add(makeBlockPanels());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(WarrantFrame.makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _speedBox, "Speed", true));
        pp.add(WarrantFrame.makeBoxPanel(false, _forward, "forward"));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeBoxPanel(false, _stageEStop, "StageEStop"));
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
        _stageEStop.setSelected(false);
        _speedBox.setText("0.5");
        _rampInterval.setText("4.0");
        _searchDepth.setText("10");
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
        setLocation(parent.getLocation().x+200, parent.getLocation().y+100);
        setAlwaysOnTop(true);
        pack();
        setVisible(true);      		
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
    	String s = (""+Math.random()).substring(2);
    	Warrant warrant = new Warrant("IW"+s, "NX"+s);
        if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
            msg = Bundle.getMessage("NoLoco");
        } else {
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
                        isLong = (ch=='0' || dccNum>255);  // leading zero means long
                        addr = addr + (isLong?"L":"S");
            		}
            		if (msg==null) {
                    	warrant.setDccAddress( new DccLocoAddress(dccNum, isLong));
                    	warrant.setTrainName(addr);            			
                    	warrant.setUserName("NX("+addr+")");            			
            		}
                } catch (NumberFormatException nfe) {
                    msg = Bundle.getMessage("BadDccAddress", addr);
                }
            }
        }
        warrant.addBlockOrders(getOrders());
        if (msg==null) {
        	msg = makeCommands(warrant);           	
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
        	_parent.scrollTable();
        	dispose();
        	_parent.closeNXFrame();           	
        }
    }
    
    private String makeCommands(Warrant w) {
        String speed = _speedBox.getText();
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
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
        	f = 0;
    	} else {
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", Float.toString(f/4), blockName));
        	f = Math.max(block.getLengthIn()-4, 0)*200;
     	}
    	w.addThrottleCommand(new ThrottleSetting((int)f, "Speed", "0.0", blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(500, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(500, "F0", "false", blockName));
       	return null;    		
   }
 
    void mouseClickedOnBlock(OBlock block) {
    	_clickCount++;
    	switch (_clickCount) {
    		case 1:
        		_originBlockBox.setText(block.getDisplayName());
        		setOriginBlock();
        		break;
    		case 2:
        		_destBlockBox.setText(block.getDisplayName());
                setDestinationBlock();
                break;
    		case 3 :
    			_viaBlockBox.setText(block.getDisplayName());
                setViaBlock();
                break;
    		case 4:
        		_avoidBlockBox.setText(block.getDisplayName());
                setAvoidBlock();
                break;
			default:
				_clickCount= 0;       				
    	}
    }
	boolean makeAndRunWarrant() {
        int depth = 10;
        String msg = null;
        try {
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

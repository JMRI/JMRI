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
    JTextField  _nameBox = new JTextField();
    JTextField  _maxSpeedBox = new JTextField();
    JTextField  _minSpeedBox = new JTextField();
    JRadioButton _forward = new JRadioButton(Bundle.getMessage("forward"));
    JRadioButton _reverse = new JRadioButton(Bundle.getMessage("reverse"));
    JCheckBox	_stageEStop = new JCheckBox();    
    JCheckBox	_haltStart = new JCheckBox();
    JCheckBox	_addTracker = new JCheckBox();
    JTextField _rampInterval = new JTextField(6);
    JTextField _numSteps = new JTextField(6);
    JTextField _searchDepth = new JTextField();
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
    JPanel		_autoRunPanel;
    JPanel		_manualPanel;

    private static NXFrame _instance;
    
    static NXFrame getInstance() {
    	if (_instance==null) {
    		_instance = new NXFrame();
    	}
    	_instance.setVisible(true);
    	_instance._dccNumBox.setText(null);
    	_instance._trainNameBox.setText(null);
    	_instance._nameBox.setText(null);
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
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.add(WarrantFrame.makeTextBoxPanel(false, _maxSpeedBox, "MaxSpeed", true));        
        p1.add(WarrantFrame.makeTextBoxPanel(false, _minSpeedBox, "MinSpeed", true));        
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(WarrantFrame.makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
        p2.add(WarrantFrame.makeTextBoxPanel(false, _trainNameBox, "TrainName", true));
        
        _autoRunPanel = new JPanel();
        _autoRunPanel.setLayout(new BoxLayout(_autoRunPanel, BoxLayout.Y_AXIS));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p1);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p2);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        _autoRunPanel.add(pp);
        bg = new ButtonGroup();
        bg.add(_forward);
        bg.add(_reverse);
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        ppp.add(_forward);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        ppp.add(_reverse);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        _autoRunPanel.add(ppp);
        _autoRunPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeBoxPanel(false, _stageEStop, "StageEStop"));
        ppp.add(WarrantFrame.makeBoxPanel(false, _haltStart, "HaltAtStart"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _rampInterval, "rampInterval", true));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _numSteps, "rampSteps", true));
//        ppp.add(WarrantFrame.makeBoxPanel(false, _addTracker, "AddTracker"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        _autoRunPanel.add(pp);
       
        _manualPanel = new JPanel();
        _manualPanel.setLayout(new BoxLayout(_manualPanel, BoxLayout.X_AXIS));
        _manualPanel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        _manualPanel.add(WarrantFrame.makeTextBoxPanel(false, _nameBox, "TrainName", true));
        _manualPanel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        
        panel.add(_autoRunPanel);
        panel.add(_manualPanel);
		_manualPanel.setVisible(false);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        
        pp = new JPanel();
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _searchDepth, "SearchDepth", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        _forward.setSelected(true);
        _stageEStop.setSelected(WarrantTableFrame._defaultEStop);
        _haltStart.setSelected(WarrantTableFrame._defaultHaltStart);
        _addTracker.setSelected(WarrantTableFrame._defaultAddTracker);
        _maxSpeedBox.setText(WarrantTableFrame._defaultMaxSpeed);
        _minSpeedBox.setText(WarrantTableFrame._defaultMinSpeed);
        _rampInterval.setText(WarrantTableFrame._defaultIntervalTime);
        _numSteps.setText(WarrantTableFrame._defaultNumSteps);
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
    	if (enable) {
    		_manualPanel.setVisible(false);
    		_autoRunPanel.setVisible(true);
    	} else {
    		_manualPanel.setVisible(true);
    		_autoRunPanel.setVisible(false);    		
    	}
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
		if (msg==null) {
        	String name =_trainNameBox.getText();
        	if (name==null || name.trim().length()==0) {
        		name = _addr;
        	}
        	String s = (""+Math.random()).substring(2);
        	warrant = new Warrant("IW"+s, "NX("+_addr+")");
        	warrant.setDccAddress( new DccLocoAddress(_dccNum, _isLong));
        	warrant.setTrainName(name);
        	
        	msg = makeCommands(warrant);           	
            if (msg==null) {
                warrant.setBlockOrders(getOrders());
            }
		}
        if (msg==null) {
        	_parent.getModel().addNXWarrant(warrant);	//need to catch propertyChange at start
        	msg = _parent.runTrain(warrant);
        	if (msg!=null) {
        		_parent.getModel().removeNXWarrant(warrant);
        	}
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            warrant = null;
        } else {
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
    	String name =_nameBox.getText();
    	if (name==null || name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("noTrainName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
    	}
    	String s = (""+Math.random()).substring(2);
    	Warrant warrant = new Warrant("IW"+s, "NX("+name+")");
    	warrant.setTrainName(name);            			
        warrant.setRoute(0, getOrders());
    	_parent.getModel().addNXWarrant(warrant);
    	warrant.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
    	_parent.scrollTable();
    	dispose();
    	_parent.closeNXFrame();           	
    }
    
    public static final float SCALE = 87.1f;
    static final float FACTOR = 0.779f;
    
    private String makeCommands(Warrant w) {
    	float maxSpeed = 0; 
    	float minSpeed = 0; 
        try {
            String speedText = _maxSpeedBox.getText();
            WarrantTableFrame._defaultMaxSpeed = speedText;
            maxSpeed = Float.parseFloat(speedText);
            if (maxSpeed>1.0 || maxSpeed<0) {
           	 return Bundle.getMessage("badSpeed");            	            		
            }
            speedText = _minSpeedBox.getText();
            WarrantTableFrame._defaultMinSpeed = speedText;
            minSpeed = Float.parseFloat(speedText);
            if (minSpeed>1.0 || minSpeed<0 || minSpeed>=maxSpeed) {
           	 return Bundle.getMessage("badSpeed");            	            		
            }
        } catch (NumberFormatException nfe) {
       	 return Bundle.getMessage("badSpeed");            	
        }
        int numSteps = 8;
        float time = 4000;
        try {
            String text = _rampInterval.getText();
            WarrantTableFrame._defaultIntervalTime = text;
        	time = Float.parseFloat(text)*1000;
        	if (time>60000 || time<0) {
                return Bundle.getMessage("invalidNumber");            	            		
        	}
            text = _numSteps.getText();
            WarrantTableFrame._defaultNumSteps = text;
            numSteps = Integer.parseInt(text);
        	if (numSteps>100 || time<0) {
                return Bundle.getMessage("invalidNumber");            	            		
        	}
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("invalidNumber");            	            		
        }
        float delta = (maxSpeed - minSpeed)/(numSteps-1);

        List<BlockOrder> orders = getOrders();
    	String blockName = orders.get(0).getBlock().getDisplayName();
    	w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "Forward", 
    										(_forward.isSelected()?"true":"false"), blockName));

    	// distance required for N speed changes of equal increments of velocity, v, starting with speed, V. is proportional to
    	//  t[V + V=v + V+2v + ....... + V+nv}] = t[(n+1)V + d(n+1)n/2] = t(n+1)(V+nd/2) = D
    	// distance required for 'minSpeed' to top speed in 'numSteps' steps
    	float maxRampLength = time*(numSteps + 1)*(minSpeed + delta*numSteps/2)/(FACTOR*SCALE*2);
    	OBlock block = orders.get(0).getBlock();
    	float totalLen = block.getLengthIn()/2;		// estimated distance of the route
    	int orderSize = orders.size();
    	for (int i=1; i<orderSize-1; i++) {
    		float len = orders.get(i).getBlock().getLengthIn();
    		if (len<=0) {
    			// intermediate blocks should not be zero
    			log.warn("block \""+orders.get(i).getBlock().getDisplayName()+"\" has length zero. Using "+
    					maxRampLength+ " for actual length.");
    			len = maxRampLength;
    		}
    		totalLen += len;
    	}
    	totalLen += orders.get(orderSize-1).getBlock().getLengthIn();		// OK if user has set to 0

    	// start train
		int idx = 0;		// block index
 		block = orders.get(0).getBlock();
 		float blockLen = block.getLengthIn();
 		blockName = block.getDisplayName();
		float curSpeed = rampSpeed(w, (int)time, 0, minSpeed, blockName, 1);		
		int curSteps = 1;
		int steps = 1;
		
 		float rampLength = maxRampLength;		// actual ramp distance to use.
 		// adjust for room
		float b = 2*minSpeed+delta;
    	if (totalLen <= 2*maxRampLength) {
    		rampLength = totalLen/2;
    		numSteps = (int)((Math.sqrt(b*b + 8*delta*(SCALE*FACTOR*rampLength/time-minSpeed)) - b)/(2*delta));
    		steps = (int)((Math.sqrt(b*b + 8*delta*(SCALE*FACTOR*blockLen/time-minSpeed)) - b)/(2*delta));
    		if (curSteps+steps > numSteps) {
    			steps = numSteps - curSteps;
    		} else if (steps<1) {
    			steps = 1;
    		}
    	} else {	// enough room to run to maxSpeed
    		steps = numSteps-1;
    	}
       	if (log.isDebugEnabled()) log.debug("Route length= "+totalLen+" uses "+numSteps+" speed steps of delta= "+
       			delta+" for rampLength = "+rampLength);
       	
       	// start ramp
		float delay	= 0;			// time needed to complete running the block			
		float remainingLen = 0;		// length remaining in route
		for (;;) {
			curSteps += steps;
			float dist = time*(steps + 1)*(curSpeed + steps*delta/2)/(FACTOR*SCALE*2);
        	if (log.isDebugEnabled()) log.debug("Ramp up in block \""+blockName+"\" from start speed "+curSpeed+
        			". "+steps+ " steps used in distance "+dist);
			curSpeed = rampSpeed(w, (int)time, curSpeed, delta, blockName, steps);
       		totalLen -= blockLen;
       		
    		for (int i=idx+1; i<orderSize; i++) {
    			float len = orders.get(i).getBlock().getLengthIn();
        		if (len<=0 && i<orderSize-1)  {
        			len = maxRampLength;
        		}
    			remainingLen += len;
    		}
    		if (remainingLen<rampLength) {
            	if (log.isDebugEnabled()) log.debug("Remaininng route length "+remainingLen+" less than ramp length "+rampLength);
    			blockLen = Math.max(blockLen-dist, 0);
    			remainingLen += blockLen;
    			// start ramp down in this block
    			break;
    		}			
       		remainingLen = 0;
     		block = orders.get(++idx).getBlock();
     		blockName = block.getDisplayName();
    		blockLen = block.getLengthIn();
    		if (blockLen<=0 && idx<orderSize-1)  {
    			blockLen = maxRampLength;
    		}
    		w.addThrottleCommand(new ThrottleSetting((int)time, "NoOp", "Enter Block", blockName));
    		if (curSteps==numSteps) {
    			break;
    		}
    		b = 2*curSpeed+delta;
			steps = (int)((Math.sqrt(b*b + 8*delta*(SCALE*FACTOR*blockLen/time-curSpeed)) - b)/(2*delta));
			if (curSteps+steps > numSteps) {
				steps = numSteps - curSteps;
			} else if (steps<1) {
				steps = 1;
			}
 		}
       	if (log.isDebugEnabled()) log.debug("Ramp Up done at block \""+blockName+"\" curSteps= "+curSteps+
       			", curSpeed="+curSpeed+" remainingLen="+remainingLen+", rampLength= "+rampLength);
			
		// run through mid route at max speed
		while (idx<orderSize-2) {
			block = orders.get(idx+1).getBlock();
     		float len = block.getLengthIn();
    		if (len<=0) {
    			len = maxRampLength;
    		}
    		if (totalLen < rampLength+len ) {
    			break;
    		}
    		idx++;
     		blockName = block.getDisplayName();
    		delay = len*FACTOR*SCALE/curSpeed;
    		w.addThrottleCommand(new ThrottleSetting((int)delay, "NoOp", "Enter Block", blockName));        		
    		totalLen -= len;   			
		}
		
		// Ramp down.  use negative delta
		float delayDistance = 0;
		if (remainingLen!=0) {
			delayDistance = remainingLen-rampLength;
			blockLen = Math.max(blockLen-delayDistance, 0);			
		} else {
     		remainingLen = 0;
    		for (int i=idx+1; i<orderSize; i++) {
    			float len = orders.get(i).getBlock().getLengthIn();
        		if (len<=0 && i<orderSize-1)  {
        			len = maxRampLength;
        		}
    			remainingLen += len;
    		}
    		if (idx<orderSize-1) {
         		block = orders.get(++idx).getBlock();
         		blockName = block.getDisplayName();
         		blockLen = block.getLengthIn();
         		if (blockLen<=0 && idx<orderSize-1) {
         			blockLen = maxRampLength;
         		}    			
        		delay = blockLen*FACTOR*SCALE/curSpeed;
        		w.addThrottleCommand(new ThrottleSetting((int)delay, "NoOp", "Enter Block", blockName));         			
    			delayDistance = Math.max(blockLen-rampLength, 0);
    		} else {
    			delayDistance = Math.max(blockLen-rampLength, 0);
    			blockLen = Math.max(blockLen-delayDistance, 0);			    			
    		}
		}
       	if (log.isDebugEnabled()) log.debug("Start RampDown, at block \""+blockName+"\" curSteps= "+curSteps+
       			", blockLen= "+blockLen+", delayDistance="+delayDistance+" remainingLen="+remainingLen+" totalLen= "+totalLen);
		curSteps--;		// another step to 0 is after this loop
		while (curSteps>0) {
			delay = delayDistance*FACTOR*SCALE/curSpeed;
 			curSpeed = rampSpeed(w, (int)delay, curSpeed, -delta, blockName, 1);
			rampLength -= delayDistance;
			curSteps--;
	       	if (log.isDebugEnabled()) log.debug("Delayed speed change to "+curSpeed+" by "+(int)delay+" ms in delayDistance= "+delayDistance);
			// doesn't solve to real number if blockLen is too large
//    		b = 2*curSpeed+delta;
//			steps = (int)((Math.sqrt(b*b + 8*delta*(SCALE*FACTOR*blockLen/time-curSpeed)) - b)/(2*delta));
			// fit steps to available length
			steps = 0;
			delayDistance = 0;		// use this for ramp down distance
			do {
				float len = (curSpeed - steps++*delta);
				if (len<=0) {
					steps = 0;
					break;
				}
				delayDistance += len*time/(SCALE*FACTOR);
		       	if (log.isDebugEnabled()) log.debug("delayDistance= "+delayDistance+"  after "+steps+ " steps at "+blockName);
			} while (delayDistance<=blockLen && steps<curSteps);
     		if (idx==orderSize-1) {
     			// at last block
     	    	if (_stageEStop.isSelected()) {
     	    		WarrantTableFrame._defaultEStop = true;
     	        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
     	        	time = 0;
     	    	} else {
     	    		WarrantTableFrame._defaultEStop = false;
     	        	if (log.isDebugEnabled()) log.debug("Ramp down in block \""+blockName+"\" using "+steps+" steps of distance "+
     	        			delayDistance+" in blockLen "+blockLen+" from start speed of "+curSpeed);
     	        	if (steps>1) {
         				curSpeed = rampSpeed(w, (int)time, curSpeed, -delta, blockName, steps);     	        		
     	        	} else {
         	        	time = Math.max((blockLen-delayDistance), 0)*FACTOR*SCALE/(curSpeed);     	        		
     	        	}
    	     	}
     	    	break;
     		} else {
     			float dist = 0;
     			if (curSteps>1) {
     				if (curSteps-steps<1) {
     					steps = curSteps-1;
     				}
        			dist = time*(steps + 1)*(curSpeed + steps*delta/2)/(FACTOR*SCALE*2);
                	if (log.isDebugEnabled()) log.debug("Ramp down in block \""+blockName+"\" using "+steps+" steps of distance "+
     	        			dist+" in blockLen "+blockLen+" from start speed of "+curSpeed);
        			curSpeed = rampSpeed(w, (int)time, curSpeed, -delta, blockName, steps);    				
         			rampLength -= delayDistance;
        			curSteps -= steps;
     			}
         		block = orders.get(++idx).getBlock();
         		blockLen = block.getLengthIn();
         		if (blockLen<=0 && idx<orderSize-1) {
         			blockLen = maxRampLength;
         		}
         		blockName = block.getDisplayName();
         		remainingLen = 0;
        		for (int i=idx; i<orderSize; i++) {
        			float len = orders.get(i).getBlock().getLengthIn();
            		if (len<=0 && i<orderSize-1)  {
            			len = maxRampLength;
            		}
        			remainingLen += len;
        		}
    			delayDistance = Math.max(remainingLen-rampLength, 0);
    			blockLen = Math.max(blockLen-delayDistance, 0);			
        		w.addThrottleCommand(new ThrottleSetting((int)delay, "NoOp", "Enter Block", blockName));				     			
     		}
		}
		
    	w.addThrottleCommand(new ThrottleSetting((int)time, "Speed", "0.0", blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(3000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F0", "false", blockName));
    	if (_addTracker.isSelected()) {
    		WarrantTableFrame._defaultAddTracker = true;
    	   	w.addThrottleCommand(new ThrottleSetting(10, "START TRACKER", "", blockName));
    	} else {
    		WarrantTableFrame._defaultAddTracker = false;
    	}
    	return null;
    }
    private float rampSpeed(Warrant w, int time, float speed, float delta, String blockName, int incr) {
     	for (int i=0; i<incr; i++) {
    		speed += delta;
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(speed), blockName));        		    		
    	}
    	return speed;
    }
    
    private String _addr;
    private int _dccNum;
    private boolean  _isLong;
 
	boolean makeAndRunWarrant() {
        int depth = 10;
        String msg = null;
    	_addr = _dccNumBox.getText();
        if (_addr!= null && _addr.length() != 0) {
        	_addr = _addr.toUpperCase().trim();
        	_isLong = false;
    		Character ch = _addr.charAt(_addr.length()-1);
    		try {
        		if (!Character.isDigit(ch)) {
        			if (ch!='S' && ch!='L' && ch!=')') {
        				msg = Bundle.getMessage("BadDccAddress", _addr);
        			}
        			if (ch==')') {
                    	_dccNum = Integer.parseInt(_addr.substring(0, _addr.length()-3));
                    	ch = _addr.charAt(_addr.length()-2);
                    	_isLong = (ch=='L');
        			} else {
                    	_dccNum = Integer.parseInt(_addr.substring(0, _addr.length()-1));        				
                    	_isLong = (ch=='L');
        			}
        		} else {
            		_dccNum = Integer.parseInt(_addr);
            		ch = _addr.charAt(0);
            		_isLong = (ch=='0' || _dccNum>127);  // leading zero means long
                    _addr = _addr + (_isLong?"L":"S");
        		}
            } catch (NumberFormatException nfe) {
                msg = Bundle.getMessage("BadDccAddress", _addr);
            }
        } else {
        	msg = Bundle.getMessage("BadDccAddress", _addr);
        }
        if (msg==null) {
        	try {
            	WarrantTableFrame._defaultSearchdepth = _searchDepth.getText();
                depth = Integer.parseInt(_searchDepth.getText());
            } catch (NumberFormatException nfe) {
            	depth = 10;
            }
            msg = findRoute(depth);
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
	}
	
    static Logger log = LoggerFactory.getLogger(NXFrame.class.getName());
}

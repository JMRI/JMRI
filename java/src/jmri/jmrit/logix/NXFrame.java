package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JMenuBar;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = -8971792418011219112L;
	WarrantTableFrame 	_parent;
    // Throttle setting = T.   Velocity(in/ms) = V.  V = T/S*F
    private float _scale = 87.1f;
    static final float FACTOR = 0.8117f;
    
    JTextField  _dccNumBox = new JTextField();
    JTextField  _trainNameBox = new JTextField();
    JTextField  _nameBox = new JTextField();
    JTextField  _maxSpeedBox = new JTextField();
    JTextField  _minSpeedBox = new JTextField();
    JRadioButton _forward = new JRadioButton(Bundle.getMessage("forward"));
    JRadioButton _reverse = new JRadioButton(Bundle.getMessage("reverse"));
    JCheckBox	_stageEStop = new JCheckBox();    
    JCheckBox	_haltStartBox = new JCheckBox();
//    JCheckBox	_addTracker = new JCheckBox();
    JTextField _rampInterval = new JTextField(6);
    JTextField _numStepsBox = new JTextField(6);
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
    JPanel		_autoRunPanel;
    JPanel		_manualPanel;
	// Session persistent defaults for NX warrants
	static boolean _eStop = false;
	static boolean _haltStart = false;
//	static boolean _addTracker = false;
	static float _maxSpeed = 0.5f;
	static float _minSpeed = 0.075f;
	static float _intervalTime = 2000f;
	static int _numSteps = 15;
    static boolean _firstInstance = true;
    static Point _loc = null;
    static Dimension _dim = null;

    private static NXFrame _instance;
    
    static public NXFrame getInstance() {
    	if (_instance==null) {
    		_instance = new NXFrame();
    	}
    	_instance._dccNumBox.setText(null);
    	_instance._trainNameBox.setText(null);
    	_instance._nameBox.setText(null);
    	_instance.clearRoute();
    	return _instance;
    }

    private NXFrame() {
		super();
		_parent = WarrantTableFrame.getInstance();
    }
    
    public void init() {
		makeMenus();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10,10));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue());
        panel.add(makeBlockPanels());
        panel.add(searchDepthPanel(false));
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
        ppp.add(WarrantFrame.makeBoxPanel(false, _haltStartBox, "HaltAtStart"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _rampInterval, "rampInterval", true));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _numStepsBox, "rampSteps", true));
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
        
        _forward.setSelected(true);
        _stageEStop.setSelected(_eStop);
        _haltStartBox.setSelected(_haltStart);
//        _addTracker.setSelected(WarrantTableFrame._defaultAddTracker);
        _maxSpeedBox.setText(Float.toString(_maxSpeed));
        _minSpeedBox.setText(Float.toString(_minSpeed));
        _rampInterval.setText(Float.toString(_intervalTime/1000));
        _numStepsBox.setText(Integer.toString(_numSteps));
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
                    	closeFrame();
                    }
                });
        p.add(button);
        panel.add(p);
        mainPanel.add(panel);
        getContentPane().add(mainPanel);
        addWindowListener(new java.awt.event.WindowAdapter() {
        	@Override
            public void windowClosing(java.awt.event.WindowEvent e) {
            	closeFrame();
            }
        });
        if (_firstInstance) {
            setLocation(_parent.getLocation().x+200, _parent.getLocation().y+100);
            _firstInstance = false;
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setAlwaysOnTop(true);
        pack();
//        setVisible(false);      		
	}
    private void makeMenus() {
		setTitle(Bundle.getMessage("AutoWarrant"));
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.NXWarrant", true);
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
    
	@Override
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
	@Override
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
        	if (_haltStartBox.isSelected()) {
        		_haltStart = true;
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
        		_haltStart = false;         		
         	}
        	_parent.scrollTable();
        	closeFrame();
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
    	closeFrame();
    }
    
    private void closeFrame() {
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
    	dispose();
    	_parent.closeNXFrame();           	    	
    }

    public void setNumSteps(int s) {
    	_numSteps = s;
    }
    public int getNumSteps() {
    	return _numSteps;
    }
    public void setMaxSpeed(float s) {
    	_maxSpeed = s;
    }
    public float getMaxSpeed() {
    	return _maxSpeed;
    }
    public void setMinSpeed(float s) {
    	_minSpeed = s;
    }
    public float getMinSpeed() {
    	return _minSpeed;
    }
    public void setTimeInterval(float s) {
    	_intervalTime = s;
    }
    public float getTimeInterval() {
    	return _intervalTime;
    }
    public void setStartHalt(boolean s) {
    	_haltStart = s;
    }
    public boolean getStartHalt() {
    	return _haltStart;
    }
    public void setScale(float s) {
    	_scale = s;
    }
    public float getScale() {
    	return _scale;
    }
    
    private String makeCommands(Warrant w) {
    	float maxSpeed = 0; 
    	float minSpeed = 0; 
        try {
            String speedText = _maxSpeedBox.getText();
            maxSpeed = Float.parseFloat(speedText);
            if (maxSpeed>1.0 || maxSpeed<0) {
            	return Bundle.getMessage("badSpeed");            	            		
            }
            _maxSpeed = maxSpeed;
            speedText = _minSpeedBox.getText();
            minSpeed = Float.parseFloat(speedText);
            if (minSpeed>1.0 || minSpeed<0 || minSpeed>=maxSpeed) {
            	return Bundle.getMessage("badSpeed");            	            		
            }
            _minSpeed = minSpeed;
        } catch (NumberFormatException nfe) {
        	return Bundle.getMessage("badSpeed");            	
        }
        int numSteps = 8;
        float time = 4000;
        try {
            String text = _rampInterval.getText();
        	time = Float.parseFloat(text)*1000;
        	if (time>60000 || time<0) {
                return Bundle.getMessage("invalidNumber");            	            		
        	}
        	_intervalTime = time;
        	text = _numStepsBox.getText();
        	numSteps = Integer.parseInt(text);
        	if (numSteps>100 || time<0) {
                return Bundle.getMessage("invalidNumber");            	            		
        	}
        	_numSteps = numSteps;
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("invalidNumber");            	            		
        }
        float delta = (maxSpeed - minSpeed)/(numSteps-1);

        List<BlockOrder> orders = getOrders();
 		OBlock block = orders.get(0).getBlock();
    	String blockName = block.getDisplayName();
    	w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "Forward", 
    										(_forward.isSelected()?"true":"false"), blockName));

    	float maxRampLength = (minSpeed*time/2)/(FACTOR*_scale);
    	// assume linear speed change
    	float curSpeed = minSpeed;
    	for (int step = 1; step <numSteps; step++) {
    		maxRampLength += (curSpeed + delta/2)*time/(FACTOR*_scale);
    		curSpeed += delta;
    	}

    	float totalLen = block.getLengthIn()/2;		// estimated distance of the route
    	int orderSize = orders.size();
    	for (int i=1; i<orderSize-1; i++) {
    		float len = orders.get(i).getBlock().getLengthIn();
    		if (len<=0) {
    			// intermediate blocks should not be zero
    			log.warn(w.getDisplayName()+" route through block \""+orders.get(i).getBlock().getDisplayName()+"\" has length zero. Using "+
    					maxRampLength+ " for actual length.");
    			len = maxRampLength;
    		}
    		totalLen += len;
    	}
    	totalLen += orders.get(orderSize-1).getBlock().getLengthIn()/2;		// OK if user has set to 0

 		float rampLength = maxRampLength;		// actual ramp distance to use.
 		// adjust for room
    	if (totalLen <= 2*maxRampLength) {
    		rampLength = (minSpeed*time/2)/(FACTOR*_scale);
    		int steps = 0;
        	curSpeed = minSpeed;
        	float dist = 0;
        	for (int step = 1; step < numSteps; step++) {
        		dist = (curSpeed + delta/2)*time/(FACTOR*_scale);
        		if (rampLength + dist < totalLen/2) {
           			rampLength += dist;
           			steps++;
            		curSpeed += delta;
       			} else {
       				break;
       			}
        	}
        	curSpeed -= delta;
        	rampLength -= (curSpeed + delta/2)*time/(FACTOR*_scale);
        	numSteps = steps;
    	}
       	if (log.isDebugEnabled()) log.debug("Route length= "+totalLen+" uses "+numSteps+" speed steps of delta= "+
       			delta+" for rampLength = "+rampLength+" (maxRampLength= "+maxRampLength+")");
    	if (log.isDebugEnabled()) {
    		float rampDownLen = 0;
    		log.debug("curSpeed= "+curSpeed);
    		for (int step=numSteps; step>1; step--) {
    			rampDownLen += (curSpeed - delta/2)*time/(FACTOR*_scale);
    			curSpeed -= delta;
    		}
    		rampDownLen += (curSpeed/2)*time/(FACTOR*_scale);
    		log.debug("rampDownLen= "+rampDownLen+" uses "+numSteps+" speed steps of delta= "+
           			delta+" for rampLength = "+rampLength+" next to last curSpeed= "+curSpeed);
    	}
    	       	
		int idx = 0;		// block index
 		float blockLen = block.getLengthIn()/2;

		float noopTime = 0;			// ms time for entry into next block
		float curDistance = 0;		// distance traveled in current block
    	float remRamp = rampLength;
    	// start train
		float speedTime = time;		// ms time to complete speed step in next block
		curSpeed = minSpeed;
    	w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
		int curSteps = 1;
    	
    	boolean start = true;
		while (curSteps < numSteps) {
			if (start) {
				curDistance = (minSpeed*time/2)/(FACTOR*_scale);
	    		remRamp -= curDistance;
				start = false;
			}
			int steps = 0;
			float speed = curSpeed;
			// Assume linear speed change
			while (curSteps+steps < numSteps) {				
        		float dist = (speed + delta/2)*time/(FACTOR*_scale);
        		if (curDistance + dist < blockLen) {
        			curDistance += dist;
        			speed += delta;
        			steps++;
        			remRamp -= dist;
       			} else {
       				break;
       			}
			}
			if (steps>0) {
		    	curSpeed += delta;
		    	w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
				if (steps>1) {
					curSpeed = rampSpeed(w, (int)time, curSpeed, delta, blockName, steps-1);	//steps<=0 OK, no speed change
				}
		    	if (log.isDebugEnabled()) log.debug("Continue Ramp Up at "+(int)speedTime+"ms in block \""+blockName+
		    			"\" to speed "+curSpeed+" after "+steps+" steps to reach curDistance= "+curDistance+", remRamp= "+remRamp);
				curSteps += steps;
			}

    		totalLen -= blockLen;
    		if (curSteps==numSteps) {    			
    			noopTime = (blockLen-curDistance)*(FACTOR*_scale)/(curSpeed);  // constant
    			speedTime = 0;
    			curDistance = 0;
    		} else {
    			noopTime = (blockLen-curDistance)*(FACTOR*_scale)/(curSpeed-delta/2); // accelerating
    			speedTime = time - noopTime;
    			curDistance = (curSpeed-delta/2)*speedTime/(FACTOR*_scale);
    		}
        	if (log.isDebugEnabled()) log.debug("Leave RampUp block \""+blockName+"\" noopTime= "+noopTime+
        			", in distance="+curSpeed*noopTime/(FACTOR*_scale)+", blockLen= "+blockLen+
        			", remRamp= "+remRamp);
     		block = orders.get(++idx).getBlock();
     		blockName = block.getDisplayName();
    		blockLen = block.getLengthIn();
    		if (blockLen<=0 && idx<orderSize-1)  {
    			blockLen = rampLength;
    		}
    		w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
    		if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
 		}
       	if (log.isDebugEnabled()) log.debug("Ramp Up done at block \""+blockName+"\" curSteps= "+curSteps+
       			", curSpeed="+curSpeed+", blockLen= "+blockLen+" totalLen= "+totalLen+", rampLength= "+
       			rampLength+", remRamp= "+remRamp);
			
		// run through mid route at max speed
		while (idx<orderSize-1) {
       		if (totalLen-blockLen <= rampLength) {
       			// Start ramp down in this block
        		break;
       		}
    		totalLen -= blockLen;
    		// constant speed
    		noopTime = (blockLen-curDistance)*(FACTOR*_scale)/curSpeed;    			
        	if (log.isDebugEnabled()) log.debug("Leave MidRoute block \""+blockName+"\" noopTime= "+noopTime+
        			", curDistance="+curDistance+", blockLen= "+blockLen+", totalLen= "+totalLen);
			block = orders.get(++idx).getBlock();
     		blockName = block.getDisplayName();
			blockLen = block.getLengthIn();
			if (idx==orderSize-1) {
				blockLen /= 2;
			} else if (blockLen<=0) {
    			blockLen = maxRampLength;
			}
    		w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
    		if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
      		curDistance = 0;
		}
    	if (log.isDebugEnabled()) log.debug("Start Ramp Down at block \""+blockName+"\", curSteps= "+curSteps+
    			", curDistance= "+curDistance+", blockLen= "+blockLen+", totalLen= "+totalLen+
    			", rampLength= "+rampLength+" curSpeed= "+curSpeed);
		
		// Ramp down.  use negative delta
    	remRamp = rampLength;
    	start = true;
    	while (curSteps>0) {
     		if (idx==orderSize-1) {
     			// at last block
     	    	if (_stageEStop.isSelected()) {
     	    		_eStop = true;
     	        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
     	        	time = 0;
     	        	break;
     	    	} else {
     	    		_eStop = false;     			
     	    	}
     		}
     		if (start) {
     			// constant speed
        		speedTime = (totalLen-curDistance-rampLength)*(FACTOR*_scale)/curSpeed;
        		curDistance = totalLen-rampLength;
        		start = false;
    		}
			int steps = 1;		// at least one speed change.  Maybe more.
			float speed = curSpeed;
			while (curSteps-steps > 1) {				
        		float dist = (speed - delta/2)*time/(FACTOR*_scale);
        		if (curDistance + dist < blockLen) {
        			curDistance += dist;
        			speed -= delta;
        			steps++;
        			remRamp -= dist;
       			} else {
       				break;
       			}
			}
			if (steps>0) {
		    	curSpeed -= delta;
		    	w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
				if (steps>1) {
					curSpeed = rampSpeed(w, (int)time, curSpeed, -delta, blockName, steps-1);	//steps==0 OK, no speed change					
				}
		    	if (log.isDebugEnabled()) log.debug("Continue Ramp Down at "+(int)speedTime+"ms in block \""+blockName+
		    			"\" to speed "+curSpeed+" after "+steps+" steps to reach curDistance= "+curDistance+", remRamp= "+remRamp);
				curSteps -= steps;
			}     		
     		if (idx==orderSize-1) {
     			if (blockLen==0) {
     				time = 0;
     			}
     			break;
     		}    		
    		totalLen -= blockLen;
    		if (idx < orderSize-1) {
    			noopTime = (blockLen-curDistance)*(FACTOR*_scale)/(curSpeed+delta/2);
    			speedTime = time - noopTime;
            	if (log.isDebugEnabled()) log.debug("Leave RampDown block \""+blockName+"\" noopTime= "+noopTime+
            			", in distance="+curSpeed*noopTime/(FACTOR*_scale)+", blockLen= "+blockLen+
            			", totalLen= "+totalLen+", remRamp= "+remRamp);
         		block = orders.get(++idx).getBlock();
         		blockName = block.getDisplayName();
        		blockLen = block.getLengthIn();
        		if (blockLen<=0 && idx<orderSize-1)  {
        			blockLen = maxRampLength;
        		}
        		w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
        		if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
    			curDistance = (curSpeed+delta/2)*speedTime/(FACTOR*_scale);
    		}
    	}
    	if (log.isDebugEnabled()) {
    		curDistance += curSpeed*speedTime/(FACTOR*_scale);
    		remRamp -=  curSpeed*speedTime/(FACTOR*_scale);     			
        	log.debug("Ramp down last speed change in block \""+blockName+"\" to speed "+curSpeed+
        			" after "+(int)time+"ms. at curDistance= "+curDistance+", remRamp= "+remRamp);    		
    	}
    	w.addThrottleCommand(new ThrottleSetting((int)time, "Speed", "0.0", blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(3000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F0", "false", blockName));
/*    	if (_addTracker.isSelected()) {
    		WarrantTableFrame._defaultAddTracker = true;
    	   	w.addThrottleCommand(new ThrottleSetting(10, "START TRACKER", "", blockName));
    	} else {
    		WarrantTableFrame._defaultAddTracker = false;
    	}*/
    	return null;
    }
    static private float rampSpeed(Warrant w, int time, float speed, float delta, String blockName, int incr) {
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
            msg = findRoute();
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

package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
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
    JCheckBox	_haltStartBox = new JCheckBox();
//    JCheckBox	_addTracker = new JCheckBox();
    JTextField _rampInterval = new JTextField(6);
    JTextField _numStepsBox = new JTextField(6);
    JTextField _searchDepth = new JTextField();
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
    JPanel		_autoRunPanel;
    JPanel		_manualPanel;
    HashMap<Float, JRadioButtonMenuItem> _scaleMap = new HashMap<Float, JRadioButtonMenuItem>();
    JMenu 		_scaleMenu;
    ButtonGroup _scaleButtons = new ButtonGroup();
	// Session persistent defaults for NX warrants
	static boolean _eStop = false;
	static boolean _haltStart = false;
//	static boolean _addTracker = false;
	static int _searchdepth = 15;
	static float _maxSpeed = 0.5f;
	static float _minSpeed = 0.075f;
	static float _intervalTime = 4000f;
	static int _numSteps = 8;


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
        
        pp = new JPanel();
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _searchDepth, "SearchDepth", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        _forward.setSelected(true);
        _stageEStop.setSelected(_eStop);
        _haltStartBox.setSelected(_haltStart);
//        _addTracker.setSelected(WarrantTableFrame._defaultAddTracker);
        _maxSpeedBox.setText(Float.toString(_maxSpeed));
        _minSpeedBox.setText(Float.toString(_minSpeed));
        _rampInterval.setText(Float.toString(_intervalTime/1000));
        _numStepsBox.setText(Integer.toString(_numSteps));
        _searchDepth.setText(Integer.toString(_searchdepth));
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
//        setVisible(false);      		
	}
    private void makeMenus() {
		setTitle(Bundle.getMessage("AutoWarrant"));
        JMenuBar menuBar = new JMenuBar();
        _scaleMenu = new JMenu(Bundle.getMessage("MenuScale"));
        menuBar.add(_scaleMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.NXWarrant", true);
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "G", "20.3"), 20.3f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "L", "38"), 38f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "O", "43"), 43f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "S", "64"), 64f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "OO", "76.2"), 76.2f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "HO", "87.1"), 87.1f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "TT", "120"), 120f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "N", "160"), 160f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "Z", "220"), 220f));
        _scaleMenu.add(makeItem(Bundle.getMessage("scaleMenu", "T", "480"), 480f));
        _scaleMenu.add(makeCustomItem(_scale));
    	setScaleMenu();
    }
    private JRadioButtonMenuItem makeItem(String name, float scale) {
    	ActionListener act = new ActionListener() {
        	float scale;
            public void actionPerformed(ActionEvent e) {
            	setScale(scale);
            	setScaleMenu();
            }
            ActionListener init (float s) {
            	scale = s;
                return this;
            }
               		
    	}.init(scale);
        JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(name);
        rbmi.addActionListener(act);
        _scaleButtons.add(rbmi);
        _scaleMap.put(Float.valueOf(scale), rbmi); 
    	return rbmi;
    }
    private JRadioButtonMenuItem makeCustomItem(float scale) {
    	ActionListener act = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	scaleDialog();
            }
    	};
    	JRadioButtonMenuItem rbmi = _scaleMap.get(scale);
    	if (rbmi!=null) {
                rbmi = new JRadioButtonMenuItem(Bundle.getMessage("custom"));
    	} else {
            rbmi = new JRadioButtonMenuItem(Bundle.getMessage("scaleMenu", Bundle.getMessage("custom"), Float.valueOf(scale)));    		
    	}
        rbmi.addActionListener(act);
        _scaleMenu.add(rbmi);
        _scaleButtons.add(rbmi);
        _scaleMap.put(Float.valueOf(0), rbmi); 
        return rbmi;
    }
    private void setScaleMenu() {
    	JRadioButtonMenuItem rbmi = _scaleMap.get(_scale);
    	if (rbmi==null) {
    		rbmi = _scaleMap.get(0f);
    		_scaleMenu.remove(rbmi);
    		_scaleButtons.remove(rbmi);
    		rbmi = makeCustomItem(_scale);
    	}
		rbmi.setSelected(true);
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

    private void scaleDialog() {
    	String s = JOptionPane.showInputDialog(this, Bundle.getMessage("customInput"), 
    			Bundle.getMessage("customTitle"), JOptionPane.QUESTION_MESSAGE);
    	try {
    		Float f = new Float(s);
    		float num = f.floatValue();
    		if (num <= 1) {
    			throw new NumberFormatException();
    		}
    		setScale(num);
    	} catch (NumberFormatException nfe) {
    		JOptionPane.showMessageDialog(this, Bundle.getMessage("customError", s), 
    				Bundle.getMessage("customTitle"), JOptionPane.ERROR_MESSAGE);
    	}
    	setScaleMenu();
    }
    public void setSearchDepth(int s) {
    	_searchdepth = s;
    }
    public float getSearchDepth() {
    	return _searchdepth;
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
    // Throttle setting = T.   Velocity(in/ms) = V.  V = T/S*F
    float _scale = 87.1f;
    static final float FACTOR = 0.8117f;
    
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

    	// distance required for N speed changes of equal increments of velocity, v, starting with speed, V. is proportional to
    	//  t[V + V=v + V+2v + ....... + V+nv}] = t[(n+1)V + d(n+1)n/2] = t(n+1)(V+nd/2) = D
    	// distance required for 'minSpeed' to top speed in 'numSteps' steps
    	float maxRampLength = time*numSteps*(minSpeed + delta*(numSteps-1)/2)/(FACTOR*_scale);

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
//		float b = (2*minSpeed+delta)/(FACTOR*_scale);
    	if (totalLen <= 2*maxRampLength) {
    		rampLength = 0;
    		numSteps = 0;
       		for (;;) {
       			float len = (minSpeed + numSteps*delta)*time/(FACTOR*_scale);
       			if (rampLength+len < totalLen/2) {
           			rampLength += len;
           			numSteps++;
       			} else {
       				break;
       			}
       		}
//    		numSteps = (int)((Math.sqrt(b*b + 8*(delta/(FACTOR*_scale))*(rampLength/time-minSpeed/(FACTOR*_scale))) - b)*(FACTOR*_scale)/(2*delta));
    	}
       	if (log.isDebugEnabled()) log.debug("Route length= "+totalLen+" uses "+numSteps+" speed steps of delta= "+
       			delta+" for rampLength = "+rampLength+" for route of length "+totalLen);
/*       	if (log.isDebugEnabled()) {
       		float sum = 0;
       		for (int i=0; i<=numSteps; i++) {
       			float len = (minSpeed + i*delta)*time/(FACTOR*_scale);
       			sum += len;
       			log.debug("Step #"+i+" speed= "+(minSpeed+i*delta)+", distance= "+len+", RampLenght= "+sum);
       		}
       	}*/
       	
    	// start train
		int idx = 0;		// block index
 		float blockLen = block.getLengthIn()/2;

 		// start ramp
		float speedTime = time;		// ms time to next speed change in block
		float noopTime = 0;			// ms time for entry into next block
		int curSteps = 0;
		float curSpeed = 0;
		float curDistance = 0;		// distance traveled in current block
		float speedIncre = minSpeed;
    	if (log.isDebugEnabled()) log.debug("Ramp up in block \""+blockName+"\" to start speed "+curSpeed);
		
    	float remRamp = rampLength;
		while (curSteps < numSteps) {
			curSpeed = rampSpeed(w, (int)speedTime, curSpeed, speedIncre, blockName, 1);
			speedIncre = delta;
			curSteps++;
			curDistance = curSpeed*speedTime/(FACTOR*_scale);
	    	if (log.isDebugEnabled()) log.debug("Ramp up 1 speed change in block \""+blockName+"\" to speed "+curSpeed+
	    			" after "+speedTime+"ms. at curDistance= "+curDistance);
			int steps = 0;
			while (curSteps+steps<numSteps) {				
				float len = (curSpeed + steps*delta)*time/(FACTOR*_scale);
				if (len + curDistance < blockLen) {
					curDistance += len;
					steps++;
				} else {
					break;
				}
			}
//    		steps = (int)((Math.sqrt(b*b + 8*(delta/(FACTOR*_scale))*(blockLen/time-minSpeed/(FACTOR*_scale))) - b)*(FACTOR*_scale)/(2*delta));
			speedTime = time;
			curSpeed = rampSpeed(w, (int)time, curSpeed, delta, blockName, steps);
			curSteps += steps;
	    	if (log.isDebugEnabled()) log.debug("Ramp up in block \""+blockName+"\" to speed "+curSpeed+
	    			" after "+steps+" steps. curDistance= "+curDistance);

       		if (totalLen-blockLen <= rampLength || remRamp <= 0) {
       			// leave enough room to ramp down OR ramp up is done.
        		break;
       		}
    		totalLen -= blockLen;
    		remRamp -= curDistance;
			noopTime = 0;		// ms time for entry into next block
			if (blockLen > curDistance) {
				noopTime = (blockLen-curDistance)*(FACTOR*_scale)/curSpeed;
			}
    		speedTime = time - noopTime;
        	if (log.isDebugEnabled()) log.debug("Leave RampUp block \""+blockName+"\" noopTime= "+noopTime+
        			", speedTime= "+speedTime+", curDistance="+curDistance+", blockLen= "+blockLen+
        			", totalLen= "+totalLen+", remRamp= "+remRamp);
     		block = orders.get(++idx).getBlock();
     		blockName = block.getDisplayName();
    		blockLen = block.getLengthIn();
    		if (blockLen<=0 && idx<orderSize-1)  {
    			blockLen = maxRampLength;
    		}
    		w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
    		curDistance = 0;
 		}
       	if (log.isDebugEnabled()) log.debug("Ramp Up done at block \""+blockName+"\" curSteps= "+curSteps+
       			", curSpeed="+curSpeed+", blockLen= "+blockLen+" totalLen= "+totalLen+", rampLength= "+rampLength);
			
		// run through mid route at max speed
		while (idx<orderSize-1) {
       		if (totalLen-blockLen <= rampLength) {
       			// Start ramp down in this block
        		break;
       		}
    		totalLen -= blockLen;
     		noopTime = blockLen*(FACTOR*_scale)/curSpeed;
        	if (log.isDebugEnabled()) log.debug("Leave MidRoute block \""+blockName+"\" noopTime= "+noopTime+
        			", curDistance="+curDistance+", blockLen= "+blockLen+
        			", totalLen= "+totalLen+", rampLength= "+rampLength);
			block = orders.get(++idx).getBlock();
     		blockName = block.getDisplayName();
			blockLen = block.getLengthIn();
			if (idx==orderSize-1) {
				blockLen /= 2;
			} else if (blockLen<=0) {
    			blockLen = maxRampLength;
			}
    		w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
      		curDistance = 0;
		}
    	if (log.isDebugEnabled()) log.debug("Start Ramp Down at block \""+blockName+"\", curSteps= "+curSteps+
    			", curDistance= "+curDistance+", blockLen= "+blockLen+", totalLen= "+totalLen+", rampLength= "+rampLength);
		
		// Ramp down.  use negative delta
//		 doesn't solve to real number if blockLen is too large
//		b = 2*curSpeed+delta;
//		steps = (int)((Math.sqrt(b*b + 8*delta*(_scale*FACTOR*blockLen/time-curSpeed)) - b)/(2*delta));
    	remRamp = rampLength;
    	boolean start = true;
    	while (curSteps>0) {
     		if (start) {
          		speedTime = (totalLen-remRamp-curDistance)*(FACTOR*_scale)/curSpeed;     			
     		} else {
     			if (noopTime < time) {
     				speedTime = time - noopTime;
     			} else {
     				speedTime = 0;
     			}
     		}
     		start = false;
     		if (idx==orderSize-1) {
     			// at last block
 	    		blockLen /= 2;
     	    	if (_stageEStop.isSelected()) {
     	    		_eStop = true;
     	        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
     	        	time = 0;
     	    	} else {
     	    		_eStop = false;
     	        	if (curSteps>1) {
         				curSpeed = rampSpeed(w, (int)speedTime, curSpeed, -delta, blockName, 1);     	        		
         		    	if (log.isDebugEnabled()) log.debug("Ramp down 1 speed change in last block \""+blockName+"\" to speed "+curSpeed+
         		    			" after "+speedTime+"ms. from curDistance= "+curDistance);
         		    	curSteps -=2;
         				curSpeed = rampSpeed(w, (int)time, curSpeed, -delta, blockName, curSteps);     	        		        		    		
         	        	if (log.isDebugEnabled()) log.debug("Ramp down in last block \""+blockName+"\" using "+curSteps+
         	        			" steps in blockLen "+blockLen);
     	        	} else {
         	        	time = Math.max((blockLen-curDistance), 0)*(FACTOR*_scale)/(curSpeed);     	        		
         	        	if (log.isDebugEnabled()) log.debug("Set time in last block \""+blockName+"\" time= "+time+", curDistance= "+
         	        			curDistance+" in blockLen "+blockLen+" with speed= "+curSpeed);
     	        	}
    	     	}
     	    	break;
     		}
			float dist = 0; 
			int steps = 0;
			float len = 0;
			while (curSteps-steps>0) {				
				len = (curSpeed - steps*delta)*time/(FACTOR*_scale);
				if (len + curDistance + dist < blockLen) {
					dist += len;
					steps++;
				} else {
					break;
				}
			}
			if (curDistance<blockLen && totalLen-blockLen <= remRamp && curSteps>1) {
	      		curDistance += curSpeed*speedTime/(FACTOR*_scale);
		       	dist = curSpeed*time/(FACTOR*_scale);			// distance used for this step
				curSpeed = rampSpeed(w, (int)speedTime, curSpeed, -delta, blockName, 1);
				curSteps--;
		       	if (log.isDebugEnabled()) log.debug("Ramp Down 1st speed change to "+curSpeed+" after "+(int)speedTime+
		       			" ms at distance= "+curDistance+", remRamp= "+remRamp+" in block "+blockName);
				remRamp -= dist;
	      		curDistance += dist;
				while (curDistance < blockLen && totalLen-blockLen <= remRamp && curSteps>1) {
			       	dist = curSpeed*time/(FACTOR*_scale);
	            	curSpeed = rampSpeed(w, (int)time, curSpeed, -delta, blockName, 1);    				
					curSteps--;
			       	if (log.isDebugEnabled()) log.debug("Ramp Down speed change to "+curSpeed+" at distance= "+curDistance+
			       			", remRamp= "+remRamp +", dist= "+dist+" in block "+blockName);						
					remRamp -= dist;
		      		curDistance += dist;
				}
		       	if (log.isDebugEnabled()) log.debug("Ramp Down in block "+blockName+" done. blockLen= "+blockLen+", curDistance= "+curDistance+
		       			", remRamp= "+remRamp+" curSpeed= "+curSpeed);
			}
			curDistance -= dist;
			noopTime = (blockLen-curDistance)*(FACTOR*_scale)/curSpeed;
			curDistance = 0;
			if (idx<orderSize-1) {
	    		totalLen -= blockLen;   			
	        	if (log.isDebugEnabled()) log.debug("Leave RampDown block \""+blockName+"\" rem curDistance="+curDistance+
	        			", noopTime= "+noopTime+",  blockLen= "+blockLen+
	        			", totalLen= "+totalLen+", remRamp= "+remRamp);
	     		block = orders.get(++idx).getBlock();
	     		blockName = block.getDisplayName();
	    		blockLen = block.getLengthIn();
	    		if (blockLen<=0 && idx<orderSize-1)  {
	    			blockLen = maxRampLength;
	    		}
	    		w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
			}
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
        		 _searchdepth = Integer.parseInt(_searchDepth.getText());
            } catch (NumberFormatException nfe) {
            	_searchdepth = 15;
            }
            msg = findRoute(_searchdepth);
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

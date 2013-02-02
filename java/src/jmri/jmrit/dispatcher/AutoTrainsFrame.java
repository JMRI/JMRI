// AutoTrainsFrame.java

package jmri.jmrit.dispatcher;

import org.apache.log4j.Logger;
import jmri.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * AutoTrainsFrame provides a user interface to trains that are running 
 *  automatically under Dispatcher.
 * <P>
 * There is only one AutoTrains window. AutoTrains are added and deleted from this window
 *  as they are added or terminated.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author			Dave Duchamp   Copyright (C) 2010
 * @version			$Revision$
 */
public class AutoTrainsFrame extends jmri.util.JmriJFrame {
	
    public AutoTrainsFrame (DispatcherFrame disp) {
		super(false,true);
		_dispatcher = disp;
		initializeAutoTrainsWindow();
	}
	
	static final ResourceBundle rb = ResourceBundle
	.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");


	// instance variables
	private DispatcherFrame _dispatcher = null;
	private ArrayList<AutoActiveTrain> _autoTrainsList = new ArrayList<AutoActiveTrain>();
	private ArrayList<java.beans.PropertyChangeListener> _listeners = 
							new ArrayList<java.beans.PropertyChangeListener>();
	
	// accessor functions
	public ArrayList<AutoActiveTrain> getAutoTrainsList(){return _autoTrainsList;}
	public void addAutoActiveTrain(AutoActiveTrain aat) {
		if (aat!=null) {
			_autoTrainsList.add(aat);
			ActiveTrain at = aat.getActiveTrain();
			java.beans.PropertyChangeListener listener = null;
			at.addPropertyChangeListener(listener = new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					handleActiveTrainChange(e);
				}
			});
			_listeners.add(listener);
			displayAutoTrains();
		}
	}
	public void removeAutoActiveTrain(AutoActiveTrain aat) {
		for (int i=0; i<_autoTrainsList.size(); i++) {
			if (_autoTrainsList.get(i) == aat) {
				_autoTrainsList.remove(i);
				ActiveTrain at = aat.getActiveTrain();
				at.removePropertyChangeListener(_listeners.get(i));
				_listeners.remove(i);
				displayAutoTrains();
				return;
			}
		}		
	}
	private void handleActiveTrainChange(java.beans.PropertyChangeEvent e) {
		displayAutoTrains();
	}
	
	// variables for AutoTrains window
	protected JmriJFrame autoTrainsFrame = null;
	private Container contentPane = null;
	// note: the following array lists are synchronized with _autoTrainsList
	private ArrayList<JPanel> _JPanels = new ArrayList<JPanel>();
	private ArrayList<JLabel> _trainLabels = new ArrayList<JLabel>();
	private ArrayList<JButton> _stopButtons = new ArrayList<JButton>();
	private ArrayList<JButton> _manualButtons = new ArrayList<JButton>();
	private ArrayList<JButton> _resumeAutoRunningButtons = new ArrayList<JButton>();
	private ArrayList<JRadioButton> _forwardButtons = new ArrayList<JRadioButton>();
	private ArrayList<JRadioButton> _reverseButtons = new ArrayList<JRadioButton>();
	private ArrayList<JSlider> _speedSliders = new ArrayList<JSlider>();
	
	private ArrayList<JSeparator> _separators = new ArrayList<JSeparator>();
	
	private void initializeAutoTrainsWindow() {
		autoTrainsFrame = this;
		autoTrainsFrame.setTitle(rb.getString("TitleAutoTrains"));
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		autoTrainsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.AutoTrains", true);
		contentPane = autoTrainsFrame.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		// set up 6 auto trains to size the panel
		for (int i=0; i<6; i++) {
			newTrainLine();
			if (i==0) _separators.get(i).setVisible(false);
		}
		contentPane.add (new JSeparator());
		contentPane.add (new JSeparator());
		JPanel pB = new JPanel();
		pB.setLayout(new FlowLayout());
		JButton stopAllButton = new JButton(rb.getString("StopAll"));
		pB.add(stopAllButton);
		stopAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopAllPressed(e);
			}
		});
		stopAllButton.setToolTipText(rb.getString("StopAllButtonHint"));
		contentPane.add(pB);
		autoTrainsFrame.pack();
		placeWindow();
		displayAutoTrains();
		autoTrainsFrame.setVisible(true);
	}
	private void newSeparator() {
		JSeparator sep = new JSeparator();
		_separators.add(sep);
		contentPane.add(sep);
	}	
	private void newTrainLine() {
		int i = _JPanels.size();
		final String s = ""+i;
		newSeparator();
		JPanel px = new JPanel();
		px.setLayout(new FlowLayout());
		_JPanels.add(px);
		JLabel tLabel = new JLabel("      ");
		px.add(tLabel);
		_trainLabels.add(tLabel);
		JButton tStop = new JButton(rb.getString("ResumeButton"));
		px.add(tStop);
		_stopButtons.add(tStop);
		tStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopResume(s);
			}
		});
		JButton tManual = new JButton(rb.getString("ToManualButton"));
		px.add(tManual);
		_manualButtons.add(tManual);
		tManual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manualAuto(s);
			}
		});
		JButton tResumeAuto = new JButton(rb.getString("ResumeAutoButton"));
		px.add(tResumeAuto);
		_resumeAutoRunningButtons.add(tResumeAuto);
		tResumeAuto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resumeAutoOperation(s);
			}
		});
		tResumeAuto.setVisible(false);
		tResumeAuto.setToolTipText(rb.getString("ResumeAutoButtonHint"));
		ButtonGroup directionGroup = new ButtonGroup();
		JRadioButton fBut = new JRadioButton(rb.getString("ForwardRadio"));
		px.add(fBut);
		_forwardButtons.add(fBut);
		fBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				directionButton(s);
			}
		});
		directionGroup.add(fBut);
		JRadioButton rBut = new JRadioButton(rb.getString("ReverseRadio"));
		px.add(rBut);
		_reverseButtons.add(rBut);
		rBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				directionButton(s);
			}
		});
		directionGroup.add(rBut);
		JSlider sSlider = new JSlider(0,100,0);
		px.add(sSlider);
		_speedSliders.add(sSlider);
		sSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int val = ((JSlider)(e.getSource())).getValue();
				sliderChanged(s,val);
			}
		});
		
		contentPane.add(px);
	}						
	private void placeWindow() {
		// get size and placement of Dispatcher Window, screen size, and window size
		Point dispPt = _dispatcher.getLocationOnScreen();
		Dimension dispDim = _dispatcher.getSize();
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenDim.height-120;
        int screenWidth = screenDim.width-20;
		Dimension dim = getSize();
		int width = dim.width;
		int height = dim.height;
		// place AutoTrains window to the right of Dispatcher window, if it will fit.
		int upperLeftX = dispPt.x+dispDim.width;
		int upperLeftY = 0;
		if ( (upperLeftX+width) > screenWidth) {
			// won't fit, place it below Dispatcher window, if it will fit.
			upperLeftX = 0;
			upperLeftY = dispPt.y+dispDim.height;
			if ( (upperLeftY+height) > screenHeight) {				
				// if all else fails, place it at the upper left of the screen, and let the user adjust placement
				upperLeftY = 0;
			}
		}
		setLocation (upperLeftX,upperLeftY);		
	}
						   
	public void stopResume(String s) {
		int index = getTrainIndex (s);
		if (index>=0) {
			AutoActiveTrain aat = _autoTrainsList.get(index);
			if (aat.getAutoEngineer()!=null) {
				ActiveTrain at = aat.getActiveTrain();
				if (at.getStatus()==ActiveTrain.STOPPED) {
					// resume
					aat.setEngineDirection();
					aat.getAutoEngineer().setHalt(false);
					aat.restoreSavedSpeed();
					at.setStatus(aat.getSavedStatus());
					if ( (at.getStatus()==ActiveTrain.RUNNING) || 
							(at.getStatus()==ActiveTrain.WAITING) ) {
						aat.setSpeedBySignal();
					}
				}
				else {
					// stop
					aat.getAutoEngineer().setHalt(true);
					aat.saveSpeed();
					aat.setSavedStatus(at.getStatus());
					at.setStatus(ActiveTrain.STOPPED);
					if (at.getMode()==ActiveTrain.MANUAL) {
						_speedSliders.get(index).setValue(0);
					}
				}
			}
			else {
				log.error ("unexpected null autoEngineer");
			}
		}
		displayAutoTrains();
	}				
							
	public void manualAuto(String s) {
		int index = getTrainIndex (s);
		if (index>=0) {
			AutoActiveTrain aat = _autoTrainsList.get(index);
			ActiveTrain at = aat.getActiveTrain();
			// if train is AUTOMATIC mode, change it to MANUAL
			if (at.getMode()==ActiveTrain.AUTOMATIC) {
				at.setMode(ActiveTrain.MANUAL);
				if (aat.getAutoEngineer()!=null) {
					aat.saveSpeed();
					aat.getAutoEngineer().setHalt(true);
					aat.setTargetSpeed(0.0f);
					aat.waitUntilStopped();
					aat.getAutoEngineer().setHalt(false);
					
				}
			}
			else if (at.getMode()==ActiveTrain.MANUAL) {
				at.setMode(ActiveTrain.AUTOMATIC);
				aat.restoreSavedSpeed();
				aat.setForward(!aat.getRunInReverse());
				if ( (at.getStatus()==ActiveTrain.RUNNING) || 
						(at.getStatus()==ActiveTrain.WAITING) ) {
					aat.setSpeedBySignal();
				}
			}
		}
		displayAutoTrains();
	}

	public void resumeAutoOperation(String s) {
		int index = getTrainIndex (s);
		if (index>=0) {
			AutoActiveTrain aat = _autoTrainsList.get(index);
			aat.resumeAutomaticRunning();
		}
		displayAutoTrains();
	}
			
	public void directionButton(String s) {
		int index = getTrainIndex (s);
		if (index>=0) {
			AutoActiveTrain aat = _autoTrainsList.get(index);
			ActiveTrain at = aat.getActiveTrain();
			if (at.getMode()==ActiveTrain.MANUAL) {
				aat.setForward(_forwardButtons.get(index).isSelected());
			}
			else {
				log.warn("unexpected direction button change on line "+s);
			}
		}
	}
	public void sliderChanged(String s, int value) {
		int index = getTrainIndex (s);
		if (index>=0) {
			AutoActiveTrain aat = _autoTrainsList.get(index);
			ActiveTrain at = aat.getActiveTrain();
			if (at.getMode()==ActiveTrain.MANUAL) {
				float speedValue = value;
				speedValue = speedValue*0.01f;
				aat.setTargetSpeed(speedValue);
			}
			else {
				log.warn("unexpected slider change on line "+s);
			}
		}
	}
	
	private int getTrainIndex (String s) {
		int index = -1;
		try {
			index = Integer.parseInt(s);
		}
		catch (Exception e) {
			log.warn("exception when parsing index from AutoTrains window - "+s);
		}
		if ( (index>=0) && (index<_autoTrainsList.size()) ) {
			return index;
		}
		log.error("bad train index in auto trains table "+index);
		return (-1);
	}
	
	public void stopAllPressed(ActionEvent e) {
		for (int i=0; i<_autoTrainsList.size(); i++) {
			AutoActiveTrain aat = _autoTrainsList.get(i);
			ActiveTrain at = aat.getActiveTrain();
			if ( (at.getStatus()!=ActiveTrain.STOPPED) && (aat.getAutoEngineer()!=null) ) {
				aat.getAutoEngineer().setHalt(true);
				aat.saveSpeed();
				aat.setSavedStatus(at.getStatus());
				at.setStatus(ActiveTrain.STOPPED);
			}
		}
		displayAutoTrains();
	}
	
	protected void displayAutoTrains() {
		// set up AutoTrains to display
		while(_autoTrainsList.size()>_JPanels.size()) newTrainLine();
		for (int i=0; i<_autoTrainsList.size(); i++) {
			AutoActiveTrain aat = _autoTrainsList.get(i);
			if (aat!=null) {
				if (i>0) {
					JSeparator sep = _separators.get(i);
					sep.setVisible(true);
				}
				JPanel panel = _JPanels.get(i);
				panel.setVisible(true);
				ActiveTrain at = aat.getActiveTrain();
				JLabel tName = _trainLabels.get(i);
				tName.setText(at.getTrainName());
				JButton stopButton = _stopButtons.get(i);
				if (at.getStatus()==ActiveTrain.STOPPED) {
					stopButton.setText(rb.getString("ResumeButton"));
					stopButton.setToolTipText(rb.getString("ResumeButtonHint"));
					_resumeAutoRunningButtons.get(i).setVisible(false);
				}
				else if (at.getStatus()==ActiveTrain.WORKING) {
					stopButton.setVisible(false);
				}
				else {
					stopButton.setText(rb.getString("StopButton"));
					stopButton.setToolTipText(rb.getString("StopButtonHint"));
					stopButton.setVisible(true);
				}
				JButton manualButton = _manualButtons.get(i);
				if (at.getMode()==ActiveTrain.AUTOMATIC) {
					manualButton.setText(rb.getString("ToManualButton"));
					manualButton.setToolTipText(rb.getString("ToManualButtonHint"));
					manualButton.setVisible(true);
					_resumeAutoRunningButtons.get(i).setVisible(false);
					_forwardButtons.get(i).setVisible(false);
					_reverseButtons.get(i).setVisible(false);
					_speedSliders.get(i).setVisible(false);
				}
				else if ( (at.getMode()==ActiveTrain.MANUAL) && ( (at.getStatus()==ActiveTrain.WORKING) ||
										(at.getStatus()==ActiveTrain.READY)	) ) {
					manualButton.setVisible(false);
					_resumeAutoRunningButtons.get(i).setVisible(true);
					_forwardButtons.get(i).setVisible(false);
					_reverseButtons.get(i).setVisible(false);
					_speedSliders.get(i).setVisible(false);
				}	
				else {
					manualButton.setText(rb.getString("ToAutoButton"));
					manualButton.setToolTipText(rb.getString("ToAutoButtonHint"));
					_forwardButtons.get(i).setVisible(true);
					_reverseButtons.get(i).setVisible(true);
					_speedSliders.get(i).setVisible(true);
					_forwardButtons.get(i).setSelected(aat.getForward());
					_reverseButtons.get(i).setSelected(!aat.getForward());
					int speedValue = (int)(aat.getTargetSpeed()*100.0f);
					_speedSliders.get(i).setValue(speedValue);
				}
			}
		}
		// clear unused item rows, if needed
		for (int j=_autoTrainsList.size(); j<_JPanels.size(); j++) {
			JPanel panel = _JPanels.get(j);
			panel.setVisible(false);
			JSeparator sep = _separators.get(j);
			sep.setVisible(false);
		}
		autoTrainsFrame.pack();
		autoTrainsFrame.setVisible(true);
	}	
	
	
	static Logger log = Logger.getLogger(AutoTrainsFrame.class.getName());

}

/* @(#)AutoTrainsFrame.java */

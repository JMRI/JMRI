/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import jmri.CommonTurnoutOperation;
import jmri.TurnoutOperation;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;

/**
 * Extension of TurnoutOperationConfig to handle config for common aspects of some
 * subclasses
 * @author John Harper	Copyright 2005
 *
 */
public class CommonTurnoutOperationConfig extends TurnoutOperationConfig {

	JSpinner intervalSpinner;
	JSpinner maxTriesSpinner;
	CommonTurnoutOperation myOp;
	
	/**
	 * create the config panel, if there is one, and in any case return it
	 * @return	the JPanel to configure this operation type
	 */
	public CommonTurnoutOperationConfig(TurnoutOperation op) {
		super(op);
		myOp = (CommonTurnoutOperation)op;
		Box vbox = Box.createVerticalBox();
		Box hbox1 = Box.createHorizontalBox();
		Box hbox2 = Box.createHorizontalBox();
		vbox.add(hbox1);
		vbox.add(hbox2);
		vbox.add(Box.createVerticalGlue());
		hbox1.add(new JLabel("Interval:     "));
		hbox1.add(Box.createHorizontalGlue());
		intervalSpinner = new JSpinner();
		intervalSpinner.setMinimumSize(new Dimension(100,20));
		SpinnerNumberModel intervalSpinnerModel=(SpinnerNumberModel)intervalSpinner.getModel();
		intervalSpinnerModel.setMaximum(new Integer(myOp.maxInterval));
		intervalSpinnerModel.setMinimum(new Integer(myOp.minInterval));
		intervalSpinnerModel.setStepSize(new Integer(myOp.intervalStepSize));
		intervalSpinner.setModel(intervalSpinnerModel);
		hbox1.add(intervalSpinner);
		hbox2.add(new JLabel("Times to try:   "));
		hbox2.add(Box.createHorizontalGlue());
		maxTriesSpinner = new JSpinner();
		maxTriesSpinner.setMinimumSize(new Dimension(100,20));
		SpinnerNumberModel maxTriesSpinnerModel=(SpinnerNumberModel)maxTriesSpinner.getModel();
		maxTriesSpinnerModel.setMaximum(new Integer(myOp.maxMaxTries));
		maxTriesSpinnerModel.setMinimum(new Integer(myOp.minMaxTries));
		maxTriesSpinner.setModel(maxTriesSpinnerModel);
		hbox2.add(maxTriesSpinner);
		intervalSpinner.setValue(new Integer((int)myOp.getInterval()));
		maxTriesSpinner.setValue(new Integer(myOp.getMaxTries()));
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(Box.createHorizontalStrut(150));
		vbox.add(hbox3);
		add(vbox);
	}

	/**
	 * called when OK button pressed in config panel, to retrieve and set new values
	 */
	public void endConfigure() {
		long newInterval = ((Integer)intervalSpinner.getValue()).intValue();
		myOp.setInterval(newInterval);
		int newMaxTries = ((Integer)maxTriesSpinner.getValue()).intValue();
		myOp.setMaxTries(newMaxTries);
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CommonTurnoutOperationConfig.class.getName());
}

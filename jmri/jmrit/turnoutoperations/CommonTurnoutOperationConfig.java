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
import jmri.util.JSpinnerUtil;

/**
 * Extension of TurnoutOperationConfig to handle config for common aspects of some
 * subclasses
 * @author John Harper	Copyright 2005
 *
 */
public class CommonTurnoutOperationConfig extends TurnoutOperationConfig {

	JComponent intervalSpinner;   // actually a JSpinner
	JComponent maxTriesSpinner;   // actually a JSpinner
	CommonTurnoutOperation myOp;
	
	/**
	 * Create the config JPanel, if there is one, to configure this operation type
	 */
	public CommonTurnoutOperationConfig(TurnoutOperation op) {
		super(op);
		myOp = (CommonTurnoutOperation)op;

	    maxTriesSpinner = JSpinnerUtil.getJSpinner();
		intervalSpinner = JSpinnerUtil.getJSpinner();
		if ( (maxTriesSpinner == null) || (intervalSpinner == null)) {
			JOptionPane.showMessageDialog(null,
					"Turnout automation parameters cannot be modified with the Java Virtual Machine you are using",
					"Cannot modify parameters", JOptionPane.ERROR_MESSAGE);
			valid = false;
			return;
		}
		Box vbox = Box.createVerticalBox();
		Box hbox1 = Box.createHorizontalBox();
		Box hbox2 = Box.createHorizontalBox();
		vbox.add(hbox1);
		vbox.add(hbox2);
		vbox.add(Box.createVerticalGlue());
		hbox1.add(new JLabel("Interval:     "));
		hbox1.add(Box.createHorizontalGlue());
		intervalSpinner.setMinimumSize(new Dimension(100,20));

		JSpinnerUtil.setModelMaximum(intervalSpinner, new Integer(myOp.maxInterval));
		JSpinnerUtil.setModelMinimum(intervalSpinner, new Integer(myOp.minInterval));
		JSpinnerUtil.setModelStepSize(intervalSpinner, new Integer(myOp.intervalStepSize));

		hbox1.add(intervalSpinner);
		hbox2.add(new JLabel("Times to try:   "));
		hbox2.add(Box.createHorizontalGlue());
		maxTriesSpinner.setMinimumSize(new Dimension(100,20));

		JSpinnerUtil.setModelMaximum(maxTriesSpinner, new Integer(myOp.maxMaxTries));
		JSpinnerUtil.setModelMinimum(maxTriesSpinner, new Integer(myOp.minMaxTries));

		hbox2.add(maxTriesSpinner);

		JSpinnerUtil.setValue(intervalSpinner, new Integer((int)myOp.getInterval()));
		JSpinnerUtil.setValue(maxTriesSpinner, new Integer(myOp.getMaxTries()));

		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(Box.createHorizontalStrut(150));
		vbox.add(hbox3);
		add(vbox);
	}

	/**
	 * called when OK button pressed in config panel, to retrieve and set new values
	 */
	public void endConfigure() {
		int newInterval = ((Integer)JSpinnerUtil.getValue(intervalSpinner)).intValue();
		myOp.setInterval(newInterval);
		int newMaxTries = ((Integer)JSpinnerUtil.getValue(maxTriesSpinner)).intValue();
		myOp.setMaxTries(newMaxTries);
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CommonTurnoutOperationConfig.class.getName());
}

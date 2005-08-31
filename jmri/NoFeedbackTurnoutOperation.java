/**
 * 
 */
package jmri;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


import org.jdom.Element;

/**
 * NoFeedBackTurnoutOperation class - specialization of TurnoutOperation to provide
 * automatic retry for a turnout with no feedback
 * @author John Harper
 * @version $Revision: 1.1 $
 */
public class NoFeedbackTurnoutOperation extends CommonTurnoutOperation {

	// This class can deal with ANY feedback mode, although it may not be the best one
	final int feedbackModes =
			AbstractTurnout.DIRECT | AbstractTurnout.ONESENSOR | AbstractTurnout.TWOSENSOR;
	
	/*
	 * Default values and constraints
	 */
	
	static public final int defaultInterval = 300;
	static public final int defaultMaxTries = 2;
	
	public NoFeedbackTurnoutOperation(String n, int i, int mt) {
		super(n, i, mt);
		setFeedbackModes(feedbackModes);
	}
	
	/**
	 * constructor with default values - this creates the "defining instance" of
	 * the operation type hence it cannot be deleted
	 */
	public NoFeedbackTurnoutOperation() {
		this("NoFeedback", defaultInterval, defaultMaxTries);
	}
	
	public int getDefaultInterval() {
		return defaultInterval;
	}
	
	public int getDefaultMaxTries() {
		return defaultMaxTries;
	}
	
	static public int getDefaultIntervalStatic() {
		return defaultInterval;
	}
	
	static public int getDefaultMaxTriesStatic() {
		return defaultMaxTries;
	}
	
	/**
	 * get a TurnoutOperator instance for this operation
	 * @return	the operator
	 */
	public TurnoutOperator getOperator(AbstractTurnout t) {
		return new NoFeedbackTurnoutOperator(t, interval, maxTries);
	}
	
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NoFeedbackTurnoutOperation.class.getName());
}

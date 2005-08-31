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
 * CommonTurnoutOperation class - specialization of TurnoutOperation to contain
 * common properties and methods for concrete subclasses
 * @author John Harper
 * @version $Revision: 1.1 $
 */
public abstract class CommonTurnoutOperation extends TurnoutOperation {

	/*
	 * Parameters of this object
	 */
	long interval;					// time between attempts
	int maxTries;					// no of times to try

	/*
	 * Default values and constraints
	 */
	
	static public final int minInterval = 100;
	static public final int maxInterval = 5000;		// let's not get silly...
	static public final int intervalStepSize = 50;
	static public final int minMaxTries = 1;
	static public final int maxMaxTries = 10;
	
	public CommonTurnoutOperation(String n, long i, int mt) {
		super(n);
		interval = getDefaultInterval();
		maxTries = getDefaultMaxTries();
		setInterval(i);
		setMaxTries(mt);
	}
	
	/**
	 * get a TurnoutOperator instance for this operation
	 * @return	the operator
	 */
	public abstract TurnoutOperator getOperator(AbstractTurnout t);
	
	public long getInterval() { return interval; };
	
	public int getMaxTries() { return maxTries; };
	
	public abstract int getDefaultInterval();
	public abstract int getDefaultMaxTries();
	
	/**
	 * set new value for interval. do nothing if not in range.
	 * @param newInterval
	 */
	public void setInterval(long newInterval) {
		if (newInterval>=minInterval && newInterval<=maxInterval) {
			interval = newInterval;
		}
	}
	
	/**
	 * set new value for MaxTries. do nothing if not in range.
	 * @param newMaxTries
	 */
	public void setMaxTries(int newMaxTries) {
		if (newMaxTries>=minMaxTries && newMaxTries<=maxMaxTries) {
			maxTries = newMaxTries;
		}
	}
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CommonTurnoutOperation.class.getName());
}

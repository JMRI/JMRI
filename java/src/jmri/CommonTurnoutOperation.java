/**
 * 
 */
package jmri;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnout;

/**
 * CommonTurnoutOperation class - specialization of TurnoutOperation to contain
 * common properties and methods for concrete subclasses
 * @author John Harper
 * @version $Revision$
 */
public abstract class CommonTurnoutOperation extends TurnoutOperation {

	/*
	 * Parameters of this object
	 */
	int interval;					// time between attempts
	int maxTries;					// no of times to try

	/*
	 * Default values and constraints
	 */
	
	static public final int minInterval = 100;
	static public final int maxInterval = 5000;		// let's not get silly...
	static public final int intervalStepSize = 50;
	static public final int minMaxTries = 1;
	static public final int maxMaxTries = 10;
	
	public CommonTurnoutOperation(String n, int i, int mt) {
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
	
	public int getInterval() { return interval; }
	
	public int getMaxTries() { return maxTries; }
	
	public abstract int getDefaultInterval();
	public abstract int getDefaultMaxTries();
	
	public boolean equivalentTo(TurnoutOperation other) {
		if (this.getClass()==other.getClass()) {
			return interval==((CommonTurnoutOperation)other).interval &&
				maxTries==((CommonTurnoutOperation)other).maxTries;
		} else return false;
	}

	/**
	 * set new value for interval. do nothing if not in range.
	 * @param newInterval
	 */
	public void setInterval(int newInterval) {
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
	
    static Logger log = Logger.getLogger(CommonTurnoutOperation.class.getName());
}

/**
 * 
 */
package jmri;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnout;
import jmri.implementation.SensorTurnoutOperator;

/**
 * SensorTurnoutOperation class - specialization of TurnoutOperation to provide
 * automatic retry for a turnout with explicit feedback from sensor(s)
 * @author John Harper	Copyright 2005
 * @version $Revision$
 */
public class SensorTurnoutOperation extends CommonTurnoutOperation {

	// This class can deal with explicit feedback modes
	final int feedbackModes = AbstractTurnout.ONESENSOR | AbstractTurnout.TWOSENSOR |AbstractTurnout.EXACT |AbstractTurnout.INDIRECT;
	
	/*
	 * Default values and constraints
	 */
	
	static public final int defaultInterval = 300;
	static public final int defaultMaxTries = 3;
	
	public SensorTurnoutOperation(String n, int i, int mt) {
		super(n, i, mt);
		setFeedbackModes(feedbackModes);
	}
	
	/**
	 * constructor with default values - this creates the "defining instance" of
	 * the operation type hence it cannot be deleted
	 */
	public SensorTurnoutOperation() {
		this("Sensor", defaultInterval, defaultMaxTries);
	}
	
	/**
	 * return clone with different name
	 */
	public TurnoutOperation makeCopy(String n) {
		return new SensorTurnoutOperation(n, interval, maxTries);
	}

	public int getDefaultInterval() {
		return defaultInterval;
	}
	
	public int getDefaultMaxTries() {
		return defaultMaxTries;
	}
	/**
	 * get a TurnoutOperator instance for this operation
	 * @return	the operator
	 */
	public TurnoutOperator getOperator(AbstractTurnout t) {
		return new SensorTurnoutOperator(t, interval, maxTries);
	}
	
	
    static Logger log = Logger.getLogger(SensorTurnoutOperation.class.getName());
}

/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import jmri.TurnoutOperation;

/**
 * Configuration for RawTurnoutOperation class
 * All the work is done by the Common... class
 * Based on NoFeedbackTurountOperationConfig.java
 * @author Paul Bender Copyright 2008
 *
 */
public class RawTurnoutOperationConfig extends CommonTurnoutOperationConfig {

	/**
	 * Create the config JPanel, if there is one, to configure this operation type
	 */
	public RawTurnoutOperationConfig(TurnoutOperation op) {
		super(op);
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RawTurnoutOperationConfig.class.getName());
}

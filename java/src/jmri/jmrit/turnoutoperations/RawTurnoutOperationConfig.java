/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import org.apache.log4j.Logger;
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
	
	static Logger log = Logger.getLogger(RawTurnoutOperationConfig.class.getName());
}

/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	static Logger log = LoggerFactory.getLogger(RawTurnoutOperationConfig.class.getName());
}

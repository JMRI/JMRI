/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.TurnoutOperation;

/**
 * Configuration for NoFeedbackTurnoutOperation class
 * All the work is done by the Common... class
 * @author John Harper	Copyright 2005
 *
 */
public class NoFeedbackTurnoutOperationConfig extends CommonTurnoutOperationConfig {

	/**
	 * Create the config JPanel, if there is one, to configure this operation type
	 */
	public NoFeedbackTurnoutOperationConfig(TurnoutOperation op) {
		super(op);
	}
	
	static Logger log = LoggerFactory.getLogger(NoFeedbackTurnoutOperationConfig.class.getName());
}

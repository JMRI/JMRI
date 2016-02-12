/**
 *
 */
package jmri.jmrit.turnoutoperations;

import jmri.TurnoutOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for NoFeedbackTurnoutOperation class All the work is done by
 * the Common... class
 *
 * @author John Harper	Copyright 2005
 *
 */
public class NoFeedbackTurnoutOperationConfig extends CommonTurnoutOperationConfig {

    /**
     *
     */
    private static final long serialVersionUID = -8073858688153354354L;

    /**
     * Create the config JPanel, if there is one, to configure this operation
     * type
     */
    public NoFeedbackTurnoutOperationConfig(TurnoutOperation op) {
        super(op);
    }

    private final static Logger log = LoggerFactory.getLogger(NoFeedbackTurnoutOperationConfig.class.getName());
}

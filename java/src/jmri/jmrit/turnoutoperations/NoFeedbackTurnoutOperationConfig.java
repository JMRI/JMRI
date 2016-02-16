/**
 *
 */
package jmri.jmrit.turnoutoperations;

import jmri.TurnoutOperation;

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
}

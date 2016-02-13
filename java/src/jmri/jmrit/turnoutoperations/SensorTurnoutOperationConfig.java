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
public class SensorTurnoutOperationConfig extends CommonTurnoutOperationConfig {

    /**
     *
     */
    private static final long serialVersionUID = -6249075363448607648L;

    /**
     * Create the config JPanel, if there is one, to configure this operation
     * type
     */
    public SensorTurnoutOperationConfig(TurnoutOperation op) {
        super(op);
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTurnoutOperationConfig.class.getName());
}

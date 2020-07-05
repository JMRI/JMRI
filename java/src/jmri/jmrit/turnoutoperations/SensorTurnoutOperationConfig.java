package jmri.jmrit.turnoutoperations;

import jmri.TurnoutOperation;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Configuration for NoFeedbackTurnoutOperation class All the work is done by
 * the Common... class
 *
 * @author John Harper Copyright 2005
 */
@API(status = MAINTAINED)
public class SensorTurnoutOperationConfig extends CommonTurnoutOperationConfig {

    /**
     * Create the config JPanel, if there is one, to configure this operation
     * type
     * @param op turnout operation.
     */
    public SensorTurnoutOperationConfig(TurnoutOperation op) {
        super(op);
    }
}

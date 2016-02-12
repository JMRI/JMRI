/**
 *
 */
package jmri.jmrit.turnoutoperations;

import jmri.TurnoutOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for RawTurnoutOperation class All the work is done by the
 * Common... class Based on NoFeedbackTurountOperationConfig.java
 *
 * @author Paul Bender Copyright 2008
 *
 */
public class RawTurnoutOperationConfig extends CommonTurnoutOperationConfig {

    /**
     *
     */
    private static final long serialVersionUID = -4748428194151474727L;

    /**
     * Create the config JPanel, if there is one, to configure this operation
     * type
     */
    public RawTurnoutOperationConfig(TurnoutOperation op) {
        super(op);
    }

    private final static Logger log = LoggerFactory.getLogger(RawTurnoutOperationConfig.class.getName());
}

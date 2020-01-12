package jmri;

import jmri.implementation.AbstractTurnout;
import jmri.implementation.NoFeedbackTurnoutOperator;

/**
 * NoFeedBackTurnoutOperation class - specialization of TurnoutOperation to
 * provide automatic retry for a turnout with no feedback.
 *
 * @author John Harper
 */
public class NoFeedbackTurnoutOperation extends CommonTurnoutOperation {

    // This class can deal with ANY feedback mode, although it may not be the best one
    final int feedbackModes
            = AbstractTurnout.DIRECT | AbstractTurnout.ONESENSOR | AbstractTurnout.TWOSENSOR | AbstractTurnout.INDIRECT | AbstractTurnout.EXACT | AbstractTurnout.MONITORING;

    /*
     * Default values and constraints.
     */
    static public final int defaultInterval = 300;
    static public final int defaultMaxTries = 2;

    public NoFeedbackTurnoutOperation(String n, int i, int mt) {
        super(n, i, mt);
        setFeedbackModes(feedbackModes);
    }

    /**
     * Constructor with default values - this creates the "defining instance" of
     * the operation type hence it cannot be deleted.
     */
    public NoFeedbackTurnoutOperation() { this("NoFeedback", defaultInterval, defaultMaxTries); }

    /**
     * Return clone with different name.
     */
    @Override
    public TurnoutOperation makeCopy(String n) {
        return new NoFeedbackTurnoutOperation(n, interval, maxTries);
    }

    @Override
    public int getDefaultInterval() {
        return defaultInterval;
    }

    @Override
    public int getDefaultMaxTries() {
        return defaultMaxTries;
    }

    static public int getDefaultIntervalStatic() {
        return defaultInterval;
    }

    static public int getDefaultMaxTriesStatic() {
        return defaultMaxTries;
    }

    /**
     * Get a TurnoutOperator instance for this operation.
     *
     * @return the operator
     */
    @Override
    public TurnoutOperator getOperator(AbstractTurnout t) {
        return new NoFeedbackTurnoutOperator(t, interval, maxTries);
    }

}

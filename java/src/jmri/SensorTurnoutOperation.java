package jmri;

import jmri.implementation.AbstractTurnout;
import jmri.implementation.SensorTurnoutOperator;

/**
 * SensorTurnoutOperation class - specialization of TurnoutOperation to provide
 * automatic retry for a turnout with explicit feedback from sensor(s).
 *
 * @author John Harper Copyright 2005
 */
public class SensorTurnoutOperation extends CommonTurnoutOperation {

    // This class can deal with explicit feedback modes
    final int feedbackModes = AbstractTurnout.ONESENSOR | AbstractTurnout.TWOSENSOR | AbstractTurnout.EXACT | AbstractTurnout.INDIRECT;

    /*
     * Default values and constraints.
     */
    static public final int defaultInterval = 300;
    static public final int defaultMaxTries = 3;

    public SensorTurnoutOperation(String n, int i, int mt) {
        super(n, i, mt);
        setFeedbackModes(feedbackModes);
    }

    /**
     * Constructor with default values - this creates the "defining instance" of
     * the operation type hence it cannot be deleted.
     */
    public SensorTurnoutOperation() {
        this("Sensor", defaultInterval, defaultMaxTries);
    }

    /**
     * Return clone with different name.
     */
    @Override
    public TurnoutOperation makeCopy(String n) {
        return new SensorTurnoutOperation(n, interval, maxTries);
    }

    @Override
    public int getDefaultInterval() {
        return defaultInterval;
    }

    @Override
    public int getDefaultMaxTries() {
        return defaultMaxTries;
    }

    /**
     * Get a TurnoutOperator instance for this operation.
     *
     * @return the operator
     */
    @Override
    public TurnoutOperator getOperator(AbstractTurnout t) {
        return new SensorTurnoutOperator(t, interval, maxTries);
    }

}

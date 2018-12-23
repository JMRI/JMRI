package jmri;

import jmri.implementation.AbstractTurnout;

/**
 * Specialization of TurnoutOperation to contain
 * common properties and methods for concrete subclasses.
 *
 * @author John Harper
 */
public abstract class CommonTurnoutOperation extends TurnoutOperation {

    /*
     * Parameters of this object
     */
    int interval;     // time between attempts
    int maxTries;     // no of times to try

    /*
     * Default values and constraints
     */
    static public final int minInterval = 100;
    static public final int maxInterval = 5000;  // let's not get silly...
    static public final int intervalStepSize = 50;
    static public final int minMaxTries = 1;
    static public final int maxMaxTries = 10;

    public CommonTurnoutOperation(String n, int i, int mt) {
        super(n);
        interval = getDefaultInterval();
        maxTries = getDefaultMaxTries();
        setInterval(i);
        setMaxTries(mt);
    }

    /**
     * Get a TurnoutOperator instance for this operation.
     *
     * @return the operator
     */
    @Override
    public abstract TurnoutOperator getOperator(AbstractTurnout t);

    public int getInterval() {
        return interval;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public abstract int getDefaultInterval();

    public abstract int getDefaultMaxTries();

    @Override
    public boolean equivalentTo(TurnoutOperation other) {
        if (this.getClass() == other.getClass()) {
            return interval == ((CommonTurnoutOperation) other).interval
                    && maxTries == ((CommonTurnoutOperation) other).maxTries;
        } else {
            return false;
        }
    }

    /**
     * Set new value for interval. Do nothing if not in range.
     *
     * @param newInterval new retry interval time
     */
    public void setInterval(int newInterval) {
        if (newInterval >= minInterval && newInterval <= maxInterval) {
            interval = newInterval;
        }
    }

    /**
     * Set new value for MaxTries. Do nothing if not in range.
     *
     * @param newMaxTries new maximum number of retries
     */
    public void setMaxTries(int newMaxTries) {
        if (newMaxTries >= minMaxTries && newMaxTries <= maxMaxTries) {
            maxTries = newMaxTries;
        }
    }

}

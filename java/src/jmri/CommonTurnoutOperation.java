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

    /**
     * Create common properties for Turnout Operation.
     * @param name Operator Name.
     * @param interval Interval between retries.
     * @param maxTries maximum retry attempts.
     */
    public CommonTurnoutOperation(String name, int interval, int maxTries) {
        super(name);
        init(interval, maxTries);
    }

    private void init(int interval, int maxTries) {
        setInterval(getDefaultInterval()); // set to default just in case
        setInterval(interval); // this might not be a valid number.
        setMaxTries(getDefaultMaxTries()); // set to default just in case
        setMaxTries(maxTries); // this might not be a valid number.
        InstanceManager.getDefault(TurnoutOperationManager.class).addOperation(this);
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
        if (other!= null && this.getClass() == other.getClass()) {
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
            return;
        }
        log.warn("Could not set {} Interval to {}, out of range {}-{}",
            name, newInterval, minInterval, maxInterval);
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommonTurnoutOperation.class);

}

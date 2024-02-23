package jmri;

import jmri.implementation.AbstractTurnout;
import jmri.implementation.RawTurnoutOperator;

/**
 * RawTurnoutOperation class - specialization of TurnoutOperation to provide
 * automatic retry for a turnout with no feedback by sending raw NMRA commands
 * to the turnout. This class is based on the NoTurnoutOperation class.
 *
 * @author Paul Bender
 */
public class RawTurnoutOperation extends CommonTurnoutOperation {

    // This class should only be used with DIRECT, ONESENSOR or TWOSENSOR
    // feedback modes.
    static final int SUPPORTED_FEEDBACK_MODES
            = Turnout.DIRECT | Turnout.EXACT | Turnout.INDIRECT
            | Turnout.ONESENSOR | Turnout.TWOSENSOR | Turnout.LNALTERNATE ;

    /*
     * Default values and constraints.
     */
    static public final int defaultInterval = 300;
    static public final int defaultMaxTries = 1;

    public RawTurnoutOperation(String n, int i, int mt) {
        super(n, i, mt);
        setFeedbackModes(SUPPORTED_FEEDBACK_MODES);
    }

    /**
     * Constructor with default values - this creates the "defining instance" of
     * the operation type hence it cannot be deleted.
     */
    public RawTurnoutOperation() { this("Raw", defaultInterval, defaultMaxTries); }

    /**
     * Return clone with different name.
     */
    @Override
    public TurnoutOperation makeCopy(String n) {
        return new RawTurnoutOperation(n, interval, maxTries);
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
        return new RawTurnoutOperator(t, interval, maxTries);
    }

    @Override
    public String getToolTip(){
        return Bundle.getMessage("TurnoutOperationRawTip");
    }

}

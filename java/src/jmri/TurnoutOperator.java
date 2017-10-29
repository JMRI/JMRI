package jmri;

import jmri.implementation.AbstractTurnout;

/**
 * Some (not much) common machinery for the concrete turnout operator classes.
 *
 * @author John Harper Copyright 2005
 *
 */
public abstract class TurnoutOperator extends Thread {

    protected AbstractTurnout myTurnout;

    protected TurnoutOperator(AbstractTurnout t) {
        myTurnout = t;
        setName("Operating turnout " + t.getSystemName());
    }

    protected void operatorCheck() throws TurnoutOperatorException {
        if (myTurnout.getCurrentOperator() != this) {
            throw new TurnoutOperatorException();
        }
    }

    /**
     * Exception thrown when the turnout's operator has changed while the
     * operator is running. This implies that another operation has been started
     * and that this one should just quietly stop doing its thing.
     */
    static public class TurnoutOperatorException extends Exception {
    }
}

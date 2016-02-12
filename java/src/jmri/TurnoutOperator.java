/**
 *
 */
package jmri;

import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some (not much) common machinery for the concrete turnout operator classes.
 *
 * @author John Harper	Copyright 2005
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
    static public class TurnoutOperatorException extends java.lang.Exception {

        /**
         *
         */
        private static final long serialVersionUID = -9039683362922025389L;
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutOperator.class.getName());
}

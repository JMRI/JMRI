/**
 * 
 */
package jmri;


/**
 * Some (not much) common machinery for the concrete turnout operator
 * classes.
 * @author John Harper	Copyright 2005
 *
 */
public abstract class TurnoutOperator extends Thread {
	
	AbstractTurnout myTurnout;
	
	TurnoutOperator(AbstractTurnout t) {
		myTurnout = t;
		setName("Operating turnout "+t.getSystemName());
	}
	
	/**
	 * Exception thrown when the turnout's operator has changed while the
	 * operator is running. This implies that another operation has been
	 * started and that this one should just quietly stop doing its thing.
	 */
	class TurnoutOperatorException extends java.lang.Exception { };
	
	protected void operatorCheck() throws TurnoutOperatorException {
		if (myTurnout.getCurrentOperator()!= this) {
			throw new TurnoutOperatorException();
		}
	}
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutOperator.class.getName());
}

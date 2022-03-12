package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG male AnalogExpressionBean socket.
 */
public interface MaleAnalogExpressionSocket
        extends MaleSocket, AnalogExpressionBean {

    /**
     * {@inheritDoc}
     * <P>
     * This method must ensure that the result is not a Double.NaN, negative
     * infinity or positive infinity. If that is the case, it must throw an
     * IllegalArgumentException before checking if an error has occured.
     * <P>
     * If the socket is not enabled, the method returns the value 0.0f.
     * <P>
     * If an error occurs and are handled, the method returns the value 0.0f.
     * @throws JmriException when an exception occurs
     * @throws IllegalArgumentException if the result a Double.NaN, negative
     * infinity or positive infinity
     */
    @Override
    public double evaluate() throws JmriException;
    
}

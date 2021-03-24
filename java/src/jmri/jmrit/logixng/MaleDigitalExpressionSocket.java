package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG male DigitalExpressionBean socket.
 */
public interface MaleDigitalExpressionSocket
        extends MaleSocket, DigitalExpressionBean {

    /**
     * {@inheritDoc}
     * <P>
     * This method must ensure that the value is not a Double.NaN, negative
     * infinity or positive infinity. If that is the case, it must throw an
     * IllegalArgumentException before checking if an error has occured.
     */
    @Override
    public boolean evaluate() throws JmriException;
    
    /**
     * Get the last result of the evaluation.
     * @return the last result
     */
    public boolean getLastResult();
    
}

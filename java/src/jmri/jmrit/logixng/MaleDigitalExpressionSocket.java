package jmri.jmrit.logixng;

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
    public boolean evaluate() throws Exception;
    
}

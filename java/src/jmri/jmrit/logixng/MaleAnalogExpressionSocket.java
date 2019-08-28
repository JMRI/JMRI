package jmri.jmrit.logixng;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

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
     */
    @Override
    public double evaluate();

}

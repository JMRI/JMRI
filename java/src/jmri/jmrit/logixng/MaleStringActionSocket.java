package jmri.jmrit.logixng;

import javax.annotation.Nonnull;
import jmri.JmriException;

/**
 * A LogixNG male StringActionBean socket.
 */
public interface MaleStringActionSocket
        extends MaleSocket, StringActionBean {

    /**
     * {@inheritDoc}
     * <P>
     * This method must ensure that the value is not a Double.NaN, negative
     * infinity or positive infinity. If that is the case, it must throw an
     * IllegalArgumentException before checking if an error has occured.
     */
    @Override
    public void setValue(@Nonnull String value) throws JmriException;
    
}

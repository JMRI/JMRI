package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG male DigitalBooleanActionBean socket.
 */
public interface MaleDigitalBooleanAction
        extends MaleSocket, DigitalBooleanActionBean {

    /**
     * {@inheritDoc}
     * <P>
     * This method must ensure that the value is not a Double.NaN, negative
     * infinity or positive infinity. If that is the case, it must throw an
     * IllegalArgumentException before checking if an error has occured.
     */
    @Override
    public void execute(boolean hasChangedToTrue) throws JmriException;
    
}

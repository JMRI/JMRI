package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG male AnalogActionBean socket.
 */
public interface MaleAnalogActionSocket
        extends MaleSocket, AnalogActionBean {

    /**
     * {@inheritDoc}
     * <P>
     * This method must ensure that the value is not a Double.NaN, negative
     * infinity or positive infinity. If that is the case, it must throw an
     * IllegalArgumentException before checking if an error has occured.
     */
    @Override
    public void setValue(double value) throws JmriException;

}

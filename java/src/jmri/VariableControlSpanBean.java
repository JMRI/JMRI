package jmri;

import javax.annotation.CheckReturnValue;

/**
 * Interface for {@link NamedBean} indicating that the bean may control more than one output.
 * <p>
 * Originally just used by {@link Turnout},
 * this is available for any bean type with variable output span.
 *
 * @author Bob Jacobsen Copyright 2022
 */
public interface VariableControlSpanBean extends NamedBean {

    /**
     * Provide the number of input/output bits this bean controls.
     * <p>
     * Typically just one, some systems provide outputs that control two outputs, e.g. C/MRI.
     *
     * @return the number of bits
     */
    @CheckReturnValue
    int getNumberControlBits();

    /**
     * Set number of input/output bits this bean controls.
     *
     * @param num the size of the input/output, currently 1 or 2
     */
    @InvokeOnLayoutThread
    void setNumberControlBits(int num);

}

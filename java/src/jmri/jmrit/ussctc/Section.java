package jmri.jmrit.ussctc;

import jmri.*;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 * <p>
 * The type argument defines the communications from central to field and from field to central
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface Section<To extends Enum<To>, From extends Enum<To>> {

    /**
     * Inform CTC machine part of section that a code-send operation (from machine to field)
     * has started.
     * @return The value to be conveyed to the field.
     */
    public To codeSendStart();
    
    /**
     * Provide the value to the field unit at the end of the code-send operation.
     */
    public void codeValueDelivered(To value);    
    
    /**
     * Inform field part of section that an indication operation (from field to machine)
     * has started.
     * @return The value to be conveyed to the CTC machine.
     */
    public From indicationStart();

    /**
     * Provide value to the CTC machine at the end of the indication-send operation
     */
    public void indicationComplete(From value);    
    
    
}

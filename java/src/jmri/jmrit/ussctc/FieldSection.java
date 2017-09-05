package jmri.jmrit.ussctc;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 * <p>
 * The type argument defines the communications from central to field and from field to central
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface FieldSection<To extends Enum<To>, From extends Enum<From>> {

    /**
     * Provide the transferred value to the field unit at the end of the code-send operation.
     */
    public void codeValueDelivered(To value);    
    
    /**
     * Inform field part of section that an indication operation (from field to machine)
     * has started and obtain the value to be conveyed to the central CTC machine.
     * @return The value to be conveyed to the central CTC machine.
     */
    public From indicationStart();
    
}

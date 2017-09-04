package jmri.jmrit.ussctc;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 * <p>
 * The type argument defines the communications from central to field and from field to central
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface CentralSection<To extends Enum<To>, From extends Enum<From>> {

    /**
     * Inform central CTC machine part of section that a code-send operation (from central to field)
     * has started and obtain the value to be sent over the line.
     * @return The value to be conveyed to the field.
     */
    public To codeSendStart();
    
    /**
     * Provides the code sequence to the central CTC machine at the end of the indication-send operation
     */
    public void indicationComplete(From value);    
    
}

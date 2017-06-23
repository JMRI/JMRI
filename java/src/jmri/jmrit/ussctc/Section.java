package jmri.jmrit.ussctc;

import jmri.*;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface Section {

    /**
     * Inform CTC machine part of section that a code-send operation (from machine to field)
     * has started.
     * @return The value to be conveyed to the field.
     */
    public Station.Value codeSendStart();
    
    /**
     * Provide the value to the field unit at the end of the code-send operation.
     */
    public void codeValueDelivered(Station.Value value);    
    
    /**
     * Inform field part of section that an indication operation (from field to machine)
     * has started.
     * @return The value to be conveyed to the CTC machine.
     */
    public Station.Value indicationStart();

    /**
     * Provide value to the CTC machine at the end of the indication-send operation
     */
    public void indicationComplete(Station.Value value);    
    
    
}

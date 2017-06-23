package jmri.jmrit.ussctc;

import jmri.*;

/**
 * A section is the base type for the pieces that made up a {@link Column}.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface Section {

    /**
     * Inform CTC machine part of section that a code-send operation (from machine to field)
     * has started.
     */
    public Station.Value codeSendStart();
    
    /**
     * Provide value to the field unit
     */
    public void codeValueDelivered(Station.Value value);    
    
    /**
     * Indication starting
     */
    public Station.Value indicationStart();

    /**
     * Provide value to the field unit
     */
    public void indicationComplete(Station.Value value);    
    
    
}

package jmri.jmrit.ussctc;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 * <p>
 * The type argument defines the communications from central to field and from field to central
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@API(status = MAINTAINED)
public interface CentralSection<To extends Enum<To>, From extends Enum<From>> {

    /**
     * Inform central CTC machine part of section that a code-send operation (from central to field)
     * has started and obtain the value to be sent over the line.
     * @return The value to be conveyed to the field.
     */
    public To codeSendStart();
    
    /**
     * Provides the code sequence to the central CTC machine at the end of the indication-send operation.
     * @param value to be conveyed.
     */
    public void indicationComplete(From value);    
    
}

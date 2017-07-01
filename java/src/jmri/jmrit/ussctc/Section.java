package jmri.jmrit.ussctc;

import jmri.*;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 * It combines a {@link jmri.jmrit.ussctc.CentralSection} and a {@link jmri.jmrit.ussctc.FieldSection}
 * into one for convenience and consistency.
 * <p>
 * The type argument defines the communications from central to field and from field to central
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface Section<To extends Enum<To>, From extends Enum<From>>
                 extends CentralSection<To, From>, FieldSection<To, From> {
    
}

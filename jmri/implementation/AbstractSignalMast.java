// AbstractSignalMast.java

package jmri.implementation;

import java.util.ResourceBundle;
import jmri.*;
 /**
 * Abstract class providing the basic logic of the SignalMast interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */
public abstract class AbstractSignalMast extends AbstractNamedBean
    implements SignalMast, java.io.Serializable {

    public AbstractSignalMast(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalMast(String systemName) {
        super(systemName);
    }
        
}

/* @(#)AbstractSignalMast.java */

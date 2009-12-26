// SingleHeadSignalMast.java

package jmri.implementation;

import java.util.ResourceBundle;

import jmri.*;
import jmri.util.NamedBeanHandle;

 /**
 * SignalMast implemented via one SignalHead object
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */
public class SingleHeadSignalMast extends AbstractSignalMast {

    public SingleHeadSignalMast(String systemName, String userName) {
        super(systemName, userName);
    }

    public SingleHeadSignalMast(String systemName) {
        super(systemName);
    }
        
    public void setAspect(String aspect) { 
        // set the outputs
        
        // do standard processing
        super.setAspect(aspect);
    }

    NamedBeanHandle<SignalHead> head;
    SignalAppearanceMap map;
    
}

/* @(#)SingleHeadSignalMast.java */

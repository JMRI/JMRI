// DefaultSignalMastManager.java

package jmri.managers;

import jmri.*;
import jmri.implementation.AbstractManager;


/**
 * Default implementation of a SignalMastManager.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author  Bob Jacobsen Copyright (C) 2009
 * @version	$Revision: 1.1 $
 */
public class DefaultSignalMastManager extends AbstractManager
    implements SignalMastManager, java.beans.PropertyChangeListener {

    public DefaultSignalMastManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'F'; }

    public SignalMast getSignalMast(String name) {
        if (name==null || name.length()==0) { return null; }
        SignalMast t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalMast getBySystemName(String key) {
		String name = key.toUpperCase();
        return (SignalMast)_tsys.get(name);
    }

    public SignalMast getByUserName(String key) {
        return (SignalMast)_tuser.get(key);
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalMastManager.class.getName());
}

/* @(#)DefaultSignalMastManager.java */

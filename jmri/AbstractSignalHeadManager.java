// AbstractSignalHeadManager.java

package jmri;

import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;


/**
 * Abstract partial implementation of a SignalHeadManager.
 * <P>
 * Not truly an abstract class, this might have been better named
 * DefaultSignalHeadManager.  But we've got it here for the eventual
 * need to provide system-specific implementations.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.10 $
 */
public class AbstractSignalHeadManager extends AbstractManager
    implements SignalHeadManager, java.beans.PropertyChangeListener {

    public AbstractSignalHeadManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'H'; }


    public SignalHead getBySystemName(String key) {
        return (SignalHead)_tsys.get(key);
    }

    public SignalHead getByUserName(String key) {
        return (SignalHead)_tuser.get(key);
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractSignalHeadManager.class.getName());
}

/* @(#)AbstractSignalHeadManager.java */

// AbstractTurnoutManager.java

package jmri;

import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;


/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.12 $
 */
public abstract class AbstractTurnoutManager extends AbstractManager
    implements TurnoutManager, java.beans.PropertyChangeListener {

    // abstract methods to be provided by subclasses
    public abstract Turnout newTurnout(String systemName, String userName);

    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        _tsys.clear();
        _tuser.clear();
    }

    public char typeLetter() { return 'T'; }

    // implemented methods

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new turnout using this as a
     * default name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Turnout getTurnout(String name) {
        Turnout t = getByUserName(name);
        if (t!=null) return t;

        t = getBySystemName(name);
        if (t!=null) return t;

        // did not exist under either name; create via a default
        // of either a valid system name, or a number that can create one
        return newTurnout(null, name);
    }

    protected Hashtable _tsys = new Hashtable();   // stores known Turnout instances by system name
    protected Hashtable _tuser = new Hashtable();   // stores known Turnout instances by user name

    public Turnout getBySystemName(String key) {
        return (Turnout)_tsys.get(key);
    }

    public Turnout getByUserName(String key) {
        return (Turnout)_tuser.get(key);
    }

    protected String prefix;

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter() { return prefix.charAt(0); }

    // keep track of Turnout user name changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();
            String now = (String) e.getNewValue();
            Turnout t = getByUserName(old);
            log.debug("change user name from ("+old+") to ("+now+")");
            _tuser.remove(old);
            _tuser.put(now, t);
        }
    }

    public List getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List out = new ArrayList();
        Enumeration en = _tsys.elements();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = ((Turnout)en.nextElement()).getSystemName();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractTurnoutManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */

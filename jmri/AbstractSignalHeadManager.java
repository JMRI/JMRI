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
 * @version	$Revision: 1.9 $
 */
public class AbstractSignalHeadManager extends AbstractManager
    implements SignalHeadManager, java.beans.PropertyChangeListener {

    public AbstractSignalHeadManager() {
        // register the result for later configuration
        InstanceManager.configureManagerInstance().registerConfig(this);
        log.debug("register");
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'H'; }


    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        InstanceManager.configureManagerInstance().deregister(this);
        _tsys.clear();
        _tuser.clear();
    }

    // implemented methods
    protected Hashtable _tsys = new Hashtable();   // stores known instances by system name
    protected Hashtable _tuser = new Hashtable();   // stores known instances by user name

    public SignalHead getBySystemName(String key) {
        return (SignalHead)_tsys.get(key);
    }

    public SignalHead getByUserName(String key) {
        return (SignalHead)_tuser.get(key);
    }

    /**
     * Remember a SignalHead created outside the manager.
     */
    public void register(SignalHead s) {
        String systemName = s.getSystemName();
        if (getBySystemName(systemName)!=null)
            log.debug("SignalHead systemname="+systemName+" already registered");
        else
            _tsys.put(systemName, s);
        String userName = s.getUserName();
        if (log.isDebugEnabled()) log.debug("Register "+systemName+" "+userName);
        if (userName != null) {
            if (getByUserName(userName)!=null)
                log.debug("SignalHead username="+userName+" already registered");
            else
                _tuser.put(userName, s);
        }
        firePropertyChange("length", null, new Integer(_tsys.size()));
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of SignalHead user name changes.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered SignalHead objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();
            String now = (String) e.getNewValue();
            SignalHead t = getByUserName(old);
            _tuser.remove(old);
            _tuser.put(now, t);
        }
    }

    public List getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List out = new ArrayList();
        Enumeration en = _tsys.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = (String)en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractSignalHeadManager.class.getName());

}

/* @(#)AbstractSignalHeadManager.java */

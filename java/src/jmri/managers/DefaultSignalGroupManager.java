// DefaultSignalGroupManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.managers.AbstractManager;
import jmri.implementation.DefaultSignalGroup;

import java.io.*;

import java.util.List;
import java.util.ArrayList;


/**
 * Default implementation of a SignalGroupManager.
 * <P>
 * This loads automatically the first time used.
 * <p>
 *
 *
 * @author  Bob Jacobsen Copyright (C) 2009
 * @version	$Revision$
 */
public class DefaultSignalGroupManager extends AbstractManager
    implements SignalGroupManager, java.beans.PropertyChangeListener {

    public DefaultSignalGroupManager() {
        super();
        
        // load when created, which will generally
        // be the first time referenced
        //load();
    }
    
    public int getXMLOrder(){
        return Manager.SIGNALGROUPS;
    }

    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'F'; }
    
    public SignalGroup getSignalGroup(String name) {
        SignalGroup t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalGroup getBySystemName(String key) {
        return (SignalGroup)_tsys.get(key);
    }

    public SignalGroup getByUserName(String key) {
        return (SignalGroup)_tuser.get(key);
    }
    
    public SignalGroup newSignalGroup(String sys){
        SignalGroup g;
        g = new DefaultSignalGroup(sys);
        register(g);
        return g;
    
    }
    
    public SignalGroup provideSignalGroup(String systemName, String userName) {
        SignalGroup r;
        r = getByUserName(systemName);
        if (r!=null) return r;
        r = getBySystemName(systemName);
        if (r!=null) return r;
        // Route does not exist, create a new group
		r = new DefaultSignalGroup(systemName,userName);
		// save in the maps
		register(r);
		return r;
    }

    List<String> getListOfNames() {
        List<String> retval = new ArrayList<String>();
        // first locate the signal system directory
        // and get names of systems
        File signalDir = new File("xml"+File.separator+"signals");
        File[] files = signalDir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                // check that there's an aspects.xml file
                File aspects = new File(files[i].getPath()+File.separator+"aspects.xml");
                if (aspects.exists()) {
                    log.debug("found system: "+files[i].getName());
                    retval.add(files[i].getName());
                }
            }
        }
        return retval;
    }
    
        
    static DefaultSignalGroupManager _instance = null;
    static public DefaultSignalGroupManager instance() {
        if (_instance == null) {
            _instance = new DefaultSignalGroupManager();
        }
        return (_instance);
    }
    
    public void deleteSignalGroup(SignalGroup s) {
        deregister(s);
    }

    static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManager.class.getName());
}

/* @(#)DefaultSignalGroupManager.java */

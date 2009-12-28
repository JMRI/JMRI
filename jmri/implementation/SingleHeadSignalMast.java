// SingleHeadSignalMast.java

package jmri.implementation;

import java.util.*;

import jmri.*;
import jmri.util.NamedBeanHandle;

 /**
 * SignalMast implemented via one SignalHead object.
 * <p>
 * System name specifies the creation information:
<pre>
IF:basic:one-searchlight:IH1
</pre>
 * The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>IH1 - colon-separated list of names for SignalHeads
 * </ul>
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.4 $
 */
public class SingleHeadSignalMast extends AbstractSignalMast {

    public SingleHeadSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public SingleHeadSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
    }
        
    void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) { 
            log.error("SignalMast system name needs at least three parts: "+systemName);
            throw new IllegalArgumentException("System name needs at least three parts: "+systemName);
        }
        if (!parts[0].equals("IF$shsm"))
            log.warn("SignalMast system name should start with IF: "+systemName);
        // checking complete, init
        configureSignalSystemDefinition(parts[1]);
        configureAspectTable(parts[1], parts[2]);
        configureHeads(parts);
    }
    
    void configureSignalSystemDefinition(String name) {
        systemDefn = InstanceManager.signalSystemManagerInstance().getSystem(name);
        if (systemDefn == null) {
            log.error("Did not find signal definition: "+name);
            throw new IllegalArgumentException("Signal definition not found: "+name);
        }
    }
    
    void configureAspectTable(String signalSystemName, String aspectMapName) {
        map = DefaultSignalAppearanceMap.getMap(signalSystemName, aspectMapName);
    }
    
    void configureHeads(String parts[]) {
        heads = new ArrayList<NamedBeanHandle<SignalHead>>();
        for (int i = 3; i < parts.length; i++) {
            NamedBeanHandle<SignalHead> s 
                = new NamedBeanHandle<SignalHead>(parts[i],
                        InstanceManager.signalHeadManagerInstance().getSignalHead(parts[i]));
            heads.add(s);
        }
    }
    
    public void setAspect(String aspect) { 
        // check it's a choice
        if ( !map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: "+aspect);
            throw new IllegalArgumentException("attempting to set invalid aspect: "+aspect);
        }
        
        // set the outputs
        map.setAppearances(aspect, heads);
        // do standard processing
        super.setAspect(aspect);
    }

    public Vector<String> getValidAspects() {
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    public SignalSystem getSignalSystem() {
        return systemDefn;
    }

    List<NamedBeanHandle<SignalHead>> heads;
    DefaultSignalAppearanceMap map;
    SignalSystem systemDefn;
    
    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleHeadSignalMast.class.getName());
}

/* @(#)SingleHeadSignalMast.java */

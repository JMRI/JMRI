// SignalHeadSignalMast.java
package jmri.implementation;
import java.util.*;

import jmri.*;
import jmri.util.NamedBeanHandle;

 /**
 * SignalMast implemented via one SignalHead object.
 * <p>
 * System name specifies the creation information:
<pre>
IF:basic:one-searchlight:(IH1)(IH2)
</pre>
 * The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(IH1)(IH2) - colon-separated list of names for SignalHeads
 * </ul>
 * There was an older form where the names where colon separated:  IF:basic:one-searchlight:IH1:IH2
 * This was deprecated because colons appear in e.g. SE8c system names.
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>IH1:IH2 - colon-separated list of names for SignalHeads
 * </ul>
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.11 $
 */
public class SignalHeadSignalMast extends AbstractSignalMast {

    public SignalHeadSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public SignalHeadSignalMast(String systemName) {
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
        if (!parts[0].equals("IF$shsm")) {
            log.warn("SignalMast system name should start with IF: "+systemName);
        }
        String prefix = parts[0];
        String system = parts[1];
        String mast = parts[2];

        // if "mast" contains (, it's new style
        if (mast.indexOf('(') == -1) {
            // old style
            configureSignalSystemDefinition(system);
            configureAspectTable(system, mast);
            configureSpecificAppearance(system, mast);
            configureAspectRelationMapTable(system, mast);
            configureHeads(parts, 3);
        } else {
            // new style
            mast = mast.substring(0, mast.indexOf("("));
            String interim = systemName.substring(prefix.length()+1+system.length()+1);
            String parenstring = interim.substring(interim.indexOf("("), interim.length());
            java.util.List<String> parens = jmri.util.StringUtil.splitParens(parenstring);
            configureSignalSystemDefinition(system);
            configureAspectTable(system, mast);
            configureSpecificAppearance(system, mast);
            configureAspectRelationMapTable(system, mast);
            String[] heads = new String[parens.size()];
            int i=0;
            for (String p : parens) {
                heads[i] = p.substring(1, p.length()-1);
                i++;
            }
            configureHeads(heads, 0);            
        }
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
    
    void configureHeads(String parts[], int start) {
        heads = new ArrayList<NamedBeanHandle<SignalHead>>();
        for (int i = start; i < parts.length; i++) {
            String name = parts[i];
            NamedBeanHandle<SignalHead> s 
                = new NamedBeanHandle<SignalHead>(parts[i],
                        InstanceManager.signalHeadManagerInstance().getSignalHead(name));
            heads.add(s);
        }
    }   

    void configureAspectRelationMapTable(String signalSystemName, String aspectMapName) {
        aspectRelationMap = DefaultSignalAppearanceMap.getAspectRelationMap(signalSystemName, aspectMapName);
    }

    /**
    * Returns a list of postential aspects that we could set the signalmast to
    * given the state of the advanced signal mast.
    */
    public String[] getAspect(String advancedAspect){
        if (aspectRelationMap==null){
            log.error("aspect relationships have not been defined or loaded");
            throw new IllegalArgumentException("aspect relationships have not been defined or loaded");
        }
        if (advancedAspect==null){
            String[] danger = new String[1];
            danger[0] = getSpecificAppearance(jmri.implementation.DefaultSignalAppearanceMap.DANGER);
            return danger;
        }
        if(aspectRelationMap.containsKey(advancedAspect)){
            //String[] validAspects = aspectRelationMap.get(advancedAspect);
            return aspectRelationMap.get(advancedAspect);
        }
        return null;
    }

    void configureSpecificAppearance(String signalSystemName, String aspectMapName){
        specialMap = DefaultSignalAppearanceMap.getSpecificMap(signalSystemName, aspectMapName);
    }

    /**
    * Get the aspest to be displayed by this signalmast for a given appearance
    * @param appearance HELD = 0, PERMISSIVE = 1, DANGER = 2, DARK = 3;
    */
    public String getSpecificAppearance(int appearance){
        if (specialMap.containsKey(appearance)){
            return specialMap.get(appearance);
        }
        return null;
    }
    @Override
    public void setAspect(String aspect) { 
        // check it's a choice
        if ( !map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: "+aspect);
            throw new IllegalArgumentException("attempting to set invalid aspect: "+aspect);
        }
        
        // set the outputs
        if (log.isDebugEnabled()) log.debug("setAspect \""+aspect+"\", numHeads= "+heads.size());
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
    
    public SignalAppearanceMap getAppearanceMap() {
        return map;
    }
    
    @Override
    public void setHeld(boolean state) {
        // set all Heads to state
        for (NamedBeanHandle<SignalHead> h : heads) {
            h.getBean().setHeld(state);
        }
        super.setHeld(state);
    }

    @Override
    public void setLit(boolean state) {
        // set all Heads to state
        for (NamedBeanHandle<SignalHead> h : heads) {
            h.getBean().setLit(state);
        }
        super.setLit(state);
    }
    
    List<NamedBeanHandle<SignalHead>> heads;
    DefaultSignalAppearanceMap map;
    Hashtable<Integer, String> specialMap;
    Hashtable<String, String[]> aspectRelationMap;
    SignalSystem systemDefn;
    
    static final protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadSignalMast.class.getName());
}

/* @(#)SignalHeadSignalMast.java */

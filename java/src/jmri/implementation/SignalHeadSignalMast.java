// SignalHeadSignalMast.javaa
package jmri.implementation;
import java.util.*;

import jmri.*;
import jmri.NamedBeanHandle;

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
 * @version     $Revision$
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
            configureHeads(parts, 3);
        } else {
            // new style
            mast = mast.substring(0, mast.indexOf("("));
            String interim = systemName.substring(prefix.length()+1+system.length()+1);
            String parenstring = interim.substring(interim.indexOf("("), interim.length());
            java.util.List<String> parens = jmri.util.StringUtil.splitParens(parenstring);
            configureSignalSystemDefinition(system);
            configureAspectTable(system, mast);
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

    @Override
    public void setAspect(String aspect) { 
        // check it's a choice
        if ( !map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
        } else if (disabledAspects.contains(aspect)){
            log.warn("attempting to set an aspect that has been disabled: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: "+aspect+" on mast: "+getDisplayName());
        }
        
        // set the outputs
        if (log.isDebugEnabled()) log.debug("setAspect \""+aspect+"\", numHeads= "+heads.size());
        map.setAppearances(aspect, heads);
        // do standard processing
        super.setAspect(aspect);
    }
    
    /**
    * returns a list of all the valid aspects, that have not been disabled
    */
    public Vector<String> getValidAspects() {
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            if(!disabledAspects.contains(aspect))
                v.add(aspect);
        }
        return v;
    }
    
    /**
    * returns a list of all the known aspects for this mast, including those that have been disabled
    */
    public Vector<String> getAllKnownAspects(){
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }
    
    ArrayList<String> disabledAspects = new ArrayList<String>(1);
    
    public void setAspectDisabled(String aspect){
        if(aspect==null || aspect.equals(""))
            return;
        if(!map.checkAspect(aspect)){
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if(!disabledAspects.contains(aspect))
            disabledAspects.add(aspect);
    }
    
    public void setAspectEnabled(String aspect){
        if(aspect==null || aspect.equals(""))
            return;
        if(!map.checkAspect(aspect)){
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if(disabledAspects.contains(aspect))
            disabledAspects.remove(aspect);
    }
    
    public List<String> getDisabledAspects(){
        return disabledAspects;
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
            try {
                h.getBean().setHeld(state);
            } catch (java.lang.NullPointerException ex){
                log.error("NPE caused when trying to set Held due to missing signal head in mast " + getDisplayName());
            }
        }
        super.setHeld(state);
    }

    @Override
    public void setLit(boolean state) {
        // set all Heads to state
        for (NamedBeanHandle<SignalHead> h : heads) {
            try {
                h.getBean().setLit(state);
            }  catch (java.lang.NullPointerException ex){
                log.error("NPE caused when trying to set Dark due to missing signal head in mast " + getDisplayName());
            }
        }
        super.setLit(state);
    }
    
    List<NamedBeanHandle<SignalHead>> heads;
    DefaultSignalAppearanceMap map;
    SignalSystem systemDefn;
    
    public List<NamedBeanHandle<SignalHead>> getHeadsUsed(){
        return heads;
    }
    
    static final protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadSignalMast.class.getName());
}

/* @(#)SignalHeadSignalMast.java */

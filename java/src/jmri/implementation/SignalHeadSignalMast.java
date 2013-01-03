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
        setAppearances(aspect);
        // do standard processing
        super.setAspect(aspect);
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
    
    public List<NamedBeanHandle<SignalHead>> getHeadsUsed(){
        return heads;
    }
    
    //taken out of the defaultsignalappearancemap
    public void setAppearances(String aspect) {
        if (map==null){
            log.error("No appearance map defined, unable to set appearance " + getDisplayName());
            return;
        }
        if (map.getSignalSystem() !=null &&  map.getSignalSystem().checkAspect(aspect) && map.getAspectSettings(aspect)!=null)
            log.warn("Attempt to set "+getSystemName()+" to undefined aspect: "+aspect);
        else if ((map.getAspectSettings(aspect)!=null) && (heads.size() > map.getAspectSettings(aspect).length))
            log.warn("setAppearance to \""+aspect+"\" finds "+heads.size()+" heads but only "+map.getAspectSettings(aspect).length+" settings");
        
        int delay = 0;
        try {
            if(map.getProperty(aspect, "delay")!=null){
                delay = Integer.parseInt(map.getProperty(aspect, "delay"));
            }
        } catch (Exception e){
            log.debug("No delay set");
            //can be considered normal if does not exists or is invalid
        }
        HashMap<SignalHead, Integer> delayedSet = new HashMap<SignalHead, Integer>(heads.size());
        for (int i = 0; i < heads.size(); i++) {
            // some extensive checking
            boolean error = false;
            if (heads.get(i) == null){
                log.error("Null head "+i+" in setAppearances");
                error = true;
            }
            if (heads.get(i).getBean() == null){
                log.error("Could not get bean for head "+i+" in setAppearances");
                error = true;
            }
            if (map.getAspectSettings(aspect) == null){
                log.error("Couldn't get table array for aspect \""+aspect+"\" in setAppearances");
                error = true;
            }

            if(!error){
                SignalHead head = heads.get(i).getBean();
                int toSet = map.getAspectSettings(aspect)[i];
                if(delay == 0){
                    head.setAppearance(toSet);
                    if (log.isDebugEnabled()) log.debug("Setting "+head.getSystemName()+" to "+
                                    head.getAppearanceName(toSet));
                } else {
                    delayedSet.put(head, toSet);
                }
            }
            else
                log.error("head appearance not set due to an error");
        }
        if(delay!=0){
            //If a delay is required we will fire this off into a seperate thread and let it get on with it.
            final HashMap<SignalHead, Integer> thrDelayedSet = delayedSet;
            final int thrDelay = delay;
            Runnable r = new Runnable() {
                public void run() {
                    setDelayedAppearances(thrDelayedSet, thrDelay);
                }
            };
            Thread thr = new Thread(r);
            thr.setName(getDisplayName() + " delayed set appearance");
            try{
                thr.start();
            } catch (java.lang.IllegalThreadStateException ex){
                log.error(ex.toString());
            }
        }
        return;
    }
    
    private void setDelayedAppearances(final HashMap<SignalHead, Integer> delaySet, final int delay){
        for(SignalHead head: delaySet.keySet()){
            final SignalHead thrHead = head;
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        thrHead.setAppearance(delaySet.get(thrHead));
                        if (log.isDebugEnabled()) log.debug("Setting "+thrHead.getSystemName()+" to "+
                                thrHead.getAppearanceName(delaySet.get(thrHead)));
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            
            Thread thr = new Thread(r);
            thr.setName(getDisplayName());
            try{
                thr.start();
                thr.join();
            } catch (java.lang.IllegalThreadStateException ex){
                log.error(ex.toString());
            } catch (InterruptedException ex) {
                log.error(ex.toString());
            }
        }
    }
    
    public static List<SignalHead> getSignalHeadsUsed(){
        List<SignalHead> headsUsed = new ArrayList<SignalHead>();
        for(String val : InstanceManager.signalMastManagerInstance().getSystemNameList()){
            SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(val);
            if(mast instanceof jmri.implementation.SignalHeadSignalMast){
                java.util.List<NamedBeanHandle<SignalHead>> masthead = ((jmri.implementation.SignalHeadSignalMast)mast).getHeadsUsed();
                for(NamedBeanHandle<SignalHead> bean : masthead){
                    headsUsed.add(bean.getBean());
                }
            }
        }
        return headsUsed;
    }
    
    public static String isHeadUsed(SignalHead head){
        for(String val : InstanceManager.signalMastManagerInstance().getSystemNameList()){
            SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(val);
            if(mast instanceof jmri.implementation.SignalHeadSignalMast){
                java.util.List<NamedBeanHandle<SignalHead>> masthead = ((jmri.implementation.SignalHeadSignalMast)mast).getHeadsUsed();
                for(NamedBeanHandle<SignalHead> bean : masthead){
                    if((bean.getBean())==head)
                        return ((jmri.implementation.SignalHeadSignalMast)mast).getDisplayName();
                }
            }
        }
        return null;
    }
    
    static final protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadSignalMast.class.getName());
}

/* @(#)SignalHeadSignalMast.java */

// TurnoutSignalMast.javaa
package jmri.implementation;
import java.util.*;

import jmri.*;
import jmri.NamedBeanHandle;

 /**
 * SignalMast implemented via Turnout objects.
 * <p>
 * A Signalmast that is built up using turnouts to control
 * a specific appearance.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 19027 $
 */
public class TurnoutSignalMast extends AbstractSignalMast {

    public TurnoutSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public TurnoutSignalMast(String systemName) {
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
        if (!parts[0].equals("IF$tsm")) {
            log.warn("SignalMast system name should start with IF: "+systemName);
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        String tmp = parts[2].substring(parts[2].indexOf("($")+2, parts[2].indexOf(")"));
        try {
            int autoNumber = Integer.parseInt(tmp);
            if (autoNumber > lastRef) {
                lastRef = autoNumber;
            } 
        } catch (NumberFormatException e){
            log.warn("Auto generated SystemName "+ systemName + " is not in the correct format");
        }
        
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
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

    @Override
    public void setAspect(String aspect) { 
        // check it's a choice
        if ( !map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
        }
        
        if(resetPreviousStates){
            //Clear all the current states, this will result in the signalmast going blank for a very short time.
            for(String appearances: turnouts.keySet()){
                int setState = Turnout.CLOSED;
                if(turnouts.get(appearances).getTurnoutState()==Turnout.CLOSED)
                    setState = Turnout.THROWN;
                if(turnouts.get(appearances).getTurnout().getKnownState()!=setState){
                    turnouts.get(appearances).getTurnout().setCommandedState(setState);
                }
            }
        }
        Turnout turnToSet = turnouts.get(aspect).getTurnout();
        int stateToSet = turnouts.get(aspect).getTurnoutState();
        //Set the new signal mast state
        turnToSet.setCommandedState(stateToSet);
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
    public void setLit(boolean state) {
        // set all Heads to state
        for(String appearances: turnouts.keySet()){
            int setState = Turnout.CLOSED;
            if(turnouts.get(appearances).getTurnoutState()==Turnout.CLOSED)
                setState = Turnout.THROWN;
            if(turnouts.get(appearances).getTurnout().getKnownState()!=setState){
                turnouts.get(appearances).getTurnout().setCommandedState(setState);
            }
        }
        super.setLit(state);
    }
    
    public String getTurnoutName(String appearance){
        TurnoutAspect aspect = turnouts.get(appearance);
        if(aspect!=null)
            return aspect.getTurnoutName();
        return "";
    }
    
    public int getTurnoutState(String appearance){
        TurnoutAspect aspect = turnouts.get(appearance);
        if(aspect!=null)
            return aspect.getTurnoutState();
        return -1;
    }
    
    public void setTurnout(String appearance, String turn, int state){
        if(turnouts.containsKey(appearance)){
            log.debug("Appearance " + appearance + " is already defined so will override");
            turnouts.remove(appearance);
        }
        turnouts.put(appearance, new TurnoutAspect(turn, state));
    }
    
    DefaultSignalAppearanceMap map;
    SignalSystem systemDefn;
    
    HashMap<String, TurnoutAspect> turnouts = new HashMap<String, TurnoutAspect>();
    
    boolean resetPreviousStates = false;
    
    /**
    * If the signal mast driver requires the previous state to be cleared down before the next
    * state is set.
    */
    public void resetPreviousStates(boolean boo) { resetPreviousStates = boo; }
    
    public boolean resetPreviousStates() { return resetPreviousStates; }
    
    static class TurnoutAspect{
        
        NamedBeanHandle<Turnout> namedTurnout;
        int state;
        
        TurnoutAspect(String turnoutName, int turnoutState){
            Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turn);
            state = turnoutState;
        }
        
        Turnout getTurnout(){
            return namedTurnout.getBean();
        }
        
        String getTurnoutName() {
            return namedTurnout.getName();
        }
        
        int getTurnoutState(){
            return state;
        }
    
    }
    
    public List<NamedBeanHandle<Turnout>> getHeadsUsed(){
        return new ArrayList<NamedBeanHandle<Turnout>>();
    }
    
    public static int getLastRef(){ return lastRef; }
    
    static int lastRef = 0;
    
    static final protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutSignalMast.class.getName());
}

/* @(#)TurnoutSignalMast.java */

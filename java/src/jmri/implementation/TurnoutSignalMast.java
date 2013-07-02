// TurnoutSignalMast.javaa
package jmri.implementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Override
    public void setAspect(String aspect) { 
        // check it's a choice
        if ( !map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
        }  else if (disabledAspects.contains(aspect)){
            log.warn("attempting to set an aspect that has been disabled: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: "+aspect+" on mast: "+getDisplayName());
        }
        if(getLit()){ //If the signalmast is lit, then send the commands to change the aspect.
            if(resetPreviousStates){
                //Clear all the current states, this will result in the signalmast going blank for a very short time.
                for(String appearances: turnouts.keySet()){
                    if(!isAspectDisabled(appearances)){
                        int setState = Turnout.CLOSED;
                        if(turnouts.get(appearances).getTurnoutState()==Turnout.CLOSED)
                            setState = Turnout.THROWN;
                        if(turnouts.get(appearances).getTurnout().getKnownState()!=setState){
                            turnouts.get(appearances).getTurnout().setCommandedState(setState);
                        }
                    }
                }
            }
            Turnout turnToSet = turnouts.get(aspect).getTurnout();
            int stateToSet = turnouts.get(aspect).getTurnoutState();
            //Set the new signal mast state
            if(turnToSet!=null){
                turnToSet.setCommandedState(stateToSet);
            } else {
                log.error("Trying to set a state " + aspect + " on signal mast " + getDisplayName() + " which has not been configured");
            }
        } else if(log.isDebugEnabled()) log.debug("Mast set to unlit, will not send aspect change to hardware");
        super.setAspect(aspect);
    }
    
    TurnoutAspect unLit = null;;
    
    public void setUnLitTurnout(String turnoutName, int turnoutState){
        unLit = new TurnoutAspect(turnoutName, turnoutState);
    }
    
    public String getUnLitTurnoutName(){
        if(unLit!=null)
            return unLit.getTurnoutName();
        return null;
    }
    
    public Turnout getUnLitTurnout(){
        if(unLit!=null)
            return unLit.getTurnout();
        return null;
    }
    
    public int getUnLitTurnoutState(){
        if(unLit!=null)
            return unLit.getTurnoutState();
        return -1;
    }
    
    @Override
    public void setLit(boolean newLit) {
        if(!allowUnLit() || newLit == getLit()){
            return;
        }
        if(newLit){
            //This will force the signalmast to send out the commands to set the aspect again.
            setAspect(getAspect());
        } else {
            if(unLit!=null){
                Turnout t = unLit.getTurnout();
                if(t!=null && t.getKnownState()!=getUnLitTurnoutState()){
                    t.setCommandedState(getUnLitTurnoutState());
                }
            // set all Heads to state
            } else {
                for(String appearances: turnouts.keySet()){
                    int setState = Turnout.CLOSED;
                    if(turnouts.get(appearances).getTurnoutState()==Turnout.CLOSED)
                        setState = Turnout.THROWN;
                    if(turnouts.get(appearances).getTurnout().getKnownState()!=setState){
                        turnouts.get(appearances).getTurnout().setCommandedState(setState);
                    }
                }
            }
        }
        super.setLit(newLit);
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
    
    HashMap<String, TurnoutAspect> turnouts = new HashMap<String, TurnoutAspect>();
    
    boolean resetPreviousStates = false;
    
    /**
    * If the signal mast driver requires the previous state to be cleared down before the next
    * state is set.
    */
    public void resetPreviousStates(boolean boo) { resetPreviousStates = boo; }
    
    public boolean resetPreviousStates() { return resetPreviousStates; }
    
    static class TurnoutAspect implements java.io.Serializable{
        
        NamedBeanHandle<Turnout> namedTurnout;
        int state;
        
        TurnoutAspect(String turnoutName, int turnoutState){
            if(turnoutName!=null && !turnoutName.equals("")){
                Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
                namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turn);
                state = turnoutState;
            }
        }
        
        Turnout getTurnout(){
            if(namedTurnout==null)
                return null;
            return namedTurnout.getBean();
        }
        
        String getTurnoutName() {
            if(namedTurnout==null)
                return null;
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
    
    static final protected Logger log = LoggerFactory.getLogger(TurnoutSignalMast.class.getName());
}

/* @(#)TurnoutSignalMast.java */

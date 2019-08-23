package jmri.implementation;

import java.util.*;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignalMast implemented via Turnout objects.
 * <p>
 * A SignalMast that is built up using turnouts to control a specific
 * appearance. System name specifies the creation information:
 * <pre>
 * IF$tsm:basic:one-searchlight(IT1)(IT2)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$tsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(IT1)(IT2) - colon-separated list of names for Turnouts
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2014
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

    private static final String mastType = "IF$tsm";

    private void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: {}", systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals(mastType)) {
            log.warn("SignalMast system name should start with {} but is {}", mastType, systemName);
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        setMastType(mast);

        String tmp = parts[2].substring(parts[2].indexOf("($") + 2, parts[2].indexOf(")"));
        try {
            int autoNumber = Integer.parseInt(tmp);
            if (autoNumber > getLastRef()) {
                setLastRef(autoNumber);
            }
        } catch (NumberFormatException e) {
            log.warn("Auto generated SystemName {} is not in the correct format", systemName);
        }

        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    @Override
    public void setAspect(@Nonnull String aspect) {
        // check it's a choice
        if (!map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
        } else if (disabledAspects.contains(aspect)) {
            log.warn("attempting to set an aspect that has been disabled: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
        }
        
        
        if (getLit()) { // If the signalmast is lit, then send the commands to change the aspect.
            
            // reset all states before setting this one, including this one
            if (resetPreviousStates) {
                // Clear all the current states, this will result in the signalmast going blank for a very short time.
                for (Map.Entry<String, TurnoutAspect> entry : turnouts.entrySet()) {
                    String appearance = entry.getKey();
                    TurnoutAspect aspt = entry.getValue();
                    if (!isAspectDisabled(appearance)) {
                        int setState = Turnout.CLOSED;
                        if (aspt.getTurnoutState() == Turnout.CLOSED) {
                            setState = Turnout.THROWN;
                        }
                        if (aspt.getTurnout() != null ) {
                            if (aspt.getTurnout().getKnownState() != setState) {
                                aspt.getTurnout().setCommandedState(setState);
                            }
                        } else {
                            log.error("Trying to reset \"{}\" on signal mast \"{}\" which has not been configured", appearance, getDisplayName());
                        }
                    }
                }
            }

            // set the finel state if possible
            if (turnouts.get(aspect) != null && turnouts.get(aspect).getTurnout() != null) {
                Turnout turnToSet = turnouts.get(aspect).getTurnout();
                int stateToSet = turnouts.get(aspect).getTurnoutState();
                turnToSet.setCommandedState(stateToSet);
            } else {
                log.error("Trying to set \"{}\" on signal mast \"{}\" which has not been configured", aspect, getDisplayName());
            }
            
        } else if (log.isDebugEnabled()) {
            log.debug("Mast set to unlit, will not send aspect change to hardware");
        }
        super.setAspect(aspect);
    }

    private TurnoutAspect unLit = null;

    public void setUnLitTurnout(String turnoutName, int turnoutState) {
        unLit = new TurnoutAspect(turnoutName, turnoutState);
    }

    public String getUnLitTurnoutName() {
        if (unLit != null) {
            return unLit.getTurnoutName();
        }
        return null;
    }

    public Turnout getUnLitTurnout() {
        if (unLit != null) {
            return unLit.getTurnout();
        }
        return null;
    }

    public int getUnLitTurnoutState() {
        if (unLit != null) {
            return unLit.getTurnoutState();
        }
        return -1;
    }

    @Override
    public void setLit(boolean newLit) {
        if (!allowUnLit() || newLit == getLit()) {
            return;
        }
        super.setLit(newLit);
        if (newLit) {
            // This will force the signalmast to send out the commands to set the aspect again.
            setAspect(getAspect());
        } else {
            if (unLit != null) {
                // there is a specific unlit output defined
                Turnout t = unLit.getTurnout();
                if (t != null && t.getKnownState() != getUnLitTurnoutState()) {
                    t.setCommandedState(getUnLitTurnoutState());
                }
            } else {
                // turn everything off
                for (TurnoutAspect aspect : turnouts.values()) {
                    int setState = Turnout.CLOSED;
                    if (aspect.getTurnoutState() == Turnout.CLOSED) {
                        setState = Turnout.THROWN;
                    }
                    if (aspect.getTurnout().getKnownState() != setState) {
                        aspect.getTurnout().setCommandedState(setState);
                    }
                }
            }
        }
    }

    public String getTurnoutName(String appearance) {
        TurnoutAspect aspect = turnouts.get(appearance);
        if (aspect != null) {
            return aspect.getTurnoutName();
        }
        return "";
    }

    public int getTurnoutState(String appearance) {
        TurnoutAspect aspect = turnouts.get(appearance);
        if (aspect != null) {
            return aspect.getTurnoutState();
        }
        return -1;
    }

    public void setTurnout(String appearance, String turn, int state) {
        if (turnouts.containsKey(appearance)) {
            log.debug("Appearance {} is already defined so will override", appearance);
            turnouts.remove(appearance);
        }
        turnouts.put(appearance, new TurnoutAspect(turn, state));
    }

    HashMap<String, TurnoutAspect> turnouts = new HashMap<>();

    private boolean resetPreviousStates = false;

    /**
     * If the signal mast driver requires the previous state to be cleared down
     * before the next state is set.
     *
     * @param boo true if prior states should be cleared; false otherwise
     */
    public void resetPreviousStates(boolean boo) {
        resetPreviousStates = boo;
    }

    public boolean resetPreviousStates() {
        return resetPreviousStates;
    }

    static class TurnoutAspect {

        NamedBeanHandle<Turnout> namedTurnout;
        int state;

        TurnoutAspect(String turnoutName, int turnoutState) {
            if (turnoutName != null && !turnoutName.equals("")) {
                state = turnoutState;
                Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
                if (turn == null) {  
                    log.error("TurnoutAspect couldn't locate turnout {}", turnoutName);
                    return;
                }
                namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turn);
            }
        }

        Turnout getTurnout() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getBean();
        }

        String getTurnoutName() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getName();
        }

        int getTurnoutState() {
            return state;
        }
    }

    boolean isTurnoutUsed(Turnout t) {
        for (TurnoutAspect ta : turnouts.values()) {
            if (t.equals(ta.getTurnout())) {
                return true;
            }
        }
        if (t.equals(getUnLitTurnout())) {
            return true;
        }
        return false;
    }

    public List<NamedBeanHandle<Turnout>> getHeadsUsed() {
        return new ArrayList<NamedBeanHandle<Turnout>>();
    }

    /**
     *
     * @param newVal for ordinal of all TurnoutSignalMasts in use
     */
    protected static void setLastRef(int newVal) {
        lastRef = newVal;
    }

    /**
     * @return highest ordinal of all TurnoutSignalMasts in use
     */
    public static int getLastRef() {
        return lastRef;
    }

    /**
     * Ordinal of all TurnoutSignalMasts to create unique system name.
     */
    protected static volatile int lastRef = 0;
    // TODO narrow access to static, once jmri/jmrit/beantable/signalmast/TurnoutSignalMastAddPane uses setLastRef(n)
    //private static volatile int lastRef = 0;

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            if (evt.getOldValue() instanceof Turnout) {
                if (isTurnoutUsed((Turnout) evt.getOldValue())) {
                    java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseTurnoutSignalMastVeto", getDisplayName()), e); //IN18N
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutSignalMast.class);

}

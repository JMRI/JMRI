package jmri.jmrix.openlcb;

import java.util.HashMap;
import javax.annotation.Nonnull;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.SignalMast;
import jmri.implementation.AbstractSignalMast;
import jmri.jmrix.SystemConnectionMemo;

import org.openlcb.EventID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a SignalMast that use <B>OpenLCB Events</B>
 * to set aspects
 * <P>
 * This implementation writes out to the OpenLCB when it's commanded to
 * change appearance, and updates its internal state when it hears Events from
 * the network (including its own events).
 * <p>
 * System name specifies the creation information:
 * <pre>
 * IF$dsm:basic:one-searchlight(123)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>I - system prefix
 * <li>F$olm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(123) - number distinguishing this from others
 * </ul>
 * <p>
 * EventIDs are returned in their CanonicalString form; using OlcbAddress.toString() instead
 * would return the format in which they were provided.
 * <p>
 * To keep OpenLCB distributed state consistent, setAspect does not immediately
 * change the local aspect.  Instead, it produced the relevant EventId on the 
 * network, waiting for that to return and do the local state change, notification, etc.
 * <p>
 * Needs to have held/unheld, lit/unlit state completed - those need to Produce and Consume events as above
 * Based upon {@link jmri.implementation.DccSignalMast} by Kevin Dickerson
 *
 * @author Bob Jacobsen    Copyright (c) 2017, 2018
 */
public class OlcbSignalMast extends AbstractSignalMast {

    public OlcbSignalMast(String sys, String user) {
        super(sys, user);
        configureFromName(sys);
    }

    public OlcbSignalMast(String sys) {
        super(sys);
        configureFromName(sys);
    }

    public OlcbSignalMast(String sys, String user, String mastSubType) {
        super(sys, user);
        mastType = mastSubType;
        configureFromName(sys);
    }

    protected String mastType = "F$olm";
    boolean consistent = false; // vs inconsistent/initializing
    
    // not sure why this is a CanSystemConnectionMemo in simulator, but it is 
    jmri.jmrix.can.CanSystemConnectionMemo systemMemo;

    protected void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: " + systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].endsWith(mastType)) {
            log.warn("First part of SignalMast system name is incorrect " + systemName + " : " + mastType);
        } else {
            String systemPrefix = parts[0].substring(0, parts[0].indexOf("$") - 1);
            java.util.List<SystemConnectionMemo> memoList = jmri.InstanceManager.getList(SystemConnectionMemo.class);

            for (SystemConnectionMemo memo : memoList) {
                if (memo.getSystemPrefix().equals(systemPrefix)) {
                    if (memo instanceof OlcbSystemConnectionMemo) {
                        systemMemo = (OlcbSystemConnectionMemo) memo;
                    } else {
                        log.error("Can't create mast \""+systemName+"\" because system \"" + systemPrefix + "\" is not OlcbSystemConnectionMemo but rather "+memo.getClass());
                    }
                    break;
                }
            }

            if (systemMemo == null) {
                log.error("No OpenLCB connection found for system prefix \"" + systemPrefix + "\", so mast \""+systemName+"\" will not function");
            }
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        String tmp = parts[2].substring(parts[2].indexOf("(") + 1, parts[2].indexOf(")"));
        try {
            mastNumber = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            log.warn("Mast number of SystemName " + systemName + " is not in the correct format");
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    int mastNumber; // used to tell them apart
    
    protected HashMap<String, OlcbAddress> appearanceToOutput = new HashMap<>();

    public void setOutputForAppearance(String appearance, String event) {
        if (appearanceToOutput.containsKey(appearance)) {
            log.debug("Appearance " + appearance + " is already defined as " + appearanceToOutput.get(appearance).toCanonicalString());
            appearanceToOutput.remove(appearance);
        }
        appearanceToOutput.put(appearance, new OlcbAddress(event));
    }

    public String getOutputForAppearance(String appearance) {
        if (!appearanceToOutput.containsKey(appearance)) {
            log.error("Trying to get appearance " + appearance + " but it has not been configured");
            return "";
        }
        return appearanceToOutput.get(appearance).toCanonicalString();
    }

    @Override
    public void setAspect(String aspect) {

        if (appearanceToOutput.containsKey(aspect) && appearanceToOutput.get(aspect) != null) {
            System.out.println("Produce output event "+appearanceToOutput.get(aspect));
        } else {
            log.warn("Trying to set aspect (" + aspect + ") that has not been configured on mast " + getDisplayName());
        }
        // Normally, the local state is changed by super.setAspect(aspect); here; see comment at top
    }

    /**
     * This needs a better implementation, because 
     * now it's going to do a linear search through
     * the aspects in every OlcbSignalMast in the program.
     * Maybe run through a manager that does a single search to
     * find the relevant OlcbSignalMast(s)?
     */
    public boolean consumeEvent(@Nonnull EventID event) {
        if (appearanceToOutput.containsValue(event)) {
            for (String aspect : appearanceToOutput.keySet()) {
                if (appearanceToOutput.get(aspect).equals(event)) {
                    updateState(aspect); // only the first is done; there shouldn't be more than one, and order isn't preserved
                    return true;
                }
            }
        }
        if (event.equals(litEventId)) {
            super.setLit(true);
        }
        if (event.equals(notLitEventId)) {
            super.setLit(false);
        }
        if (event.equals(heldEventId)) {
            super.setHeld(true);
        }
        if (event.equals(notHeldEventId)) {
            super.setHeld(false);
        }
        return false;
    }

    /** 
     * When the aspect change has returned from the OpenLCB network,
     * change the local state and notify
     */
    void updateState(String aspect) {
        String oldAspect = this.aspect;
        this.aspect = aspect;
        this.speed = (String) getSignalSystem().getProperty(aspect, "speed");
        firePropertyChange("Aspect", oldAspect, aspect);
    }

    /** 
     * Always communicates via OpenLCB
     */
    @Override
    public void setLit(boolean newLit) {
        if (newLit) {
            System.out.println("Produce output event "+getLitEventId());
        } else {
            System.out.println("Produce output event "+getNotLitEventId());
        }
        // does not call super.setLit because no local state change until Event consumed
    }

    /** 
     * Always communicates via OpenLCB
     */
    @Override
    public void setHeld(boolean newHeld) {
        if (newHeld) {
            System.out.println("Produce output event "+getHeldEventId());
        } else {
            System.out.println("Produce output event "+getNotHeldEventId());
        }
        // does not call super.setHeld because no local state change until Event consumed
    }

    EventID litEventId = null;
    public void setLitEventId(String event) { litEventId = new OlcbAddress(event).toEventID(); }
    public String getLitEventId() { return new OlcbAddress(litEventId).toCanonicalString(); }
    EventID notLitEventId = null;
    public void setNotLitEventId(String event) { notLitEventId = new OlcbAddress(event).toEventID(); }
    public String getNotLitEventId() { return new OlcbAddress(notLitEventId).toCanonicalString(); }

    EventID heldEventId = null;
    public void setHeldEventId(String event) { heldEventId = new OlcbAddress(event).toEventID(); }
    public String getHeldEventId() { return new OlcbAddress(heldEventId).toCanonicalString(); }
    EventID notHeldEventId = null;
    public void setNotHeldEventId(String event) { notHeldEventId = new OlcbAddress(event).toEventID(); }
    public String getNotHeldEventId() { return new OlcbAddress(notHeldEventId).toCanonicalString(); }


    private final static Logger log = LoggerFactory.getLogger(OlcbSignalMast.class);

}



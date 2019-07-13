package jmri.jmrix.loconet;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.IdTag;
import jmri.LocoAddress;
import jmri.Reporter;
import jmri.CollectingReporter;
import jmri.PhysicalLocationReporter;
import jmri.implementation.AbstractIdTagReporter;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractIdTagReporter for LocoNet layouts.
 * <p>
 * This implementation reports Transponding messages.
 * <p>
 * Each transponding message creates a new current report. The last report is
 * always available, and is the same as the contents of the last transponding
 * message received.
 * <p>
 * Reports are Strings, formatted as
 * <ul>
 *   <li>NNNN enter - locomotive address NNNN entered the transponding zone. Short
 *                    vs long address is indicated by the NNNN value
 *   <li>NNNN exits - locomotive address NNNN left the transponding zone.
 *   <li>NNNN seen northbound - LISSY measurement
 *   <li>NNNN seen southbound - LISSY measurement
 * </ul>
 *
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 */
public class LnReporter extends AbstractIdTagReporter implements LocoNetListener, CollectingReporter {

    public LnReporter(int number, LnTrafficController tc, String prefix) {  // a human-readable Reporter number must be specified!
        super(prefix + "R" + number);  // can't use prefix here, as still in construction
        log.debug("new Reporter {}", number);
        _number = number;
        // At construction, register for messages
        tc.addLocoNetListener(~0, this);
        this.tc = tc;
        entrySet = new HashSet<TranspondingTag>();
    }

    LnTrafficController tc;

    /**
      * @return the LocoNet address number for this reporter.
      */
    public int getNumber() {
        return _number;
    }

    /**
      * {@inheritDoc}
      */
    @Override
    public void message(LocoNetMessage l) {
        // check message type
        if ((l.getOpCode() == 0xD0) && ((l.getElement(1) & 0xC0) == 0)) {
            transpondingReport(l);
        }
        if ((l.getOpCode() == 0xE4) && (l.getElement(1) == 0x08)) {
            lissyReport(l);
        } else {
            return; // nothing
        }
    }

    /**
     * Handle transponding message
     */
    void transpondingReport(LocoNetMessage l) {
        // check address
        int addr = ((l.getElement(1) & 0x1F) * 128) + l.getElement(2) + 1;
        if (addr != getNumber()) {
            return;
        }

        // get direction
        boolean enter = ((l.getElement(1) & 0x20) != 0);

        // get loco address
        int loco;
        if (l.getElement(3) == 0x7D) {
            loco = l.getElement(4);
        } else {
            loco = l.getElement(3) * 128 + l.getElement(4);
        }

        notify(null); // set report to null to make sure listeners update
        IdTag idTag = InstanceManager.getDefault(TranspondingTagManager.class).provideIdTag(""+loco);
        if(enter) {
           idTag.setProperty("entryexit","enter");
           if(!entrySet.contains(idTag)){
              entrySet.add((TranspondingTag)idTag);
           }
        } else {
           idTag.setProperty("entryexit","exits");
           if(entrySet.contains(idTag)){
              entrySet.remove(idTag);
           }
        }
        log.debug("Tag: " + idTag);
        notify(idTag);
        setState(enter ? loco : -1);
    }

    /**
     * Handle LISSY message
     */
    void lissyReport(LocoNetMessage l) {
        // check unit address
        int unit = (l.getElement(4) & 0x7F);
        if (unit != getNumber()) {
            return;
        }

        int loco = (l.getElement(6) & 0x7F) + 128 * (l.getElement(5) & 0x7F);

        // get direction
        boolean north = ((l.getElement(3) & 0x20) == 0);

        notify(null); // set report to null to make sure listeners update
        // get loco address
        IdTag idTag = InstanceManager.getDefault(TranspondingTagManager.class).provideIdTag(""+loco);
        if(north) {
           idTag.setProperty("seen","seen northbound");
        } else {
           idTag.setProperty("seen","seen southbound");
        }
        log.debug("Tag: " + idTag);
        notify(idTag);
        setState(loco);
    }

    /**
      * {@inheritDoc}
      */
    @Override
    public void notify(IdTag id) {
        log.debug("Notify: " + this.mSystemName);
        if (id != null) {
            log.debug("Tag: " + id);
            AbstractIdTagReporter r;
            if ((r = (AbstractIdTagReporter) id.getWhereLastSeen()) != null) {
                log.debug("Previous reporter: " + r.getSystemName());
                if (!(r.equals(this)) && r.getCurrentReport() == id) {
                    log.debug("Notify previous");
                    r.notify(null);
                } else {
                    log.debug("Current report was: " + r.getCurrentReport());
                }
            }
            id.setWhereLastSeen(this);
            log.debug("Seen here: " + this.mSystemName);
        }
        setReport(id);
        setState(id != null ? IdTag.SEEN : IdTag.UNSEEN);
    }


    /**
     * Provide an int value for use in scripts, etc. This will be the numeric
     * locomotive address last seen, unless the last message said the loco was
     * exiting. Note that there may still some other locomotive in the
     * transponding zone!
     *
     * @return -1 if the last message specified exiting
     */
    @Override
    public int getState() {
        return lastLoco;
    }

    /**
      * {@inheritDoc}
      */
    @Override
    public void setState(int s) {
        lastLoco = s;
    }
    int lastLoco = -1;

    /**
      * {@inheritDoc}
      */
    @Override
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        super.dispose();
    }

    /**
     * Parses out a (possibly old) LnReporter-generated report string to extract info used by
     * the public PhysicalLocationReporter methods.  Returns a Matcher that, if successful, should
     * have the following groups defined.
     * matcher.group(1) : the locomotive address
     * matcher.group(2) : (enter | exit | seen)
     * matcher.group(3) | (northbound | southbound) -- Lissy messages only
     * <p>
     * NOTE: This code is dependent on the transpondingReport() and lissyReport() methods.  
     * If they change, the regex here must change.
     */
    private Matcher parseReport(String rep) {
        if (rep == null) {
            return (null);
        }
        Pattern ln_p = Pattern.compile("(\\d+) (enter|exits|seen)\\s*(northbound|southbound)?");  // Match a number followed by the word "enter".  This is the LocoNet pattern. // NOI18N
        Matcher m = ln_p.matcher(rep);
        return (m);
    }

    /**
      * {@inheritDoc}
      */
    // Parses out a (possibly old) LnReporter-generated report string to extract the address from the front.
    // Assumes the LocoReporter format is "NNNN [enter|exit]"
    @Override
    public LocoAddress getLocoAddress(String rep) {
        // Extract the number from the head of the report string
        log.debug("report string: {}", rep);
        Matcher m = this.parseReport(rep);
        if ((m != null) && m.find()) {
            log.debug("Parsed address: {}", m.group(1));
            return (new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
        } else {
            return (null);
        }
    }

    /**
      * {@inheritDoc}
      */
    // Parses out a (possibly old) LnReporter-generated report string to extract the direction from the end.
    // Assumes the LocoReporter format is "NNNN [enter|exit]"
    @Override
    public PhysicalLocationReporter.Direction getDirection(String rep) {
        // Extract the direction from the tail of the report string
        log.debug("report string: {}", rep); // NOI18N
        Matcher m = this.parseReport(rep);
        if (m.find()) {
            log.debug("Parsed direction: {}", m.group(2)); // NOI18N
            if (m.group(2).equals("enter")) { // NOI18N
                // LocoNet Enter message
                return (PhysicalLocationReporter.Direction.ENTER);
            } else if (m.group(2).equals("seen")) { // NOI18N
                // Lissy message.  Treat them all as "entry" messages.
                return (PhysicalLocationReporter.Direction.ENTER);
            } else {
                return (PhysicalLocationReporter.Direction.EXIT);
            }
        } else {
            return (PhysicalLocationReporter.Direction.UNKNOWN);
        }
    }

    /**
      * {@inheritDoc}
      */
    @Override
    public PhysicalLocation getPhysicalLocation() {
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }

    /**
      * {@inheritDoc}
      */
    // Does not use the parameter S.
    @Override
    public PhysicalLocation getPhysicalLocation(String s) {
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }


    // Collecting Reporter Interface methods
    /**
      * {@inheritDoc}
      */
     @Override
     public java.util.Collection getCollection(){
        return entrySet;
     }

    // data members
    private int _number;   // LocoNet Reporter number
    private HashSet<TranspondingTag> entrySet=null;

    private final static Logger log = LoggerFactory.getLogger(LnReporter.class);

}

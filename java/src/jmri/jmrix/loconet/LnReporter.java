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
 * This implementation reports Transponding messages from LocoNet-based "Reporters".
 * 
 * For LocoNet connections, a "Reporter" represents either a Digitrax "transponding zone" or a 
 * Lissy "measurement zone".  The messages from these Reporters are handled by this code.
 * 
 * The LnReporterManager is responsible for decode of appropriate LocoNet messages
 * and passing only those messages to the Reporter which match its Reporter address.
 * 
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
public class LnReporter extends AbstractIdTagReporter implements CollectingReporter {

    public LnReporter(int number, LnTrafficController tc, String prefix) {  // a human-readable Reporter number must be specified!
        super(prefix + "R" + number);  // can't use prefix here, as still in construction
        log.debug("new Reporter {}", number);
        _number = number;
        // At construction, register for messages
        entrySet = new HashSet<TranspondingTag>();
    }


    /**
      * @return the LocoNet address number for this reporter.
      */
    public int getNumber() {
        return _number;
    }

    /**
      * Process loconet message handed to us from the LnReporterManager
      * @param l - a loconetmessage.
      */
    public void messageFromManager(LocoNetMessage l) {
        // check message type
        if (isTranspondingLocationReport(l) || isTranspondingFindReport(l)) {
            transpondingReport(l);
        }
        if ((l.getOpCode() == LnConstants.OPC_LISSY_UPDATE) && (l.getElement(1) == 0x08)) {
            lissyReport(l);
        } else {
            return; // nothing
        }
    }

    /**
     * Check if message is a Transponding Location Report message
     * 
     * A Transponding Location Report message is sent by transponding hardware
     * when a transponding mobile decoder enters or leaves a transponding zone.
     * 
     * @param l LocoNet message to check
     * @return true if message is a Transponding Location Report, else false.
     */
    public final boolean isTranspondingLocationReport(LocoNetMessage l) {
        return ((l.getOpCode() == LnConstants.OPC_MULTI_SENSE)
            && ((l.getElement(1) & 0xC0) == 0)) ;
    }

    /**
     * Check if message is a Transponding Find Report message
     * 
     * A Transponding Location Report message is sent by transponding hardware
     * in response to a Transponding Find Request message when the addressed
     * decoder is within a transponding zone and the decoder is transponding-enabled.
     * 
     * @param l LocoNet message to check
     * @return true if message is a Transponding Find Report, else false.
     */
    public final boolean isTranspondingFindReport(LocoNetMessage l) {
        return (l.getOpCode() == LnConstants.OPC_PEER_XFER
            && l.getElement(1) == 0x09
            && l.getElement(2) == 0 );
    }

    /**
     * Handle transponding message passed to us by the LnReporting Manager
     *
     * Assumes that the LocoNet message is a valid transponding message.
     * 
     * @param l - incoming loconetmessage
     */
    void transpondingReport(LocoNetMessage l) {
        boolean enter;
        int loco;
        IdTag idTag;
        if (l.getOpCode() == LnConstants.OPC_MULTI_SENSE) {
            enter = ((l.getElement(1) & 0x20) != 0); // get reported direction
        } else {
            enter = true; // a response for a find request. Always handled as entry.
        }
        loco = getLocoAddrFromTranspondingMsg(l); // get loco address

        notify(null); // set report to null to make sure listeners update

        idTag = InstanceManager.getDefault(TranspondingTagManager.class).provideIdTag("" + loco);
        idTag.setProperty("entryexit", "enter");
        if (enter) {
            idTag.setProperty("entryexit", "enter");
            if (!entrySet.contains(idTag)) {
                entrySet.add((TranspondingTag) idTag);
            }
        } else {
            idTag.setProperty("entryexit", "exits");
            if (entrySet.contains(idTag)) {
                entrySet.remove(idTag);
            }
        }
        log.debug("Tag: " + idTag);
        notify(idTag);
        setState(enter ? loco : -1);
    }

    /**
     * extract long or short address from transponding message
     * 
     * Assumes that the LocoNet message is a valid transponding message.
     * 
     * @param l LocoNet message
     * @return loco address
     */
    public int getLocoAddrFromTranspondingMsg(LocoNetMessage l) {
        if (l.getElement(3) == 0x7D) {
            return l.getElement(4);
        }
        return l.getElement(3) * 128 + l.getElement(4);
        
    }

    /**
     * Handle LISSY message
     */
    void lissyReport(LocoNetMessage l) {
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
            switch (m.group(2)) {
                case "enter":
                    // NOI18N
                    // LocoNet Enter message
                    return (PhysicalLocationReporter.Direction.ENTER);
                case "seen":
                    // NOI18N
                    // Lissy message.  Treat them all as "entry" messages.
                    return (PhysicalLocationReporter.Direction.ENTER);
                default:
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

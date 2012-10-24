// LnReporter.java

package jmri.jmrix.loconet;

import jmri.implementation.AbstractReporter;
import jmri.PhysicalLocationReporter;
import jmri.util.PhysicalLocation;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Extend jmri.AbstractReporter for LocoNet layouts
 * <P>
 * This implementation reports transponding messages.
 *<P>
 * Each transponding message creates a new current report.  The last
 * report is always available, and is the same as the contents of
 * the last transponding message received.
 *<P>
 * Reports are Strings, formatted as
 *  <ul>
 *  <li>NNNN enter - locomotive address NNNN entered the 
 *       transponding zone.  Short vs long address is indicated
 *       by the NNNN value
 *  <LI>NNNN exits - locomotive address NNNN left the transponding zone.
 *  <LI>NNNN seen northbound - LISSY measurement
 *  <LI>NNNN seen southbound - LISSY measurement
 * </ul>
 *<p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2001, 2007
 * @version			$Revision$
 */
 
public class LnReporter extends AbstractReporter implements LocoNetListener, PhysicalLocationReporter {

    public LnReporter(int number, LnTrafficController tc, String prefix) {  // a human-readable Reporter number must be specified!
        super(prefix+"R"+number);  // can't use prefix here, as still in construction
        log.debug("new Reporter "+number);
         _number = number;
         // At construction, register for messages
         tc.addLocoNetListener(~0, this);
         this.tc = tc;
     }

    LnTrafficController tc;
    
    public int getNumber() { return _number; }

     // implementing classes will typically have a function/listener to get
     // updates from the layout, which will then call
     //		public void firePropertyChange(String propertyName,
     //					      	Object oldValue,
     //						Object newValue)
     // _once_ if anything has changed state (or set the commanded state directly)
    public void message(LocoNetMessage l) {
         // check message type
		if ( (l.getOpCode() == 0xD0) && ( (l.getElement(1) & 0xC0) == 0) ) 
		    transpondingReport(l);
		if ( (l.getOpCode() == 0xE4) && ( l.getElement(1) == 0x08) ) 
		    lissyReport(l);
        else return; // nothing
    }

    /**
     * Handle transponding message
     */
    void transpondingReport(LocoNetMessage l) {
		// check address
        int addr = ((l.getElement(1)&0x1F)*128) + l.getElement(2) + 1;
		if (addr != getNumber()) return;
		
		// get direction
		boolean enter = ( (l.getElement(1) & 0x20) != 0) ;	
		
		// get loco address
		int loco;
		if (l.getElement(3) == 0x7D )
			loco = l.getElement(4);
		else 
			loco = l.getElement(3)*128 + l.getElement(4);
        
        lastLoco = (enter? loco : -1);
        setReport(""+loco+(enter?" enter":" exits"));
    }
    
    /**
     * Handle LISSY message
     */
    void lissyReport(LocoNetMessage l) {
		// check unit address
        int unit = (l.getElement(4)&0x7F);
        if (unit != getNumber()) return;
        
        // get loco address
        int loco = (l.getElement(6)&0x7F)+128*(l.getElement(5)&0x7F);
		
		// get direction
		boolean north = ( (l.getElement(3) & 0x20) == 0) ;	
		
		// get loco address
        setReport(""+loco+" seen "+(north?"northbound":"southbound"));

    }
    
	/**
	 * Provide an int value for use in scripts, etc.  This will be
	 * the numeric locomotive address last seen, unless the last 
	 * message said the loco was exiting. Note that there may still some
	 * other locomotive in the transponding zone!
	 * @return -1 if the last message specified exiting
	 */
	public int getState() {
	 	return lastLoco;
	}

	public void setState(int s) {
	 	lastLoco = s;
	}	 
	int lastLoco = -1;
	 
    public void dispose() {
         tc.removeLocoNetListener(~0, this);
         super.dispose();
    }

    // parseReport()
    // Parses out a (possibly old) LnReporter-generated report string to extract info used by
    // the public PhysicalLocationReporter methods.  Returns a Matcher that, if successful, should
    // have the following groups defined.
    // matcher.group(1) : the locomotive address
    // matcher.group(2) : (enter | exit | seen)
    // matcher.group(3) | (northbound | southbound) -- Lissy messages only
    //
    // NOTE: This code is dependent on the transpondingReport() and lissyReport() methods above.  If they change, the regex here must change.
    private Matcher parseReport(String rep) {
	Pattern ln_p = Pattern.compile("(\\d+) (enter|exits|seen)\\s*(northbound|southbound)?");  // Match a number followed by the word "enter".  This is the LocoNet pattern.
	Matcher m = ln_p.matcher(rep);
	return(m);
    }
     
    // Parses out a (possibly old) LnReporter-generated report string to extract the address from the front.
    // Assumes the LocoReporter format is "NNNN [enter|exit]"
    public LocoAddress getLocoAddress(String rep) {
	// Extract the number from the head of the report string
	log.debug("report string: " + rep);
	Matcher m = this.parseReport(rep);
	if (m.find()) {
	    log.debug("Parsed address: " + m.group(1));
	    return(new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
	} else {
	    return(null);
	}
    }

    // Parses out a (possibly old) LnReporter-generated report string to extract the direction from the end.
    // Assumes the LocoReporter format is "NNNN [enter|exit]"
    public PhysicalLocationReporter.Direction getDirection(String rep) {
	// Extract the direction from the tail of the report string
	log.debug("report string: " + rep);
	Matcher m = this.parseReport(rep);
	if (m.find()) {
	    log.debug("Parsed direction: " + m.group(2));
	    if (m.group(2).equals("enter")) {
		// LocoNet Enter message
		return(PhysicalLocationReporter.Direction.ENTER);
	    } else if (m.group(2).equals("seen")) {
		// Lissy message.  Treat them all as "entry" messages.
		return(PhysicalLocationReporter.Direction.ENTER);
	    } else {
		return(PhysicalLocationReporter.Direction.EXIT);
	    }
	} else {
	    return(PhysicalLocationReporter.Direction.UNKNOWN);
	}
    }

    public PhysicalLocation getPhysicalLocation() {
	return(PhysicalLocation.getBeanPhysicalLocation(this));
    }

    // data members
    int _number;   // loconet Reporter number

    @SuppressWarnings("unused")
	private boolean myAddress(int a1, int a2) {
         // the "+ 1" in the following converts to throttle-visible numbering
         return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnReporter.class.getName());

 }

/* @(#)LnReporter.java */

// LnReporter.java

package jmri.jmrix.loconet;

import jmri.AbstractReporter;

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
 * </ul>
 *<p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
 
 public class LnReporter extends AbstractReporter implements LocoNetListener {

    public LnReporter(int number) {  // a human-readable Reporter number must be specified!
        super("LR"+number);  // can't use prefix here, as still in construction
        log.debug("new Reporter "+number);
         _number = number;
         // At construction, register for messages
         if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.warn("No LocoNet connection, Reporter won't update");
     }

     public int getNumber() { return _number; }

     // implementing classes will typically have a function/listener to get
     // updates from the layout, which will then call
     //		public void firePropertyChange(String propertyName,
     //					      	Object oldValue,
     //						Object newValue)
     // _once_ if anything has changed state (or set the commanded state directly)
     public void message(LocoNetMessage l) {
         // check message type
		if (l.getOpCode() != 0xD0) return;
		if ( (l.getElement(1) & 0xC0) != 0) return;

		// message type OK, check address
        int addr = (l.getElement(1)*128&0x1F) + l.getElement(2) + 1;
		if (addr != getNumber()) return;
		
		// this is real, get direction
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
         LnTrafficController.instance().removeLocoNetListener(~0, this);
     }

     // data members
     int _number;   // loconet Reporter number

     private boolean myAddress(int a1, int a2) {
         // the "+ 1" in the following converts to throttle-visible numbering
         return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number;
     }
     static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnReporter.class.getName());

 }

/* @(#)LnReporter.java */

/**
 * 
 */
package jmri.configurexml.turnoutoperations;

import org.jdom.Element;

import java.lang.Integer;
import java.lang.reflect.Constructor;

import jmri.CommonTurnoutOperation;
import jmri.TurnoutOperation;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;

/**
 * Concrete subclass to save/restore NoFeedbackTurnoutOperation object
 * to/from XML.
 * @author John Harper	Copyright 2005
 *
 */
public abstract class CommonTurnoutOperationXml extends TurnoutOperationXml {

	public Element store(Object op) {
		CommonTurnoutOperation myOp = (CommonTurnoutOperation)op;
		Element elem = super.store(op);
		elem.addAttribute("interval", String.valueOf(myOp.getInterval()));
		elem.addAttribute("maxtries", String.valueOf(myOp.getMaxTries()));
		return elem;
	}
	
	/**
	 * called for a newly-constructed object to load it from an XML element
	 * @param e the XML element of type "turnoutOperation"
	 */
	public TurnoutOperation loadOne(Element e, Constructor constr, int di, int dmt) {
		int interval = di;
		int maxTries = dmt;
		boolean noDelete = false;
		TurnoutOperation result = null;
        if (e.getAttribute("name") == null) {
            log.warn("unexpected null in name "+e+" "+e.getAttributes());
            return null;
        }
        String name = e.getAttribute("name").getValue();
        if (e.getAttribute("interval") != null) {
        	try {
            	interval = Integer.parseInt(e.getAttribute("interval").getValue());        		
        	} catch(NumberFormatException ex) { };
        }
        if (e.getAttribute("maxtries") != null) {
        	try {
            	maxTries = Integer.parseInt(e.getAttribute("maxtries").getValue());        		
        	} catch(NumberFormatException ex) { };
        }
        // constructor takes care of enrolling the new operation
        try {
        	result =
        		(CommonTurnoutOperation)constr.newInstance(new Object[]{name, new Integer(interval), new Integer(maxTries)});
        } catch (Exception except) {			// too many to list!
        	log.warn("failed to create instance of "+constr.getDeclaringClass().getName()+" for \""+name+"\"");
        }
        if (log.isDebugEnabled()) log.debug("create turnout operation: ("+name+")");	
        return result;
	}
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CommonTurnoutOperationXml.class.getName());
}

package jmri.configurexml.turnoutoperations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import jmri.CommonTurnoutOperation;
import jmri.TurnoutOperation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete subclass to save/restore NoFeedbackTurnoutOperation object to/from
 * XML.
 *
 * @author John Harper Copyright 2005
 */
public abstract class CommonTurnoutOperationXml extends TurnoutOperationXml {

    @Override
    public Element store(Object op) {
        CommonTurnoutOperation myOp = (CommonTurnoutOperation) op;
        Element elem = super.store(op);
        elem.setAttribute("interval", String.valueOf(myOp.getInterval()));
        elem.setAttribute("maxtries", String.valueOf(myOp.getMaxTries()));
        return elem;
    }

    /**
     * called for a newly-constructed object to load it from an XML element
     *
     * @param e the XML element of type "turnoutOperation"
     * @param constr constructor of subclass of TurnoutOperation to create
     * @param di default interval
     * @param dmt default max tries
     * @return a TurnoutOperation or null if unable to load from e
     */
    public TurnoutOperation loadOne(Element e, Constructor<?> constr, int di, int dmt) {
        int interval = di;
        int maxTries = dmt;
//  boolean noDelete = false;
        TurnoutOperation result = null;
        if (e.getAttribute("name") == null) {
            log.warn("unexpected null in name " + e + " " + e.getAttributes());
            return null;
        }
        String name = e.getAttribute("name").getValue();
        if (e.getAttribute("interval") != null) {
            try {
                interval = Integer.parseInt(e.getAttribute("interval").getValue());
            } catch (NumberFormatException ex) {
            }
        }
        if (e.getAttribute("maxtries") != null) {
            try {
                maxTries = Integer.parseInt(e.getAttribute("maxtries").getValue());
            } catch (NumberFormatException ex) {
            }
        }
        // constructor takes care of enrolling the new operation
        try {
            result = (TurnoutOperation) constr.newInstance(new Object[]{name, interval, maxTries});
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e1) {
            log.error("while creating CommonTurnoutOperation", e1);
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("create turnout operation: (" + name + ")");
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(CommonTurnoutOperationXml.class);

}

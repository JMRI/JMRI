/**
 * 
 */
package jmri.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

import java.util.List;

import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;

/**
 * @author John Harper
 *
 */
public class TurnoutOperationManagerXml extends jmri.configurexml.AbstractXmlAdapter {

    public TurnoutOperationManagerXml() {
    }

    public void setStoreElementClass(Element elem) {
        elem.setAttribute("class",getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @SuppressWarnings("unchecked")
	public boolean load(Element operationsElement) {
    	boolean result = true;
    	TurnoutOperationManager manager = TurnoutOperationManager.getInstance();
        if (operationsElement.getAttribute("automate") != null) {
        	try {
            	manager.setDoOperations(operationsElement.getAttribute("automate").getValue().equals("true"));        		
        	} catch(NumberFormatException ex) {
        		result = false;
        	}
        }
    	List<Element> operationsList = operationsElement.getChildren("operation");
    	if (log.isDebugEnabled()) log.debug("Found "+operationsList.size()+" operations");
    	for (int i=0; i<operationsList.size(); i++) {
    		TurnoutOperationXml.loadOperation(operationsList.get(i));
    	}
    	return result;
    }

    public Element store(Object o) {
    	Element elem = new Element("operations");
    	if (o instanceof TurnoutOperationManager) {
    		TurnoutOperationManager manager = (TurnoutOperationManager)o;
    		elem.setAttribute("automate", String.valueOf(manager.getDoOperations()));
    		TurnoutOperation[] operations = manager.getTurnoutOperations();
    		for (int i=0; i<operations.length; ++i) {
    			TurnoutOperation op = operations[i];
    			if (!op.isNonce()) {		// nonces are stored with their respective turnouts
    				TurnoutOperationXml adapter = TurnoutOperationXml.getAdapter(op);
    				if (adapter != null) {
    					Element opElem = adapter.store(op);
    					if (opElem != null) {
    						elem.addContent(opElem);
    					}
    				}
    			}
    		}
    	}
    	return elem;
    }

    static Logger log = Logger.getLogger(TurnoutOperationManagerXml.class.getName());
}

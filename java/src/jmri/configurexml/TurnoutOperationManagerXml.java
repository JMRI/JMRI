/**
 *
 */
package jmri.configurexml;

import java.util.List;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Harper
 *
 */
public class TurnoutOperationManagerXml extends jmri.configurexml.AbstractXmlAdapter {

    public TurnoutOperationManagerXml() {
    }

    public void setStoreElementClass(Element elem) {
        elem.setAttribute("class", getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element sharedOperations, Element perNodeOperations) {
        boolean result = true;
        TurnoutOperationManager manager = TurnoutOperationManager.getInstance();
        if (sharedOperations.getAttribute("automate") != null) {
            try {
                manager.setDoOperations(sharedOperations.getAttribute("automate").getValue().equals("true"));
            } catch (NumberFormatException ex) {
                result = false;
            }
        }
        List<Element> operationsList = sharedOperations.getChildren("operation");
        if (log.isDebugEnabled()) {
            log.debug("Found " + operationsList.size() + " operations");
        }
        for (int i = 0; i < operationsList.size(); i++) {
            TurnoutOperationXml.loadOperation(operationsList.get(i));
        }
        return result;
    }

    public Element store(Object o) {
        Element elem = new Element("operations");
        if (o instanceof TurnoutOperationManager) {
            TurnoutOperationManager manager = (TurnoutOperationManager) o;
            elem.setAttribute("automate", String.valueOf(manager.getDoOperations()));
            TurnoutOperation[] operations = manager.getTurnoutOperations();
            for (int i = 0; i < operations.length; ++i) {
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

    private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerXml.class.getName());
}

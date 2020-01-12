package jmri.configurexml;

import java.util.List;
import jmri.*;
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

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element sharedOperations, Element perNodeOperations) {
        boolean result = true;
        TurnoutOperationManager manager = InstanceManager.getDefault(TurnoutOperationManager.class);
        if (sharedOperations.getAttribute("automate") != null) {
            try {
                manager.setDoOperations(sharedOperations.getAttribute("automate").getValue().equals("true"));
            } catch (NumberFormatException ex) {
                result = false;
            }
        }
        List<Element> operationsList = sharedOperations.getChildren("operation");
        log.debug("Found {} Operations", operationsList.size());
        for (Element oper : operationsList) {
            TurnoutOperationXml.loadOperation(oper);
        }
        return result;
    }

    @Override
    public Element store(Object o) {
        Element elem = new Element("operations");
        if (o instanceof TurnoutOperationManager) {
            TurnoutOperationManager manager = (TurnoutOperationManager) o;
            elem.setAttribute("automate", String.valueOf(manager.getDoOperations()));
            TurnoutOperation[] operations = manager.getTurnoutOperations();
            for (TurnoutOperation op : operations) {
                if (!op.isNonce()) {  // nonces are stored with their respective turnouts
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

    private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerXml.class);

}

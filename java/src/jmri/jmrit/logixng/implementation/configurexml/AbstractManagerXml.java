package jmri.jmrit.logixng.implementation.configurexml;

import java.util.List;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;

import org.jdom2.Element;


/**
 * Provides the functionality for configuring ActionManagers
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public abstract class AbstractManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Store data for a MaleSocket
     *
     * @param maleSocket the socket to store
     * @return Element containing the complete info
     */
    public Element storeMaleSocket(MaleSocket maleSocket) {
        Element element = new Element("maleSocket");
        element.addContent(new Element("errorHandling").addContent(maleSocket.getErrorHandlingType().name()));
        for (SymbolTable.VariableData data : maleSocket.getLocalVariables()) {
            Element elementVariable = new Element("localVariable");
            elementVariable.addContent(new Element("name").addContent(data._name));
            elementVariable.addContent(new Element("type").addContent(data._initalValueType.name()));
            elementVariable.addContent(new Element("data").addContent(data._initialValueData));
            element.addContent(elementVariable);
        }
/*
        setStoreElementClass(actions);
        DigitalActionManager tm = (DigitalActionManager) o;
//        System.out.format("DefaultDigitalActionManagerXml: manager: %s%n", tm);
        if (tm != null) {
            for (DigitalActionBean action : tm.getNamedBeanSet()) {
                log.debug("action system name is " + action.getSystemName());  // NOI18N
//                log.error("action system name is " + action.getSystemName() + ", " + action.getLongDescription());  // NOI18N
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(getAction(action));
                    if (e != null) {
                        actions.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing action: {}", e, e);
                }
            }
        }
*/
        return (element);
    }

    /**
     * Utility method to load the individual DigitalActionBean objects. If
     * there's no additional info needed for a specific action type, invoke
     * this with the parent of the set of DigitalActionBean elements.
     *
     * @param element Element containing the MaleSocket element to load.
     * @param maleSocket the socket to load
     */
    public void loadMaleSocket(Element element, MaleSocket maleSocket) {
        
        Element elementMaleSocket = element.getChild("maleSocket");
        if (elementMaleSocket == null) {
            throw new IllegalArgumentException("maleSocket is null");
        }
        
        Element errorHandlingElement = elementMaleSocket.getChild("errorHandling");
        if (errorHandlingElement != null) {
            maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType
                    .valueOf(errorHandlingElement.getTextTrim()));
        }
        
        List<Element> localVariableList = elementMaleSocket.getChildren("localVariable");  // NOI18N
        log.debug("Found " + localVariableList.size() + " male sockets");  // NOI18N
        
        for (Element e : localVariableList) {
            Element elementName = e.getChild("name");
            
            InitialValueType type = null;
            Element elementType = e.getChild("type");
            if (elementType != null) {
                type = InitialValueType.valueOf(elementType.getTextTrim());
            }
            
            Element elementData = e.getChild("data");
            
            if (elementName == null) throw new IllegalArgumentException("Element 'name' does not exists");
            if (type == null) throw new IllegalArgumentException("Element 'type' does not exists");
            if (elementData == null) throw new IllegalArgumentException("Element 'data' does not exists");
            
            maleSocket.addLocalVariable(elementName.getTextTrim(), type, elementData.getTextTrim());
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractManagerXml.class);
}

package jmri.jmrit.logixng.tools.debugger.configurexml;

import java.util.List;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.configurexml.MaleSocketXml;

import org.jdom2.Element;

// import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * Handle XML configuration for AbstractDebuggerMaleSocket objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AbstractDebuggerMaleSocketXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML
        implements MaleSocketXml {

    public AbstractDebuggerMaleSocketXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ActionMany
     *
     * @param o Object to store, of type ActionMany
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
//        AbstractMaleSocket maleSocket = (AbstractMaleSocket) o;
        
        Element element = new Element("AbstractDebuggerMaleSocket");
        element.setAttribute("class", this.getClass().getName());
/*        
        element.addContent(new Element("errorHandling").addContent(maleSocket.getErrorHandlingType().name()));
        for (SymbolTable.VariableData data : maleSocket.getLocalVariables()) {
            Element elementVariable = new Element("localVariable");
            elementVariable.addContent(new Element("name").addContent(data._name));
            elementVariable.addContent(new Element("type").addContent(data._initalValueType.name()));
            elementVariable.addContent(new Element("data").addContent(data._initialValueData));
            element.addContent(elementVariable);
        }
*/        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        return false;
    }
    
    @Override
    public boolean load(Element maleSocketElement, MaleSocket maleSocket) {
/*        
        Element errorHandlingElement = maleSocketElement.getChild("errorHandling");
        if (errorHandlingElement != null) {
            maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType
                    .valueOf(errorHandlingElement.getTextTrim()));
        }
        
        List<Element> localVariableList = maleSocketElement.getChildren("localVariable");  // NOI18N
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
*/        
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractDebuggerMaleSocketXml.class);
}

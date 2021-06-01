package jmri.jmrit.logixng.implementation.configurexml;

import java.util.List;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.configurexml.MaleSocketXml;

import org.jdom2.Element;

import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AbstractMaleSocketXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML
        implements MaleSocketXml {

    public AbstractMaleSocketXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ActionMany
     *
     * @param o Object to store, of type ActionMany
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AbstractMaleSocket maleSocket = (AbstractMaleSocket) o;
        
        Element element = new Element("AbstractMaleSocket");
        element.setAttribute("enabled", maleSocket.isEnabled() ? "yes" : "no");  // NOI18N
        element.setAttribute("catchAbortExecution", maleSocket.getCatchAbortExecution()? "yes" : "no");  // NOI18N
        element.setAttribute("class", this.getClass().getName());
        
        // Only store error handling type of the inner most socket
        if (!(maleSocket.getObject() instanceof MaleSocket)) {
            element.addContent(new Element("errorHandling").addContent(maleSocket.getErrorHandlingType().name()));
        }
        
        for (SymbolTable.VariableData data : maleSocket.getLocalVariables()) {
            Element elementVariable = new Element("LocalVariable");
            elementVariable.addContent(new Element("name").addContent(data._name));
            elementVariable.addContent(new Element("type").addContent(data._initalValueType.name()));
            elementVariable.addContent(new Element("data").addContent(data._initialValueData));
            element.addContent(elementVariable);
        }
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        return false;
    }
    
    @Override
    public boolean load(Element maleSocketElement, MaleSocket maleSocket) {
        if (!(maleSocket instanceof AbstractMaleSocket)) {
            throw new IllegalArgumentException("maleSocket is not an AbstractMaleSocket: "+maleSocket.getClass().getName());
        }
        
        String enabled = "yes";
        if (maleSocketElement.getAttribute("enabled") != null) {  // NOI18N
            enabled = maleSocketElement.getAttribute("enabled").getValue();  // NOI18N
        }
        ((AbstractMaleSocket)maleSocket).setEnabledFlag("yes".equals(enabled));
        
        String catchAbortExecution = "no";
        if (maleSocketElement.getAttribute("catchAbortExecution") != null) {  // NOI18N
            catchAbortExecution = maleSocketElement.getAttribute("catchAbortExecution").getValue();  // NOI18N
        }
        ((AbstractMaleSocket)maleSocket).setCatchAbortExecution("yes".equals(catchAbortExecution));
        
        Element errorHandlingElement = maleSocketElement.getChild("errorHandling");
        if (errorHandlingElement != null) {
            maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType
                    .valueOf(errorHandlingElement.getTextTrim()));
        }
        
        List<Element> localVariableList = maleSocketElement.getChildren("LocalVariable");  // NOI18N
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
        
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMaleSocketXml.class);
}

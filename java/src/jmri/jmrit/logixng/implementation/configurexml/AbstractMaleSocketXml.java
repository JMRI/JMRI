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

        Element element = new Element("AbstractMaleSocket");                     // NOI18N
        element.setAttribute("enabled", maleSocket.isEnabled() ? "yes" : "no");  // NOI18N
        element.setAttribute("locked", maleSocket.isLocked() ? "yes" : "no");    // NOI18N
        element.setAttribute("system", maleSocket.isSystem() ? "yes" : "no");    // NOI18N
        element.setAttribute("catchAbortExecution", maleSocket.getCatchAbortExecution()? "yes" : "no");  // NOI18N
        element.setAttribute("class", this.getClass().getName());

        // Only store error handling type of the inner most socket
        if (!(maleSocket.getObject() instanceof MaleSocket)) {
            element.addContent(new Element("errorHandling").addContent(maleSocket.getErrorHandlingType().name()));  // NOI18N
        }

        for (SymbolTable.VariableData data : maleSocket.getLocalVariables()) {
            Element elementVariable = new Element("LocalVariable");                                     // NOI18N
            elementVariable.addContent(new Element("name").addContent(data._name));                     // NOI18N
            elementVariable.addContent(new Element("type").addContent(data._initialValueType.name()));   // NOI18N
            elementVariable.addContent(new Element("data").addContent(data._initialValueData));         // NOI18N
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

        String enabled = "yes";     // NOI18N
        if (maleSocketElement.getAttribute("enabled") != null) {  // NOI18N
            enabled = maleSocketElement.getAttribute("enabled").getValue();  // NOI18N
        }
        ((AbstractMaleSocket)maleSocket).setEnabledFlag("yes".equals(enabled)); // NOI18N

        String locked = "no";       // NOI18N
        if (maleSocketElement.getAttribute("locked") != null) {  // NOI18N
            locked = maleSocketElement.getAttribute("locked").getValue();   // NOI18N
        }
        ((AbstractMaleSocket)maleSocket).setLocked("yes".equals(locked));   // NOI18N

        String system = "no";       // NOI18N
        if (maleSocketElement.getAttribute("system") != null) {  // NOI18N
            system = maleSocketElement.getAttribute("system").getValue();   // NOI18N
        }
        ((AbstractMaleSocket)maleSocket).setSystem("yes".equals(system));   // NOI18N

        String catchAbortExecution = "no";      // NOI18N
        if (maleSocketElement.getAttribute("catchAbortExecution") != null) {  // NOI18N
            catchAbortExecution = maleSocketElement.getAttribute("catchAbortExecution").getValue();  // NOI18N
        }
        ((AbstractMaleSocket)maleSocket).setCatchAbortExecution("yes".equals(catchAbortExecution));  // NOI18N

        Element errorHandlingElement = maleSocketElement.getChild("errorHandling");     // NOI18N
        if (errorHandlingElement != null) {
            maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType
                    .valueOf(errorHandlingElement.getTextTrim()));
        }

        List<Element> localVariableList = maleSocketElement.getChildren("LocalVariable");  // NOI18N
        log.debug("Found {} male sockets", localVariableList.size() );  // NOI18N

        for (Element e : localVariableList) {
            Element elementName = e.getChild("name");   // NOI18N

            InitialValueType type = null;
            Element elementType = e.getChild("type");   // NOI18N
            if (elementType != null) {
                type = InitialValueType.valueOf(elementType.getTextTrim());
            }

            Element elementData = e.getChild("data");   // NOI18N

            if (elementName == null) throw new IllegalArgumentException("Element 'name' does not exists");  // NOI18N
            if (type == null) throw new IllegalArgumentException("Element 'type' does not exists");         // NOI18N
            if (elementData == null) throw new IllegalArgumentException("Element 'data' does not exists");  // NOI18N

            maleSocket.addLocalVariable(elementName.getTextTrim(), type, elementData.getTextTrim());
        }

        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMaleSocketXml.class);
}

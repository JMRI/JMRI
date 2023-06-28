package jmri.jmrit.logixng.actions.configurexml;

import org.jdom2.Element;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ForEach;
import jmri.jmrit.logixng.actions.CommonManager;
import jmri.jmrit.logixng.actions.ForEach.UserSpecifiedSource;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Handle XML configuration for ForEach objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ForEachXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ForEachXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ForEach p = (ForEach) o;

        Element element = new Element("ForEach");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectVariableXml = new LogixNG_SelectStringXml();
        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();

        element.addContent(new Element("useCommonSource").addContent(p.isUseCommonSource()? "yes" : "no"));
        element.addContent(new Element("commonManager").addContent(p.getCommonManager().name()));   // NOI18N

        element.addContent(new Element("operation").addContent(p.getUserSpecifiedSource().name()));   // NOI18N

        element.addContent(selectVariableXml.store(p.getSelectVariable(), "otherVariable"));

        element.addContent(selectMemoryNamedBeanXml.store(p.getSelectMemoryNamedBean(), "memoryNamedBean"));

        element.addContent(new Element("formula").addContent(p.getFormula()));  // NOI18N

        element.addContent(new Element("localVariable").addContent(p.getLocalVariableName()));

        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ForEach h = new ForEach(sys, uname);

        loadCommon(h, shared);

        var selectVariableXml = new LogixNG_SelectStringXml();
        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();

        Element useCommonSourceElem = shared.getChild("useCommonSource");
        if (useCommonSourceElem != null) {
            h.setUseCommonSource("yes".equals(useCommonSourceElem.getTextTrim()));
        }

        Element commonManagerType = shared.getChild("commonManager");   // NOI18N
        if (commonManagerType != null) {
            try {
                h.setCommonManager(CommonManager.valueOf(commonManagerType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set variable operation: {}", commonManagerType.getTextTrim(), e);  // NOI18N
            }
        }

        Element operationType = shared.getChild("operation");   // NOI18N
        if (operationType != null) {
            try {
                h.setUserSpecifiedSource(UserSpecifiedSource.valueOf(operationType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set variable operation: {}", operationType.getTextTrim(), e);  // NOI18N
            }
        }

        selectVariableXml.load(shared.getChild("otherVariable"), h.getSelectVariable());

        selectMemoryNamedBeanXml.load(shared.getChild("memoryNamedBean"), h.getSelectMemoryNamedBean());

        Element formula = shared.getChild("formula");   // NOI18N
        if (formula != null) {
            try {
                h.setFormula(formula.getTextTrim());
            } catch (ParserException e) {
                log.error("cannot set data: {}", formula.getTextTrim(), e);  // NOI18N
            }
        }

        Element localVariable = shared.getChild("localVariable");
        if (localVariable != null) {
            h.setLocalVariableName(localVariable.getTextTrim());
        }

        Element socketName = shared.getChild("Socket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("Socket").getChild("systemName");
        if (socketSystemName != null) {
            h.setSocketSystemName(socketSystemName.getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForEachXml.class);
}

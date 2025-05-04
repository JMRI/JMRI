package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ExecuteProgram;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringListXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExecuteProgram objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class ExecuteProgramXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExecuteProgramXml() {
    }

    /**
     * Default implementation for storing the contents of a ExecuteProgram
     *
     * @param o Object to store, of type ExecuteProgram
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExecuteProgram p = (ExecuteProgram) o;

        Element element = new Element("ExecuteProgram");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

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

        var selectProgramXml = new LogixNG_SelectStringXml();
        element.addContent(selectProgramXml.store(p.getSelectProgram(), "program"));

        var selectParametersXml = new LogixNG_SelectStringListXml();
        element.addContent(selectParametersXml.store(p.getSelectParameters(), "parameters"));

        element.addContent(new Element("resultVariable").addContent(p.getResultLocalVariable()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExecuteProgram h = new ExecuteProgram(sys, uname);

        Element socketNameElement = shared.getChild("Socket").getChild("socketName");
        String socketName = socketNameElement.getTextTrim();
        Element socketSystemNameElement = shared.getChild("Socket").getChild("systemName");
        String socketSystemName = null;
        if (socketSystemNameElement != null) {
            socketSystemName = socketSystemNameElement.getTextTrim();
        }

        h.getChild(0).setName(socketName);
        h.setSocketSystemName(socketSystemName);

        loadCommon(h, shared);

        var selectProgramXml = new LogixNG_SelectStringXml();
        selectProgramXml.load(shared.getChild("program"), h.getSelectProgram());

        var selectParametersXml = new LogixNG_SelectStringListXml();
        selectParametersXml.load(shared.getChild("parameters"), h.getSelectParameters());

        Element elem = shared.getChild("resultVariable");
        if (elem != null) h.setResultLocalVariable(elem.getTextTrim());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgramXml.class);
}

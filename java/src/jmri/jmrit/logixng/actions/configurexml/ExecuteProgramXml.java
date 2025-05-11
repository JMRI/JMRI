package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ExecuteProgram;
import jmri.jmrit.logixng.util.configurexml.*;

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

        var selectStringXml = new LogixNG_SelectStringXml();
        var selectStringListXml = new LogixNG_SelectStringListXml();
        var selectCharsetXml = new LogixNG_SelectCharsetXml();

        element.addContent(selectStringXml.store(p.getSelectProgram(), "program"));
        element.addContent(selectStringListXml.store(p.getSelectParameters(), "parameters"));
        element.addContent(selectStringListXml.store(p.getSelectEnvironment(), "environment"));
        element.addContent(selectStringXml.store(p.getSelectWorkingDirectory(), "workingDirectory"));
        element.addContent(selectCharsetXml.store(p.getSelectCharset(), "charset"));

        element.addContent(new Element("outputVariable").addContent(p.getOutputLocalVariable()));
        element.addContent(new Element("errorVariable").addContent(p.getErrorLocalVariable()));
        element.addContent(new Element("exitCodeVariable").addContent(p.getExitCodeLocalVariable()));

//        element.addContent(new Element("launchThread").addContent(p.getLaunchThread() ? "yes" : "no"));
        element.addContent(new Element("callChildOnEveryOutput").addContent(p.getCallChildOnEveryOutput() ? "yes" : "no"));
        element.addContent(new Element("joinOutput").addContent(p.getJoinOutput() ? "yes" : "no"));

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

        var selectStringXml = new LogixNG_SelectStringXml();
        var selectStringListXml = new LogixNG_SelectStringListXml();
        var selectCharsetXml = new LogixNG_SelectCharsetXml();

        selectStringXml.load(shared.getChild("program"), h.getSelectProgram());
        selectStringListXml.load(shared.getChild("parameters"), h.getSelectParameters());
        selectStringListXml.load(shared.getChild("environment"), h.getSelectEnvironment());
        selectStringXml.load(shared.getChild("workingDirectory"), h.getSelectWorkingDirectory());
        selectCharsetXml.load(shared.getChild("charset"), h.getSelectCharset());

        Element elem = shared.getChild("outputVariable");
        if (elem != null) h.setOutputLocalVariable(elem.getTextTrim());

        elem = shared.getChild("errorVariable");
        if (elem != null) h.setErrorLocalVariable(elem.getTextTrim());

        elem = shared.getChild("exitCodeVariable");
        if (elem != null) h.setExitCodeLocalVariable(elem.getTextTrim());

        elem = shared.getChild("launchThread");
//        if (elem != null) h.setLaunchThread("yes".equals(elem.getTextTrim()));

        elem = shared.getChild("callChildOnEveryOutput");
        if (elem != null) h.setCallChildOnEveryOutput("yes".equals(elem.getTextTrim()));

        elem = shared.getChild("joinOutput");
        if (elem != null) h.setJoinOutput("yes".equals(elem.getTextTrim()));

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgramXml.class);
}

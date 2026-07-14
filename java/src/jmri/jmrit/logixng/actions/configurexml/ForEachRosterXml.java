package jmri.jmrit.logixng.actions.configurexml;

import org.jdom2.Element;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ForEachRoster;

/**
 * Handle XML configuration for ForEachRoster objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2026
 */
public class ForEachRosterXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ForEachRosterXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ForEachRoster p = (ForEachRoster) o;

        Element element = new Element("ForEachRoster");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

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
        ForEachRoster h = new ForEachRoster(sys, uname);

        loadCommon(h, shared);

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

}

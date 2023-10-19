package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.RunOnce;

import org.jdom2.Element;


/**
 * Handle XML configuration for RunOnce objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class RunOnceXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        RunOnce p = (RunOnce) o;

        Element element = new Element("RunOnce");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        String socketSystemName;
        Element e = new Element("Socket");
        e.addContent(new Element("socketName").addContent(p.getSocket().getName()));
        MaleSocket socket = p.getSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getSocketSystemName();
        }
        if (socketSystemName != null) {
            e.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        String socketName = null;
        String socketSystemName = null;

        if (shared.getChild("Socket") != null) {
            socketName = shared.getChild("Socket").getChild("socketName").getTextTrim();
            Element socketSystemNameElement = shared.getChild("Socket").getChild("systemName");
            if (socketSystemNameElement != null) {
                socketSystemName = socketSystemNameElement.getTextTrim();
            }
        }

        RunOnce h = new RunOnce(sys, uname, socketName, socketSystemName);

        loadCommon(h, shared);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RunOnceXml.class);
}

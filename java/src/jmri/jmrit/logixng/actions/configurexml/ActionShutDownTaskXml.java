package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionShutDownTask;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionShutDownTask objects.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist  Copyright (C) 2022
 */
public class ActionShutDownTaskXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionShutDownTaskXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionShutDownTask p = (ActionShutDownTask) o;

        Element element = new Element("ActionShutDownTask");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Element callSocketElement = new Element("CallSocket");
        callSocketElement.addContent(new Element("socketName").addContent(p.getCallSocket().getName()));
        MaleSocket callSocket = p.getCallSocket().getConnectedSocket();
        String callSocketSystemName;
        if (callSocket != null) {
            callSocketSystemName = callSocket.getSystemName();
        } else {
            callSocketSystemName = p.getCallSocketSystemName();
        }
        if (callSocketSystemName != null) {
            callSocketElement.addContent(new Element("systemName").addContent(callSocketSystemName));
        }
        element.addContent(callSocketElement);

        Element runSocketElement = new Element("RunSocket");
        runSocketElement.addContent(new Element("socketName").addContent(p.getRunSocket().getName()));
        MaleSocket runSocket = p.getRunSocket().getConnectedSocket();
        String runSocketSystemName;
        if (runSocket != null) {
            runSocketSystemName = runSocket.getSystemName();
        } else {
            runSocketSystemName = p.getRunSocketSystemName();
        }
        if (runSocketSystemName != null) {
            runSocketElement.addContent(new Element("systemName").addContent(runSocketSystemName));
        }
        element.addContent(runSocketElement);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        Element callSocketNameElement = shared.getChild("CallSocket").getChild("socketName");
        String callSocketName = callSocketNameElement.getTextTrim();
        Element callSocketSystemNameElement = shared.getChild("CallSocket").getChild("systemName");
        String callSocketSystemName = null;
        if (callSocketSystemNameElement != null) {
            callSocketSystemName = callSocketSystemNameElement.getTextTrim();
        }

        Element runSocketNameElement = shared.getChild("RunSocket").getChild("socketName");
        String runSocketName = runSocketNameElement.getTextTrim();
        Element runSocketSystemNameElement = shared.getChild("RunSocket").getChild("systemName");
        String runSocketSystemName = null;
        if (runSocketSystemNameElement != null) {
            runSocketSystemName = runSocketSystemNameElement.getTextTrim();
        }

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionShutDownTask h = new ActionShutDownTask(sys, uname);

        loadCommon(h, shared);

        h.getCallSocket().setName(callSocketName);
        h.setCallSocketSystemName(callSocketSystemName);

        h.getRunSocket().setName(runSocketName);
        h.setRunSocketSystemName(runSocketSystemName);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionShutDownTaskXml.class);
}

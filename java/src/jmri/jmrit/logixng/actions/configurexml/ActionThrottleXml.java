package jmri.jmrit.logixng.actions.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.SystemConnectionMemo;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionThrottle;

import org.jdom2.Element;

import jmri.jmrit.logixng.MaleSocket;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionThrottleXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionThrottleXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionThrottle
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionThrottle p = (ActionThrottle) o;

        Element element = new Element("Throttle");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Element e2 = new Element("LocoAddressSocket");
        e2.addContent(new Element("socketName").addContent(p.getLocoAddressSocket().getName()));
        MaleSocket socket = p.getLocoAddressSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoAddressSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("LocoSpeedSocket");
        e2.addContent(new Element("socketName").addContent(p.getLocoSpeedSocket().getName()));
        socket = p.getLocoSpeedSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoSpeedSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("LocoDirectionSocket");
        e2.addContent(new Element("socketName").addContent(p.getLocoDirectionSocket().getName()));
        socket = p.getLocoDirectionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoDirectionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("LocoFunctionSocket");
        e2.addContent(new Element("socketName").addContent(p.getLocoFunctionSocket().getName()));
        socket = p.getLocoFunctionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoFunctionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("LocoFunctionOnOffSocket");
        e2.addContent(new Element("socketName").addContent(p.getLocoFunctionOnOffSocket().getName()));
        socket = p.getLocoFunctionOnOffSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoFunctionOnOffSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        if (!p.isStopLocoWhenSwitchingLoco()) {
            element.addContent(new Element("stopLocoWhenSwitchingLoco")
                    .addContent("no"));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionThrottle h = new ActionThrottle(sys, uname);

        loadCommon(h, shared);

        Element socketName = shared.getChild("LocoAddressSocket").getChild("socketName");
        h.getLocoAddressSocket().setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("LocoAddressSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setLocoAddressSocketSystemName(socketSystemName.getTextTrim());
        }

        socketName = shared.getChild("LocoSpeedSocket").getChild("socketName");
        h.getLocoSpeedSocket().setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("LocoSpeedSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setLocoSpeedSocketSystemName(socketSystemName.getTextTrim());
        }

        socketName = shared.getChild("LocoDirectionSocket").getChild("socketName");
        h.getLocoDirectionSocket().setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("LocoDirectionSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setLocoDirectionSocketSystemName(socketSystemName.getTextTrim());
        }

        Element locoFunctionSocket = shared.getChild("LocoFunctionSocket");
        if (locoFunctionSocket != null) {
            socketName = locoFunctionSocket.getChild("socketName");
            h.getLocoFunctionSocket().setName(socketName.getTextTrim());
            socketSystemName = locoFunctionSocket.getChild("systemName");
            if (socketSystemName != null) {
                h.setLocoFunctionSocketSystemName(socketSystemName.getTextTrim());
            }
        }

        Element locoFunctionOnOffSocket = shared.getChild("LocoFunctionOnOffSocket");
        if (locoFunctionOnOffSocket != null) {
            socketName = locoFunctionOnOffSocket.getChild("socketName");
            h.getLocoFunctionOnOffSocket().setName(socketName.getTextTrim());
            socketSystemName = locoFunctionOnOffSocket.getChild("systemName");
            if (socketSystemName != null) {
                h.setLocoFunctionOnOffSocketSystemName(socketSystemName.getTextTrim());
            }
        }

        Element systemConnection = shared.getChild("systemConnection");
        if (systemConnection != null) {
            String systemConnectionName = systemConnection.getTextTrim();
            List<SystemConnectionMemo> systemConnections =
                    jmri.InstanceManager.getList(SystemConnectionMemo.class);

            for (SystemConnectionMemo memo : systemConnections) {
                if (memo.getSystemPrefix().equals(systemConnectionName)) {
                    h.setMemo(memo);
                    break;
                }
            }
        }

        Element stopLocoWhenSwitchingLoco = shared.getChild("stopLocoWhenSwitchingLoco");
        if (stopLocoWhenSwitchingLoco != null) {
            h.setStopLocoWhenSwitchingLoco("yes".equals(stopLocoWhenSwitchingLoco.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionThrottleXml.class);
}

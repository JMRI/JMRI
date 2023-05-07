package jmri.jmrit.logixng.actions.configurexml;

import java.util.*;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionRequestUpdateAllSensors;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionRequestUpdateAllSensors objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ActionRequestUpdateAllSensorsXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionRequestUpdateAllSensorsXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionSlotUsage
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionRequestUpdateAllSensors p = (ActionRequestUpdateAllSensors) o;

        Element element = new Element("ActionRequestUpdateAllSensors");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionRequestUpdateAllSensors h = new ActionRequestUpdateAllSensors(sys, uname, null);

        loadCommon(h, shared);

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

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionRequestUpdateAllSensorsXml.class);
}

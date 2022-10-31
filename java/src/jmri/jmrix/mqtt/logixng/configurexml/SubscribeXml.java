package jmri.jmrix.mqtt.logixng.configurexml;

import java.util.List;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.jmrix.mqtt.logixng.Subscribe;

import org.jdom2.Element;

/**
 * Handle XML configuration for Subscribe objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class SubscribeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SubscribeXml() {
    }

    /**
     * Default implementation for storing the contents of a subscribe action.
     *
     * @param o Object to store, of type Publish
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Subscribe p = (Subscribe) o;

        Element element = new Element("MQTTSubscribe");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        if (p.getSubscribeToTopic() != null) {
            element.addContent(new Element("subscribeToTopic")
                    .addContent(p.getSubscribeToTopic()));
        }

        if (p.getLastTopicLocalVariable() != null) {
            element.addContent(new Element("lastTopicLocalVariable")
                    .addContent(p.getLastTopicLocalVariable()));
        }
        element.addContent(new Element("removeChannelFromLastTopic").addContent(p.getRemoveChannelFromLastTopic() ? "yes" : "no"));

        if (p.getLastMessageLocalVariable() != null) {
            element.addContent(new Element("lastMessageLocalVariable")
                    .addContent(p.getLastMessageLocalVariable()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        MqttSystemConnectionMemo memo = null;

        Element systemConnection = shared.getChild("systemConnection");
        if (systemConnection != null) {
            String systemConnectionName = systemConnection.getTextTrim();
            List<MqttSystemConnectionMemo> systemConnections =
                    jmri.InstanceManager.getList(MqttSystemConnectionMemo.class);

            for (MqttSystemConnectionMemo m : systemConnections) {
                if (m.getSystemPrefix().equals(systemConnectionName)) {
                    memo = m;
                    break;
                }
            }
        }

        Subscribe h = new Subscribe(sys, uname, memo);

        loadCommon(h, shared);

        if (shared.getChild("subscribeToTopic") != null) {
            h.setSubscribeToTopic(shared.getChild("subscribeToTopic").getTextTrim());
        }
        if (shared.getChild("lastTopicLocalVariable") != null) {
            h.setLastTopicLocalVariable(shared.getChild("lastTopicLocalVariable").getTextTrim());
        }
        Element elem = shared.getChild("removeChannelFromLastTopic");  // NOI18N
        h.setRemoveChannelFromLastTopic((elem != null) ? elem.getTextTrim().equals("yes") : false);  // NOI18N

        if (shared.getChild("lastMessageLocalVariable") != null) {
            h.setLastMessageLocalVariable(shared.getChild("lastMessageLocalVariable").getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublishXml.class);
}

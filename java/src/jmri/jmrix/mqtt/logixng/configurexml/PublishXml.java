package jmri.jmrix.mqtt.logixng.configurexml;

import java.util.List;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.jmrix.mqtt.logixng.Publish;
import jmri.jmrix.mqtt.logixng.Publish.Retain;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for Publish objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class PublishXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public PublishXml() {
    }

    /**
     * Default implementation for storing the contents of a publish action.
     *
     * @param o Object to store, of type Publish
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Publish p = (Publish) o;

        var selectTopicXml = new LogixNG_SelectStringXml();
        var selectDataXml = new LogixNG_SelectStringXml();

        Element element = new Element("MQTTPublish");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        element.addContent(selectTopicXml.store(p.getSelectTopic(), "topic"));
        element.addContent(selectDataXml.store(p.getSelectMessage(), "message"));

        element.addContent(new Element("retain").addContent(p.getRetain().name()));

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

        Publish h = new Publish(sys, uname, memo);

        var selectTopicXml = new LogixNG_SelectStringXml();
        var selectDataXml = new LogixNG_SelectStringXml();

        loadCommon(h, shared);

        selectTopicXml.load(shared.getChild("topic"), h.getSelectTopic());
        selectDataXml.load(shared.getChild("message"), h.getSelectMessage());

        Element retainElem = shared.getChild("retain");
        if (retainElem != null) {
            h.setRetain(Retain.valueOf(retainElem.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublishXml.class);
}

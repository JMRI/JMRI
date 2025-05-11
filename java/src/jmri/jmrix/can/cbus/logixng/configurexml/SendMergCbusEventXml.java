package jmri.jmrix.can.cbus.logixng.configurexml;

import java.util.List;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectIntegerXml;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.logixng.CategoryMergCbus;
import jmri.jmrix.can.cbus.logixng.SendMergCbusEvent;

import org.jdom2.Element;

/**
 * Handle XML configuration for SendCbusEvent objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class SendMergCbusEventXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SendMergCbusEventXml() {
    }

    /**
     * Default implementation for storing the contents of a clock action.
     *
     * @param o Object to store, of type SendCbusEvent
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SendMergCbusEvent p = (SendMergCbusEvent) o;

        var selectEnumXml = new LogixNG_SelectEnumXml<SendMergCbusEvent.CbusEventType>();
        var selectIntegerXml = new LogixNG_SelectIntegerXml();

        Element element = new Element("SendMergCbusEvent");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        element.addContent(selectIntegerXml.store(p.getSelectNodeNumber(), "nodeNumber"));
        element.addContent(selectIntegerXml.store(p.getSelectEventNumber(), "eventNumber"));
        element.addContent(selectEnumXml.store(p.getSelectEventType(), "eventType"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SendMergCbusEvent h = new SendMergCbusEvent(sys, uname, null);

        var selectEnumXml = new LogixNG_SelectEnumXml<SendMergCbusEvent.CbusEventType>();
        var selectRateXml = new LogixNG_SelectIntegerXml();

        loadCommon(h, shared);

        Element systemConnection = shared.getChild("systemConnection");
        if (systemConnection != null) {
            String systemConnectionName = systemConnection.getTextTrim();
            List<CanSystemConnectionMemo> systemConnections = CategoryMergCbus.getMergConnections();

            for (CanSystemConnectionMemo memo : systemConnections) {
                if (memo.getSystemPrefix().equals(systemConnectionName)) {
                    h.setMemo(memo);
                    break;
                }
            }
        }

        selectRateXml.load(shared.getChild("nodeNumber"), h.getSelectNodeNumber());
        selectRateXml.load(shared.getChild("eventNumber"), h.getSelectEventNumber());
        selectEnumXml.load(shared.getChild("eventType"), h.getSelectEventType());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SendCbusEventXml.class);
}

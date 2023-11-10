package jmri.jmrit.logixng.expressions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.Timer;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for Timer objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class TimerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TimerXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionMany
     *
     * @param o Object to store, of type ActionMany
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Timer p = (Timer) o;

        Element element = new Element("Timer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("delayAddressing").addContent(p.getDelayAddressing().name()));
        element.addContent(new Element("delay").addContent(Integer.toString(p.getDelay())));
        element.addContent(new Element("delayReference").addContent(p.getDelayReference()));
        element.addContent(new Element("delayLocalVariable").addContent(p.getDelayLocalVariable()));
        element.addContent(new Element("delayFormula").addContent(p.getDelayFormula()));

        element.addContent(new Element("unit").addContent(p.getUnit().name()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Timer h = new Timer(sys, uname);

        loadCommon(h, shared);

        Element delayElement = shared.getChild("delay");
        int delay = 0;
        if (delayElement != null) {
            delay = Integer.parseInt(delayElement.getText());
        }
        h.setDelay(delay);

        Element unit = shared.getChild("unit");
        if (unit != null) {
            h.setUnit(TimerUnit.valueOf(unit.getTextTrim()));
        }

        try {
            Element elem = shared.getChild("delayAddressing");
            if (elem != null) {
                h.setDelayAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("delayReference");
            if (elem != null) h.setDelayReference(elem.getTextTrim());

            elem = shared.getChild("delayLocalVariable");
            if (elem != null) h.setDelayLocalVariable(elem.getTextTrim());

            elem = shared.getChild("delayFormula");
            if (elem != null) h.setDelayFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerXml.class);
}

package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Delay;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for Delay objects.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class DelayXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DelayXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Delay p = (Delay) o;

        Element element = new Element("Delay");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        element.addContent(new Element("time").addContent(Integer.toString(p.getTime())));
        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(new Element("timeUnitAddressing").addContent(p.getTimeUnitAddressing().name()));
        element.addContent(new Element("timeUnit").addContent(p.getTimeUnit().name()));
        element.addContent(new Element("timeUnitReference").addContent(p.getTimeUnitReference()));
        element.addContent(new Element("timeUnitLocalVariable").addContent(p.getTimeUnitLocalVariable()));
        element.addContent(new Element("timeUnitFormula").addContent(p.getTimeUnitFormula()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Delay h = new Delay(sys, uname);
        
        loadCommon(h, shared);
        
        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("time");
            if (elem != null) {
                h.setTime(Integer.parseInt(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());


            elem = shared.getChild("timeUnitAddressing");
            if (elem != null) {
                h.setTimeUnitAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element lightState = shared.getChild("timeUnit");
            if (lightState != null) {
                h.setTimeUnit(Delay.TimeUnit.valueOf(lightState.getTextTrim()));
            }

            elem = shared.getChild("timeUnitReference");
            if (elem != null) h.setTimeUnitReference(elem.getTextTrim());

            elem = shared.getChild("timeUnitLocalVariable");
            if (elem != null) h.setTimeUnitLocalVariable(elem.getTextTrim());

            elem = shared.getChild("timeUnitFormula");
            if (elem != null) h.setTimeUnitFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DelayXml.class);
}

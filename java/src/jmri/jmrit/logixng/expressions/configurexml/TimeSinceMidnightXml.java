package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.expressions.TimeSinceMidnight;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class TimeSinceMidnightXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TimeSinceMidnightXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        TimeSinceMidnight p = (TimeSinceMidnight) o;

        Element element = new Element("TimeSinceMidnight");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("type").addContent(p.getType().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        TimeSinceMidnight h = new TimeSinceMidnight(sys, uname);

        loadCommon(h, shared);

        Element type = shared.getChild("type");
        if (type != null) {
            h.setType(TimeSinceMidnight.Type.valueOf(type.getTextTrim()));
        }
        
        InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeSinceMidnightXml.class);
}

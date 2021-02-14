package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.expressions.ExpressionClock;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionClockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionClockXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionClock p = (ExpressionClock) o;

        Element element = new Element("ExpressionClock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        element.addContent(new Element("type").addContent(p.getType().name()));
        element.addContent(new Element("beginTime").addContent(Integer.toString(p.getBeginTime())));
        element.addContent(new Element("endTime").addContent(Integer.toString(p.getEndTime())));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionClock h = new ExpressionClock(sys, uname);

        loadCommon(h, shared);

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        Element type = shared.getChild("type");
        if (type != null) {
            h.setType(ExpressionClock.Type.valueOf(type.getTextTrim()));
        }
        
        int beginTime = 0;
        int endTime = 0;
        Element beginTimeElement = shared.getChild("beginTime");
        Element endTimeElement = shared.getChild("endTime");
        
        if (beginTimeElement != null) {
            try {
                beginTime = Integer.parseInt(beginTimeElement.getTextTrim());
            } catch (NumberFormatException e) {
                log.error("cannot parse beginTime: " + beginTimeElement.getTextTrim(), e);
            }
        }
        if (endTimeElement != null) {
            try {
                endTime =  Integer.parseInt(endTimeElement.getTextTrim());
                h.setRange(beginTime, endTime);
            } catch (NumberFormatException e) {
                log.error("cannot parse endTime: " + endTimeElement.getTextTrim(), e);
            }
        }
        h.setRange(beginTime, endTime);

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionClockXml.class);
}

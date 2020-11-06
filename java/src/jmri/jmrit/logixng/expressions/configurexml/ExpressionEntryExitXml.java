package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.expressions.ExpressionEntryExit;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionEntryExitXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionEntryExitXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionEntryExit p = (ExpressionEntryExit) o;

//        if (p.getLightName() == null) throw new RuntimeException("aaaaa");
        
        Element element = new Element("expression-entryexit");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

//        NamedBeanHandle light = p.getLight();
//        if (light != null) {
//            element.addContent(new Element("light").addContent(light.getName()));
//        }
        
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
//        element.addContent(new Element("lightState").addContent(p.getLightState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionEntryExit h = new ExpressionEntryExit(sys, uname);

        loadCommon(h, shared);

//        Element lightName = shared.getChild("light");
//        if (lightName != null) {
//            Light t = InstanceManager.getDefault(LightManager.class).getLight(lightName.getTextTrim());
//            if (t != null) h.setLight(t);
//            else h.removeLight();
//        }

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLightXml.class);
}

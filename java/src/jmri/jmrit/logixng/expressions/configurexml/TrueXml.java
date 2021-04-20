package jmri.jmrit.logixng.expressions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.True;

import org.jdom2.Element;

import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class TrueXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TrueXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTrueSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        True p = (True) o;

        Element element = new Element("True");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DigitalExpressionBean h = new True(sys, uname);

        loadCommon(h, shared);

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrueXml.class);
}

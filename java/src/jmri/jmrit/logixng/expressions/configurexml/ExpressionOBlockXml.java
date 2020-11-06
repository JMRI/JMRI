package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.expressions.ExpressionOBlock;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionOBlockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionOBlockXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionOBlock p = (ExpressionOBlock) o;

//        if (p.getLightName() == null) throw new RuntimeException("aaaaa");
        
        Element element = new Element("expression-oblock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        OBlock oblock = p.getOBlock();
        if (oblock != null) {
            element.addContent(new Element("oblock").addContent(oblock.getSystemName()));
        }
        
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        element.addContent(new Element("oblockStatus").addContent(p.getOBlockStatus().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionOBlock h = new ExpressionOBlock(sys, uname);

        loadCommon(h, shared);

        Element oblockName = shared.getChild("oblock");
        if (oblockName != null) {
            OBlock t = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class)
                    .getOBlock(oblockName.getTextTrim());
            if (t != null) h.setOBlock(t);
            else h.removeOBlock();
        }

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        Element oblockStatus = shared.getChild("oblockStatus");
        if (oblockStatus != null) {
            h.setOBlockStatus(OBlock.OBlockStatus.valueOf(oblockStatus.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLightXml.class);
}

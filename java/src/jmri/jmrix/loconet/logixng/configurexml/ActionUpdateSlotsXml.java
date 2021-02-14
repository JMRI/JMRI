package jmri.jmrix.loconet.logixng.configurexml;

import java.util.*;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.logixng.ActionUpdateSlots;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSlotUsage objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class ActionUpdateSlotsXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionUpdateSlotsXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ExpressionSlotUsage
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionUpdateSlots p = (ActionUpdateSlots) o;

        Element element = new Element("ActionLoconetUpdateSlots");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionUpdateSlots h = new ActionUpdateSlots(sys, uname, null);

        loadCommon(h, shared);

        Element systemConnection = shared.getChild("systemConnection");
        if (systemConnection != null) {
            String systemConnectionName = systemConnection.getTextTrim();
            List<LocoNetSystemConnectionMemo> systemConnections =
                    jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
            
            for (LocoNetSystemConnectionMemo memo : systemConnections) {
                if (memo.getSystemPrefix().equals(systemConnectionName)) {
                    h.setMemo(memo);
                    break;
                }
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionUpdateSlotsXml.class);
}

package jmri.jmrit.logixng.implementation.configurexml;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.implementation.ClipboardMany;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ClipboardManyXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a Many
     *
     * @param o Object to store, of type Many
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ClipboardMany p = (ClipboardMany) o;

        Element element = new Element("Many");
        element.setAttribute("class", this.getClass().getName());

        for (int i=0; i < p.getChildCount(); i++) {
            Element e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getChild(i).getName()));
            MaleSocket socket = p.getChild(i).getConnectedSocket();
            
            String socketSystemName;
            String socketManager;
            if (socket != null) {
                socketSystemName = socket.getSystemName();
                socketManager = socket.getManager().getClass().getName();
                e2.addContent(new Element("systemName").addContent(socketSystemName));
                e2.addContent(new Element("manager").addContent(socketManager));
            }
            element.addContent(e2);
        }

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public ClipboardMany loadItem(Element shared) {
        
        List<ClipboardMany.ItemData> systemNamesAndClasses = new ArrayList<>();
        
        Element actionElement = shared.getChild("Many");
        for (Element socketElement : actionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            Element managerElement = socketElement.getChild("manager");
            String manager = null;
            if (managerElement != null) {
                manager = managerElement.getTextTrim();
            }
            systemNamesAndClasses.add(new ClipboardMany.ItemData(socketName, systemName, manager));
        }
        
        ClipboardMany h = new ClipboardMany("", null, systemNamesAndClasses);
        
        loadCommon(h, shared);
        
//        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        
//        log.warn("Register action: " + h.getSystemName() + ", " + h.getLongDescription());
        return h;
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalManyXml.class);
    
}

package jmri.jmrit.logixng.actions.configurexml;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;

import jmri.jmrit.logixng.StringActionBean;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.StringMany;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class StringManyXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public StringManyXml() {
//        jmri.managers.configurexml.DefaultConditionalManagerXml a;
    }
/*
    @SuppressWarnings("unchecked")  // Reflection does not support generics
    private List<ActionMany.ActionEntry> getActionEntry(Many actionMany)
            throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        
        Field f = actionMany.getClass().getDeclaredField("actionEntries");
        f.setAccessible(true);
        return (List<ActionMany.ActionEntry>) f.get(actionMany);
    }
*/
    /**
     * Default implementation for storing the contents of a Many
     *
     * @param o Object to store, of type Many
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        StringMany p = (StringMany) o;

        Element element = new Element("StringMany");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        Element e = new Element("Actions");
        for (int i=0; i < p.getChildCount(); i++) {
            Element e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getChild(i).getName()));
            MaleSocket socket = p.getChild(i).getConnectedSocket();
            String socketSystemName;
            if (socket != null) {
                socketSystemName = socket.getSystemName();
            } else {
                socketSystemName = p.getActionSystemName(i);
            }
            if (socketSystemName != null) {
                e2.addContent(new Element("systemName").addContent(socketSystemName));
            }
            e.addContent(e2);
        }
        element.addContent(e);

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        
        Element actionElement = shared.getChild("Actions");
        for (Element socketElement : actionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            actionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        StringActionBean h = new StringMany(sys, uname, actionSystemNames);
        
        loadCommon(h, shared);
        
        InstanceManager.getDefault(StringActionManager.class).registerAction(h);
        
//        log.warn("Register action: " + h.getSystemName() + ", " + h.getLongDescription());
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManyXml.class);

}

package jmri.jmrix.loconet.logixng.configurexml;

import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.jmrix.loconet.logixng.StringActionLocoNet_OPC_PEER;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class StringActionLocoNet_OPC_PEERXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public StringActionLocoNet_OPC_PEERXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        StringActionLocoNet_OPC_PEER p = (StringActionLocoNet_OPC_PEER) o;

        Element element = new Element("string-action-loconet-opc-peer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

//        NamedBeanHandle memory = p.getMemory();
//        if (memory != null) {
//            element.addContent(new Element("memory").addContent(memory.getName()));
//        }
        
        return element;
    }
/*
    Element addLightElement(NamedBeanHandle<Light> to, String which) {
        Element el = new Element("lightname");
        el.setAttribute("defines", which);
        el.addContent(to.getName());
        return el;
    }

    Element addLightElement(Light to) {
        String user = to.getUserName();
        String sys = to.getSystemName();

        Element el = new Element("light");
        el.setAttribute("systemName", sys);
        if (user != null) {
            el.setAttribute("userName", user);
        }

        return el;
    }
*/
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
//        List<Element> l = shared.getChildren("lightname");
/*        
        if (l.size() == 0) {
            l = shared.getChildren("light");  // older form
        }
        NamedBeanHandle<Light> low = loadLight(l.get(0));
        NamedBeanHandle<Light> high = loadLight(l.get(1));
*/        
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        StringActionLocoNet_OPC_PEER h = new StringActionLocoNet_OPC_PEER(sys, uname);

        loadCommon(h, shared);

//        Element memoryName = shared.getChild("memory");
//        if (memoryName != null) {
//            h.setMemory(InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim()));
//        }

        // this.checkedNamedBeanReference()
        // <T extends NamedBean> T checkedNamedBeanReference(String name, @Nonnull T type, @Nonnull Manager<T> m) {

        InstanceManager.getDefault(StringActionManager.class).registerAction(h);
        return true;
    }
    
    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(StringActionLocoNet_OPC_PEERXml.class);
}

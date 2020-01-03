package jmri.jmrix.loconet.logixng.configurexml;

import java.nio.charset.Charset;
import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.jmrix.loconet.logixng.StringActionLocoNetOpcPeer;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class StringActionLocoNetOpcPeerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public StringActionLocoNetOpcPeerXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        StringActionLocoNetOpcPeer p = (StringActionLocoNetOpcPeer) o;

        Element element = new Element("string-action-loconet-opc-peer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        element.addContent(new Element("manufacturerID").addContent(Integer.toString(p.getManufacturerID())));
        element.addContent(new Element("developerID").addContent(Integer.toString(p.getDeveloperID())));
        element.addContent(new Element("sourceAddress").addContent(Integer.toString(p.getSourceAddress())));
        element.addContent(new Element("destAddress").addContent(Integer.toString(p.getDestAddress())));
        element.addContent(new Element("svAddress").addContent(Integer.toString(p.get_SV_Address())));
        element.addContent(new Element("charset").addContent(p.getCharset().name()));
//        NamedBeanHandle memory = p.getMemory();
//        if (memory != null) {
//            element.addContent(new Element("memory").addContent(memory.getName()));
//        }
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        StringActionLocoNetOpcPeer p = new StringActionLocoNetOpcPeer(sys, uname);
        
        loadCommon(p, shared);
        
        Element numberElement = shared.getChild("manufacturerID");
        if (numberElement != null) {
            p.setManufacturerID(Integer.parseInt(numberElement.getTextTrim()));
        }
        
        numberElement = shared.getChild("developerID");
        if (numberElement != null) {
            p.setDeveloperID(Integer.parseInt(numberElement.getTextTrim()));
        }
        
        numberElement = shared.getChild("sourceAddress");
        if (numberElement != null) {
            p.setSourceAddress(Integer.parseInt(numberElement.getTextTrim()));
        }
        
        numberElement = shared.getChild("destAddress");
        if (numberElement != null) {
            p.setDestAddress(Integer.parseInt(numberElement.getTextTrim()));
        }
        
        numberElement = shared.getChild("svAddress");
        if (numberElement != null) {
            p.set_SV_Address(Integer.parseInt(numberElement.getTextTrim()));
        }
        
        Element charsetName = shared.getChild("charset");
        if (charsetName != null) {
            Charset charset = Charset.availableCharsets().get(charsetName.getTextTrim());
            if (charset != null) {
                p.setCharset(charset);
            } else {
                log.error("Charset " + charset + " is not found");
            }
        }
        
//        Element memoryName = shared.getChild("memory");
//        if (memoryName != null) {
//            h.setMemory(InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim()));
//        }
        
        InstanceManager.getDefault(StringActionManager.class).registerAction(p);
        return true;
    }
    
    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(StringActionLocoNetOpcPeerXml.class);
}

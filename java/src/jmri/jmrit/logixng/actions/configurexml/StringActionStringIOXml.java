package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.actions.StringActionStringIO;

import org.jdom2.Element;

/**
 *
 */
public class StringActionStringIOXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public StringActionStringIOXml() {
    }
    
    /**
     * Default implementation for storing the contents of a StringActionStringIO
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        StringActionStringIO p = (StringActionStringIO) o;

        Element element = new Element("StringActionStringIO");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle memory = p.getStringIO();
        if (memory != null) {
            element.addContent(new Element("stringIO").addContent(memory.getName()));
        }
        
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        StringActionStringIO h = new StringActionStringIO(sys, uname);

        loadCommon(h, shared);

        Element stringIOName = shared.getChild("stringIO");
        if (stringIOName != null) {
            StringIO m = InstanceManager.getDefault(StringIOManager.class).getNamedBean(stringIOName.getTextTrim());
            if (m != null) h.setStringIO(m);
            else h.removeStringIO();
        }

        InstanceManager.getDefault(StringActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionStringIOXml.class);
}

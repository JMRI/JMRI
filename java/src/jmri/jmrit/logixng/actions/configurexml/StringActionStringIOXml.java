package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.actions.StringActionStringIO;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

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

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<StringIO>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        StringActionStringIO h = new StringActionStringIO(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<StringIO>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "stringIO");

        InstanceManager.getDefault(StringActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionStringIOXml.class);
}

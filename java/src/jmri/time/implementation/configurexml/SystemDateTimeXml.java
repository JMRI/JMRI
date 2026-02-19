package jmri.time.implementation.configurexml;

import java.util.Objects;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;
import jmri.time.implementation.SystemDateTime;

import org.jdom2.Element;

/**
 * Store and load an SystemDateTime.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class SystemDateTimeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    @Override
    public Element store(Object o) {
        SystemDateTime p = (SystemDateTime) o;

        Element element = new Element("InternalDateTime");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class).getBySystemName(sys);

        if (tp == null) {
            tp = new SystemDateTime(sys);
            InstanceManager.getDefault(TimeProviderManager.class).register(tp);
        }

        if (!Objects.equals(uname, tp.getUserName())) {
            tp.setUserName(uname);
        }

        loadCommon(tp, shared);

        return true;
    }

}

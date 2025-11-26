package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ValidationError;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ValidationError objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class ValidationErrorXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ValidationErrorXml() {
    }

    /**
     * Default implementation for storing the contents of an ValidationError
     *
     * @param o Object to store, of type ValidationError
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ValidationError p = (ValidationError) o;

        Element element = new Element("ValidationError");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectMessageXml = new LogixNG_SelectStringXml();
        element.addContent(selectMessageXml.store(p.getSelectMessage(), "message"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ValidationError h = new ValidationError(sys, uname);

        loadCommon(h, shared);

        var selectMessageXml = new LogixNG_SelectStringXml();
        selectMessageXml.load(shared.getChild("message"), h.getSelectMessage());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationErrorXml.class);
}

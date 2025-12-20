package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ExecuteProgram;
import jmri.jmrit.logixng.util.configurexml.*;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExecuteProgram objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class ExecuteProgramXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExecuteProgramXml() {
    }

    /**
     * Default implementation for storing the contents of a ExecuteProgram
     *
     * @param o Object to store, of type ExecuteProgram
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExecuteProgram p = (ExecuteProgram) o;

        Element element = new Element("ExecuteProgram");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectStringXml = new LogixNG_SelectStringXml();
        var selectStringListXml = new LogixNG_SelectStringListXml();

        element.addContent(selectStringXml.store(p.getSelectProgram(), "program"));
        element.addContent(selectStringListXml.store(p.getSelectParameters(), "parameters"));
        element.addContent(selectStringXml.store(p.getSelectWorkingDirectory(), "workingDirectory"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExecuteProgram h = new ExecuteProgram(sys, uname);

        loadCommon(h, shared);

        var selectStringXml = new LogixNG_SelectStringXml();
        var selectStringListXml = new LogixNG_SelectStringListXml();

        selectStringXml.load(shared.getChild("program"), h.getSelectProgram());
        selectStringListXml.load(shared.getChild("parameters"), h.getSelectParameters());
        selectStringXml.load(shared.getChild("workingDirectory"), h.getSelectWorkingDirectory());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgramXml.class);
}

package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.LogLocalVariables;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class LogLocalVariablesXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LogLocalVariablesXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LogLocalVariables p = (LogLocalVariables) o;

        Element element = new Element("LogLocalVariables");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("includeGlobalVariables").addContent(p.isIncludeGlobalVariables()? "yes" : "no"));
        element.addContent(new Element("expandArraysAndMaps").addContent(p.isExpandArraysAndMaps()? "yes" : "no"));
        if (p.isShowClassName()) {
            element.addContent(new Element("showClassName").addContent("yes"));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        LogLocalVariables h = new LogLocalVariables(sys, uname);

        loadCommon(h, shared);

        Element includeGlobalVariables = shared.getChild("includeGlobalVariables");
        if (includeGlobalVariables != null) {
            h.setIncludeGlobalVariables("yes".equals(includeGlobalVariables.getTextTrim()));
        } else {
            h.setIncludeGlobalVariables(true);
        }

        Element _expand = shared.getChild("expandArraysAndMaps");
        if (_expand != null) {
            h.setExpandArraysAndMaps("yes".equals(_expand.getTextTrim()));
        } else {
            h.setExpandArraysAndMaps(false);
        }

        Element _showClassName = shared.getChild("showClassName");
        if (_showClassName != null) {
            h.setShowClassName("yes".equals(_showClassName.getTextTrim()));
        } else {
            h.setShowClassName(false);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogLocalVariablesXml.class);
}

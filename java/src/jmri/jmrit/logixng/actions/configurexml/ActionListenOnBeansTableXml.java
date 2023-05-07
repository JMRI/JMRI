package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.TableRowOrColumn;
import jmri.jmrit.logixng.actions.ActionListenOnBeansTable;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionListenOnBeansTable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionListenOnBeansTableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionListenOnBeansTableXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionListenOnBeansTable p = (ActionListenOnBeansTable) o;

        Element element = new Element("ActionListenOnBeansTable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<NamedTable>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("rowOrColumnName").addContent(p.getRowOrColumnName()));
        element.addContent(new Element("tableRowOrColumn").addContent(p.getTableRowOrColumn().name()));

        element.setAttribute("includeCellsWithoutHeader",
                p.getIncludeCellsWithoutHeader() ? "yes" : "no");  // NOI18N

        element.addContent(new Element("namedBeanType").addContent(p.getNamedBeanType().name()));

        element.addContent(new Element("localVariableNamedBean").addContent(p.getLocalVariableNamedBean()));
        element.addContent(new Element("localVariableEvent").addContent(p.getLocalVariableEvent()));
        element.addContent(new Element("localVariableNewValue").addContent(p.getLocalVariableNewValue()));

        element.setAttribute("listenOnAllProperties",
                p.getListenOnAllProperties()? "yes" : "no");  // NOI18N

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionListenOnBeansTable h = new ActionListenOnBeansTable(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<NamedTable>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "table", null, null, null, null);

        Element tableRowOrColumnElement = shared.getChild("tableRowOrColumn");
        TableRowOrColumn tableRowOrColumn =
                TableRowOrColumn.valueOf(tableRowOrColumnElement.getTextTrim());
        h.setTableRowOrColumn(tableRowOrColumn);

        Element rowOrColumnName = shared.getChild("rowOrColumnName");
        if (rowOrColumnName != null) {
            h.setRowOrColumnName(rowOrColumnName.getTextTrim());
        }

        String includeCellsWithoutHeader = "no";
        Attribute attribute = shared.getAttribute("includeCellsWithoutHeader");
        if (attribute != null) {  // NOI18N
            includeCellsWithoutHeader = attribute.getValue();  // NOI18N
        }
        h.setIncludeCellsWithoutHeader("yes".equals(includeCellsWithoutHeader));

        Element namedBeanTypeElement = shared.getChild("namedBeanType");
        NamedBeanType namedBeanType =
                NamedBeanType.valueOf(namedBeanTypeElement.getTextTrim());
        h.setNamedBeanType(namedBeanType);

        Element variableName = shared.getChild("localVariableNamedBean");
        if (variableName != null) {
            h.setLocalVariableNamedBean(variableName.getTextTrim());
        }

        variableName = shared.getChild("localVariableEvent");
        if (variableName != null) {
            h.setLocalVariableEvent(variableName.getTextTrim());
        }

        variableName = shared.getChild("localVariableNewValue");
        if (variableName != null) {
            h.setLocalVariableNewValue(variableName.getTextTrim());
        }

        String listenOnAllProperties = "no";
        attribute = shared.getAttribute("listenOnAllProperties");
        if (attribute != null) {  // NOI18N
            listenOnAllProperties = attribute.getValue();  // NOI18N
        }
        h.setListenOnAllProperties("yes".equals(listenOnAllProperties));

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansTableXml.class);
}

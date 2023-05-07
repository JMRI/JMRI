package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.TableRowOrColumn;
import jmri.jmrit.logixng.actions.ActionFindTableRowOrColumn;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionFindTableRowOrColumn objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ActionFindTableRowOrColumnXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionFindTableRowOrColumnXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionFindTableRowOrColumn p = (ActionFindTableRowOrColumn) o;

        Element element = new Element("ActionFindTableRowOrColumn");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<NamedTable>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("rowOrColumnName").addContent(p.getRowOrColumnName()));
        element.addContent(new Element("tableRowOrColumn").addContent(p.getTableRowOrColumn().name()));

        element.setAttribute("includeCellsWithoutHeader",
                p.getIncludeCellsWithoutHeader() ? "yes" : "no");  // NOI18N

        element.addContent(new Element("localVariableNamedBean").addContent(p.getLocalVariableNamedBean()));
        element.addContent(new Element("localVariableRow").addContent(p.getLocalVariableRow()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionFindTableRowOrColumn h = new ActionFindTableRowOrColumn(sys, uname);

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

        Element variableName = shared.getChild("localVariableNamedBean");
        if (variableName != null) {
            h.setLocalVariableNamedBean(variableName.getTextTrim());
        }

        variableName = shared.getChild("localVariableRow");
        if (variableName != null) {
            h.setLocalVariableRow(variableName.getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionFindTableRowOrColumnXml.class);
}

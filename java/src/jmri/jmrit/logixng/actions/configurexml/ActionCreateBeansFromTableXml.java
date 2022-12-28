package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.TableRowOrColumn;
import jmri.jmrit.logixng.actions.ActionCreateBeansFromTable;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionCreateBeansFromTable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ActionCreateBeansFromTableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionCreateBeansFromTableXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionCreateBeansFromTable p = (ActionCreateBeansFromTable) o;

        Element element = new Element("ActionCreateBeansFromTable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<NamedTable>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("rowOrColumnSystemName").addContent(p.getRowOrColumnSystemName()));

        if (p.getRowOrColumnUserName() != null && !p.getRowOrColumnUserName().isBlank()) {
            element.addContent(new Element("rowOrColumnUserName").addContent(p.getRowOrColumnUserName()));
        }
        element.addContent(new Element("tableRowOrColumn").addContent(p.getTableRowOrColumn().name()));

        element.setAttribute("onlyCreatableTypes",
                p.isOnlyCreatableTypes()? "yes" : "no");  // NOI18N

        element.setAttribute("includeCellsWithoutHeader",
                p.isIncludeCellsWithoutHeader() ? "yes" : "no");  // NOI18N

        element.setAttribute("moveUserName",
                p.isMoveUserName()? "yes" : "no");  // NOI18N

        element.setAttribute("updateToUserName",
                p.isUpdateToUserName()? "yes" : "no");  // NOI18N

        element.setAttribute("removeOldBean",
                p.isRemoveOldBean()? "yes" : "no");  // NOI18N

        element.addContent(new Element("namedBeanType").addContent(p.getNamedBeanType().name()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionCreateBeansFromTable h = new ActionCreateBeansFromTable(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<NamedTable>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "table", null, null, null, null);

        Element tableRowOrColumnElement = shared.getChild("tableRowOrColumn");
        TableRowOrColumn tableRowOrColumn =
                TableRowOrColumn.valueOf(tableRowOrColumnElement.getTextTrim());
        h.setTableRowOrColumn(tableRowOrColumn);

        Element rowOrColumnSystemName = shared.getChild("rowOrColumnSystemName");
        if (rowOrColumnSystemName != null) {
            h.setRowOrColumnSystemName(rowOrColumnSystemName.getTextTrim());
        }

        Element rowOrColumnUserName = shared.getChild("rowOrColumnUserName");
        if (rowOrColumnUserName != null) {
            h.setRowOrColumnUserName(rowOrColumnUserName.getTextTrim());
        }

        String onlyCreatableTypes = "yes";
        Attribute attribute = shared.getAttribute("onlyCreatableTypes");
        if (attribute != null) {  // NOI18N
            onlyCreatableTypes = attribute.getValue();  // NOI18N
        }
        h.setOnlyCreatableTypes("yes".equals(onlyCreatableTypes));

        String includeCellsWithoutHeader = "no";
        attribute = shared.getAttribute("includeCellsWithoutHeader");
        if (attribute != null) {  // NOI18N
            includeCellsWithoutHeader = attribute.getValue();  // NOI18N
        }
        h.setIncludeCellsWithoutHeader("yes".equals(includeCellsWithoutHeader));

        String moveUserName = "no";
        attribute = shared.getAttribute("moveUserName");
        if (attribute != null) {  // NOI18N
            moveUserName = attribute.getValue();  // NOI18N
        }
        h.setMoveUserName("yes".equals(moveUserName));

        String updateToUserName = "no";
        attribute = shared.getAttribute("updateToUserName");
        if (attribute != null) {  // NOI18N
            updateToUserName = attribute.getValue();  // NOI18N
        }
        h.setUpdateToUserName("yes".equals(updateToUserName));

        String removeOldBean = "no";
        attribute = shared.getAttribute("removeOldBean");
        if (attribute != null) {  // NOI18N
            removeOldBean = attribute.getValue();  // NOI18N
        }
        h.setRemoveOldBean("yes".equals(removeOldBean));

        Element namedBeanTypeElement = shared.getChild("namedBeanType");
        NamedBeanType namedBeanType =
                NamedBeanType.valueOf(namedBeanTypeElement.getTextTrim());
        h.setNamedBeanType(namedBeanType);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionCreateBeansFromTableXml.class);
}

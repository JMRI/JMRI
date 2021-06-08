package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionListenOnBeansTable;
import jmri.jmrit.logixng.actions.NamedBeanType;

import org.jdom2.Attribute;
import org.jdom2.Element;

import jmri.jmrit.logixng.TableRowOrColumn;

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

        if (p.getTable() != null) {
            element.addContent(new Element("table").addContent(p.getTable().getName()));
        }
        element.addContent(new Element("rowOrColumnName").addContent(p.getRowOrColumnName()));
        element.addContent(new Element("tableRowOrColumn").addContent(p.getTableRowOrColumn().name()));
        
        element.setAttribute("includeCellsWithoutHeader",
                p.getIncludeCellsWithoutHeader() ? "yes" : "no");  // NOI18N
        
        element.addContent(new Element("namedBeanType").addContent(p.getNamedBeanType().name()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionListenOnBeansTable h = new ActionListenOnBeansTable(sys, uname);
        
        loadCommon(h, shared);
        
        Element tableRowOrColumnElement = shared.getChild("tableRowOrColumn");
        TableRowOrColumn tableRowOrColumn =
                TableRowOrColumn.valueOf(tableRowOrColumnElement.getTextTrim());
        h.setTableRowOrColumn(tableRowOrColumn);
        
        Element tableName = shared.getChild("table");
        if (tableName != null) {
            h.setTable(tableName.getTextTrim());
        }
        
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
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansTableXml.class);
}

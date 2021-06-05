package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionListenOnBeansTable;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.jmrit.logixng.util.parser.ParserException;

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
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionListenOnBeansTable p = (ActionListenOnBeansTable) o;

        Element element = new Element("ActionListenOnBeansTable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        element.addContent(new Element("namedBeanType").addContent(p.getNamedBeanType().name()));
        
        NamedBeanHandle<NamedTable> table = p.getTable();
        if (table != null) {
            element.addContent(new Element("table").addContent(table.getName()));
        }
        
        element.addContent(new Element("tableRowOrColumn").addContent(p.getTableRowOrColumn().name()));
        
        element.addContent(new Element("rowOrColumnName").addContent(p.getRowOrColumnName()));
        
        
        
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));
        
        element.addContent(new Element("stateAddressing").addContent(p.getStateAddressing().name()));
        element.addContent(new Element("sensorState").addContent(p.getBeanState().name()));
        element.addContent(new Element("stateReference").addContent(p.getStateReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getStateLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getStateFormula()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionListenOnBeansTable h = new ActionListenOnBeansTable(sys, uname);

        loadCommon(h, shared);

        Element tableName = shared.getChild("table");
        if (tableName != null) {
            NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(tableName.getTextTrim());
            if (t != null) h.setTable(t);
            else h.removeTable();
        }

        try {
            Element elem = shared.getChild("namedBeanType");
            if (elem != null) {
                h.setNamedBeanType(NamedBeanType.valueOf(elem.getTextTrim()));
            }
            
            elem = shared.getChild("tableRowOrColumn");
            if (elem != null) {
                h.setTableRowOrColumn(TableRowOrColumn.valueOf(elem.getTextTrim()));
            }
            
            elem = shared.getChild("rowOrColumnName");
            if (elem != null) h.setRowOrColumnName(elem.getTextTrim());
            
            
            
            
            
            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setStateAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element sensorState = shared.getChild("sensorState");
            if (sensorState != null) {
                h.setBeanState(ActionListenOnBeansTable.SensorState.valueOf(sensorState.getTextTrim()));
            }
            
            elem = shared.getChild("stateReference");
            if (elem != null) h.setStateReference(elem.getTextTrim());
            
            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setStateLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("stateFormula");
            if (elem != null) h.setStateFormula(elem.getTextTrim());
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansTableXml.class);
}

package jmri.jmrit.logixng.actions.configurexml;

import org.jdom2.Element;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.TableRowOrColumn;
import jmri.jmrit.logixng.actions.TableForEach;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Handle XML configuration for TableForEach objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class TableForEachXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TableForEachXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        TableForEach p = (TableForEach) o;

        Element element = new Element("TableForEach");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.addContent(new Element("localVariable").addContent(p.getLocalVariableName()));
        
        if (p.getTable() != null) {
            element.addContent(new Element("table").addContent(p.getTable().getName()));
        }

        element.addContent(new Element("tableAddressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("tableReference").addContent(p.getTableReference()));
        element.addContent(new Element("tableLocalVariable").addContent(p.getTableLocalVariable()));
        element.addContent(new Element("tableFormula").addContent(p.getTableFormula()));

        element.addContent(new Element("rowOrColumnAddressing").addContent(p.getRowOrColumnAddressing().name()));
        element.addContent(new Element("rowOrColumnName").addContent(p.getRowOrColumnName()));
        element.addContent(new Element("rowOrColumnReference").addContent(p.getRowOrColumnReference()));
        element.addContent(new Element("rowOrColumnLocalVariable").addContent(p.getRowOrColumnLocalVariable()));
        element.addContent(new Element("rowOrColumnFormula").addContent(p.getRowOrColumnFormula()));

        element.addContent(new Element("tableRowOrColumn").addContent(p.getRowOrColumn().name()));
        
        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        TableForEach h = new TableForEach(sys, uname);
        
        loadCommon(h, shared);
        
        Element tableRowOrColumnElement = shared.getChild("tableRowOrColumn");
        TableRowOrColumn tableRowOrColumn =
                TableRowOrColumn.valueOf(tableRowOrColumnElement.getTextTrim());
        h.setRowOrColumn(tableRowOrColumn);
        
        Element socketName = shared.getChild("Socket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("Socket").getChild("systemName");
        if (socketSystemName != null) {
            h.setSocketSystemName(socketSystemName.getTextTrim());
        }
        
        Element tableName = shared.getChild("table");
        if (tableName != null) {
            h.setTable(tableName.getTextTrim());
        }
        
        try {
            Element elem = shared.getChild("tableAddressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("tableReference");
            if (elem != null) h.setTableReference(elem.getTextTrim());

            elem = shared.getChild("tableLocalVariable");
            if (elem != null) h.setTableLocalVariable(elem.getTextTrim());

            elem = shared.getChild("tableFormula");
            if (elem != null) h.setTableFormula(elem.getTextTrim());


            elem = shared.getChild("rowOrColumnAddressing");
            if (elem != null) {
                h.setRowOrColumnAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element rowOrColumnName = shared.getChild("rowOrColumnName");
            if (rowOrColumnName != null) h.setRowOrColumnName(rowOrColumnName.getTextTrim());

            elem = shared.getChild("rowOrColumnReference");
            if (elem != null) h.setRowOrColumnReference(elem.getTextTrim());

            elem = shared.getChild("rowOrColumnLocalVariable");
            if (elem != null) h.setRowOrColumnLocalVariable(elem.getTextTrim());

            elem = shared.getChild("rowOrColumnFormula");
            if (elem != null) h.setRowOrColumnFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        Element rowOrColumnName = shared.getChild("rowOrColumnName");
        if (rowOrColumnName != null) {
            h.setRowOrColumnName(rowOrColumnName.getTextTrim());
        }
        
        Element localVariable = shared.getChild("localVariable");
        if (localVariable != null) {
            h.setLocalVariableName(localVariable.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableForEachXml.class);
}

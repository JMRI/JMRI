package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.TableForEach;

import org.jdom2.Attribute;
import org.jdom2.Element;

import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.TableRowOrColumn;

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
        element.addContent(new Element("rowOrColumnName").addContent(p.getRowOrColumnName()));
        element.addContent(new Element("tableRowOrColumn").addContent(p.getTableRowOrColumn().name()));
        
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
    public boolean load(Element shared, Element perNode) {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        TableForEach h = new TableForEach(sys, uname);
        
        loadCommon(h, shared);
        
        Element tableRowOrColumnElement = shared.getChild("tableRowOrColumn");
        TableRowOrColumn tableRowOrColumn =
                TableRowOrColumn.valueOf(tableRowOrColumnElement.getTextTrim());
        h.setTableRowOrColumn(tableRowOrColumn);
        
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

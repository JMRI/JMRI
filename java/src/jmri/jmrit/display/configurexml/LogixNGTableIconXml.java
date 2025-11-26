package jmri.jmrit.display.configurexml;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.LogixNGTableIcon objects.
 *
 * @author Bob Jacobsen     Copyright: Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class LogixNGTableIconXml extends PositionableLabelXml {

    public LogixNGTableIconXml() {
    }

    /**
     * Default implementation for storing the contents of a LogixNGTableIcon
     *
     * @param o Object to store, of type LogixNGTableIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LogixNGTableIcon p = (LogixNGTableIcon) o;

        Element element = new Element("logixNGTableIcon");

        // include attributes
        element.setAttribute("logixNGTable", p.getTableModel().getTable().getDisplayName());

        Module validateModule = p.getTableModel().getValidateModule();
        if (validateModule != null) {
            element.setAttribute("validateModule", validateModule.getDisplayName());
        }
        element.setAttribute("isEditable", p.getTableModel().isEditable() ? "yes" : "no");
        element.setAttribute("editableColumns", p.getTableModel().getEditableColumns());

        StringBuilder sb = new StringBuilder();
        JTable jTable = p.getJTable();
        TableColumnModel colModel = jTable.getColumnModel();
        for (int col=0; col < colModel.getColumnCount(); col++) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(colModel.getColumn(col).getWidth());
        }
        element.setAttribute("columnWidths", sb.toString());

        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        storeLogixNG_Data(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LogixNGTableIconXml");
        return element;
    }

    /**
     * Load, starting with the logixNGTableIcon element, then all the value-icon
     * pairs
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        Editor p = (Editor) o;

        String name;
        Attribute attr = element.getAttribute("logixNGTable");
        if (attr == null) {
            log.error("incorrect information for a logixNGTable location; must use logixNGTable name");
            p.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }

        NamedTable namedTable = jmri.InstanceManager.getDefault(NamedTableManager.class).getNamedTable(name);

        if (namedTable == null) {
            log.error("LogixNG Table named '{}' not found.", attr.getValue());
            p.loadFailed();
            return;
        }

        LogixNGTableIcon l = new LogixNGTableIcon(name, p);

        attr = element.getAttribute("validateModule");
        if (attr != null) {
            name = attr.getValue();
            Module validateModule = jmri.InstanceManager.getDefault(ModuleManager.class).getModule(name);
            l.getTableModel().setValidateModule(validateModule);
        }

        attr = element.getAttribute("isEditable");
        if (attr != null) {
            String value = attr.getValue();
            l.getTableModel().setEditable("yes".equals(value));
        }

        attr = element.getAttribute("editableColumns");
        if (attr != null) {
            l.getTableModel().setEditableColumns(attr.getValue());
        }

        attr = element.getAttribute("columnWidths");
        if (attr != null) {
            String[] widths = attr.getValue().split(",");
            JTable jTable = l.getJTable();
            TableColumnModel colModel = jTable.getColumnModel();
            for (int col=0; col < colModel.getColumnCount(); col++) {
                if (widths.length >= col+1) {
                    colModel.getColumn(col).setPreferredWidth(Integer.parseInt(widths[col]));
                }
            }
        }

        loadTextInfo(l, element);

        try {
            p.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }

        loadLogixNG_Data(l, element);

        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);

        javax.swing.JComponent textField = l.getTextComponent();
        jmri.jmrit.display.PositionablePopupUtil util = l.getPopupUtility();
        if (util.hasBackground()) {
            textField.setBackground(util.getBackground());
        } else {
            textField.setBackground(null);
            textField.setOpaque(false);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LogixNGTableIconXml.class);
}

package jmri.jmrit.display.configurexml;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.GlobalVariableSpinnerIcon objects.
 *
 * @author Pete Cressman    Copyright (c) 2012
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GlobalVariableComboIconXml extends PositionableLabelXml {

    public GlobalVariableComboIconXml() {
    }

    /**
     * Default implementation for storing the contents of a GlobalVariableSpinnerIcon
     *
     * @param obj Object to store, of type GlobalVariableSpinnerIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object obj) {

        GlobalVariableComboIcon globalVariableIcon = (GlobalVariableComboIcon) obj;

        Element element = new Element("globalVariableComboIcon");

        Element elem = new Element("itemList");
        DefaultComboBoxModel<String> model = globalVariableIcon.getComboModel();
        for (int i = 0; i < model.getSize(); i++) {
            Element e = new Element("item");
            e.setAttribute("index", "" + i);
            e.addContent(model.getElementAt(i));
            elem.addContent(e);
        }
        element.addContent(elem);

        // include attributes
        element.setAttribute("globalVariable", globalVariableIcon.getNamedGlobalVariable().getName());
        storeCommonAttributes(globalVariableIcon, element);
        storeTextInfo(globalVariableIcon, element);

        storeLogixNG_Data(globalVariableIcon, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.GlobalVariableComboIconXml");
        return element;
    }

    /**
     * Load, starting with the globalVariableComboIcon element, then all the value-icon
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

        Element elem = element.getChild("itemList");
        List<Element> list = elem.getChildren("item");
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            String item = e.getText();
            try {
                int idx = e.getAttribute("index").getIntValue();
                items[idx] = item;
            } catch ( org.jdom2.DataConversionException ex) {
                log.error("failed to convert ComboBoxIcon index attribute");
                if (items[i]==null) {
                    items[i] = item;
                }
            }
        }

        GlobalVariableComboIcon l = new GlobalVariableComboIcon(p, items);

        loadTextInfo(l, element);
        String name;
        Attribute attr = element.getAttribute("globalVariable");
        if (attr == null) {
            log.error("incorrect information for a globalVariable location; must use globalVariable name");
            p.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }

        GlobalVariable m = jmri.InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable(name);

        if (m != null) {
            l.setGlobalVariable(name);
        } else {
            log.error("GlobalVariable named '{}' not found.", attr.getValue());
            p.loadFailed();
            return;
        }

        try {
            p.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }

        loadLogixNG_Data(l, element);

        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);
    }

    private final static Logger log = LoggerFactory.getLogger(GlobalVariableComboIconXml.class);
}

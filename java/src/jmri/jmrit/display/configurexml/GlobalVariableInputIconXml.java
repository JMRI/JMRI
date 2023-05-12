package jmri.jmrit.display.configurexml;

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
 * @author Bob Jacobsen     Copyright: Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GlobalVariableInputIconXml extends PositionableLabelXml {

    public GlobalVariableInputIconXml() {
    }

    /**
     * Default implementation for storing the contents of a GlobalVariableSpinnerIcon
     *
     * @param o Object to store, of type GlobalVariableSpinnerIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        GlobalVariableInputIcon p = (GlobalVariableInputIcon) o;

        Element element = new Element("globalVariableInputIcon");

        // include attributes
        element.setAttribute("colWidth", "" + p.getNumColumns());
        element.setAttribute("globalVariable", p.getNamedGlobalVariable().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        storeLogixNG_Data(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.GlobalVariableInputIconXml");
        return element;
    }

    /**
     * Load, starting with the globalVariableInputIcon element, then all the value-icon
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

        int nCol = 2;
        try {
            nCol = element.getAttribute("colWidth").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert colWidth attribute");
        }

        GlobalVariableInputIcon l = new GlobalVariableInputIcon(nCol, p);

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

        javax.swing.JComponent textField = l.getTextComponent();
        jmri.jmrit.display.PositionablePopupUtil util = l.getPopupUtility();
        if (util.hasBackground()) {
            textField.setBackground(util.getBackground());
        } else {
            textField.setBackground(null);
            textField.setOpaque(false);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(GlobalVariableInputIconXml.class);
}

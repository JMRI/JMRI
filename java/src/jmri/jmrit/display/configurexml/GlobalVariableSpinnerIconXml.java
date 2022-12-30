package jmri.jmrit.display.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;

import org.jdom2.Element;

/**
 * Handle configuration for display.GlobalVariableSpinnerIcon objects.
 *
 * @author Bob Jacobsen     Copyright: Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GlobalVariableSpinnerIconXml extends PositionableLabelXml {

    public GlobalVariableSpinnerIconXml() {
    }

    /**
     * Default implementation for storing the contents of a GlobalVariableSpinnerIcon
     *
     * @param o Object to store, of type GlobalVariableSpinnerIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        GlobalVariableSpinnerIcon p = (GlobalVariableSpinnerIcon) o;

        Element element = new Element("globalVariableIcon");

        // include attributes
        element.setAttribute("globalVariable", p.getNamedGlobalVariable().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        storeLogixNG_Data(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.GlobalVariableSpinnerIconXml");
        return element;
    }

    /**
     * Load, starting with the globalVariableIcon element, then all the value-icon pairs
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        Editor p = (Editor) o;
        GlobalVariableSpinnerIcon l = new GlobalVariableSpinnerIcon(p);

        l.setGlobalVariable(element.getAttribute("globalVariable").getValue());

        loadTextInfo(l, element);
        try {
            p.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }

        loadLogixNG_Data(l, element);

        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);
    }
}

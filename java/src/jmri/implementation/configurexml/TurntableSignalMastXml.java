package jmri.jmrit.implementation.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import org.jdom2.Element;

/**
 * Handle XML configuration for TurntableSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @author George Warner
 */
public class TurntableSignalMastXml extends AbstractXmlAdapter {

    public TurntableSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurntableSignalMast
     *
     * @param o Object to store, of type TurntableSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        // TurntableSignalMasts are stored as part of the LayoutTurntable.
        // We must return a non-null element here to prevent the XmlAdapter mechanism
        // from falling back to a superclass adapter (like VirtualSignalMastXml) which
        // would incorrectly save properties and cause loading errors.
        Element e = new Element("signalmast");
        e.setAttribute("class", this.getClass().getName());
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // TurntableSignalMasts are loaded as part of the LayoutTurntable, so we don't need to do anything here.
        return true;
    }
}
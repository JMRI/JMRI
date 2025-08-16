package jmri.jmrit.display.layoutEditor.configurexml;

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
        // TurntableSignalMasts are stored as part of the LayoutTurntable, so we don't need to do anything here.
        // The class needs to exist to prevent a ClassNotFoundException, but it doesn't need to store anything.
        return null;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // TurntableSignalMasts are loaded as part of the LayoutTurntable, so we don't need to do anything here.
        return true;
    }
}
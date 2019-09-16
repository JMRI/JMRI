package jmri.managers.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistance of ProxyTurnoutManager
 * <p>
 * This class is named as being the persistant form of the ProxyTurnoutManager
 * class, but there's no object of that form created or used.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class ProxyTurnoutManagerXml extends AbstractXmlAdapter {

    public ProxyTurnoutManagerXml() {
    }

    /**
     * Default implementation for storing the static contents of a
     * PositionableLabel
     *
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        return null;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    /**
     * Doesn't need to do anything, shouldn't get invoked
     *
     * @param element Top level Element to unpack.
     * @param o       PanelEditor as an Object
     */
    @Override
    public void load(Element element, Object o) {
    }

}

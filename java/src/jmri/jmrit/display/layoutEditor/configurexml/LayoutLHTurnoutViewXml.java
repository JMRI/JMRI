package jmri.jmrit.display.layoutEditor.configurexml;

import org.jdom2.Element;

/**
 * This module handles configuration for display.LayoutTurnoutView objects for a
 * LayoutEditor.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutLHTurnoutViewXml extends LayoutTurnoutViewXml {
    
    public LayoutLHTurnoutViewXml() {
    }

    @Override
    protected void addClass(Element element) {
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutLHTurnoutXml");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHTurnoutViewXml.class);
}

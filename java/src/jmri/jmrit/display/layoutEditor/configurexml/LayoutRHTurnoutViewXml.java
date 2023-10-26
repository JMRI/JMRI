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
public class LayoutRHTurnoutViewXml extends LayoutTurnoutViewXml {

    public LayoutRHTurnoutViewXml() {
    }

    @Override
    protected void addClass(Element element) {
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutRHTurnoutXml");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHTurnoutViewXml.class);
}

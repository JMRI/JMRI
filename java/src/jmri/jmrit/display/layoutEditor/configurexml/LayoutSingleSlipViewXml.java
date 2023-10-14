package jmri.jmrit.display.layoutEditor.configurexml;

import org.jdom2.Element;

/**
 * This module handles configuration for display.LayoutSlipView objects for a
 * LayoutEditor.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutSingleSlipViewXml extends LayoutSlipViewXml {

    public LayoutSingleSlipViewXml() {
    }
    
    @Override
    protected void addClass(Element element) {
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutSingleSlipXml");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipViewXml.class);
}

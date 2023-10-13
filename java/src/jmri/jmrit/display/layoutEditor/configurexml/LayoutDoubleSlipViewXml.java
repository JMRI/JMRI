package jmri.jmrit.display.layoutEditor.configurexml;

import org.jdom2.Element;

/**
 * This module handles configuration for display.LayoutSlipView objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutDoubleSlipViewXml extends LayoutSlipViewXml {

    public LayoutDoubleSlipViewXml() {
    }

    @Override
    protected void addClass(Element element) {
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutDoubleSlipXml");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleSlipViewXml.class);
}

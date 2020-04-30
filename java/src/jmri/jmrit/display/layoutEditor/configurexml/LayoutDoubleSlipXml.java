package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutSingleSlip;
import jmri.jmrit.display.layoutEditor.LayoutDoubleSlip;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutSlip objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutDoubleSlipXml extends LayoutSlipXml {

    public LayoutDoubleSlipXml() {
    }

    // private final static Logger log = LoggerFactory.getLogger(LayoutDoubleSlipXml.class);
}

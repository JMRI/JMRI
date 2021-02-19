package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;

import jmri.Turnout;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutTurnoutView objects for a
 * LayoutEditor.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutDoubleXOverViewXml extends LayoutXOverViewXml {

    static final EnumIO<LayoutTurnout.LinkType> linkEnumMap = new EnumIoOrdinals<>(LayoutTurnout.LinkType.class);
    static final EnumIO<LayoutTurnout.TurnoutType> tTypeEnumMap = new EnumIoOrdinals<>(LayoutTurnout.TurnoutType.class);
    
    public LayoutDoubleXOverViewXml() {
    }

    @Override
    protected void addClass(Element element) {
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutDoubleXOverXml");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleXOverViewXml.class);
}

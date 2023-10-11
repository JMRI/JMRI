package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import org.jdom2.Element;

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

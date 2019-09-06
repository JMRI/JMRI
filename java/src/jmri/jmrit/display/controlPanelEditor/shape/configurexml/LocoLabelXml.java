package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.shape.LocoLabel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import org.jdom2.Element;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class LocoLabelXml extends PositionableRectangleXml {

    public LocoLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        return null;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
    }
}

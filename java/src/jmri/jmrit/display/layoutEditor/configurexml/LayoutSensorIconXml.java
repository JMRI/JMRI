package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.configurexml.SensorIconXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy class, just present so files that refer to this class (e.g. pre JMRI
 * 2.7.8 files) can still be read by deferring to the present class.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author Kevin Dickerson, Deprecated
 * @deprecated 2.7.8  - left so old files can be read
 */
@Deprecated
public class LayoutSensorIconXml extends AbstractXmlAdapter {

    public LayoutSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutSensorIcon
     *
     * @param o Object to store, of type LayoutSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SensorIconXml tmp = new SensorIconXml();
        return tmp.store(o);
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        SensorIconXml tmp = new SensorIconXml();
        tmp.load(element, o);

    }

    private final static Logger log = LoggerFactory.getLogger(LayoutSensorIconXml.class.getName());

}

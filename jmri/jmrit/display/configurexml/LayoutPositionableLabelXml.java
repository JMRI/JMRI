package jmri.jmrit.display.configurexml;

import org.jdom.Element;
import jmri.configurexml.XmlAdapter;
//import java.awt.Color;
//import org.jdom.DataConversionException;
//import jmri.jmrit.display.PositionableLabel;
//import org.jdom.Attribute;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.7.8 files) can still be read by
 * deferring to the present class.
 *
 * @author David Duchamp Copyright: Copyright (c) 2009
 * @author Kevin Dickerson, Deprecated
 * @version $Revision: 1.16 $
 * @deprecated 2.7.8
 */
@Deprecated

public class LayoutPositionableLabelXml implements XmlAdapter {

    public LayoutPositionableLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutPositionableLabel
     * @param o Object to store, of type LayoutPositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PositionableLabelXml tmp = new PositionableLabelXml();
        return tmp.store(o);
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a LayoutPositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PositionableLabelXml tmp = new PositionableLabelXml();
        tmp.load(element, o);
	}
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutPositionableLabelXml.class.getName());

}
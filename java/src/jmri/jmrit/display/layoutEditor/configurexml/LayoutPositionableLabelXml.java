package jmri.jmrit.display.layoutEditor.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;
import jmri.configurexml.*;
import jmri.jmrit.display.configurexml.*;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.7.8 files) can still be read by
 * deferring to the present class.
 *
 * @author David Duchamp Copyright: Copyright (c) 2009
 * @author Kevin Dickerson, Deprecated
 * @version $Revision$
 * @deprecated 2.7.8
 */

@Deprecated
public class LayoutPositionableLabelXml extends AbstractXmlAdapter {

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
    
    static Logger log = LoggerFactory.getLogger(LayoutPositionableLabelXml.class.getName());

}

// LayoutSignalHeadIconXml.java

package jmri.jmrit.display.layoutEditor.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.configurexml.*;
import jmri.jmrit.display.configurexml.*;

import org.jdom.Element;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.7.8 files) can still be read by
 * deferring to the present class.
 *
 * Handle configuration for display.LayoutSignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @author Kevin Dickerson, Deprecated
 * @version $Revision$
 * @deprecated 2.7.8
 */
  @Deprecated
public class LayoutSignalHeadIconXml extends AbstractXmlAdapter {

    public LayoutSignalHeadIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutSignalHeadIcon
     * @param o Object to store, of type LayoutSignalHeadIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SignalHeadIconXml tmp = new SignalHeadIconXml();
        return tmp.store(o);

    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
    
        SignalHeadIconXml tmp = new SignalHeadIconXml();
        tmp.load(element, o);
        // create the objects
    }

    static Logger log = LoggerFactory.getLogger(LayoutSignalHeadIconXml.class.getName());

}

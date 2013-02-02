package jmri.jmrit.display.layoutEditor.configurexml;

import org.apache.log4j.Logger;
import jmri.configurexml.*;

import org.jdom.Element;


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
public class LayoutMemoryIconXml extends AbstractXmlAdapter {

    public LayoutMemoryIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutMemoryIcon
     * @param o Object to store, of type LayoutMemoryIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        MemoryIconXml tmp = new MemoryIconXml();
        return tmp.store(o);
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the memoryicon element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
	public void load(Element element, Object o) {
        MemoryIconXml tmp = new MemoryIconXml();
        tmp.load(element, o);
    }

    static Logger log = Logger.getLogger(LayoutMemoryIconXml.class.getName());
}

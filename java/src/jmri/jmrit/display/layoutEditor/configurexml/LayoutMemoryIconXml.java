package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy class, just present so files that refer to this class (e.g. pre JMRI
 * 2.7.8 files) can still be read by deferring to the present class.
 *
 * @author David Duchamp Copyright: Copyright (c) 2009
 * @author Kevin Dickerson, Deprecated
 * @deprecated 2.7.8 - left so old files can be read
 */
@Deprecated
public class LayoutMemoryIconXml extends AbstractXmlAdapter {

    public LayoutMemoryIconXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutMemoryIcon
     *
     * @param o Object to store, of type LayoutMemoryIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        MemoryIconXml tmp = new MemoryIconXml();
        return tmp.store(o);
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the memoryicon element, then all the value-icon pairs
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        MemoryIconXml tmp = new MemoryIconXml();
        tmp.load(element, o);
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutMemoryIconXml.class.getName());
}

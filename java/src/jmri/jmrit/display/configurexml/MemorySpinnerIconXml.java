package jmri.jmrit.display.configurexml;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemorySpinnerIcon;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.MemorySpinnerIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class MemorySpinnerIconXml extends PositionableLabelXml {

    public MemorySpinnerIconXml() {
    }

    /**
     * Default implementation for storing the contents of a MemorySpinnerIcon
     *
     * @param o Object to store, of type MemorySpinnerIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MemorySpinnerIcon p = (MemorySpinnerIcon) o;

        Element element = new Element("memoryicon");

        // include attributes
        element.setAttribute("memory", p.getNamedMemory().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemorySpinnerIconXml");
        return element;
    }

    /**
     * Load, starting with the memoryicon element, then all the value-icon pairs
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor) o;
        MemorySpinnerIcon l = new MemorySpinnerIcon(p);

        l.setMemory(element.getAttribute("memory").getValue());

        loadTextInfo(l, element);
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);
    }

    static Logger log = LoggerFactory.getLogger(MemorySpinnerIconXml.class.getName());
}

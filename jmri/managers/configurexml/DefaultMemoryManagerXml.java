package jmri.managers.configurexml;

import jmri.managers.DefaultMemoryManager;

import org.jdom.Element;

/**
 * Persistency implementation for the default MemoryManager persistance.
 * <P>The state of memory objects is not persisted, just their existance.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class DefaultMemoryManagerXml extends AbstractMemoryManagerConfigXML {

    public DefaultMemoryManagerXml() {
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param memories The top-level element being created
     */
    public void setStoreElementClass(Element memories) {
        memories.setAttribute("class","jmri.managers.configurexml.DefaultMemoryManagerXml");
    }

    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param memories Top level Element to unpack.
     */
    public void load(Element memories) {
        // create the master object
        DefaultMemoryManager.instance();
        // load individual routes
        loadMemories(memories);

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultMemoryManagerXml.class.getName());
}
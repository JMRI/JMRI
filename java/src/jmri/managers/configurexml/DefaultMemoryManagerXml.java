package jmri.managers.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;

import org.jdom.Element;

/**
 * Persistency implementation for the default MemoryManager persistance.
 * <P>The state of memory objects is not persisted, just their existance.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
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
        memories.setAttribute("class",this.getClass().getName());
    }

    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param memories Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element memories) {
        // ensure the master object exists
        InstanceManager.memoryManagerInstance();
        // load individual routes
        loadMemories(memories);
        return true;
    }

    static Logger log = LoggerFactory.getLogger(DefaultMemoryManagerXml.class.getName());
}

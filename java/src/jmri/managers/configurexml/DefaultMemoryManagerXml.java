package jmri.managers.configurexml;

import jmri.InstanceManager;
import org.jdom2.Element;

/**
 * Persistency implementation for the default MemoryManager persistence.
 * <p>
 * The state of memory objects is not persisted, just their existence.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class DefaultMemoryManagerXml extends AbstractMemoryManagerConfigXML {

    public DefaultMemoryManagerXml() {
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param memories The top-level element being created
     */
    @Override
    public void setStoreElementClass(Element memories) {
        memories.setAttribute("class", this.getClass().getName());
    }

    /**
     * Create a MemoryManager object of the correct class, then register and
     * fill it.
     *
     * @param sharedMemories Top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedMemories, Element perNodeMemories) {
        // ensure the master object exists
        InstanceManager.memoryManagerInstance();
        // load individual routes
        loadMemories(sharedMemories);
        return true;
    }

}

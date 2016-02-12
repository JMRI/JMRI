package jmri.managers.configurexml;

import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistency implementation for the default MemoryManager persistance.
 * <P>
 * The state of memory objects is not persisted, just their existance.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
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
    public boolean load(Element sharedMemories, Element perNodeMemories) {
        // ensure the master object exists
        InstanceManager.memoryManagerInstance();
        // load individual routes
        loadMemories(sharedMemories);
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultMemoryManagerXml.class.getName());
}

package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * MemoryManagers, working with AbstractMemoryManagers.
 * <p>
 * Also serves as base class for {@link jmri.configurexml.BlockManagerXml} persistence.
 * <p>
 * Typically, a subclass will just implement the load(Element memories) class,
 * relying on implementation here to load the individual Memory objects. Note
 * that these are stored explicitly, so the resolution mechanism doesn't need to
 * see *Xml classes for each specific Memory or AbstractMemory subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 */
public abstract class AbstractMemoryManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractMemoryManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a MemoryManager.
     *
     * @param o Object to store, of type MemoryManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element memories = new Element("memories");
        setStoreElementClass(memories);
        MemoryManager mm = (MemoryManager) o;
        if (mm != null) {
            SortedSet<Memory> memList = mm.getNamedBeanSet();
            // don't return an element if there are no memories to include
            if (memList.isEmpty()) {
                return null;
            }
            // store the memories
            for (Memory m : memList) {
                String mName = m.getSystemName();
                log.debug("system name is {}", mName);

                Element elem = new Element("memory");
                elem.addContent(new Element("systemName").addContent(mName));

                // store common part
                storeCommon(m, elem);

                // store value if non-null; null values omitted
                Object obj = m.getValue();
                if (obj != null) {
                    if (obj instanceof RosterEntry) {
                        String valueClass = obj.getClass().getName();
                        String value = ((RosterEntry) obj).getId();
                        elem.setAttribute("value", value);
                        elem.setAttribute("valueClass", valueClass);
                    } else {
                        String value = obj.toString();
                        elem.setAttribute("value", value);
                    }
                }

                log.debug("store Memory {}", mName);
                memories.addContent(elem);
            }
        }
        return memories;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param memories The top-level element being created
     */
    abstract public void setStoreElementClass(Element memories);

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a MemoryManager object of the correct class, then register and
     * fill it.
     *
     * @param sharedMemories  Shared top level Element to unpack.
     * @param perNodeMemories Per-node top level Element to unpack.
     * @return true if successful
     * @throws jmri.configurexml.JmriConfigureXmlException if error during load.
     */
    @Override
    abstract public boolean load(Element sharedMemories, Element perNodeMemories) throws JmriConfigureXmlException;

    /**
     * Utility method to load the individual Memory objects. If there's no
     * additional info needed for a specific Memory type, invoke this with the
     * parent of the set of Memory elements.
     *
     * @param memories Element containing the Memory elements to load.
     */
    public void loadMemories(Element memories) {
        List<Element> memoryList = memories.getChildren("memory");
        log.debug("Found {} Memory objects", memoryList.size());
        MemoryManager mm = InstanceManager.memoryManagerInstance();

        for (Element el : memoryList) {
            String sysName = getSystemName(el);
            if (sysName == null) {
                log.warn("unexpected null in systemName {}", (el));
                break;
            }

            String userName = getUserName(el);

            checkNameNormalization(sysName, userName, mm);

            log.debug("create Memory: ({})({})", sysName, (userName == null ? "<null>" : userName));
            Memory m = mm.newMemory(sysName, userName);
            if (el.getAttribute("value") != null) {
                loadValue(el, m);
            }
            // load common parts
            loadCommon(m, el);
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.memoryManagerInstance().getXMLOrder();
    }

    private void loadValue(Element memory, Memory m) {
        String value = memory.getAttribute("value").getValue();
        if (memory.getAttribute("valueClass") != null) {
            String adapter = memory.getAttribute("valueClass").getValue();
            if (adapter.equals("jmri.jmrit.roster.RosterEntry")) {
                RosterEntry re = jmri.jmrit.roster.Roster.getDefault().getEntryForId(value);
                m.setValue(re);
                return;
            }
        }
        m.setValue(value);
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMemoryManagerConfigXML.class);

}

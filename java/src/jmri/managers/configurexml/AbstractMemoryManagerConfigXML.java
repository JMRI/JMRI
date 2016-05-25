package jmri.managers.configurexml;

import java.util.List;
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
 * <P>
 * Typically, a subclass will just implement the load(Element memories) class,
 * relying on implementation here to load the individual Memory objects. Note
 * that these are stored explicitly, so the resolution mechanism doesn't need to
 * see *Xml classes for each specific Memory or AbstractMemory subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision$
 */
public abstract class AbstractMemoryManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractMemoryManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a MemoryManager
     *
     * @param o Object to store, of type MemoryManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element memories = new Element("memories");
        setStoreElementClass(memories);
        MemoryManager tm = (MemoryManager) o;
        if (tm != null) {
            java.util.Iterator<String> iter
                    = tm.getSystemNameList().iterator();

            // don't return an element if there are not memories to include
            if (!iter.hasNext()) {
                return null;
            }

            // store the memories
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname == null) {
                    log.error("System name null during store");
                }
                log.debug("system name is " + sname);
                Memory m = tm.getBySystemName(sname);
                Element elem = new Element("memory")
                        .setAttribute("systemName", sname);
                elem.addContent(new Element("systemName").addContent(sname));

                // store common part
                storeCommon(m, elem);
                // store value if non-null; null values omitted
                Object obj = m.getValue();
                if (obj != null) {
                    if (obj instanceof jmri.jmrit.roster.RosterEntry) {
                        String valueClass = obj.getClass().getName();
                        String value = ((RosterEntry) obj).getId();
                        elem.setAttribute("value", value);
                        elem.setAttribute("valueClass", valueClass);
                    } else {
                        String value = obj.toString();
                        elem.setAttribute("value", value);
                    }
                }

                log.debug("store Memory " + sname);
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

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a MemoryManager object of the correct class, then register and
     * fill it.
     *
     * @param sharedMemories Shared top level Element to unpack.
     * @param perNodeMemories Per-node top level Element to unpack.
     * @return true if successful
     * @throws jmri.configurexml.JmriConfigureXmlException
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
    @SuppressWarnings("unchecked")
    public void loadMemories(Element memories) {
        List<Element> memoryList = memories.getChildren("memory");
        if (log.isDebugEnabled()) {
            log.debug("Found " + memoryList.size() + " Memory objects");
        }
        MemoryManager tm = InstanceManager.memoryManagerInstance();

        for (int i = 0; i < memoryList.size(); i++) {

            String sysName = getSystemName(memoryList.get(i));
            if (sysName == null) {
                log.warn("unexpected null in systemName " + (memoryList.get(i)));
                break;
            }

            String userName = getUserName(memoryList.get(i));

            if (log.isDebugEnabled()) {
                log.debug("create Memory: (" + sysName + ")(" + (userName == null ? "<null>" : userName) + ")");
            }
            Memory m = tm.newMemory(sysName, userName);
            if (memoryList.get(i).getAttribute("value") != null) {
                loadValue(memoryList.get(i), m);
            }
            // load common parts
            loadCommon(m, memoryList.get(i));
        }
    }

    public int loadOrder() {
        return InstanceManager.memoryManagerInstance().getXMLOrder();
    }

    void loadValue(Element memory, Memory m) {
        String value = memory.getAttribute("value").getValue();
        if (memory.getAttribute("valueClass") != null) {
            String adapter = memory.getAttribute("valueClass").getValue();
            if (adapter.equals("jmri.jmrit.roster.RosterEntry")) {
                RosterEntry re = jmri.jmrit.roster.Roster.instance().getEntryForId(value);
                m.setValue(re);
                return;
            }
        }
        m.setValue(value);
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMemoryManagerConfigXML.class.getName());
}

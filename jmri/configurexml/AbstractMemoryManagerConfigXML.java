package jmri.configurexml;

import jmri.InstanceManager;
import jmri.MemoryManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring MemoryManagers, working with
 * AbstractMemoryManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element Memorys)
 * class, relying on implementation here to load the individual Memorys.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Memory or AbstractMemory subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public abstract class AbstractMemoryManagerConfigXML implements XmlAdapter {

    public AbstractMemoryManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * MemoryManager
     * @param o Object to store, of type MemoryManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element memorys = new Element("memorys");
        setStoreElementClass(memorys);
        MemoryManager tm = (MemoryManager) o;
        if (tm!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                String uname = tm.getBySystemName(sname).getUserName();
                Element elem = new Element("memory")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                log.debug("store Memory "+sname+":"+uname);
                memorys.addContent(elem);

            }
        }
        return memorys;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param memorys The top-level element being created
     */
    abstract public void setStoreElementClass(Element memorys);

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param memorys Top level Element to unpack.
     */
    abstract public void load(Element memorys);

    /**
     * Utility method to load the individual Memory objects.
     * If there's no additional info needed for a specific Memory type,
     * invoke this with the parent of the set of Memory elements.
     * @param memorys Element containing the Memory elements to load.
     */
    public void loadMemorys(Element memorys) {
        List memoryList = memorys.getChildren("memory");
        if (log.isDebugEnabled()) log.debug("Found "+memoryList.size()+" Memorys");
        MemoryManager tm = InstanceManager.memoryManagerInstance();

        for (int i=0; i<memoryList.size(); i++) {
            if ( ((Element)(memoryList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(memoryList.get(i)))+" "+((Element)(memoryList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(memoryList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(memoryList.get(i))).getAttribute("userName") != null)
            userName = ((Element)(memoryList.get(i))).getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("create Memory: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            tm.newMemory(sysName, userName);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMemoryManagerConfigXML.class.getName());
}
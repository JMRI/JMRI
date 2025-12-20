package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import jmri.InstanceManager;
import jmri.StringIO;
import jmri.StringIOManager;
import org.jdom2.Element;

/**
 * Provides the abstract base and store functionality for configuring
 * StringIOManagers, working with AbstractStringIOManagers.
 * <p>
 * Typically, a subclass will just implement the load(Element StringIOs) class,
 * relying on implementation here to load the individual StringIOs. Note that
 * these are stored explicitly, so the resolution mechanism doesn't need to see
 * *Xml classes for each specific StringIO or AbstractStringIO subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008, 2009, 2024
 */
public abstract class AbstractStringIOManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractStringIOManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a StringIOManager
     *
     * @param o Object to store, of type StringIOManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        log.debug("AbstractStringIOManagerConfigXML store with {}",o);
        Element stringIOs = new Element("stringios");
        setStoreElementClass(stringIOs);
        StringIOManager rm = (StringIOManager) o;
        if (rm != null) {
            SortedSet<StringIO> rList = rm.getNamedBeanSet();
            // don't return an element if there are no StringIOs to include
            if (rList.isEmpty()) {
                return null;
            }
            // store the StringIOs
            for (var r : rList) {
                String rName = r.getSystemName();
                log.debug("system name is {}", rName);
                Element elem = new Element("stringio");
                elem.addContent(new Element("systemName").addContent(rName));
                // store common parts
                storeCommon(r, elem);

                log.debug("store StringIO {}", rName);
                stringIOs.addContent(elem);
            }
        }
        return stringIOs;
    }

    /**
     * Subclass provides an implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param stringIOs The top-level element being created
     */
    abstract public void setStoreElementClass(Element stringIOs);

    /**
     * Utility method to load the individual StringIO objects. If there's no
     * additional info needed for a specific StringIO type, invoke this with the
     * parent of the set of StringIO elements.
     *
     * @param stringIOs Element containing the StringIO elements to load.
     * @return true if successful
     */
    public boolean loadStringIOs(Element stringIOs) {
        boolean result = true;
        List<Element> stringIOList = stringIOs.getChildren("stringio");
        log.debug("Found {} StringIOs", stringIOList.size());
        var tm = InstanceManager.getDefault(jmri.StringIOManager.class);
        tm.setPropertyChangesSilenced("beans", true);

        for (Element e : stringIOList) {
            String sysName = getSystemName(e);
            if (sysName == null) {
                log.warn("unexpected null in systemName {} {}", e, e.getAttributes());
                result = false;
                break;
            }

            String userName = getUserName(e);

            log.debug("create StringIO: ({})({})", sysName, (userName == null ? "<null>" : userName));
            var r = tm.newStringIO(sysName, userName);
            loadCommon(r, e);
        }
        tm.setPropertyChangesSilenced("beans", false);
        return result;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.StringIOManager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractStringIOManagerConfigXML.class);

}

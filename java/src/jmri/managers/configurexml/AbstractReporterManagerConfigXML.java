package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * ReporterManagers, working with AbstractReporterManagers.
 * <p>
 * Typically, a subclass will just implement the load(Element Reporters) class,
 * relying on implementation here to load the individual Reporters. Note that
 * these are stored explicitly, so the resolution mechanism doesn't need to see
 * *Xml classes for each specific Reporter or AbstractReporter subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008, 2009
 */
public abstract class AbstractReporterManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractReporterManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a ReporterManager
     *
     * @param o Object to store, of type ReporterManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element reporters = new Element("reporters");
        setStoreElementClass(reporters);
        ReporterManager rm = (ReporterManager) o;
        if (rm != null) {
            SortedSet<Reporter> rList = rm.getNamedBeanSet();
            // don't return an element if there are no reporters to include
            if (rList.isEmpty()) {
                return null;
            }
            // store the Reporters
            for (Reporter r : rList) {
                String rName = r.getSystemName();
                log.debug("system name is {}", rName);
                Element elem = new Element("reporter");
                elem.addContent(new Element("systemName").addContent(rName));
                // store common parts
                storeCommon(r, elem);

                log.debug("store Reporter {}", rName);
                reporters.addContent(elem);
            }
        }
        return reporters;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param reporters The top-level element being created
     */
    abstract public void setStoreElementClass(Element reporters);

    /**
     * Utility method to load the individual Reporter objects. If there's no
     * additional info needed for a specific Reporter type, invoke this with the
     * parent of the set of Reporter elements.
     *
     * @param reporters Element containing the Reporter elements to load.
     * @return true if successful
     */
    public boolean loadReporters(Element reporters) {
        boolean result = true;
        List<Element> reporterList = reporters.getChildren("reporter");
        log.debug("Found {} reporters", reporterList.size());
        ReporterManager tm = InstanceManager.getDefault(jmri.ReporterManager.class);
        tm.setDataListenerMute(true);

        for (Element e : reporterList) {
            String sysName = getSystemName(e);
            if (sysName == null) {
                log.warn("unexpected null in systemName {} {}", e, e.getAttributes());
                result = false;
                break;
            }

            String userName = getUserName(e);

            log.debug("create Reporter: ({})({})", sysName, (userName == null ? "<null>" : userName));
            Reporter r = tm.newReporter(sysName, userName);
            loadCommon(r, e);
        }
        tm.setDataListenerMute(false);
        return result;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.ReporterManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractReporterManagerConfigXML.class);

}

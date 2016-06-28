package jmri.managers.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * ReporterManagers, working with AbstractReporterManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element Reporters) class,
 * relying on implementation here to load the individual Reporters. Note that
 * these are stored explicitly, so the resolution mechanism doesn't need to see
 * *Xml classes for each specific Reporter or AbstractReporter subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008, 2009
 * @version $Revision$
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
    public Element store(Object o) {
        Element reporters = new Element("reporters");
        setStoreElementClass(reporters);
        ReporterManager tm = (ReporterManager) o;
        if (tm != null) {
            java.util.Iterator<String> iter
                    = tm.getSystemNameList().iterator();

            // don't return an element if there are not reporters to include
            if (!iter.hasNext()) {
                return null;
            }

            // store the reporters
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname == null) {
                    log.error("System name null during store");
                    break;
                }
                log.debug("system name is " + sname);
                Reporter r = tm.getBySystemName(sname);
                Element elem = new Element("reporter")
                        .setAttribute("systemName", sname); // deprecated for 2.9.* series
                elem.addContent(new Element("systemName").addContent(sname));
                // store common parts
                storeCommon(r, elem);

                log.debug("store Reporter " + sname);
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
    @SuppressWarnings("unchecked")
    public boolean loadReporters(Element reporters) {
        boolean result = true;
        List<Element> reporterList = reporters.getChildren("reporter");
        if (log.isDebugEnabled()) {
            log.debug("Found " + reporterList.size() + " reporters");
        }
        ReporterManager tm = InstanceManager.reporterManagerInstance();

        for (int i = 0; i < reporterList.size(); i++) {

            String sysName = getSystemName(reporterList.get(i));
            if (sysName == null) {
                log.warn("unexpected null in systemName " + reporterList.get(i) + " " + reporterList.get(i).getAttributes());
                result = false;
                break;
            }

            String userName = getUserName(reporterList.get(i));

            if (log.isDebugEnabled()) {
                log.debug("create Reporter: (" + sysName + ")(" + (userName == null ? "<null>" : userName) + ")");
            }
            Reporter r = tm.newReporter(sysName, userName);
            loadCommon(r, reporterList.get(i));
        }
        return result;
    }

    public int loadOrder() {
        return InstanceManager.reporterManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractReporterManagerConfigXML.class.getName());
}

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import java.util.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring ReporterManagers, working with
 * AbstractReporterManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element Reporters)
 * class, relying on implementation here to load the individual Reporters.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Reporter or AbstractReporter subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 1.4 $
 */
public abstract class AbstractReporterManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractReporterManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * ReporterManager
     * @param o Object to store, of type ReporterManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element reporters = new Element("reporters");
        setStoreElementClass(reporters);
        ReporterManager tm = (ReporterManager) o;
        if (tm!=null) {
            java.util.Iterator iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not reporters to include
            if (!iter.hasNext()) return null;
            
            // store the reporters
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Reporter r = tm.getBySystemName(sname);
                Element elem = new Element("reporter")
                            .setAttribute("systemName", sname);
                // store common parts
                storeCommon(r, elem);
                
                log.debug("store Reporter "+sname);
                reporters.addContent(elem);

            }
        }
        return reporters;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param reporters The top-level element being created
     */
    abstract public void setStoreElementClass(Element reporters);

    /**
     * Create a ReporterManager object of the correct class, then
     * register and fill it.
     * @param reporters Top level Element to unpack.
     */
    abstract public void load(Element reporters);

    /**
     * Utility method to load the individual Reporter objects.
     * If there's no additional info needed for a specific Reporter type,
     * invoke this with the parent of the set of Reporter elements.
     * @param reporters Element containing the Reporter elements to load.
     */
    public void loadReporters(Element reporters) {
        List reporterList = reporters.getChildren("reporter");
        if (log.isDebugEnabled()) log.debug("Found "+reporterList.size()+" reporters");
        ReporterManager tm = InstanceManager.reporterManagerInstance();

        for (int i=0; i<reporterList.size(); i++) {
            if ( ((Element)(reporterList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(reporterList.get(i)))+" "+((Element)(reporterList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(reporterList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(reporterList.get(i))).getAttribute("userName") != null)
            userName = ((Element)(reporterList.get(i))).getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("create Reporter: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            Reporter r = tm.newReporter(sysName, userName);
            loadCommon(r, ((Element)(reporterList.get(i))));
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractReporterManagerConfigXML.class.getName());
}
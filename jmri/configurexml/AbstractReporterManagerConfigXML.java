package jmri.configurexml;

import jmri.InstanceManager;
import jmri.ReporterManager;
import com.sun.java.util.collections.List;
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
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public abstract class AbstractReporterManagerConfigXML implements XmlAdapter {

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
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                String uname = tm.getBySystemName(sname).getUserName();
                Element elem = new Element("reporter")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                log.debug("store Reporter "+sname+":"+uname);
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
            tm.newReporter(sysName, userName);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractReporterManagerConfigXML.class.getName());
}
// AbstractLightManagerConfigXML.java

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.LightManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring LightManagers, working with
 * AbstractLightManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element sensors)
 * class, relying on implementation here to load the individual sensors.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Light or AbstractLight subclass at store time.
 * <P>
 * Based on AbstractSensorManagerConfigXML.java
 *
 * @author Dave Duchamp Copyright (c) 2004
 * @version $Revision: 1.1 $
 */
public abstract class AbstractLightManagerConfigXML implements XmlAdapter {

    public AbstractLightManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * LightManager
     * @param o Object to store, of type LightManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element lights = new Element("lights");
        setStoreElementClass(lights);
        LightManager tm = (LightManager) o;
        if (tm!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                String uname = tm.getBySystemName(sname).getUserName();
                Element elem = new Element("light")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                log.debug("store light "+sname+":"+uname);
                lights.addContent(elem);

            }
        }
        return lights;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param lights The top-level element being created
     */
    abstract public void setStoreElementClass(Element lights);

    /**
     * Create a LightManager object of the correct class, then
     * register and fill it.
     * @param sensors Top level Element to unpack.
     */
    abstract public void load(Element lights);

    /**
     * Utility method to load the individual Light objects.
     * If there's no additional info needed for a specific light type,
     * invoke this with the parent of the set of Light elements.
     * @param lights Element containing the Light elements to load.
     */
    public void loadLights(Element lights) {
        List lightList = lights.getChildren("light");
        if (log.isDebugEnabled()) log.debug("Found "+lightList.size()+" lights");
        LightManager tm = InstanceManager.lightManagerInstance();

        for (int i=0; i<lightList.size(); i++) {
            if ( ((Element)(lightList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(lightList.get(i)))+" "+((Element)(lightList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(lightList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(lightList.get(i))).getAttribute("userName") != null)
            userName = ((Element)(lightList.get(i))).getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("create light: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            tm.newLight(sysName, userName);
// here add code to initialize other attributes
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractLightManagerConfigXML.class.getName());
}
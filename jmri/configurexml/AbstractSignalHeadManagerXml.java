package jmri.configurexml;

import jmri.AbstractSignalHeadManager;
import jmri.InstanceManager;
import jmri.SignalHeadManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring SignalHeadManagers, working with
 * AbstractSignalHeadManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element turnouts)
 * class, relying on implementation here to load the individual turnouts.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific SignalHead or AbstractSignalHead subclass at store time.
 * <P>
 * Based on AbstractTurnoutManagerConfigXML
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.4 $
 */
public class AbstractSignalHeadManagerXml implements XmlAdapter {

    public AbstractSignalHeadManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalHeadManager
     * @param o Object to store, of type SignalHeadManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element signalheads = new Element("signalheads");
        setStoreElementClass(signalheads);
        SignalHeadManager sm = (SignalHeadManager) o;
        if (sm!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    sm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Object sub = sm.getBySystemName(sname);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e!=null) signalheads.addContent(e);
                } catch (Exception e) {
                    log.error("Error storing signalhead: "+e);
                    e.printStackTrace();
                }
            }
        }
        return signalheads;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param turnouts The top-level element being created
     */
    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.configurexml.AbstractSignalHeadManagerXml");
    }

    /**
     * Create a SignalHeadManager object of the correct class, then
     * register and fill it.
     * @param signalheads Top level Element to unpack.
     */
    public void load(Element signalheads) {
        // create the master object
        AbstractSignalHeadManager mgr = new AbstractSignalHeadManager();
        replaceSignalHeadManager(mgr);

        // load individual turnouts
        loadSignalHeads(signalheads);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }


    /**
     * Utility method to load the individual SignalHead objects.
     * If there's no additional info needed for a specific signal head type,
     * invoke this with the parent of the set of SignalHead elements.
     * @param signalheads Element containing the SignalHead elements to load.
     */
    public void loadSignalHeads(Element signalheads) {
        SignalHeadManager sm = InstanceManager.signalHeadManagerInstance();

        // load the contents
        List items = signalheads.getChildren();
        if (log.isDebugEnabled()) log.debug("Found "+items.size()+" signal heads");
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = (Element)items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via "+adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                // and do it
                adapter.load(item);
            } catch (Exception e) {
                log.error("Exception while loading "+item.getName()+":"+e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Replace the current signal head manager, if there is one, with
     * one newly created during a load operation. This is skipped
     * if they are of the same absolute type.
     * @param pManager
     */
    protected void replaceSignalHeadManager(SignalHeadManager pManager) {
        if (InstanceManager.signalHeadManagerInstance().getClass().getName()
                .equals(pManager.getClass().getName()))
            return;
        // if old manager exists, remove it from configuration process
        if (InstanceManager.signalHeadManagerInstance() != null)
            InstanceManager.configureManagerInstance().deregister(
                InstanceManager.signalHeadManagerInstance() );

        // register new one with InstanceManager
        InstanceManager.setSignalHeadManager(pManager);
        // register new one for configuration
        InstanceManager.configureManagerInstance().registerConfig(pManager);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractSignalHeadManagerXml.class.getName());

}
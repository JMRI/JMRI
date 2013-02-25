package jmri.managers.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.managers.AbstractSignalHeadManager;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.configurexml.XmlAdapter;

import java.util.List;
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
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @version $Revision$
 */
public class AbstractSignalHeadManagerXml extends AbstractNamedBeanManagerConfigXML {

    public AbstractSignalHeadManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalHeadManager.
     * <P>
     * Unlike most other managers, the individual
     * SignalHead objects are stored separately via the
     * configuration system so they can have separate type
     * information.
     * @param o Object to store, of type SignalHeadManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element signalheads = new Element("signalheads");
        setStoreElementClass(signalheads);
        SignalHeadManager sm = (SignalHeadManager) o;
        if (sm!=null) {
            java.util.Iterator<String> iter =
                                    sm.getSystemNameList().iterator();

            // don't return an element if there are not signalheads to include
            if (!iter.hasNext()) return null;
            
            // store the signalheads
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                SignalHead sub = sm.getBySystemName(sname);
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
        turnouts.setAttribute("class",this.getClass().getName());
    }

    /**
     * Create a SignalHeadManager object of the correct class, then
     * register and fill it.
     * @param signalheads Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element signalheads) {
        // create the master object
        replaceSignalHeadManager();

        // load individual turnouts
        loadSignalHeads(signalheads);
        return true;
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
    @SuppressWarnings("unchecked")
	public void loadSignalHeads(Element signalheads) {
        InstanceManager.signalHeadManagerInstance();

        // load the contents
        List<Element> items = signalheads.getChildren();
        if (log.isDebugEnabled()) log.debug("Found "+items.size()+" signal heads");
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
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
     */
    protected void replaceSignalHeadManager() {
        if (InstanceManager.signalHeadManagerInstance().getClass().getName()
                .equals(AbstractSignalHeadManager.class.getName()))
            return;
        // if old manager exists, remove it from configuration process
        if (InstanceManager.signalHeadManagerInstance() != null)
            InstanceManager.configureManagerInstance().deregister(
                InstanceManager.signalHeadManagerInstance() );

        // register new one with InstanceManager
        AbstractSignalHeadManager pManager = new AbstractSignalHeadManager();
        InstanceManager.setSignalHeadManager(pManager);
        // register new one for configuration
        InstanceManager.configureManagerInstance().registerConfig(pManager, jmri.Manager.SIGNALHEADS);
    }
    
    public int loadOrder(){
        return InstanceManager.signalHeadManagerInstance().getXMLOrder();
    }

    static Logger log = LoggerFactory.getLogger(AbstractSignalHeadManagerXml.class.getName());

}

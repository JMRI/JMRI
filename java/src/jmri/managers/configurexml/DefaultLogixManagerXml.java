// DefaultLogixManagerXML.java

package jmri.managers.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.managers.DefaultLogixManager;
import java.util.List;
import org.jdom.Element;

/**
 * Provides the functionality for
 * configuring LogixManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision$
 */
public class DefaultLogixManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultLogixManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LogixManager
     * @param o Object to store, of type LogixManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element logixs = new Element("logixs");
        setStoreElementClass(logixs);
        LogixManager tm = (LogixManager) o;
        if (tm!=null) {
            java.util.Iterator<String> iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not Logix to include
            if (!iter.hasNext()) return null;
            
            // store the Logix
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("logix system name is "+sname);
                Logix x = tm.getBySystemName(sname);
				boolean enabled = x.getEnabled();
                Element elem = new Element("logix")
                            .setAttribute("systemName", sname);
                elem.addContent(new Element("systemName").addContent(sname));
                
                // store common part
                storeCommon(x, elem);
                
				if (enabled) elem.setAttribute("enabled","yes");
				else elem.setAttribute("enabled","no");
				// save child Conditionals
				int numConditionals = x.getNumConditionals();
				if (numConditionals>0) {
					String cSysName = "";
					Element cElem = null;
					for (int k = 0;k<numConditionals;k++) {
						cSysName = x.getConditionalByNumberOrder(k);
						cElem = new Element("logixConditional");
						cElem.setAttribute("systemName",cSysName);
						cElem.setAttribute("order",Integer.toString(k));
						elem.addContent(cElem);
					}
				}
				logixs.addContent(elem);
			}
		}
		return (logixs);	
	}

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param logixs The top-level element being created
     */
    public void setStoreElementClass(Element logixs) {
        logixs.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a LogixManager object of the correct class, then
     * register and fill it.
     * @param logixs Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element logixs) {
        // create the master object
        replaceLogixManager();
        // load individual logixs
        loadLogixs(logixs);
        return true;
    }

    /**
     * Utility method to load the individual Logix objects.
     * If there's no additional info needed for a specific logix type,
     * invoke this with the parent of the set of Logix elements.
     * @param logixs Element containing the Logix elements to load.
     */
    @SuppressWarnings("unchecked")
	public void loadLogixs(Element logixs) {
		List<Element> logixList = logixs.getChildren("logix");
        if (log.isDebugEnabled()) log.debug("Found "+logixList.size()+" logixs");
        LogixManager tm = InstanceManager.logixManagerInstance();

        for (int i=0; i<logixList.size(); i++) {

            String sysName = getSystemName(logixList.get(i));
            if (sysName == null) {
                log.warn("unexpected null in systemName "+logixList.get(i));
                break;
            }

            String userName = null;
			//boolean enabled = true;
			String yesno = "";
            if (logixList.get(i).getAttribute("userName") != null) {
                userName = logixList.get(i).getAttribute("userName").getValue();
			}
            if (logixList.get(i).getAttribute("enabled") != null) {
				yesno = logixList.get(i).getAttribute("enabled").getValue();
			}				
			if (log.isDebugEnabled()) log.debug("create logix: ("+sysName+")("+
										(userName==null?"<null>":userName)+")");
            Logix x = tm.createNewLogix(sysName, userName);
            if (x!=null) {
                // load common part
                loadCommon(x, logixList.get(i));
                
				// set enabled/disabled if attribute was present
				if ( (yesno!=null) && (!yesno.equals("")) ) {
					if (yesno.equals("yes")) x.setEnabled(true);
					else if (yesno.equals("no")) x.setEnabled(false);
				}
				// load conditionals, if there are any
                List<Element> logixConditionalList = logixList.get(i).getChildren("logixConditional");
				if (logixConditionalList.size()>0) {
					// add conditionals
                    for (int n=0; n<logixConditionalList.size(); n++) {
                        if (logixConditionalList.get(n).getAttribute("systemName") == null) {
                            log.warn("unexpected null in systemName "+logixConditionalList.get(n)+
                                                " "+logixConditionalList.get(n).getAttributes());
                            break;
                        }
                        String cSysName = logixConditionalList.get(n)
                                                            .getAttribute("systemName").getValue();
                        int cOrder = Integer.parseInt(logixConditionalList.get(n)
                                                            .getAttribute("order").getValue());
						// add conditional to logix
						x.addConditional(cSysName,cOrder);
					}
				}
			}	
	    }
	}

    /**
     * Replace the current LogixManager, if there is one, with
     * one newly created during a load operation. This is skipped
     * if they are of the same absolute type.
     */
    protected void replaceLogixManager() {
        if (InstanceManager.logixManagerInstance().getClass().getName()
                .equals(DefaultLogixManager.class.getName()))
            return;
        // if old manager exists, remove it from configuration process
        if (InstanceManager.logixManagerInstance() != null)
            InstanceManager.configureManagerInstance().deregister(
                InstanceManager.logixManagerInstance() );

        // register new one with InstanceManager
        DefaultLogixManager pManager = DefaultLogixManager.instance();
        InstanceManager.setLogixManager(pManager);
        // register new one for configuration
        InstanceManager.configureManagerInstance().registerConfig(pManager, jmri.Manager.LOGIXS);
    }
    
    public int loadOrder(){
        return InstanceManager.logixManagerInstance().getXMLOrder();
    }

    static Logger log = LoggerFactory.getLogger(DefaultLogixManagerXml.class.getName());
}

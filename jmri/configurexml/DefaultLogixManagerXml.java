// DefaultLogixManagerXML.java

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.DefaultLogixManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the functionality for
 * configuring LogixManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class DefaultLogixManagerXml implements XmlAdapter {

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
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("logix system name is "+sname);
                Logix x = tm.getBySystemName(sname);
                String uname = x.getUserName();
                Element elem = new Element("logix")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
				// save child Conditionals
				int numConditionals = x.getNumConditionals();
				if (numConditionals>0) {
					String cSysName = "";
					Element cElem = null;
					for (int k = 0;k<numConditionals;k++) {
						cSysName = x.getConditionalByNumberOrder(k);
						cElem = new Element("logixConditional");
						cElem.addAttribute("systemName",cSysName);
						cElem.addAttribute("order",Integer.toString(k));
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
        logixs.addAttribute("class","jmri.configurexml.DefaultLogixManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a LogixManager object of the correct class, then
     * register and fill it.
     * @param logixs Top level Element to unpack.
     */
    public void load(Element logixs) {
        // create the master object
        replaceLogixManager();
        // load individual logixs
        loadLogixs(logixs);
    }

    /**
     * Utility method to load the individual Logix objects.
     * If there's no additional info needed for a specific logix type,
     * invoke this with the parent of the set of Logix elements.
     * @param logixs Element containing the Logix elements to load.
     */
    public void loadLogixs(Element logixs) {
		List logixList = logixs.getChildren("logix");
        if (log.isDebugEnabled()) log.debug("Found "+logixList.size()+" logixs");
        LogixManager tm = InstanceManager.logixManagerInstance();

        for (int i=0; i<logixList.size(); i++) {
            if ( ((Element)(logixList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(logixList.get(i)))+" "+
										((Element)(logixList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(logixList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(logixList.get(i))).getAttribute("userName") != null)
                userName = ((Element)(logixList.get(i))).getAttribute("userName").getValue();
				
			if (log.isDebugEnabled()) log.debug("create logix: ("+sysName+")("+
										(userName==null?"<null>":userName)+")");
            Logix x = tm.createNewLogix(sysName, userName);
            if (x!=null) {
				// load conditionals, if there are any
                List logixConditionalList = ((Element)(logixList.get(i))).getChildren("logixConditional");
				if (logixConditionalList.size()>0) {
					// add conditionals
                    for (int n=0; n<logixConditionalList.size(); n++) {
                        if ( ((Element)(logixConditionalList.get(n))).getAttribute("systemName") == null) {
                            log.warn("unexpected null in systemName "+((Element)(logixConditionalList.get(n)))+
                                                " "+((Element)(logixConditionalList.get(n))).getAttributes());
                            break;
                        }
                        String cSysName = ((Element)(logixConditionalList.get(n)))
                                                            .getAttribute("systemName").getValue();
                        int cOrder = Integer.parseInt(((Element)(logixConditionalList.get(n)))
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
        InstanceManager.configureManagerInstance().registerConfig(pManager);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultLogixManagerXml.class.getName());
}

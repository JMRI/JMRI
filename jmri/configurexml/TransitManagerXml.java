// TransitManagerXML.java

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;

import java.util.List;
import java.util.ArrayList;
import org.jdom.*;

/**
 * Provides the functionality for
 * configuring a TransitManager
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2008
 * @version $Revision: 1.1 $
 */
public class TransitManagerXml extends AbstractNamedBeanManagerConfigXML {

    public TransitManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * TransitManager
     * @param o Object to store, of type TransitManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element transits = new Element("transits");
        setStoreElementClass(transits);
        TransitManager tm = (TransitManager) o;
        if (tm!=null) {
            java.util.Iterator iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not Transits to include
            if (!iter.hasNext()) return null;
            
            // store the Transit
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("Transit system name is "+sname);
                Transit x = tm.getBySystemName(sname);
                Element elem = new Element("transit")
                            .setAttribute("systemName", sname);
                
                // store common part
                storeCommon(x, elem);
                
				// save child transitsection entries
				ArrayList tsList = x.getTransitSectionList();
				Element tsElem = null;
				for (int k = 0; k<tsList.size(); k++) {
					TransitSection ts = (TransitSection)tsList.get(k);
					if (ts!=null) {						
						tsElem = new Element ("transitsection");
						tsElem.setAttribute("sectionname",ts.getSection().getSystemName());
						tsElem.setAttribute("sequence",Integer.toString(ts.getSequenceNumber()));
						tsElem.setAttribute("direction",Integer.toString(ts.getDirection()));
						tsElem.setAttribute("action",Integer.toString(ts.getAction()));
						tsElem.setAttribute("data",Integer.toString(ts.getData()));
						tsElem.setAttribute("alternate",""+(ts.isAlternate()?"yes":"no"));
						elem.addContent(tsElem);
					}
				}
				
				transits.addContent(elem);
			}
		}
		return (transits);	
	}

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param transits The top-level element being created
     */
    public void setStoreElementClass(Element transits) {
        transits.setAttribute("class","jmri.configurexml.TransitManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a TransitManager object of the correct class, then
     * register and fill it.
     * @param transits Top level Element to unpack.
     */
    public void load(Element transits) {
        // load individual Transits
        loadTransits(transits);
    }

    /**
     * Utility method to load the individual Transit objects.
     * If there's no additional info needed for a specific Transit type,
     * invoke this with the parent of the set of Transit elements.
     * @param transits Element containing the Transit elements to load.
     */
    public void loadTransits(Element transits) {
		List transitList = transits.getChildren("transit");
        if (log.isDebugEnabled()) log.debug("Found "+transitList.size()+" transits");
        TransitManager tm = InstanceManager.transitManagerInstance();

        for (int i=0; i<transitList.size(); i++) {
            if ( ((Element)(transitList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(transitList.get(i)))+" "+
										((Element)(transitList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(transitList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(transitList.get(i))).getAttribute("userName") != null) {
                userName = ((Element)(transitList.get(i))).getAttribute("userName").getValue();
			}
            Transit x = tm.createNewTransit(sysName, userName);
            if (x!=null) {
                // load common part
                loadCommon(x, ((Element)(transitList.get(i))));

				// load transitsection children
               List transitTransitSectionList = ((Element)(transitList.get(i))).getChildren("transitsection");
				for (int n = 0; n<transitTransitSectionList.size(); n++) {
					Element elem = (Element)transitTransitSectionList.get(n);
					int seq = 0;
					int dir = Section.UNKNOWN;
					int act = TransitSection.NONE;
					int dat = 5;
					boolean alt = false;
					String sectionName = elem.getAttribute("sectionname").getValue();
					try {
						seq = elem.getAttribute("sequence").getIntValue();
						dir = elem.getAttribute("direction").getIntValue();
						act = elem.getAttribute("action").getIntValue();
						dat = elem.getAttribute("data").getIntValue();
					}
					catch (Exception e) {
						log.error("Data Conversion Exception when loading direction of entry point - "+e);
					}
					if (elem.getAttribute("alternate").getValue().equals("yes")) alt = true;
					TransitSection ts = new TransitSection(sectionName,seq,dir,act,dat,alt);
					if (ts==null) {
						log.error("Trouble creation TransitSection for Transit - "+sysName);
					}
					else {
						x.addTransitSection(ts);
					}
				}                
			}	
	    }
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TransitManagerXml.class.getName());
}

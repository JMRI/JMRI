// TransitManagerXML.java

package jmri.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;

import java.util.List;
import java.util.ArrayList;
import org.jdom.*;

/**
 * Provides the functionality for
 * configuring a TransitManager
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2008
 * @version $Revision$
 */
public class TransitManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

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
            java.util.Iterator<String> iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not Transits to include
            if (!iter.hasNext()) return null;
            
            // store the Transit
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname==null) log.error("System name null during store");
                else {
                    log.debug("Transit system name is "+sname);
                    Transit x = tm.getBySystemName(sname);
                    Element elem = new Element("transit")
                                .setAttribute("systemName", sname);

                    // store common part
                    storeCommon(x, elem);

                    // save child transitsection entries
                    ArrayList<TransitSection> tsList = x.getTransitSectionList();
                    Element tsElem = null;
                    for (int k = 0; k<tsList.size(); k++) {
                        TransitSection ts = tsList.get(k);
                        if (ts!=null) {						
                            tsElem = new Element ("transitsection");
                            Section tSection = ts.getSection();
                            if (tSection!=null) tsElem.setAttribute("sectionname",tSection.getSystemName());
                            else tsElem.setAttribute("sectionname","null");
                            tsElem.setAttribute("sequence",Integer.toString(ts.getSequenceNumber()));
                            tsElem.setAttribute("direction",Integer.toString(ts.getDirection()));
                            tsElem.setAttribute("alternate",""+(ts.isAlternate()?"yes":"no"));                
                            // save child transitsectionaction entries if any
                            ArrayList<TransitSectionAction> tsaList = ts.getTransitSectionActionList();
                            if (tsaList.size()>0) {
                                Element tsaElem = null;
                                for (int m = 0; m<tsaList.size(); m++) {
                                    TransitSectionAction tsa = tsaList.get(m);
                                    if (tsa!=null) {						
                                        tsaElem = new Element ("transitsectionaction");
                                        tsaElem.setAttribute("whencode",Integer.toString(tsa.getWhenCode()));
                                        tsaElem.setAttribute("whatcode",Integer.toString(tsa.getWhatCode()));
                                        tsaElem.setAttribute("whendata",Integer.toString(tsa.getDataWhen()));
                                        tsaElem.setAttribute("whenstring",tsa.getStringWhen());
                                        tsaElem.setAttribute("whatdata1",Integer.toString(tsa.getDataWhat1()));
                                        tsaElem.setAttribute("whatdata2",Integer.toString(tsa.getDataWhat2()));
                                        tsaElem.setAttribute("whatstring",tsa.getStringWhat());									
                                        tsElem.addContent(tsaElem);
                                    }
                                }
                            }
                            elem.addContent(tsElem);
                        }
                    }

                    transits.addContent(elem);
                }
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
     * @return true if successful
     */
    public boolean load(Element transits) {
        // load individual Transits
        loadTransits(transits);
        return true;
    }

    /**
     * Utility method to load the individual Transit objects.
     * If there's no additional info needed for a specific Transit type,
     * invoke this with the parent of the set of Transit elements.
     * @param transits Element containing the Transit elements to load.
     */
    @SuppressWarnings({ "unchecked", "null" })
	public void loadTransits(Element transits) {
		List<Element> transitList = transits.getChildren("transit");
        if (log.isDebugEnabled()) log.debug("Found "+transitList.size()+" transits");
        TransitManager tm = InstanceManager.transitManagerInstance();

        for (int i=0; i<transitList.size(); i++) {
            if (transitList.get(i).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+ transitList.get(i)+" "+
										transitList.get(i).getAttributes());
                break;
            }
            String sysName = transitList.get(i).getAttribute("systemName").getValue();
            String userName = null;
            if (transitList.get(i).getAttribute("userName") != null) {
                userName = transitList.get(i).getAttribute("userName").getValue();
			}
            Transit x = tm.createNewTransit(sysName, userName);
            if (x!=null) {
                // load common part
                loadCommon(x, transitList.get(i));

				// load transitsection children
               List<Element> transitTransitSectionList = transitList.get(i).getChildren("transitsection");
				for (int n = 0; n<transitTransitSectionList.size(); n++) {
					Element elem = transitTransitSectionList.get(n);
					int seq = 0;
					int dir = Section.UNKNOWN;
					boolean alt = false;
					String sectionName = elem.getAttribute("sectionname").getValue();
                    if (sectionName.equals("null")) {
                        log.warn("When loading configuration - missing Section in Transit "+sysName);
                    }
					try {
						seq = elem.getAttribute("sequence").getIntValue();
						dir = elem.getAttribute("direction").getIntValue();
					}
					catch (Exception e) {
						log.error("Data Conversion Exception when loading direction of entry point - "+e);
					}
					if (elem.getAttribute("alternate").getValue().equals("yes")) alt = true;
					TransitSection ts = new TransitSection(sectionName,seq,dir,alt);
					if (ts==null) {
						log.error("Trouble creation TransitSection for Transit - "+sysName);
					}
					else {
						x.addTransitSection(ts);
						// load transitsectionaction children, if any
						List<Element> transitTransitSectionActionList = transitTransitSectionList.get(n).
											getChildren("transitsectionaction");
						for (int m = 0; m<transitTransitSectionActionList.size(); m++) {
							Element elemx = transitTransitSectionActionList.get(m);
							int tWhen = 1;
							int tWhat = 1;
							int tWhenData = 0;
							String tWhenString = elemx.getAttribute("whenstring").getValue();
							int tWhatData1 = 0;
							int tWhatData2 = 0;
							String tWhatString = elemx.getAttribute("whatstring").getValue();
							try {
								tWhen = elemx.getAttribute("whencode").getIntValue();
								tWhat = elemx.getAttribute("whatcode").getIntValue();
								tWhenData = elemx.getAttribute("whendata").getIntValue();
								tWhatData1 = elemx.getAttribute("whatdata1").getIntValue();
								tWhatData2 = elemx.getAttribute("whatdata2").getIntValue();
							}
							catch (Exception e) {
								log.error("Data Conversion Exception when loading transit section action - "+e);
							}
							TransitSectionAction tsa = new TransitSectionAction(tWhen,tWhat,tWhenData,
										tWhatData1,tWhatData2,tWhenString,tWhatString);
							if (tsa==null) {
								log.error("Trouble creating TransitSectionAction for Transit - "+sysName);
							}
							else {
								ts.addAction(tsa);
							}
						}							
					}
				}                
			}	
	    }
	}
    
    public int loadOrder(){
        return InstanceManager.transitManagerInstance().getXMLOrder();
    }

    static Logger log = LoggerFactory.getLogger(TransitManagerXml.class.getName());
}

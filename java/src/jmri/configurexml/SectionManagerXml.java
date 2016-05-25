package jmri.configurexml;

import java.util.List;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for persistence of a SectionManager
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2008
 */
public class SectionManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SectionManagerXml() {
    }

    /**
     * Implementation for storing the contents of a SectionManager
     *
     * @param o Object to store, of type SectionManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element sections = new Element("sections");
        setStoreElementClass(sections);
        SectionManager tm = (SectionManager) o;
        if (tm != null) {
            java.util.Iterator<String> iter
                    = tm.getSystemNameList().iterator();

            // don't return an element if there are not Sections to include
            if (!iter.hasNext()) {
                return null;
            }

            // store the Section
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname == null) {
                    log.error("System name null during store");
                } else {
                    log.debug("Section system name is " + sname);
                    Section x = tm.getBySystemName(sname);
                    if (x.getSectionType() != Section.DYNAMICADHOC) {
                        Element elem = new Element("section")
                                .setAttribute("systemName", sname);

                        // store common part
                        storeCommon(x, elem);
                        String txt = "userdefined";
                        if (x.getSectionType() == Section.SIGNALMASTLOGIC) {
                            txt = "signalmastlogic";
                        }
                        elem.setAttribute("creationtype", txt);
                        txt = x.getForwardStoppingSensorName();
                        if ((txt != null) && (!txt.equals(""))) {
                            elem.setAttribute("fstopsensorname", txt);
                        }
                        txt = x.getReverseStoppingSensorName();
                        if ((txt != null) && (!txt.equals(""))) {
                            elem.setAttribute("rstopsensorname", txt);
                        }
                        txt = x.getForwardBlockingSensorName();
                        if ((txt != null) && (!txt.equals(""))) {
                            elem.setAttribute("fsensorname", txt);
                        }
                        txt = x.getReverseBlockingSensorName();
                        if ((txt != null) && (!txt.equals(""))) {
                            elem.setAttribute("rsensorname", txt);
                        }
                        if (x.getSectionType() == Section.USERDEFINED) {
                            // save child block entries
                            int index = 0;
                            Block b = x.getBlockBySequenceNumber(index);
                            Element bElem = null;
                            while (b != null) {
                                bElem = new Element("blockentry");
                                bElem.setAttribute("sName", b.getSystemName());
                                bElem.setAttribute("order", Integer.toString(index));
                                elem.addContent(bElem);
                                index++;
                                b = x.getBlockBySequenceNumber(index);
                            }
                            // save child entry points
                            List<EntryPoint> epList = x.getEntryPointList();
                            Element epElem = null;
                            EntryPoint ep = null;
                            for (int i = 0; i < epList.size(); i++) {
                                ep = epList.get(i);
                                if (ep != null) {
                                    epElem = new Element("entrypoint");
                                    // add some protection against a reading problem
                                    if (ep.getFromBlock() == null) {
                                        log.error("Unexpected null getFromBlock while storing ep " + i + " in Section " + sname + ", skipped");
                                        break;
                                    }
                                    epElem.setAttribute("fromblock", ep.getFromBlock().getSystemName());
                                    if (ep.getBlock() == null) {
                                        log.error("Unexpected null getBlock while storing ep " + i + " in Section " + sname + ", skipped");
                                        break;
                                    }
                                    epElem.setAttribute("toblock", ep.getBlock().getSystemName());
                                    epElem.setAttribute("direction", Integer.toString(ep.getDirection()));
                                    epElem.setAttribute("fixed", "" + (ep.isFixed() ? "yes" : "no"));
                                    epElem.setAttribute("fromblockdirection", "" + ep.getFromBlockDirection());
                                    elem.addContent(epElem);
                                }
                            }
                        }
                        sections.addContent(elem);
                    }
                }
            }
        }
        return (sections);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param sections The top-level element being created
     */
    public void setStoreElementClass(Element sections) {
        sections.setAttribute("class", "jmri.configurexml.SectionManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a SectionManager object of the correct class, then register and
     * fill it.
     *
     * @param sharedSections  Top level Element to unpack.
     * @param perNodeSections Per-node Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedSections, Element perNodeSections) {
        // load individual Sections
        loadSections(sharedSections, perNodeSections);
        return true;
    }

    /**
     * Utility method to load the individual Section objects. If there's no
     * additional info needed for a specific Section type, invoke this with the
     * parent of the set of Section elements.
     *
     * @param sharedSections  Element containing the Section elements to load.
     * @param perNodeSections Per-node Element containing the Section elements
     *                        to load.
     */
    public void loadSections(Element sharedSections, Element perNodeSections) {
        List<Element> sectionList = sharedSections.getChildren("section");
        if (log.isDebugEnabled()) {
            log.debug("Found " + sectionList.size() + " sections");
        }
        SectionManager tm = InstanceManager.sectionManagerInstance();

        for (int i = 0; i < sectionList.size(); i++) {
            if (sectionList.get(i).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName " + sectionList.get(i) + " "
                        + (sectionList.get(i)).getAttributes());
                break;
            }
            String sysName = (sectionList.get(i)).getAttribute("systemName").getValue();
            String userName = null;
            if (sectionList.get(i).getAttribute("userName") != null) {
                userName = (sectionList.get(i)).getAttribute("userName").getValue();
            }
            Section x = tm.createNewSection(sysName, userName);
            if (x != null) {
                // load common part
                loadCommon(x, (sectionList.get(i)));

                if (sectionList.get(i).getAttribute("creationtype") != null) {
                    String creationType = sectionList.get(i).getAttribute("creationtype").getValue();
                    if (creationType.equals("userdefined")) {
                        x.setSectionType(Section.USERDEFINED);
                    } else if (creationType.equals("signalmastlogic")) {
                        x.setSectionType(Section.SIGNALMASTLOGIC);
                    }
                }

                if (sectionList.get(i).getAttribute("fsensorname") != null) {
                    String forName = (sectionList.get(i)).getAttribute("fsensorname").getValue();
                    x.delayedSetForwardBlockingSensorName(forName);
                }
                if (sectionList.get(i).getAttribute("rsensorname") != null) {
                    String revName = sectionList.get(i).getAttribute("rsensorname").getValue();
                    x.delayedSetReverseBlockingSensorName(revName);
                }
                if (sectionList.get(i).getAttribute("fstopsensorname") != null) {
                    String forName = sectionList.get(i).getAttribute("fstopsensorname").getValue();
                    x.delayedSetForwardStoppingSensorName(forName);
                }
                if (sectionList.get(i).getAttribute("rstopsensorname") != null) {
                    String revName = sectionList.get(i).getAttribute("rstopsensorname").getValue();
                    x.delayedSetReverseStoppingSensorName(revName);
                }

                // load block entry children
                List<Element> sectionBlockList = sectionList.get(i).getChildren("blockentry");
                for (int n = 0; n < sectionBlockList.size(); n++) {
                    Element elem = sectionBlockList.get(n);
                    x.delayedAddBlock(elem.getAttribute("sName").getValue());
// insert code here to verify sequence number if needed in the future
                }

                // load entry point children
                List<Element> sectionEntryPointList = sectionList.get(i).getChildren("entrypoint");
                for (int n = 0; n < sectionEntryPointList.size(); n++) {
                    Element elem = sectionEntryPointList.get(n);
                    String blockName = elem.getAttribute("toblock").getValue();
                    String fromBlockName = elem.getAttribute("fromblock").getValue();
                    String fromBlockDirection = "";
                    if (elem.getAttribute("fromblockdirection") != null) {
                        fromBlockDirection = elem.getAttribute("fromblockdirection").getValue();
                    }
                    EntryPoint ep = new EntryPoint(blockName, fromBlockName, fromBlockDirection);
                    //if (ep!=null) {
                    try {
                        ep.setDirection(elem.getAttribute("direction").getIntValue());
                    } catch (Exception e) {
                        log.error("Data Conversion Exception when loading direction of entry point - " + e);
                    }
                    boolean fixed = true;
                    if (elem.getAttribute("fixed").getValue().equals("no")) {
                        fixed = false;
                    }
                    ep.setFixed(fixed);
                    if (ep.isForwardType()) {
                        x.addToForwardList(ep);
                    } else if (ep.isReverseType()) {
                        x.addToReverseList(ep);
                    }
                    //}
                }
            }
        }
    }

    public int loadOrder() {
        return InstanceManager.sectionManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(SectionManagerXml.class.getName());
}

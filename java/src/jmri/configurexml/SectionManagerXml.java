package jmri.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for persistence of a SectionManager.
 *
 * @author Dave Duchamp Copyright (c) 2008
 */
public class SectionManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SectionManagerXml() {
    }

    /**
     * Implementation for storing the contents of a SectionManager.
     *
     * @param o Object to store, of type SectionManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element sections = new Element("sections");
        setStoreElementClass(sections);
        SectionManager sctm = (SectionManager) o;
        if (sctm != null) {
            SortedSet<Section> sctList = sctm.getNamedBeanSet();
            // don't return an element if there are no Sections to include
            if (sctList.isEmpty()) {
                return null;
            }

            // store the Sections
            for (Section x : sctList) {
                if (x == null) {
                    log.error("Memory null during store, skipped");
                    break;
                }
                String sName = x.getSystemName();
                log.debug("Section system name is {}", sName);

                if (x.getSectionType() != Section.DYNAMICADHOC) {
                    Element elem = new Element("section");
                    elem.addContent(new Element("systemName").addContent(sName));

                    // As a work-around for backward compatibility, store systemName and username as attribute.
                    // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                    elem.setAttribute("systemName", sName);
                    String uname = x.getUserName();
                    if (uname != null && !uname.equals("")) {
                        elem.setAttribute("userName", uname);
                    }

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
                        int i = 0;
                        for (EntryPoint ep : epList) {
                            if (ep != null) {
                                epElem = new Element("entrypoint");
                                // add some protection against a reading problem
                                if (ep.getFromBlock() == null) {
                                    log.error("Unexpected null getFromBlock while storing ep {} in Section {}, skipped", i, sName);
                                    break;
                                }
                                epElem.setAttribute("fromblock", ep.getFromBlock().getSystemName());
                                if (ep.getBlock() == null) {
                                    log.error("Unexpected null getBlock while storing ep {} in Section {}, skipped", i, sName);
                                    break;
                                }
                                epElem.setAttribute("toblock", ep.getBlock().getSystemName());
                                epElem.setAttribute("direction", Integer.toString(ep.getDirection()));
                                epElem.setAttribute("fixed", "" + (ep.isFixed() ? "yes" : "no"));
                                epElem.setAttribute("fromblockdirection", "" + ep.getFromBlockDirection());
                                elem.addContent(epElem);
                                i++;
                            }
                        }
                    }
                    sections.addContent(elem);
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

    @Override
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
        log.debug("Found {} Sections", sectionList.size());
        SectionManager sctm = InstanceManager.getDefault(jmri.SectionManager.class);
        sctm.setDataListenerMute(true);
        
        for (Element s : sectionList) {
            String sysName = getSystemName(s);
            String userName = getUserName(s);
            Section x = sctm.createNewSection(sysName, userName);
            if (x != null) {
                // load common part
                loadCommon(x, (s));

                if (s.getAttribute("creationtype") != null) {
                    String creationType = s.getAttribute("creationtype").getValue();
                    if (creationType.equals("userdefined")) {
                        x.setSectionType(Section.USERDEFINED);
                    } else if (creationType.equals("signalmastlogic")) {
                        x.setSectionType(Section.SIGNALMASTLOGIC);
                    }
                }
                if (s.getAttribute("fsensorname") != null) {
                    String forName = s.getAttribute("fsensorname").getValue();
                    x.delayedSetForwardBlockingSensorName(forName);
                }
                if (s.getAttribute("rsensorname") != null) {
                    String revName = s.getAttribute("rsensorname").getValue();
                    x.delayedSetReverseBlockingSensorName(revName);
                }
                if (s.getAttribute("fstopsensorname") != null) {
                    String forName = s.getAttribute("fstopsensorname").getValue();
                    x.delayedSetForwardStoppingSensorName(forName);
                }
                if (s.getAttribute("rstopsensorname") != null) {
                    String revName = s.getAttribute("rstopsensorname").getValue();
                    x.delayedSetReverseStoppingSensorName(revName);
                }

                // load block entry children
                List<Element> sectionBlockList = s.getChildren("blockentry");
                for (Element elem : sectionBlockList) {
                    x.delayedAddBlock(elem.getAttribute("sName").getValue());
                    // insert code here to verify sequence number if needed in the future
                }

                // load entry point children
                List<Element> sectionEntryPointList = s.getChildren("entrypoint");
                for (Element elem : sectionEntryPointList) {
                    String blockName = elem.getAttribute("toblock").getValue();
                    String fromBlockName = elem.getAttribute("fromblock").getValue();
                    String fromBlockDirection = "";
                    if (elem.getAttribute("fromblockdirection") != null) {
                        fromBlockDirection = elem.getAttribute("fromblockdirection").getValue();
                    }
                    EntryPoint ep = new EntryPoint(blockName, fromBlockName, fromBlockDirection);
                    try {
                        ep.setDirection(elem.getAttribute("direction").getIntValue());
                    } catch (Exception e) {
                        log.error("Data Conversion Exception when loading direction of entry point - ", e);
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
                }
            }
        }
        sctm.setDataListenerMute(false);
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.SectionManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(SectionManagerXml.class);

}

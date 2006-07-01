package jmri.jmrit.blockboss.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.blockboss.BlockBossLogic;
import java.util.Enumeration;

import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Handle XML persistance of Simple Signal Logic objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class BlockBossLogicXml implements XmlAdapter {

    public BlockBossLogicXml() {
    }

    /**
     * Default implementation for storing the contents of
     * all the BLockBossLogic elements.
     * <P>
     * Static members in the BlockBossLogic class record the
     * complete set of items.  This function writes those out
     * as a single XML element.
     *
     * @param o Object to start process, but not actually used
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        Enumeration e = BlockBossLogic.entries();
        if (!e.hasMoreElements()) return null;  // nothing to write!
        Element blocks = new Element("blocks");
        blocks.addAttribute("class", this.getClass().getName());

        while ( e.hasMoreElements()) {
            BlockBossLogic p = (BlockBossLogic) e.nextElement();
            Element block = new Element("block");
            block.addAttribute("signal", p.getDrivenSignal());
            if (p.getSensor()!=null) block.addAttribute("watchedsensor", p.getSensor());
            if (p.getTurnout()!=null) {
                block.addAttribute("watchedturnout", p.getTurnout());
                block.addAttribute("watchedturnoutstate", ""+p.getTurnoutState());
            }
            if (p.getWatchedSignal()!=null) {
                block.addAttribute("watchedsignal", p.getWatchedSignal());
                block.addAttribute("useflashyellow", ""+p.getUseFlash());
            }
            blocks.addContent(block);

        }

        return blocks;
    }

    /**
     * Update static data from XML file
     * @param element Top level blocks Element to unpack.
      */
    public void load(Element element) {
        List l = element.getChildren("block");
        for (int i = 0; i<l.size(); i++) {
            Element block = (Element)l.get(i);
            BlockBossLogic bb = BlockBossLogic.getStoppedObject(block.getAttributeValue("signal"));
            if (block.getAttribute("watchedsensor")!=null)
                bb.setSensor(block.getAttributeValue("watchedsensor"));
            try {
                if (block.getAttribute("watchedturnout")!=null)
                    bb.setTurnout(block.getAttributeValue("watchedturnout"),
                                  block.getAttribute("watchedturnoutstate").getIntValue());
                if (block.getAttribute("watchedsignal")!=null)
                    bb.setWatchedSignal(block.getAttributeValue("watchedsignal"),
                                        block.getAttribute("useflashyellow").getBooleanValue());
            } catch (org.jdom.DataConversionException e) {
                log.warn("error reading blocks from file"+e);
            }
            bb.retain();
            bb.start();
        }
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("load(Element, Object) called unexpectedly");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogicXml.class.getName());

}
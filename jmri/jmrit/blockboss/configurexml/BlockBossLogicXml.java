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
 * @version $Revision: 1.2 $
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
            block.addAttribute("mode", ""+p.getMode());
            if (p.getSensor()!=null) block.addAttribute("watchedsensor", p.getSensor());
            if (p.getTurnout()!=null) {
                block.addAttribute("watchedturnout", p.getTurnout());
            }
            if (p.getWatchedSignal1()!=null) {
                block.addAttribute("watchedsignal1", p.getWatchedSignal1());
            }
            if (p.getWatchedSignal2()!=null) {
                block.addAttribute("watchedsignal2", p.getWatchedSignal2());
            }
            block.addAttribute("useflashyellow", ""+p.getUseFlash());
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
                bb.setMode(block.getAttribute("mode").getIntValue());
                if (block.getAttribute("watchedturnout")!=null)
                    bb.setTurnout(block.getAttributeValue("watchedturnout"));
                if (block.getAttribute("watchedsignal1")!=null)
                    bb.setWatchedSignal1(block.getAttributeValue("watchedsignal1"),
                                        block.getAttribute("useflashyellow").getBooleanValue());
                if (block.getAttribute("watchedsignal2")!=null)
                    bb.setWatchedSignal2(block.getAttributeValue("watchedsignal2"));
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
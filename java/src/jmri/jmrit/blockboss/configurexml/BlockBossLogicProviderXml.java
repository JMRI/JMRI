package jmri.jmrit.blockboss.configurexml;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.SignalHeadManager;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.blockboss.BlockBossLogicProvider;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Handle XML persistance of Simple Signal Logic objects.
 *
 * <p>
 * In JMRI 2.1.5, the XML written by this package was changed.
 * <p>
 * Previously, it wrote a single "blocks" element, which contained multiple
 * "block" elements to represent each individual BlockBoss (Simple Signal Logic)
 * object.
 * <p>
 * These names were too generic, and conflicted with storing true Block objects.
 * <p>
 * Starting in JMRI 2.1.5 (May 2008), these were changed to "signalelements" and
 * "signalelement" respectively.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2005
 *
 * Revisions to add facing point sensors, approach lighting, and limited speed.
 * Dick Bronson (RJB) 2006
 */
public class BlockBossLogicProviderXml extends jmri.configurexml.AbstractXmlAdapter {

    private static final String SYSTEM_NAME = "systemName";
    private static final String SIGNAL = "signal";
    private static final String APPROACHSENSOR_1 = "approachsensor1";
    private static final String WATCHEDTURNOUT = "watchedturnout";
    private static final String WATCHEDSIGNAL_1 = "watchedsignal1";
    private static final String WATCHEDSIGNAL_1_ALT = "watchedsignal1alt";
    private static final String WATCHEDSIGNAL_2 = "watchedsignal2";
    private static final String WATCHEDSIGNAL_2_ALT = "watchedsignal2alt";
    private static final String WATCHEDSENSOR_1 = "watchedsensor1";
    private static final String WATCHEDSENSOR_1_ALT = "watchedsensor1alt";
    private static final String WATCHEDSENSOR_2 = "watchedsensor2";
    private static final String WATCHEDSENSOR_2_ALT = "watchedsensor2alt";
    private static final String LIMITSPEED_1 = "limitspeed1";
    private static final String LIMITSPEED_2 = "limitspeed2";
    private static final String RESTRICTINGSPEED_1 = "restrictingspeed1";
    private static final String RESTRICTINGSPEED_2 = "restrictingspeed2";
    private static final String USEFLASHYELLOW = "useflashyellow";
    private static final String DISTANTSIGNAL = "distantsignal";
    private final BlockBossLogicProvider blockBossLogicProvider;

    public BlockBossLogicProviderXml() {
        blockBossLogicProvider = InstanceManager.getDefault(BlockBossLogicProvider.class);
    }

    /**
     * Default implementation for storing the contents of all the BLockBossLogic
     * elements.
     * <p>
     * Static members in the BlockBossLogic class record the complete set of
     * items. This function writes those out as a single XML element.
     *
     * @param o Object to start process, but not actually used
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        Enumeration<BlockBossLogic> e = Collections.enumeration(blockBossLogicProvider.provideAll());
        if (!e.hasMoreElements()) {
            return null;  // nothing to write!
        }
        Element blocks = new Element("signalelements");
        blocks.setAttribute("class", this.getClass().getName());

        while (e.hasMoreElements()) {
            BlockBossLogic p = e.nextElement();
            Element block = getElementFromBlockBossLogic(p);
            blocks.addContent(block);
        }

        return blocks;
    }

    private Element getElementFromBlockBossLogic(BlockBossLogic p) {
        Element block = new Element("signalelement");
        block.setAttribute(SIGNAL, p.getDrivenSignal());
        block.setAttribute("mode", "" + p.getMode());

        if (p.getApproachSensor1() != null) {
            block.setAttribute(APPROACHSENSOR_1, p.getApproachSensor1());
        }

        if (p.getSensor1() != null) {
            block.addContent(storeSensor(p.getSensor1()));
        }
        if (p.getSensor2() != null) {
            block.addContent(storeSensor(p.getSensor2()));
        }
        if (p.getSensor3() != null) {
            block.addContent(storeSensor(p.getSensor3()));
        }
        if (p.getSensor4() != null) {
            block.addContent(storeSensor(p.getSensor4()));
        }
        if (p.getSensor5() != null) {
            block.addContent(storeSensor(p.getSensor5()));
        }

        if (p.getTurnout() != null) {
            block.setAttribute(WATCHEDTURNOUT, p.getTurnout());
        }
        if (p.getWatchedSignal1() != null) {
            block.setAttribute(WATCHEDSIGNAL_1, p.getWatchedSignal1());
        }
        if (p.getWatchedSignal1Alt() != null) {
            block.setAttribute(WATCHEDSIGNAL_1_ALT, p.getWatchedSignal1Alt());
        }
        if (p.getWatchedSignal2() != null) {
            block.setAttribute(WATCHEDSIGNAL_2, p.getWatchedSignal2());
        }
        if (p.getWatchedSignal2Alt() != null) {
            block.setAttribute(WATCHEDSIGNAL_2_ALT, p.getWatchedSignal2Alt());
        }
        if (p.getWatchedSensor1() != null) {
            block.setAttribute(WATCHEDSENSOR_1, p.getWatchedSensor1());
        }
        if (p.getWatchedSensor1Alt() != null) {
            block.setAttribute(WATCHEDSENSOR_1_ALT, p.getWatchedSensor1Alt());
        }
        if (p.getWatchedSensor2() != null) {
            block.setAttribute(WATCHEDSENSOR_2, p.getWatchedSensor2());
        }
        if (p.getWatchedSensor2Alt() != null) {
            block.setAttribute(WATCHEDSENSOR_2_ALT, p.getWatchedSensor2Alt());
        }

        block.setAttribute(LIMITSPEED_1, "" + p.getLimitSpeed1());
        block.setAttribute(LIMITSPEED_2, "" + p.getLimitSpeed2());
        if (p.getRestrictingSpeed1())
            block.setAttribute(RESTRICTINGSPEED_1, "" + p.getRestrictingSpeed1());
        if (p.getRestrictingSpeed2())
            block.setAttribute(RESTRICTINGSPEED_2, "" + p.getRestrictingSpeed2());
        block.setAttribute(USEFLASHYELLOW, "" + p.getUseFlash());
        block.setAttribute(DISTANTSIGNAL, "" + p.getDistantSignal());

        // add comment, if present
        if (p.getComment() != null) {
            Element c = new Element("comment");
            c.addContent(p.getComment());
            block.addContent(c);
        }
        return block;
    }

    private Element storeSensor(String name) {
        Element e = new Element("sensorname");
        e.addContent(name);
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("signalelement");

        // try old format if there are no new entries
        // this is for backward compatibility only
        if (l.size() == 0) {
            l = shared.getChildren("block");
        }

        // process each item
        for (Element block : l) {
            BlockBossLogic bb = getBlockBossLogicFromElement(block);
            if (bb == null) {
                continue;
            }
            loadBlockBossLogicDetailsFromElement(block, bb);
        }
        return true;
    }

    private boolean loadBlockBossLogicDetailsFromElement(Element block, BlockBossLogic bb) {
        boolean result = loadOptionalApproachSensor(block, bb);

        result &= loadOptionalWatchedSensor(block, bb);

        result &= loadOldFormSensorNames(block, bb);

        try {
            bb.setMode(block.getAttribute("mode").getIntValue());
            if (block.getAttribute(DISTANTSIGNAL) != null) {
                bb.setDistantSignal(block.getAttribute(DISTANTSIGNAL).getBooleanValue());
            }
            if (block.getAttribute(LIMITSPEED_1) != null) {
                bb.setLimitSpeed1(block.getAttribute(LIMITSPEED_1).getBooleanValue());
            }
            if (block.getAttribute(RESTRICTINGSPEED_1) != null) {
                bb.setRestrictingSpeed1(block.getAttribute(RESTRICTINGSPEED_1).getBooleanValue());
            }
            if (block.getAttribute(LIMITSPEED_2) != null) {
                bb.setLimitSpeed2(block.getAttribute(LIMITSPEED_2).getBooleanValue());
            }
            if (block.getAttribute(RESTRICTINGSPEED_2) != null) {
                bb.setRestrictingSpeed2(block.getAttribute(RESTRICTINGSPEED_2).getBooleanValue());
            }
            result &= loadWatchedTurnout(block, bb);

            result &= loadWatchedSignal1(block, bb);

            result &= loadWAtchedSignal1Alt(block, bb);

            result &= loadWatchedSignal2(block, bb);

            result &= loadWatchedSignal2Alt(block, bb);

            result &= loadWatchedSensor1(block, bb);

            result &= loadWatchedSensor1Alt(block, bb);

            result &= loadWatchedSensor2(block, bb);

            result &= loadWatchedSensor2Alt(block, bb);

            // load comment, if present
            String c = block.getChildText("comment");
            if (c != null) {
                bb.setComment(c);
            }

        } catch (org.jdom2.DataConversionException e) {
            log.warn("error reading blocks from file{}", e);
            result = false;
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in the signal element attribute list");
            result = false;
        }
        try {
            blockBossLogicProvider.register(bb);
            bb.start();
        } catch (IllegalArgumentException e) {
            log.debug("An error occurred trying to start the signal logic {} :: message = {}", bb.getDrivenSignal(), e.getMessage());
            result = false;
        }
        return result;
    }

    private boolean loadOldFormSensorNames(Element block, BlockBossLogic bb) {
        boolean result = true;
        // old form of sensors with system names
        List<Element> sl = block.getChildren("sensor");
        try {
            getSensorAttributesUsingSystemName(bb, sl);
        } catch (IllegalArgumentException e) {
            log.error("An error occurred loading the sensors list in the SSL");
            result = false;
        }
        // new form of sensors with system names
        sl = block.getChildren("sensorname");
        try {
            if (sl.size() >= 1 && sl.get(0) != null) {
                bb.setSensor1(sl.get(0).getText());
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred loading the sensor1 list in the SSL for {}", bb.getDrivenSignal());
            result = false;
        }

        try {
            if (sl.size() >= 2 && sl.get(1) != null) {
                bb.setSensor2(sl.get(1).getText());
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred loading the sensor2 list in the SSL for {}", bb.getDrivenSignal());
            result = false;
        }

        try {
            if (sl.size() >= 3 && sl.get(2) != null) {
                bb.setSensor3(sl.get(2).getText());
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred loading the sensor3 list in the SSL for {}", bb.getDrivenSignal());
            result = false;
        }

        try {
            if (sl.size() >= 4 && sl.get(3) != null) {
                bb.setSensor4(sl.get(3).getText());
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred loading the sensor4 list in the SSL for {}", bb.getDrivenSignal());
            result = false;
        }

        try {
            if (sl.size() >= 5 && sl.get(4) != null) {
                bb.setSensor5(sl.get(4).getText());
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred loading the sensor5 list in the SSL for {}", bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadOptionalWatchedSensor(Element block, BlockBossLogic bb) {
        boolean result = true;
        if (block.getAttribute("watchedsensor") != null) {   // for older XML files
            try {
                bb.setSensor1(block.getAttributeValue("watchedsensor"));
            } catch (IllegalArgumentException e) {
                log.error("An error occurred loading the watched sensor in the SSL for {}", bb.getDrivenSignal());
                result = false;
            }
        }
        return result;
    }

    private boolean loadOptionalApproachSensor(Element block, BlockBossLogic bb) {
        boolean result = true;
        if (block.getAttribute(APPROACHSENSOR_1) != null) {
            try {
                bb.setApproachSensor1(block.getAttributeValue(APPROACHSENSOR_1));
            } catch (IllegalArgumentException e) {
                log.error("An error occurred loading the approach sensor for the signal elements for {}", bb.getDrivenSignal());
                result = false;
            }
        }
        return result;
    }

    private boolean loadWatchedSensor2Alt(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSENSOR_2_ALT) != null) {
                bb.setWatchedSensor2Alt(block.getAttributeValue(WATCHEDSENSOR_2_ALT));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched sensor 2 alt ({})element attribute list for {}", block.getAttributeValue(WATCHEDSENSOR_2_ALT), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedSensor2(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSENSOR_2) != null) {
                bb.setWatchedSensor2(block.getAttributeValue(WATCHEDSENSOR_2));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched sensor 2 ({}) element attribute list for {}", block.getAttributeValue(WATCHEDSENSOR_2), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedSensor1Alt(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSENSOR_1_ALT) != null) {
                bb.setWatchedSensor1Alt(block.getAttributeValue(WATCHEDSENSOR_1_ALT));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched sensor 1 alt ({}) element attribute list for {}", block.getAttributeValue(WATCHEDSENSOR_1_ALT), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedSensor1(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSENSOR_1) != null) {
                bb.setWatchedSensor1(block.getAttributeValue(WATCHEDSENSOR_1));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched sensor 1 ({}) element attribute list for {}", block.getAttributeValue(WATCHEDSENSOR_1), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedSignal2Alt(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSIGNAL_2_ALT) != null) {
                bb.setWatchedSignal2Alt(block.getAttributeValue(WATCHEDSIGNAL_2_ALT));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched signal 2 alt ({}) element attribute list for {}", block.getAttributeValue(WATCHEDSIGNAL_2_ALT), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedSignal2(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSIGNAL_2) != null) {
                bb.setWatchedSignal2(block.getAttributeValue(WATCHEDSIGNAL_2));
            }

        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched signal 2 ({})element attribute list for {}", block.getAttributeValue(WATCHEDSIGNAL_2), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWAtchedSignal1Alt(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSIGNAL_1_ALT) != null) {
                bb.setWatchedSignal1Alt(block.getAttributeValue(WATCHEDSIGNAL_1_ALT));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched signal 1 alt ({})element attribute list for {}", block.getAttributeValue(WATCHEDSIGNAL_1_ALT), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedSignal1(Element block, BlockBossLogic bb) throws DataConversionException {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDSIGNAL_1) != null) {
                bb.setWatchedSignal1(block.getAttributeValue(WATCHEDSIGNAL_1), block.getAttribute(USEFLASHYELLOW).getBooleanValue());
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched signal 1 ({})element attribute list for {}", block.getAttributeValue(WATCHEDSIGNAL_1), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private boolean loadWatchedTurnout(Element block, BlockBossLogic bb) {
        boolean result = true;
        try {
            if (block.getAttribute(WATCHEDTURNOUT) != null) {
                bb.setTurnout(block.getAttributeValue(WATCHEDTURNOUT));
            }
        } catch (IllegalArgumentException e) {
            log.error("An error occurred in retrieving the watched turnout ({})element attribute list for {}", block.getAttributeValue(WATCHEDTURNOUT), bb.getDrivenSignal());
            result = false;
        }
        return result;
    }

    private BlockBossLogic getBlockBossLogicFromElement(Element block) {
        BlockBossLogic blockBossLogic;
        String signalName = block.getAttributeValue(SIGNAL);
        if (signalName == null || signalName.isEmpty()) {
            // this is an error
            log.error("Ignoring a <signalelement> element with no signal attribute value");
            return null;
        }

        if (InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName) == null) {
            // this is an error
            log.error("SignalHead {} not defined, <signalelement> element referring to it is ignored", signalName);
            return null;
        }

        try {
            blockBossLogic = BlockBossLogic.getStoppedObject(signalName);
        } catch (IllegalArgumentException e) {
            // Potential exception in BLockBossProvider:provide via BlockBossLogic.
            return null;
        }
        return blockBossLogic;
    }

    private void getSensorAttributesUsingSystemName(BlockBossLogic bb, List<Element> sl) {
        if (sl.size() >= 1 && sl.get(0) != null) {
            bb.setSensor1(sl.get(0).getAttributeValue(SYSTEM_NAME));
        }
        if (sl.size() >= 2 && sl.get(1) != null) {
            bb.setSensor2(sl.get(1).getAttributeValue(SYSTEM_NAME));
        }
        if (sl.size() >= 3 && sl.get(2) != null) {
            bb.setSensor3(sl.get(2).getAttributeValue(SYSTEM_NAME));
        }
        if (sl.size() >= 4 && sl.get(3) != null) {
            bb.setSensor4(sl.get(3).getAttributeValue(SYSTEM_NAME));
        }
        if (sl.size() >= 5 && sl.get(4) != null) {
            bb.setSensor5(sl.get(4).getAttributeValue(SYSTEM_NAME));
        }
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        log.error("load(Element, Object) called unexpectedly");
    }

    @Override
    public int loadOrder() {
        return Manager.BLOCKBOSS;
    }

    private static final Logger log = LoggerFactory.getLogger(BlockBossLogicProviderXml.class);

}

package jmri.jmrit.blockboss.configurexml;

import java.util.Enumeration;
import java.util.List;

import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.*;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BlockBossLogicXml extends jmri.configurexml.AbstractXmlAdapter {

    public BlockBossLogicXml() {
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

        Enumeration<BlockBossLogic> e = BlockBossLogic.entries();
        if (!e.hasMoreElements()) {
            return null;  // nothing to write!
        }
        Element blocks = new Element("signalelements");
        blocks.setAttribute("class", this.getClass().getName());

        while (e.hasMoreElements()) {
            BlockBossLogic p = e.nextElement();
            Element block = new Element("signalelement");
            block.setAttribute("signal", p.getDrivenSignal());
            block.setAttribute("mode", "" + p.getMode());

            if (p.getApproachSensor1() != null) {
                block.setAttribute("approachsensor1", p.getApproachSensor1());
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
                block.setAttribute("watchedturnout", p.getTurnout());
            }
            if (p.getWatchedSignal1() != null) {
                block.setAttribute("watchedsignal1", p.getWatchedSignal1());
            }
            if (p.getWatchedSignal1Alt() != null) {
                block.setAttribute("watchedsignal1alt", p.getWatchedSignal1Alt());
            }
            if (p.getWatchedSignal2() != null) {
                block.setAttribute("watchedsignal2", p.getWatchedSignal2());
            }
            if (p.getWatchedSignal2Alt() != null) {
                block.setAttribute("watchedsignal2alt", p.getWatchedSignal2Alt());
            }
            if (p.getWatchedSensor1() != null) {
                block.setAttribute("watchedsensor1", p.getWatchedSensor1());
            }
            if (p.getWatchedSensor1Alt() != null) {
                block.setAttribute("watchedsensor1alt", p.getWatchedSensor1Alt());
            }
            if (p.getWatchedSensor2() != null) {
                block.setAttribute("watchedsensor2", p.getWatchedSensor2());
            }
            if (p.getWatchedSensor2Alt() != null) {
                block.setAttribute("watchedsensor2alt", p.getWatchedSensor2Alt());
            }

            block.setAttribute("limitspeed1", "" + p.getLimitSpeed1());
            block.setAttribute("limitspeed2", "" + p.getLimitSpeed2());
            if (p.getRestrictingSpeed1())
                block.setAttribute("restrictingspeed1", "" + p.getRestrictingSpeed1());
            if (p.getRestrictingSpeed2())
                block.setAttribute("restrictingspeed2", "" + p.getRestrictingSpeed2());
            block.setAttribute("useflashyellow", "" + p.getUseFlash());
            block.setAttribute("distantsignal", "" + p.getDistantSignal());

            // add comment, if present
            if (p.getComment() != null) {
                Element c = new Element("comment");
                c.addContent(p.getComment());
                block.addContent(c);
            }

            blocks.addContent(block);

        }

        return blocks;
    }

    Element storeSensor(String name) {
        Element e = new Element("sensorname");
        e.addContent(name);
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        List<Element> l = shared.getChildren("signalelement");

        // try old format if there are no new entries
        // this is for backward compatibility only
        if (l.size() == 0) {
            l = shared.getChildren("block");
        }

        // process each item
        for (int i = 0; i < l.size(); i++) {
            Element block = l.get(i);
            BlockBossLogic bb = null;
            try {
                String signalName = block.getAttributeValue("signal");
                if (signalName == null || signalName.isEmpty()) {
                    // this is an error
                    log.error("Ignoring a <signalelement> element with no signal attribute value");
                    break;
                }
                if (InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName) == null) {
                    // this is an error
                    log.error("SignalHead {} not defined, <signalelement> element referring to it is ignored", signalName);
                    break;
                }
                bb = BlockBossLogic.getStoppedObject(signalName);
            } catch (Exception e) {
                log.error("An error occurred trying to find the signal for the signal elements for " + block.getAttributeValue("signal"), e);
                result = false;
            }
            if (bb != null) {
                if (block.getAttribute("approachsensor1") != null) {
                    try {
                        bb.setApproachSensor1(block.getAttributeValue("approachsensor1"));
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred loading the approach sensor for the signal elements for " + bb.getDrivenSignal());
                        result = false;
                    }
                }
                if (block.getAttribute("watchedsensor") != null) {   // for older XML files
                    try {
                        bb.setSensor1(block.getAttributeValue("watchedsensor"));
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred loading the watched sensor in the SSL for " + bb.getDrivenSignal());
                        result = false;
                    }
                }

                // old form of sensors with system names
                List<Element> sl = block.getChildren("sensor");
                try {
                    if (sl.size() >= 1 && sl.get(0) != null) {
                        bb.setSensor1(sl.get(0).getAttributeValue("systemName"));
                    }
                    if (sl.size() >= 2 && sl.get(1) != null) {
                        bb.setSensor2(sl.get(1).getAttributeValue("systemName"));
                    }
                    if (sl.size() >= 3 && sl.get(2) != null) {
                        bb.setSensor3(sl.get(2).getAttributeValue("systemName"));
                    }
                    if (sl.size() >= 4 && sl.get(3) != null) {
                        bb.setSensor4(sl.get(3).getAttributeValue("systemName"));
                    }
                    if (sl.size() >= 5 && sl.get(4) != null) {
                        bb.setSensor5(sl.get(4).getAttributeValue("systemName"));
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred loading the sensors list in the SSL");
                    result = false;
                }
                // new form of sensors with system names
                sl = block.getChildren("sensorname");
                try {
                    if (sl.size() >= 1 && sl.get(0) != null) {
                        bb.setSensor1(sl.get(0).getText());
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred loading the sensor1 list in the SSL for " + bb.getDrivenSignal());
                    result = false;
                }

                try {
                    if (sl.size() >= 2 && sl.get(1) != null) {
                        bb.setSensor2(sl.get(1).getText());
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred loading the sensor2 list in the SSL for " + bb.getDrivenSignal());
                    result = false;
                }

                try {
                    if (sl.size() >= 3 && sl.get(2) != null) {
                        bb.setSensor3(sl.get(2).getText());
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred loading the sensor3 list in the SSL for " + bb.getDrivenSignal());
                    result = false;
                }

                try {
                    if (sl.size() >= 4 && sl.get(3) != null) {
                        bb.setSensor4(sl.get(3).getText());
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred loading the sensor4 list in the SSL for " + bb.getDrivenSignal());
                    result = false;
                }

                try {
                    if (sl.size() >= 5 && sl.get(4) != null) {
                        bb.setSensor5(sl.get(4).getText());
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred loading the sensor5 list in the SSL for " + bb.getDrivenSignal());
                    result = false;
                }

                try {
                    bb.setMode(block.getAttribute("mode").getIntValue());
                    if (block.getAttribute("distantsignal") != null) {
                        bb.setDistantSignal(block.getAttribute("distantsignal").getBooleanValue());
                    }
                    if (block.getAttribute("limitspeed1") != null) {
                        bb.setLimitSpeed1(block.getAttribute("limitspeed1").getBooleanValue());
                    }
                    if (block.getAttribute("restrictingspeed1") != null) {
                        bb.setRestrictingSpeed1(block.getAttribute("restrictingspeed1").getBooleanValue());
                    }
                    if (block.getAttribute("limitspeed2") != null) {
                        bb.setLimitSpeed2(block.getAttribute("limitspeed2").getBooleanValue());
                    }
                    if (block.getAttribute("restrictingspeed2") != null) {
                        bb.setRestrictingSpeed2(block.getAttribute("restrictingspeed2").getBooleanValue());
                    }
                    try {
                        if (block.getAttribute("watchedturnout") != null) {
                            bb.setTurnout(block.getAttributeValue("watchedturnout"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched turnout (" + block.getAttributeValue("watchedturnout") + ")element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }

                    try {
                        if (block.getAttribute("watchedsignal1") != null) {
                            bb.setWatchedSignal1(block.getAttributeValue("watchedsignal1"),
                                    block.getAttribute("useflashyellow").getBooleanValue());
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched signal 1 (" + block.getAttributeValue("watchedsignal1") + ")element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }

                    try {
                        if (block.getAttribute("watchedsignal1alt") != null) {
                            bb.setWatchedSignal1Alt(block.getAttributeValue("watchedsignal1alt"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched signal 1 alt (" + block.getAttributeValue("watchedsignal1alt") + ")element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }

                    try {
                        if (block.getAttribute("watchedsignal2") != null) {
                            bb.setWatchedSignal2(block.getAttributeValue("watchedsignal2"));
                        }

                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched signal 2 (" + block.getAttributeValue("watchedsignal2") + ")element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }

                    try {
                        if (block.getAttribute("watchedsignal2alt") != null) {
                            bb.setWatchedSignal2Alt(block.getAttributeValue("watchedsignal2alt"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched signal 2 alt (" + block.getAttributeValue("watchedsignal2alt") + ") element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }

                    try {
                        if (block.getAttribute("watchedsensor1") != null) {
                            bb.setWatchedSensor1(block.getAttributeValue("watchedsensor1"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched sensor 1 (" + block.getAttributeValue("watchedsensor1") + ") element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }

                    try {
                        if (block.getAttribute("watchedsensor1alt") != null) {
                            bb.setWatchedSensor1Alt(block.getAttributeValue("watchedsensor1alt"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched sensor 1 alt (" + block.getAttributeValue("watchedsensor1alt") + ") element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }
                    try {
                        if (block.getAttribute("watchedsensor2") != null) {
                            bb.setWatchedSensor2(block.getAttributeValue("watchedsensor2"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched sensor 2 (" + block.getAttributeValue("watchedsensor2") + ") element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }
                    try {
                        if (block.getAttribute("watchedsensor2alt") != null) {
                            bb.setWatchedSensor2Alt(block.getAttributeValue("watchedsensor2alt"));
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        log.error("An error occurred in retrieving the watched sensor 2 alt (" + block.getAttributeValue("watchedsensor2alt") + ")element attribute list for " + bb.getDrivenSignal());
                        result = false;
                    }
                    // load comment, if present
                    String c = block.getChildText("comment");
                    if (c != null) {
                        bb.setComment(c);
                    }

                } catch (org.jdom2.DataConversionException e) {
                    log.warn("error reading blocks from file" + e);
                    result = false;
                } catch (java.lang.IllegalArgumentException e) {
                    log.error("An error occurred in the signal element attribute list");
                    result = false;
                }
                try {
                    bb.retain();
                    bb.start();
                } catch (java.lang.NullPointerException e) {
                    log.error("An error occurred trying to start the signal logic " + bb.getDrivenSignal());
                    result = false;
                }
            }
        }
        return result;
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
        return jmri.Manager.BLOCKBOSS;
    }

    private final static Logger log = LoggerFactory.getLogger(BlockBossLogicXml.class);

}

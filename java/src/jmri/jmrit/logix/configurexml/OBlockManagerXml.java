package jmri.jmrit.logix.configurexml;

import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import javax.swing.JOptionPane;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Path;
import jmri.Reporter;
import jmri.Turnout;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.WarrantTableAction;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring the
 * OBlockManager.
 * <p>
 * Typically, a subclass will just implement the load(Element oblocks)
 * class, relying on implementation here to load the individual oblock
 * objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2009
 */
public class OBlockManagerXml // extends XmlFile
        extends jmri.configurexml.AbstractXmlAdapter {

    public OBlockManagerXml() {
    }

    /**
     * Store the contents of a OBlockManager.
     *
     * @param o Object to store, of type BlockManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element blocks = new Element("oblocks");
        blocks.setAttribute("class", "jmri.jmrit.logix.configurexml.OBlockManagerXml");
        OBlockManager obm = (OBlockManager) o;
        if (obm != null) {
            SortedSet<OBlock> oblockList = obm.getNamedBeanSet();
            // don't return an element if there are no oblocks to include
            if (oblockList.isEmpty()) {
                return null;
            }
            for (OBlock block : oblockList) {
                String sName = block.getSystemName();
                String uName = block.getUserName();
                log.debug("OBlock: sysName= {}, userName= {}", sName, uName);
                Element elem = new Element("oblock");
                elem.setAttribute("systemName", sName);
                if (uName != null && !uName.isEmpty()) {
                    elem.setAttribute("userName", uName); // doing this for compatibility during 2.9.* series
                    elem.addContent(new Element("userName").addContent(uName));
                }
                String comment = block.getComment();
                if (comment != null && !comment.isEmpty()) {
                    Element c = new Element("comment");
                    c.addContent(comment);
                    elem.addContent(c);
                }
                elem.setAttribute("length", "" + block.getLengthMm());
                elem.setAttribute("units", block.isMetric() ? "true" : "false");
                elem.setAttribute("curve", "" + block.getCurvature());
                if (block.getNamedSensor() != null) {
                    Element se = new Element("sensor");
                    se.setAttribute("systemName", block.getNamedSensor().getName());
                    elem.addContent(se);
                }
                if (block.getNamedErrorSensor() != null) {
                    Element se = new Element("errorSensor");
                    se.setAttribute("systemName", block.getNamedErrorSensor().getName());
                    elem.addContent(se);
                }
                if (block.getReporter() != null) {
                    Element se = new Element("reporter");
                    se.setAttribute("systemName", block.getReporter().getSystemName());
                    se.setAttribute("reportCurrent", block.isReportingCurrent() ? "true" : "false");
                    elem.addContent(se);
                }
                elem.setAttribute("permissive", block.getPermissiveWorking() ? "true" : "false");
                elem.setAttribute("speedNotch", block.getBlockSpeed());

                List<Path> paths = block.getPaths();
                for (Path op : paths) {
                    elem.addContent(storePath((OPath) op));
                }
                List<Portal> portals = block.getPortals();
                for (Portal po : portals) {
                    elem.addContent(storePortal(po));
                }
                // and put this element out
                blocks.addContent(elem);
            }
        }
        return blocks;
    }

    static private Element storePortal(Portal portal) {
        Element elem = new Element("portal");
        elem.setAttribute("portalName", portal.getName());
        OBlock block = portal.getFromBlock();
        if (block != null) {
            Element fromElem = new Element("fromBlock");
            fromElem.setAttribute("blockName", block.getSystemName());
            List<OPath> paths = portal.getFromPaths();
            if (paths != null) {
                for (OPath path : paths) {
                    fromElem.addContent(storePathKey(path));
                }
            }
            elem.addContent(fromElem);
        } else {
            log.error("Portal \"{}\" has no fromBlock!", portal.getName());
        }
        NamedBean signal = portal.getFromSignal();
        if (signal != null) {
            Element fromElem = new Element("fromSignal");
            fromElem.setAttribute("signalName", signal.getSystemName());
            fromElem.setAttribute("signalDelay", "" + portal.getFromSignalOffset());
            elem.addContent(fromElem);
        }
        block = portal.getToBlock();
        if (block != null) {
            Element toElem = new Element("toBlock");
            toElem.setAttribute("blockName", block.getSystemName());
            List<OPath> paths = portal.getToPaths();
            if (paths != null) {
                for (OPath path : paths) {
                    toElem.addContent(storePathKey(path));
                }
            }
            elem.addContent(toElem);
        } else {
            log.error("Portal \"{}\" has no toBlock!", portal.getName());
        }
        signal = portal.getToSignal();
        if (signal != null) {
            Element toElem = new Element("toSignal");
            toElem.setAttribute("signalName", signal.getSystemName());
            toElem.setAttribute("signalDelay", "" + portal.getToSignalOffset());
            elem.addContent(toElem);
        }
        return elem;
    }   // storePortal

    /**
     * Key is sufficient to mark the Portal's knowledge of the path. Full path
     * info will get loaded from the HashMap.
     */
    static private Element storePathKey(OPath path) {
        Element elem = new Element("path");
        elem.setAttribute("pathName", path.getName());
        elem.setAttribute("blockName", "" + path.getBlock().getSystemName());
        return elem;
    }

    static private Element storePath(OPath path) {
        Element elem = new Element("path");
        elem.setAttribute("pathName", path.getName());
        elem.setAttribute("blockName", "" + path.getBlock().getSystemName());
        Portal portal = path.getFromPortal();
        if (portal != null) {
            elem.setAttribute("fromPortal", portal.getName());
        }
        portal = path.getToPortal();
        if (portal != null) {
            elem.setAttribute("toPortal", portal.getName());
        }
        List<BeanSetting> list = path.getSettings();
        for (BeanSetting bs : list) {
            Element e = new Element("setting");
            e.setAttribute("turnout", bs.getBeanName());
            e.setAttribute("set", "" + bs.getSetting());
            elem.addContent(e);
        }
        elem.setAttribute("fromDirection", "" + path.getFromBlockDirection());
        elem.setAttribute("toDirection", "" + path.getToBlockDirection());
        // get actual object stored length.
        elem.setAttribute("length", "" + path.getLength());
        return elem;
    }

    /**
     * Due to the forward and backward referencing among OBlock, OPath and
     * Portal no precedence order exists to fully create these objects in one
     * pass. The unique naming of these objects allows the use of Hashmaps to
     * hold them for update.
     */
    private HashMap<String, OBlock> _blockMap;
    private HashMap<String, OPath> _pathMap;
    private OBlockManager _manager;
    private PortalManager _portalMgr;

    private OBlock getBlock(String sysName) {
        OBlock block = _blockMap.get(sysName);
        if (block == null) {
            try {
                block = _manager.provideOBlock(sysName);
                log.debug("found OBlock: ({}) {}", sysName, block);
            } catch (IllegalArgumentException ex) {
                block = _manager.createNewOBlock(sysName, null);
                log.debug("create OBlock: ({})", sysName);
            }
            _blockMap.put(sysName, block);
        }
        return block;
    }

    private OPath getPath(OBlock block, String name) {
        String key = block.getSystemName() + name;
        OPath path = _pathMap.get(key);
        if (path == null) {
            path = new OPath(block, name);
            _pathMap.put(key, path);
            log.debug("create OPath: \"{}\" in block {}", name, block.getSystemName());
        }
        return path;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        _blockMap = new HashMap<>();
        _pathMap = new HashMap<>();
        _manager = InstanceManager.getDefault(OBlockManager.class);
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        List<Element> blockList = shared.getChildren("oblock");
        log.debug("Found {} OBlock objects", blockList.size());
        for (Element bl : blockList) {
            loadBlock(bl);
        }
        // Build data structure for blocks to know with whom they share turnouts.
        // check whether any turnouts are shared between two blocks;
        for (OBlock oblock : _manager.getNamedBeanSet()) {
            WarrantTableAction.checkSharedTurnouts(oblock);
        }
        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("load called. Invalid method.");
    }

    private void loadBlock(Element elem) {
        if (elem.getAttribute("systemName") == null) {
            log.error("unexpected null for block systemName elem= ", elem);
            return;
        }
        String sysName = elem.getAttribute("systemName").getValue();
        String userName = null;
        if (elem.getAttribute("userName") != null) {
            userName = elem.getAttribute("userName").getValue();
        }
        log.debug("Load block sysName= {}, userName= {}", sysName, userName);
        // Portal may have already created a skeleton of this block
        OBlock block = getBlock(sysName);
        if (block == null) {
            log.error("Null block!? sysName= {}, userName= {}", sysName, userName);
            return;
        }
        block.setUserName(userName);
        String c = elem.getChildText("comment");
        if (c != null) {
            block.setComment(c);
        }
        if (elem.getAttribute("units") != null) {
            block.setMetricUnits(elem.getAttribute("units").getValue().equals("true"));
        } else {
            block.setMetricUnits(false);
        }
        if (elem.getAttribute("length") != null) {
            block.setLength(Float.valueOf(elem.getAttribute("length").getValue()).floatValue());
        }
        if (elem.getAttribute("curve") != null) {
            block.setCurvature(Integer.parseInt((elem.getAttribute("curve")).getValue()));
        }
        List<Element> sensors = elem.getChildren("sensor");
        if (sensors.size() > 1) {
            log.error("More than one sensor present: {}", sensors.size());
        }
        if (sensors.size() > 0) {
            // sensor
            String name = sensors.get(0).getAttribute("systemName").getValue();
            block.setSensor(name);
        }
        Element errSensor = elem.getChild("errorSensor");
        if (errSensor != null) {
            // sensor
            String name = errSensor.getAttribute("systemName").getValue();
            block.setErrorSensor(name);
        }
        Element reporter = elem.getChild("reporter");
        if (reporter != null) {
            // sensor
            String name = reporter.getAttribute("systemName").getValue();
            try {
                Reporter rep = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(name);
                if (rep != null) {
                    block.setReporter(rep);
                }
            } catch (Exception ex) {
                log.error("No Reporter named \"{}\" found. threw exception: {}", name,  ex);
            }
            if (reporter.getAttribute("reportCurrent") != null) {
                block.setReportingCurrent(reporter.getAttribute("reportCurrent").getValue().equals("true"));
            } else {
                block.setReportingCurrent(false);
            }
        }
        if (elem.getAttribute("permissive") != null) {
            block.setPermissiveWorking(elem.getAttribute("permissive").getValue().equals("true"));
        } else {
            block.setPermissiveWorking(false);
        }
        if (elem.getAttribute("speedNotch") != null) {
            try {
                block.setBlockSpeed(elem.getAttribute("speedNotch").getValue());
            } catch (jmri.JmriException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + elem.getAttribute("speedNotch").getValue());
            }
        }

        List<Element> portals = elem.getChildren("portal");
        for (Element po : portals) {
            Portal portal = loadPortal(po);
            if (portal != null) {
                block.addPortal(portal);
            }
        }

        List<Element> paths = elem.getChildren("path");
        for (Element pa : paths) {
            if (!block.addPath(loadPath(pa, block))) {
                log.error("load: block \"{}\" failed to add path \"{}\" in block \"{}\"",
                        sysName, pa.getName(), block.getSystemName());
            }
        }
    }   // loadBlock

    private Portal loadPortal(Element elem) {
        String userName = elem.getAttribute("portalName").getValue();
        String fromBlockName = null;
        String toBlockName = null;
        // Portals must have user names.
        Portal portal = _portalMgr.getPortal(userName);
        if (portal != null) {
            OBlock block = portal.getFromBlock();
            if (block != null) {
                fromBlockName = block.getSystemName();
            }
            block = portal.getToBlock();
            if (block != null) {
                toBlockName = block.getSystemName();
            }
        } else {
            portal = _portalMgr.providePortal(userName);
        }
        if (portal == null) {
            log.error("unable to create Portal ({}) elem attrs= {}",
                    userName, elem.getAttributes());
            return null;
        }
        log.debug("create Portal: ({})", userName);

        OBlock fromBlock = null;
        Element eFromBlk = elem.getChild("fromBlock");
        if (eFromBlk != null && eFromBlk.getAttribute("blockName") != null) {
            String name = eFromBlk.getAttribute("blockName").getValue();
            if (fromBlockName != null && !fromBlockName.equals(name)) {
                log.error("Portal user name \"{}\" has conflicting fromBlock \"{}\". Should be \"{}\"",
                        userName, fromBlockName, name);
            } else {
                fromBlock = getBlock(name);
                if (fromBlock != null) {
                    portal.setFromBlock(fromBlock, false);
                    fromBlock.addPortal(portal);

                    List<Element> ePathsFromBlock = eFromBlk.getChildren("path");
                    for (Element e : ePathsFromBlock) {
                        String pathName = e.getAttribute("pathName").getValue();
                        String blockName = e.getAttribute("blockName").getValue();
                        log.debug("Load portal= \"{}\" fromBlock= {}, pathName= {}, blockName= {}",
                                userName, fromBlock.getSystemName(), pathName, blockName);
                        OPath path = getPath(fromBlock, pathName);
                        portal.addPath(path);
                    }
                }
            }
        } else {
            log.error("Portal \"{}\" has no fromBlock!", userName);
        }

        OBlock toBlock = null;
        Element eToBlk = elem.getChild("toBlock");
        if (eToBlk != null && eToBlk.getAttribute("blockName") != null) {
            String name = eToBlk.getAttribute("blockName").getValue();
            if (toBlockName != null && !toBlockName.equals(name)) {
                log.error("Portal user name \"{}\" has conflicting toBlock \"{}\". Should be \"{}\"",
                        userName, toBlockName, name);
            } else {
                toBlock = getBlock(name);
                if (toBlock != null) {
                    portal.setToBlock(toBlock, false);
                    toBlock.addPortal(portal);

                    List<Element> ePathsToBlock = eToBlk.getChildren("path");
                    for (Element ePath : ePathsToBlock) {
                        String pathName = ePath.getAttribute("pathName").getValue();
                        String blockName = ePath.getAttribute("blockName").getValue();
                        log.debug("Load portal= \"{}\" toBlock= {}, pathName= {}, blockName= {}", userName, toBlock.getSystemName(), pathName, blockName);
                        // path is in the toBlock
                        OPath path = getPath(toBlock, pathName);
                        portal.addPath(path);
                    }
                }
            }
        } else {
            log.error("Portal \"{}\" has no toBlock!",  userName);
        }
        Element eSignal = elem.getChild("fromSignal");
        if (eSignal != null) {
            String name = eSignal.getAttribute("signalName").getValue();
            float length = 0.0f;
            try {
                Attribute attr = eSignal.getAttribute("signalDelay");
                if (attr != null) {
                    length = attr.getFloatValue();
                }
            } catch (org.jdom2.DataConversionException e) {
                log.error("Could not parse signalDelay fromSignal ({}) in portal ({})", name, userName);
            }
            portal.setProtectSignal(Portal.getSignal(name), length, toBlock);
        }
        eSignal = elem.getChild("toSignal");
        if (eSignal != null) {
            String name = eSignal.getAttribute("signalName").getValue();
            float length = 0.0f;
            try {
                Attribute attr = eSignal.getAttribute("signalDelay");
                if (attr != null) {
                    length = attr.getFloatValue();
                }
            } catch (org.jdom2.DataConversionException e) {
                log.error("Could not parse signalDelay toSignal ({}) in portal ({})", name, userName);
            }
            portal.setProtectSignal(Portal.getSignal(name), length, fromBlock);
        }

        log.debug("End Load portal {}", userName);
        return portal;
    }   // loadPortal

    OPath loadPath(Element elem, OBlock block) {
        String pName = elem.getAttribute("pathName").getValue();
        OPath path = getPath(block, pName);
        try {
            Attribute attr = elem.getAttribute("fromDirection");
            if (attr != null) {
                path.setFromBlockDirection(attr.getIntValue());
            }
            attr = elem.getAttribute("toDirection");
            if (attr != null) {
                path.setToBlockDirection(attr.getIntValue());
            }
            attr =  elem.getAttribute("length");
            if (attr != null) {
                path.setLength(attr.getFloatValue());
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("Could not parse attribute of path \"{}\" in block \"{}\")",
                    pName, block.getSystemName());
        }

        Attribute attr = elem.getAttribute("fromPortal");
        if (attr != null) {
            Portal portal = _portalMgr.providePortal(attr.getValue());
            if (portal != null) {
                path.setFromPortal(portal);
                portal.addPath(path);
            }
        }
        attr = elem.getAttribute("toPortal");
        if (attr != null) {
            Portal portal = _portalMgr.providePortal(attr.getValue());
            if (portal != null) {
                path.setToPortal(portal);
                portal.addPath(path);
            }
        }

        List<Element> settings = elem.getChildren("setting");
        log.debug("Path \"{}\" has {} settings.", pName, settings.size());
        java.util.HashSet<String> turnouts = new java.util.HashSet<>();
        int dups = 0;
        for (Element setElem : settings) {
            int setting = 0;
            try {
                setting = setElem.getAttribute("set").getIntValue();
            } catch (org.jdom2.DataConversionException e) {
                log.error("Could not parse 'set' attribute for path path \"{}\" in block \"{}\"",
                        pName,  block.getSystemName());
            }
            String sysName = setElem.getAttribute("turnout").getValue();
            if (!turnouts.contains(sysName)) {
                Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout(sysName);
                turnouts.add(sysName);
                BeanSetting bs = new BeanSetting(to, sysName, setting);
                path.addSetting(bs);
            } else {
                dups++;
            }
        }
        if (dups > 0) {
            log.warn("{} duplicate settings not loaded for path \"{}\"", dups, pName);
        }
        return path;
    }   // loadPath

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(OBlockManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(OBlockManagerXml.class);

}

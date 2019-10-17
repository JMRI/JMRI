package jmri.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Reporter;
import jmri.Turnout;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistency implementation for BlockManager persistence.
 * <p>
 * The Block objects are not yet read in, pending a reliable write out!
 * <p>
 * Every block is written twice. First, the list of blocks is written without
 * contents, so that we're sure they're all created on read-back. Then, they're
 * written out again with contents, including the block references in the path
 * elements.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @since 2.1.2
 */
public class BlockManagerXml extends jmri.managers.configurexml.AbstractMemoryManagerConfigXML {

    public BlockManagerXml() {
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param memories The top-level element being created
     */
    @Override
    public void setStoreElementClass(Element memories) {
        memories.setAttribute("class", "jmri.configurexml.BlockManagerXml");
    }

    /**
     * Store the contents of a BlockManager.
     *
     * @param o Object to store, of type BlockManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element blocks = new Element("blocks");
        setStoreElementClass(blocks);
        BlockManager bm = (BlockManager) o;
        if (bm != null) {
            //TODO: dead code strip: (don't sort - this is so JMRI preserves the order of things in the file)
            //AlphanumComparator ac = new AlphanumComparator();
            //List<String> contents = bm.getSystemNameList();
            //Collections.sort(contents, (String s1, String s2) -> ac.compare(s1, s2));
            //java.util.Iterator<String> iter = contents.iterator();

            SortedSet<Block> blkList = bm.getNamedBeanSet();
            // don't return an element if there are no blocks to include
            if (blkList.isEmpty()) {
                return null;
            }

            blocks.addContent(new Element("defaultspeed").addContent(bm.getDefaultSpeed()));
            //TODO: The block info saved includes paths that at load time might
            // reference blocks that haven't yet been loaded. In the past the
            // workaround was to write all the blocks out twice: once with just
            // the system and user names and then again with everything so that
            // at load time the first set of (minimum) blocks would create all 
            // the blocks before the second pass loaded the path information.
            // To remove the necessity of doing this (and having duplicate
            // blocks in the saved file) we've changed the load routine to make 
            // two passes: once only creating the blocks (with system & user 
            // names) and then a second pass with everything (including the 
            // paths). At some point in the future (after a major release?) we 
            // can remove writing the first set of blocks without contents 
            // (and this (now way overly verbose) comment).
            if (true) {
                // write out first set of blocks without contents
                for (Block b : blkList) {
                    try {
                        String bName = b.getSystemName();
                        Element elem = new Element("block");
                        elem.addContent(new Element("systemName").addContent(bName));

                        // As a work-around for backward compatibility, store systemName as attribute.
                        // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                        elem.setAttribute("systemName", bName);

                        // the following null check is to catch a null pointer exception that sometimes was found to happen
                        String uName = b.getUserName();
                        if ((uName != null) && (!uName.isEmpty())) {
                            elem.addContent(new Element("userName").addContent(uName));
                        }
                        log.debug("initial store Block {}", bName);

                        // and put this element out
                        blocks.addContent(elem);
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                }
            }

            // write out with contents
            for (Block b : blkList) {
                String bName = b.getSystemName();
                String uName = b.getUserName();
                if (uName == null) {
                    uName = "";
                }
                Element elem = new Element("block");
                elem.addContent(new Element("systemName").addContent(bName));

                // As a work-around for backward compatibility, store systemName as attribute.
                // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                elem.setAttribute("systemName", bName);
                log.debug("second store Block {}: {}", bName, uName);

                // add length and curvature attributes
                elem.setAttribute("length", Float.toString(b.getLengthMm()));
                elem.setAttribute("curve", Integer.toString(b.getCurvature()));

                // store common parts
                storeCommon(b, elem);

                if ((!b.getBlockSpeed().equals("")) && !b.getBlockSpeed().contains("Global")) {
                    elem.addContent(new Element("speed").addContent(b.getBlockSpeed()));
                }
                String perm = "no";
                if (b.getPermissiveWorking()) {
                    perm = "yes";
                }
                elem.addContent(new Element("permissive").addContent(perm));
                // Add content. First, the sensor
                if (b.getNamedSensor() != null) {
                    elem.addContent(new Element("occupancysensor").addContent(b.getNamedSensor().getName()));
                }

                if (b.getDeniedBlocks().size() > 0) {
                    Element denied = new Element("deniedBlocks");
                    b.getDeniedBlocks().forEach((deniedBlock) -> {
                        denied.addContent(new Element("block").addContent(deniedBlock));
                    });
                    elem.addContent(denied);
                }

                // now the Reporter
                Reporter r = b.getReporter();
                if (r != null) {
                    Element re = new Element("reporter");
                    re.setAttribute("systemName", r.getSystemName());
                    re.setAttribute("useCurrent", b.isReportingCurrent() ? "yes" : "no");
                    elem.addContent(re);
                }

                if (bm.isSavedPathInfo()) {
                    // then the paths
                    List<Path> paths = b.getPaths();
                    for (Path p : paths) {
                        addPath(elem, p);
                    }
                    // and put this element out
                }
                blocks.addContent(elem);
            }
        }
        return blocks;
    }

    private void addPath(Element e, Path p) {
        // for now, persist two directions and a bean setting
        Element pe = new Element("path");
        pe.setAttribute("todir", "" + p.getToBlockDirection());
        pe.setAttribute("fromdir", "" + p.getFromBlockDirection());
        if (p.getBlock() != null) {
            pe.setAttribute("block", "" + p.getBlock().getSystemName());
        }
        List<BeanSetting> l = p.getSettings();
        if (l != null) {
            for (BeanSetting bSet : l) {
                addBeanSetting(pe, bSet);
            }
        }
        e.addContent(pe);
    }

    private void addBeanSetting(Element e, BeanSetting bs) {
        // persist bean name, type and value
        Element bse = new Element("beansetting");
        // for now, assume turnout
        bse.setAttribute("setting", "" + bs.getSetting());
        Element be = new Element("turnout");
        be.setAttribute("systemName", bs.getBeanName());
        bse.addContent(be);
        e.addContent(bse);
    }

    /**
     * Load Blocks into the existing BlockManager.
     * <p>
     * The BlockManager in the InstanceManager is created automatically.
     *
     * @param sharedBlocks  Element containing the block elements to load
     * @param perNodeBlocks Per-node block elements to load
     * @return true if successful
     * @throws jmri.configurexml.JmriConfigureXmlException if error during load
     */
    @Override
    public boolean load(Element sharedBlocks, Element perNodeBlocks) throws JmriConfigureXmlException {
        boolean result = true;
        try {
            if (sharedBlocks.getChild("defaultspeed") != null) {
                String speed = sharedBlocks.getChild("defaultspeed").getText();
                if (speed != null && !speed.equals("")) {
                    InstanceManager.getDefault(jmri.BlockManager.class).setDefaultSpeed(speed);
                }
            }
        } catch (IllegalArgumentException ex) {
            log.error(ex.toString());
        }

        List<Element> list = sharedBlocks.getChildren("block");
        log.debug("Found {} objects", list.size());

        try {
            InstanceManager.getDefault(jmri.BlockManager.class).setDataListenerMute(true);
            // first pass don't load full contents (just create all the blocks)
            for (Element block : list) {
                loadBlock(block, false);
            }

            // second pass load full contents
            for (Element block : list) {
                loadBlock(block, true);
            }
        } finally {
            InstanceManager.getDefault(jmri.BlockManager.class).setDataListenerMute(false);
        }
        
        return result;
    }

    /**
     * Utility method to load the individual Block objects.
     *
     * @param element Element containing one block
     * @throws jmri.configurexml.JmriConfigureXmlException if element contains
     *                                                     malformed or
     *                                                     schematically invalid
     *                                                     XMl
     */
    // default optional contentsFlag parameter to true
    public void loadBlock(Element element) throws JmriConfigureXmlException {
        loadBlock(element, true);
    }

    private void loadBlock(Element element, boolean contentsFlag) throws JmriConfigureXmlException {
        String sysName = getSystemName(element);
        String userName = getUserName(element);
        log.debug("defined Block: ({})({})", sysName, (userName == null ? "<null>" : userName));

        Block block = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(sysName);
        if (block == null) { // create it if doesn't exist
            InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(sysName, userName);
            block = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(sysName);
        }
        if (block == null) {
            log.error("Unable to load block with system name {} and username of {}", sysName, (userName == null ? "<null>" : userName));
            return;
        }
        if (userName != null) {
            block.setUserName(userName);
        }
        if (!contentsFlag) {
            return;
        }
        if (element.getAttribute("length") != null) {
            // load length in millimeters
            block.setLength(Float.parseFloat(element.getAttribute("length").getValue()));
        }
        if (element.getAttribute("curve") != null) {
            // load curve attribute
            block.setCurvature(Integer.parseInt((element.getAttribute("curve")).getValue()));
        }
        try {
            block.setBlockSpeed("Global");
            if (element.getChild("speed") != null) {
                String speed = element.getChild("speed").getText();
                if (speed != null && !speed.equals("") && !speed.contains("Global")) {
                    block.setBlockSpeed(speed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }
        if (element.getChild("permissive") != null) {
            boolean permissive = false;
            if (element.getChild("permissive").getText().equals("yes")) {
                permissive = true;
            }
            block.setPermissiveWorking(permissive);
        }
        Element deniedBlocks = element.getChild("deniedBlocks");
        if (deniedBlocks != null) {
            List<Element> denyBlock = deniedBlocks.getChildren("block");
            for (Element deny : denyBlock) {
                block.addBlockDenyList(deny.getText());
            }
        }
        // load common parts
        loadCommon(block, element);

        // load sensor if present
        List<Element> sensors = element.getChildren("sensor");
        if (sensors.size() > 1) {
            log.error("More than one sensor present: {}", sensors.size());
        }
        if (sensors.size() == 1) {
            //Old method of saving sensors
            if (sensors.get(0).getAttribute("systemName") != null) {
                String name = sensors.get(0).getAttribute("systemName").getValue();
                if (!name.equals("")) {
                    block.setSensor(name);
                }
            }
        }
        if (element.getChild("occupancysensor") != null) {
            String name = element.getChild("occupancysensor").getText();
            if (!name.equals("")) {
                block.setSensor(name);
            }
        }

        // load Reporter if present
        List<Element> reporters = element.getChildren("reporter");
        if (reporters.size() > 1) {
            log.error("More than one reporter present: {}", reporters.size());
        }
        if (reporters.size() == 1) {
            // Reporter
            String name = reporters.get(0).getAttribute("systemName").getValue();
            try {
                Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).provideReporter(name);
                block.setReporter(reporter);
                block.setReportingCurrent(reporters.get(0).getAttribute("useCurrent").getValue().equals("yes"));
            } catch (IllegalArgumentException ex) {
                log.warn("failed to create Reporter \"{}\" during Block load", name);
            }
        }

        // load paths if present
        List<Element> paths = element.getChildren("path");

        int startSize = block.getPaths().size();
        int loadCount = 0;

        for (Element path : paths) {
            if (loadPath(block, path)) {
                loadCount++;
            }
        }

        if ((startSize > 0) && (loadCount > 0)) {
            log.warn("Added {} paths to block {} that already had {} blocks.", loadCount++, sysName, startSize);
        }

        if (startSize + loadCount != block.getPaths().size()) {
            log.error("Started with {} paths in block {}, added {} but final count is {}; something not right.",
                    startSize, sysName, loadCount, block.getPaths().size());
        }
    }

    /**
     * Load path into an existing Block from XML.
     *
     * @param block   Block to receive path
     * @param element Element containing path information
     * @return true if path added to block; false otherwise
     * @throws jmri.configurexml.JmriConfigureXmlException if element contains
     *                                                     malformed or
     *                                                     schematically invalid
     *                                                     XMl
     */
    public boolean loadPath(Block block, Element element) throws JmriConfigureXmlException {
        // load individual path
        int toDir = 0;
        int fromDir = 0;
        try {
            toDir = element.getAttribute("todir").getIntValue();
            fromDir = element.getAttribute("fromdir").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("Could not parse path attribute");
        } catch (NullPointerException e) {
            handleException("Block Path entry in file missing required attribute",
                    null, block.getSystemName(), block.getUserName(), null);
        }

        Block toBlock = null;
        if (element.getAttribute("block") != null) {
            String name = element.getAttribute("block").getValue();
            toBlock = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(name);
        }
        Path path = new Path(toBlock, toDir, fromDir);

        List<Element> settings = element.getChildren("beansetting");
        for (Element setting : settings) {
            loadBeanSetting(path, setting);
        }

        // check if path already in block
        if (!block.hasPath(path)) {
            block.addPath(path);
            return true;
        } else {
            log.debug("Skipping load of duplicate path {}", path);
            return false;
        }
    }

    /**
     * Load BeanSetting into an existing Path.
     *
     * @param path    Path to receive BeanSetting
     * @param element Element containing beansetting information
     */
    public void loadBeanSetting(Path path, Element element) {
        int setting = 0;
        try {
            setting = element.getAttribute("setting").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("Could not parse beansetting attribute");
        }
        List<Element> turnouts = element.getChildren("turnout");
        if (turnouts.size() != 1) {
            log.error("invalid number of turnout element children");
        }
        String name = turnouts.get(0).getAttribute("systemName").getValue();
        try {
            Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
            BeanSetting bs = new BeanSetting(t, name, setting);
            path.addSetting(bs);
        } catch (IllegalArgumentException ex) {
            log.warn("failed to create Turnout \"{}\" during Block load", name);
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.BlockManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(BlockManagerXml.class);

}

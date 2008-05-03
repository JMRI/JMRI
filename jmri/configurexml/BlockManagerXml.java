// BlockManagerXml.java

package jmri.configurexml;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.Turnout;

import java.util.List;

import org.jdom.Element;

/**
 * Persistency implementation for BlockManager persistance.
 * <P>
 * The Block objects are not yet read in, pending a reliable write out!
 * <p>
 * Every block is written twice.  First, the list of blocks is written
 * without contents, so that we're sure they're all created on read-back.
 * Then, they're written out again with contents, including the block references
 * in the path elements.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.3 $
 * @since 2.1.2
 *
 */
public class BlockManagerXml extends AbstractMemoryManagerConfigXML {

    public BlockManagerXml() {
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param memories The top-level element being created
     */
    public void setStoreElementClass(Element memories) {
        memories.setAttribute("class","jmri.configurexml.BlockManagerXml");
    }

    /**
     * Store the contents of a BlockManager.
     *
     * @param o Object to store, of type BlockManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element blocks = new Element("blocks");
        setStoreElementClass(blocks);
        BlockManager tm = (BlockManager) o;
        if (tm!=null) {
            java.util.Iterator iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not blocks to include
            if (!iter.hasNext()) return null;
            
            // write out first set of blocks without contents
             while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                Block b = tm.getBySystemName(sname);
                String uname = b.getUserName();
                Element elem = new Element("block")
                            .setAttribute("systemName", sname);
                if (uname!=null) elem.setAttribute("userName", uname);
                if (log.isDebugEnabled()) log.debug("initial store Block "+sname+":"+uname);
                
                // and put this element out
                blocks.addContent(elem);
            }
           
            // write out again with contents
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                Block b = tm.getBySystemName(sname);
                String uname = b.getUserName();
                Element elem = new Element("block")
                            .setAttribute("systemName", sname);
                if (uname!=null) elem.setAttribute("userName", uname);
                if (log.isDebugEnabled()) log.debug("second store Block "+sname+":"+uname);
                
                // Add content. First, the sensor.
                Sensor s = b.getSensor();
                if (s!=null) {
                    Element se = new Element("sensor");
                    se.setAttribute("systemName", s.getSystemName());
                    elem.addContent(se);
                }
                
                // then the paths
                List paths = b.getPaths();
                for (int i=0; i<paths.size(); i++)
                    addPath(elem, (Path)paths.get(i));
                // and put this element out
                blocks.addContent(elem);
            }
        }
        return blocks;
    }

    void addPath(Element e, Path p) {
        // for now, persist two directions and a bean setting
        Element pe = new Element("path");
        pe.setAttribute("todir", ""+p.getToBlockDirection());
        pe.setAttribute("fromdir", ""+p.getFromBlockDirection());
        pe.setAttribute("block", ""+p.getBlock().getSystemName());
        List l = p.getSettings();
        for (int i=0; i<l.size(); i++)
            addBeanSetting(pe, (BeanSetting)l.get(i));
        e.addContent(pe);
    }
    void addBeanSetting(Element e, BeanSetting bs) {
        // persist bean name, type and value
        Element bse = new Element("beansetting");
        // for now, assume turnout
        bse.setAttribute("setting", ""+bs.getSetting());
        Element be = new Element("turnout");
        be.setAttribute("systemName", bs.getBean().getSystemName());
        bse.addContent(be);
        e.addContent(bse);
    }
    
    /**
     * Load Blocks into the existing BlockManager.
     * <p>
     * The BlockManager in the InstanceManager is created automatically.
     * 
     * @param blocks Element containing the block elements to load.
     */
    public void load(Element blocks) {
        List list = blocks.getChildren("block");
        if (log.isDebugEnabled()) log.debug("Found "+list.size()+" objects");
        BlockManager tm = InstanceManager.blockManagerInstance();

        for (int i=0; i<list.size(); i++) {
            Element block = (Element)list.get(i);
            loadBlock(block);
        }
    }

    /**
     * Utility method to load the individual Block objects.
     * 
     * @param block Element holding one block
     */
    public void loadBlock(Element element) {
            if ( element.getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+element+" "+element.getAttributes());
                return;
            }
            String sysName = element.getAttribute("systemName").getValue();
            String userName = null;
            if (element.getAttribute("userName") != null)
            userName = element.getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("defined Block: ("+sysName+")("+(userName==null?"<null>":userName)+")");

            Block block = InstanceManager.blockManagerInstance().getBlock(sysName);
            if (block==null) { // create it if doesn't exist
                InstanceManager.blockManagerInstance().createNewBlock(sysName, userName);
                block = InstanceManager.blockManagerInstance().getBlock(sysName);
            }
            
            // load sensor if present
            List sensors = element.getChildren("sensor");
            if (sensors.size()>1) log.error("More than one sensor present: "+sensors.size());
            if (sensors.size()==1) {
                // sensor
                String name = ((Element)sensors.get(0)).getAttribute("systemName").getValue();
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(name);
                block.setSensor(sensor);
            }
            
            // load paths if present
            List paths = element.getChildren("path");
            for (int i=0; i<paths.size(); i++) {
                Element path = (Element)paths.get(i);
                loadPath(block, path);
            }
    }

    /**
     * Load path into an existing Block.
     *
     * @param block Block to receive path
     * @param element Element containing path information
     */
    public void loadPath(Block block, Element element) {
        // load individual path
        int toDir = 0;
        int fromDir = 0;
        try {
            toDir = element.getAttribute("todir").getIntValue();
            fromDir = element.getAttribute("fromdir").getIntValue();
        } catch (org.jdom.DataConversionException e) {
            log.error("Could not parse path attribute");
        }
        
        Block toBlock = null;
        if (element.getAttribute("block")!=null) {
            String name = element.getAttribute("block").getValue();
            toBlock = InstanceManager.blockManagerInstance().getBlock(name);
        }
        Path path = new Path(toBlock, toDir, fromDir);
        
        List settings = element.getChildren("beansetting");
        for (int i=0; i<settings.size(); i++) {
            Element setting = (Element)settings.get(i);
            loadBeanSetting(path, setting);
        }
        
        block.addPath(path);
    }

    /**
     * Load BeanSetting into an existing Path.
     * 
     * @param path Path to receive BeanSetting
     * @param element Element containing beansetting information
     */
    public void loadBeanSetting(Path path, Element element) {
        int setting = 0;
        try {
            setting = element.getAttribute("setting").getIntValue();
        } catch (org.jdom.DataConversionException e) {
            log.error("Could not parse beansetting attribute");
        }
        List turnouts = element.getChildren("turnout");
        if (turnouts.size()!=1) log.error("invalid number of turnout element children");
        String name = ((Element)turnouts.get(0)).getAttribute("systemName").getValue();
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        
        BeanSetting bs = new BeanSetting(t, setting);
        path.addSetting(bs);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockManagerXml.class.getName());
}
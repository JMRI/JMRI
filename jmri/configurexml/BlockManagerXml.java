// BlockManagerXml.java

package jmri.configurexml;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;

import java.util.List;

import org.jdom.Element;

/**
 * Persistency implementation for BlockManager persistance.
 * <P>
 * The Block objects are not yet read in, pending a reliable write out!
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
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
     * Default implementation for storing the contents of a
     * BlockManager
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

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Block b = tm.getBySystemName(sname);
                String uname = b.getUserName();
                Element elem = new Element("block")
                            .setAttribute("systemName", sname);
                if (uname!=null) elem.setAttribute("userName", uname);
                log.debug("store Block "+sname+":"+uname);
                
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
     * @param blocks Top level Element to unpack.
     */
    public void load(Element blocks) {
        // load individual blocks
        //
        // This is commented out until these are complete
        //loadBlocks(blocks);
    }

    /**
     * Utility method to load the individual Block objects.
     * @param blocks Element containing the block elements to load.
     */
    public void loadBlocks(Element blocks) {
        List list = blocks.getChildren("block");
        if (log.isDebugEnabled()) log.debug("Found "+list.size()+" objects");
        BlockManager tm = InstanceManager.blockManagerInstance();

        for (int i=0; i<list.size(); i++) {
            if ( ((Element)(list.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(list.get(i)))+" "+((Element)(list.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(list.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(list.get(i))).getAttribute("userName") != null)
            userName = ((Element)(list.get(i))).getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("create Block: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            tm.createNewBlock(sysName, userName);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockManagerXml.class.getName());
}
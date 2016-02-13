package jmri.jmrit.logix.configurexml;

import java.awt.GraphicsEnvironment;
import java.util.Iterator;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.logix.BlockOrder;
import jmri.jmrit.logix.NXFrame;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.ThrottleSetting;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for
 * configuring the CatalogTreeManager.
 * <P>
 * Typically, a subclass will just implement the load(Element catalogTree)
 * class, relying on implementation here to load the individual CatalogTree objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2009
 * 
 */
public class WarrantManagerXml //extends XmlFile
                    extends jmri.configurexml.AbstractXmlAdapter {

    public WarrantManagerXml() {
    }
    
    /**
     * Store the contents of a WarrantManager.
     *
     * @param o Object to store, of type warrantManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element warrants = new Element("warrants");
        warrants.setAttribute("class","jmri.jmrit.logix.configurexml.WarrantManagerXml");
        if (!GraphicsEnvironment.isHeadless()) {
            storeNXParams(warrants);
        }
        WarrantManager manager = (WarrantManager) o;
        Iterator<String> iter = manager.getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sname = iter.next();
            Warrant warrant = manager.getBySystemName(sname);
            String uname = warrant.getUserName();
            if (log.isDebugEnabled())
                log.debug("Warrant: sysName= "+sname+", userName= "+uname);
            Element elem = new Element("warrant");
            elem.setAttribute("systemName", sname);
            if (uname==null) uname = "";
            if (uname.length()>0) {
                elem.setAttribute("userName", uname);
            }
            String comment = warrant.getComment();
            if (comment != null) {
                Element c = new Element("comment");
                c.addContent(comment);
                elem.addContent(c);
            }
            
            List <BlockOrder> orders = warrant.getBlockOrders();
            for (int j=0; j<orders.size(); j++) {
                elem.addContent(storeOrder(orders.get(j), "blockOrder"));
            }
            
            BlockOrder viaOrder = warrant.getViaOrder();
            if (viaOrder!=null) {
                elem.addContent(storeOrder(viaOrder, "viaOrder"));
            }
            BlockOrder avoidOrder = warrant.getAvoidOrder();
            if (avoidOrder!=null) {
                elem.addContent(storeOrder(avoidOrder, "avoidOrder"));
            }

            List <ThrottleSetting> throttleCmds = warrant.getThrottleCommands();
            for (int j=0; j<throttleCmds.size(); j++) {
                elem.addContent(storeCommand(throttleCmds.get(j), "throttleCommand"));
            }

            elem.addContent(storeTrain(warrant, "train"));

            // and put this element out
            warrants.addContent(elem);
        }
        return warrants;
    }

    static Element storeTrain(Warrant warrant, String type) {
        Element elem = new Element(type);
        String str = warrant.getTrainId();
        if (str==null) str = "";
        elem.setAttribute("trainId", str);

        DccLocoAddress addr = warrant.getDccAddress();
        if (addr != null) {
            elem.setAttribute("dccAddress", ""+addr.getNumber());
            elem.setAttribute("dccType", ""+(addr.isLongAddress() ? "L" : "S"));
        }
        elem.setAttribute("runBlind", warrant.getRunBlind()?"true":"false");

        str = warrant.getTrainName();
        if (str==null) str = "";
        elem.setAttribute("trainName", str);
        
        return elem;
    }

    static Element storeOrder(BlockOrder order, String type) {
        Element elem = new Element(type);
        OBlock block = order.getBlock();
        if (block!=null) {
            Element blk = new Element("block");
            blk.setAttribute("systemName", block.getSystemName());
            String uname = block.getUserName();
            if (uname==null) uname = "";
            if (uname.length()>0) {
                blk.setAttribute("userName", uname);
            }
            elem.addContent(blk);
        } else {
            log.error("Null block in BlockOrder!");
        }
        String str = order.getPathName();
        if (str==null) str = "";
        elem.setAttribute("pathName", str);

        str = order.getEntryName();
        if (str==null) str = "";
        elem.setAttribute("entryName", str);

        str = order.getExitName();
        if (str==null) str = "";
        elem.setAttribute("exitName", str);

        return elem;
    }

    static Element storeCommand(ThrottleSetting command, String type) {
        Element elem = new Element(type);

        String time = String.valueOf(command.getTime());
        if (time==null) time = "";
        elem.setAttribute("time", time);

        String str = command.getCommand();
        if (str==null) str = "";
        elem.setAttribute("command", str);

        str = command.getValue();
        if (str==null) str = "";
        elem.setAttribute("value", str);

        str = command.getBlockName();
        if (str==null) str = "";
        elem.setAttribute("block", str);

        return elem;
    }
    
    static void storeNXParams (Element element) {
        if (jmri.InstanceManager.getDefault(OBlockManager.class).getSystemNameList().size() < 1) {
            return;
        }
        Element elem = new Element("nxparams");
        NXFrame nxFrame = NXFrame.getInstance();
        Element e = new Element("maxspeed");
        e.addContent(Float.toString(nxFrame.getMaxSpeed()));
        elem.addContent(e);
        e = new Element("haltstart");
        e.addContent(nxFrame.getStartHalt()?"yes":"no");
        elem.addContent(e);
        element.addContent(elem);
        return;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        
        // don't continue on to build NXFrame if no content
        if (shared.getChildren().size() == 0) return true;
        
        if (!GraphicsEnvironment.isHeadless()) {
            NXFrame nxFrame = NXFrame.getInstance();
            loadNXParams(nxFrame, shared.getChild("nxparams"));
//            nxFrame.init();   don't make visible
        }
        List<Element> warrantList = shared.getChildren("warrant");
        if (log.isDebugEnabled()) log.debug("Found "+warrantList.size()+" Warrant objects");
        for (int i=0; i<warrantList.size(); i++) {
            Element elem = warrantList.get(i);
            if (elem.getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+elem+" "+elem.getAttributes());
                break;
            }
            String sysName = null;
            if (elem.getAttribute("systemName") != null)
                sysName = elem.getAttribute("systemName").getValue();

            String userName = null;
            if (elem.getAttribute("userName") != null)
                userName = elem.getAttribute("userName").getValue();

            Warrant warrant = manager.createNewWarrant(sysName, userName);
            if (warrant==null) {
                log.info("Warrant \""+sysName+"("+userName+")\" previously loaded. This version not loaded.");
                continue;
            }
            List<Element> orders = elem.getChildren("blockOrder");
            for (int k=0; k<orders.size(); k++) {
                BlockOrder bo = loadBlockOrder(orders.get(k));
                if (bo==null) {
                    break;
                }
                warrant.addBlockOrder(bo);
            }
            String c = elem.getChildText("comment");
            if (c != null) {
                warrant.setComment(c);
            }
            
            Element order = elem.getChild("viaOrder");
            if (order!=null) {
                warrant.setViaOrder(loadBlockOrder(order));             
            }
            order = elem.getChild("avoidOrder");
            if (order!=null) {
                warrant.setAvoidOrder(loadBlockOrder(order));               
            }
            
            List<Element> throttleCmds = elem.getChildren("throttleCommand");
            for (int k=0; k<throttleCmds.size(); k++) {
                warrant.addThrottleCommand(loadThrottleCommand(throttleCmds.get(k)));
            }
            Element train = elem.getChild("train");
            if (train!=null) {
                loadTrain(train, warrant);
            }
        }
        return true;
    }

    public void load(Element element, Object o) throws Exception {
        log.error("load called. Invalid method.");
    }

    static void loadTrain(Element elem, Warrant warrant) {
        if (elem.getAttribute("trainId") != null) {
            warrant.setTrainId(elem.getAttribute("trainId").getValue());
        }
        if (elem.getAttribute("dccAddress") != null) {
            int address = 0;
            try {
               address = elem.getAttribute("dccAddress").getIntValue();
            } catch (org.jdom2.DataConversionException dce) {
                log.error(dce.toString()+ " for dccAddress in Warrant "+warrant.getDisplayName());
            }
            String addr = address+"("+elem.getAttribute("dccType").getValue()+")";
            warrant.setDccAddress(addr);
        }
        if (elem.getAttribute("runBlind") != null) {
            warrant.setRunBlind(elem.getAttribute("runBlind").getValue().equals("true"));
        }
        if (elem.getAttribute("trainName") != null) {
            warrant.setTrainName(elem.getAttribute("trainName").getValue());
        }
    }

    static BlockOrder loadBlockOrder(Element elem) {

        OBlock block = null;
        List<Element> blocks = elem.getChildren("block");
        if (blocks.size()>1) log.error("More than one block present: "+blocks.size());
        if (blocks.size()>0) {
            // sensor
            String name = blocks.get(0).getAttribute("systemName").getValue();
            block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).provideOBlock(name);
            if (block==null) {
                log.error("Unknown Block \""+name+"\" is null in BlockOrder.");
                return null;
            }
            if (log.isDebugEnabled()) log.debug("Load Block "+name+".");
        } else {
            log.error("Null BlockOrder element");
            return null;
        }
        Attribute attr = elem.getAttribute("pathName");
        String pathName = null;
        if (attr != null)
            pathName = attr.getValue();

        attr = elem.getAttribute("entryName");
        String entryName = null;
        if (attr != null)
            entryName =attr.getValue();

        attr = elem.getAttribute("exitName");
        String exitName = null;
        if (attr != null)
            exitName =attr.getValue();

        return new BlockOrder(block, pathName, entryName, exitName);
    }
    
    static ThrottleSetting loadThrottleCommand(Element elem) {
        long time = 0;
        try {
            time = elem.getAttribute("time").getLongValue();
        } catch (org.jdom2.DataConversionException dce) {}

        Attribute attr = elem.getAttribute("command");
        String command = null;
        if (attr != null)
            command =attr.getValue();

        attr = elem.getAttribute("value");
        String value = null;
        if (attr != null)
            value =attr.getValue();

        attr = elem.getAttribute("block");
        String block = null;
        if (attr != null)
            block =attr.getValue();

        return new ThrottleSetting(time, command, value, block);
    }
    
    static void loadNXParams(NXFrame nxFrame, Element elem) {
        if (elem==null) {
            return;
        }
        nxFrame.setVisible(false);
        Element e = elem.getChild("maxspeed");
        if (e!=null) {
            try {
                nxFrame.setMaxSpeed(Float.valueOf(e.getValue()));
            } catch (NumberFormatException nfe) {
                log.error("NXWarrant MaxSpeed; "+nfe);          
            }           
        }
        e = elem.getChild("haltstart");
        if (e!=null) {
            if (e.getValue().equals("yes")) {
                nxFrame.setStartHalt(true);
            } else {
                nxFrame.setStartHalt(false);
            }
        }
    }
    
    public int loadOrder(){
        return InstanceManager.getDefault(WarrantManager.class).getXMLOrder();
    }
    
    private final static Logger log = LoggerFactory.getLogger(WarrantManagerXml.class.getName());
}


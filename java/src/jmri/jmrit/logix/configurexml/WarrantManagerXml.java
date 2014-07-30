package jmri.jmrit.logix.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.Attribute;

import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.jmrit.logix.NXFrame;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.BlockOrder;
import jmri.jmrit.logix.ThrottleSetting;

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
        warrants.addContent(storeNXParams());
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
            elem.setAttribute("userName", uname);
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

    Element storeTrain(Warrant warrant, String type) {
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

    Element storeOrder(BlockOrder order, String type) {
        Element elem = new Element(type);
        OBlock block = order.getBlock();
        if (block!=null) {
            Element blk = new Element("block");
            blk.setAttribute("systemName", block.getSystemName());
            String uname = block.getUserName();
            if (uname==null) uname = "";
            blk.setAttribute("userName", uname);
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

    Element storeCommand(ThrottleSetting command, String type) {
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
    
    Element storeNXParams () {
        Element elem = new Element("nxparams");
        NXFrame nxFrame = NXFrame.getInstance();
        Element e = new Element("scale");
        e.addContent(Float.toString(nxFrame.getScale()));
        elem.addContent(e);
        e = new Element("maxspeed");
        e.addContent(Float.toString(nxFrame.getMaxSpeed()));
        elem.addContent(e);
        e = new Element("minspeed");
        e.addContent(Float.toString(nxFrame.getMinSpeed()));
        elem.addContent(e);
        e = new Element("timeinterval");
        e.addContent(Float.toString(nxFrame.getTimeInterval()));
        elem.addContent(e);
        e = new Element("numsteps");
        e.addContent(Integer.toString(nxFrame.getNumSteps()));
        elem.addContent(e);
        e = new Element("haltstart");
        e.addContent(nxFrame.getStartHalt()?"yes":"no");
        elem.addContent(e);
        return elem;
    }

    /**
     * Create a Warrant object of the correct class, then
     * register and fill it.
     * @param warrants Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
    public boolean load(Element warrants) {

        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        NXFrame nxFrame = NXFrame.getInstance();
        loadNXParams(nxFrame, warrants.getChild("nxparams"));
        nxFrame.init();

        List<Element> warrantList = warrants.getChildren("warrant");
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
            orders = elem.getChildren("viaOrder");
            for (int k=0; k<orders.size(); k++) {
                BlockOrder bo = loadBlockOrder(orders.get(k));
                if (bo==null) {
                    continue;
                }
                warrant.setViaOrder(bo);
            }
            List<Element> throttleCmds = elem.getChildren("throttleCommand");
            for (int k=0; k<throttleCmds.size(); k++) {
                warrant.addThrottleCommand(loadThrottleCommand(throttleCmds.get(k)));
            }
            List<Element> trains = elem.getChildren("train");
            for (int k=0; k<trains.size(); k++) {
                loadTrain(trains.get(k), warrant);
            }
        }
        return true;
    }

    public void load(Element element, Object o) throws Exception {
        log.error("load called. Invalid method.");
    }

    void loadTrain(Element elem, Warrant warrant) {
        if (elem.getAttribute("trainId") != null) {
            warrant.setTrainId(elem.getAttribute("trainId").getValue());
        }
        if (elem.getAttribute("dccAddress") != null) {
            int address = 0;
            try {
               address = elem.getAttribute("dccAddress").getIntValue();
            } catch (org.jdom.DataConversionException dce) {
                log.error(dce.toString()+ " in Warrant "+warrant.getDisplayName());
            }
            boolean isLong = true;
            if (elem.getAttribute("dccType") != null) {
                isLong = elem.getAttribute("dccType").getValue().equals("L");
            }
            warrant.setDccAddress(new DccLocoAddress(address, isLong));
        }
        if (elem.getAttribute("runBlind") != null) {
            warrant.setRunBlind(elem.getAttribute("runBlind").getValue().equals("true"));
        }
        if (elem.getAttribute("trainName") != null) {
            warrant.setTrainName(elem.getAttribute("trainName").getValue());
        }
    }

    @SuppressWarnings("unchecked")
    BlockOrder loadBlockOrder(Element elem) {

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
    
    ThrottleSetting loadThrottleCommand(Element elem) {
        long time = 0;
        try {
            time = elem.getAttribute("time").getLongValue();
        } catch (org.jdom.DataConversionException dce) {}

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
    void loadNXParams(NXFrame nxFrame, Element elem) {
    	if (elem==null) {
    		return;
    	}
        nxFrame.setVisible(false);
    	Element e = elem.getChild("scale");
    	if (e!=null) {
        	try {
        		nxFrame.setScale(Float.valueOf(e.getValue()));
        	} catch (NumberFormatException nfe) {
                log.error("Layout Scale; "+nfe);    		
        	}    		
    	}
    	e = elem.getChild("maxspeed");
    	if (e!=null) {
        	try {
        		nxFrame.setMaxSpeed(Float.valueOf(e.getValue()));
        	} catch (NumberFormatException nfe) {
                log.error("NXWarrant MaxSpeed; "+nfe);    		
        	}    		
    	}
    	e = elem.getChild("minspeed");
    	if (e!=null) {
        	try {
        		nxFrame.setMinSpeed(Float.valueOf(e.getValue()));
        	} catch (NumberFormatException nfe) {
                log.error("NXWarrant MinSpeed; "+nfe);    		
        	}    		
    	}
    	e = elem.getChild("timeinterval");
    	if (e!=null) {
        	try {
        		nxFrame.setTimeInterval(Float.valueOf(e.getValue()));
        	} catch (NumberFormatException nfe) {
                log.error("NXWarrant timeinterval; "+nfe);    		
        	}    		
    	}
    	e = elem.getChild("numsteps");
    	if (e!=null) {
        	try {
        		nxFrame.setNumSteps(Integer.valueOf(e.getValue()));
        	} catch (NumberFormatException nfe) {
                log.error("NXWarrant numSteps; "+nfe);    		
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
    
    static Logger log = LoggerFactory.getLogger(WarrantManagerXml.class.getName());
}


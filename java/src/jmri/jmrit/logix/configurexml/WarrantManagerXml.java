package jmri.jmrit.logix.configurexml;

import java.util.List;
import java.util.SortedSet;
//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.SpeedStepMode;
import jmri.jmrit.logix.BlockOrder;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.SCWarrant;
import jmri.jmrit.logix.SpeedUtil;
import jmri.jmrit.logix.ThrottleSetting;
import jmri.jmrit.logix.ThrottleSetting.Command;
import jmri.jmrit.logix.ThrottleSetting.CommandValue;
import jmri.jmrit.logix.ThrottleSetting.ValueType;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for
 * configuring the WarrantManager.
 * <p>
 * Typically, a subclass will just implement the load(Element warrant)
 * class, relying on implementation here to load the individual Warrant objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2009
 */
public class WarrantManagerXml extends jmri.configurexml.AbstractXmlAdapter {

    public WarrantManagerXml() {
    }
    
    /**
     * Store the contents of a WarrantManager.
     *
     * @param o Object to store, of type warrantManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element warrants = new Element("warrants");
        warrants.setAttribute("class", "jmri.jmrit.logix.configurexml.WarrantManagerXml");
        WarrantManager wm = (WarrantManager) o;
        if (wm != null) {
            SortedSet<Warrant> warrantList = wm.getNamedBeanSet();
            // don't return an element if there are no warrants to include
            if (warrantList.isEmpty()) {
                return null;
            }
            for (Warrant warrant : warrantList) {
                String sName = warrant.getSystemName();
                String uName = warrant.getUserName();
                log.debug("Warrant: sysName= {}, userName= {}", sName, uName);
                Element elem = new Element("warrant");
                elem.setAttribute("systemName", sName);
                if (uName == null) {
                    uName = "";
                }
                if (uName.length() > 0) {
                    elem.setAttribute("userName", uName);
                }
                if (warrant instanceof SCWarrant) {
                    elem.setAttribute("wtype", "SC");
                    elem.setAttribute("speedFactor", "" + ((SCWarrant) warrant).getSpeedFactor());
                    elem.setAttribute("timeToPlatform", "" + ((SCWarrant) warrant).getTimeToPlatform());
                    elem.setAttribute("forward", ((SCWarrant) warrant).getForward() ? "true" : "false");
                } else {
                    elem.setAttribute("wtype", "normal");
                }
                String comment = warrant.getComment();
                if (comment != null) {
                    Element c = new Element("comment");
                    c.addContent(comment);
                    elem.addContent(c);
                }

                List<BlockOrder> orders = warrant.getBlockOrders();
                if (orders == null) {
                    log.error("Warrant {} has no Route defined. (no BlockOrders) Cannot store.", warrant.getDisplayName());
                    continue;
                }
                for (BlockOrder bo : orders) {
                    elem.addContent(storeOrder(bo, "blockOrder"));
                }

                BlockOrder viaOrder = warrant.getViaOrder();
                if (viaOrder != null) {
                    elem.addContent(storeOrder(viaOrder, "viaOrder"));
                }
                BlockOrder avoidOrder = warrant.getAvoidOrder();
                if (avoidOrder != null) {
                    elem.addContent(storeOrder(avoidOrder, "avoidOrder"));
                }

                List<ThrottleSetting> throttleCmds = warrant.getThrottleCommands();
                for (ThrottleSetting ts : throttleCmds) {
                    elem.addContent(storeThrottleSetting(ts));
                }

                elem.addContent(storeTrain(warrant, "train"));

                // and put this element out
                warrants.addContent(elem);
            }
        }
        return warrants;
    }

    private static Element storeTrain(Warrant warrant, String type) {
        Element elem = new Element(type);
        SpeedUtil speedUtil = warrant.getSpeedUtil();
        String str = speedUtil.getRosterId();
        if (str==null) str = "";
        elem.setAttribute("trainId", str);

        DccLocoAddress addr = speedUtil.getDccAddress();
        if (addr != null) {
            elem.setAttribute("dccAddress", ""+addr.getNumber());
            elem.setAttribute("dccType", ""+(addr.isLongAddress() ? "L" : "S"));
        }
        elem.setAttribute("runBlind", warrant.getRunBlind()?"true":"false");
        elem.setAttribute("shareRoute", warrant.getShareRoute()?"true":"false");
        elem.setAttribute("noRamp", warrant.getNoRamp()?"true":"false");

        str = warrant.getTrainName();
        if (str==null) str = "";
        elem.setAttribute("trainName", str);
        
        return elem;
    }

    private static Element storeOrder(BlockOrder order, String type) {
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
        if (str == null) {
            str = "";
        }
        elem.setAttribute("pathName", str);

        str = order.getEntryName();
        if (str == null) {
            str = "";
        }
        elem.setAttribute("entryName", str);

        str = order.getExitName();
        if (str == null) {
            str = "";
        }
        elem.setAttribute("exitName", str);

        return elem;
    }
    private static Element storeThrottleSetting(ThrottleSetting ts) {
        Element element = new Element("throttleSetting");
        element.setAttribute("elapsedTime", String.valueOf(ts.getTime()));
        String name = ts.getBeanSystemName();
        if (name != null) {
            element.setAttribute("beanName", name);
        } else {
            element.setAttribute("beanName", "");
        }
        element.setAttribute("trackSpeed", String.valueOf(ts.getTrackSpeed()));

        Element elem = new Element("command");
        Command cmd = ts.getCommand();
        elem.setAttribute("commandType", String.valueOf(cmd.getIntId()));
        elem.setAttribute("fKey", String.valueOf(ts.getKeyNum()));
        element.addContent(elem);

        elem = new Element("commandValue");
        CommandValue cmdVal = ts.getValue();
        elem.setAttribute("valueType", String.valueOf(cmdVal.getType().getIntId()));
        elem.setAttribute("speedMode", cmdVal.getMode().name);
        elem.setAttribute("floatValue", String.valueOf(cmdVal.getFloat()));
        element.addContent(elem);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        
        if (shared.getChildren().isEmpty()) {
            return true;
        }
        
        List<Element> warrantList = shared.getChildren("warrant");
        log.debug("Found {} Warrant objects", warrantList.size());
        for (Element elem : warrantList) {
            if (elem.getAttribute("systemName") == null) {
                log.warn("unexpected null for systemName in elem {}", elem);
                break;
            }
            String sysName = null;
            if (elem.getAttribute("systemName") != null)
                sysName = elem.getAttribute("systemName").getValue();

            String userName = null;
            if (elem.getAttribute("userName") != null)
                userName = elem.getAttribute("userName").getValue();
            
            boolean SCWa = true;
            log.debug("loading warrant {}", sysName);
            Attribute wType = elem.getAttribute("wtype");
            if (wType == null) {
                log.debug("wtype is null for {}", sysName);
                SCWa = false;
            } else if (!wType.getValue().equals("SC")) {
                log.debug("wtype is {} for {}", wType.getValue(), sysName);
                SCWa = false;
            }
            
            long timeToPlatform = 500;
            Attribute TTP = elem.getAttribute("timeToPlatform");
            if (TTP != null) {
                try {
                    timeToPlatform = TTP.getLongValue();
                } catch (DataConversionException e) {
                    log.debug("ignoring DataConversionException (and reverting to default value): {}", e.toString());
                }
            }

            Warrant warrant = manager.createNewWarrant(sysName, userName, SCWa, timeToPlatform);
            if (warrant == null) {
                log.info("Warrant \"{}\" (userName={}) previously loaded. This version not loaded.", sysName, userName);
                continue;
            }
            if (SCWa && warrant instanceof SCWarrant) {
                if (elem.getAttribute("forward") != null) {
                    ((SCWarrant)warrant).setForward(elem.getAttribute("forward").getValue().equals("true"));
                }
                if (elem.getAttribute("speedFactor") != null) {
                    try {
                        ((SCWarrant)warrant).setSpeedFactor(elem.getAttribute("speedFactor").getFloatValue());
                    } catch (DataConversionException e) {
                        log.warn("error converting speed value");
                    }
                }
                warrant.setNoRamp(SCWa);
                warrant.setShareRoute(SCWa);
            }
            List<Element> orders = elem.getChildren("blockOrder");
            for (Element ord : orders) {
                BlockOrder bo = loadBlockOrder(ord);
                if (bo == null) {
                    log.error("Bad BlockOrder in warrant \"{}\".", warrant.getDisplayName());
                } else {
                    warrant.addBlockOrder(bo);
                }
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

            if (SCWa) {
                boolean forward =true;
                if (elem.getAttribute("forward") != null) {
                    forward = elem.getAttribute("forward").getValue().equals("true");
                }
                if (warrant instanceof SCWarrant) {
                    ((SCWarrant)warrant).setForward(forward);
                }
                warrant.setNoRamp(SCWa);
                warrant.setShareRoute(SCWa);
            }
            Element train = elem.getChild("train");
            if (train!=null) {
                loadTrain(train, warrant);
            }
        }

        // A second pass through the warrant list done to load the commands. This is done so that
        // references made to warrants in commands are fully specified. Due to ThrottleSetting
        // Ctor using provideWarrant to establish the referenced warrant.
        warrantList = shared.getChildren("warrant");
        for (Element elem : warrantList) {
            // boolean forward = true;  // variable not used, see GitHub JMRI/JMRI Issue #5661
            if (elem.getAttribute("systemName") == null) {
                break;
            }
            if (elem.getAttribute("systemName") != null) {
                String sysName = elem.getAttribute("systemName").getValue();
                if (sysName != null) {
                    Warrant warrant = manager.getBySystemName(sysName);
                    List<Element> throttleCmds;
                    if (warrant != null) {
                        log.debug("warrant: {}", warrant.getDisplayName());
                        throttleCmds = elem.getChildren("throttleCommand");
                        if (throttleCmds != null) {
                            log.debug("throttleCommand size= {}",throttleCmds.size());
                            throttleCmds.forEach((e) -> {
                                warrant.addThrottleCommand(loadThrottleCommand(e, warrant));
                            });
                        }
                        throttleCmds = elem.getChildren("throttleSetting");
                        if (throttleCmds != null) {
                            log.debug("throttleSetting size= {}",throttleCmds.size());
                            throttleCmds.forEach((e) -> {
                                warrant.addThrottleCommand(loadThrottleSetting(e, warrant));
                            });
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void loadTrain(Element elem, Warrant warrant) {
        SpeedUtil speedUtil = warrant.getSpeedUtil();
        if (elem.getAttribute("trainId") != null) {
            speedUtil.setRosterId(elem.getAttribute("trainId").getValue());
        }
        // if a RosterEntry exists "trainId" will be the Roster Id, otherwise a train name
        // Possible redundant call to setDccAddress() is OK
        if (elem.getAttribute("dccAddress") != null) {
            try {
               int address = elem.getAttribute("dccAddress").getIntValue();
               String addr = address+"("+elem.getAttribute("dccType").getValue()+")";
               speedUtil.setDccAddress(addr);
            } catch (org.jdom2.DataConversionException dce) {
                log.error("{} for dccAddress in Warrant {}", dce, warrant.getDisplayName());
            }
        }
        if (elem.getAttribute("runBlind") != null) {
            warrant.setRunBlind(elem.getAttribute("runBlind").getValue().equals("true"));
        }
        if (elem.getAttribute("shareRoute") != null) {
            warrant.setShareRoute(elem.getAttribute("shareRoute").getValue().equals("true"));
        }
        if (elem.getAttribute("noRamp") != null) {
            warrant.setNoRamp(elem.getAttribute("noRamp").getValue().equals("true"));
        }
        if (elem.getAttribute("trainName") != null) {
            warrant.setTrainName(elem.getAttribute("trainName").getValue());
        }
    }

    private static BlockOrder loadBlockOrder(Element elem) {

        OBlock block = null;
        List<Element> blocks = elem.getChildren("block");
        if (blocks.size()>1) log.error("More than one block present: {}", blocks.size());
        if (blocks.size()>0) {
            String name = blocks.get(0).getAttribute("systemName").getValue();
            block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(name);
            if (block == null) {
                log.error("No such Block \"{}\" found.", name);
                return null;
            }
            if (log.isDebugEnabled()) log.debug("Load Block {}.", name);
        } else {
            log.error("Null BlockOrder element");
            return null;
        }
        Attribute attr = elem.getAttribute("pathName");
        String pathName = null;
        if (attr != null) {
            pathName = attr.getValue();
        }

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

    private static ThrottleSetting loadThrottleSetting(Element element, Warrant w) {

        ThrottleSetting ts = new ThrottleSetting();

        Attribute attr = element.getAttribute("elapsedTime");
        if (attr != null) {
            ts.setTime(Long.parseLong(attr.getValue()));
        }

        Command cmd = null;
        Element elem = element.getChild("command");
        if (elem != null) {
            attr = elem.getAttribute("commandType");
            if (attr != null) {
                try {
                    cmd = ThrottleSetting.getCommandTypeFromInt(Integer.parseInt(attr.getValue()));
                    ts.setCommand(cmd);
                } catch (IllegalArgumentException iae) {
                    log.error("{} for throttleSetting {} in warrant {}",iae.getMessage(), ts.toString(), w.getDisplayName());
                }
            } else {
                log.error("Command type is null for throttleSetting {} in warrant {}", ts.toString(), w.getDisplayName());
            }
            attr = elem.getAttribute("fKey");
            if (attr != null) {
                ts.setKeyNum(Integer.parseInt(attr.getValue()));
            }
        }

        elem = element.getChild("commandValue");
        ValueType valType = null;
        SpeedStepMode mode = null;
        float floatVal = 0;
        if (elem != null) {
            attr = elem.getAttribute("valueType");
            if (attr != null) {
                try {                
                    valType = ThrottleSetting.getValueTypeFromInt(Integer.parseInt(attr.getValue()));
                } catch (IllegalArgumentException iae) {
                    log.error("{} for throttleSetting {} in warrant {}",iae.getMessage(), ts.toString(), w.getDisplayName());
                }
            } else {
                log.error("Value type is null for throttleSetting {} in warrant {}", ts.toString(), w.getDisplayName());
            }
            attr = elem.getAttribute("speedMode");
            if (attr != null) {
                mode = SpeedStepMode.getByName(attr.getValue());
            }
            attr = elem.getAttribute("floatValue");
            if (attr != null) {
                floatVal = Float.parseFloat(attr.getValue());
            }
        }
        ts.setValue(valType, mode, floatVal);

        attr = element.getAttribute("trackSpeed");
        if (attr != null) {
            ts.setTrackSpeed(Float.parseFloat(attr.getValue()));
        }
        
        attr = element.getAttribute("beanName");
        if (attr != null) {
            String errMsg = ts.setNamedBean(cmd, attr.getValue());
            if (errMsg != null) {
                log.error("{} for throttleSetting {} in warrant {}", errMsg, ts.toString(), w.getDisplayName());
            }
        }
        return ts;
    }
    
    // pre 4.21.3
//    @SuppressFBWarnings(value="NP_LOAD_OF_KNOWN_NULL_VALUE", justification="nothing wrong about a null return")
    private static ThrottleSetting loadThrottleCommand(Element elem, Warrant w) {
        long time = 0;
        try {
            time = elem.getAttribute("time").getLongValue();
        } catch (org.jdom2.DataConversionException dce) {
            log.warn("error loading throttle command");
        }

        Attribute attr = elem.getAttribute("command");
        String command = null;
        if (attr != null) {
            command = attr.getValue();
        } else {
            log.error("Command type is null. ThrottleSetting not loaded for warrant {}", w.getDisplayName());
            return null;
        }

        attr = elem.getAttribute("value");
        String value = null;
        if (attr != null)
            value =attr.getValue();

        attr = elem.getAttribute("block");
        String block = null;
        if (attr != null)
            block =attr.getValue();

        float speed = 0.0f;
        attr = elem.getAttribute("trackSpeed");
        if (attr != null) {
            try {
                speed = attr.getFloatValue();
            } catch (DataConversionException ex) {
                speed = 0.0f;
                log.error("Unable to read speed of command.", ex);
            }
        }
        
        return new ThrottleSetting(time, command, value, block, speed);
    }
    
    @Override
    public int loadOrder(){
        return InstanceManager.getDefault(WarrantManager.class).getXMLOrder();
    }
    
    private final static Logger log = LoggerFactory.getLogger(WarrantManagerXml.class);

}

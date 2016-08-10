/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.managers.configurexml;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Section;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.SignalMastLogicManager;
import jmri.SignalMastManager;
import jmri.Turnout;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kevin
 */
public class DefaultSignalMastLogicManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultSignalMastLogicManagerXml() {
        debug = log.isDebugEnabled();
    }

    private boolean debug;

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    public Element store(Object o) {
        Element signalMastLogic = new Element("signalmastlogics");
        setStoreElementClass(signalMastLogic);
        SignalMastLogicManager smlm = (SignalMastLogicManager) o;
        signalMastLogic.addContent(new Element("logicDelay").addContent(Long.toString(smlm.getSignalLogicDelay())));
        ArrayList<SignalMastLogic> sml = smlm.getSignalMastLogicList();
        for (int i = 0; i < sml.size(); i++) {
            SignalMastLogic sm = sml.get(i);
            Element source = new Element("signalmastlogic");
            source.setAttribute("source", sm.getSourceMast().getDisplayName());// added purely to make human reading of the xml easier
            source.addContent(new Element("sourceSignalMast").addContent(sm.getSourceMast().getDisplayName()));
            ArrayList<SignalMast> destination = sm.getDestinationList();
            if (destination.size() != 0) {
                for (int k = 0; k < destination.size(); k++) {
                    SignalMast dest = destination.get(k);
                    if (sml.get(i).getStoreState(dest) != SignalMastLogic.STORENONE) {
                        Element elem = new Element("destinationMast");
                        elem.setAttribute("destination", dest.getDisplayName()); // added purely to make human reading of the xml easier
                        elem.addContent(new Element("destinationSignalMast").addContent(dest.getDisplayName()));
                        elem.addContent(new Element("comment").addContent(sm.getComment(dest)));
                        if (sm.isEnabled(dest)) {
                            elem.addContent(new Element("enabled").addContent("yes"));
                        } else {
                            elem.addContent(new Element("enabled").addContent("no"));
                        }

                        if (sm.allowAutoMaticSignalMastGeneration(dest)) {
                            elem.addContent(new Element("allowAutoMaticSignalMastGeneration").addContent("yes"));
                        } else {
                            elem.addContent(new Element("allowAutoMaticSignalMastGeneration").addContent("no"));
                        }

                        if (sm.useLayoutEditor(dest)) {
                            elem.addContent(new Element("useLayoutEditor").addContent("yes"));
                        } else {
                            elem.addContent(new Element("useLayoutEditor").addContent("no"));
                        }

                        if (sm.useLayoutEditorTurnouts(dest)) {
                            elem.addContent(new Element("useLayoutEditorTurnouts").addContent("yes"));
                        } else {
                            elem.addContent(new Element("useLayoutEditorTurnouts").addContent("no"));
                        }

                        if (sm.useLayoutEditorBlocks(dest)) {
                            elem.addContent(new Element("useLayoutEditorBlocks").addContent("yes"));
                        } else {
                            elem.addContent(new Element("useLayoutEditorBlocks").addContent("no"));
                        }

                        if (sm.getAssociatedSection(dest) != null) {
                            elem.addContent(new Element("associatedSection").addContent(sm.getAssociatedSection(dest).getDisplayName()));
                        }
                        if (sm.isTurnoutLockAllowed(dest)) {
                            elem.addContent(new Element("lockTurnouts").addContent("yes"));
                        } else {
                            elem.addContent(new Element("lockTurnouts").addContent("no"));
                        }

                        if (sml.get(i).getStoreState(dest) == SignalMastLogic.STOREALL) {
                            ArrayList<Block> blocks = sm.getBlocks(dest);
                            if (blocks.size() > 0) {
                                Element blockElement = new Element("blocks");
                                for (int j = 0; j < blocks.size(); j++) {
                                    Element bloc = new Element("block");
                                    bloc.addContent(new Element("blockName").addContent(blocks.get(j).getDisplayName()));
                                    String blkState = "anyState";
                                    if (sm.getBlockState(blocks.get(j), dest) == Block.OCCUPIED) {
                                        blkState = "occupied";
                                    } else if (sm.getBlockState(blocks.get(j), dest) == Block.UNOCCUPIED) {
                                        blkState = "unoccupied";
                                    }
                                    bloc.addContent(new Element("blockState").addContent(blkState));
                                    blockElement.addContent(bloc);
                                }
                                elem.addContent(blockElement);
                            }
                            ArrayList<NamedBeanHandle<Turnout>> turnouts = sm.getNamedTurnouts(dest);
                            if (turnouts.size() > 0) {
                                Element turnoutElement = new Element("turnouts");
                                for (int j = 0; j < turnouts.size(); j++) {
                                    Element turn = new Element("turnout");
                                    turn.addContent(new Element("turnoutName").addContent(turnouts.get(j).getName()));
                                    String turnState = "thrown";
                                    if (sm.getTurnoutState(turnouts.get(j).getBean(), dest) == Turnout.CLOSED) {
                                        turnState = "closed";
                                    }
                                    turn.addContent(new Element("turnoutState").addContent(turnState));
                                    turnoutElement.addContent(turn);
                                }
                                elem.addContent(turnoutElement);
                            }
                            ArrayList<NamedBeanHandle<Sensor>> sensors = sm.getNamedSensors(dest);
                            if (sensors.size() > 0) {
                                Element sensorElement = new Element("sensors");
                                for (int j = 0; j < sensors.size(); j++) {
                                    Element sensor = new Element("sensor");
                                    sensor.addContent(new Element("sensorName").addContent(sensors.get(j).getName()));
                                    String sensorState = "inActive";
                                    if (sm.getSensorState(sensors.get(j).getBean(), dest) == Sensor.ACTIVE) {
                                        sensorState = "active";
                                    }
                                    sensor.addContent(new Element("sensorState").addContent(sensorState));
                                    sensorElement.addContent(sensor);
                                }
                                elem.addContent(sensorElement);
                            }
                            ArrayList<SignalMast> masts = sm.getSignalMasts(dest);
                            if (masts.size() > 0) {
                                Element mastElement = new Element("masts");
                                for (int j = 0; j < masts.size(); j++) {
                                    Element mast = new Element("mast");
                                    mast.addContent(new Element("mastName").addContent(masts.get(j).getDisplayName()));
                                    mast.addContent(new Element("mastState").addContent(sm.getSignalMastState(masts.get(j), dest)));
                                    mastElement.addContent(mast);
                                }
                                elem.addContent(mastElement);
                            }
                        }
                        source.addContent(elem);
                    }
                }
                signalMastLogic.addContent(source);
            }
        }
        return signalMastLogic;
    }

    public void setStoreElementClass(Element signalMastLogic) {
        signalMastLogic.setAttribute("class", "jmri.managers.configurexml.DefaultSignalMastLogicManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual Transits
        return loadSignalMastLogic(shared);
    }

    public boolean loadSignalMastLogic(Element signalMastLogic) {
        List<Element> logicList = signalMastLogic.getChildren("signalmastlogic");
        if (log.isDebugEnabled()) {
            log.debug("Found " + logicList.size() + " signal mast logics");
        }

        SignalMastManager sm = InstanceManager.getDefault(jmri.SignalMastManager.class);
        SignalMastLogicManager sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
        try {
            String logicDelay = signalMastLogic.getChild("logicDelay").getText();
            sml.setSignalLogicDelay(Long.parseLong(logicDelay));
        } catch (java.lang.NullPointerException e) {
            //Considered normal if it doesn't exists
        }
        boolean loadOk = true;
        for (Element so : logicList) {
            String source = so.getChild("sourceSignalMast").getText();
            SignalMast sourceMast = sm.getSignalMast(source);
            if (sourceMast != null) {
                SignalMastLogic logic = sml.newSignalMastLogic(sourceMast);
                List<Element> destList = so.getChildren("destinationMast");
                for (Element s : destList) {
                    String destination = s.getChild("destinationSignalMast").getText();
                    SignalMast dest = sm.getSignalMast(destination);
                    if (dest != null) {
                        logic.setDestinationMast(dest);
                        if (s.getChild("comment") != null) {
                            logic.setComment(s.getChild("comment").getText(), dest);
                        }
                        if (s.getChild("enabled") != null) {
                            if (s.getChild("enabled").getText().equals("yes")) {
                                logic.setEnabled(dest);
                            } else {
                                logic.setDisabled(dest);
                            }
                        }

                        if (s.getChild("allowAutoMaticSignalMastGeneration") != null) {
                            if (s.getChild("allowAutoMaticSignalMastGeneration").getText().equals("no")) {
                                logic.allowAutoMaticSignalMastGeneration(false, dest);
                            } else {
                                logic.allowAutoMaticSignalMastGeneration(true, dest);
                            }
                        }

                        boolean useLayoutEditorTurnout = true;
                        boolean useLayoutEditorBlock = true;
                        if (s.getChild("useLayoutEditorTurnouts") != null) {
                            if (s.getChild("useLayoutEditorTurnouts").getText().equals("no")) {
                                useLayoutEditorTurnout = false;
                            }
                        }

                        if (s.getChild("useLayoutEditorBlocks") != null) {
                            if (s.getChild("useLayoutEditorBlocks").getText().equals("no")) {
                                useLayoutEditorBlock = false;
                            }
                        }
                        try {
                            logic.useLayoutEditorDetails(useLayoutEditorTurnout, useLayoutEditorBlock, dest);
                        } catch (jmri.JmriException ex) {

                        }

                        if (s.getChild("useLayoutEditor") != null) {
                            try {
                                if (s.getChild("useLayoutEditor").getText().equals("yes")) {
                                    logic.useLayoutEditor(true, dest);
                                } else {
                                    logic.useLayoutEditor(false, dest);
                                }
                            } catch (jmri.JmriException e) {
                                //Considered normal if layout editor hasn't yet been set up.
                            }
                        }

                        if (s.getChild("associatedSection") != null) {
                            Section sect = InstanceManager.getDefault(jmri.SectionManager.class).getSection(s.getChild("associatedSection").getText());
                            logic.setAssociatedSection(sect, dest);
                        }

                        Element turnoutElem = s.getChild("turnouts");
                        if (turnoutElem != null) {
                            List<Element> turnoutList = turnoutElem.getChildren("turnout");
                            if (turnoutList.size() > 0) {
                                Hashtable<NamedBeanHandle<Turnout>, Integer> list = new Hashtable<NamedBeanHandle<Turnout>, Integer>();
                                for (Element t : turnoutList) {
                                    String turnout = t.getChild("turnoutName").getText();
                                    String state = t.getChild("turnoutState").getText();
                                    int value = Turnout.CLOSED;
                                    if (state.equals("thrown")) {
                                        value = Turnout.THROWN;
                                    }
                                    Turnout turn = InstanceManager.turnoutManagerInstance().getTurnout(turnout);
                                    if (turn != null) {
                                        NamedBeanHandle<Turnout> namedTurnout = nbhm.getNamedBeanHandle(turnout, turn);
                                        list.put(namedTurnout, value);
                                    } else if (debug) {
                                        log.debug("Unable to add Turnout " + turnout + " as it does not exist in the panel file");
                                    }

                                }
                                logic.setTurnouts(list, dest);
                            }
                        }
                        Element sensorElem = s.getChild("sensors");
                        if (sensorElem != null) {
                            List<Element> sensorList = sensorElem.getChildren("sensor");
                            if (sensorList.size() > 0) {
                                Hashtable<NamedBeanHandle<Sensor>, Integer> list = new Hashtable<NamedBeanHandle<Sensor>, Integer>();
                                for (Element sl : sensorList) {
                                    String sensorName = sl.getChild("sensorName").getText();
                                    String state = sl.getChild("sensorState").getText();
                                    int value = Sensor.INACTIVE;
                                    if (state.equals("active")) {
                                        value = Sensor.ACTIVE;
                                    }

                                    Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
                                    if (sen != null) {
                                        NamedBeanHandle<Sensor> namedSensor = nbhm.getNamedBeanHandle(sensorName, sen);
                                        list.put(namedSensor, value);
                                    } else if (debug) {
                                        log.debug("Unable to add sensor " + sensorName + " as it does not exist in the panel file");
                                    }

                                }
                                logic.setSensors(list, dest);
                            }
                        }
                        Element blockElem = s.getChild("blocks");
                        if (blockElem != null) {
                            List<Element> blockList = blockElem.getChildren("block");
                            if (blockList.size() > 0) {
                                Hashtable<Block, Integer> list = new Hashtable<Block, Integer>();
                                for (Element b : blockList) {
                                    String block = b.getChild("blockName").getText();
                                    String state = b.getChild("blockState").getText();
                                    int value = 0x03;
                                    if (state.equals("occupied")) {
                                        value = Block.OCCUPIED;
                                    } else if (state.equals("unoccupied")) {
                                        value = Block.UNOCCUPIED;
                                    }

                                    Block blk = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(block);
                                    if (blk != null) {
                                        list.put(blk, value);
                                    } else if (debug) {
                                        log.debug("Unable to add Block " + block + " as it does not exist in the panel file");
                                    }
                                }
                                logic.setBlocks(list, dest);
                            }
                        }
                        Element mastElem = s.getChild("masts");
                        if (mastElem != null) {
                            List<Element> mastList = mastElem.getChildren("mast");
                            if (mastList.size() > 0) {
                                Hashtable<SignalMast, String> list = new Hashtable<SignalMast, String>();
                                for (Element m : mastList) {
                                    String mast = m.getChild("mastName").getText();
                                    String state = m.getChild("mastState").getText();
                                    SignalMast mst = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(mast);
                                    if (mst != null) {
                                        list.put(mst, state);
                                    } else if (debug) {
                                        log.debug("Unable to add Signal Mast  " + mast + " as it does not exist in the panel file");
                                    }

                                }
                                logic.setMasts(list, dest);
                            }
                        }
                    } else {
                        log.error("Destination Mast " + destination + " Not found, logic not loaded");
                        loadOk = false;
                    }
                }
            } else {
                log.error("Source Mast " + source + " Not found, logic not loaded");
                loadOk = false;
            }
        }
        sml.initialise();
        return loadOk;
    }

    public int loadOrder() {
        return InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManagerXml.class.getName());
}

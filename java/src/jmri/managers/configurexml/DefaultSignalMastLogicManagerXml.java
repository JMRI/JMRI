package jmri.managers.configurexml;

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
    }

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    @Override
    public Element store(Object o) {
        Element signalMastLogic = new Element("signalmastlogics");
        setStoreElementClass(signalMastLogic);
        SignalMastLogicManager smlm = (SignalMastLogicManager) o;
        signalMastLogic.addContent(new Element("logicDelay").addContent(Long.toString(smlm.getSignalLogicDelay())));
        List<SignalMastLogic> smll = smlm.getSignalMastLogicList();
        for (SignalMastLogic sml : smll) {
            Element source = new Element("signalmastlogic");
            source.setAttribute("source", sml.getSourceMast().getDisplayName());// added purely to make human reading of the xml easier
            source.addContent(new Element("sourceSignalMast").addContent(sml.getSourceMast().getDisplayName()));
            List<SignalMast> destinations = sml.getDestinationList();
            for (SignalMast dest : destinations) {
                if (sml.getStoreState(dest) != SignalMastLogic.STORENONE) {
                    Element elem = new Element("destinationMast");
                    elem.setAttribute("destination", dest.getDisplayName()); // added purely to make human reading of the xml easier
                    elem.addContent(new Element("destinationSignalMast").addContent(dest.getDisplayName()));
                    elem.addContent(new Element("comment").addContent(sml.getComment(dest)));
                    if (sml.isEnabled(dest)) {
                        elem.addContent(new Element("enabled").addContent("yes"));
                    } else {
                        elem.addContent(new Element("enabled").addContent("no"));
                    }

                    if (sml.allowAutoMaticSignalMastGeneration(dest)) {
                        elem.addContent(new Element("allowAutoMaticSignalMastGeneration").addContent("yes"));
                    } else {
                        elem.addContent(new Element("allowAutoMaticSignalMastGeneration").addContent("no"));
                    }

                    if (sml.useLayoutEditor(dest)) {
                        elem.addContent(new Element("useLayoutEditor").addContent("yes"));
                    } else {
                        elem.addContent(new Element("useLayoutEditor").addContent("no"));
                    }

                    if (sml.useLayoutEditorTurnouts(dest)) {
                        elem.addContent(new Element("useLayoutEditorTurnouts").addContent("yes"));
                    } else {
                        elem.addContent(new Element("useLayoutEditorTurnouts").addContent("no"));
                    }

                    if (sml.useLayoutEditorBlocks(dest)) {
                        elem.addContent(new Element("useLayoutEditorBlocks").addContent("yes"));
                    } else {
                        elem.addContent(new Element("useLayoutEditorBlocks").addContent("no"));
                    }

                    if (sml.getAssociatedSection(dest) != null) {
                        elem.addContent(new Element("associatedSection").addContent(sml.getAssociatedSection(dest).getDisplayName()));
                    }
                    if (sml.isTurnoutLockAllowed(dest)) {
                        elem.addContent(new Element("lockTurnouts").addContent("yes"));
                    } else {
                        elem.addContent(new Element("lockTurnouts").addContent("no"));
                    }

                    if (sml.getStoreState(dest) == SignalMastLogic.STOREALL) {
                        List<Block> blocks = sml.getBlocks(dest);
                        if (blocks.size() > 0) {
                            Element blockElement = new Element("blocks");
                            for (Block bl : blocks) {
                                Element bloc = new Element("block");
                                bloc.addContent(new Element("blockName").addContent(bl.getDisplayName()));
                                String blkState = "anyState";
                                if (sml.getBlockState(bl, dest) == Block.OCCUPIED) {
                                    blkState = "occupied";
                                } else if (sml.getBlockState(bl, dest) == Block.UNOCCUPIED) {
                                    blkState = "unoccupied";
                                }
                                bloc.addContent(new Element("blockState").addContent(blkState));
                                blockElement.addContent(bloc);
                            }
                            elem.addContent(blockElement);
                        }
                        List<NamedBeanHandle<Turnout>> turnouts = sml.getNamedTurnouts(dest);
                        if (turnouts.size() > 0) {
                            Element turnoutElement = new Element("turnouts");
                            for (NamedBeanHandle<Turnout> t : turnouts) {
                                Element turn = new Element("turnout");
                                turn.addContent(new Element("turnoutName").addContent(t.getName()));
                                String turnState = "thrown";
                                if (sml.getTurnoutState(t.getBean(), dest) == Turnout.CLOSED) {
                                    turnState = "closed";
                                }
                                turn.addContent(new Element("turnoutState").addContent(turnState));
                                turnoutElement.addContent(turn);
                            }
                            elem.addContent(turnoutElement);
                        }
                        List<NamedBeanHandle<Sensor>> sensors = sml.getNamedSensors(dest);
                        if (sensors.size() > 0) {
                            Element sensorElement = new Element("sensors");
                            for (NamedBeanHandle<Sensor> s : sensors) {
                                Element sensor = new Element("sensor");
                                sensor.addContent(new Element("sensorName").addContent(s.getName()));
                                String sensorState = "inActive";
                                if (sml.getSensorState(s.getBean(), dest) == Sensor.ACTIVE) {
                                    sensorState = "active";
                                }
                                sensor.addContent(new Element("sensorState").addContent(sensorState));
                                sensorElement.addContent(sensor);
                            }
                            elem.addContent(sensorElement);
                        }
                        List<SignalMast> masts = sml.getSignalMasts(dest);
                        if (masts.size() > 0) {
                            Element mastElement = new Element("masts");
                            for (SignalMast sm : masts) {
                                Element mast = new Element("mast");
                                mast.addContent(new Element("mastName").addContent(sm.getDisplayName()));
                                mast.addContent(new Element("mastState").addContent(sml.getSignalMastState(sm, dest)));
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
        return signalMastLogic;
    }

    public void setStoreElementClass(Element signalMastLogic) {
        signalMastLogic.setAttribute("class", "jmri.managers.configurexml.DefaultSignalMastLogicManagerXml");
    }

    @Override
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
        log.debug("Found {} signal mast logics", logicList.size());

        SignalMastManager sm = InstanceManager.getDefault(jmri.SignalMastManager.class);
        SignalMastLogicManager smlm = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
        try {
            String logicDelay = signalMastLogic.getChild("logicDelay").getText();
            smlm.setSignalLogicDelay(Integer.parseInt(logicDelay));
        } catch (java.lang.NullPointerException e) {
            //Considered normal if it doesn't exists
        }
        boolean loadOk = true;
        for (Element sml : logicList) {
            String source = sml.getChild("sourceSignalMast").getText();
            SignalMast sourceMast = sm.getSignalMast(source);
            if (sourceMast != null) {
                SignalMastLogic logic = smlm.newSignalMastLogic(sourceMast);
                List<Element> destList = sml.getChildren("destinationMast");
                for (Element d : destList) {
                    String destination = d.getChild("destinationSignalMast").getText();
                    SignalMast dest = sm.getSignalMast(destination);
                    if (dest != null) {
                        logic.setDestinationMast(dest);
                        if (d.getChild("comment") != null) {
                            logic.setComment(d.getChild("comment").getText(), dest);
                        }
                        if (d.getChild("enabled") != null) {
                            if (d.getChild("enabled").getText().equals("yes")) {
                                logic.setEnabled(dest);
                            } else {
                                logic.setDisabled(dest);
                            }
                        }

                        if (d.getChild("allowAutoMaticSignalMastGeneration") != null) {
                            if (d.getChild("allowAutoMaticSignalMastGeneration").getText().equals("no")) {
                                logic.allowAutoMaticSignalMastGeneration(false, dest);
                            } else {
                                logic.allowAutoMaticSignalMastGeneration(true, dest);
                            }
                        }

                        boolean useLayoutEditorTurnout = true;
                        boolean useLayoutEditorBlock = true;
                        if (d.getChild("useLayoutEditorTurnouts") != null) {
                            if (d.getChild("useLayoutEditorTurnouts").getText().equals("no")) {
                                useLayoutEditorTurnout = false;
                            }
                        }

                        if (d.getChild("useLayoutEditorBlocks") != null) {
                            if (d.getChild("useLayoutEditorBlocks").getText().equals("no")) {
                                useLayoutEditorBlock = false;
                            }
                        }
                        try {
                            logic.useLayoutEditorDetails(useLayoutEditorTurnout, useLayoutEditorBlock, dest);
                        } catch (jmri.JmriException ex) {
                            log.error("use LayoutEditor details failed");
                        }

                        if (d.getChild("useLayoutEditor") != null) {
                            try {
                                if (d.getChild("useLayoutEditor").getText().equals("yes")) {
                                    logic.useLayoutEditor(true, dest);
                                } else {
                                    logic.useLayoutEditor(false, dest);
                                }
                            } catch (jmri.JmriException e) {
                                //Considered normal if layout editor hasn't yet been set up.
                            }
                        }

                        if (d.getChild("associatedSection") != null) {
                            Section sect = InstanceManager.getDefault(jmri.SectionManager.class).getSection(d.getChild("associatedSection").getText());
                            logic.setAssociatedSection(sect, dest);
                        }

                        Element turnoutElem = d.getChild("turnouts");
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
                                    }
                                    log.debug("Unable to add Turnout {} as it does not exist in the panel file", turnout);
                                }
                                logic.setTurnouts(list, dest);
                            }
                        }
                        Element sensorElem = d.getChild("sensors");
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
                                    }
                                    log.debug("Unable to add sensor {} as it does not exist in the panel file", sensorName);
                                }
                                logic.setSensors(list, dest);
                            }
                        }
                        Element blockElem = d.getChild("blocks");
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
                                    }
                                    log.debug("Unable to add Block {} as it does not exist in the panel file", block);
                                }
                                logic.setBlocks(list, dest);
                            }
                        }
                        Element mastElem = d.getChild("masts");
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
                                    }
                                    log.debug("Unable to add Signal Mast {} as it does not exist in the panel file", mast);
                                }
                                logic.setMasts(list, dest);
                            }
                        }
                    } else {
                        log.error("Destination Mast {} not found, logic not loaded", destination);
                        loadOk = false;
                    }
                }
            } else {
                log.error("Source Mast {} Not found, logic not loaded", source);
                loadOk = false;
            }
        }
        smlm.initialise();
        return loadOk;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManagerXml.class);

}

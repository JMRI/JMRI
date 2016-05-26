// EntryExitPairsXml.java
package jmri.jmrit.signalling.configurexml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.signalling.EntryExitPairs;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for the Entry Exit pairs unsed in
 * interlocking on a layouteditor
 *
 * @author Kevin Dickerson Copyright (c) 2007
 * @version $Revision: 1.2 $
 */
public class EntryExitPairsXml extends AbstractXmlAdapter {

    public EntryExitPairsXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionablePoint
     *
     * @param o Object to store, of type PositionablePoint
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        EntryExitPairs p = (EntryExitPairs) o;
        Element element = new Element("entryexitpairs");
        setStoreElementClass(element);
        ArrayList<LayoutEditor> editors = p.getSourcePanelList();
        if (editors.size() == 0) {
            return element;
        }
        element.addContent(new Element("cleardown").addContent("" + p.getClearDownOption()));
        if (p.getDispatcherIntegration()) {
            element.addContent(new Element("dispatcherintegration").addContent("yes"));
        }
        if (p.useDifferentColorWhenSetting()) {
            element.addContent(new Element("colourwhilesetting").addContent(colorToString(p.getSettingRouteColor())));
            element.addContent(new Element("settingTimer").addContent("" + p.getSettingTimer()));
        }
        for (int k = 0; k < editors.size(); k++) {
            LayoutEditor panel = editors.get(k);
            List<Object> nxpair = p.getSourceList(panel);
            if (nxpair != null) {
                Element panelElem = new Element("layoutPanel");
                panelElem.setAttribute("name", panel.getLayoutName());
                for (int j = 0; j < nxpair.size(); j++) {
                    Object key = nxpair.get(j);
                    Element source = new Element("source");
                    String type = "";
                    String item = "";

                    if (key instanceof SignalMast) {
                        type = "signalMast";
                        item = ((SignalMast) key).getDisplayName();
                    } else if (key instanceof Sensor) {
                        type = "sensor";
                        item = ((Sensor) key).getDisplayName();
                    } else if (key instanceof SignalHead) {
                        type = "signalHead";
                        item = ((SignalHead) key).getDisplayName();
                    }

                    source.setAttribute("type", type);
                    source.setAttribute("item", item);

                    ArrayList<Object> a = p.getDestinationList(key, panel);
                    for (int i = 0; i < a.size(); i++) {
                        Object keyDest = a.get(i);
                        String typeDest = "";
                        String itemDest = "";
                        if (keyDest instanceof SignalMast) {
                            typeDest = "signalMast";
                            itemDest = ((SignalMast) keyDest).getDisplayName();
                        } else if (keyDest instanceof Sensor) {
                            typeDest = "sensor";
                            itemDest = ((Sensor) keyDest).getDisplayName();
                        } else if (keyDest instanceof SignalHead) {
                            typeDest = "signalHead";
                            itemDest = ((SignalHead) keyDest).getDisplayName();
                        }
                        Element dest = new Element("destination");
                        dest.setAttribute("type", typeDest);
                        dest.setAttribute("item", itemDest);
                        if (!p.isUniDirection(key, panel, keyDest)) {
                            dest.setAttribute("uniDirection", "no");
                        }
                        if (!p.isEnabled(key, panel, keyDest)) {
                            dest.setAttribute("enabled", "no");
                        }
                        int nxType = p.getEntryExitType(key, panel, keyDest);
                        switch (nxType) {
                            case 0x00:
                                dest.setAttribute("nxType", "turnoutsetting");
                                break;
                            case 0x01:
                                dest.setAttribute("nxType", "signalmastlogic");
                                break;
                            case 0x02:
                                dest.setAttribute("nxType", "fullinterlocking");
                                break;
                            default:
                                dest.setAttribute("nxType", "turnoutsetting");
                                break;
                        }
                        if (p.getUniqueId(key, panel, keyDest) != null) {
                            dest.setAttribute("uniqueid", p.getUniqueId(key, panel, keyDest));
                        }
                        source.addContent(dest);
                    }
                    panelElem.addContent(source);
                }
                element.addContent(panelElem);
            }
        }
        return element;
    }

    public void setStoreElementClass(Element messages) {
        messages.setAttribute("class", "jmri.jmrit.signalling.configurexml.EntryExitPairsXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Load, starting with the layoutblock element, then all the value-icon
     * pairs
     *
     * @param shared Top level Element to unpack.
     * @return 
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // create the objects
        EntryExitPairs eep = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);

        try {
            String clearoption = shared.getChild("cleardown").getText();
            eep.setClearDownOption(Integer.parseInt(clearoption));
        } catch (java.lang.NullPointerException e) {
            //Considered normal if it doesn't exists
        }
        // get attributes
        ArrayList<Object> loadedPanel = jmri.InstanceManager.configureManagerInstance().getInstanceList(LayoutEditor.class);
        if (shared.getChild("dispatcherintegration") != null && shared.getChild("dispatcherintegration").getText().equals("yes")) {
            eep.setDispatcherIntegration(true);
        }
        if (shared.getChild("colourwhilesetting") != null) {
            eep.setSettingRouteColor(stringToColor(shared.getChild("colourwhilesetting").getText()));
            int settingTimer = 2000;
            try {
                settingTimer = Integer.parseInt(shared.getChild("settingTimer").getText());
            } catch (Exception e) {
                log.error("Error in converting timer to int " + shared.getChild("settingTimer"));
            }
            eep.setSettingTimer(settingTimer);
        }
        List<Element> panelList = shared.getChildren("layoutPanel");
        for (int k = 0; k < panelList.size(); k++) {
            String panelName = panelList.get(k).getAttribute("name").getValue();
            LayoutEditor panel = null;
            for (int i = 0; i < loadedPanel.size(); i++) {
                LayoutEditor tmp = (LayoutEditor) loadedPanel.get(i);
                if (tmp.getLayoutName().equals(panelName)) {
                    panel = tmp;
                    break;
                }
            }
            if (panel != null) {
                List<Element> sourceList = panelList.get(k).getChildren("source");
                for (int i = 0; i < sourceList.size(); i++) {
                    String sourceType = sourceList.get(i).getAttribute("type").getValue();
                    String sourceItem = sourceList.get(i).getAttribute("item").getValue();
                    NamedBean source = null;
                    if (sourceType.equals("signalMast")) {
                        source = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(sourceItem);
                    } else if (sourceType.equals("sensor")) {
                        source = jmri.InstanceManager.sensorManagerInstance().getSensor(sourceItem);
                    } else if (sourceType.equals("signalHead")) {
                        source = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(sourceItem);
                    }

                    //These two could be subbed off.
                    List<Element> destinationList = sourceList.get(i).getChildren("destination");
                    if (destinationList.size() > 0) {
                        eep.addNXSourcePoint(source, panel);
                    }
                    for (int j = 0; j < destinationList.size(); j++) {
                        String id = null;
                        if (destinationList.get(j).getAttribute("uniqueid") != null) {
                            id = destinationList.get(j).getAttribute("uniqueid").getValue();
                        }
                        String destType = destinationList.get(j).getAttribute("type").getValue();
                        String destItem = destinationList.get(j).getAttribute("item").getValue();
                        NamedBean dest = null;
                        if (destType.equals("signalMast")) {
                            dest = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(destItem);
                        } else if (destType.equals("sensor")) {
                            dest = jmri.InstanceManager.sensorManagerInstance().getSensor(destItem);
                        } else if (destType.equals("signalHead")) {
                            dest = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(destItem);
                        }
                        try {
                            eep.addNXDestination(source, dest, panel, id);
                        } catch (java.lang.NullPointerException e) {
                            log.error("An error occured while trying to add a point");
                        }
                        if ((destinationList.get(j).getAttribute("uniDirection") != null) && (destinationList.get(j).getAttribute("uniDirection").getValue().equals("no"))) {
                            eep.setUniDirection(source, panel, dest, false);
                        }
                        if ((destinationList.get(j).getAttribute("enabled") != null) && (destinationList.get(j).getAttribute("enabled").getValue().equals("no"))) {
                            eep.setEnabled(source, panel, dest, false);
                        }
                        if (destinationList.get(j).getAttribute("nxType") != null) {
                            String nxType = destinationList.get(j).getAttribute("nxType").getValue();
                            if (nxType.equals("turnoutsetting")) {
                                eep.setEntryExitType(source, panel, dest, 0x00);
                            } else if (nxType.equals("signalmastlogic")) {
                                eep.setEntryExitType(source, panel, dest, 0x01);
                            } else if (nxType.equals("fullinterlocking")) {
                                eep.setEntryExitType(source, panel, dest, 0x02);
                            }

                        }
                    }
                }
            } else {
                log.error("Panel has not been loaded");
            }
        }
        return true;
    }

    public static String colorToString(Color color) {
        if (color == Color.black) {
            return "black";
        } else if (color == Color.darkGray) {
            return "darkGray";
        } else if (color == Color.gray) {
            return "gray";
        } else if (color == Color.lightGray) {
            return "lightGray";
        } else if (color == Color.white) {
            return "white";
        } else if (color == Color.red) {
            return "red";
        } else if (color == Color.pink) {
            return "pink";
        } else if (color == Color.orange) {
            return "orange";
        } else if (color == Color.yellow) {
            return "yellow";
        } else if (color == Color.green) {
            return "green";
        } else if (color == Color.blue) {
            return "blue";
        } else if (color == Color.magenta) {
            return "magenta";
        } else if (color == Color.cyan) {
            return "cyan";
        } else if (color == null) {
            return "None";
        }
        log.error("unknown color sent to colorToString");
        return "black";
    }

    public static Color stringToColor(String string) {
        if (string.equals("black")) {
            return Color.black;
        } else if (string.equals("darkGray")) {
            return Color.darkGray;
        } else if (string.equals("gray")) {
            return Color.gray;
        } else if (string.equals("lightGray")) {
            return Color.lightGray;
        } else if (string.equals("white")) {
            return Color.white;
        } else if (string.equals("red")) {
            return Color.red;
        } else if (string.equals("pink")) {
            return Color.pink;
        } else if (string.equals("orange")) {
            return Color.orange;
        } else if (string.equals("yellow")) {
            return Color.yellow;
        } else if (string.equals("green")) {
            return Color.green;
        } else if (string.equals("blue")) {
            return Color.blue;
        } else if (string.equals("magenta")) {
            return Color.magenta;
        } else if (string.equals("cyan")) {
            return Color.cyan;
        } else if (string.equals("None")) {
            return null;
        }
        log.error("unknown color text '" + string + "' sent to stringToColor");
        return Color.black;
    }

    public int loadOrder() {
        if (jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class) == null) {
            jmri.InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);
        }
        return jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(EntryExitPairsXml.class.getName());
}

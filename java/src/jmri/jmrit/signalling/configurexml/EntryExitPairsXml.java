package jmri.jmrit.signalling.configurexml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import jmri.ConfigureManager;
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
 * This module handles configuration for the Entry Exit pairs used in
 * interlocking on a Layout Editor Panel.
 *
 * @author Kevin Dickerson Copyright (c) 2007
 */
public class EntryExitPairsXml extends AbstractXmlAdapter {

    public EntryExitPairsXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionablePoint.
     *
     * @param o Object to store, of type PositionablePoint
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        EntryExitPairs p = (EntryExitPairs) o;
        Element element = new Element("entryexitpairs");  // NOI18N 
        setStoreElementClass(element);
        ArrayList<LayoutEditor> editors = p.getSourcePanelList();
        if (editors.size() == 0) {
            return element;
        }
        element.addContent(new Element("cleardown").addContent("" + p.getClearDownOption()));  // NOI18N
        if (p.getDispatcherIntegration()) {
            element.addContent(new Element("dispatcherintegration").addContent("yes"));  // NOI18N
        }
        if (p.useDifferentColorWhenSetting()) {
            element.addContent(new Element("colourwhilesetting").addContent(colorToString(p.getSettingRouteColor())));  // NOI18N
            element.addContent(new Element("settingTimer").addContent("" + p.getSettingTimer()));  // NOI18N
        }
        for (int k = 0; k < editors.size(); k++) {
            LayoutEditor panel = editors.get(k);
            List<Object> nxpair = p.getSourceList(panel);

            Element panelElem = new Element("layoutPanel");  // NOI18N
            panelElem.setAttribute("name", panel.getLayoutName());  // NOI18N
            for (int j = 0; j < nxpair.size(); j++) {
                Object key = nxpair.get(j);
                Element source = new Element("source");  // NOI18N
                String type = "";
                String item = "";

                if (key instanceof SignalMast) {
                    type = "signalMast";  // NOI18N
                    item = ((SignalMast) key).getDisplayName();
                } else if (key instanceof Sensor) {
                    type = "sensor";  // NOI18N
                    item = ((Sensor) key).getDisplayName();
                } else if (key instanceof SignalHead) {
                    type = "signalHead";  // NOI18N
                    item = ((SignalHead) key).getDisplayName();
                }

                source.setAttribute("type", type);  // NOI18N
                source.setAttribute("item", item);  // NOI18N

                ArrayList<Object> a = p.getDestinationList(key, panel);
                for (int i = 0; i < a.size(); i++) {
                    Object keyDest = a.get(i);
                    String typeDest = "";
                    String itemDest = "";
                    if (keyDest instanceof SignalMast) {
                        typeDest = "signalMast";  // NOI18N
                        itemDest = ((SignalMast) keyDest).getDisplayName();
                    } else if (keyDest instanceof Sensor) {
                        typeDest = "sensor";  // NOI18N
                        itemDest = ((Sensor) keyDest).getDisplayName();
                    } else if (keyDest instanceof SignalHead) {
                        typeDest = "signalHead";  // NOI18N
                        itemDest = ((SignalHead) keyDest).getDisplayName();
                    }
                    Element dest = new Element("destination");  // NOI18N
                    dest.setAttribute("type", typeDest);  // NOI18N
                    dest.setAttribute("item", itemDest);  // NOI18N
                    if (!p.isUniDirection(key, panel, keyDest)) {
                        dest.setAttribute("uniDirection", "no");  // NOI18N
                    }
                    if (!p.isEnabled(key, panel, keyDest)) {
                        dest.setAttribute("enabled", "no");  // NOI18N
                    }
                    int nxType = p.getEntryExitType(key, panel, keyDest);
                    switch (nxType) {
                        case 0x00:
                            dest.setAttribute("nxType", "turnoutsetting");  // NOI18N
                            break;
                        case 0x01:
                            dest.setAttribute("nxType", "signalmastlogic");  // NOI18N
                            break;
                        case 0x02:
                            dest.setAttribute("nxType", "fullinterlocking");  // NOI18N
                            break;
                        default:
                            dest.setAttribute("nxType", "turnoutsetting");  // NOI18N
                            break;
                    }
                    if (p.getUniqueId(key, panel, keyDest) != null) {
                        dest.setAttribute("uniqueid", p.getUniqueId(key, panel, keyDest));  // NOI18N
                    }
                    source.addContent(dest);
                }
                panelElem.addContent(source);
            }
            element.addContent(panelElem);
        }
        return element;
    }

    /**
     * Define attribute for an element that is to be stored.
     *
     * @param messages Storage element
     */
    public void setStoreElementClass(Element messages) {
        messages.setAttribute("class", "jmri.jmrit.signalling.configurexml.EntryExitPairsXml");  // NOI18N
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");  // NOI18N
    }

    /**
     * Load, starting with the layoutBlock element, then all the value-icon
     * pairs.
     *
     * @param shared Top level Element to unpack
     * @param perNode ignored in this application
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // create the objects
        EntryExitPairs eep = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);

        try {
            String clearoption = shared.getChild("cleardown").getText();  // NOI18N
            eep.setClearDownOption(Integer.parseInt(clearoption));
        } catch (java.lang.NullPointerException e) {
            //Considered normal if it doesn't exist
        }
        // get attributes
        ConfigureManager cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        ArrayList<Object> loadedPanel;
        if (cm != null) {
            loadedPanel = cm.getInstanceList(LayoutEditor.class);
        } else {
            log.error("Failed getting optional default config manager");  // NOI18N
            loadedPanel = new ArrayList<Object>();
        }
        if (shared.getChild("dispatcherintegration") != null && shared.getChild("dispatcherintegration").getText().equals("yes")) {  // NOI18N
            eep.setDispatcherIntegration(true);
        }
        if (shared.getChild("colourwhilesetting") != null) {
            eep.setSettingRouteColor(stringToColor(shared.getChild("colourwhilesetting").getText()));  // NOI18N
            int settingTimer = 2000;
            try {
                settingTimer = Integer.parseInt(shared.getChild("settingTimer").getText());  // NOI18N
            } catch (Exception e) {
                log.error("Error in converting timer to int " + shared.getChild("settingTimer"));  // NOI18N
            }
            eep.setSettingTimer(settingTimer);
        }
        List<Element> panelList = shared.getChildren("layoutPanel");  // NOI18N
        for (int k = 0; k < panelList.size(); k++) {
            String panelName = panelList.get(k).getAttribute("name").getValue();  // NOI18N
            LayoutEditor panel = null;
            for (int i = 0; i < loadedPanel.size(); i++) {
                LayoutEditor tmp = (LayoutEditor) loadedPanel.get(i);
                if (tmp.getLayoutName().equals(panelName)) {
                    panel = tmp;
                    break;
                }
            }
            if (panel != null) {
                List<Element> sourceList = panelList.get(k).getChildren("source");  // NOI18N
                for (int i = 0; i < sourceList.size(); i++) {
                    String sourceType = sourceList.get(i).getAttribute("type").getValue();  // NOI18N
                    String sourceItem = sourceList.get(i).getAttribute("item").getValue();  // NOI18N
                    NamedBean source = null;
                    if (sourceType.equals("signalMast")) {  // NOI18N
                        source = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(sourceItem);
                    } else if (sourceType.equals("sensor")) {  // NOI18N
                        source = jmri.InstanceManager.sensorManagerInstance().getSensor(sourceItem);
                    } else if (sourceType.equals("signalHead")) {  // NOI18N
                        source = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(sourceItem);
                    }

                    //These two could be subbed off.
                    List<Element> destinationList = sourceList.get(i).getChildren("destination");  // NOI18N
                    if (destinationList.size() > 0) {
                        eep.addNXSourcePoint(source, panel);
                    }
                    for (int j = 0; j < destinationList.size(); j++) {
                        String id = null;
                        if (destinationList.get(j).getAttribute("uniqueid") != null) {  // NOI18N
                            id = destinationList.get(j).getAttribute("uniqueid").getValue();  // NOI18N
                        }
                        String destType = destinationList.get(j).getAttribute("type").getValue();  // NOI18N
                        String destItem = destinationList.get(j).getAttribute("item").getValue();  // NOI18N
                        NamedBean dest = null;
                        if (destType.equals("signalMast")) {  // NOI18N
                            dest = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(destItem);
                        } else if (destType.equals("sensor")) {  // NOI18N
                            dest = jmri.InstanceManager.sensorManagerInstance().getSensor(destItem);
                        } else if (destType.equals("signalHead")) {  // NOI18N
                            dest = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(destItem);
                        }
                        try {
                            eep.addNXDestination(source, dest, panel, id);
                        } catch (java.lang.NullPointerException e) {
                            log.error("An error occurred while trying to add a point");  // NOI18N
                        }
                        if ((destinationList.get(j).getAttribute("uniDirection") != null) && (destinationList.get(j).getAttribute("uniDirection").getValue().equals("no"))) {  // NOI18N
                            eep.setUniDirection(source, panel, dest, false);
                        }
                        if ((destinationList.get(j).getAttribute("enabled") != null) && (destinationList.get(j).getAttribute("enabled").getValue().equals("no"))) {  // NOI18N
                            eep.setEnabled(source, panel, dest, false);
                        }
                        if (destinationList.get(j).getAttribute("nxType") != null) {  // NOI18N
                            String nxType = destinationList.get(j).getAttribute("nxType").getValue();  // NOI18N
                            if (nxType.equals("turnoutsetting")) {  // NOI18N
                                eep.setEntryExitType(source, panel, dest, 0x00);
                            } else if (nxType.equals("signalmastlogic")) {  // NOI18N
                                eep.setEntryExitType(source, panel, dest, 0x01);
                            } else if (nxType.equals("fullinterlocking")) {  // NOI18N
                                eep.setEntryExitType(source, panel, dest, 0x02);
                            }

                        }
                    }
                }
            } else {
                log.error("Panel has not been loaded");  // NOI18N
            }
        }
        return true;
    }

    /**
     * Get a descriptive name for a given color value.
     *
     * @param color Integer value of a color to display on screen
     * @return lower case color name in English; None if color entered is null
     */
    public static String colorToString(Color color) {
        if (color == Color.black) {
            return "black";  // NOI18N
        } else if (color == Color.darkGray) {
            return "darkGray";  // NOI18N
        } else if (color == Color.gray) {
            return "gray";  // NOI18N
        } else if (color == Color.lightGray) {
            return "lightGray";  // NOI18N
        } else if (color == Color.white) {
            return "white";  // NOI18N
        } else if (color == Color.red) {
            return "red";  // NOI18N
        } else if (color == Color.pink) {
            return "pink";  // NOI18N
        } else if (color == Color.orange) {
            return "orange";  // NOI18N
        } else if (color == Color.yellow) {
            return "yellow";  // NOI18N
        } else if (color == Color.green) {
            return "green";  // NOI18N
        } else if (color == Color.blue) {
            return "blue";  // NOI18N
        } else if (color == Color.magenta) {
            return "magenta";  // NOI18N
        } else if (color == Color.cyan) {
            return "cyan";
        } else if (color == null) {
            return "None";  // NOI18N
        }
        log.error("unknown color sent to colorToString");  // NOI18N
        return "black";  // NOI18N
    }

    /**
     * Get a color value for a color name.
     *
     * @param string String describing a color
     * @return integer representing a screen color
     */
    public static Color stringToColor(String string) {
        if (string.equals("black")) {  // NOI18N
            return Color.black;
        } else if (string.equals("darkGray")) {  // NOI18N
            return Color.darkGray;
        } else if (string.equals("gray")) {  // NOI18N
            return Color.gray;
        } else if (string.equals("lightGray")) {  // NOI18N
            return Color.lightGray;
        } else if (string.equals("white")) {  // NOI18N
            return Color.white;
        } else if (string.equals("red")) {  // NOI18N
            return Color.red;
        } else if (string.equals("pink")) {  // NOI18N
            return Color.pink;
        } else if (string.equals("orange")) {  // NOI18N
            return Color.orange;
        } else if (string.equals("yellow")) {  // NOI18N
            return Color.yellow;
        } else if (string.equals("green")) {  // NOI18N
            return Color.green;
        } else if (string.equals("blue")) {  // NOI18N
            return Color.blue;
        } else if (string.equals("magenta")) {  // NOI18N
            return Color.magenta;
        } else if (string.equals("cyan")) {  // NOI18N
            return Color.cyan;
        } else if (string.equals("None")) {  // NOI18N
            return null;
        }
        log.error("unknown color text '" + string + "' sent to stringToColor");  // NOI18N
        return Color.black;
    }

    @Override
    public int loadOrder() {
        if (jmri.InstanceManager.getNullableDefault(jmri.jmrit.signalling.EntryExitPairs.class) == null) {
            jmri.InstanceManager.store(new EntryExitPairs(), EntryExitPairs.class);
        }
        return jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(EntryExitPairsXml.class.getName());
}

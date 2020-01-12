package jmri.jmrit.entryexit.configurexml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.util.ColorUtil;
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
        List<LayoutEditor> editors = p.getSourcePanelList();
        if (editors.isEmpty()) {
            return null;    //return element;   // <== don't store empty (unused) element
        }

        element.addContent(new Element("cleardown").addContent("" + p.getClearDownOption()));  // NOI18N
        if (p.getDispatcherIntegration()) {
            element.addContent(new Element("dispatcherintegration").addContent("yes"));  // NOI18N
        }
        if (p.useDifferentColorWhenSetting()) {
            element.addContent(new Element("colourwhilesetting").addContent(ColorUtil.colorToColorName(p.getSettingRouteColor())));  // NOI18N
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

                List<Object> a = p.getDestinationList(key, panel);
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
                        case 0x01:
                            dest.setAttribute("nxType", "signalmastlogic");  // NOI18N
                            break;
                        case 0x02:
                            dest.setAttribute("nxType", "fullinterlocking");  // NOI18N
                            break;
                        case 0x00:
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
     * @param shared  Top level Element to unpack
     * @param perNode ignored in this application
     * @return true if loaded without errors; false otherwise
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // create the objects
        EntryExitPairs eep = InstanceManager.getDefault(EntryExitPairs.class);

        try {
            String clearoption = shared.getChild("cleardown").getText();  // NOI18N
            eep.setClearDownOption(Integer.parseInt(clearoption));
        } catch (java.lang.NullPointerException e) {
            //Considered normal if it doesn't exist
        }
        // get attributes
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        List<Object> loadedPanel;
        if (cm != null) {
            loadedPanel = cm.getInstanceList(LayoutEditor.class);
        } else {
            log.error("Failed getting optional default config manager");  // NOI18N
            loadedPanel = new ArrayList<>();
        }
        if (shared.getChild("dispatcherintegration") != null && shared.getChild("dispatcherintegration").getText().equals("yes")) {  // NOI18N
            eep.setDispatcherIntegration(true);
        }
        if (shared.getChild("colourwhilesetting") != null) {
            try {
                eep.setSettingRouteColor(ColorUtil.stringToColor(shared.getChild("colourwhilesetting").getText()));  // NOI18N
            } catch (IllegalArgumentException e) {
                eep.setSettingRouteColor(Color.BLACK);
                log.error("Invalid color {}; using black", shared.getChild("colourwhilesetting").getText());
            }
            int settingTimer = 2000;
            try {
                settingTimer = Integer.parseInt(shared.getChild("settingTimer").getText());  // NOI18N
            } catch (NumberFormatException e) {
                log.error("Error in converting timer to int {}", shared.getChild("settingTimer"));  // NOI18N
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
                    switch (sourceType) {
                        case "signalMast": // NOI18N
                            source = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(sourceItem);
                            break;
                        case "sensor": // NOI18N
                            source = InstanceManager.getDefault(SensorManager.class).getSensor(sourceItem);
                            break;
                        case "signalHead": // NOI18N
                            source = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(sourceItem);
                            break;
                        default:
                            break;
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
                            if (!id.startsWith("IN:")) {     // NOI18N
                                id = "IN:" + id;    // NOI18N
                            }
                        }
                        String destType = destinationList.get(j).getAttribute("type").getValue();  // NOI18N
                        String destItem = destinationList.get(j).getAttribute("item").getValue();  // NOI18N
                        NamedBean dest = null;
                        switch (destType) {
                            case "signalMast": // NOI18N
                                dest = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(destItem);
                                break;
                            case "sensor": // NOI18N
                                dest = InstanceManager.getDefault(SensorManager.class).getSensor(destItem);
                                break;
                            case "signalHead": // NOI18N
                                dest = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(destItem);
                                break;
                            default:
                                break;
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
                            switch (nxType) {
                                case "turnoutsetting": // NOI18N
                                    eep.setEntryExitType(source, panel, dest, 0x00);
                                    break;
                                case "signalmastlogic": // NOI18N
                                    eep.setEntryExitType(source, panel, dest, 0x01);
                                    break;
                                case "fullinterlocking": // NOI18N
                                    eep.setEntryExitType(source, panel, dest, 0x02);
                                    break;
                                default:
                                    break;
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

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(EntryExitPairs.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(EntryExitPairsXml.class);
}

// EntryExitPairsXml.java

package jmri.jmrit.signalling.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import org.jdom.Attribute;
import org.jdom.Element;
import java.util.ArrayList;
import java.util.List;
import jmri.SignalMast;
import jmri.SignalHead;
import jmri.Sensor;
import jmri.NamedBean;

/**
 * This module handles configuration for display.PositionablePoint objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.2 $
 */
public class EntryExitPairsXml extends AbstractXmlAdapter {

    public EntryExitPairsXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionablePoint
     * @param o Object to store, of type PositionablePoint
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        EntryExitPairs p = (EntryExitPairs)o;
        Element element = new Element("entryexitpairs");
        setStoreElementClass(element);
        ArrayList<LayoutEditor> editors = p.getSourcePanelList();
        if (editors.size()==0) return element;
        for (int k = 0; k<editors.size(); k++){
            LayoutEditor panel = editors.get(k);
            List<Object> nxpair = p.getSourceList(panel);
            if(nxpair!=null){
                Element panelElem = new Element("layoutPanel");
                panelElem.setAttribute("name", panel.getLayoutName());
                for (int j = 0; j<nxpair.size(); j++){
                    Object key = nxpair.get(j);
                    Element source = new Element("source");
                    String type = "";
                    String item = "";
                    
                    if(key instanceof SignalMast){
                        type="signalMast";
                        item = ((SignalMast)key).getDisplayName();
                    } else if (key instanceof Sensor) {
                        type = "sensor";
                        item = ((Sensor)key).getDisplayName();
                    } else if (key instanceof SignalHead){
                        type = "signalHead";
                        item = ((SignalHead)key).getDisplayName();
                    }
                    
                    source.setAttribute("type", type);
                    source.setAttribute("item", item);

                    ArrayList<Object> a = p.getDestinationList(key, panel);
                    for (int i = 0; i<a.size(); i++){
                        Object keyDest = a.get(i);
                        String typeDest = "";
                        String itemDest = "";
                        if(keyDest instanceof SignalMast){
                            typeDest="signalMast";
                            itemDest = ((SignalMast)keyDest).getDisplayName();
                        } else if (keyDest instanceof Sensor) {
                            typeDest = "sensor";
                            itemDest = ((Sensor)keyDest).getDisplayName();
                        } else if (keyDest instanceof SignalHead){
                            typeDest = "signalHead";
                            itemDest = ((SignalHead)keyDest).getDisplayName();
                        }
                        Element dest = new Element("destination");
                        dest.setAttribute("type", typeDest);
                        dest.setAttribute("item", itemDest);
                        if(!p.isUniDirection(key, panel, keyDest))
                            dest.setAttribute("uniDirection", "no");
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
        messages.setAttribute("class","jmri.jmrit.signalling.configurexml.EntryExitPairsXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Load, starting with the layoutblock element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     */
    @SuppressWarnings({ "unchecked", "null" })
    public boolean load(Element element) {
        // create the objects
		EntryExitPairs eep = EntryExitPairs.instance();
		// get attributes
        ArrayList<Object> loadedPanel = jmri.InstanceManager.configureManagerInstance().getInstanceList(LayoutEditor.class);
        @SuppressWarnings("unchecked")
        List<Element> panelList = element.getChildren("layoutPanel");
        for(int k = 0; k<panelList.size(); k++){
            String panelName = panelList.get(k).getAttribute("name").getValue();
            LayoutEditor panel = null;
            for (int i=0; i<loadedPanel.size(); i++){
                LayoutEditor tmp = (LayoutEditor) loadedPanel.get(i);
                if (tmp.getLayoutName().equals(panelName)){
                    panel = tmp;
                    break;
                }
            }
            if(panel!=null){
                List<Element> sourceList = panelList.get(k).getChildren("source");
                for (int i = 0; i < sourceList.size(); i++) {
                    String sourceType = sourceList.get(i).getAttribute("type").getValue();
                    String sourceItem = sourceList.get(i).getAttribute("item").getValue();
                    NamedBean source = null;
                    if(sourceType.equals("signalMast")){
                        source = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(sourceItem);
                    } else if (sourceType.equals("sensor")){
                        source = jmri.InstanceManager.sensorManagerInstance().getSensor(sourceItem);
                    } else if (sourceType.equals("signalHead")){
                        source = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(sourceItem);
                    }
                    
                    //These two could be subbed off.
                    List<Element> destinationList = sourceList.get(i).getChildren("destination");
                    if(destinationList.size()>0){
                        eep.addNXSourcePoint(source, panel);
                    }
                    for (int j = 0; j < destinationList.size(); j++) {
                        String destType = destinationList.get(j).getAttribute("type").getValue();
                        String destItem = destinationList.get(j).getAttribute("item").getValue();
                        NamedBean dest = null;
                        if(destType.equals("signalMast")){
                            dest = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(destItem);
                        } else if (destType.equals("sensor")){
                            dest = jmri.InstanceManager.sensorManagerInstance().getSensor(destItem);
                        } else if (destType.equals("signalHead")){
                            dest = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(destItem);
                        }
                        try {
                            eep.addNXDestination(source, dest, panel);
                        } catch (java.lang.NullPointerException e) {
                            log.error("An error occured while trying to add a point");
                        }
                        if((destinationList.get(j).getAttribute("uniDirection")!=null) && (destinationList.get(j).getAttribute("uniDirection").getValue().equals("no"))){
                            eep.setUniDirection(source, panel, dest, false);
                        }
                    }
                }
            } else {
                log.error("Panel has not been loaded");
            }
        }
        return true;
    }
    
    public int loadOrder(){
        return 900;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EntryExitPairsXml.class.getName());
}
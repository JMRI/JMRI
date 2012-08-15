// DefaultXmlIOServer.java

package jmri.web.xmlio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jmri.*;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import jmri.web.server.WebServerManager;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Default implementation for XML I/O.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2008, 2009, 2010
 * @version	$Revision$
 * @see  jmri.web.xmlio.XmlIOFactory
 */
public class DefaultXmlIOServer implements XmlIOServer {
	
	static List<String> disallowedFrames = WebServerManager.getWebServerPreferences().getDisallowedFrames();

    @Override
    public Element immediateRequest(Element e) throws JmriException {

        // process panels and frames as the same elements through 2.14.
        // after 2.14, process panels as XML definitions of panels so that
        // iPads or Android tablet apps could directly render the panels.

        // first, process any "list" elements
    	//  roster, frame, and metadata are immediate only
    	//  power, turnout, sensor, memory and route can be monitored for changes, pass current values to begin
        @SuppressWarnings("unchecked")
        List<Element> lists = new ArrayList<Element>(e.getChildren("list"));
        for (Element list : lists) {
            e.removeContent(list);
            // if there is no attribute named "type", type is null
            String type = list.getAttributeValue("type");
            // if type is not null, use the attribute-based syntax
            useAttributes = (type != null);
            if (!useAttributes) {
                // if type is null, set type from a child element named "type"
                type = list.getChild("type").getText();
            }
            if (type == null){
            	log.error("type not found!");
            	continue;		         			
            }
            if (type.equals("turnout")) {
                // add an element for each turnout
                TurnoutManager m = InstanceManager.turnoutManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Turnout t = m.getTurnout(name);
                    Element n = new Element((useAttributes) ? "turnout" : "item");
                    if (useAttributes) {
                        n.setAttribute("name", name);
                        if (t.getUserName() != null) n.setAttribute("userName", t.getUserName());
                        if (t.getComment() != null) n.setAttribute("comment", t.getComment());
                        n.setAttribute("inverted", Boolean.valueOf(t.getInverted()).toString());
                    } else {
                    n.addContent(new Element("type").addContent("turnout"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    n.addContent(new Element("inverted").addContent(Boolean.valueOf(t.getInverted()).toString()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals("memory")) {
                // add an element for each memory
            	MemoryManager m = InstanceManager.memoryManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Memory t = m.getMemory(name);
                    Element n = new Element((useAttributes) ? "memory" : "item");
                    if (useAttributes) {
                        n.setAttribute("name", name);
                        if (t.getUserName() != null) n.setAttribute("userName", t.getUserName());
                        if (t.getComment() != null) n.setAttribute("comment", t.getComment());
                    } else {
                    n.addContent(new Element("type").addContent("memory"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals("route")) {
                // add an element for each route
                RouteManager m = InstanceManager.routeManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Route t = m.getRoute(name);
                    Element n = new Element((useAttributes) ? "route" : "item");
                    if (useAttributes) {
                        n.setAttribute("name", name);
                        if (t.getUserName() != null) n.setAttribute("userName", t.getUserName());
                        if (t.getComment() != null) n.setAttribute("comment", t.getComment());
                    } else {
                    n.addContent(new Element("type").addContent("route"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals("sensor")) {
                // add an element for each sensor
                SensorManager m = InstanceManager.sensorManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Sensor t = m.getSensor(name);
                    Element n = new Element((useAttributes) ? "sensor" : "item");
                    if (useAttributes) {
                        n.setAttribute("name", name);
                        if (t.getUserName() != null) n.setAttribute("userName", t.getUserName());
                        if (t.getComment() != null) n.setAttribute("comment", t.getComment());
                        n.setAttribute("inverted", Boolean.valueOf(t.getInverted()).toString());
                    } else {
                    n.addContent(new Element("type").addContent("sensor"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    n.addContent(new Element("inverted").addContent(Boolean.valueOf(t.getInverted()).toString()));
                    }
                    e.addContent(n);
                }            
            } else if (type.equals("roster")) {
            	// add an element for each roster entry
            	List <RosterEntry> rlist = Roster.instance().matchingList(null, null, null, null, null, null, null);
            	for (int i = 0; i < rlist.size(); i++) {
            		RosterEntry entry = rlist.get(i);
            		Element n = new Element((useAttributes) ? "roster" : "item");
                        if (useAttributes) {
                            n.setAttribute("name", entry.getId());
                            n.setAttribute("dccAddress", entry.getDccAddress());
                            n.setAttribute("addressLength", entry.isLongAddress() ? "L" : "S");
                            n.setAttribute("roadName", entry.getRoadName());
                            n.setAttribute("roadNumber", entry.getRoadNumber());
                            n.setAttribute("mfg", entry.getMfg());
                            n.setAttribute("model", entry.getModel());
                            n.setAttribute("comment", entry.getComment());
                            n.setAttribute("maxSpeedPct", Integer.valueOf(entry.getMaxSpeedPCT()).toString());
                            File file = new File(entry.getImagePath());
                            n.setAttribute("imageFileName", file.getName());
                            file = new File(entry.getIconPath());
                            n.setAttribute("imageIconName", file.getName());
                            Element f;
                            for (int j = 0; j < entry.getMAXFNNUM(); j++) {
                                if (entry.getFunctionLabel(j) != null) {
                                    f = new Element("function");
                                    f.setAttribute("name", "F" + j);
                                    f.setAttribute("label", entry.getFunctionLabel(j));
                                    f.setAttribute("lockable", Boolean.valueOf(entry.getFunctionLockable(j)).toString());
                                    n.addContent(f);
                                }
                            }
                        } else {
            		n.addContent(new Element("type").addContent("roster"));
            		n.addContent(new Element("name").addContent(entry.getId()));
            		n.addContent(new Element("dccAddress").addContent(entry.getDccAddress()));
            		n.addContent(new Element("addressLength").addContent(entry.isLongAddress() ? "L" : "S"));
            		n.addContent(new Element("roadName").addContent(entry.getRoadName()));
            		n.addContent(new Element("roadNumber").addContent(entry.getRoadNumber()));
            		n.addContent(new Element("mfg").addContent(entry.getMfg()));
            		n.addContent(new Element("model").addContent(entry.getModel()));
            		n.addContent(new Element("comment").addContent(entry.getComment()));
            		n.addContent(new Element("maxSpeedPct").addContent(Integer.valueOf(entry.getMaxSpeedPCT()).toString()));
            		File file = new File(entry.getImagePath());
            		n.addContent(new Element("imageFileName").addContent(file.getName()));
            		file = new File(entry.getIconPath());
            		n.addContent(new Element("imageIconName").addContent(file.getName()));
        			Element f = new Element("functionLabels");
        			Element g = new Element("functionLockables");
                	for (int j = 0; j < entry.getMAXFNNUM(); j++) {
                		if (entry.getFunctionLabel(j) != null) {
                    		f.addContent(new Element("F" + j).addContent(entry.getFunctionLabel(j)));
                    		g.addContent(new Element("F" + j).addContent(Boolean.valueOf(entry.getFunctionLockable(j)).toString()));
                		}
            		}
        			n.addContent(f);
        			n.addContent(g);
                        }
        			e.addContent(n);
            	}

            } else if (type.equals("frame")) {
            	
            	// list frames, (open JMRI windows)
                List<JmriJFrame> frames = JmriJFrame.getFrameList();
                for (JmriJFrame frame : frames) { //add all non-blank titles to list
                    if (frame.getAllowInFrameServlet()) {
                        String frameTitle = frame.getTitle();
                        if (!frameTitle.equals("") && !disallowedFrames.contains(frameTitle)) {
                            Element n = new Element((useAttributes) ? "frame" : "item");
                            if (useAttributes) {
                                n.setAttribute("name", frameTitle.replaceAll(" ", "%20"));
                                n.setAttribute("userName", frameTitle);
                            } else {
                                n.addContent(new Element("type").addContent("frame"));
                                //get rid of spaces in name
                                n.addContent(new Element("name").addContent(frameTitle.replaceAll(" ", "%20")));
                                n.addContent(new Element("userName").addContent(frameTitle));
                            }
                            e.addContent(n);
                        }
                    }
                }

            } else if (type.equals("panel")) {

            	// list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor)
                List<JmriJFrame> frames = JmriJFrame.getFrameList(ControlPanelEditor.class);
                for (JmriJFrame frame : frames) {
                    if (frame.getAllowInFrameServlet()) {
                        String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                        if (!title.equals("") && !disallowedFrames.contains(title)) {
                            Element n = new Element((useAttributes) ? "panel" : "item");
                            if (useAttributes) {
                            	n.setAttribute("name", "ControlPanel/" + title.replaceAll(" ", "%20"));
                            	n.setAttribute("userName", title);
                            	n.setAttribute("type", "Control Panel");
                            } else {
                                n.addContent(new Element("type").addContent("panel"));
                                n.addContent(new Element("name").addContent("ControlPanel/" + title.replaceAll(" ", "%20")));
                                n.addContent(new Element("userName").addContent(title));
                            }
                            e.addContent(n);
                        }
                    }
                }
                frames = JmriJFrame.getFrameList(PanelEditor.class);
                for (JmriJFrame frame : frames) {
                    if (frame.getAllowInFrameServlet()) {
                        String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                        if (!title.equals("") && !disallowedFrames.contains(title)) {
                            Element n = new Element((useAttributes) ? "panel" : "item");
                            if (useAttributes) {
                            	n.setAttribute("name", "Panel/" + title.replaceAll(" ", "%20"));
                            	n.setAttribute("userName", title);
                            	n.setAttribute("type", "Panel");
                            } else {
                                n.addContent(new Element("type").addContent("panel"));
                                n.addContent(new Element("name").addContent("Panel/" + title.replaceAll(" ", "%20")));
                                n.addContent(new Element("userName").addContent(title));
                            }
                            e.addContent(n);
                        }
                    }
                }
                frames = JmriJFrame.getFrameList(LayoutEditor.class);
                for (JmriJFrame frame : frames) {
                    if (frame.getAllowInFrameServlet()) {
                        String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                        if (!title.equals("") && !disallowedFrames.contains(title)) {
                            Element n = new Element((useAttributes) ? "panel" : "item");
                            if (useAttributes) {
                            	n.setAttribute("name", "Layout/" + title.replaceAll(" ", "%20"));
                            	n.setAttribute("userName", title);
                            	n.setAttribute("type", "Layout");
                            } else {
                                n.addContent(new Element("type").addContent("panel"));
                                n.addContent(new Element("name").addContent("Layout/" + title.replaceAll(" ", "%20")));
                                n.addContent(new Element("userName").addContent(title));
                            }
                            e.addContent(n);
                        }
                    }
                }

            } else if (type.equals("power")) {
            	// add a power element
                Element n = new Element((useAttributes) ? "power" : "item");
                if (useAttributes) {
                    n.setAttribute("name", "power");
                } else {
                n.addContent(new Element("type").addContent("power"));
                n.addContent(new Element("name").addContent("power"));
                }
                e.addContent(n);
            } else if (type.equals("metadata")) {
                // list meta data elements
                List<String> metaNames = Metadata.getSystemNameList();
                for (String mn : metaNames) {
                    Element n = new Element((useAttributes) ? "metadata" : "item");
                    if (useAttributes) {
                        n.setAttribute("name", mn);
                    } else {
                    n.addContent(new Element("type").addContent("metadata"));
                    n.addContent(new Element("name").addContent("" + mn));
                    }
                    e.addContent(n);
                }
            } else if (type.equals("railroad")) {
                // return the Web Server's Railroad name preference
                Element n = new Element("railroad");
                n.setAttribute("name", WebServerManager.getWebServerPreferences().getRailRoadName());
                e.addContent(n);
            } else log.warn("Unexpected type in list element: " + type);
        }
        
        // handle everything else
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();
        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue("name");
            useAttributes = (!type.equals("item"));
            if (!useAttributes) {
                type = item.getChild("type").getText();
                name = item.getChild("name").getText();
            } else if (name == null) {
                name = "";
            }
        
            //check for "set" values and process them
            if (type.equals("throttle")) {
                immediateSetThrottle(item);
            } else if (type.equals("turnout")) {
                immediateWriteTurnout(name, item);
                immediateReadTurnout(name, item);
            } else if (type.equals("memory")) {
                immediateWriteMemory(name, item);
                immediateReadMemory(name, item);
            } else if (type.equals("route")) {
                immediateWriteRoute(name, item);
                immediateReadRoute(name, item);
            } else if (type.equals("sensor")) {
                immediateWriteSensor(name, item);
                immediateReadSensor(name, item);
            } else if (type.equals("power")) {
                immediateWritePower(name, item);
                immediateReadPower(name, item);
            } else if (type.equals("metadata")) {
                immediateReadMetadata(name, item);
            } else if (type.equals("roster")) {
                // nothing to process
            } else if (type.equals("frame")) {
                // nothing to process
            } else if (type.equals("panel")) {
                // nothing to process
            } else if (type.equals("railroad")) {
                // nothing to process
            } else if (item.getName().equals("item")) {
                log.warn("Unexpected type in item: " + type);
            } else {
                log.warn("Unexpected element: " + type);
            }
        }
        
        return e;
    }

    @Override
    public void monitorRequest(Element e, XmlIORequestor r) throws JmriException {
        
        // check for differences now
        if (checkValues(e)) {

            // differences found, now process the read and return  
            sendMonitorReply(e, r);
            return;
        }
        // No differences, have to wait.
        DeferredRead dr = new DeferredRead();
        dr.request = e;
        dr.requestor = r;
        
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();

        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue("name");
            useAttributes = (!type.equals("item"));
            if (!useAttributes) {
                type = item.getChild("type").getText();
                name = item.getChild("name").getText();
            } else if (name == null) {
                name = "";
            }
            
            if (type.equals("turnout")) addListenerToTurnout(name, item, dr);
            else if (type.equals("memory")) addListenerToMemory(name, item, dr);
            else if (type.equals("route")) addListenerToRoute(name, item, dr);
            else if (type.equals("sensor")) addListenerToSensor(name, item, dr);
            else if (type.equals("power")) addListenerToPower(name, item, dr);
            else log.warn("Unexpected type: " + type);
        }

        // Check one more time to ensure clear of race conditions
        dr.propertyChange(null);

    }

    void sendMonitorReply(Element e, XmlIORequestor r) {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();
        
        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue("name");
            useAttributes = (!type.equals("item"));
            if (!useAttributes) {
                type = item.getChild("type").getText();
                name = item.getChild("name").getText();
            } else if (name == null) {
                name = "";
            }
            
            try {
                if (type.equals("turnout")) immediateReadTurnout(name, item);
                else if (type.equals("memory")) immediateReadMemory(name, item);
                else if (type.equals("sensor")) immediateReadSensor(name, item);
                else if (type.equals("route")) immediateReadRoute(name, item);
                else if (type.equals("power")) immediateReadPower(name, item);
                else if (type.equals("metadata")) immediateReadMetadata(name, item);
            } catch (JmriException j) {
                log.warn("exception handling " + type + " " + name, j);
            }
        }
        
        r.monitorReply(e);
    }
    
    boolean checkValues(Element e) {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();
        
        boolean changed = false;
        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue("name");
            useAttributes = (!type.equals("item"));
            if (!useAttributes) {
                type = item.getChild("type").getText();
                name = item.getChild("name").getText();
            } else if (name == null) {
                name = "";
            }
            if (item.getAttribute("value") == null &&
                    item.getChild("value") == null) return true;  // if no value, consider changed

            try {
                if (type.equals("turnout")) changed |= monitorProcessTurnout(name, item);
                else if (type.equals("memory")) changed |= monitorProcessMemory(name, item);
                else if (type.equals("sensor")) changed |= monitorProcessSensor(name, item);
                else if (type.equals("route")) changed |= monitorProcessRoute(name, item);
                else if (type.equals("power")) changed |= monitorProcessPower(name, item);
                else if (type.equals("metadata")) changed = true;
                else log.warn("Unexpected type: "+type);
            } catch (JmriException j) {
                log.warn("exception handling "+type+" "+name, j);
            }
        }
        return changed;
    }
    
    void addListenerToTurnout(String name, Element item, DeferredRead dr) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        b.addPropertyChangeListener(dr);
    }
    
    void addListenerToMemory(String name, Element item, DeferredRead dr) {
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);
        b.addPropertyChangeListener(dr);
    }
    
    void addListenerToRoute(String name, Element item, DeferredRead dr) {
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);
        b.addPropertyChangeListener(dr);
    }
    
    void addListenerToSensor(String name, Element item, DeferredRead dr) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);
        b.addPropertyChangeListener(dr);
    }
    
    void addListenerToPower(String name, Element item, DeferredRead dr) {
        PowerManager b = InstanceManager.powerManagerInstance();
        b.addPropertyChangeListener(dr);
    }
    
    void removeListenerFromTurnout(String name, Element item, DeferredRead dr) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        b.removePropertyChangeListener(dr);
    }
    
    void removeListenerFromMemory(String name, Element item, DeferredRead dr) {
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);
        b.removePropertyChangeListener(dr);
    }
    
    void removeListenerFromRoute(String name, Element item, DeferredRead dr) {
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);
        b.removePropertyChangeListener(dr);
    }
    
    void removeListenerFromSensor(String name, Element item, DeferredRead dr) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);
        b.removePropertyChangeListener(dr);
    }
    
    void removeListenerFromPower(String name, Element item, DeferredRead dr) {
        PowerManager b = InstanceManager.powerManagerInstance();
        b.removePropertyChangeListener(dr);
    }
    
    /** Return true if there is a difference   */
    boolean monitorProcessTurnout(String name, Element item) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for value element, which means compare
        if (item.getAttributeValue("value") != null) {
            return (b.getKnownState() != Integer.parseInt(item.getAttributeValue("value")));
        } else {
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            return  (b.getKnownState() != state);
        }
        }
        return false;  // no difference
    }
    
    /** Return true if there is a difference   */
    boolean monitorProcessMemory(String name, Element item) {
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);

        String s = (b.getValue() != null) ? b.getValue().toString() : "";
        // check for value element, which means compare
        // return true if strings are different
        if (item.getAttributeValue("value") != null) {
            return (!s.equals(item.getAttributeValue("value")));
        } else if (item.getChild("value") != null) {
            return (!s.equals(item.getChildText("value")));
        }
        return false;  // no difference
    }

    /** Return true if there is a difference in passed in route  */
    boolean monitorProcessRoute(String name, Element item) {
    	int newState = 0;  //default to unknown
    	RouteManager manager = InstanceManager.routeManagerInstance();
    	Route r = manager.getBySystemName(name);
    	String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
		if (turnoutsAlignedSensor != "") {  //only set if found
			Sensor routeAligned = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
	        newState = (routeAligned != null) ? routeAligned.getKnownState() : 0;  //default to unknown
		}

		int state;
        // check for value element, which means compare
        if (item.getAttributeValue("value") != null) {
            state = Integer.parseInt(item.getAttributeValue("value"));
        } else {
            state = (item.getChild("value") != null) ? Integer.parseInt(item.getChildText("value")) : 0; // default to unknown
        }
        return (newState != state); // return true if states are different
    }
    
    /**
     * Return true if there is a difference
     */
    boolean monitorProcessSensor(String name, Element item) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        // check for value element, which means compare
        if (item.getAttributeValue("value") != null) {
            return (b.getState() != Integer.parseInt(item.getAttributeValue("value")));
        } else {
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            return  (b.getState() != state);
        }
        }
        return false;  // no difference
    }
    
    /**
     * Return true if there is a difference
     */
    boolean monitorProcessPower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        if (b == null) {
            return true; // immediately reply if there is no PowerManager
        }
        // check for value element, which means compare
        if (item.getAttributeValue("value") != null) {
            return (b.getPower() != Integer.parseInt(item.getAttributeValue("value")));
        } else {
            Element v = item.getChild("value");
            if (v!=null) {
                int state = Integer.parseInt(v.getText());
                return  (b.getPower() != state);
            }
        }
        return false;  // no difference
    }
    
    void immediateWriteTurnout(String name, Element item) {
        // get turnout
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for set element, which means write
        if (item.getAttributeValue("set") != null) {
            b.setCommandedState(Integer.parseInt(item.getAttributeValue("set")));
            item.removeAttribute("set");
        } else {
        Element v = item.getChild("set");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setCommandedState(state);
            item.removeContent(v);
        }
    }
    }    
    
    void immediateWriteMemory(String name, Element item) throws JmriException {
        // get memory
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);

        // check for set element, which means write
        if (item.getAttributeValue("set") != null) {
            if (item.getAttribute("isNull") != null &&
                    item.getAttributeValue("isNull").equals(Boolean.toString(true))) {
                b.setValue(null);
            } else {
                b.setValue(item.getAttributeValue("set"));
            }
            item.removeAttribute("set");
            item.removeAttribute("isNull");
        } else {
            Element v = item.getChild("set");
            if (v != null) {
                if (item.getAttribute("isNull") != null &&
                        item.getAttributeValue("isNull").equals(Boolean.toString(true))) {
                    b.setValue(null);
                } else {
                    String state = v.getText();
                    b.setValue(state);
                }
                item.removeContent(v);
            }
        }
    }

    void immediateWriteSensor(String name, Element item) throws JmriException {
        // get sensor
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        // check for set element, which means write
        if (item.getAttributeValue("set") != null) {
            b.setState(Integer.parseInt(item.getAttributeValue("set")));
            item.removeAttribute("set");
        } else {
        Element v = item.getChild("set");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setState(state);
            item.removeContent(v);
        }
    }
    }
    
    void immediateWriteRoute(String name, Element item) throws JmriException {
        // get route
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);

        if (item.getAttributeValue("set") != null) {
            b.setRoute();
            item.removeAttribute("set");
        } else {
        // check for set element, which means write
        Element v = item.getChild("set");
        if (v!=null) {
//            int state = Integer.parseInt(v.getText());
            b.setRoute(); 
            item.removeContent(v);
        }
    }
    }
    
    void immediateWritePower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        // if the PowerManager is null, quietly do nothing, since
        // immediateReadPower(name, item) assembles a message for the
        // client indicating that the PowerManager is null
        if (b != null) {
            // check for set element, which means write
            if (item.getAttributeValue("set") != null) {
                b.setPower(Integer.parseInt(item.getAttributeValue("set")));
                item.removeAttribute("set");
                //item.setAttribute("value", Integer.toString(b.getPower()));
            } else {
                Element v = item.getChild("set");
                if (v!=null) {
                    int state = Integer.parseInt(v.getText());
                    b.setPower(state);
                    // remove set element
                    item.removeContent(v);
                }
            }
        }
    }
    
    void immediateWriteRoster(String name, Element item) throws JmriException {
        log.error("no immediate write for roster element");
    }
    
    void immediateWriteFrame(String name, Element item) throws JmriException {
        log.error("no immediate write for frame element");
    }
    
    void immediateWriteMetadata(String name, Element item) throws JmriException {
        log.error("no immediate write for metadata element");
    }

    void immediateReadTurnout(String name, Element item) {
        // get turnout
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        if (useAttributes) {
            item.setAttribute("value", Integer.toString(b.getKnownState()));
        } else {
        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getKnownState());
    }
    }
    
    void immediateReadMemory(String name, Element item) {
        // get memory
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);

        String s = (b.getValue() != null) ? b.getValue().toString() : "";
        if (useAttributes) {
            item.setAttribute("value", s);
            if (b.getValue() == null) {
                item.setAttribute("isNull", "true");
            }
        } else {
            Element v = item.getChild("value");

            // Start read: ensure value element
            if (v == null) item.addContent(v = new Element("value"));

            // set result
            v.setText(s);
            if (b.getValue() == null) {
                v.setAttribute("isNull", "true");
            }
        }
    }
    
    void immediateReadRoute(String name, Element item) {

    	String state = "0";  //default to unknown
    	RouteManager manager = InstanceManager.routeManagerInstance();
    	Route r = manager.getBySystemName(name);
    	String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
		if (turnoutsAlignedSensor != "") {  //only set if found
			Sensor routeAligned = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
			state = Integer.toString((routeAligned != null) ? routeAligned.getKnownState() : 0);
		}
        
        if (useAttributes) {
            item.setAttribute("value", state);
        } else {
            Element v = item.getChild("value");

            // Start read: ensure value element
            if (v == null) item.addContent(v = new Element("value"));

            // set result
            v.setText(state);
        }
    }
    
    void immediateReadSensor(String name, Element item) {
        // get sensor
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        if (useAttributes) {
            item.setAttribute("value", Integer.toString(b.getState()));
        } else {
        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getState());
    }
    }
    
    void immediateReadPower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        String p = (b != null) ? Integer.toString(b.getPower()) : "";
        if (useAttributes) {
            item.setAttribute("value", p);
            if (b == null) {
                item.setAttribute("isNull", Boolean.toString(true));
            }
        } else {
            Element v = item.getChild("value");

            // Start read: ensure value element
            if (v == null) item.addContent(v = new Element("value"));

            // set result
            v.setText(p);
            if (b == null) item.setAttribute("isNull", Boolean.toString(true));
        }
    }
    
    void immediateReadMetadata(String name, Element item) throws JmriException {

        if (useAttributes) {
            item.setAttribute("value", Metadata.getBySystemName(name));
            if (name.equals("JMRIVERSION")) {
                item.setAttribute("major", Metadata.getBySystemName("JMRIVERMAJOR"));
                item.setAttribute("minor", Metadata.getBySystemName("JMRIVERMINOR"));
                item.setAttribute("test", Metadata.getBySystemName("JMRIVERTEST"));
            }
        } else {
            Element v = item.getChild("value");

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element("value"));
            }

            // set result
            v.setText("" + Metadata.getBySystemName(name));
            if (name.equals("JMRIVERSION")) {
                item.addContent(v = new Element("major"));
                v.setText("" + Metadata.getBySystemName("JMRIVERMAJOR"));
                item.addContent(v = new Element("minor"));
                v.setText("" + Metadata.getBySystemName("JMRIVERMINOR"));
                item.addContent(v = new Element("test"));
                v.setText("" + Metadata.getBySystemName("JMRIVERTEST"));
            }
        }
    }
    
    static HashMap<Integer, ThrottleContext> map = new HashMap<Integer, ThrottleContext>();
    
    void immediateSetThrottle(Element item) {
        Integer address;
        useAttributes = (item.getAttribute("address") != null);
        if (useAttributes) {
            address = Integer.parseInt(item.getAttributeValue("address"));
        } else {
            address = Integer.parseInt(item.getChild("address").getText());
        }
        ThrottleContext tc = map.get(address);
        if (tc == null) {
            // first request does the allocation
            InstanceManager.throttleManagerInstance().requestThrottle(address, new ThrottleListener() {

                @Override
                public void notifyThrottleFound(DccThrottle t) {
                    log.debug("callback for throttle");
                    // store back into context
                    ThrottleContext tc = new ThrottleContext();
                    tc.throttle = t;
                    Integer address = Integer.valueOf(((DccLocoAddress) t.getLocoAddress()).getNumber());
                    map.put(address, tc);
                }

                @Override
                public void notifyFailedThrottleRequest(jmri.DccLocoAddress address, String reason) {
                }
            });
        } else {
            log.debug("process active throttle");
            // set speed, etc, as needed
            DccThrottle t = tc.throttle;

            if (useAttributes) {
            	Attribute a;
            	try {
            		if ((a = item.getAttribute("speed")) != null) {
            			t.setSpeedSetting(a.getFloatValue());
            		} else {
            			item.setAttribute("speed", Float.toString(t.getSpeedSetting()));
            		}

            		if ((a = item.getAttribute("forward")) != null) {
            			t.setIsForward(a.getBooleanValue());
            		} else {
            			item.setAttribute("forward", Boolean.valueOf(t.getIsForward()).toString());
            		}

            		if ((a = item.getAttribute("F0")) != null) {
            			t.setF0(a.getBooleanValue());
            		} else {
            			item.setAttribute("F0", Boolean.valueOf(t.getF0()).toString());
            		}

            		if ((a = item.getAttribute("F1")) != null) {
            			t.setF1(a.getBooleanValue());
            		} else {
            			item.setAttribute("F1", Boolean.valueOf(t.getF1()).toString());
            		}

            		if ((a = item.getAttribute("F2")) != null) {
            			t.setF2(a.getBooleanValue());
            		} else {
            			item.setAttribute("F2", Boolean.valueOf(t.getF2()).toString());
            		}

            		if ((a = item.getAttribute("F3")) != null) {
            			t.setF3(a.getBooleanValue());
            		} else {
            			item.setAttribute("F3", Boolean.valueOf(t.getF3()).toString());
            		}

            		if ((a = item.getAttribute("F4")) != null) {
            			t.setF4(a.getBooleanValue());
            		} else {
            			item.setAttribute("F4", Boolean.valueOf(t.getF4()).toString());
            		}

            		if ((a = item.getAttribute("F5")) != null) {
            			t.setF5(a.getBooleanValue());
            		} else {
            			item.setAttribute("F5", Boolean.valueOf(t.getF5()).toString());
            		}

            		if ((a = item.getAttribute("F6")) != null) {
            			t.setF6(a.getBooleanValue());
            		} else {
            			item.setAttribute("F6", Boolean.valueOf(t.getF6()).toString());
            		}

            		if ((a = item.getAttribute("F7")) != null) {
            			t.setF7(a.getBooleanValue());
            		} else {
            			item.setAttribute("F7", Boolean.valueOf(t.getF7()).toString());
            		}

            		if ((a = item.getAttribute("F8")) != null) {
            			t.setF8(a.getBooleanValue());
            		} else {
            			item.setAttribute("F8", Boolean.valueOf(t.getF8()).toString());
            		}

            		if ((a = item.getAttribute("F9")) != null) {
            			t.setF9(a.getBooleanValue());
            		} else {
            			item.setAttribute("F9", Boolean.valueOf(t.getF9()).toString());
            		}

            		if ((a = item.getAttribute("F10")) != null) {
            			t.setF10(a.getBooleanValue());
            		} else {
            			item.setAttribute("F10", Boolean.valueOf(t.getF10()).toString());
            		}

            		if ((a = item.getAttribute("F11")) != null) {
            			t.setF11(a.getBooleanValue());
            		} else {
            			item.setAttribute("F11", Boolean.valueOf(t.getF11()).toString());
            		}

            		if ((a = item.getAttribute("F12")) != null) {
            			t.setF12(a.getBooleanValue());
            		} else {
            			item.setAttribute("F12", Boolean.valueOf(t.getF12()).toString());
            		}
            		if ((a = item.getAttribute("F13")) != null) {
            			t.setF13(a.getBooleanValue());
            		} else {
            			item.setAttribute("F13", Boolean.valueOf(t.getF13()).toString());
            		}
            		if ((a = item.getAttribute("F14")) != null) {
            			t.setF14(a.getBooleanValue());
            		} else {
            			item.setAttribute("F14", Boolean.valueOf(t.getF14()).toString());
            		}
            		if ((a = item.getAttribute("F15")) != null) {
            			t.setF15(a.getBooleanValue());
            		} else {
            			item.setAttribute("F15", Boolean.valueOf(t.getF15()).toString());
            		}
            		if ((a = item.getAttribute("F16")) != null) {
            			t.setF16(a.getBooleanValue());
            		} else {
            			item.setAttribute("F16", Boolean.valueOf(t.getF16()).toString());
            		}
            		if ((a = item.getAttribute("F17")) != null) {
            			t.setF17(a.getBooleanValue());
            		} else {
            			item.setAttribute("F17", Boolean.valueOf(t.getF17()).toString());
            		}
            		if ((a = item.getAttribute("F18")) != null) {
            			t.setF18(a.getBooleanValue());
            		} else {
            			item.setAttribute("F18", Boolean.valueOf(t.getF18()).toString());
            		}
            		if ((a = item.getAttribute("F19")) != null) {
            			t.setF19(a.getBooleanValue());
            		} else {
            			item.setAttribute("F19", Boolean.valueOf(t.getF19()).toString());
            		}
            		if ((a = item.getAttribute("F20")) != null) {
            			t.setF20(a.getBooleanValue());
            		} else {
            			item.setAttribute("F20", Boolean.valueOf(t.getF20()).toString());
            		}
            		if ((a = item.getAttribute("F21")) != null) {
            			t.setF21(a.getBooleanValue());
            		} else {
            			item.setAttribute("F21", Boolean.valueOf(t.getF21()).toString());
            		}
            		if ((a = item.getAttribute("F22")) != null) {
            			t.setF22(a.getBooleanValue());
            		} else {
            			item.setAttribute("F22", Boolean.valueOf(t.getF22()).toString());
            		}
            		if ((a = item.getAttribute("F23")) != null) {
            			t.setF23(a.getBooleanValue());
            		} else {
            			item.setAttribute("F23", Boolean.valueOf(t.getF23()).toString());
            		}
            		if ((a = item.getAttribute("F24")) != null) {
            			t.setF24(a.getBooleanValue());
            		} else {
            			item.setAttribute("F24", Boolean.valueOf(t.getF24()).toString());
            		}
            		if ((a = item.getAttribute("F25")) != null) {
            			t.setF25(a.getBooleanValue());
            		} else {
            			item.setAttribute("F25", Boolean.valueOf(t.getF25()).toString());
            		}
            		if ((a = item.getAttribute("F26")) != null) {
            			t.setF26(a.getBooleanValue());
            		} else {
            			item.setAttribute("F26", Boolean.valueOf(t.getF26()).toString());
            		}
            		if ((a = item.getAttribute("F27")) != null) {
            			t.setF27(a.getBooleanValue());
            		} else {
            			item.setAttribute("F27", Boolean.valueOf(t.getF27()).toString());
            		}
            		if ((a = item.getAttribute("F28")) != null) {
            			t.setF28(a.getBooleanValue());
            		} else {
            			item.setAttribute("F28", Boolean.valueOf(t.getF28()).toString());
            		}

            	} catch (DataConversionException e) {

            	}
            } else {
            	Element e;

            	if ( (e = item.getChild("speed")) != null) {
            		t.setSpeedSetting(Float.parseFloat(e.getText()));
            	}
            	else item.addContent(new Element("speed")
            	.addContent(
            			""+t.getSpeedSetting()
            	));

            	if ( (e = item.getChild("forward")) != null) {
            		if (e.getText().equals("false")) t.setIsForward(false);
            		else t.setIsForward(true);
            	}
            	else item.addContent(new Element("forward")
            	.addContent(
            			t.getIsForward() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F0")) != null)  
            		if (e.getText().equals("false")) t.setF0(false);
            		else t.setF0(true);
            	else item.addContent(new Element("F0")
            	.addContent(
            			t.getF0() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F1")) != null)  
            		if (e.getText().equals("false")) t.setF1(false);
            		else t.setF1(true);
            	else item.addContent(new Element("F1")
            	.addContent(
            			t.getF1() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F2")) != null)  
            		if (e.getText().equals("false")) t.setF2(false);
            		else t.setF2(true);
            	else item.addContent(new Element("F2")
            	.addContent(
            			t.getF2() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F3")) != null)  
            		if (e.getText().equals("false")) t.setF3(false);
            		else t.setF3(true);
            	else item.addContent(new Element("F3")
            	.addContent(
            			t.getF3() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F4")) != null)  
            		if (e.getText().equals("false")) t.setF4(false);
            		else t.setF4(true);
            	else item.addContent(new Element("F4")
            	.addContent(
            			t.getF4() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F5")) != null)  
            		if (e.getText().equals("false")) t.setF5(false);
            		else t.setF5(true);
            	else item.addContent(new Element("F5")
            	.addContent(
            			t.getF5() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F6")) != null)  
            		if (e.getText().equals("false")) t.setF6(false);
            		else t.setF6(true);
            	else item.addContent(new Element("F6")
            	.addContent(
            			t.getF6() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F7")) != null)  
            		if (e.getText().equals("false")) t.setF7(false);
            		else t.setF7(true);
            	else item.addContent(new Element("F7")
            	.addContent(
            			t.getF7() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F8")) != null)  
            		if (e.getText().equals("false")) t.setF8(false);
            		else t.setF8(true);
            	else item.addContent(new Element("F8")
            	.addContent(
            			t.getF8() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F9")) != null)  
            		if (e.getText().equals("false")) t.setF9(false);
            		else t.setF9(true);
            	else item.addContent(new Element("F9")
            	.addContent(
            			t.getF9() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F10")) != null)  
            		if (e.getText().equals("false")) t.setF10(false);
            		else t.setF10(true);
            	else item.addContent(new Element("F10")
            	.addContent(
            			t.getF10() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F11")) != null)  
            		if (e.getText().equals("false")) t.setF11(false);
            		else t.setF11(true);
            	else item.addContent(new Element("F11")
            	.addContent(
            			t.getF11() ? "true" : "false"
            	));

            	if ( (e = item.getChild("F12")) != null)  
            		if (e.getText().equals("false")) t.setF12(false);
            		else t.setF12(true);
            	else item.addContent(new Element("F12")
            	.addContent(
            			t.getF12() ? "true" : "false"
            	));
            	if ( (e = item.getChild("F13")) != null)  
            		if (e.getText().equals("false")) t.setF13(false);
            		else t.setF13(true);
            	else item.addContent(new Element("F13").addContent(t.getF13() ? "true" : "false"));
            	if ( (e = item.getChild("F14")) != null)  
            		if (e.getText().equals("false")) t.setF14(false);
            		else t.setF14(true);
            	else item.addContent(new Element("F14").addContent(t.getF14() ? "true" : "false"));
            	if ( (e = item.getChild("F15")) != null)  
            		if (e.getText().equals("false")) t.setF15(false);
            		else t.setF15(true);
            	else item.addContent(new Element("F15").addContent(t.getF15() ? "true" : "false"));
            	if ( (e = item.getChild("F16")) != null)  
            		if (e.getText().equals("false")) t.setF16(false);
            		else t.setF16(true);
            	else item.addContent(new Element("F16").addContent(t.getF16() ? "true" : "false"));
            	if ( (e = item.getChild("F17")) != null)  
            		if (e.getText().equals("false")) t.setF17(false);
            		else t.setF17(true);
            	else item.addContent(new Element("F17").addContent(t.getF17() ? "true" : "false"));
            	if ( (e = item.getChild("F18")) != null)  
            		if (e.getText().equals("false")) t.setF18(false);
            		else t.setF18(true);
            	else item.addContent(new Element("F18").addContent(t.getF18() ? "true" : "false"));
            	if ( (e = item.getChild("F19")) != null)  
            		if (e.getText().equals("false")) t.setF19(false);
            		else t.setF19(true);
            	else item.addContent(new Element("F19").addContent(t.getF19() ? "true" : "false"));
            	if ( (e = item.getChild("F20")) != null)  
            		if (e.getText().equals("false")) t.setF20(false);
            		else t.setF20(true);
            	else item.addContent(new Element("F20").addContent(t.getF20() ? "true" : "false"));
            	if ( (e = item.getChild("F21")) != null)  
            		if (e.getText().equals("false")) t.setF21(false);
            		else t.setF21(true);
            	else item.addContent(new Element("F21").addContent(t.getF21() ? "true" : "false"));
            	if ( (e = item.getChild("F22")) != null)  
            		if (e.getText().equals("false")) t.setF22(false);
            		else t.setF22(true);
            	else item.addContent(new Element("F22").addContent(t.getF22() ? "true" : "false"));
            	if ( (e = item.getChild("F23")) != null)  
            		if (e.getText().equals("false")) t.setF23(false);
            		else t.setF23(true);
            	else item.addContent(new Element("F23").addContent(t.getF23() ? "true" : "false"));
            	if ( (e = item.getChild("F24")) != null)  
            		if (e.getText().equals("false")) t.setF24(false);
            		else t.setF24(true);
            	else item.addContent(new Element("F24").addContent(t.getF24() ? "true" : "false"));
            	if ( (e = item.getChild("F25")) != null)  
            		if (e.getText().equals("false")) t.setF25(false);
            		else t.setF25(true);
            	else item.addContent(new Element("F25").addContent(t.getF25() ? "true" : "false"));
            	if ( (e = item.getChild("F26")) != null)  
            		if (e.getText().equals("false")) t.setF26(false);
            		else t.setF26(true);
            	else item.addContent(new Element("F26").addContent(t.getF26() ? "true" : "false"));
            	if ( (e = item.getChild("F27")) != null)  
            		if (e.getText().equals("false")) t.setF27(false);
            		else t.setF27(true);
            	else item.addContent(new Element("F27").addContent(t.getF27() ? "true" : "false"));
            	if ( (e = item.getChild("F28")) != null)  
            		if (e.getText().equals("false")) t.setF28(false);
            		else t.setF28(true);
            	else item.addContent(new Element("F28").addContent(t.getF28() ? "true" : "false"));

            }
            // The speedStepMode is sent every time since a XMLIO client may
            // reuse an existing throttle context and not be aware of the mode
            // if the mode is only sent when the throttle context is created.
            // This will only be sent as an attribute of a throttle element
            // to prevent clients that don't know what to do with unknown
            // elements from crashing.
            item.setAttribute("SSM", Integer.toString(t.getSpeedStepMode()));
        }
    }

    // class to persist (from one usage to another)
    // throttle information
    static class ThrottleContext {
        DccThrottle throttle;
    }
    
    // class for firing off requests to handle a deferred read
    class DeferredRead implements java.beans.PropertyChangeListener {
        Element request;
        XmlIORequestor requestor;
        
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            boolean changed = checkValues(request);
            
            if (!changed) return;
            
            // found change, pull listeners and return
            @SuppressWarnings("unchecked")
            List<Element> items = request.getChildren("item");
    
            for (Element item : items) {
                String type = item.getAttributeValue("type");
                String name = item.getAttributeValue("name");
                if (type == null) {
                    type = item.getChild("type").getText();
                    name = item.getChild("name").getText();
                }
                
                if (type.equals("turnout")) removeListenerFromTurnout(name, item, this);
                else if (type.equals("memory")) removeListenerFromMemory(name, item, this);
                else if (type.equals("sensor")) removeListenerFromSensor(name, item, this);
                else if (type.equals("route")) removeListenerFromRoute(name, item, this);
                else if (type.equals("power")) removeListenerFromPower(name, item, this);
                else log.warn("Unexpected type: "+type);
            }
            
            sendMonitorReply(request, requestor);
            
        }
    }
    
    boolean useAttributes = false;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultXmlIOServer.class.getName());
}

/* @(#)DefaultXmlIOServer.java */

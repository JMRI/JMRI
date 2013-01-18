// DefaultXmlIOServer.java
package jmri.web.xmlio;

import java.beans.PropertyChangeListener;
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
import jmri.util.StringUtil;
import jmri.web.server.WebServerManager;
import static jmri.web.xmlio.XmlIO.*;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Default implementation for XML I/O.
 *
 * <hr> This file is part of JMRI. <P> JMRI is free software; you can
 * redistribute it and/or modify it under the terms of version 2 of the GNU
 * General Public License as published by the Free Software Foundation. See the
 * "COPYING" file for a copy of this license. <P> JMRI is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2009, 2010
 * @version	$Revision$
 * @see jmri.web.xmlio.XmlIOFactory
 */
public class DefaultXmlIOServer implements XmlIOServer {

    static List<String> disallowedFrames = WebServerManager.getWebServerPreferences().getDisallowedFrames();
    static HashMap<Integer, ThrottleContext> map = new HashMap<Integer, ThrottleContext>();
    boolean useAttributes = false;
    static Logger log = Logger.getLogger(DefaultXmlIOServer.class);
    
    @Override
    public Element immediateRequest(Element e) throws JmriException {

        // process panels and frames as the same elements through 2.14.
        // after 2.14, process panels as XML definitions of panels so that
        // iPads or Android tablet apps could directly render the panels.

        // first, process any list elements
        // roster, frame, panel, metadata and railroad are immediate only
        // power, turnout, sensor, signalhead, signalmast, memory and route 
        // can be monitored for changes, pass current values to begin
        // throttle accepts changes
        @SuppressWarnings("unchecked")
        List<Element> lists = new ArrayList<Element>(e.getChildren(LIST));
        for (Element list : lists) {
            e.removeContent(list);
            // if there is no attribute named TYPE, type is null
            String type = list.getAttributeValue(TYPE);
            // if type is not null, use the attribute-based syntax
            useAttributes = (type != null);
            if (!useAttributes) {
                // if type is null, set type from a child element named TYPE
                type = list.getChild(TYPE).getText();
            }
            if (type == null) {
                log.error("type not found!");
                continue;
            }
            if (type.equals(TURNOUT)) {
                // add an element for each turnout
                TurnoutManager m = InstanceManager.turnoutManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Turnout t = m.getTurnout(name);
                    Element n = new Element((useAttributes) ? TURNOUT : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, name);
                        if (t.getUserName() != null) {
                            n.setAttribute(USERNAME, t.getUserName());
                        }
                        if (t.getComment() != null) {
                            n.setAttribute(COMMENT, t.getComment());
                        }
                        n.setAttribute(INVERTED, Boolean.valueOf(t.getInverted()).toString());
                    } else {
                        n.addContent(new Element(TYPE).addContent(TURNOUT));
                        n.addContent(new Element(NAME).addContent(name));
                        n.addContent(new Element(USERNAME).addContent(t.getUserName()));
                        n.addContent(new Element(COMMENT).addContent(t.getComment()));
                        n.addContent(new Element(INVERTED).addContent(Boolean.valueOf(t.getInverted()).toString()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(MEMORY)) {
                // add an element for each memory
                MemoryManager m = InstanceManager.memoryManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Memory t = m.getMemory(name);
                    Element n = new Element((useAttributes) ? MEMORY : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, name);
                        if (t.getUserName() != null) {
                            n.setAttribute(USERNAME, t.getUserName());
                        }
                        if (t.getComment() != null) {
                            n.setAttribute(COMMENT, t.getComment());
                        }
                    } else {
                        n.addContent(new Element(TYPE).addContent(MEMORY));
                        n.addContent(new Element(NAME).addContent(name));
                        n.addContent(new Element(USERNAME).addContent(t.getUserName()));
                        n.addContent(new Element(COMMENT).addContent(t.getComment()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(ROUTE)) {
                // add an element for each route
                RouteManager m = InstanceManager.routeManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Route t = m.getRoute(name);
                    Element n = new Element((useAttributes) ? ROUTE : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, name);
                        if (t.getUserName() != null) {
                            n.setAttribute(USERNAME, t.getUserName());
                        }
                        if (t.getComment() != null) {
                            n.setAttribute(COMMENT, t.getComment());
                        }
                    } else {
                        n.addContent(new Element(TYPE).addContent(ROUTE));
                        n.addContent(new Element(NAME).addContent(name));
                        n.addContent(new Element(USERNAME).addContent(t.getUserName()));
                        n.addContent(new Element(COMMENT).addContent(t.getComment()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(SENSOR)) {
                // add an element for each sensor
                SensorManager m = InstanceManager.sensorManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Sensor t = m.getSensor(name);
                    Element n = new Element((useAttributes) ? SENSOR : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, name);
                        if (t.getUserName() != null) {
                            n.setAttribute(USERNAME, t.getUserName());
                        }
                        if (t.getComment() != null) {
                            n.setAttribute(COMMENT, t.getComment());
                        }
                        n.setAttribute(INVERTED, Boolean.valueOf(t.getInverted()).toString());
                    } else {
                        n.addContent(new Element(TYPE).addContent(SENSOR));
                        n.addContent(new Element(NAME).addContent(name));
                        n.addContent(new Element(USERNAME).addContent(t.getUserName()));
                        n.addContent(new Element(COMMENT).addContent(t.getComment()));
                        n.addContent(new Element(INVERTED).addContent(Boolean.valueOf(t.getInverted()).toString()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(SIGNAL_HEAD)) {
                // add an element for each signalhead
                SignalHeadManager m = InstanceManager.signalHeadManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    SignalHead t = m.getSignalHead(name);
                    Element n = new Element((useAttributes) ? SIGNAL_HEAD : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, name);
                        if (t.getUserName() != null) {
                            n.setAttribute(USERNAME, t.getUserName());
                        }
                        if (t.getComment() != null) {
                            n.setAttribute(COMMENT, t.getComment());
                        }
                    } else {
                        n.addContent(new Element(TYPE).addContent(SIGNAL_HEAD));
                        n.addContent(new Element(NAME).addContent(name));
                        n.addContent(new Element(USERNAME).addContent(t.getUserName()));
                        n.addContent(new Element(COMMENT).addContent(t.getComment()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(SIGNAL_MAST)) {
                // add an element for each signalmast
                SignalMastManager m = InstanceManager.signalMastManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    SignalMast t = m.getSignalMast(name);
                    Element n = new Element((useAttributes) ? SIGNAL_MAST : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, name);
                        if (t.getUserName() != null) {
                            n.setAttribute(USERNAME, t.getUserName());
                        }
                        if (t.getComment() != null) {
                            n.setAttribute(COMMENT, t.getComment());
                        }
                    } else {
                        n.addContent(new Element(TYPE).addContent(SIGNAL_MAST));
                        n.addContent(new Element(NAME).addContent(name));
                        n.addContent(new Element(USERNAME).addContent(t.getUserName()));
                        n.addContent(new Element(COMMENT).addContent(t.getComment()));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(ROSTER)) {
                // add an element for each roster entry
                List<RosterEntry> rlist = Roster.instance().matchingList(null, null, null, null, null, null, null);
                for (int i = 0; i < rlist.size(); i++) {
                    RosterEntry entry = rlist.get(i);
                    Element n = new Element((useAttributes) ? ROSTER : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, entry.getId());
                        n.setAttribute(DCC_ADDRESS, entry.getDccAddress());
                        n.setAttribute(ADDRESS_LENGTH, entry.isLongAddress() ? L : S);
                        n.setAttribute(ROAD_NAME, entry.getRoadName());
                        n.setAttribute(ROAD_NUMBER, entry.getRoadNumber());
                        n.setAttribute(MFG, entry.getMfg());
                        n.setAttribute(MODEL, entry.getModel());
                        n.setAttribute(COMMENT, entry.getComment());
                        n.setAttribute(MAX_SPEED_PCT, Integer.valueOf(entry.getMaxSpeedPCT()).toString());
                        File file = new File(entry.getImagePath());
                        n.setAttribute(IMAGE_FILE_NAME, file.getName());
                        file = new File(entry.getIconPath());
                        n.setAttribute(IMAGE_ICON_NAME, file.getName());
                        Element f;
                        for (int j = 0; j < entry.getMAXFNNUM(); j++) {
                            if (entry.getFunctionLabel(j) != null) {
                                f = new Element(FUNCTION);
                                f.setAttribute(NAME, F + j);
                                f.setAttribute(LABEL, entry.getFunctionLabel(j));
                                f.setAttribute(LOCKABLE, Boolean.valueOf(entry.getFunctionLockable(j)).toString());
                                n.addContent(f);
                            }
                        }
                    } else {
                        n.addContent(new Element(TYPE).addContent(ROSTER));
                        n.addContent(new Element(NAME).addContent(entry.getId()));
                        n.addContent(new Element(DCC_ADDRESS).addContent(entry.getDccAddress()));
                        n.addContent(new Element(ADDRESS_LENGTH).addContent(entry.isLongAddress() ? L : S));
                        n.addContent(new Element(ROAD_NAME).addContent(entry.getRoadName()));
                        n.addContent(new Element(ROAD_NUMBER).addContent(entry.getRoadNumber()));
                        n.addContent(new Element(MFG).addContent(entry.getMfg()));
                        n.addContent(new Element(MODEL).addContent(entry.getModel()));
                        n.addContent(new Element(COMMENT).addContent(entry.getComment()));
                        n.addContent(new Element(MAX_SPEED_PCT).addContent(Integer.valueOf(entry.getMaxSpeedPCT()).toString()));
                        File file = new File(entry.getImagePath());
                        n.addContent(new Element(IMAGE_FILE_NAME).addContent(file.getName()));
                        file = new File(entry.getIconPath());
                        n.addContent(new Element(IMAGE_ICON_NAME).addContent(file.getName()));
                        Element f = new Element(FUNCTION_LABEL);
                        Element g = new Element(FUNCTION_LOCKABLE);
                        for (int j = 0; j < entry.getMAXFNNUM(); j++) {
                            if (entry.getFunctionLabel(j) != null) {
                                f.addContent(new Element(F + j).addContent(entry.getFunctionLabel(j)));
                                g.addContent(new Element(F + j).addContent(Boolean.valueOf(entry.getFunctionLockable(j)).toString()));
                            }
                        }
                        n.addContent(f);
                        n.addContent(g);
                    }
                    e.addContent(n);
                }

            } else if (type.equals(FRAME)) {

                // list frames, (open JMRI windows)
                List<JmriJFrame> frames = JmriJFrame.getFrameList();
                for (JmriJFrame frame : frames) { //add all non-blank titles to list
                    if (frame.getAllowInFrameServlet()) {
                        String frameTitle = frame.getTitle();
                        if (!frameTitle.equals("") && !disallowedFrames.contains(frameTitle)) {
                            String escapedTitle = StringUtil.escapeString(frameTitle);
                            Element n = new Element((useAttributes) ? FRAME : ITEM);
                            if (useAttributes) {
                                n.setAttribute(NAME, escapedTitle);
                                n.setAttribute(USERNAME, frameTitle);
                            } else {
                                n.addContent(new Element(TYPE).addContent(FRAME));
                                n.addContent(new Element(NAME).addContent(escapedTitle));
                                n.addContent(new Element(USERNAME).addContent(frameTitle));
                            }
                            e.addContent(n);
                        }
                    }
                }

            } else if (type.equals(PANEL_ELEMENT)) {

                // list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor)
                List<JmriJFrame> frames = JmriJFrame.getFrameList(ControlPanelEditor.class);
                for (JmriJFrame frame : frames) {
                    if (frame.getAllowInFrameServlet()) {
                        String title = ((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                        if (!title.equals("") && !disallowedFrames.contains(title)) {
                            String escapedTitle = StringUtil.escapeString(title);
                            Element n = new Element((useAttributes) ? PANEL_ELEMENT : ITEM);
                            if (useAttributes) {
                                n.setAttribute(NAME, CONTROLPANEL + PATH_SEP + escapedTitle);
                                n.setAttribute(USERNAME, title);
                                n.setAttribute(TYPE, CONTROLPANEL);
                            } else {
                                n.addContent(new Element(TYPE).addContent(PANEL_ELEMENT));
                                n.addContent(new Element(NAME).addContent(CONTROLPANEL + PATH_SEP + escapedTitle));
                                n.addContent(new Element(USERNAME).addContent(title));
                            }
                            e.addContent(n);
                        }
                    }
                }
                frames = JmriJFrame.getFrameList(PanelEditor.class);
                for (JmriJFrame frame : frames) {
                    if (frame.getAllowInFrameServlet() && !(LayoutEditor.class.isInstance(frame))) {  //skip LayoutEditor panels, as they will be added next
                        String title = ((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                        if (!title.equals("") && !disallowedFrames.contains(title)) {
                            String escapedTitle = StringUtil.escapeString(title);
                            Element n = new Element((useAttributes) ? PANEL_ELEMENT : ITEM);
                            if (useAttributes) {
                                n.setAttribute(NAME, PANEL + PATH_SEP + escapedTitle);
                                n.setAttribute(USERNAME, title);
                                n.setAttribute(TYPE, PANEL);
                            } else {
                                n.addContent(new Element(TYPE).addContent(PANEL_ELEMENT));
                                n.addContent(new Element(NAME).addContent(PANEL + PATH_SEP + escapedTitle));
                                n.addContent(new Element(USERNAME).addContent(title));
                            }
                            e.addContent(n);
                        }
                    }
                }
                frames = JmriJFrame.getFrameList(LayoutEditor.class);
                for (JmriJFrame frame : frames) {
                    if (frame.getAllowInFrameServlet()) {
                        String title = ((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                        if (!title.equals("") && !disallowedFrames.contains(title)) {
                            String escapedTitle = StringUtil.escapeString(title);
                            Element n = new Element((useAttributes) ? PANEL_ELEMENT : ITEM);
                            if (useAttributes) {
                                n.setAttribute(NAME, LAYOUT + PATH_SEP + escapedTitle);
                                n.setAttribute(USERNAME, title);
                                n.setAttribute(TYPE, LAYOUT);
                            } else {
                                n.addContent(new Element(TYPE).addContent(PANEL_ELEMENT));
                                n.addContent(new Element(NAME).addContent(LAYOUT + PATH_SEP + escapedTitle));
                                n.addContent(new Element(USERNAME).addContent(title));
                            }
                            e.addContent(n);
                        }
                    }
                }

            } else if (type.equals(POWER)) {
                // add a power element
                Element n = new Element((useAttributes) ? POWER : ITEM);
                if (useAttributes) {
                    n.setAttribute(NAME, POWER);
                } else {
                    n.addContent(new Element(TYPE).addContent(POWER));
                    n.addContent(new Element(NAME).addContent(POWER));
                }
                e.addContent(n);
            } else if (type.equals(METADATA)) {
                // list meta data elements
                List<String> metaNames = Metadata.getSystemNameList();
                for (String mn : metaNames) {
                    Element n = new Element((useAttributes) ? METADATA : ITEM);
                    if (useAttributes) {
                        n.setAttribute(NAME, mn);
                    } else {
                        n.addContent(new Element(TYPE).addContent(METADATA));
                        n.addContent(new Element(NAME).addContent("" + mn));
                    }
                    e.addContent(n);
                }
            } else if (type.equals(RAILROAD)) {
                // return the Web Server's Railroad name preference
                Element n = new Element(RAILROAD);
                n.setAttribute(NAME, WebServerManager.getWebServerPreferences().getRailRoadName());
                e.addContent(n);
            } else {
                log.warn("Unexpected type in list element: " + type);
            }
        }

        // handle everything else
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();
        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue(NAME);
            useAttributes = (!type.equals(ITEM));
            if (!useAttributes) {
                type = item.getChild(TYPE).getText();
                name = item.getChild(NAME).getText();
            } else if (name == null) {
                name = "";
            }

            //check for SET values and process them
            if (type.equals(THROTTLE)) {
                immediateSetThrottle(item);
            } else if (type.equals(TURNOUT)) {
                immediateWriteTurnout(name, item);
                immediateReadTurnout(name, item);
            } else if (type.equals(MEMORY)) {
                immediateWriteMemory(name, item);
                immediateReadMemory(name, item);
            } else if (type.equals(ROUTE)) {
                immediateWriteRoute(name, item);
                immediateReadRoute(name, item);
            } else if (type.equals(SENSOR)) {
                immediateWriteSensor(name, item);
                immediateReadSensor(name, item);
            } else if (type.equals(SIGNAL_HEAD)) {
                immediateWriteSignalHead(name, item);
                immediateReadSignalHead(name, item);
            } else if (type.equals(SIGNAL_MAST)) {
                immediateWriteSignalMast(name, item);
                immediateReadSignalMast(name, item);
            } else if (type.equals(POWER)) {
                immediateWritePower(name, item);
                immediateReadPower(name, item);
            } else if (type.equals(METADATA)) {
                immediateReadMetadata(name, item);
            } else if (type.equals(ROSTER)) {
                // nothing to process
            } else if (type.equals(FRAME)) {
                // nothing to process
            } else if (type.equals(PANEL_ELEMENT)) {
                // nothing to process
            } else if (type.equals(RAILROAD)) {
                // nothing to process
            } else if (item.getName().equals(ITEM)) {
                log.warn("Unexpected type in item: " + type);
            } else {
                log.warn("Unexpected element: " + type);
            }
        }

        return e;
    }

    @Override
    public void monitorRequest(Element e, XmlIORequestor r, String client, Thread thread) throws JmriException {

        // check for differences now
        if (checkValues(e)) {

            // differences found, now process the read and return  
            sendMonitorReply(e, r, client, thread);
            return;
        }
        // No differences, have to wait.
        DeferredRead dr = new DeferredRead();
        dr.request = e;
        dr.requestor = r;
        dr.client = client;
        dr.thread = thread;

        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();

        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue(NAME);
            useAttributes = (!type.equals(ITEM));
            if (!useAttributes) {
                type = item.getChild(TYPE).getText();
                name = item.getChild(NAME).getText();
            } else if (name == null) {
                name = "";
            }

            if (type.equals(TURNOUT)) {
                addListenerToTurnout(name, item, dr);
            } else if (type.equals(MEMORY)) {
                addListenerToMemory(name, item, dr);
            } else if (type.equals(ROUTE)) {
                addListenerToRoute(name, item, dr);
            } else if (type.equals(SENSOR)) {
                addListenerToSensor(name, item, dr);
            } else if (type.equals(SIGNAL_HEAD)) {
                addListenerToSignalHead(name, item, dr);
            } else if (type.equals(SIGNAL_MAST)) {
                addListenerToSignalMast(name, item, dr);
            } else if (type.equals(POWER)) {
                addListenerToPower(name, item, dr);
            } else {
                log.warn("Unexpected type: " + type);
            }
        }

        // Check one more time to ensure clear of race conditions
        dr.propertyChange(null);

    }

    void sendMonitorReply(Element e, XmlIORequestor r, String client, Thread thread) {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();

        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue(NAME);
            useAttributes = (!type.equals(ITEM));
            if (!useAttributes) {
                type = item.getChild(TYPE).getText();
                name = item.getChild(NAME).getText();
            } else if (name == null) {
                name = "";
            }

            try {
                if (type.equals(TURNOUT)) {
                    immediateReadTurnout(name, item);
                } else if (type.equals(MEMORY)) {
                    immediateReadMemory(name, item);
                } else if (type.equals(SENSOR)) {
                    immediateReadSensor(name, item);
                } else if (type.equals(SIGNAL_HEAD)) {
                    immediateReadSignalHead(name, item);
                } else if (type.equals(SIGNAL_MAST)) {
                    immediateReadSignalMast(name, item);
                } else if (type.equals(ROUTE)) {
                    immediateReadRoute(name, item);
                } else if (type.equals(POWER)) {
                    immediateReadPower(name, item);
                } else if (type.equals(METADATA)) {
                    immediateReadMetadata(name, item);
                }
            } catch (JmriException j) {
                log.warn("exception handling " + type + " " + name, j);
            }
        }

        r.monitorReply(e, thread);
    }

    boolean checkValues(Element e) {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren();

        boolean changed = false;
        for (Element item : items) {
            String type = item.getName();
            String name = item.getAttributeValue(NAME);
            useAttributes = (!type.equals(ITEM));
            if (!useAttributes) {
                type = item.getChild(TYPE).getText();
                name = item.getChild(NAME).getText();
            } else if (name == null) {
                name = "";
            }
            if (item.getAttribute(VALUE) == null
                    && item.getChild(VALUE) == null) {
                return true;  // if no value, consider changed
            }
            try {
                if (type.equals(TURNOUT)) {
                    changed |= monitorProcessTurnout(name, item);
                } else if (type.equals(MEMORY)) {
                    changed |= monitorProcessMemory(name, item);
                } else if (type.equals(SENSOR)) {
                    changed |= monitorProcessSensor(name, item);
                } else if (type.equals(SIGNAL_HEAD)) {
                    changed |= monitorProcessSignalHead(name, item);
                } else if (type.equals(SIGNAL_MAST)) {
                    changed |= monitorProcessSignalMast(name, item);
                } else if (type.equals(ROUTE)) {
                    changed |= monitorProcessRoute(name, item);
                } else if (type.equals(POWER)) {
                    changed |= monitorProcessPower(name, item);
                } else if (type.equals(METADATA)) {
                    changed = true;
                } else {
                    log.warn("Unexpected type: " + type);
                }
            } catch (JmriException j) {
                log.warn("exception handling " + type + " " + name, j);
            }
        }
        return changed;
    }

    void addListenerToTurnout(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To Turnout " + name + " for " + dr.client);
        }
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        b.addPropertyChangeListener(dr);
    }

    void addListenerToMemory(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To Memory " + name + " for " + dr.client);
        }
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);
        b.addPropertyChangeListener(dr);
    }

    void addListenerToRoute(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To Route " + name + " for " + dr.client);
        }
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);
        b.addPropertyChangeListener(dr);
    }

    void addListenerToSensor(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To Sensor " + name + " for " + dr.client);
        }
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);
        b.addPropertyChangeListener(dr);
    }

    void addListenerToSignalHead(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To SignalHead " + name + " for " + dr.client);
        }
        SignalHead b = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        b.addPropertyChangeListener(dr);
    }

    void addListenerToSignalMast(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To SignalMast " + name + " for " + dr.client);
        }
        SignalMast b = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        b.addPropertyChangeListener(dr);
    }

    void addListenerToPower(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("adding Listener To Power " + name + " for " + dr.client);
        }
        PowerManager b = InstanceManager.powerManagerInstance();
        b.addPropertyChangeListener(dr);
    }

    void removeListenerFromTurnout(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener From Turnout " + name + " for " + dr.client);
        }
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        b.removePropertyChangeListener(dr);
    }

    void removeListenerFromMemory(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener From Memory " + name + " for " + dr.client);
        }
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);
        b.removePropertyChangeListener(dr);
    }

    void removeListenerFromRoute(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener From Route " + name + " for " + dr.client);
        }
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);
        b.removePropertyChangeListener(dr);
    }

    void removeListenerFromSensor(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener from Sensor " + name + " for " + dr.client);
        }
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);
        b.removePropertyChangeListener(dr);
    }

    void removeListenerFromSignalHead(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener from SignalHead " + name + " for " + dr.client);
        }
        SignalHead b = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        b.removePropertyChangeListener(dr);
    }

    void removeListenerFromSignalMast(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener from SignalMast " + name + " for " + dr.client);
        }
        SignalMast b = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        b.removePropertyChangeListener(dr);
    }

    void removeListenerFromPower(String name, Element item, DeferredRead dr) {
        if (log.isDebugEnabled()) {
            log.debug("removing Listener From Power " + name + " for " + dr.client);
        }
        PowerManager b = InstanceManager.powerManagerInstance();
        b.removePropertyChangeListener(dr);
    }

    /**
     * Return true if there is a difference
     */
    boolean monitorProcessTurnout(String name, Element item) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for value element, which means compare
        if (item.getAttributeValue(VALUE) != null) {
            return (b.getKnownState() != Integer.parseInt(item.getAttributeValue(VALUE)));
        } else {
            Element v = item.getChild(VALUE);
            if (v != null) {
                int state = Integer.parseInt(v.getText());
                return (b.getKnownState() != state);
            }
        }
        return false;  // no difference
    }

    /**
     * Return true if there is a difference
     */
    boolean monitorProcessMemory(String name, Element item) {
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);

        String s = (b.getValue() != null) ? b.getValue().toString() : "";
        // check for value element, which means compare
        // return true if strings are different
        if (item.getAttributeValue(VALUE) != null) {
            return (!s.equals(item.getAttributeValue(VALUE)));
        } else if (item.getChild(VALUE) != null) {
            return (!s.equals(item.getChildText(VALUE)));
        }
        return false;  // no difference
    }

    /**
     * Return true if there is a difference in passed in route
     */
    boolean monitorProcessRoute(String name, Element item) {
        int newState = 0;  //default to unknown
        RouteManager manager = InstanceManager.routeManagerInstance();
        Route r = manager.getBySystemName(name);
        String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
        if (!"".equals(turnoutsAlignedSensor)) {  //only set if found
            Sensor routeAligned = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
            newState = (routeAligned != null) ? routeAligned.getKnownState() : 0;  //default to unknown
        }

        int state;
        // check for value element, which means compare
        if (item.getAttributeValue(VALUE) != null) {
            state = Integer.parseInt(item.getAttributeValue(VALUE));
        } else {
            state = (item.getChild(VALUE) != null) ? Integer.parseInt(item.getChildText(VALUE)) : 0; // default to unknown
        }
        return (newState != state); // return true if states are different
    }

    /**
     * Return true if there is a difference
     */
    boolean monitorProcessSensor(String name, Element item) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        // check for value element, which means compare
        if (item.getAttributeValue(VALUE) != null) {
            return (b.getState() != Integer.parseInt(item.getAttributeValue(VALUE)));
        } else {
            Element v = item.getChild(VALUE);
            if (v != null) {
                int state = Integer.parseInt(v.getText());
                return (b.getState() != state);
            }
        }
        return false;  // no difference
    }

    /**
     * Return true if there is a difference
     */
    boolean monitorProcessSignalHead(String name, Element item) {
        SignalHead b = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (b == null) {
            log.warn("SignalHead " + name + " not found, skipping.");
            return false;
        }
        int state = b.getState();
        if (b.getHeld()) {
            state = SignalHead.HELD;  //also handle held as a state   
        }

        // check for value element, which means compare
        if (item.getAttributeValue(VALUE) != null) {
            return (state != Integer.parseInt(item.getAttributeValue(VALUE)));
        } else {
            Element v = item.getChild(VALUE);
            if (v != null) {
                int newState = Integer.parseInt(v.getText());
                return (state != newState);
            }
        }
        return false;  // no difference
    }

    /**
     * Return true if there is a difference
     */
    boolean monitorProcessSignalMast(String name, Element item) {
        SignalMast b = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        if (b == null) {
            log.warn("SignalMast " + name + " not found, skipping.");
            return false;
        }
        String state = b.getAspect();
        if ((b.getHeld()) && (b.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null)) {
            state = HELD;
        } else if ((b.getLit()) && (b.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK) != null)) {
            state = DARK;
        } else if (state == null) {
            state = UNKNOWN;
        }

        // check for value element, which means compare
        if (item.getAttributeValue(VALUE) != null) {
            return (!state.equals(item.getAttributeValue(VALUE)));
        } else {
            Element v = item.getChild(VALUE);
            if (v != null) {
                String newState = v.getText();
                return (!state.equals(newState));
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
        if (item.getAttributeValue(VALUE) != null) {
            return (b.getPower() != Integer.parseInt(item.getAttributeValue(VALUE)));
        } else {
            Element v = item.getChild(VALUE);
            if (v != null) {
                int state = Integer.parseInt(v.getText());
                return (b.getPower() != state);
            }
        }
        return false;  // no difference
    }

    void immediateWriteTurnout(String name, Element item) {
        // get turnout
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for set element, which means write
        if (item.getAttributeValue(SET) != null) {
            b.setCommandedState(Integer.parseInt(item.getAttributeValue(SET)));
            item.removeAttribute(SET);
        } else {
            Element v = item.getChild(SET);
            if (v != null) {
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
        if (item.getAttributeValue(SET) != null) {
            if (item.getAttribute(IS_NULL) != null
                    && item.getAttributeValue(IS_NULL).equals(Boolean.toString(true))) {
                b.setValue(null);
            } else {
                b.setValue(item.getAttributeValue(SET));
            }
            item.removeAttribute(SET);
            item.removeAttribute(IS_NULL);
        } else {
            Element v = item.getChild(SET);
            if (v != null) {
                if (item.getAttribute(IS_NULL) != null
                        && item.getAttributeValue(IS_NULL).equals(Boolean.toString(true))) {
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
        if (item.getAttributeValue(SET) != null) {
            b.setState(Integer.parseInt(item.getAttributeValue(SET)));
            item.removeAttribute(SET);
        } else {
            Element v = item.getChild(SET);
            if (v != null) {
                int state = Integer.parseInt(v.getText());
                b.setState(state);
                item.removeContent(v);
            }
        }
    }

    void immediateWriteSignalHead(String name, Element item) throws JmriException {
        // get signalhead
        SignalHead b = InstanceManager.signalHeadManagerInstance().getSignalHead(name);

        // check for set element, which means write
        if (item.getAttributeValue(SET) != null) {
            b.setState(Integer.parseInt(item.getAttributeValue(SET)));
            item.removeAttribute(SET);
        } else {
            Element v = item.getChild(SET);
            if (v != null) {
                int state = Integer.parseInt(v.getText());
                b.setState(state);
                item.removeContent(v);
            }
        }
    }

    void immediateWriteSignalMast(String name, Element item) throws JmriException {
        // get signalMast
        SignalMast b = InstanceManager.signalMastManagerInstance().getSignalMast(name);

        // check for set element, which means write
        if (item.getAttributeValue(SET) != null) {
            try {
                b.setAspect(item.getAttributeValue(SET));
            } catch (IllegalArgumentException e) { //ignore invalid change requests
            }
            item.removeAttribute(SET);
        } else {
            Element v = item.getChild(SET);
            if (v != null) {
                String state = v.getText();
                try {
                    b.setAspect(state);
                } catch (IllegalArgumentException e) { //ignore invalid change requests
                }
                item.removeContent(v);
            }
        }
    }

    void immediateWriteRoute(String name, Element item) throws JmriException {
        // get route
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);

        if (item.getAttributeValue(SET) != null) {
            b.setRoute();
            item.removeAttribute(SET);
        } else {
            // check for set element, which means write
            Element v = item.getChild(SET);
            if (v != null) {
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
            if (item.getAttributeValue(SET) != null) {
                b.setPower(Integer.parseInt(item.getAttributeValue(SET)));
                item.removeAttribute(SET);
                //item.setAttribute(VALUE, Integer.toString(b.getPower()));
            } else {
                Element v = item.getChild(SET);
                if (v != null) {
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
            item.setAttribute(VALUE, Integer.toString(b.getKnownState()));
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText("" + b.getKnownState());
        }
    }

    void immediateReadMemory(String name, Element item) {
        // get memory
        Memory b = InstanceManager.memoryManagerInstance().provideMemory(name);

        String s = (b.getValue() != null) ? b.getValue().toString() : "";
        if (useAttributes) {
            item.setAttribute(VALUE, s);
            if (b.getValue() == null) {
                item.setAttribute(IS_NULL, TRUE);
            }
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText(s);
            if (b.getValue() == null) {
                v.setAttribute(IS_NULL, TRUE);
            }
        }
    }

    void immediateReadRoute(String name, Element item) {

        String state = "0";  //default to unknown
        RouteManager manager = InstanceManager.routeManagerInstance();
        Route r = manager.getBySystemName(name);
        String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
        if (!"".equals(turnoutsAlignedSensor)) {  //only set if found
            Sensor routeAligned = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
            state = Integer.toString((routeAligned != null) ? routeAligned.getKnownState() : 0);
        }

        if (useAttributes) {
            item.setAttribute(VALUE, state);
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText(state);
        }
    }

    void immediateReadSensor(String name, Element item) {
        // get sensor
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        if (useAttributes) {
            item.setAttribute(VALUE, Integer.toString(b.getState()));
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText("" + b.getState());
        }
    }

    void immediateReadSignalHead(String name, Element item) {
        // get signalhead
        SignalHead b = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        int state = b.getState();
        if (b.getHeld()) {
            state = SignalHead.HELD;  //also handle held as a state   
        }
        if (useAttributes) {
            item.setAttribute(VALUE, Integer.toString(state));
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText("" + state);
        }
    }

    void immediateReadSignalMast(String name, Element item) {
        // get signalMast
        SignalMast b = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        String state = b.getAspect();
        if ((b.getHeld()) && (b.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null)) {
            state = HELD;
        } else if ((b.getLit()) && (b.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK) != null)) {
            state = DARK;
        } else if (state == null) {
            state = UNKNOWN;
        }
        if (useAttributes) {
            item.setAttribute(VALUE, state);
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText(state);
        }
    }

    void immediateReadPower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        String p = (b != null) ? Integer.toString(b.getPower()) : "";
        if (useAttributes) {
            item.setAttribute(VALUE, p);
            if (b == null) {
                item.setAttribute(IS_NULL, Boolean.toString(true));
            }
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText(p);
            if (b == null) {
                item.setAttribute(IS_NULL, Boolean.toString(true));
            }
        }
    }

    void immediateReadMetadata(String name, Element item) throws JmriException {

        if (useAttributes) {
            item.setAttribute(VALUE, Metadata.getBySystemName(name));
            if (name.equals(Metadata.JMRIVERSION)) {
                item.setAttribute(Metadata.JMRIVERMAJOR, Metadata.getBySystemName(Metadata.JMRIVERMAJOR));
                item.setAttribute(Metadata.JMRIVERMINOR, Metadata.getBySystemName(Metadata.JMRIVERMINOR));
                item.setAttribute(Metadata.JMRIVERTEST, Metadata.getBySystemName(Metadata.JMRIVERTEST));
            }
        } else {
            Element v = item.getChild(VALUE);

            // Start read: ensure value element
            if (v == null) {
                item.addContent(v = new Element(VALUE));
            }

            // set result
            v.setText("" + Metadata.getBySystemName(name));
            if (name.equals(Metadata.JMRIVERSION)) {
                item.addContent(v = new Element(Metadata.JMRIVERMAJOR));
                v.setText("" + Metadata.getBySystemName(Metadata.JMRIVERMAJOR));
                item.addContent(v = new Element(Metadata.JMRIVERMINOR));
                v.setText("" + Metadata.getBySystemName(Metadata.JMRIVERMINOR));
                item.addContent(v = new Element(Metadata.JMRIVERTEST));
                v.setText("" + Metadata.getBySystemName(Metadata.JMRIVERTEST));
            }
        }
    }

    void immediateSetThrottle(Element item) {
        Integer address;
        useAttributes = (item.getAttribute(ADDRESS) != null);
        if (useAttributes) {
            address = Integer.parseInt(item.getAttributeValue(ADDRESS));
        } else {
            address = Integer.parseInt(item.getChild(ADDRESS).getText());
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
                    if ((a = item.getAttribute(SPEED)) != null) {
                        t.setSpeedSetting(a.getFloatValue());
                    } else {
                        item.setAttribute(SPEED, Float.toString(t.getSpeedSetting()));
                    }

                    if ((a = item.getAttribute(FORWARD)) != null) {
                        t.setIsForward(a.getBooleanValue());
                    } else {
                        item.setAttribute(FORWARD, Boolean.valueOf(t.getIsForward()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F0)) != null) {
                        t.setF0(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F0, Boolean.valueOf(t.getF0()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F1)) != null) {
                        t.setF1(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F1, Boolean.valueOf(t.getF1()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F2)) != null) {
                        t.setF2(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F2, Boolean.valueOf(t.getF2()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F3)) != null) {
                        t.setF3(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F3, Boolean.valueOf(t.getF3()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F4)) != null) {
                        t.setF4(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F4, Boolean.valueOf(t.getF4()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F5)) != null) {
                        t.setF5(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F5, Boolean.valueOf(t.getF5()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F6)) != null) {
                        t.setF6(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F6, Boolean.valueOf(t.getF6()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F7)) != null) {
                        t.setF7(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F7, Boolean.valueOf(t.getF7()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F8)) != null) {
                        t.setF8(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F8, Boolean.valueOf(t.getF8()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F9)) != null) {
                        t.setF9(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F9, Boolean.valueOf(t.getF9()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F10)) != null) {
                        t.setF10(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F10, Boolean.valueOf(t.getF10()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F11)) != null) {
                        t.setF11(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F11, Boolean.valueOf(t.getF11()).toString());
                    }

                    if ((a = item.getAttribute(Throttle.F12)) != null) {
                        t.setF12(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F12, Boolean.valueOf(t.getF12()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F13)) != null) {
                        t.setF13(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F13, Boolean.valueOf(t.getF13()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F14)) != null) {
                        t.setF14(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F14, Boolean.valueOf(t.getF14()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F15)) != null) {
                        t.setF15(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F15, Boolean.valueOf(t.getF15()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F16)) != null) {
                        t.setF16(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F16, Boolean.valueOf(t.getF16()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F17)) != null) {
                        t.setF17(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F17, Boolean.valueOf(t.getF17()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F18)) != null) {
                        t.setF18(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F18, Boolean.valueOf(t.getF18()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F19)) != null) {
                        t.setF19(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F19, Boolean.valueOf(t.getF19()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F20)) != null) {
                        t.setF20(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F20, Boolean.valueOf(t.getF20()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F21)) != null) {
                        t.setF21(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F21, Boolean.valueOf(t.getF21()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F22)) != null) {
                        t.setF22(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F22, Boolean.valueOf(t.getF22()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F23)) != null) {
                        t.setF23(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F23, Boolean.valueOf(t.getF23()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F24)) != null) {
                        t.setF24(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F24, Boolean.valueOf(t.getF24()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F25)) != null) {
                        t.setF25(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F25, Boolean.valueOf(t.getF25()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F26)) != null) {
                        t.setF26(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F26, Boolean.valueOf(t.getF26()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F27)) != null) {
                        t.setF27(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F27, Boolean.valueOf(t.getF27()).toString());
                    }
                    if ((a = item.getAttribute(Throttle.F28)) != null) {
                        t.setF28(a.getBooleanValue());
                    } else {
                        item.setAttribute(Throttle.F28, Boolean.valueOf(t.getF28()).toString());
                    }

                } catch (DataConversionException e) {
                }
            } else {
                Element e;

                if ((e = item.getChild(SPEED)) != null) {
                    t.setSpeedSetting(Float.parseFloat(e.getText()));
                } else {
                    item.addContent(new Element(SPEED)
                            .addContent(
                            "" + t.getSpeedSetting()));
                }

                if ((e = item.getChild(FORWARD)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setIsForward(false);
                    } else {
                        t.setIsForward(true);
                    }
                } else {
                    item.addContent(new Element(FORWARD)
                            .addContent(
                            t.getIsForward() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F0)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF0(false);
                    } else {
                        t.setF0(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F0)
                            .addContent(
                            t.getF0() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F1)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF1(false);
                    } else {
                        t.setF1(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F1)
                            .addContent(
                            t.getF1() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F2)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF2(false);
                    } else {
                        t.setF2(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F2)
                            .addContent(
                            t.getF2() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F3)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF3(false);
                    } else {
                        t.setF3(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F3)
                            .addContent(
                            t.getF3() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F4)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF4(false);
                    } else {
                        t.setF4(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F4)
                            .addContent(
                            t.getF4() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F5)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF5(false);
                    } else {
                        t.setF5(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F5)
                            .addContent(
                            t.getF5() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F6)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF6(false);
                    } else {
                        t.setF6(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F6)
                            .addContent(
                            t.getF6() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F7)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF7(false);
                    } else {
                        t.setF7(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F7)
                            .addContent(
                            t.getF7() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F8)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF8(false);
                    } else {
                        t.setF8(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F8)
                            .addContent(
                            t.getF8() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F9)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF9(false);
                    } else {
                        t.setF9(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F9)
                            .addContent(
                            t.getF9() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F10)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF10(false);
                    } else {
                        t.setF10(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F10)
                            .addContent(
                            t.getF10() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F11)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF11(false);
                    } else {
                        t.setF11(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F11)
                            .addContent(
                            t.getF11() ? TRUE : FALSE));
                }

                if ((e = item.getChild(Throttle.F12)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF12(false);
                    } else {
                        t.setF12(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F12)
                            .addContent(
                            t.getF12() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F13)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF13(false);
                    } else {
                        t.setF13(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F13).addContent(t.getF13() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F14)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF14(false);
                    } else {
                        t.setF14(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F14).addContent(t.getF14() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F15)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF15(false);
                    } else {
                        t.setF15(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F15).addContent(t.getF15() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F16)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF16(false);
                    } else {
                        t.setF16(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F16).addContent(t.getF16() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F17)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF17(false);
                    } else {
                        t.setF17(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F17).addContent(t.getF17() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F18)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF18(false);
                    } else {
                        t.setF18(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F18).addContent(t.getF18() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F19)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF19(false);
                    } else {
                        t.setF19(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F19).addContent(t.getF19() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F20)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF20(false);
                    } else {
                        t.setF20(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F20).addContent(t.getF20() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F21)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF21(false);
                    } else {
                        t.setF21(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F21).addContent(t.getF21() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F22)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF22(false);
                    } else {
                        t.setF22(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F22).addContent(t.getF22() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F23)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF23(false);
                    } else {
                        t.setF23(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F23).addContent(t.getF23() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F24)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF24(false);
                    } else {
                        t.setF24(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F24).addContent(t.getF24() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F25)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF25(false);
                    } else {
                        t.setF25(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F25).addContent(t.getF25() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F26)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF26(false);
                    } else {
                        t.setF26(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F26).addContent(t.getF26() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F27)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF27(false);
                    } else {
                        t.setF27(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F27).addContent(t.getF27() ? TRUE : FALSE));
                }
                if ((e = item.getChild(Throttle.F28)) != null) {
                    if (e.getText().equals(FALSE)) {
                        t.setF28(false);
                    } else {
                        t.setF28(true);
                    }
                } else {
                    item.addContent(new Element(Throttle.F28).addContent(t.getF28() ? TRUE : FALSE));
                }

            }
            // The speedStepMode is sent every time since a XMLIO client may
            // reuse an existing throttle context and not be aware of the mode
            // if the mode is only sent when the throttle context is created.
            // This will only be sent as an attribute of a throttle element
            // to prevent clients that don't know what to do with unknown
            // elements from crashing.
            if (useAttributes) {
                item.setAttribute(SSM, Integer.toString(t.getSpeedStepMode()));
            } else {
                item.addContent(new Element(SSM).addContent(Integer.toString(t.getSpeedStepMode())));
            }
        }
    }

    // class to persist (from one usage to another)
    // throttle information
    static class ThrottleContext {

        DccThrottle throttle;
    }

    // class for firing off requests to handle a deferred read
    class DeferredRead implements PropertyChangeListener {

        public Thread thread;
        Element request;
        XmlIORequestor requestor;
        String client;

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            boolean changed = checkValues(request);

            if (!changed) {
                return;
            }

            // found change, pull listeners and return
            @SuppressWarnings("unchecked")
            List<Element> items = request.getChildren();
            for (Element item : items) {
                String type = item.getName();
                String name = item.getAttributeValue(NAME);
                useAttributes = (!type.equals(ITEM));
                if (!useAttributes) {
                    type = item.getChild(TYPE).getText();
                    name = item.getChild(NAME).getText();
                } else if (name == null) {
                    name = "";
                }
                if (type.equals(TURNOUT)) {
                    removeListenerFromTurnout(name, item, this);
                } else if (type.equals(MEMORY)) {
                    removeListenerFromMemory(name, item, this);
                } else if (type.equals(SENSOR)) {
                    removeListenerFromSensor(name, item, this);
                } else if (type.equals(SIGNAL_HEAD)) {
                    removeListenerFromSignalHead(name, item, this);
                } else if (type.equals(SIGNAL_MAST)) {
                    removeListenerFromSignalMast(name, item, this);
                } else if (type.equals(ROUTE)) {
                    removeListenerFromRoute(name, item, this);
                } else if (type.equals(POWER)) {
                    removeListenerFromPower(name, item, this);
                } else {
                    log.warn("Unexpected type: " + type);
                }
            }

            sendMonitorReply(request, requestor, client, thread);

        }
    }
}

/* @(#)DefaultXmlIOServer.java */

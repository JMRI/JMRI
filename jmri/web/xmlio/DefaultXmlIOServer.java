// DefaultXmlIOServer.java

package jmri.web.xmlio;

import jmri.*;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;

import org.jdom.*;

import java.io.File;
import java.util.*;

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
 * @version	$Revision: 1.21 $
 * @see  jmri.web.xmlio.XmlIOFactory
 */
public class DefaultXmlIOServer implements XmlIOServer {

    public Element immediateRequest(Element e) throws JmriException {
    
        // first, process any "list" elements
    	//  roster and panel are immediate only
    	//  power, turnout, sensor and route can be monitored for changes, pass current values to begin
        @SuppressWarnings("unchecked")
        List<Element> lists = new ArrayList(e.getChildren("list"));
        for (Element list : lists) {
            e.removeContent(list);
            String type = list.getChild("type").getText();
            if (type.equals("turnout")) {
                // add an element for each turnout
                TurnoutManager m = InstanceManager.turnoutManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Turnout t = m.getTurnout(name);
                    Element n = new Element("item");
                    n.addContent(new Element("type").addContent("turnout"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    n.addContent(new Element("inverted").addContent(Boolean.valueOf(t.getInverted()).toString()));
                    e.addContent(n);
                }
            } else if (type.equals("route")) {
                // add an element for each route
                RouteManager m = InstanceManager.routeManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Route t = m.getRoute(name);
                    Element n = new Element("item");
                    n.addContent(new Element("type").addContent("route"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    e.addContent(n);
                }
            } else if (type.equals("sensor")) {
                // add an element for each sensor
                SensorManager m = InstanceManager.sensorManagerInstance();
                List<String> names = m.getSystemNameList();
                for (String name : names) {
                    Sensor t = m.getSensor(name);
                    Element n = new Element("item");
                    n.addContent(new Element("type").addContent("sensor"));
                    n.addContent(new Element("name").addContent(name));
                    n.addContent(new Element("userName").addContent(t.getUserName()));
                    n.addContent(new Element("comment").addContent(t.getComment()));
                    n.addContent(new Element("inverted").addContent(Boolean.valueOf(t.getInverted()).toString()));
                    e.addContent(n);
                }            
            } else if (type.equals("roster")) {
            	// add an element for each roster entry
            	List <RosterEntry> rlist = Roster.instance().matchingList(null, null, null, null, null, null, null);
            	for (int i = 0; i < rlist.size(); i++) {
            		RosterEntry entry = rlist.get(i);
            		Element n = new Element("item");
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
        			e.addContent(n);
            	}

            } else if (type.equals("panel")) {
            	// list panels, (open JMRI windows)
            	List<JmriJFrame> framesList = JmriJFrame.getFrameList();
            	int framesNumber = framesList.size();
            	for (int i = 0; i < framesNumber; i++) { //add all non-blank titles to list
            		JmriJFrame iFrame = framesList.get(i);
            		String frameTitle = iFrame.getTitle();
            		if (!frameTitle.equals("")) {
            			Element n = new Element("item");
                        n.addContent(new Element("type").addContent("panel"));
                        //get rid of spaces in name
            			n.addContent(new Element("name").addContent(frameTitle.replaceAll(" ","%20")));
            			n.addContent(new Element("userName").addContent(frameTitle)); 
            			e.addContent(n);
            		}
            	}

            } else if (type.equals("power")) {
            	// add a power element
                Element n = new Element("item");
                n.addContent(new Element("type").addContent("power"));
                n.addContent(new Element("name").addContent("power"));
                e.addContent(n);
            } else log.warn("Unexpected type in list element: "+type);
        }
        
        // then process "throttle" elements
        @SuppressWarnings("unchecked")
        List<Element> throttles = e.getChildren("throttle");
        for (Element throttle : throttles) {
            immediateSetThrottle(throttle);
        }
        
        // then handle the specific items elements.
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren("item");
        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            
            //check for "set" values and process them
            if (type.equals("turnout")) immediateWriteTurnout(name, item);
            else if (type.equals("route")) immediateWriteRoute(name, item);
            else if (type.equals("sensor")) immediateWriteSensor(name, item);
            else if (type.equals("power")) immediateWritePower(name, item);
            else if (type.equals("roster")) {}
            else if (type.equals("panel")) {}
            else log.warn("Unexpected type in item: "+type);
        }
        
        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            
            if (type.equals("turnout")) immediateReadTurnout(name, item);
            else if (type.equals("route")) immediateReadRoute(name, item);
            else if (type.equals("sensor")) immediateReadSensor(name, item);
            else if (type.equals("power")) immediateReadPower(name, item);
        }
        
        return e;
    }

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
        List<Element> items = e.getChildren("item");

        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            
            if (type.equals("turnout")) addListenerToTurnout(name, item, dr);
            else if (type.equals("route")) addListenerToRoute(name, item, dr);
            else if (type.equals("sensor")) addListenerToSensor(name, item, dr);
            else if (type.equals("power")) addListenerToPower(name, item, dr);
            else log.warn("Unexpected type: "+type);
        }

        // Check one more time to ensure clear of race conditions
        dr.propertyChange(null);

    }

    void sendMonitorReply(Element e, XmlIORequestor r) {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren("item");
        
        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            
            try {
                if (type.equals("turnout")) immediateReadTurnout(name, item);
                else if (type.equals("sensor")) immediateReadSensor(name, item);
                else if (type.equals("route")) immediateReadRoute(name, item);
                else if (type.equals("power")) immediateReadPower(name, item);
            } catch (JmriException j) {
                log.warn("exception handling "+type+" "+name, j);
            }
        }
        
        r.monitorReply(e);
        return;
    }
    
    boolean checkValues(Element e) {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren("item");
        
        boolean changed = false;
        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            if (item.getChild("value") == null) return true;  // if no value, consider changed

            try {
                if (type.equals("turnout")) changed |= monitorProcessTurnout(name, item);
                else if (type.equals("sensor")) changed |= monitorProcessSensor(name, item);
                else if (type.equals("route")) changed |= monitorProcessRoute(name, item);
                else if (type.equals("power")) changed |= monitorProcessPower(name, item);
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
    
    /**
     * Return true if there is a difference
     */
    boolean monitorProcessTurnout(String name, Element item) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for value element, which means compare
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            return  (b.getKnownState() != state);
        }
        return false;  // no difference
    }
    
    /**
     * Return true if there is a difference
     */
    boolean monitorProcessRoute(String name, Element item) {
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);

        // check for value element, which means compare
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            return  (b.getState() != state);
        }
        return false;  // no difference
    }
    
    /**
     * Return true if there is a difference
     */
    boolean monitorProcessSensor(String name, Element item) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        // check for value element, which means compare
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            return  (b.getState() != state);
        }
        return false;  // no difference
    }
    
    /**
     * Return true if there is a difference
     */
    boolean monitorProcessPower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        // check for value element, which means compare
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            return  (b.getPower() != state);
        }
        return false;  // no difference
    }
    
    void immediateWriteTurnout(String name, Element item) {
        // get turnout
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for set element, which means write
        Element v = item.getChild("set");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setCommandedState(state);
            item.removeContent(v);
        }
    }
    
    void immediateWriteSensor(String name, Element item) throws JmriException {
        // get turnout
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        // check for set element, which means write
        Element v = item.getChild("set");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setState(state);
            item.removeContent(v);
        }
    }
    
    void immediateWriteRoute(String name, Element item) throws JmriException {
        // get route
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);

        // check for set element, which means write
        Element v = item.getChild("set");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setState(state);  //TODO: this is not correct, fix next time
            item.removeContent(v);
        }
    }
    
    void immediateWritePower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        // check for set element, which means write
        Element v = item.getChild("set");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setPower(state);
            // remove set element
            item.removeContent(v);
        }

        v = item.getChild("value");
        if (v == null) item.addContent(v = new Element("value"));
        // set result
        v.setText(""+b.getPower());
    }
    
    void immediateWriteRoster(String name, Element item) throws JmriException {
        log.error("no immediate write for roster element");
    }
    
    void immediateWritePanel(String name, Element item) throws JmriException {
        log.error("no immediate write for panel element");
    }
    
    void immediateReadTurnout(String name, Element item) {
        // get turnout
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getKnownState());
    }
    
    void immediateReadRoute(String name, Element item) {
        // get route
        Route b = InstanceManager.routeManagerInstance().provideRoute(name, null);

        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getState());
    }
    
    void immediateReadSensor(String name, Element item) {
        // get turnout
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getState());
    }
    
    void immediateReadPower(String name, Element item) throws JmriException {
        // get power manager
        PowerManager b = InstanceManager.powerManagerInstance();

        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getPower());
    }
    
    static HashMap<Integer, ThrottleContext> map = new HashMap<Integer, ThrottleContext>();
    
    void immediateSetThrottle(Element item) {
        Integer address = Integer.parseInt(item.getChild("address").getText());
        ThrottleContext tc = map.get(address);
        if (tc == null) {
            // first request does the allocation
            InstanceManager.throttleManagerInstance()
                .requestThrottle(address,new ThrottleListener() {
                    public void notifyThrottleFound(DccThrottle t) {
                        log.debug("callback for throttle");
                        // store back into context
                        ThrottleContext tc = new ThrottleContext();
                        tc.throttle = t;
                        Integer address = Integer.valueOf( ((DccLocoAddress)t.getLocoAddress()).getNumber());
                        map.put(address, tc);
                    }
                    public void notifyFailedThrottleRequest(jmri.DccLocoAddress address, String reason){
                    }
                });

        } else {
            log.debug("process active throttle");
            // set speed, etc, as needed
            DccThrottle t = tc.throttle;
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
        
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            boolean changed = checkValues(request);
            
            if (!changed) return;
            
            // found change, pull listeners and return
            @SuppressWarnings("unchecked")
            List<Element> items = request.getChildren("item");
    
            for (Element item : items) {
                String type = item.getChild("type").getText();
                String name = item.getChild("name").getText();
                
                if (type.equals("turnout")) removeListenerFromTurnout(name, item, this);
                else if (type.equals("sensor")) removeListenerFromSensor(name, item, this);
                else if (type.equals("route")) removeListenerFromRoute(name, item, this);
                else if (type.equals("power")) removeListenerFromPower(name, item, this);
                else log.warn("Unexpected type: "+type);
            }
            
            sendMonitorReply(request, requestor);
            
        }
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultXmlIOServer.class.getName());
}

/* @(#)DefaultXmlIOServer.java */

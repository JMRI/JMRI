// DefaultXmlIOServer.java

package jmri.web.xmlio;

import jmri.*;
import org.jdom.*;
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
 * @version	$Revision: 1.2 $
 * @see  jmri.web.xmlio.XmlIOFactory
 */
public class DefaultXmlIOServer implements XmlIOServer {

    public Element immediateRequest(Element e) throws JmriException {
        @SuppressWarnings("unchecked")
        List<Element> items = e.getChildren("item");
        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            
            if (type.equals("turnout")) immediateWriteTurnout(name, item);
            else if (type.equals("sensor")) immediateWriteSensor(name, item);
            else log.warn("Unexpected type: "+type);
        }
        
        for (Element item : items) {
            String type = item.getChild("type").getText();
            String name = item.getChild("name").getText();
            
            if (type.equals("turnout")) immediateReadTurnout(name, item);
            else if (type.equals("sensor")) immediateReadSensor(name, item);
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
            else if (type.equals("sensor")) addListenerToSensor(name, item, dr);
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
            
            if (type.equals("turnout")) immediateReadTurnout(name, item);
            else if (type.equals("sensor")) immediateReadSensor(name, item);
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
            
            if (type.equals("turnout")) changed |= monitorProcessTurnout(name, item);
            else if (type.equals("sensor")) changed |= monitorProcessSensor(name, item);
            else log.warn("Unexpected type: "+type);
        }
        return changed;
    }
    
    void addListenerToTurnout(String name, Element item, DeferredRead dr) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        b.addPropertyChangeListener(dr);
    }
    
    void addListenerToSensor(String name, Element item, DeferredRead dr) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);
        b.addPropertyChangeListener(dr);
    }
    
    void removeListenerFromTurnout(String name, Element item, DeferredRead dr) {
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        b.removePropertyChangeListener(dr);
    }
    
    void removeListenerFromSensor(String name, Element item, DeferredRead dr) {
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);
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
    
    void immediateWriteTurnout(String name, Element item) {
        // get turnout
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout(name);

        // check for value element, which means write
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setCommandedState(state);
        }
    }
    
    void immediateWriteSensor(String name, Element item) throws JmriException {
        // get turnout
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        // check for value element, which means write
        Element v = item.getChild("value");
        if (v!=null) {
            int state = Integer.parseInt(v.getText());
            b.setState(state);
        }
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
    
    void immediateReadSensor(String name, Element item) {
        // get turnout
        Sensor b = InstanceManager.sensorManagerInstance().provideSensor(name);

        Element v = item.getChild("value");

        // Start read: ensure value element
        if (v == null) item.addContent(v = new Element("value"));
        
        // set result
        v.setText(""+b.getState());
    }
    
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
                else log.warn("Unexpected type: "+type);
            }
            
            sendMonitorReply(request, requestor);
            
        }
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultXmlIOServer.class.getName());
}

/* @(#)DefaultXmlIOServer.java */

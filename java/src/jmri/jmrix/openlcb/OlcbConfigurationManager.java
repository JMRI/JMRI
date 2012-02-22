// OlcbConfigurationManager.java

package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.InstanceManager;
import java.util.ResourceBundle;

import org.openlcb.can.AliasMap;
import org.openlcb.can.MessageBuilder;
import org.openlcb.can.OpenLcbCanFrame;
import org.openlcb.MimicNodeStore;
import org.openlcb.Connection;
import org.openlcb.Message;

import jmri.jmrix.can.*;

/**
 * Does configuration for OpenLCB communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision: 19643 $
 */
public class OlcbConfigurationManager extends jmri.jmrix.can.ConfigurationManager {
    
    public OlcbConfigurationManager(CanSystemConnectionMemo memo){
        super(memo);
        
        InstanceManager.store(cf = new jmri.jmrix.openlcb.swing.OpenLcbComponentFactory(adapterMemo), 
            jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, OlcbConfigurationManager.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    public void configureManagers(){
    
        // create OpenLCB objects
        aliasMap = new AliasMap();
        messageBuilder = new MessageBuilder(aliasMap);
        nodeStore = new MimicNodeStore();
            
        // create JMRI objects
        InstanceManager.setSensorManager(
            getSensorManager());
            
        InstanceManager.setTurnoutManager(
            getTurnoutManager());
        
        // do the connections
        TrafficController tc = adapterMemo.getTrafficController();
        
        tc.addCanListener(new ReceivedFrameAdapter());
        
        // show active
        ActiveFlag.setActive();
    }
    
    AliasMap aliasMap;
    MessageBuilder messageBuilder;
    MimicNodeStore nodeStore;
    
    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled())
            return false;
        if (type.equals(jmri.SensorManager.class))
            return true;
        if (type.equals(jmri.TurnoutManager.class))
            return true;
        if (type.equals(AliasMap.class))
            return true;
        if (type.equals(MessageBuilder.class))
            return true;
        if (type.equals(MimicNodeStore.class))
            return true;
        return false; // nothing, by default
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled())
            return null;
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        if (T.equals(AliasMap.class))
            return (T)aliasMap;
        if (T.equals(MessageBuilder.class))
            return (T)messageBuilder;
        if (T.equals(MimicNodeStore.class))
            return (T)nodeStore;
        return null; // nothing, by default
    }
    
    protected OlcbTurnoutManager turnoutManager;
    
    public OlcbTurnoutManager getTurnoutManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (turnoutManager == null)
            turnoutManager = new OlcbTurnoutManager(adapterMemo);
        return turnoutManager;
    }
    
    protected OlcbSensorManager sensorManager;
    
    public OlcbSensorManager getSensorManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (sensorManager == null)
            sensorManager = new OlcbSensorManager(adapterMemo);
        return sensorManager;
    }
    
    public void dispose(){
        if (turnoutManager != null) 
            InstanceManager.deregister(turnoutManager, jmri.jmrix.openlcb.OlcbTurnoutManager.class);
        if (sensorManager != null) 
            InstanceManager.deregister(sensorManager, jmri.jmrix.openlcb.OlcbSensorManager.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.deregister(this, OlcbConfigurationManager.class);
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        //No actions that can be loaded at startup
        return null;
    }

    /**
     * Receives frames from the TrafficController, and
     * forwards into OpenLCB system objects
     */
    class ReceivedFrameAdapter implements jmri.jmrix.can.CanListener {
        public synchronized void message(CanMessage l) {
            int header = l.getHeader();
            
            OpenLcbCanFrame frame = new OpenLcbCanFrame(header & 0xFFF);
            frame.setHeader(l.getHeader());
            if (l.getNumDataElements() != 0) {
                byte[] data = new byte[l.getNumDataElements()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte)l.getElement(i);
                }
                frame.setData(data);
            }
            
            aliasMap.processFrame(frame);
            if (log.isDebugEnabled()) log.debug("processing message frame "+frame);
            java.util.List<Message> list = messageBuilder.processFrame(frame);
            if (list != null) {
                for (Message m : list) {
                    nodeStore.put(m, null);
                }
            }
        }
        public synchronized void reply(CanReply l) { 
            int header = l.getHeader();
            
            OpenLcbCanFrame frame = new OpenLcbCanFrame(header & 0xFFF);
            frame.setHeader(l.getHeader());
            if (l.getNumDataElements() < 0) {
                log.error("Unexpected negative length in "+l);
            }
            if (l.getNumDataElements() > 0) {
                byte[] data = new byte[l.getNumDataElements()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte)l.getElement(i);
                }
                frame.setData(data);
            }
            
            aliasMap.processFrame(frame);
            if (log.isDebugEnabled()) log.debug("processing reply frame "+frame);
            java.util.List<Message> list = messageBuilder.processFrame(frame);
            if (list != null) {
                for (Message m : list) {
                    nodeStore.put(m, null);
                }
            }
        }
    }
    
    static class TransmittedFrameAdapter implements Connection {
        public void put(org.openlcb.Message m,org.openlcb.Connection c) {
            System.out.println("c: "+m);
        }
    }

static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbConfigurationManager.class.getName());
}

/* @(#)ConfigurationManager.java */

package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating a MERG CBUS Command Station + other network objects.
 * <p>
 * By default starts with 1 command station, a node in SLiM mode,
 * and an event request responder.
 * <p>
 * All simulation responses can be sent as {@link jmri.jmrix.can.CanMessage} or {@link jmri.jmrix.can.CanReply}
 *
 * @author Steve Young Copyright (C) 2018
 * @see CbusDummyCS
 * @see CbusEventResponder
 * @see CbusDummyNode
 * @since 4.15.2
 */
public class CbusSimulator {

    private final CanSystemConnectionMemo memo;
    public ArrayList<CbusDummyCS> _csArr;
    public ArrayList<CbusDummyNode> _ndArr;
    public ArrayList<CbusEventResponder> _evResponseArr;

    public CbusSimulator(CanSystemConnectionMemo sysmemo){
        memo = sysmemo;
        // todo - store in memo, not instance
        jmri.InstanceManager.store(this,CbusSimulator.class);
        init();
    }
    
    public final void init(){
        log.info("Starting CBUS Network Simulation Tools");
        _csArr = new ArrayList<>();
        _csArr.add(new CbusDummyCS(memo)); // type, id, memo
        
        _ndArr = new ArrayList<>();
        // _ndArr.add(new CbusDummyNode(0,165,0,0,memo)); // nn, manufacturer, type, canid, memo
        
        _evResponseArr = new ArrayList<>();
        _evResponseArr.add(new CbusEventResponder(memo) );
    }
    
    public int getNumCS(){
        return _csArr.size();
    }
    
    public int getNumNd(){
        return _ndArr.size();
    }
    
    public int getNumEv(){
        return _evResponseArr.size();
    }
    
    public CbusDummyCS getCS(int id){
        return _csArr.get(id);
    } 
    
    public CbusDummyNode getNd(int id){
        return _ndArr.get(id);
    }

    public void addNode ( CbusDummyNode nd) {
        _ndArr.add(nd);
    }

    public void removeNode(CbusDummyNode nd) {
        _ndArr.remove(nd);
    }

    public CbusEventResponder getEv( int id ){
        return _evResponseArr.get(id);
    }
    
    public CbusDummyCS getNewCS(){
        CbusDummyCS newcs = new CbusDummyCS(memo);
        _csArr.add(newcs);
        return newcs;
    }

    public CbusEventResponder getNewEv(){
        CbusEventResponder newcs = new CbusEventResponder(memo);
        _evResponseArr.add(newcs);
        return newcs;
    }

    // removes CanListeners
    // resets all command stations to stop any session timers
    public void dispose() {
        log.info("Stopping all CBUS Simulation Tools");
        for (int i = 0; i < _csArr.size(); i++) {
            _csArr.get(i).dispose();
            _csArr.set(i,null);
        }
        _csArr = null;
        
        for (int i = 0; i < _ndArr.size(); i++) {
            _ndArr.get(i).dispose();
            _ndArr.set(i,null);
        }        
        _ndArr = null;
        
        for (int i = 0; i < _evResponseArr.size(); i++) {
            _evResponseArr.get(i).dispose();
            _evResponseArr.set(i,null);
        } 
        _evResponseArr = null;
        
        jmri.InstanceManager.deregister(this, CbusSimulator.class);
    }

    private static final Logger log = LoggerFactory.getLogger(CbusSimulator.class);

}

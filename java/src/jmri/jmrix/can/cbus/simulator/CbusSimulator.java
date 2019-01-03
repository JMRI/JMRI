package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.simulator.CbusDummyCS;
import jmri.jmrix.can.cbus.simulator.CbusEventResponder;
import jmri.jmrix.can.TrafficController;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simultaing a MERG CBUS Command Station + other network objects
 * @author Steve Young Copyright (C) 2018
 * @since 4.15.2
 */
public class CbusSimulator implements CanListener {

    private TrafficController tc;
    private CanSystemConnectionMemo memo;

    public ArrayList<CbusDummyCS> _csNodes;
    public ArrayList<CbusEventResponder> _evResponseArr;

    private Boolean _processIn;
    private Boolean _processOut;
    private Boolean _sendIn;
    private Boolean _sendOut;
    
    public static ArrayList<String> csTypes = new ArrayList<String>();
    public static ArrayList<String> csTypesTip = new ArrayList<String>();
    
    public static ArrayList<String> evModes = new ArrayList<String>();
    public static ArrayList<String> evModesTip = new ArrayList<String>();
    
    public static int DEFAULT_DELAY = 100; // ms

    public CbusSimulator(CanSystemConnectionMemo sysmemo){
        memo = sysmemo;
        if (memo != null) {
            tc = memo.getTrafficController();
            tc.addCanListener(this);
        }
        init();
    }
    
    public CbusSimulator() {
    }
    
    public void init(){
        log.info("Starting CBUS Network Simulation Tools");
        
        csTypes.add(Bundle.getMessage("cSDisabled"));
        csTypesTip.add(null);
        
        csTypes.add(Bundle.getMessage("csStandard"));
        csTypesTip.add("Based on CANCMD v3");
        
        evModes.add(Bundle.getMessage("HighlightDisabled"));
        evModesTip.add(null);
        
        evModes.add(Bundle.getMessage("onOffRand"));
        evModesTip.add(null);          
        
        evModes.add(Bundle.getMessage("odOnEvOff"));
        evModesTip.add(null);         
        
        evModes.add(Bundle.getMessage("CbusEventOn"));
        evModesTip.add(null);         
        
        evModes.add(Bundle.getMessage("CbusEventOff"));
        evModesTip.add(null);
        
        _csNodes = new ArrayList<CbusDummyCS>();
        _csNodes.add(new CbusDummyCS(1,this));
        
        _evResponseArr = new ArrayList<CbusEventResponder>();
        _evResponseArr.add(new CbusEventResponder(1,this) );
        
        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        
    }
    
    public int getNumCS(){
        return _csNodes.size();
    }
    
    public int getNewCSID(int type){
        _csNodes.add(new CbusDummyCS(type,this));
        return _csNodes.size()-1;
    }
    
    public int getNewEvID(int mode){
        _evResponseArr.add(new CbusEventResponder(mode,this));
        return _evResponseArr.size()-1;
    }
    
    public void setProcessIn( Boolean newval){
        _processIn = newval;
    }
    
    public void setProcessOut( Boolean newval){
        _processOut = newval;
    }

    public void setSendIn( Boolean newval){
        _sendIn = newval;
    }

    public void setSendOut( Boolean newval){
        _sendOut = newval;
    }
    
    public Boolean getProcessIn() {
        return _processIn;
    }
    
    public Boolean getProcessOut() {
        return _processOut;
    }    
    
    public Boolean getSendIn() {
        return _sendIn;
    }    
    
    public Boolean getSendOut() {
        return _sendOut;
    }       
    
    public void resetCs(int cs){
        _csNodes.get(cs).resetCS();
    }

    @Override
    public void message(CanMessage m) {
        if ( _processOut ) {
            CanFrame test = m;
            processCan(test);
        }
    }

    @Override
    public void reply(CanReply r) {
        if ( _processIn ) {
            CanFrame test = r;
            processCan(test);
        }
    }

    public void processCan(CanFrame m) {
        for ( int i=0 ; ( i < _csNodes.size() ) ; i++ ) {
            int opc = 0;
            if ( _csNodes.get(i).getDummyType() != 0 ) {
                opc = m.getElement(0);
            }
            
            if ( opc == CbusConstants.CBUS_RTON ) {
                _csNodes.get(i).setTrackPower(true);
            }
            else if ( opc == CbusConstants.CBUS_RTOF ) {
                _csNodes.get(i).setTrackPower(false);
            }
            else if ( opc == CbusConstants.CBUS_RESTP ) {
                _csNodes.get(i).setEstop();
            }
            else if ( opc == CbusConstants.CBUS_RLOC ) {
                int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
                boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
                _csNodes.get(i).processrloc(rcvdIntAddr,rcvdIsLong);
            }
            else if ( opc == CbusConstants.CBUS_QLOC ) {
                int session = m.getElement(1);
                _csNodes.get(i).processQloc( session );
            }
            else if ( opc == CbusConstants.CBUS_DSPD ) {
                int session = m.getElement(1);
                int speeddir = m.getElement(2);
                _csNodes.get(i).processDspd( session, speeddir );
            }
            else if ( opc == CbusConstants.CBUS_DKEEP ) {
                int session = m.getElement(1);
                _csNodes.get(i).processDkeep( session );
            }
            else if ( opc == CbusConstants.CBUS_KLOC ) {
                int session = m.getElement(1);
                _csNodes.get(i).processKloc( session );
            }
        }
        
        if (CbusOpCodes.isEventRequest(m.getElement(0))) {
            for ( int i=0 ; ( i < _evResponseArr.size() ) ; i++ ) {
                _evResponseArr.get(i).processEventforResponse(m);
            }
        }
    }
    
    public void sendReplyWithDelay(CanReply r, int delay) {
        CbusMessage.setId(r, tc.getCanid() );
        CbusMessage.setPri(r, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        ThreadingUtil.runOnLayoutEventually( ()->{
            try {
                Thread.sleep(delay); // default 100ms
                if (_sendIn) {
                    tc.sendCanReply(r, this);
                }
                if (_sendOut) {
                    CanMessage m = new CanMessage(r);
                    tc.sendCanMessage(m, this);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.warn("sendMessage: Exception: " + ex.toString());
            }
        });
    }
    
    public void dispose() {
        if (tc != null) {
            tc.removeCanListener(this);
        }
        _csNodes=null;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusSimulator.class);

}

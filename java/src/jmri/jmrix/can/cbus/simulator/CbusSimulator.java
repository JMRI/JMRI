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
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
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

    public ArrayList<CbusDummyCS> _csArr;
    public ArrayList<CbusDummyNode> _ndArr;
    public ArrayList<CbusEventResponder> _evResponseArr;

    private Boolean _processIn;
    private Boolean _processOut;
    private Boolean _sendIn;
    private Boolean _sendOut;
    
    public static ArrayList<String> csTypes = new ArrayList<String>();
    public static ArrayList<String> csTypesTip = new ArrayList<String>();

    public static ArrayList<Integer> ndTypes = new ArrayList<Integer>();
    
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
        
        ndTypes.add(0);
        // 29 CANPAN
        ndTypes.add(29);
        
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
        
        _csArr = new ArrayList<CbusDummyCS>();
        _csArr.add(new CbusDummyCS(1,this,0)); // type, this, id
        
        _ndArr = new ArrayList<CbusDummyNode>();
        _ndArr.add(new CbusDummyNode(0,this,1)); // type, this, id
        
        _evResponseArr = new ArrayList<CbusEventResponder>();
        _evResponseArr.add(new CbusEventResponder(1,this,0) );
        
        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        
    }
    
    public int getNumCS(){
        return _csArr.size();
    }
    
    public int getNewCSID(int type){
        int newIndex = 1;
        for (int i = 0; i < _csArr.size(); i++) {
            if ( newIndex <= _csArr.get(i).getSimId() ) {
                newIndex = _csArr.get(i).getSimId()+1;
            }
        }
        _csArr.add(new CbusDummyCS(type,this,newIndex));
        return newIndex;
    }
    
    public int getNewEvID(int mode){
        int newIndex = 1;
        for (int i = 0; i < _evResponseArr.size(); i++) {
            if ( newIndex <= _evResponseArr.get(i).getSimId() ) {
                newIndex = _evResponseArr.get(i).getSimId()+1;
            }
        }
        _evResponseArr.add(new CbusEventResponder(mode,this,newIndex));
        return newIndex;
    }
    
    public int getNewNdID(int mode){
        int newIndex = 1;
        for (int i = 0; i < _ndArr.size(); i++) {
            if ( newIndex <= _ndArr.get(i).getSimId() ) {
                newIndex = _ndArr.get(i).getSimId()+1;
            }
        }
        _ndArr.add(new CbusDummyNode(0,this,newIndex)); 
        return newIndex;
    }
    
    public CbusDummyNode getNodeFromId(int id){
        for (int i = 0; i < _ndArr.size(); i++) {
            if ( id==_ndArr.get(i).getSimId() ){
                return _ndArr.get(i);
            }
        }        
        return null;
    }

    public CbusDummyCS getCSFromId(int id){
        for (int i = 0; i < _csArr.size(); i++) {
            if ( id==_csArr.get(i).getSimId() ){
                return _csArr.get(i);
            }
        }        
        return null;
    }

    public CbusEventResponder getEvRFromId(int id){
        for (int i = 0; i < _evResponseArr.size(); i++) {
            if ( id==_evResponseArr.get(i).getSimId() ){
                return _evResponseArr.get(i);
            }
        }        
        return null;
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
        for (int i = 0; i < _csArr.size(); i++) {
            if ( cs==_csArr.get(i).getSimId() ){
                _csArr.get(i).resetCS();
            }
        }
    }

    public void resetNd(int id){
        for (int i = 0; i < _ndArr.size(); i++) {
            if ( id==_ndArr.get(i).getSimId() ){
                _ndArr.get(i).resetNode();
            }
        }
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
        for ( int i=0 ; ( i < _csArr.size() ) ; i++ ) {
            int opc = 0;
            if ( _csArr.get(i).getDummyType() != 0 ) {
                opc = m.getElement(0);
            }
            
            if ( opc == CbusConstants.CBUS_RTON ) {
                _csArr.get(i).setTrackPower(true);
            }
            else if ( opc == CbusConstants.CBUS_RTOF ) {
                _csArr.get(i).setTrackPower(false);
            }
            else if ( opc == CbusConstants.CBUS_RESTP ) {
                _csArr.get(i).setEstop();
            }
            else if ( opc == CbusConstants.CBUS_RLOC ) {
                int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
                boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
                _csArr.get(i).processrloc(rcvdIntAddr,rcvdIsLong);
            }
            else if ( opc == CbusConstants.CBUS_QLOC ) {
                int session = m.getElement(1);
                _csArr.get(i).processQloc( session );
            }
            else if ( opc == CbusConstants.CBUS_DSPD ) {
                int session = m.getElement(1);
                int speeddir = m.getElement(2);
                _csArr.get(i).processDspd( session, speeddir );
            }
            else if ( opc == CbusConstants.CBUS_DKEEP ) {
                int session = m.getElement(1);
                _csArr.get(i).processDkeep( session );
            }
            else if ( opc == CbusConstants.CBUS_KLOC ) {
                int session = m.getElement(1);
                _csArr.get(i).processKloc( session );
            }
        }
        
        if (CbusOpCodes.isEventRequest(m.getElement(0))) {
            for ( int i=0 ; ( i < _evResponseArr.size() ) ; i++ ) {
                _evResponseArr.get(i).processEventforResponse(m);
            }
        }
        
        for ( int i=0 ; ( i < _ndArr.size() ) ; i++ ) {
            _ndArr.get(i).passMessage(m);
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
        _csArr=null;
        _ndArr=null;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusSimulator.class);

}

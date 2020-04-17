package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.swing.simulator.CsPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating a MERG CBUS Command Station.
 * Can operate as stand-alone or managed via @CbusSimulator.
 *
 * @author Steve Young Copyright (C) 2018 2019
 * @see CbusSimulator
 * @see CbusDummyCSSession
 * @since 4.15.2
 */
public class CbusDummyCS extends CbusSimCanListener {
    
    private ArrayList<CbusDummyCSSession> _csSessions;
    private int _simType;
    private int _maxSessions;
    private int _currentSessions;
    private boolean _trackOn;
    private boolean _estop;
    private CsPane _pane;
    
    public ArrayList<String> csTypes = new ArrayList<>();
    public ArrayList<String> csTypesTip = new ArrayList<>();
    
    protected static int DEFAULT_CS_TIMEOUT = 60000; // ms
    protected static int DEFAULT_SESSION_START_SPDDIR = 128;  // default DCC speed direction on start session
    
    public CbusDummyCS( CanSystemConnectionMemo sysmemo ){
        super(sysmemo,null);
        init();
    }

    private void init() {
        
        _csSessions = new ArrayList<>();
        _maxSessions = 32;
        _currentSessions = 0;
        setDelay(100);
        _trackOn = true;
        _estop = false;
        _pane = null;

        csTypes.add(Bundle.getMessage("cSDisabled"));
        csTypesTip.add(null);
        csTypes.add(Bundle.getMessage("csStandard"));
        csTypesTip.add("Based on CANCMD v3");
        setDummyType(1);
    }

    public int getNumberSessions(){
        return _currentSessions;
    }
    
    public void resetCS() {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            destroySession(_csSessions.get(i));
        }
        _csSessions = null;
        _csSessions = new ArrayList<>();
        _currentSessions = 0;
        if ( _pane  != null ){
            _pane.setNumSessions(_currentSessions);
        }
    }
    
    public void setDummyType(int type){
        _simType = type;
        if ( type == 0 ){
            resetCS();
        }
        log.info("Simulated Command Station: {}", csTypes.get(_simType) );
    }
    
    public int getDummyType() {
        return _simType;
    }
    
    public void setPane(CsPane pane) {
        _pane = pane;
    }
    
    // move to private in future
    public boolean getResponseRSTAT() {
        log.debug("estop {}",_estop);
        return false;
    } 
    
    private int getNextSession() {
        log.debug("max sessions {}",_maxSessions);
        ArrayList<Integer> nxtSessionList = new ArrayList<>();
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            nxtSessionList.add(_csSessions.get(i).getSessionNum());
        }
        for ( int i=1 ; (i < 257 ) ; i++) {
            if (!nxtSessionList.contains(i)){
                return i;
            }
        }            
        return 1000;
    }
    
    private void setTrackPower(Boolean trueorfalse) {
        _trackOn = trueorfalse;
        CanReply r = new CanReply(1); // num elements
        if (_trackOn) {
            r.setElement(0, CbusConstants.CBUS_TON);
        } else {
            r.setElement(0, CbusConstants.CBUS_TOF);
        }
        send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
    }

    protected void setEstop(Boolean estop) {
        _estop = estop;
        if (_estop) {
            CanReply r = new CanReply(1);
            r.setElement(0, CbusConstants.CBUS_ESTOP);
           send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
            for ( int i=0 ; (i < _csSessions.size()) ; i++) {
                _csSessions.get(i).setSpd(1);
            }
        }
    }        
    
    private int getExistingSession( int rcvdIntAddr, Boolean rcvdIsLong ) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( 
                ( _csSessions.get(i).getrcvdIntAddr() == rcvdIntAddr ) &&
                ( _csSessions.get(i).getisLong().equals(rcvdIsLong) )  && 
                ( _csSessions.get(i).getIsDispatched() == false ) 
            ) {
                return _csSessions.get(i).getSessionNum();
            }
        }
        return -1;
    }
    
    private void processrloc( int rcvdIntAddr, Boolean rcvdIsLong ) {
        
        // check for existing session
        int exSession = getExistingSession( rcvdIntAddr, rcvdIsLong );
        if ( exSession > -1 )  {
            int locoaddr = rcvdIntAddr;
            if (rcvdIsLong) {
                locoaddr = locoaddr | 0xC000;
            }
            CanReply r = new CanReply(4);
            r.setElement(0, CbusConstants.CBUS_ERR);
            r.setElement(1, (locoaddr / 256)); // addr hi
            r.setElement(2, locoaddr & 0xff);  // addr low
            r.setElement(3, 2);
            send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
            return;
        }

        int sessionid=getNextSession();
        
        CbusDummyCSSession session = new CbusDummyCSSession( this, sessionid,rcvdIntAddr, rcvdIsLong);
        _csSessions.add(session);
        _currentSessions++;
        if ( _pane  != null ){
            _pane.setNumSessions(_currentSessions);
        }
        session.sendPloc();
    }
    
    private void processQloc( int session ) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( _csSessions.get(i).getSessionNum() == session  && (
            !_csSessions.get(i).getIsDispatched() )) {
                _csSessions.get(i).sendPloc();
                return;
            }
        }
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, session);
        r.setElement(2, 0);
        r.setElement(3, 3);
        send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
    }
    
    private void processDspd ( int session, int speeddir) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( _csSessions.get(i).getSessionNum() == session  && (
            !_csSessions.get(i).getIsDispatched() ) ) {
                _csSessions.get(i).setSpd(speeddir);
                if ((speeddir & 0x7f) != 1) {
                    setEstop(false);
                }
                return;
            }
        }
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, session);
        r.setElement(2, 0);
        r.setElement(3, 3);
        send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
    }
    
    private void processDkeep ( int session ) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( ( _csSessions.get(i).getSessionNum() == session ) && (
            !_csSessions.get(i).getIsDispatched() )) {
                _csSessions.get(i).keepAlive();
                return;
            }
        }
        // send error if no session present
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, session);
        r.setElement(2, 0);
        r.setElement(3, 3);
        send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
    }

    protected void destroySession (CbusDummyCSSession session) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( _csSessions.get(i) == session ) {
                
                _csSessions.get(i).dispose();
                
                _csSessions.set(i, null);
                _csSessions.remove(i);
                _currentSessions--;
                if ( _pane  != null ){
                    _pane.setNumSessions(_currentSessions);
                }
                return;
            }
        }
        log.error("session not found to destroy");
    }

    private void processKloc ( int session ) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( _csSessions.get(i).getSessionNum() == session  && (
            !_csSessions.get(i).getIsDispatched() )) {
                destroySession(_csSessions.get(i) );
                return;
            }
        }
        // session not present error sent
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, session);
        r.setElement(2, 0);
        r.setElement(3, 3);
        send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startProcessFrame(AbstractMessage m) {
        // log.warn("dummy node canframe {}",m);
        if ( getDummyType() == 0 ) {
            return;
        }
        int opc = m.getElement(0);
        int session = m.getElement(1);
        switch (opc) {
            case CbusConstants.CBUS_RTON:
                setTrackPower(true);
                break;
            case CbusConstants.CBUS_RTOF:
                setTrackPower(false);
                break;
            case CbusConstants.CBUS_RESTP:
                setEstop(true);
                break;
            case CbusConstants.CBUS_RLOC:
                int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
                boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
                processrloc(rcvdIntAddr,rcvdIsLong);
                break;
            case CbusConstants.CBUS_QLOC:
                processQloc( session );
                break;
            case CbusConstants.CBUS_DSPD:
                processDspd( session, m.getElement(2) );
                break;
            case CbusConstants.CBUS_DKEEP:
                processDkeep( session );
                break;
            case CbusConstants.CBUS_KLOC:
                processKloc( session );
                break;
            default:
                break;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(){
        super.dispose();
        resetCS();
    }

    private static final Logger log = LoggerFactory.getLogger(CbusDummyCS.class);

}

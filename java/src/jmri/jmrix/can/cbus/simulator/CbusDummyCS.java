package jmri.jmrix.can.cbus.simulator;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.Timer;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;
import jmri.jmrix.can.cbus.swing.simulator.SimulatorPane.CsPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CbusDummyCS {
    
    private CbusSimulator _sim;
    private ArrayList<DummyCsSession> _csSessions;
    
    int _simType;
    int _maxSessions;
    int _currentSessions;
    int _networkDelay;
    Boolean _trackOn;
    Boolean _estop;
    CsPane _pane;
    
    private static int DEFAULT_CS_TIMEOUT = 60000; // ms
    private static int DEFAULT_SESSION_START_SPDDIR = 128;  // default DCC speed direction on start session
    
    public CbusDummyCS( int type, CbusSimulator sim ){
        _sim = sim;
        _simType = type;
        _csSessions = new ArrayList<DummyCsSession>();
        _maxSessions = 32;
        _currentSessions = 0;
        _networkDelay = CbusSimulator.DEFAULT_DELAY;
        _trackOn = true;
        _estop = false;
        _pane = null;
        
        if (_sim != null) {
            log.info("Simulated Command Station: {}", CbusSimulator.csTypes.get(_simType) );
        }
    }
    
    public void resetCS () {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            destroySession(_csSessions.get(i));
        }
        _csSessions = null;
        _csSessions = new ArrayList<DummyCsSession>();
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
    }
    
    protected int getDummyType() {
        return _simType;
    }
    
    public void setPane(CsPane pane) {
        _pane = pane;
    }
    
    protected Boolean getResponseRSTAT() {
        return false;
    }
    
    int getDelay() {
        return _networkDelay;
    }
    
    int getNextSession() {
        ArrayList<Integer> nxtSessionList = new ArrayList<Integer>();
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
    
    void setTrackPower(Boolean trueorfalse) {
        _trackOn = trueorfalse;
        CanReply r = new CanReply(1); // num elements
        if (_trackOn) {
            r.setElement(0, CbusConstants.CBUS_TON);
        } else {
            r.setElement(0, CbusConstants.CBUS_TOF);
        }
        _sim.sendReplyWithDelay(r,getDelay());
    }

    void setEstop() {
        _estop = true;
        CanReply r = new CanReply(1);
        r.setElement(0, CbusConstants.CBUS_ESTOP);
        _sim.sendReplyWithDelay(r,getDelay());
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            _csSessions.get(i).setSpd(1);
        }
    }        
    
    int getExistingSession( int rcvdIntAddr, Boolean rcvdIsLong ) {
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
    
    void processrloc( int rcvdIntAddr, Boolean rcvdIsLong ) {
        
        // check for existing session
        int exSession = getExistingSession( rcvdIntAddr, rcvdIsLong );
        if ( exSession > 0 )  {
            int locoaddr = rcvdIntAddr;
            if (rcvdIsLong) {
                locoaddr = locoaddr | 0xC000;
            }
            CanReply r = new CanReply(4);
            r.setElement(0, CbusConstants.CBUS_ERR);
            r.setElement(1, (locoaddr / 256)); // addr hi
            r.setElement(2, locoaddr & 0xff);  // addr low
            r.setElement(3, 2);
            _sim.sendReplyWithDelay( r,getDelay() );
            return;
        }

        int sessionid=getNextSession();
        
        DummyCsSession session = new DummyCsSession( this, sessionid,rcvdIntAddr, rcvdIsLong);
        _csSessions.add(session);
        _currentSessions++;
        if ( _pane  != null ){
            _pane.setNumSessions(_currentSessions);
        }
        session.sendPloc();
    }
    
    void processQloc( int session ) {
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
        _sim.sendReplyWithDelay( r,getDelay() );
    }
    
    void processDspd ( int session, int speeddir) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( _csSessions.get(i).getSessionNum() == session  && (
            !_csSessions.get(i).getIsDispatched() ) ) {
                _csSessions.get(i).setSpd(speeddir);
                return;
            }
        }
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, session);
        r.setElement(2, 0);
        r.setElement(3, 3);
        _sim.sendReplyWithDelay( r,getDelay() );
    }
    
    void processDkeep ( int session ) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( ( _csSessions.get(i).getSessionNum() == session ) && (
            !_csSessions.get(i).getIsDispatched() )) {
                _csSessions.get(i)._RefreshTimer.restart();
                return;
            }
        }            
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, session);
        r.setElement(2, 0);
        r.setElement(3, 3);
        _sim.sendReplyWithDelay( r,getDelay() );
    }

    void destroySession (DummyCsSession session) {
        for ( int i=0 ; (i < _csSessions.size()) ; i++) {
            if ( _csSessions.get(i) == session ) {
                _csSessions.get(i)._RefreshTimer.stop();
                _csSessions.get(i)._RefreshTimer = null;
                _csSessions.set(i, null);
                _csSessions.remove(i);
                _currentSessions--;
                if ( _pane  != null ){
                    _pane.setNumSessions(_currentSessions);
                }
                return;
            }
        }
        log.warn("session not found to destroy");
    }

    void processKloc ( int session ) {
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
        _sim.sendReplyWithDelay( r,getDelay() );
    }

    private class DummyCsSession {
        
        CbusDummyCS _cs;
        int _sessionID;
        int _Addr;
        Boolean _isLong;
        int _speedDirection;
        int _fa;
        int _fb;
        int _fc;
        Timer _RefreshTimer;
        Boolean _dispatched;
        
        // last timeout
        
        public DummyCsSession (CbusDummyCS cs, int sessionID, int rcvdIntAddr,Boolean rcvdIsLong ) {
            _cs = cs;
            _sessionID = sessionID;
            _Addr = rcvdIntAddr;
            _isLong = rcvdIsLong;
            _speedDirection = DEFAULT_SESSION_START_SPDDIR;
            _fa = 0;
            _fb = 0;
            _fc = 0;
            _dispatched=false;
            _RefreshTimer = new Timer(DEFAULT_CS_TIMEOUT, new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    csSessionTimeout();
                }
            });
            _RefreshTimer.setRepeats(false);     // refresh until stopped by dispose
            _RefreshTimer.start();
        }
        
        void csSessionTimeout() {
            log.info("Session {} Timeout",_sessionID);
            if (_cs.getDummyType() == 1 ) { // CANCMD v3
                _cs.destroySession(this);
            }
        }
        
        void sendPloc() {
            int locoaddr = _Addr;
            if (_isLong) {
                locoaddr = locoaddr | 0xC000;
            }
            CanReply r = new CanReply(7);
            r.setElement(0, CbusConstants.CBUS_PLOC);
            r.setElement(1, _sessionID);
            r.setElement(2, (locoaddr / 256)); // addr hi
            r.setElement(3, locoaddr & 0xff);  // addr low
            r.setElement(4, _speedDirection);
            r.setElement(5, _fa);
            r.setElement(6, _fb);
            r.setElement(7, _fc);
            _sim.sendReplyWithDelay( r,_cs.getDelay() );
        }
        
        int getSessionNum() {
            return _sessionID;
        }
        
        int getrcvdIntAddr() {
            return _Addr;
        }
        
        Boolean getisLong() {
            return _isLong;
        }
        
        void setSpd( int speeddir) {
            _speedDirection = speeddir;
            _RefreshTimer.restart();
            if (speeddir != 1) {
                _cs._estop = false;
            }
        }
        
        Boolean getIsDispatched() {
            return _dispatched;
        }
        
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusDummyCS.class);
}

package jmri.jmrix.can.cbus.simulator;

import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.Timer;
import jmri.DccLocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating a MERG CBUS Command Station Loco Session.
 *
 * @author Steve Young Copyright (C) 2018 2019
 * @see CbusDummyCS
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusDummyCSSession {
    
    private CbusDummyCS _cs;
    private int _sessionID;
    private int _Addr;
    private Boolean _isLong;
    private int _speedDirection;
    private int _fa;
    private int _fb;
    private int _fc;
    private Timer _RefreshTimer;
    private Boolean _dispatched;
    private SpeedStepMode _speedStepMode = SpeedStepMode.NMRA_DCC_128;
    
    public CbusDummyCSSession (CbusDummyCS cs, int sessionID, int rcvdIntAddr,Boolean rcvdIsLong ) {
        _cs = cs;
        _sessionID = sessionID;
        _Addr = rcvdIntAddr;
        _isLong = rcvdIsLong;
        init();
    }
    
    private void init(){
        _speedDirection = CbusDummyCS.DEFAULT_SESSION_START_SPDDIR;
        _fa = 0;
        _fb = 0;
        _fc = 0;
        _dispatched=false;
        _RefreshTimer = new Timer(CbusDummyCS.DEFAULT_CS_TIMEOUT, new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                csSessionTimeout();
            }
        });
        _RefreshTimer.setRepeats(false);
        _RefreshTimer.start();
    }
    
    protected void dispose(){
        _RefreshTimer.stop();
        _RefreshTimer = null;
    }
    
    private void csSessionTimeout() {
        log.info("Session {} Timeout",_sessionID);
        if (_cs.getDummyType() == 1 ) { // CANCMD v3
            _cs.destroySession(this);
        }
    }
    
    protected void keepAlive() {
        _RefreshTimer.restart();
    }
    
    protected void sendPloc() {
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
        _cs.send.sendWithDelay(r,_cs.getSendIn(),_cs.getSendOut(),_cs.getDelay());
    }
    
    protected int getSessionNum() {
        return _sessionID;
    }
    
    protected int getrcvdIntAddr() {
        return _Addr;
    }
    
    protected Boolean getisLong() {
        return _isLong;
    }
    
    protected void setSpd( int speeddir) {
        _speedDirection = speeddir;
        _RefreshTimer.restart();
        _cs.sessionChanged(this);
    }

    /**
     * Stop the loco at once, as for a request for an emergency stop of every
     * loco (RESTP): the session's speed becomes the emergency stop value of
     * its speed step mode, 2 in 28 step mode and 1 otherwise, with the
     * direction bit clear.
     */
    protected void emergencyStop() {
        setSpd(_speedStepMode == SpeedStepMode.NMRA_DCC_28 ? 2 : 1);
    }

    /**
     * Set the speed step mode, as a request to set it (STMOD) does.
     *
     * @param mode the mode
     */
    protected void setSpeedStepMode(@Nonnull SpeedStepMode mode) {
        _speedStepMode = mode;
        _RefreshTimer.restart();
        _cs.sessionChanged(this);
    }

    /**
     * @return the loco address the session is for
     * @since 5.19.1
     */
    @Nonnull
    public DccLocoAddress getLocoAddress() {
        return new DccLocoAddress(_Addr, _isLong);
    }

    /**
     * The session's speed and direction, as the last request to set them
     * (DSPD) gave it: the direction in bit 7 (set for forward) and the speed
     * in bits 0 to 6, in the session's speed step mode.
     *
     * @return the speed and direction byte
     * @since 5.19.1
     */
    public int getSpeedDirection() {
        return _speedDirection;
    }

    /**
     * @return the session's speed step mode: 128 steps unless a request to
     *         set the mode (STMOD) has said otherwise
     * @since 5.19.1
     */
    @Nonnull
    public SpeedStepMode getSpeedStepMode() {
        return _speedStepMode;
    }

    /**
     * @return the session number
     * @since 5.19.1
     */
    public int getSessionNumber() {
        return _sessionID;
    }
    
    protected Boolean getIsDispatched() {
        return _dispatched;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusDummyCSSession.class);

}

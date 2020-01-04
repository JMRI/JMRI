package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.Timer;
import jmri.jmrix.can.cbus.CbusEvent;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to represent a request event in the MERG CBUS event request monitor table
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventRequestMonitorEvent extends CbusEvent {
    
    // last feedback state
    public enum FbState{
        LfbFinding, LfbGood, LfbBad, LfbUnknown;
    }
    
    private ActionListener eventFeedbackListener;
    private final CbusEventRequestDataModel _model;
    private Date _timestamp;
    private int _feedbackTimeout;
    private int _feedbackTotReqd;
    private int _extraEvent;
    private int _extraNode;
    private int _feedbackOutstanding;
    private FbState _lfb;
    private Timer _timer;
    
    public CbusEventRequestMonitorEvent( int nn, int en, 
        EvState state, Date timestamp, int feedbackTimeout, int feedbackTotReqd,
        CbusEventRequestDataModel model ){
        
        super(nn,en);
        _state = state;
        _model = model;
        _timestamp = timestamp;
        _feedbackTimeout = feedbackTimeout;
        _feedbackTotReqd = feedbackTotReqd;
        _extraEvent = 0;
        _extraNode = 0;
        _feedbackOutstanding = 0;
        _lfb = FbState.LfbUnknown;
        _timer = null;
    }
    
    public Boolean matchesFeedback(int nn, int en) {
        return (nn == _extraNode) && (en == _extraEvent);
    }
    
    protected Date getDate(){
        return _timestamp;
    }
    
    protected void setDate(Date newval) {
        _timestamp = newval;
    }

    protected void setFeedbackTimeout(int newval) {
        _feedbackTimeout = newval;
    }
    
    protected int getFeedbackTimeout() {
        return _feedbackTimeout;
    }
    
    protected int getFeedbackTotReqd() {
        return _feedbackTotReqd;
    }
    
    protected void setFeedbackTotReqd( int newval ) {
        _feedbackTotReqd = newval;
    }
    
    protected int getExtraEvent(){
        return _extraEvent;
    }

    protected int getExtraNode(){
        return _extraNode;
    }
    
    protected void setExtraEvent( int newval ) {
        _extraEvent = newval;
    }

    protected void setExtraNode( int newval ) {
        _extraNode = newval;
    }
    
    protected void setFeedbackOutstanding( int newval ) {
        _feedbackOutstanding = newval;
    }

    protected int getFeedbackOutstanding() {
        return _feedbackOutstanding;
    }
    
    protected FbState getLastFb() {
        return _lfb;
    }

    protected void setLastFb( FbState newval ) {
        _lfb = newval;
    }
    
    protected void setRequestReceived(){
        _feedbackOutstanding = _feedbackTotReqd;
        
       // getFeedbackOutstanding
        _model.setValueAt(FbState.LfbFinding, _model.eventRow(getNn(),getEn()), 
            CbusEventRequestDataModel.LASTFEEDBACK_COLUMN);
        
        _model.setValueAt(getFeedbackOutstanding(), _model.eventRow(getNn(),getEn()), 
            CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN);
        if ( getFeedbackTotReqd() > 0 ) {
            startTheTimer();
        }
    }
    
    private void startTheTimer(){
        
        final String _evName = this.toString();
        eventFeedbackListener = (ActionEvent e) -> {
            _model.setValueAt(0, _model.eventRow( getNn(),getEn() ), 
                    CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN);
            _model.setValueAt(CbusEventRequestMonitorEvent.FbState.LfbBad, _model.eventRow(getNn(),getEn()),
                    CbusEventRequestDataModel.LASTFEEDBACK_COLUMN);
            _model.addToLog(3, Bundle.getMessage("FeedBackNotOK", _evName ) );
            _timer.stop();
            _timer = null;
        };
        _timer = new Timer( getFeedbackTimeout(), eventFeedbackListener);
        _timer.setRepeats( false );
        _timer.start();        
    }

    protected void stopTheTimer() {
        if ( _timer !=null ) {
            _timer.stop();
            _timer=null;
        }
        eventFeedbackListener=null;
    }
    
    protected void setResponseReceived(){
        if ( _feedbackOutstanding < 0 ) {
            return;
        }
        _feedbackOutstanding--;
        
        if ( _feedbackOutstanding > 0 ) {
            _model.setValueAt(_feedbackOutstanding, _model.eventRow(getNn(),getEn()), 
                CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN);
        }
        if ( _feedbackOutstanding == 0 ) {
            _model.setValueAt(_feedbackOutstanding, _model.eventRow(getNn(),getEn()), 
                CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN);
            if ( _timer != null ) {
                stopTheTimer();
                _model.setValueAt(FbState.LfbGood, _model.eventRow(getNn(),getEn()),
                    CbusEventRequestDataModel.LASTFEEDBACK_COLUMN);
                _model.addToLog(2,Bundle.getMessage("FeedBackOK",this.toString() ) );
            }
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventRequestMonitorEvent.class);

}

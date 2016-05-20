// CbusEventFilterAction.java

package jmri.jmrix.can.cbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import java.awt.Color;

/**
 * Class to implement filtering of CBUS events.
 *
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision$
 */
public class CbusEventFilter {
    
    /** Creates a new instance of CbusEventFilter */
    public CbusEventFilter() {
    }
    
    /**
     * Filter an event, based on previous settings.
     * @param m CanMessage to filter.
     * @return true if event matches
     */
    public boolean filter(CanMessage m) {
        log.debug("Filter on Message: "+m.toString());
        if (!CbusMessage.isEvent(m)) { return false; }
        if (_nnEnabled && (CbusMessage.getNodeNumber(m) != _nn)) { return false; }
        if (_evEnabled && (CbusMessage.getEvent(m) != _ev)) { return false; }
        if ((_type != CbusConstants.EVENT_EITHER)
                && (_type != CbusMessage.getEventType(m))) { return false; }
        log.debug("Message matches filter");
        return true;
    }
    
    public boolean filter(CanReply r) {
        log.debug("Filter on Reply: "+r.toString());
        if (!CbusMessage.isEvent(r)) { return false; }
        if (_nnEnabled && (CbusMessage.getNodeNumber(r) != _nn)) { return false; }
        if (_evEnabled && (CbusMessage.getEvent(r) != _ev)) { return false; }
        if ((_type != CbusConstants.EVENT_EITHER)
                && (_type != CbusMessage.getEventType(r))) { return false; }
        log.debug("Reply matches filter");
        return true;
    }
    
    // control terms to be included in filter
    
    /**
     * Set whether NN (Node Number) will be included in filter.
     */
    public void setNnEnable(boolean b) { 
        log.debug("Filter NN enable: "+b);
        _nnEnabled = b;
    }
    /**
     * Set whether Ev (event number) will be included in filter.
     */
    public void setEvEnable(boolean b) { 
        log.debug("Filter EV enable: "+b);
        _evEnabled = b;
    }
    
    // filter values
    public void setNn(int n) { 
        log.debug("Filter NN set: "+n);
        _nn = n;
    }
    public void setEv(int n) {
        log.debug("Filter EV set: "+n);
        _ev = n;
    }
    
    /**
     * Set value of type to match. Type is the ON, OFF, etc. value
     * in the CBUS frame. See {@link CbusConstants} for
     * values; CbusConstants.EVENT_EITHER matches either ON or OFF.
     */
    public void setType(int n) { 
        log.debug("Filter EV type set: "+n);
        _type = n;
    }

    public void setColor(Color c) { _color = c; }
    public Color getColor() { return _color; }
    
    private boolean _nnEnabled;
    private boolean _evEnabled;
    private int _nn;
    private int _ev;
    private int _type;
    private Color _color;
    
    static Logger log = LoggerFactory.getLogger(CbusEventFilter.class.getName());
}

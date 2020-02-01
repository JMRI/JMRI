package jmri.jmrix.can.cbus;

import java.awt.Color;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to implement highlighting of CBUS events.
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class CbusEventHighlighter {
    
    private boolean _nnEnabled;
    private boolean _evEnabled;
    private int _nn;
    private int _ev;
    private int _type;
    private int _dir;
    private Color _color;
    
    /**
     * Creates a new instance of CbusEventHighlighter
     */
    public CbusEventHighlighter() {
    }

    /**
     * highlight an event, based on previous settings.
     *
     * @param m CanMessage to highlight.
     * @return true if event matches
     */
    public boolean highlight(CanMessage m) {
        return !((doNotHighlight(m)) 
            || ((_dir != CbusConstants.EVENT_DIR_EITHER)
                && (_dir != CbusConstants.EVENT_DIR_OUT)));
    }
    
    /**
     * 
     * @param m CanFrame to test against
     * @return False if not an Event, or Node, or Event or On / Off does not match
     */
    private boolean doNotHighlight(AbstractMessage m) {
        return ((!CbusMessage.isEvent(m)) 
            || (_nnEnabled && (CbusMessage.getNodeNumber( m) != _nn))
            || (_evEnabled && (CbusMessage.getEvent(m) != _ev))
            || ((_type != CbusConstants.EVENT_EITHER)
                && (_type != CbusMessage.getEventType(m))));
    }

    public boolean highlight(CanReply r) {
        return !((doNotHighlight(r)) 
            || ((_dir != CbusConstants.EVENT_DIR_EITHER)
                && (_dir != CbusConstants.EVENT_DIR_IN)));
    }

    // control terms to be included in highlight
    
    /**
     * Set whether NN (Node Number) will be included in highlight.
     * @param b True to highlight a Node Number
     */
    public void setNnEnable(boolean b) {
        _nnEnabled = b;
    }

    /**
     * Set whether Ev (event number) will be included in highlight.
     * @param b True to highlight an Event Number
     */
    public void setEvEnable(boolean b) {
        _evEnabled = b;
    }

    /**
     * Set a Node Number to highlight.
     * @param n Node Number
     */
    public void setNn(int n) {
        _nn = n;
    }

    /**
     * Set an Event Number to highlight.
     * @param n Event Number
     */
    public void setEv(int n) {
        _ev = n;
    }

    /**
     * Set value of type to match.Type is the ON, OFF, etc. value in the CBUS
     * frame.
     * CbusConstants.EVENT_EITHER matches either ON or OFF.
     * @param n See {@link CbusConstants} for values
     */
    public void setType(int n) {
        _type = n;
    }

    /**
     * Set value of direction to match.
     * @param n EVENT_DIR_UNSET EVENT_DIR_IN, EVENT_DIR_OUT, EVENT_EITHER_DIR EVENT_DIR_EITHER
     */
    public void setDir(int n) {
        _dir = n;
    }

    /**
     * Set value of Colour
     * @param c Colour to use
     */
    public void setColor(Color c) {
        _color = c;
    }

    /**
     * Get value of Colour to highlight.
     * @return Colour to use
     */
    public Color getColor() {
        return _color;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventHighlighter.class);
}

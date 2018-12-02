package jmri.jmrix.can.cbus;

import java.awt.Color;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.debug("Highlight on Message: " + m.toString());
        if (!CbusMessage.isEvent(m)) {
            return false;
        }
        if (_nnEnabled && (CbusMessage.getNodeNumber(m) != _nn)) {
            return false;
        }
        if (_evEnabled && (CbusMessage.getEvent(m) != _ev)) {
            return false;
        }
        if ((_type != CbusConstants.EVENT_EITHER)
                && (_type != CbusMessage.getEventType(m))) {
            return false;
        }
        if ((_dir != CbusConstants.EVENT_EITHER_DIR)
                && (_dir != CbusConstants.EVENT_OUT)) {
            // log.debug("does not match event dir {}",_dir);
            return false;
        }
        // log.debug("Message matches highlight with direction {}",_dir);
        return true;
    }

    public boolean highlight(CanReply r) {
        log.debug("highlight on Reply: " + r.toString());
        if (!CbusMessage.isEvent(r)) {
            return false;
        }
        if (_nnEnabled && (CbusMessage.getNodeNumber(r) != _nn)) {
            return false;
        }
        if (_evEnabled && (CbusMessage.getEvent(r) != _ev)) {
            return false;
        }
        if ((_type != CbusConstants.EVENT_EITHER)
                && (_type != CbusMessage.getEventType(r))) {
            return false;
        }
        if ((_dir != CbusConstants.EVENT_EITHER_DIR)
                && (_dir != CbusConstants.EVENT_IN)) {
            // log.debug("does not match event dir {}",_dir);
            return false;
        }
        // log.debug("Reply matches highlight with event dir {}",_dir);
        return true;
    }

    // control terms to be included in highlight
    /**
     * Set whether NN (Node Number) will be included in highlight.
     */
    public void setNnEnable(boolean b) {
        log.debug("highlight NN enable: " + b);
        _nnEnabled = b;
    }

    /**
     * Set whether Ev (event number) will be included in highlight.
     */
    public void setEvEnable(boolean b) {
        log.debug("highlight EV enable: " + b);
        _evEnabled = b;
    }

    // highlight values
    public void setNn(int n) {
        log.debug("highlight NN set: " + n);
        _nn = n;
    }

    public void setEv(int n) {
        log.debug("highlight EV set: " + n);
        _ev = n;
    }

    /**
     * Set value of type to match. Type is the ON, OFF, etc. value in the CBUS
     * frame. See {@link CbusConstants} for values; CbusConstants.EVENT_EITHER
     * matches either ON or OFF.
     */
    public void setType(int n) {
        log.debug("highlight EV type set: " + n);
        _type = n;
    }

    /**
     * Set value of direction to match. Type is the EVENT_IN, EVENT_OUT, EVENT_EITHER_DIR
     */
    public void setDir(int n) {
        log.debug("highlight EV direction set: " + n);
        _dir = n;
    }

    public void setColor(Color c) {
        _color = c;
    }

    public Color getColor() {
        return _color;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventHighlighter.class);
}

package jmri.jmrix.can.cbus;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class CbusEvent {
    
    public int _nn;
    public int _en;
    
    public CbusEvent( int _nn, int _en){
        this._nn = _nn;
        this._en = _en;
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusEvent.class);
}

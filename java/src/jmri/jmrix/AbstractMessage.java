package jmri.jmrix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for various message implementations used by the 
 * various abstract TrafficController classes.
 *
 * @author Bob Jacobsen Copyright 2007, 2008
 */
public abstract class AbstractMessage implements Message {

    /**
     * Creates a new instance of AbstractMessage
     */
    public AbstractMessage() {
    }

    public AbstractMessage(int n) {
        if (n < 1) {
            log.error("invalid length in call to ctor");
        }
        _nDataChars = n;
        _dataChars = new int[n];
    }

    public AbstractMessage(String s) {
        this(s.length());
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = s.charAt(i);
        }
    }

    @SuppressWarnings("null")
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH",
            justification = "we want to force an exception")
    public AbstractMessage(AbstractMessage m) {
        if (m == null) {
            log.error("copy ctor of null message throws exception");
        }
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
    }

    @Override
    public int getElement(int n) {
        return _dataChars[n];
    }

    // accessors to the bulk data
    @Override
    public int getNumDataElements() {
        return _nDataChars;
    }

    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = v;
    }

    // display format
    protected int[] _dataChars = null;

    // display format
    // contents (private)
    protected int _nDataChars = 0;


    /*
     * Equals operator compares only base data
     */
    @Override
    public boolean equals(Object obj){
        if (obj == null) return false; // basic contract
        if( this.getClass() != obj.getClass() ) {
            return false;
        }
        AbstractMessage m = (AbstractMessage) obj;
        if(this.getNumDataElements() != m.getNumDataElements()){
            return false;
        }
        for(int i = 0;i<this.getNumDataElements();i++){
            if(this.getElement(i)!=m.getElement(i)){
                return false;
            }
        }
        return true;
    }

    /**
     * Hash code from base data
     */
    @Override
    public int hashCode() {
        int retval = 0;
        for(int i = 0;i<this.getNumDataElements();i++){ 
            retval += this.getElement(i);
        }
        return retval;
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMessage.class.getName());
}

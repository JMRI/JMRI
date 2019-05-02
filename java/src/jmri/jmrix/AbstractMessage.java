package jmri.jmrix;

import java.util.Objects;
import javax.annotation.Nonnull;
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
     * Create a new instance of AbstractMessage.
     */
    public AbstractMessage() {
    }

    /**
     * Create a new instance of AbstractMessage of a given byte size.
     *
     * @param n number of elements
     */
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

    public AbstractMessage(@Nonnull AbstractMessage m) {
        Objects.requireNonNull(m, "Unable to create message by copying null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        System.arraycopy(m._dataChars, 0, _dataChars, 0, _nDataChars);
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


    /**
     * Equals operator compares only base data.
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
            if(this.getElement(i) != m.getElement(i)){
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

    private final static Logger log = LoggerFactory.getLogger(AbstractMessage.class);

}

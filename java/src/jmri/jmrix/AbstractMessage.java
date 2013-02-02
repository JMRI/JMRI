// AbstractMessage.java

package jmri.jmrix;

import org.apache.log4j.Logger;


/**
 * Add description of class here.
 *
 * @author Bob Jacobsen  Copyright 2007, 2008
 * @version   $Revision$
 */
public abstract class AbstractMessage implements Message {
    
    /** Creates a new instance of AbstractMessage */
    public AbstractMessage() {
    }

    public AbstractMessage(int n) {
        if (n<1)
            log.error("invalid length in call to ctor");
        _nDataChars = n;
        _dataChars = new int[n];
    }

    public AbstractMessage(String s) {
        this(s.length());
        for (int i = 0; i<_nDataChars; i++)
            _dataChars[i] = s.charAt(i);
    }

    @SuppressWarnings("null")
	public AbstractMessage(AbstractMessage m) {
    	if (m == null)
    		log.error("copy ctor of null message throws exception");
    	_nDataChars = m._nDataChars;
    	_dataChars = new int[_nDataChars];
    	for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    public int getElement(int n) {        return _dataChars[n];}


    // accessors to the bulk data
    public int getNumDataElements() {        return _nDataChars;}

    public void setElement(int n, int v) {         _dataChars[n] = v;  }


    // display format

    protected int[] _dataChars = null;


    // display format

    // contents (private)
    protected int _nDataChars = 0;

    private static Logger log = Logger.getLogger(AbstractMessage.class.getName());

}

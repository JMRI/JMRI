// AbstractMessage.java

package jmri.jmrix;

import jmri.util.StringUtil;

/**
 * Add description of class here.
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version   $Revision: 1.2 $
 */
public abstract class AbstractMessage implements Message {
    
    /** Creates a new instance of AbstractMessage */
    public AbstractMessage() {
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

    private static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMessage.class.getName());

}

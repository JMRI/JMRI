// AbstractSignalHead.java

package jmri;

 /**
 * Abstract class providing the basic logic of the SignalHead interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version     $Revision: 1.7 $
 */
public abstract class AbstractSignalHead extends AbstractNamedBean
    implements SignalHead, java.io.Serializable {

    public AbstractSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalHead(String systemName) {
        super(systemName);
    }

    protected int mAppearance = DARK;
    public int getAppearance() { return mAppearance; }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //						Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state

}

/* @(#)AbstractSignalHead.java */

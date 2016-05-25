package jmri.layout;

/**
 * @author Alex Shepherd Copyright (c) 2002
 * @deprecated 4.3.5
 */
@Deprecated
public class LayoutElement extends javax.swing.tree.DefaultMutableTreeNode implements LayoutEventInterface {

    /**
     *
     */
    private static final long serialVersionUID = 2020897539141972121L;
    private LayoutAddress mAddress = null;
    private LayoutEventData mData = null;
    private LayoutEventSource mListeners = null;

    public LayoutElement(LayoutAddress pAddress) {
        // If we only have an Address then make that the name also
        mAddress = pAddress;
        if (pAddress != null) {
            userObject = pAddress.toString();
        }
    }

    public LayoutElement(String pName, LayoutAddress pAddress) {
        userObject = pName;
        mAddress = pAddress;
    }

    public synchronized LayoutEventData getData() {
        return mData;
    }

    public synchronized void setData(LayoutEventData pData) {
        mData = pData;

        LayoutElement vElement = this;
        while (vElement != null) {
            vElement.message(mData);
            vElement = (LayoutElement) vElement.getParent();
        }
    }

    // There is only a getAddress and no setAddress because the mAddress is the key
    // for the mChildren Map so it the depends upon it being immutable. To allow
    // an Address to change, would require the entry in the hashmap to be fixed
    // and that would require this element to know about its parent, to go ask it
    // to fix the problem and that is not a good thing IMHO...
    public LayoutAddress getAddress() {
        return mAddress;
    }

    // If there is no user defined names specified then return the Address as a string
    public String getName() {
        return (userObject != null) ? (String) userObject : mAddress.toString();
    }

    public void setName(String pName) {
        userObject = pName;
    }

    public synchronized void addEventListener(LayoutEventListener pListener) {
        if (mListeners == null) {
            mListeners = new LayoutEventSource();
        }
        mListeners.addEventListener(pListener);
    }

    public synchronized void removeEventListener(LayoutEventListener pListener) {
        if (mListeners != null) {
            mListeners.removeEventListener(pListener);
        }
    }

    protected synchronized void message(LayoutEventData pLayoutEvent) {
        if (mListeners != null) {
            mListeners.message(pLayoutEvent);
        }
    }

}

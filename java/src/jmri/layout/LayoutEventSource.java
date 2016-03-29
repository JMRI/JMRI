package jmri.layout;

import java.util.ArrayList;

/**
 * @author Alex Shepherd Copyright (c) 2002
 * @deprecated 4.3.5
 */
@Deprecated
public class LayoutEventSource implements LayoutEventInterface {

    private ArrayList<LayoutEventListener> mListeners = new ArrayList<LayoutEventListener>();

    public LayoutEventSource() {
    }

    public synchronized void addEventListener(LayoutEventListener pListener) {
        if (!mListeners.contains(pListener)) {
            mListeners.add(pListener);
        }
    }

    public synchronized void removeEventListener(LayoutEventListener pListener) {
        if (mListeners != null) {
            mListeners.remove(pListener);
        }
    }

    protected void message(LayoutEventData pLayoutEvent) {
        Object vListenersArray[] = null;

        synchronized (this) {
            if (mListeners.size() > 0) {
                vListenersArray = mListeners.toArray();
            }
        }
        if (vListenersArray != null) {
            for (int vListenerIndex = 0; vListenerIndex < vListenersArray.length; vListenerIndex++) {
                ((LayoutEventListener) vListenersArray[vListenerIndex]).message(pLayoutEvent);
            }
        }
    }
}

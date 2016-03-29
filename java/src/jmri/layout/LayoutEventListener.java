package jmri.layout;

import java.util.EventListener;

/**
 * @author Alex Shepherd Copyright (c) 2002
 * @see jmri.layout.LayoutEventInterface
 * @deprecated 4.3.5
 */
@Deprecated
public interface LayoutEventListener extends EventListener {

    public void message(LayoutEventData pLayoutEvent);
}

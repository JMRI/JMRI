package jmri.layout;

/**
 * @author Alex Shepherd Copyright (c) 2002
 * @see jmri.layout.LayoutEventListener
 * @deprecated 4.3.5
 */
@Deprecated
public interface LayoutEventInterface {

    public void addEventListener(LayoutEventListener pListener);

    public void removeEventListener(LayoutEventListener pListener);
}

package jmri.layout;

/**
 * @author   Alex Shepherd   Copyright (c) 2002
 * @version $Revision$
 * @see jmri.layout.LayoutEventListener
 */

public interface LayoutEventInterface
{
    public void addEventListener( LayoutEventListener pListener ) ;

    public void removeEventListener( LayoutEventListener pListener ) ;
}
package jmri.layout;

import com.sun.java.util.collections.ArrayList;

/**
 * @author   Alex Shepherd Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class LayoutEventSource implements LayoutEventInterface
{
    private ArrayList   mListeners = new ArrayList() ;

    public LayoutEventSource()
    {
    }

    public synchronized void addEventListener( LayoutEventListener pListener )
    {
        if( !mListeners.contains( pListener ) )
            mListeners.add( pListener ) ;
    }

    public synchronized void removeEventListener( LayoutEventListener pListener )
    {
        if( mListeners != null )
            mListeners.remove( pListener ) ;
    }

    protected void message( LayoutEventData pLayoutEvent )
    {
        Object vListenersArray[] = null ;

        synchronized( this )
        {
            if( mListeners.size() > 0 )
                vListenersArray = mListeners.toArray() ;
        }
        if( vListenersArray != null )
            for( int vListenerIndex = 0; vListenerIndex < vListenersArray.length; vListenerIndex++ )
                ((LayoutEventListener)vListenersArray[ vListenerIndex ]).message( pLayoutEvent ) ;
    }
}
package jmri.layout;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Revision: 1.1 $
 */

import java.util.TreeMap;

public class LayoutElement extends LayoutEventSource
{
    private String  mName ;
    private String  mAddress ;
    private TreeMap mChildren = new TreeMap() ;

    private LayoutEventData mData = null ;

    public LayoutElement( String pAddress )
    {
            // If we only have an Address then make that the name also
        mName = pAddress ;
        mAddress = pAddress ;
    }

    public LayoutElement( String pName, String pAddress )
    {
        mName = pName ;
        mAddress = pAddress ;
    }

    public synchronized LayoutEventData getData()
    {
        return mData ;
    }

    public synchronized void setData( LayoutEventData pData )
    {
        mData = pData ;
        message( mData ) ;
    }

    public boolean hasChildren() { return mChildren.size() > 0 ; }

        // There is only a getAddress and no setAddress because the mAddress is the key
        // for the mChildren Map so it the depends upon it being immutable. To allow
        // an Address to change, would require the entry in the hashmap to be fixed
        // and that would require this element to know about its parent, to go ask it
        // to fix the problem and that is not a good thing IMHO...
    public String getAddress() { return mAddress ; }

    public String getName() { return mName ; }
    public void setName( String pName ) { mName = pName ; }

    public LayoutElement getChild( String pAddress )
    {
        synchronized( mChildren )
        {
            return (LayoutElement) mChildren.get( pAddress ) ;
        }
    }

    public boolean addChild( LayoutElement pLayoutElement )
    {
        System.out.println( "Add: " + pLayoutElement.getAddress() );

        synchronized( mChildren )
        {
            if( mChildren.containsKey( pLayoutElement.mAddress ) )
                return false ;

            mChildren.put( pLayoutElement.mAddress, pLayoutElement ) ;
            return true ;
        }
    }

    public boolean removeChild( LayoutElement pLayoutElement )
    {
        synchronized( mChildren )
        {
            return mChildren.remove( pLayoutElement.mAddress ) != null ;
        }
    }
}
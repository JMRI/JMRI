package jmri.layout;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Alex Shepherd
 * @version $Revision: 1.1 $
 */

import java.lang.Integer ;

public class Layout implements LayoutEventListener, LayoutEventInterface
{
    private LayoutElement mRootElement = null ;

    public Layout( String pHostname )
    {
        mRootElement = new LayoutElement( "LocalHost" ) ;
    }

    public LayoutElement getLayoutTree() { return mRootElement ; }

    public void addEventListener( LayoutEventListener pListener )
    {
        mRootElement.addEventListener( pListener );
    }

    public void removeEventListener( LayoutEventListener pListener )
    {
        mRootElement.removeEventListener( pListener );
    }

    public void message( LayoutEventData pLayoutEvent )
    {
        String vNodeAddress = pLayoutEvent.getLayoutName() ;
        LayoutElement vLayoutNode = mRootElement.getChild( vNodeAddress ) ;
        if( vLayoutNode == null )
        {
            vLayoutNode = new LayoutElement( vNodeAddress ) ;
            mRootElement.addChild( vLayoutNode ) ;
        }

        vNodeAddress = pLayoutEvent.getTypeDescription() ;
        LayoutElement vTypeNode = vLayoutNode.getChild( vNodeAddress ) ;
        if( vTypeNode == null )
        {
            vTypeNode = new LayoutElement( vNodeAddress ) ;
            vLayoutNode.addChild( vTypeNode ) ;
        }

        vNodeAddress = pLayoutEvent.getAddress() ;
        LayoutElement vNode = vTypeNode.getChild( vNodeAddress ) ;
        if( vNode == null )
        {
            vNode = new LayoutElement( vNodeAddress ) ;
            vTypeNode.addChild( vNode ) ;
        }

        vNode.setData( pLayoutEvent );
        vTypeNode.message( pLayoutEvent );
        vLayoutNode.message( pLayoutEvent );
        mRootElement.message( pLayoutEvent);
    }

    private void log( String pMessage )
    {
        System.out.println( pMessage );
    }
}
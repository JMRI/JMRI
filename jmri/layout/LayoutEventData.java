package jmri.layout;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Revision: 1.2 $
 */

import java.util.Date ;

public class LayoutEventData
{
    private Date            mTimeStamp ;
    private LayoutAddress   mLayoutAddress ;
    private String          mState ;

    LayoutEventData( String pLayoutName, int pType, int pOffset, String pState )
    {
        mTimeStamp = new Date() ;
        mState = pState ;
        mLayoutAddress = new LayoutAddress( pLayoutName, pType, pOffset ) ;
    }

    public Date getTimeStamp() { return mTimeStamp ; }

    public LayoutAddress getLayoutAddress() { return mLayoutAddress ; }

    public String getState() { return mState ; }

    public String toString()
    {
        return "TimeStamp: " + mTimeStamp.toString() +
            " Address: " + mLayoutAddress.toString() +
            " State: " + getState() ;
    }
}
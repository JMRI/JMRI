package jmri.layout;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Revision: 1.1 $
 */

import java.util.Date ;

public class LayoutEventData
{
    public static final int ELEMENT_TYPE_SENSOR = 0 ;
    public static final int ELEMENT_TYPE_TURNOUT = 1 ;
    public static final int ELEMENT_TYPE_LOCO = 2 ;
    public static final int ELEMENT_TYPE_MISC = 3 ;

    private String[] mTypeDescriptions = { "Sensor", "Turnout", "Loco", "Misc" } ;
    private Date    mTimeStamp ;
    private String  mLayoutName ;
    private int     mType ;
    private String  mAddress ;
    private String  mState ;

    LayoutEventData( String pLayoutName, int pType, String pAddress, String pState )
    {
        mTimeStamp = new Date() ;
        mLayoutName = pLayoutName ;
        mType = pType ;
        mAddress = pAddress ;
        mState = pState ;
    }

    public Date getTimeStamp() { return mTimeStamp ; }

    public String getLayoutName() { return mLayoutName ; }

    public int getType() { return mType ; }

    public String getTypeDescription() { return mTypeDescriptions[ mType ] ; }

    public String getTypeDescription( int pType ) { return mTypeDescriptions[ pType ] ; }

    public String getAddress() { return mAddress ; }

    public String getState() { return mState ; }

    public String toString()
    {
        return "TimeStamp: " + mTimeStamp.toString() +
            " Layout: " + mLayoutName +
            " Type: " + getTypeDescription( mType ) +
            " Address: " + mAddress +
            " State: " + getState() ;
    }
}
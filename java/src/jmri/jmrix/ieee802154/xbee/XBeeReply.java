// XBeeReply.java

package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.XBeeResponse;

/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 * @version     $Revision$
 */
public class XBeeReply extends jmri.jmrix.ieee802154.IEEE802154Reply {

    XBeeTrafficController tc = null;

    XBeeResponse xbresponse = null;
	
    // create a new one
    public  XBeeReply(XBeeTrafficController tc) {
        super(tc);
        this.tc = tc;
        setBinary(true);
    }

    public XBeeReply(XBeeTrafficController tc, String s) {
        super(tc,s);
        this.tc = tc;
        setBinary(true);
        xbresponse=new com.rapplogic.xbee.api.GenericResponse();
        byte ba[]=jmri.util.StringUtil.bytesFromHexString(s);
        int ia[]=new int[ba.length];
        for(int i=0;i<ba.length;i++) ia[i]=(int)ba[i];
        xbresponse.setRawPacketBytes(ia);
    }

    public XBeeReply(XBeeTrafficController tc, XBeeReply l) {
        super(tc,l);
        this.tc = tc;
        xbresponse = l.xbresponse;
        _dataChars=xbresponse.getRawPacketBytes();
        setBinary(true);
    }

    public XBeeReply(XBeeTrafficController tc, XBeeResponse xbr) {
        super(tc); 
        this.tc = tc;
        xbresponse = xbr;
        _dataChars=xbr.getRawPacketBytes();
        setBinary(true);
    }

    public String toMonitorString() { return xbresponse.toString(); }

    public String toString() { 
           String s="";
           int packet[]=xbresponse.getProcessedPacketBytes();
	   for(int i=0;i<packet.length;i++) 
               s=s+jmri.util.StringUtil.twoHexFromInt(packet[i]);
           return s; }

    public XBeeResponse getXBeeResponse() { return xbresponse; }
    public void setXBeeResponse(XBeeResponse xbr) { xbresponse = xbr; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XBeeReply.class.getName());

}

/* @(#)XBeeReply.java */

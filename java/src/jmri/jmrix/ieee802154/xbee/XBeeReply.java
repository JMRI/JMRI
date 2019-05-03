package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.packet.UnknownXBeePacket;
import com.digi.xbee.api.packet.XBeePacket;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011 Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 */
public class XBeeReply extends jmri.jmrix.ieee802154.IEEE802154Reply {

    XBeePacket xbresponse = null;

    // create a new one
    public XBeeReply() {
        super();
        setBinary(true);
    }

    public XBeeReply(String s) {
        super(s);
        setBinary(true);
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        for(int i=0;i<ba.length;i++) {
           _dataChars[i] = ba[i];
        }
        _nDataChars=ba.length;
        xbresponse = UnknownXBeePacket.createPacket(ba);
    }

    public XBeeReply(XBeeReply l) {
        super(l);
        xbresponse = l.xbresponse;
        byte data[] = xbresponse.getPacketData();
        for(int i=0;i<data.length;i++) {
           _dataChars[i] = data[i];
        }
        _nDataChars=data.length;
        setBinary(true);
    }

    public XBeeReply(XBeePacket xbr) {
        super();
        xbresponse = xbr;
        byte data[] = xbr.getPacketData();
        for(int i=0;i<data.length;i++) {
           _dataChars[i] = data[i];
        }
        _nDataChars=data.length;
        setBinary(true);
    }

    @Override
    public String toMonitorString() {
        return xbresponse.toPrettyString();
    }

    @Override
    public String toString() {
        return xbresponse.toString();
    }

    public XBeePacket getXBeeResponse() {
        return xbresponse;
    }

    public void setXBeeResponse(XBeePacket xbr) {
        xbresponse = xbr;
    }

}



package jmri.jmrix.openlcb.swing;

import org.openlcb.EventID;

/**
 * Provide static methods for parsing EventID values.
 *
 * Should eventually be moved to org.openlcb.EventId
 *
 * @author Bob Jacobsen
 */
public class EventIdParser {
       
       
    /**
     * @return "" if nothing interesting about the event    
     */    
    static public String parse(EventID eventID) {
        String eid = eventID.toShortString().substring(0, 2);
        switch (eid) {
            case "00":
                return reserved(eventID);
            case "01": 
                return wellKnown(eventID);
            case "09": 
                if (eventID.toShortString().startsWith("09.00.99.FF")) {
                    return trainSearch(eventID);
                } 
                // deliberately falling through
                //$FALL-THROUGH$
            default:
                return "";
        }
    }
    
    static public String reserved(EventID eventID) {
        return "Reserved "+eventID.toShortString();
    }
    
    static public String wellKnown(EventID eventID) {
        String eid = eventID.toShortString();
        switch (eid) {
            case "01.00.00.00.00.00.FF.FF":
                return "Emergency off";
            case "01.00.00.00.00.00.FF.FE":
                return "Clear Emergency Off";
            case "01.00.00.00.00.00.FF.FD":
                return "Emergency stop of all operations";
            case "01.00.00.00.00.00.FF.FC":
                return "Clear emergency stop of all operations";
            case "01.00.00.00.00.00.FF.F8":
                return "Node recorded a new log entry";
            case "01.00.00.00.00.00.FF.F1":
                return "Power supply brownout detected below minimum required by node";
            case "01.00.00.00.00.00.FF.F0":
                return "Power supply brownout detected below minimum required by standard";
            case "01.00.00.00.00.00.FE.00":
                return "Ident button combination pressed";
            case "01.00.00.00.00.00.FD.01":
                return "Link error code 1 – the specific meaning is link wire protocol specific";
            case "01.00.00.00.00.00.FD.02":
                return "Link error code 2";
            case "01.00.00.00.00.00.FD.03":
                return "Link error code 3";
            case "01.00.00.00.00.00.FD.04":
                return "Link error code 4";

            case "01.01.00.00.00.00.02.01":
                return "Duplicate Node ID Detected";
            case "01.01.00.00.00.00.03.03":
                return "This node is a Train";
            case "01.01.00.00.00.00.03.04":
                return "This node is a Train Control Proxy";
            case "01.01.00.00.00.00.06.01":
                return "Firmware Corrupted";
            case "01.01.00.00.00.00.06.02":
                return "Firmware Upgrade Request by Hardware Switch";

            default:
                // check for fastclock and DCC ranges
                if (eid.startsWith("01.01.00.00.01")) {
                    return fastClock(eventID);
                } else if (eid.startsWith("01.01.02")) {
                    return dccRange(eventID);
                } else {
                    return "Well-Known "+eid;
                }
        }
    }
    
    static public String fastClock(EventID eventID) {
        var clockNum = eventID.toShortString().substring(16, 17);
        var contents = eventID.getContents();
        var lowByte = contents[7]&0xFF;
        var highByte = contents[6]&0xFF;
        var highByteMasked = 0x7F&highByte;
        var bothBytes = highByte*256+lowByte;
        String function = "";
        
        String set = ((0x80 & highByte) == 0x80) ? "Set " : "";

        if ((highByte & 0xF0) == 0xC0) {  // set rate
            int rate = (highByte&0xF)*256+lowByte;
            function = "Set rate "+(rate/4.);
        } else if (bothBytes == 0xF000) {  //
            function = "Query";
        } else if (bothBytes == 0xF001) {  //
            function = "Stop";
        } else if (bothBytes == 0xF002) {  //
            function = "Start";
        } else if (bothBytes == 0xF003) {  //
            function = "Date Rollover";
        } else if (highByteMasked < 24) {  // time
            String lowString = "00"+Integer.toString(lowByte);
            lowString = lowString.substring(lowString.length()-2);
            function = set+"time "+highByteMasked+":"+lowString;
        } else if (highByteMasked <= 0x2C) {  // date
            String lowString = "00"+Integer.toString(lowByte);
            lowString = lowString.substring(lowString.length()-2);
            function = set+"date "+(highByteMasked-0x20)+"/"+lowString;
        } else if (highByteMasked < 0x40) {  // year
            int year = (highByteMasked*256+lowByte)-0x3000;
            function = set+"year "+year;
        } else {
            function = "reserved";
        }
        
        return "Fast Clock "+clockNum+" "+function;
    }

    static public String dccRange(EventID eventID) {
        String eid = eventID.toShortString();
        if (eid.startsWith("01.01.02.00.00.FF")) {
            return "DCC Basic Acc Addr Activate "+(eventID.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FE")) {
            return "DCC Basic Acc Addr Deactivate "+(eventID.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FD")) {
            return "DCC Turnout Feedback On "+(eventID.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FC")) {
            return "DCC Turnout Feedback Off "+(eventID.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FB")) {
            return "DCC Sensor On "+(eventID.toLong()&0xFFFL);
        } else if (eid.startsWith("01.01.02.00.00.FA")) {
            return "DCC Sensor Off "+(eventID.toLong()&0xFFFL);
        } else if (eid.startsWith("01.01.02.00.01")) {
            return "DCC Extended Accessory "
                +((eventID.toLong()>>8)&0x7FFL)
                +" "+(eventID.toLong()&0xFF);
        } else {
            return "DCC Well-Known "+eventID;
        }
    }
    
    static public String trainSearch(EventID eventID) {
        return "Train Search";
    }
    
    /**
     * Take the eventID from a range, and return
     * the lower flag bytes as a hex dotted string.
     */
    static public long rangeSuffix(EventID eventID) {
        // find the mask value
        long eid = eventID.toLong();
        long sampleBit = eid & 0x01;
        long mask = 0L;
        while ( (eid &0x01L) == sampleBit) {
            mask = (mask <<1) | 0x01;
            eid = eid >> 1;
        }
        return mask;
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventIdParser.class);
}

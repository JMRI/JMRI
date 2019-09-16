package jmri.jmrix.dcc4pc;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.RailCom;
import jmri.Sensor;
import jmri.implementation.AbstractRailComReporter;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractRailComReporter for Dcc4Pc Reporters Implemenation for providing
 * status of rail com decoders at this reporter location.
 * <p>
 * The reporter will decode the rail com packets and add the information to the
 * rail com tag.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcReporter extends AbstractRailComReporter {

    public Dcc4PcReporter(String systemName, String userName) {  // a human-readable Reporter number must be specified!
        super(systemName, userName);  // can't use prefix here, as still in construction
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // data members
    transient RailComPacket[] rcPacket = new RailComPacket[3];

    void setPacket(int[] arraytemp, int dcc_addr_type, int dcc_addr, int cvNumber, int speed, int packetTypeCmd) {
        if (log.isDebugEnabled()) {
            log.debug(getDisplayName() + " dcc_addr " + dcc_addr + " " + cvNumber + " " + speed);
        }
        RailComPacket rc = new RailComPacket(arraytemp, dcc_addr_type, dcc_addr, cvNumber, speed);
        decodeRailComInfo(rc, packetTypeCmd);
        rcPacket[2] = rcPacket[1];
        rcPacket[1] = rcPacket[0];
        rcPacket[0] = rc;
        if (log.isDebugEnabled()) {
            log.debug("Packets Seen " + packetseen + " in error " + packetsinerror);
        }
    }

    static class RailComPacket {

        transient final int[] rcPacket;
        int dcc_addr_type;
        int dccAddress;
        int cvNumber;
        int speed;

        RailComPacket(int[] array, int dcc_addr_type, int dcc_addr, int cvNumber, int speed) {
            rcPacket = array;
            this.dcc_addr_type = dcc_addr_type;
            this.dccAddress = dcc_addr;
            this.cvNumber = cvNumber;
            this.speed = speed;
        }

        int[] getPacket() {
            return rcPacket;
        }

        int getAddressType() {
            return dcc_addr_type;
        }

        int getDccAddress() {
            return dccAddress;
        }

        int getCvNumber() {
            return cvNumber;
        }

        int getSpeed() {
            return speed;
        }

        String toHexString() {
            StringBuilder buf = new StringBuilder();
            buf.append("0x" + Integer.toHexString(0xFF & rcPacket[0]));
            for (int i = 1; i < rcPacket.length; i++) {
                buf.append(", 0x" + Integer.toHexString(0xFF & rcPacket[i]));
            }
            return buf.toString();
        }
    }

    void duplicatePacket(int dup) {
        RailComPacket temp;
        switch (dup) {
            case 0x00:
                break; //re-use the rcPacket at the head.
            case 0x02:
                temp = rcPacket[1];  //move rcPacket one to the head
                rcPacket[1] = rcPacket[0];
                rcPacket[0] = temp;
                break;
            case 0x03:
                temp = rcPacket[2]; //move rcPacket two to the head
                rcPacket[2] = rcPacket[1];
                rcPacket[1] = rcPacket[0];
                rcPacket[0] = temp;
                break;
            default:
                break;
        }
    }

    int state = Sensor.UNKNOWN;

    public void setRailComState(int ori) {
        if (state == ori) {
            return;
        }
        state = ori;
        if (ori == Sensor.INACTIVE || ori == Sensor.UNKNOWN) {
            //We reset everything as the associated sensor has gone inactive
            synchronized (this) {
                addr = 0;
                address_part_1 = 0x100;
                address_part_2 = -1;
                addr_type = -1;
                actual_speed = -1;
                actual_load = -1;
                actual_temperature = -1;
                fuelLevel = -1;
                waterLevel = -1;
                location = -1;
                routing_no = -1;
            }
            cvNumber = -1;
            cvValues = new Hashtable<Integer, Integer>();
            setReport(null);
        } else if (ori == RailCom.ORIENTA || ori == RailCom.ORIENTB) {
            if (super.getCurrentReport() != null && super.getCurrentReport() instanceof RailCom) {
                ((RailCom) super.getCurrentReport()).setOrientation(state);
            }
            firePropertyChange("currentReport", null, null);
        }
    }

    public int getRailComState() {
        return state;
    }

    public String getReport() {
        if (super.getCurrentReport() != null && super.getCurrentReport() instanceof RailCom) {
            return ((RailCom) super.getCurrentReport()).getTagID();
        }
        if ((getRailComState() < RailCom.ORIENTA) || (rcPacket[0] == null) || rcPacket[0].getPacket() == null) {
            return "";
        }
        return "";
    }

    //packet Length is a temp store used for decoding the railcom packet
    int packetLength = 0;

    void setPacketLength(int i) {
        packetLength = i;
    }

    int getPacketLength() {
        return packetLength;
    }

    int addr = 0;
    int address_part_1 = 0x100;
    int address_part_2 = -1;
    int addr_type = -1;
    int actual_speed = -1;
    int actual_load = -1;
    int actual_temperature = -1;
    int fuelLevel = -1;
    int waterLevel = -1;
    int location = -1;
    int routing_no = -1;
    int cvNumber = -1;
    int cvvalue = -1;

    int addressp1found = 0;

    static int packetseen = 0;
    static int packetsinerror = 0;
    Hashtable<Integer, Integer> cvValues = new Hashtable<Integer, Integer>();

    synchronized void decodeRailComInfo(RailComPacket rc, int packetTypeCmd) {
        if (log.isDebugEnabled()) {
            log.debug(getDisplayName() + " " + packetTypeCmd);
        }
        addressp1found++;
        RailCom rcTag = null;
        if (log.isDebugEnabled()) {
            log.debug(this.getDisplayName() + " " + super.getCurrentReport());
        }
        if (super.getCurrentReport() instanceof RailCom) {
            rcTag = (RailCom) super.getCurrentReport();
        }
        int[] packet = rc.getPacket();
        char chbyte;
        char type;

        if (log.isDebugEnabled()) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < packet.length; ++i) {
                buf.append(packet[i]);
            }
            String s = buf.toString();
            log.debug("Rail Comm Packets " + s);

        }
        int i = 0;
        while (i < packet.length) {
            packetseen++;
            chbyte = (char) packet[i];
            chbyte = decode[chbyte];
            if (chbyte == ERROR) {
                if (log.isDebugEnabled()) {
                    log.debug(this.getDisplayName() + " Error packet stage 1: " + Integer.toHexString(packet[i]));
                }
                packetsinerror++;
                return;
            }
            i++;
            if ((chbyte & ACK) == ACK) {
                chbyte = (char) packet[i];
                i++;
                chbyte = decode[chbyte];
                if (chbyte == ERROR) {
                    if (log.isDebugEnabled()) {
                        log.debug(this.getDisplayName() + " Error packet stage 2");
                    }
                    packetsinerror++;
                    return;
                }
                if ((chbyte & ACK) == ACK) {
                    if (packet.length <= (i + 1)) {
                        log.debug("No further data to process Only had the ack 1");
                        break;
                    }
                    chbyte = (char) packet[i];
                    i++;
                    chbyte = decode[chbyte];
                }
            }
            if (packet.length <= i) {
                break;
            }
            type = chbyte;
            chbyte = (char) packet[i];
            chbyte = decode[chbyte];
            if ((chbyte == ERROR) || ((chbyte & ACK) == ACK)) {
                if (log.isDebugEnabled()) {
                    log.debug(this.getDisplayName() + " Error packet stage 3 " + Integer.toHexString(packet[i]) + "\n" + rc.toHexString());
                }
                i++;
                packetsinerror++;
                return;
            }

            chbyte = (char) (((type & 0x03) << 6) | (chbyte & 0x3f));
            type = (char) ((type >> 2) & 0x0F);

            switch (type) {
                case 0:
                    if (log.isDebugEnabled()) {
                        log.debug(this.getDisplayName() + " CV Value " + ((int) chbyte) + rcTag);
                    }
                    cvvalue = chbyte;
                    if (rcTag != null) {
                        rcTag.setWhereLastSeen(this);
                        if (rcTag.getExpectedCv() != -1) {
                            rcTag.setCvValue(chbyte);
                        } else {
                            rcTag.setCV(rc.getCvNumber(), chbyte);
                        }
                    }
                    break;
                case 4:
                    if (log.isDebugEnabled()) {
                        log.debug(this.getDisplayName() + " Create/Get id tag for " + rc.getDccAddress());
                    }
                    addr = rc.getDccAddress();
                    addr_type = rc.getAddressType();
                    break;
                case 1: // Address byte 1
                    if (log.isDebugEnabled()) {
                        log.debug("Address Byte 1");
                    }
                    address_part_1 = (0x100 | chbyte);
                    addressp1found = 0;
                    break;
                case 2: //Address byte 2
                    if (log.isDebugEnabled()) {
                        log.debug(this.getDisplayName() + " Address part 2:");
                    }
                    address_part_2 = chbyte;
                    if (packetTypeCmd == 0x03) {
                        log.debug("Type three packet so shouldn't not pair part one with part two if it came from the previous packet");
                        //As the last command was a type 3, an address part one packet can not be paired with this address part two packet.  Therefore will set it back to default
                        //address_part_1 = 0x100;
                        // break;
                    }
                    if (!((address_part_1 & 0x100) == 0x100)) {
                        log.debug(this.getDisplayName() + " Break at Address part 1, part one not complete");
                        break;
                    }
                    rcTag = decodeAddress();
                    break;
                case 3: //Actual Speed / load
                    if ((chbyte & 0x80) == 0x80) {
                        actual_speed = (chbyte & 0x7f);
                        log.debug(this.getDisplayName() + " Actual Speed: " + actual_speed);
                    } else {
                        actual_load = (chbyte & 0x7f);
                        log.debug(this.getDisplayName() + " Actual Load: " + actual_load);
                    }
                    if (rcTag != null) {
                        rcTag.setActualLoad(actual_load);
                        rcTag.setActualSpeed(actual_speed);
                        rcTag.setWhereLastSeen(this);
                    }
                    break;
                case 5: //Routing number
                    routing_no = chbyte;
                    if (rcTag != null) {
                        rcTag.setRoutingNo(routing_no);
                        rcTag.setWhereLastSeen(this);
                    }
                    break;
                case 6: //Location
                    location = chbyte;
                    if (rcTag != null) {
                        rcTag.setLocation(location);
                        rcTag.setWhereLastSeen(this);
                    }
                    break;
                case 7: //Fuel water
                    if ((chbyte & 0x80) == 0x80) {
                        fuelLevel = (chbyte & 0x7f);
                    } else {
                        waterLevel = (chbyte & 0x7f);
                    }

                    if (rcTag != null) {
                        rcTag.setWaterLevel(waterLevel);
                        rcTag.setFuelLevel(fuelLevel);
                        rcTag.setWhereLastSeen(this);
                    }
                    break;
                case 8: //Temp
                    if (!((chbyte & 0x80) == 0x80)) {
                        actual_temperature = (chbyte & 0x7F);
                    }
                    if (rcTag != null) {
                        rcTag.setActualTemperature(actual_temperature);
                        rcTag.setWhereLastSeen(this);
                    }
                    break;
                case 15: //CV Address  Value
                    log.debug(this.getDisplayName() + " CV Address and value:");
                    i = i + 2;
                    //len = 4;
                    break;
                default:
                    log.info("unknown railcom type packet " + type);
                    break;
            }
            i++;

        }
    }

    RailCom decodeAddress() {
        RailCom rcTag;
        if (log.isDebugEnabled()) {
            log.debug(this.getDisplayName() + " Create/Get id tag for " + addr);
        }
        rcTag = (RailCom)jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + addr);

        if ((fuelLevel != -1)) {
            rcTag.setFuelLevel(fuelLevel);
        }
        if ((waterLevel != -1)) {
            rcTag.setWaterLevel(waterLevel);
        }
        if ((routing_no != -1)) {
            rcTag.setRoutingNo(routing_no);
        }
        if ((location != -1)) {
            rcTag.setLocation(location);
        }
        if ((actual_temperature != -1)) {
            rcTag.setActualTemperature(actual_temperature);
        }
        if ((actual_load != -1)) {
            rcTag.setActualLoad(actual_load);
        }
        if ((actual_speed != -1)) {
            rcTag.setActualSpeed(actual_speed);
        }
        for (Map.Entry<Integer, Integer> entry : cvValues.entrySet()) {
            rcTag.setCV(entry.getKey(), entry.getValue());
            if (cvvalue != -1) {
                rcTag.setCvValue(cvvalue);
            }
        }

        address_part_1 = 0;
        address_part_2 = -1;
        notify(rcTag);
        return rcTag;
    }

    RailCom provideTag(int address, int addr_type) {
        log.debug("provide Tag");
        RailCom rcTag = (RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + address);
        notify(rcTag);
        return rcTag;
    }

    public final static char ACK = 0x80;
    public final static char ACK_1 = 0x81;
    public final static char ACK_2 = 0x82;
    public final static char ACK_3 = 0x83;
    public final static char ACK_4 = 0x84;
    public final static char ACK_5 = 0x85;
    public final static char ACK_6 = 0x86;
    public final static char ERROR = 0xFF;

    private final static char[] decode = new char[]{
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ACK_1,
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, 0x33,
        ERROR, ERROR, ERROR, 0x34, ERROR, 0x35, 0x36, ERROR,
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, 0x3A,
        ERROR, ERROR, ERROR, 0x3B, ERROR, 0x3C, 0x37, ERROR,
        ERROR, ERROR, ERROR, 0x3F, ERROR, 0x3D, 0x38, ERROR,
        ERROR, 0x3E, 0x39, ERROR, ACK_5, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, 0x24,
        ERROR, ERROR, ERROR, 0x23, ERROR, 0x22, 0x21, ERROR,
        ERROR, ERROR, ERROR, 0x1F, ERROR, 0x1E, 0x20, ERROR,
        ERROR, 0x1D, 0x1C, ERROR, 0x1B, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, 0x19, ERROR, 0x18, 0x1A, ERROR,
        ERROR, 0x17, 0x16, ERROR, 0x15, ERROR, ERROR, ERROR,
        ERROR, 0x25, 0x14, ERROR, 0x13, ERROR, ERROR, ERROR,
        0x32, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ACK_2,
        ERROR, ERROR, ERROR, 0x0E, ERROR, 0x0D, 0x0C, ERROR,
        ERROR, ERROR, ERROR, 0x0A, ERROR, 0x09, 0x0B, ERROR,
        ERROR, 0x08, 0x07, ERROR, 0x06, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, 0x04, ERROR, 0x03, 0x05, ERROR,
        ERROR, 0x02, 0x01, ERROR, 0x00, ERROR, ERROR, ERROR,
        ERROR, 0x0F, 0x10, ERROR, 0x11, ERROR, ERROR, ERROR,
        0x12, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, ACK_3, ERROR, 0x2B, 0x30, ERROR,
        ERROR, 0x2A, 0x2F, ERROR, 0x31, ERROR, ERROR, ERROR,
        ERROR, 0x29, 0x2E, ERROR, 0x2D, ERROR, ERROR, ERROR,
        0x2C, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,
        ERROR, ACK_6, 0x28, ERROR, 0x27, ERROR, ERROR, ERROR,
        0x26, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,
        ACK_4, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,
        ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR};

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcReporter.class);

}

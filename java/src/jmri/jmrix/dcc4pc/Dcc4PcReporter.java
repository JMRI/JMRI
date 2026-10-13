package jmri.jmrix.dcc4pc;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import jmri.CollectingReporter;
import jmri.DccLocoAddress;
import jmri.IdTag;
import jmri.RailCom;
import jmri.Reporter;
import jmri.Sensor;
import jmri.implementation.AbstractRailComReporter;

/**
 * Extend jmri.implementation.AbstractRailComReporter for Dcc4Pc Reporters.
 * Implementation for providing status of rail com decoders at this
 * reporter location.
 * <p>
 * The reporter will decode the rail com packets and add the information to the
 * rail com tag.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcReporter extends AbstractRailComReporter implements CollectingReporter {

    public Dcc4PcReporter(String systemName, String userName) {  // a human-readable Reporter number must be specified!
        super(systemName, userName);  // can't use prefix here, as still in construction
    }

    // The locos in this block, oldest first. Dcc4PcBlockLocos decides who is
    // present, from channel 2 replies and clean channel 1 addresses, so that
    // several locos in one block can be told apart.
    // It's changed on the packet decode thread and read by the GUI (the Reporter
    // table), so it's a copy-on-write list and readers always see a consistent
    // snapshot.
    private final List<IdTag> entryOrder = new CopyOnWriteArrayList<>();

    /** @return the locos in this block, oldest first, as one consistent snapshot */
    private IdTag[] entrySnapshot() {
        return entryOrder.toArray(new IdTag[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> getCollection() {
        return (Collection<Object>) (Collection<?>) Collections.unmodifiableCollection(entryOrder);
    }

    private static RailCom tagFor(int address) {
        return (RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + address);
    }

    /**
     * A loco has been found in this block.
     *
     * @param address loco address
     * @param isLong  true for a long (14-bit) address
     */
    void locoEntered(int address, boolean isLong) {
        RailCom tag = tagFor(address);
        tag.setDccAddress(new DccLocoAddress(address, isLong));
        RailCom.Orientation o = orientation();
        if (o != null) {
            tag.setOrientation(o);
        }
        entryOrder.remove(tag);
        entryOrder.add(tag);
        notify(tag);
    }

    /**
     * A loco already in this block has been heard again: update its "last
     * seen" time. This doesn't make it the current report, so with several
     * locos here the report doesn't flip between them.
     *
     * @param address loco address
     */
    void locoHeard(int address) {
        RailCom tag = tagFor(address);
        if (entryOrder.contains(tag)) {
            tag.setWhereLastSeen(this);
        }
    }

    /**
     * A loco has left this block.
     *
     * @param address loco address
     */
    void locoExited(int address) {
        RailCom tag = tagFor(address);
        if (!entryOrder.remove(tag)) {
            return;
        }
        if (getCurrentReport() == tag) {
            updateCurrentReportAfterExit();
        } else {
            notifyCollectionUpdated();
        }
    }

    /**
     * Report the most recently entered loco still in this block, as
     * {@link jmri.jmrix.openlcb.OlcbReporter} does.
     */
    private void updateCurrentReportAfterExit() {
        IdTag[] tags = entrySnapshot();
        if (tags.length == 0) {
            notify(null);
            return;
        }
        for (int i = tags.length - 1; i >= 0; i--) {
            IdTag candidate = tags[i];
            if (candidate.getWhereLastSeen() == this) {
                notify(candidate);
                return;
            }
        }
        IdTag fallback = tags[tags.length - 1];
        setReport(fallback);
        setState(IdTag.SEEN);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Exits are reported by {@link #locoExited(int)}, so a loco seen elsewhere
     * stays in the collection until then; only the current report moves on.
     */
    @Override
    public void notifySeenElsewhere(IdTag tag, Reporter newReporter) {
        if (!this.equals(newReporter) && getCurrentReport() == tag) {
            IdTag[] tags = entrySnapshot();
            for (int i = tags.length - 1; i >= 0; i--) {
                IdTag candidate = tags[i];
                if (candidate != tag && candidate.getWhereLastSeen() == this) {
                    notify(candidate);
                    return;
                }
            }
        }
    }

    /**
     * The report text is worked out when a loco is reported; if that loco is
     * the current report and its data (such as speed) changes later, work it
     * out again so the Reporter table shows the new value.
     */
    private void refreshReport(RailCom tag) {
        if (getCurrentReport() == tag) {
            setReport(tag);
            firePropertyChange("currentReport", null, tag);
        }
    }

    /** @return the orientation the input reports, or null if it reports none */
    private RailCom.Orientation orientation() {
        if (state == REPRESENTS_RAILCOM_EAST) {
            return RailCom.Orientation.EAST;
        } else if (state == REPRESENTS_RAILCOM_WEST) {
            return RailCom.Orientation.WEST;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExitReports() {
        return true;
    }

    // data members
    transient RailComPacket[] rcPacket = new RailComPacket[3];

    void setPacket(int[] arraytemp, int dcc_addr_type, int dcc_addr, int cvNumber, int speed, int packetTypeCmd) {
        log.debug("{} dcc_addr {} {} {}", getDisplayName(), dcc_addr, cvNumber, speed);
        RailComPacket rc = new RailComPacket(arraytemp, dcc_addr_type, dcc_addr, cvNumber, speed);
        decodeRailComInfo(rc, packetTypeCmd);
        rcPacket[2] = rcPacket[1];
        rcPacket[1] = rcPacket[0];
        rcPacket[0] = rc;
        synchronized(lock) {
            log.debug("Packets Seen {} in error {}", packetseen, packetsinerror);
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
            buf.append("0x").append(Integer.toHexString(0xFF & rcPacket[0]));
            for (int i = 1; i < rcPacket.length; i++) {
                buf.append(", 0x").append(Integer.toHexString(0xFF & rcPacket[i]));
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
    
    static final int REPRESENTS_RAILCOM_EAST = 0x10;
    static final int REPRESENTS_RAILCOM_WEST = 0x20;
        
    public void setRailComState(int ori) {
        if (state == ori) {
            return;
        }
        state = ori;
        if (ori == Sensor.INACTIVE || ori == Sensor.UNKNOWN) {
            //We reset everything as the associated sensor has gone inactive
            synchronized (this) {
                addr = 0;
                address_part_1 = 0; // no ADR_HIGH yet; 0x100 would pass for short-address high byte 0
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
            cvValues = new Hashtable<>();
            entryOrder.clear();
            setReport(null);
        } else if (ori == REPRESENTS_RAILCOM_EAST || ori == REPRESENTS_RAILCOM_WEST) {
            // the reader gives one orientation per input, so it applies to every loco here
            RailCom.Orientation orientation = orientation();
            for (IdTag t : entryOrder) {
                ((RailCom) t).setOrientation(orientation);
            }
            if (super.getCurrentReport() instanceof RailCom) {
                ((RailCom) super.getCurrentReport()).setOrientation(orientation);
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
        if ((getRailComState() < REPRESENTS_RAILCOM_EAST) || (rcPacket[0] == null) || rcPacket[0].getPacket() == null) {
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
    int address_part_1 = 0; // 0x100 bit set once an ADR_HIGH has arrived
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
    private static final Object lock = new Object();

    Hashtable<Integer, Integer> cvValues = new Hashtable<>();

    /**
     * The first two bytes of a cutout's data are channel 1. Channel 2 is a
     * separate time slot, so a bad byte in channel 1 (often two decoders'
     * address broadcasts colliding) mustn't throw away channel 2: on
     * 2026-10-13 that discarded POM answers that came with channel 1
     * {@code 95 04}.
     */
    static final int CHANNEL_1_LENGTH = 2;

    void decodeRailComInfo(RailComPacket rc, int packetTypeCmd) {

        synchronized(lock) {
            addressp1found++;
            RailCom rcTag = null;

            if (super.getCurrentReport() instanceof RailCom) {
                rcTag = (RailCom) super.getCurrentReport();
            }
            // Channel 2 data (POM values, DYN) comes from the loco the DCC packet was sent
            // to, which with several locos here needn't be the current report.
            // Only that decoder replies on channel 2, so if it isn't one of the locos here,
            // the data belongs to nobody here; never give it to whichever loco is current.
            RailCom ch2Tag = null;
            if (rc.getDccAddress() > 0) {
                RailCom dest = tagFor(rc.getDccAddress());
                if (entryOrder.contains(dest)) {
                    ch2Tag = dest;
                }
            }
            int[] packet = rc.getPacket();
            char chbyte;
            char type;

            if (log.isDebugEnabled()) {
                log.debug("{} packet type {}", getDisplayName(), packetTypeCmd);
                log.debug("decodeRailComInfo {} {}", this.getDisplayName(), super.getCurrentReport());
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < packet.length; ++i) {
                    buf.append(packet[i]);
                }
                log.debug("Rail Comm Packets {}", buf);

            }
            int i = 0;
            while (i < packet.length) {
                packetseen++;
                chbyte = (char) packet[i];
                chbyte = decode[chbyte];
                if (chbyte == ERROR) {
                    if (log.isDebugEnabled()) {
                        log.debug("{} Error packet stage 1: {}", this.getDisplayName(), Integer.toHexString(packet[i]));
                    }
                    packetsinerror++;
                    if (i < CHANNEL_1_LENGTH) {
                        i = CHANNEL_1_LENGTH; // channel 2 is a separate slot: still decode it
                        continue;
                    }
                    return;
                }
                i++;
                if ((chbyte & ACK) == ACK) {
                    // ACK, NACK or BUSY: a single symbol, and channel 2 may hold several
                    continue;
                }
                if (packet.length <= i) {
                    break;
                }
                type = chbyte;
                chbyte = (char) packet[i];
                chbyte = decode[chbyte];
                if ((chbyte == ERROR) || ((chbyte & ACK) == ACK)) {
                    if (log.isDebugEnabled()) {
                        log.debug("{} Error packet stage 3 {}\n{}", this.getDisplayName(), Integer.toHexString(packet[i]), rc.toHexString());
                    }
                    i++;
                    packetsinerror++;
                    if (i <= CHANNEL_1_LENGTH) {
                        i = CHANNEL_1_LENGTH; // channel 2 is a separate slot: still decode it
                        continue;
                    }
                    return;
                }

                chbyte = (char) (((type & 0x03) << 6) | (chbyte & 0x3f));
                type = (char) ((type >> 2) & 0x0F);
                // RCN-217: EXT (3) and DYN (7) datagrams are three symbols long and
                // IDs 8-11 six; the rest are two. Skipping the extra symbols stops
                // them being read as the start of another datagram, such as a
                // bogus ADR_HIGH from a DYN subindex.
                int extraSymbols = 0;
                if (type == 3 || type == 7) {
                    extraSymbols = 1;
                } else if (type >= 8 && type <= 11) {
                    extraSymbols = 4;
                }

                switch (type) {
                    case 0:
                        log.debug("{} CV Value {}{}", this.getDisplayName(), (int) chbyte, ch2Tag);
                        cvvalue = chbyte;
                        if (ch2Tag != null && rc.getCvNumber() > 0) {
                            // Only the reply to a POM packet for the CV being read or written
                            // answers it: replies to earlier POM packets, still being repeated,
                            // arrive too, and were taken as the answer (2026-10-13).
                            if (ch2Tag.getExpectedCv() == rc.getCvNumber()) {
                                ch2Tag.setCvValue(chbyte);
                            } else {
                                ch2Tag.setCV(rc.getCvNumber(), chbyte);
                            }
                        }
                        break;
                    case 4:
                        if (log.isDebugEnabled()) {
                            log.debug("{} Create/Get id tag for {}", this.getDisplayName(), rc.getDccAddress());
                        }
                        addr = rc.getDccAddress();
                        addr_type = rc.getAddressType();
                        break;
                    case 1: // Address byte 1
                        log.debug("Address Byte 1");
                        // only 0 (short), 10xxxxxx (long) or 0110xxxx (consist) are addresses
                        if (chbyte != 0 && (chbyte & 0xC0) != 0x80 && (chbyte & 0xF0) != 0x60) {
                            log.debug("{} Ignoring invalid address byte 1 {}", this.getDisplayName(), Integer.toHexString(chbyte));
                            break;
                        }
                        address_part_1 = (0x100 | chbyte);
                        addressp1found = 0;
                        break;
                    case 2: //Address byte 2
                        log.debug("{} Address part 2:", this.getDisplayName());
                        address_part_2 = chbyte;
                        if (packetTypeCmd == 0x03) {
                            log.debug("Type three packet so shouldn't not pair part one with part two if it came from the previous packet");
                            //As the last command was a type 3, an address part one packet can not be paired with this address part two packet.  Therefore will set it back to default
                            //address_part_1 = 0x100;
                            // break;
                        }
                        if (!((address_part_1 & 0x100) == 0x100)) {
                            log.debug("{} Break at Address part 1, part one not complete", this.getDisplayName());
                            break;
                        }
                        addr = combineAddress(address_part_1 & 0xFF, address_part_2);
                        rcTag = decodeAddress();
                        break;
                    case 3: // EXT (RCN-217), location information; not decoded yet
                        log.debug("{} EXT datagram", this.getDisplayName());
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
                    case 7: // DYN (RCN-217): 8-bit value, then a 6-bit subindex in the third symbol
                        if (i + 1 >= packet.length || decode[packet[i + 1]] == ERROR
                                || (decode[packet[i + 1]] & ACK) == ACK) {
                            log.debug("{} DYN datagram without a valid subindex", this.getDisplayName());
                            break;
                        }
                        int subindex = decode[packet[i + 1]] & 0x3F;
                        log.debug("{} DYN subindex {} value {}", this.getDisplayName(), subindex, (int) chbyte);
                        if (subindex == 0) { // actual speed 0-255
                            actual_speed = chbyte;
                        } else if (subindex == 1) { // actual speed 256-511
                            actual_speed = chbyte + 256;
                        } else {
                            break; // other DYN values are not decoded yet
                        }
                        if (ch2Tag != null && ch2Tag.getActualSpeed() != actual_speed) {
                            ch2Tag.setActualSpeed(actual_speed);
                            refreshReport(ch2Tag);
                        }
                        break;
                    case 8: // XPOM (RCN-217, IDs 8-11); not decoded yet
                    case 9:
                    case 10:
                    case 11:
                        log.debug("{} XPOM datagram", this.getDisplayName());
                        break;
                    case 15: //CV Address  Value
                        log.debug("{} CV Address and value:", this.getDisplayName());
                        i = i + 2;
                        //len = 4;
                        break;
                    default:
                        log.debug("unknown railcom type packet {}", type);
                        break;
                }
                i += 1 + extraSymbols;

            }
        }
    }

    RailCom decodeAddress() {
        RailCom rcTag;
        log.debug("{} Create/Get id tag for {}", this.getDisplayName(), addr);
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
        // No notify(rcTag) here: Dcc4PcBlockLocos decides which locos are present
        // and calls locoEntered(), which also sets the orientation.
        return rcTag;
    }

    /**
     * Combine the ADR_HIGH and ADR_LOW payloads of a RailCom channel 1
     * broadcast into a locomotive address (RCN-217 5.2.1).
     *
     * @param high ADR_HIGH payload: 0 for a short address, 10xxxxxx for the
     *             top six bits of a long address, 0110xxxx for a consist
     * @param low  ADR_LOW payload
     * @return the address
     */
    static int combineAddress(int high, int low) {
        if ((high & 0xC0) == 0x80) {
            return ((high & 0x3F) << 8) | (low & 0xFF);
        }
        return low & 0x7F;
    }

    RailCom provideTag(int address, int addr_type) {
        log.debug("provide Tag");
        RailCom rcTag = (RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + address);
        notify(rcTag);
        return rcTag;
    }

    public static final char ACK = 0x80;
    public static final char ACK_1 = 0x81;
    public static final char ACK_2 = 0x82;
    public static final char ACK_3 = 0x83;
    public static final char ACK_4 = 0x84;
    public static final char ACK_5 = 0x85;
    public static final char ACK_6 = 0x86;
    public static final char ERROR = 0xFF;

    private static final char[] decode = new char[]{
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dcc4PcReporter.class);

}

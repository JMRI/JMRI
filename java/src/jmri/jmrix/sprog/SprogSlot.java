package jmri.jmrix.sprog;

import java.util.Arrays;
import jmri.DccLocoAddress;
import jmri.SpeedStepMode;
import jmri.NmraPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent information for a DCC Command Station Queue entry where each entry
 * is a DCC packet to be transmitted to the rails.
 * <p>
 * A SlotListener can be registered to hear of changes in this slot. All changes
 * in values will result in notification.
 * <p>
 * Updated by Andrew Crosland February 2012 to allow slots to hold 28 step speed
 * packets
 *
 * @author	Andrew Crosland Copyright (C) 2006, 2012
 * @author	Andrew Berridge 2010
 */
public class SprogSlot {

    private boolean speedPacket = false;
    private SpeedStepMode speedMode = SpeedStepMode.NMRA_DCC_128;

    public SprogSlot(int num) {
        payload = new byte[SprogConstants.MAX_PACKET_LENGTH];
        payload[0] = 0;
        payload[1] = 0;
        payload[2] = 0;
        repeat = -1;
        addr = 0;
        isLong = false;
        spd = 0;
        forward = true;
        status = SprogConstants.SLOT_FREE;
        slot = num;
        opsPkt = false;
    }

    private byte[] payload;
    // repeat of -1 is a persistent entry, ie a loco slot
    private int repeat;
    private int addr;
    private boolean isLong;
    private int spd;
    private boolean forward;
    private int status;
    private int slot;
    private boolean opsPkt;

    private boolean f0to4Packet = false;

    public boolean isF0to4Packet() {
        return f0to4Packet;
    }

    public boolean isF5to8Packet() {
        return f5to8Packet;
    }

    public boolean isF9to12Packet() {
        return f9to12Packet;
    }

    private boolean f5to8Packet = false;
    private boolean f9to12Packet = false;

    private boolean repeatF0 = false;
    private boolean repeatF1 = false;
    private boolean repeatF2 = false;
    private boolean repeatF3 = false;
    private boolean repeatF4 = false;
    private boolean repeatF5 = false;
    private boolean repeatF6 = false;
    private boolean repeatF7 = false;
    private boolean repeatF8 = false;
    private boolean repeatF9 = false;
    private boolean repeatF10 = false;
    private boolean repeatF11 = false;
    private boolean repeatF12 = false;

    /**
     * Set the contents of the slot. Intended for accessory packets.
     *
     * @param address int
     * @param payload byte[]
     * @param repeat  int
     */
    public void set(int address, byte[] payload, int repeat) {
        addr = address;

        Arrays.copyOf(payload, payload.length);

        this.setRepeat(repeat);
        status = SprogConstants.SLOT_IN_USE;
    }

    public void setAccessoryPacket(int address, boolean closed, int repeats) {
        this.payload = NmraPacket.accDecoderPkt(address, closed);
        this.addr = address + 10000;
        this.repeat = repeats;
        status = SprogConstants.SLOT_IN_USE;
    }

    public boolean isSpeedPacket() {
        return speedPacket;
    }

    public void setSpeed(SpeedStepMode mode, int address, boolean isLongAddress, int speed, boolean forward) {
        addr = address;
        isLong = isLongAddress;
        spd = speed;
        this.speedPacket = true;
        this.speedMode = mode;
        this.f0to4Packet = false;
        this.forward = forward;
        if (mode == SpeedStepMode.NMRA_DCC_28) {
            this.payload = jmri.NmraPacket.speedStep28Packet(true, addr,
                    isLong, spd, forward);
        } else {
            this.payload = jmri.NmraPacket.speedStep128Packet(addr,
                    isLong, spd, forward);
        }
        status = SprogConstants.SLOT_IN_USE;
    }

    public void setOps(int address, boolean longAddr, int cv, int val) {
        payload = NmraPacket.opsCvWriteByte(address, longAddr, cv, val);
        this.repeat = SprogConstants.OPS_REPEATS;
        this.opsPkt = true;
        status = SprogConstants.SLOT_IN_USE;
    }

    public void f5to8packet(int address, boolean isLongAddress,
            boolean f5, boolean f5Momentary,
            boolean f6, boolean f6Momentary,
            boolean f7, boolean f7Momentary,
            boolean f8, boolean f8Momentary) {

        this.f5to8Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF5 && !f5)
                || (this.repeatF6 && !f6)
                || (this.repeatF7 && !f7)
                || (this.repeatF8 && !f8)) {
            this.repeat = 3; //Then repeat 3 times
        }

        if (!f5Momentary && f5) {
            this.repeatF5 = true;
        } else {
            this.repeatF5 = false;
        }
        if (!f6Momentary && f6) {
            this.repeatF6 = true;
        } else {
            this.repeatF6 = false;
        }
        if (!f7Momentary && f7) {
            this.repeatF7 = true;
        } else {
            this.repeatF7 = false;
        }
        if (!f8Momentary && f8) {
            this.repeatF8 = true;
        } else {
            this.repeatF8 = false;
        }

        this.payload = jmri.NmraPacket.function5Through8Packet(address,
                isLongAddress,
                f5, f6, f7, f8);
        this.status = SprogConstants.SLOT_IN_USE;

    }

    public void f9to12packet(int address, boolean isLongAddress,
            boolean f9, boolean f9Momentary,
            boolean f10, boolean f10Momentary,
            boolean f11, boolean f11Momentary,
            boolean f12, boolean f12Momentary) {

        this.f9to12Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF9 && !f9)
                || (this.repeatF10 && !f10)
                || (this.repeatF11 && !f11)
                || (this.repeatF12 && !f12)) {
            this.repeat = 3; //Then repeat 3 times
        }

        if (!f9Momentary && f9) {
            this.repeatF9 = true;
        } else {
            this.repeatF9 = false;
        }
        if (!f10Momentary && f10) {
            this.repeatF10 = true;
        } else {
            this.repeatF10 = false;
        }
        if (!f11Momentary && f11) {
            this.repeatF11 = true;
        } else {
            this.repeatF11 = false;
        }
        if (!f12Momentary && f12) {
            this.repeatF12 = true;
        } else {
            this.repeatF12 = false;
        }

        this.payload = jmri.NmraPacket.function9Through12Packet(address,
                isLongAddress,
                f9, f10, f11, f12);
        this.status = SprogConstants.SLOT_IN_USE;

    }

    public void f0to4packet(int address, boolean isLongAddress,
            boolean f0, boolean f0Momentary,
            boolean f1, boolean f1Momentary,
            boolean f2, boolean f2Momentary,
            boolean f3, boolean f3Momentary,
            boolean f4, boolean f4Momentary) {

        this.f0to4Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF0 && !f0)
                || (this.repeatF1 && !f1)
                || (this.repeatF2 && !f2)
                || (this.repeatF3 && !f3)
                || (this.repeatF4 && !f4)) {
            this.repeat = 3; //Then repeat 3 times
        }

        if (!f0Momentary && f0) {
            this.repeatF0 = true;
        } else {
            this.repeatF0 = false;
        }
        if (!f1Momentary && f1) {
            this.repeatF1 = true;
        } else {
            this.repeatF1 = false;
        }
        if (!f2Momentary && f2) {
            this.repeatF2 = true;
        } else {
            this.repeatF2 = false;
        }
        if (!f3Momentary && f3) {
            this.repeatF3 = true;
        } else {
            this.repeatF3 = false;
        }
        if (!f4Momentary && f4) {
            this.repeatF4 = true;
        } else {
            this.repeatF4 = false;
        }
        this.payload = jmri.NmraPacket.function0Through4Packet(address,
                isLongAddress,
                f0, f1, f2, f3, f4);
        this.status = SprogConstants.SLOT_IN_USE;

    }

    public boolean isFinished() {
        if (this.isF0to4Packet()) {
            if ((this.repeatF0 || this.repeatF1 || this.repeatF2 || this.repeatF3 || this.repeatF4)) {
                return false;
            }
        }
        if (this.isF5to8Packet()) {
            if ((this.repeatF5 || this.repeatF6 || this.repeatF7 || this.repeatF8)) {
                return false;
            }
        }
        if (this.isF9to12Packet()) {
            if ((this.repeatF9 || this.repeatF10 || this.repeatF11 || this.repeatF12)) {
                return false;
            }
        }
        if (this.isSpeedPacket() && this.status == SprogConstants.SLOT_IN_USE) {
            return false;
        }
        if (this.repeat > 0 && this.status == SprogConstants.SLOT_IN_USE) {
            return false;
        }
        /* Finished - clear and return true */
        this.clear();
        return true;
    }

    public void eStop() {
        this.setSpeed(this.speedMode, this.addr, this.isLong, 1, this.forward);
    }

    // Access methods

    public void clear() {
        status = SprogConstants.SLOT_FREE;
        addr = 0;
        spd = 0;
        speedPacket = false;
        f0to4Packet = false;
        if (payload != null) {
            payload[0] = 0;
            payload[1] = 0;
            payload[2] = 0;
        }
        opsPkt = false;
    }

    public boolean isLongAddress() {
        return isLong;
    }

    public boolean isFree() {
        return (status == SprogConstants.SLOT_FREE);
    }

    public int slotStatus() {
        return status;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int r) {
        repeat = r;
    }

    private int doRepeat() {
        if (repeat > 0) {
            log.debug("Slot " + slot + " repeats");
            repeat--;
            if (repeat == 0) {
                log.debug("Clear slot " + slot + " due to repeats exhausted");
                this.clear();
            }
        }
        return repeat;
    }

    public int speed() {
        return spd;
    }

    public int locoAddr() {
        return addr;
    }

    public int getAddr() {
        if (opsPkt == false) {
            return addr;
        } else {
            return addressFromPacket();
        }
    }

    public void setAddr(int a) {
        addr = a;
    }

    public boolean getIsLong() {
        if (opsPkt == false) {
            return isLong;
        } else {
            return ((payload[0] & 0xC0) >= 0xC0);
        }
    }

    public void setIsLong(boolean a) {
        isLong = a;
    }

    public boolean isForward() {
        return forward;
    }

    public boolean isOpsPkt() {
        return opsPkt;
    }

    public boolean isActiveAddressMatch(DccLocoAddress address) {
        return ( status == SprogConstants.SLOT_IN_USE && getAddr() == address.getNumber() && getIsLong() == address.isLongAddress() );
    }

    /**
     * Get the payload of this slot. Note - if this slot has a number of
     * repeats, calling this method will also decrement the internal repeat
     * counter.
     *
     * @return a byte array containing the payload of this slot
     */
    public byte[] getPayload() {

        byte[] p;
        if (payload != null) {
            p = Arrays.copyOf(payload, getPayloadLength());//, a Java 1.6 construct
        } else {
            p = new byte[0];
        }
        /*byte [] p = new byte[getPayloadLength()];
         for (int i = 0; i<getPayloadLength(); i++) p[i] = payload[i];*/

        //decrement repeat counter if appropriate
        doRepeat();
        return p;

    }

    public int getSlotNumber() {
        return slot;
    }

    private int getPayloadLength() {
        return this.payload.length;
    }

    private long lastUpdateTime; // Time of last update for detecting stale slots

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Get the address from the packet.
     *
     * @return int address from payload
     */
    private int addressFromPacket() {
        if (isFree()) {
            return -1;
        }
        // First deal with possible extended address
        if ((payload[0] & 0xC0) == 0xC0) {
            return ((payload[0] & 0x3F) << 8 | (payload[1] & 0xFF));
        }
        return payload[0];
    }

    private final static Logger log = LoggerFactory.getLogger(SprogSlot.class);
}

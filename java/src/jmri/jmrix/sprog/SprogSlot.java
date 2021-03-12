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
 * @author Andrew Crosland Copyright (C) 2006, 2012
 * @author Andrew Berridge 2010
 */
public class SprogSlot {

    private boolean speedPacket = false;
    private SpeedStepMode speedMode = SpeedStepMode.NMRA_DCC_128;

    public SprogSlot(int num) {
        payload = new byte[SprogConstants.MAX_PACKET_LENGTH];
        payload[0] = 0;
        payload[1] = 0;
        payload[2] = 0;
        f0to4Packet = false;
        f5to8Packet = false;
        f9to12Packet = false;
        f13to20Packet = false;
        f21to28Packet = false;
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
    private final int slot;
    private boolean opsPkt;

    private boolean f0to4Packet;
    private boolean f5to8Packet;
    private boolean f9to12Packet;
    private boolean f13to20Packet;
    private boolean f21to28Packet;

    public boolean isF0to4Packet() {
        return f0to4Packet;
    }

    public boolean isF5to8Packet() {
        return f5to8Packet;
    }

    public boolean isF9to12Packet() {
        return f9to12Packet;
    }

    public boolean isF13to20Packet() {
        return f13to20Packet;
    }

    public boolean isF21to28Packet() {
        return f21to28Packet;
    }

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
    private boolean repeatF13 = false;
    private boolean repeatF14 = false;
    private boolean repeatF15 = false;
    private boolean repeatF16 = false;
    private boolean repeatF17 = false;
    private boolean repeatF18 = false;
    private boolean repeatF19 = false;
    private boolean repeatF20 = false;
    private boolean repeatF21 = false;
    private boolean repeatF22 = false;
    private boolean repeatF23 = false;
    private boolean repeatF24 = false;
    private boolean repeatF25 = false;
    private boolean repeatF26 = false;
    private boolean repeatF27 = false;
    private boolean repeatF28 = false;

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
        this.f5to8Packet = false;
        this.f9to12Packet = false;
        this.f13to20Packet = false;
        this.f21to28Packet = false;
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

        this.repeatF5 = !f5Momentary && f5;
        this.repeatF6 = !f6Momentary && f6;
        this.repeatF7 = !f7Momentary && f7;
        this.repeatF8 = !f8Momentary && f8;

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

        this.repeatF9 = !f9Momentary && f9;
        this.repeatF10 = !f10Momentary && f10;
        this.repeatF11 = !f11Momentary && f11;
        this.repeatF12 = !f12Momentary && f12;

        this.payload = jmri.NmraPacket.function9Through12Packet(address,
                isLongAddress,
                f9, f10, f11, f12);
        this.status = SprogConstants.SLOT_IN_USE;

    }

    public void f13to20packet(int address, boolean isLongAddress,
            boolean f13, boolean f13Momentary,
            boolean f14, boolean f14Momentary,
            boolean f15, boolean f15Momentary,
            boolean f16, boolean f16Momentary,
            boolean f17, boolean f17Momentary,
            boolean f18, boolean f18Momentary,
            boolean f19, boolean f19Momentary,
            boolean f20, boolean f20Momentary) {

        this.f13to20Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF13 && !f13)
                || (this.repeatF14 && !f14)
                || (this.repeatF15 && !f15)
                || (this.repeatF16 && !f16)
                || (this.repeatF17 && !f17)
                || (this.repeatF18 && !f18)
                || (this.repeatF19 && !f19)
                || (this.repeatF20 && !f20)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF13 = !f13Momentary && f13;
        this.repeatF14 = !f14Momentary && f14;
        this.repeatF15 = !f15Momentary && f15;
        this.repeatF16 = !f16Momentary && f16;
        this.repeatF17 = !f17Momentary && f17;
        this.repeatF18 = !f18Momentary && f18;
        this.repeatF19 = !f19Momentary && f19;
        this.repeatF20 = !f20Momentary && f20;

        this.payload = jmri.NmraPacket.function13Through20Packet(address,
                isLongAddress,
                f13, f14, f15, f16,
                f17, f18, f19, f20);
        this.status = SprogConstants.SLOT_IN_USE;
    }

    public void f21to28packet(int address, boolean isLongAddress,
            boolean f21, boolean f21Momentary,
            boolean f22, boolean f22Momentary,
            boolean f23, boolean f23Momentary,
            boolean f24, boolean f24Momentary,
            boolean f25, boolean f25Momentary,
            boolean f26, boolean f26Momentary,
            boolean f27, boolean f27Momentary,
            boolean f28, boolean f28Momentary) {

        this.f21to28Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF21 && !f21)
                || (this.repeatF22 && !f22)
                || (this.repeatF23 && !f23)
                || (this.repeatF24 && !f24)
                || (this.repeatF25 && !f25)
                || (this.repeatF26 && !f26)
                || (this.repeatF27 && !f27)
                || (this.repeatF28 && !f28)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF21 = !f21Momentary && f21;
        this.repeatF22 = !f22Momentary && f22;
        this.repeatF23 = !f23Momentary && f23;
        this.repeatF24 = !f24Momentary && f24;
        this.repeatF25 = !f25Momentary && f25;
        this.repeatF26 = !f26Momentary && f26;
        this.repeatF27 = !f27Momentary && f27;
        this.repeatF28 = !f28Momentary && f28;

        this.payload = jmri.NmraPacket.function21Through28Packet(address,
                isLongAddress,
                f21, f22, f23, f24,
                f25, f26, f27, f28);
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

        this.repeatF0 = !f0Momentary && f0;
        this.repeatF1 = !f1Momentary && f1;
        this.repeatF2 = !f2Momentary && f2;
        this.repeatF3 = !f3Momentary && f3;
        this.repeatF4 = !f4Momentary && f4;
        
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
        if (this.isF13to20Packet()) {
            if ((this.repeatF13 || this.repeatF14 || this.repeatF15 || this.repeatF16)
                    || (this.repeatF17 || this.repeatF18 || this.repeatF19 || this.repeatF20)) {
                return false;
            }
        }
        if (this.isF21to28Packet()) {
            if ((this.repeatF21 || this.repeatF22 || this.repeatF23 || this.repeatF24)
                    || (this.repeatF25 || this.repeatF26 || this.repeatF27 || this.repeatF28)) {
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
        f5to8Packet = false;
        f9to12Packet = false;
        f13to20Packet = false;
        f21to28Packet = false;
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
            log.debug("Slot {} repeats", slot);
            repeat--;
            if (repeat == 0) {
                log.debug("Clear slot {} due to repeats exhausted", slot);
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

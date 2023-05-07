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
        f29to36Packet = false;
        f37to44Packet = false;
        f45to52Packet = false;
        f53to60Packet = false;
        f61to68Packet = false;
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
    private boolean f29to36Packet;
    private boolean f37to44Packet;
    private boolean f45to52Packet;
    private boolean f53to60Packet;
    private boolean f61to68Packet;

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

    public boolean isF29to36Packet() {
        return f29to36Packet;
    }

    public boolean isF37to44Packet() {
        return f37to44Packet;
    }

    public boolean isF45to52Packet() {
        return f45to52Packet;
    }

    public boolean isF53to60Packet() {
        return f53to60Packet;
    }

    public boolean isF61to68Packet() {
        return f61to68Packet;
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
    private boolean repeatF29 = false;
    private boolean repeatF30 = false;
    private boolean repeatF31 = false;
    private boolean repeatF32 = false;
    private boolean repeatF33 = false;
    private boolean repeatF34 = false;
    private boolean repeatF35 = false;
    private boolean repeatF36 = false;
    private boolean repeatF37 = false;
    private boolean repeatF38 = false;
    private boolean repeatF39 = false;
    private boolean repeatF40 = false;
    private boolean repeatF41 = false;
    private boolean repeatF42 = false;
    private boolean repeatF43 = false;
    private boolean repeatF44 = false;
    private boolean repeatF45 = false;
    private boolean repeatF46 = false;
    private boolean repeatF47 = false;
    private boolean repeatF48 = false;
    private boolean repeatF49 = false;
    private boolean repeatF50 = false;
    private boolean repeatF51 = false;
    private boolean repeatF52 = false;
    private boolean repeatF53 = false;
    private boolean repeatF54 = false;
    private boolean repeatF55 = false;
    private boolean repeatF56 = false;
    private boolean repeatF57 = false;
    private boolean repeatF58 = false;
    private boolean repeatF59 = false;
    private boolean repeatF60 = false;
    private boolean repeatF61 = false;
    private boolean repeatF62 = false;
    private boolean repeatF63 = false;
    private boolean repeatF64 = false;
    private boolean repeatF65 = false;
    private boolean repeatF66 = false;
    private boolean repeatF67 = false;
    private boolean repeatF68 = false;

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
        this.f29to36Packet = false;
        this.f37to44Packet = false;
        this.f45to52Packet = false;
        this.f53to60Packet = false;
        this.f61to68Packet = false;
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

    public void f29to36packet(int address, boolean isLongAddress,
            boolean f29, boolean f29Momentary,
            boolean f30, boolean f30Momentary,
            boolean f31, boolean f31Momentary,
            boolean f32, boolean f32Momentary,
            boolean f33, boolean f33Momentary,
            boolean f34, boolean f34Momentary,
            boolean f35, boolean f35Momentary,
            boolean f36, boolean f36Momentary) {

        this.f29to36Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF29 && !f29)
                || (this.repeatF30 && !f30)
                || (this.repeatF31 && !f31)
                || (this.repeatF32 && !f32)
                || (this.repeatF33 && !f33)
                || (this.repeatF34 && !f34)
                || (this.repeatF35 && !f35)
                || (this.repeatF36 && !f36)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF29 = !f29Momentary && f29;
        this.repeatF30 = !f30Momentary && f30;
        this.repeatF31 = !f31Momentary && f31;
        this.repeatF32 = !f32Momentary && f32;
        this.repeatF33 = !f33Momentary && f33;
        this.repeatF34 = !f34Momentary && f34;
        this.repeatF35 = !f35Momentary && f35;
        this.repeatF36 = !f36Momentary && f36;

        this.payload = jmri.NmraPacket.function29Through36Packet(address,
                isLongAddress,
                f29, f30, f31, f32,
                f33, f34, f35, f36);
        this.status = SprogConstants.SLOT_IN_USE;
    }

    public void f37to44packet(int address, boolean isLongAddress,
            boolean f37, boolean f37Momentary,
            boolean f38, boolean f38Momentary,
            boolean f39, boolean f39Momentary,
            boolean f40, boolean f40Momentary,
            boolean f41, boolean f41Momentary,
            boolean f42, boolean f42Momentary,
            boolean f43, boolean f43Momentary,
            boolean f44, boolean f44Momentary) {

        this.f37to44Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF37 && !f37)
                || (this.repeatF38 && !f38)
                || (this.repeatF39 && !f39)
                || (this.repeatF40 && !f40)
                || (this.repeatF41 && !f41)
                || (this.repeatF42 && !f42)
                || (this.repeatF43 && !f43)
                || (this.repeatF44 && !f44)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF37 = !f37Momentary && f37;
        this.repeatF38 = !f38Momentary && f38;
        this.repeatF39 = !f39Momentary && f39;
        this.repeatF40 = !f40Momentary && f40;
        this.repeatF41 = !f41Momentary && f41;
        this.repeatF42 = !f42Momentary && f42;
        this.repeatF43 = !f43Momentary && f43;
        this.repeatF44 = !f44Momentary && f44;

        this.payload = jmri.NmraPacket.function37Through44Packet(address,
                isLongAddress,
                f37, f38, f39, f40,
                f41, f42, f43, f44);
        this.status = SprogConstants.SLOT_IN_USE;
    }

    public void f45to52packet(int address, boolean isLongAddress,
            boolean f45, boolean f45Momentary,
            boolean f46, boolean f46Momentary,
            boolean f47, boolean f47Momentary,
            boolean f48, boolean f48Momentary,
            boolean f49, boolean f49Momentary,
            boolean f50, boolean f50Momentary,
            boolean f51, boolean f51Momentary,
            boolean f52, boolean f52Momentary) {

        this.f45to52Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF45 && !f45)
                || (this.repeatF46 && !f46)
                || (this.repeatF47 && !f47)
                || (this.repeatF48 && !f48)
                || (this.repeatF49 && !f49)
                || (this.repeatF50&& !f50)
                || (this.repeatF51 && !f51)
                || (this.repeatF52 && !f52)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF45 = !f45Momentary && f45;
        this.repeatF46 = !f46Momentary && f46;
        this.repeatF47 = !f47Momentary && f47;
        this.repeatF48 = !f48Momentary && f48;
        this.repeatF49 = !f49Momentary && f49;
        this.repeatF50 = !f50Momentary && f50;
        this.repeatF51 = !f51Momentary && f51;
        this.repeatF52 = !f52Momentary && f52;

        this.payload = jmri.NmraPacket.function45Through52Packet(address,
                isLongAddress,
                f45, f46, f47, f48,
                f49, f50, f51, f52);
        this.status = SprogConstants.SLOT_IN_USE;
    }

    public void f53to60packet(int address, boolean isLongAddress,
            boolean f53, boolean f53Momentary,
            boolean f54, boolean f54Momentary,
            boolean f55, boolean f55Momentary,
            boolean f56, boolean f56Momentary,
            boolean f57, boolean f57Momentary,
            boolean f58, boolean f58Momentary,
            boolean f59, boolean f59Momentary,
            boolean f60, boolean f60Momentary) {

        this.f53to60Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF53 && !f53)
                || (this.repeatF54 && !f54)
                || (this.repeatF55 && !f55)
                || (this.repeatF56 && !f56)
                || (this.repeatF57 && !f57)
                || (this.repeatF58&& !f59)
                || (this.repeatF59 && !f59)
                || (this.repeatF60 && !f60)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF53 = !f53Momentary && f53;
        this.repeatF54 = !f54Momentary && f54;
        this.repeatF55 = !f55Momentary && f55;
        this.repeatF56 = !f56Momentary && f56;
        this.repeatF57 = !f57Momentary && f57;
        this.repeatF58 = !f58Momentary && f58;
        this.repeatF59 = !f59Momentary && f59;
        this.repeatF60 = !f60Momentary && f60;

        this.payload = jmri.NmraPacket.function53Through60Packet(address,
                isLongAddress,
                f53, f54, f55, f56,
                f57, f58, f59, f60);
        this.status = SprogConstants.SLOT_IN_USE;
    }

    public void f61to68packet(int address, boolean isLongAddress,
            boolean f61, boolean f61Momentary,
            boolean f62, boolean f62Momentary,
            boolean f63, boolean f63Momentary,
            boolean f64, boolean f64Momentary,
            boolean f65, boolean f65Momentary,
            boolean f66, boolean f66Momentary,
            boolean f67, boolean f67Momentary,
            boolean f68, boolean f68Momentary) {

        this.f61to68Packet = true;
        this.addr = address;
        this.isLong = isLongAddress;

        //Were we repeating any functions which we are now not?
        if ((this.repeatF61 && !f61)
                || (this.repeatF62 && !f62)
                || (this.repeatF63 && !f63)
                || (this.repeatF64 && !f64)
                || (this.repeatF65 && !f65)
                || (this.repeatF66&& !f66)
                || (this.repeatF67 && !f67)
                || (this.repeatF68 && !f68)) {
            this.repeat = 3; //Then repeat 3 times
        }

        this.repeatF61 = !f61Momentary && f61;
        this.repeatF62 = !f62Momentary && f62;
        this.repeatF63 = !f63Momentary && f63;
        this.repeatF64 = !f64Momentary && f64;
        this.repeatF65 = !f65Momentary && f65;
        this.repeatF66 = !f66Momentary && f66;
        this.repeatF67 = !f67Momentary && f67;
        this.repeatF68 = !f68Momentary && f68;

        this.payload = jmri.NmraPacket.function61Through68Packet(address,
                isLongAddress,
                f61, f62, f63, f64,
                f65, f66, f67, f68);
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
        f29to36Packet = false;
        f37to44Packet = false;
        f45to52Packet = false;
        f53to60Packet = false;
        f61to68Packet = false;
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

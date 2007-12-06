package jmri.jmrix.nce;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an NCE connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.  This is not the NCE system
 * standard, but is used as an expedient here.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 1.14 $
 */
public class NceThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public NceThrottle(DccLocoAddress address)
    {
        super();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        this.f0           = false;
        this.f1           = false;
        this.f2           = false;
        this.f3           = false;
        this.f4           = false;
        this.f5           = false;
        this.f6           = false;
        this.f7           = false;
        this.f8           = false;
        this.f9           = false;
        this.f10           = false;
        this.f11           = false;
        this.f12           = false;
        this.address      = address;
        this.isForward    = true;

    }

    DccLocoAddress address;

    public LocoAddress getLocoAddress() { return address; }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
			( f0 ? 0x10 : 0) |
			( f1 ? 0x01 : 0) |
			( f2 ? 0x02 : 0) |
			( f3 ? 0x04 : 0) |
			( f4 ? 0x08 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG1, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

			// This code can be eliminated once we confirm that the NCE 0xA2
			// commands work properly
		} else {
			byte[] result = jmri.NmraPacket.function0Through4Packet(address
					.getNumber(), address.isLongAddress(), getF0(), getF1(),
					getF2(), getF3(), getF4());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Send the message to set the state of functions F5, F6, F7, F8.
	 */
	protected void sendFunctionGroup2() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
			(f8 ? 0x08 : 0) |
			(f7 ? 0x04 : 0)	|
			(f6 ? 0x02 : 0) |
			(f5 ? 0x01 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG2, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

			// This code can be eliminated once we confirm that the NCE 0xA2
			// commands work properly
		} else {
			byte[] result = jmri.NmraPacket.function5Through8Packet(address
					.getNumber(), address.isLongAddress(), getF5(), getF6(),
					getF7(), getF8());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Send the message to set the state of functions F9, F10, F11, F12.
	 */
    protected void sendFunctionGroup3() {
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			
			int data = 0x00 |
            ( f12 ? 0x08 : 0) |
            ( f11 ? 0x04 : 0) |
            ( f10 ? 0x02 : 0) |
            ( f9  ? 0x01 : 0);
			
			byte[] bl = NceBinaryCommand.nceLocoCmd(locoAddr,
					NceBinaryCommand.LOCO_CMD_FG3, (byte) data);
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

			// This code can be eliminated once we confirm that the NCE 0xA2
			// commands work properly
		} else {
			byte[] result = jmri.NmraPacket.function9Through12Packet(address
					.getNumber(), address.isLongAddress(), getF9(), getF10(),
					getF11(), getF12());
			NceMessage m = NceMessage.sendPacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
	}

    /**
	 * Set the speed & direction.
	 * <P>
	 * 
	 * @param speed
	 *            Number from 0 to 1; less than zero is emergency stop
	 */
    public void setSpeedSetting(float speed) {
		this.speedSetting = speed;
		int value = (int) ((127 - 1) * speed); // -1 for rescale to avoid estop
		if (value > 0)
			value = value + 1; // skip estop
		if (value > 127)
			value = 127; // max possible speed
		
		// The NCE USB doesn't support the NMRA packet format
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			int locoAddr = address.getNumber();
			if (address.isLongAddress())
				locoAddr += 0xC000;
			byte[] bl;
			// speed or estop?
			if (value >= 0) {
				bl = NceBinaryCommand.nceLocoCmd(locoAddr,
						(isForward ? NceBinaryCommand.LOCO_CMD_FWD_128SPEED
								: NceBinaryCommand.LOCO_CMD_REV_128SPEED),
						(byte) value);
			// estop
			} else {
				bl = NceBinaryCommand.nceLocoCmd(locoAddr,
						(isForward ? NceBinaryCommand.LOCO_CMD_FWD_ESTOP
								: NceBinaryCommand.LOCO_CMD_REV_ESTOP),
						(byte) 0);
			}
			NceMessage m = NceMessage.createBinaryMessage(bl);
			NceTrafficController.instance().sendNceMessage(m, null);

		// This code can be eliminated once we confirm that the NCE 0xA2 commands work properly
		} else {
			if (value < 0)
				value = 1; // skip emergency stop
			byte[] result = jmri.NmraPacket.speedStep128Packet(address
					.getNumber(), address.isLongAddress(), value, isForward);
			NceMessage m = NceMessage.queuePacketMessage(result);
			NceTrafficController.instance().sendNceMessage(m, null);
		}
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    /**
     * Finished with this throttle.  Right now, this does nothing,
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
		// release is called twice when throttle frame closed and throttle
		// exists, therefore it is not a reportable error
		if (!active) {
			if (log.isDebugEnabled())
				log.warn("release called when not active");
		} else
			dispose();
	}

    /**
	 * Dispose when finished with this object. After this, further usage of this
	 * Throttle object will result in a JmriException.
	 */
    public void dispose() {
        log.debug("dispose");

        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceThrottle.class.getName());

}
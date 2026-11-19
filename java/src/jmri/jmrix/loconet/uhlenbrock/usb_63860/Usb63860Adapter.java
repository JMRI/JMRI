package jmri.jmrix.loconet.uhlenbrock.usb_63860;

import jmri.jmrix.loconet.uhlenbrock.UhlenbrockConnectionTypeList;
import jmri.jmrix.loconet.uhlenbrock.usb_63120.UsbUhlenbrock63120Adapter;

/**
 * Adapter for the USB side of the Uhlenbrock "LocoNet-USB und WLAN Interface",
 * part no. 63860.
 * <p>
 * The 63860 shows up as a virtual serial port (Silicon Labs CP210x USB to UART
 * bridge; driver built into Windows 10/11, macOS and Linux). It passes raw
 * LocoNet messages and echoes every message it sends, using the same PC to
 * LocoNet communication rules as the older 63120 USB adapter:
 * <ul>
 * <li>Send one message, then wait until it has been received back before
 * sending the next one; process other messages received in the meantime.
 * <li>If the next message after the echo is a LACK, it belongs to the
 * message just sent.
 * </ul>
 * This is exactly what {@link jmri.jmrix.loconet.LnPacketizerStrict} does, so
 * that packetizer is the default for this adapter.
 * <p>
 * The 63860 has no hardware handshake: its CTS line is never asserted, so
 * hardware flow control would block all output. Flow control is therefore
 * always off and not offered as an option.
 * <p>
 * Supported baud rates (interface LNCV 2): 19200, 38400, 57600, 115200
 * (factory default 115200).
 *
 * @author detami01 Copyright (C) 2026
 * @see jmri.jmrix.loconet.uhlenbrock.usb_63120.UsbUhlenbrock63120Adapter
 */
public class Usb63860Adapter extends UsbUhlenbrock63120Adapter {

    public Usb63860Adapter() {
        super();
        setManufacturer(UhlenbrockConnectionTypeList.UHLEN);

        // No flow control choice: the CP210x of the 63860 never asserts CTS
        // (confirmed on hardware), so hardware flow control would block
        // all output. Flow control is always off, see setLocalFlowControl().
        options.remove(option1Name);

        // Packetizer: default to Strict, which implements the
        // send / wait-for-echo / LACK handling required by Uhlenbrock.
        String[] packetizerNames = new String[]{
                Bundle.getMessage("PacketizerTypelnPacketizer"),
                Bundle.getMessage("PacketizerTypelnPacketizerStrict")};
        options.put(option4Name, new Option(Bundle.getMessage("PacketizerTypeLabel"),
                packetizerNames, true, Option.Type.JCOMBOBOX, packetizerNames[1]));
    }

    /**
     * The 63860 has no hardware handshake, so never use flow control.
     */
    @Override
    protected void setLocalFlowControl() {
        setFlowControl(currentSerialPort, FlowControl.NONE);
    }

    /**
     * CTS is not connected on the 63860, so do not let it block transmission.
     * {@inheritDoc}
     */
    @Override
    public boolean okToSend() {
        return true;
    }

    @Override
    protected void reportOpen(String portName) {
        log.info("Connecting Uhlenbrock 63860 (USB) via {} {}", portName, currentSerialPort);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Usb63860Adapter.class);

}

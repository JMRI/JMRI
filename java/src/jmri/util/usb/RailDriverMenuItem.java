package jmri.util.usb;

import static jmri.server.json.JSON.NULL;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.swing.JMenuItem;
import javax.usb.UsbDevice;
import jmri.InstanceManager;
import jmri.jmrit.throttle.AddressPanel;
import jmri.jmrit.throttle.ControlPanel;
import jmri.jmrit.throttle.FunctionPanel;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.ThrottleWindow;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RailDriver support
 * <p>
 * @author George Warner Copyright (C) 2017
 */
public class RailDriverMenuItem extends JMenuItem
        implements HidServicesListener, PropertyChangeListener {

    private static final short VENDOR_ID = 0x05F3;
    private static final short PRODUCT_ID = 0x00D2;
    private static final int PACKET_LENGTH = 64;
    public static final String SERIAL_NUMBER = null;

    private UsbDevice usbDevice = null;
    private HidDevice hidDevice = null;

    public RailDriverMenuItem(String name) {
        this();
        setText(name);
    }

    public RailDriverMenuItem() {

        super();

        //TODO: remove " (build in)" if/when this replaces Raildriver script
        setText("RailDriver (built in)");

        try {
            HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
            hidServicesSpecification.setAutoShutdown(true);
            hidServicesSpecification.setScanInterval(500);
            hidServicesSpecification.setPauseInterval(5000);
            hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

            // Get HID services using custom specification
            HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
            hidServices.addHidServicesListener(RailDriverMenuItem.this);

            log.debug("Starting HID services.");
            hidServices.start();

            // Provide a list of attached devices
            //log.info("Enumerating attached devices...");
            //for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            //    log.info(hidDevice.toString());
            //}
            //
            // Open the device device by Vendor ID, Product ID and serial number
            HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
            if (hidDevice != null) {
                log.info("Got RailDriver hidDevice: " + hidDevice);
                // Consider overriding dropReportIdZero on Windows
                // if you see "The parameter is incorrect"
                // HidApi.dropReportIdZero = true;
                setupRailDriver(hidDevice);
            }
        } catch (HidException ex) {
            log.error("HidException: {}", ex);
        }
    }

    private Thread thread = null;
    private ThrottleWindow throttleWindow = null;
    private ThrottleFrame activeThrottleFrame = null;
    private ControlPanel controlPanel = null;
    private FunctionPanel functionPanel = null;
    private AddressPanel addressPanel = null;
    private int numThrottles = 0;
    private int numControllers = 0;

    private void setupRailDriver(HidDevice hidDevice) {
        this.hidDevice = hidDevice;
        if (hidDevice != null) {
            setLEDs("Pro");
            speakerOn();

            testRailDriver(false);  // set true to test RailDriver functions

            //# open a throttle window and get components
            ThrottleFrameManager tfManager = InstanceManager.getDefault(ThrottleFrameManager.class);
            throttleWindow = tfManager.createThrottleWindow();
            activeThrottleFrame = throttleWindow.addThrottleFrame();
            //# move throttle on screen so multiple throttles don't overlay each other
            throttleWindow.setLocation(400 * numThrottles, 50 * numThrottles);
            numThrottles++;
            numControllers++;
            activeThrottleFrame.toFront();
            controlPanel = activeThrottleFrame.getControlPanel();
            functionPanel = activeThrottleFrame.getFunctionPanel();
            addressPanel = activeThrottleFrame.getAddressPanel();
            throttleWindow.addPropertyChangeListener(this);
            activeThrottleFrame.addPropertyChangeListener(this);

            if (thread != null) {
                thread.interrupt();
                try {
                    thread.join(3);
                } catch (InterruptedException ex) {
                    log.debug("InterruptedException : {}", ex);
                }
            }
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buff_old = new byte[14];	// read buffer
                    Arrays.fill(buff_old, (byte) 0);

                    while (!thread.isInterrupted()) {
                        byte[] buff_new = new byte[14];	// read buffer
                        int ret = hidDevice.read(buff_new);
                        if (ret >= 0) {
                            //log.info("hidDevice.read: " + buff_new);
                            // DO STUFF HERE!
                            for (int i = 0; i < buff_new.length; i++) {
                                if (buff_old[i] != buff_new[i]) {
                                    log.info("buff[{}] = {}", i, String.format("0x%02X", buff_new[i]));
                                    buff_old[i] = buff_new[i];
                                }
                            }
                        } else {
                            String error = hidDevice.getLastErrorMessage();
                            if (error != null) {
                                log.error("hidDevice.read error: " + error);
                            }
                        }
                    }
                }
            });
            thread.setName("RailDriver");
            thread.start();
        }
    }

    private void testRailDriver(boolean testFlag) {
        if (testFlag) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //
                    // this is here for testing the SevenSegmentAlpha (LED display)
                    //
                    for (int pass = 0; pass < 3; pass++) {
                        for (char c = 'A'; c < 'Z'; c++) {
                            String s = "";
                            for (int i = 0; i < 3; i++) {
                                char ci = (char) (c + i);
                                ci = (char) (((ci - 'A') % 26) + 'A');
                                s += ci;
                                if (0 == ci % 3) {
                                    s += '.';
                                }
                            }
                            setLEDs(s);
                            sleep(0.25);
                        }
                    }

                    sendString("The quick brown fox jumps over the lazy dog.", 0.250);
                    sleep(2.0);

                    setLEDs("8.8.8.");
                    sleep(2.0);

                    setLEDs("???");
                    sleep(3.0);

                    setLEDs("Pro");
                }
            }).start();
        }
    }

    /**
     * send a string to the LED display (asychroniously)
     *
     * @param string what to send
     * @param delay  how much to delay before shifting in next character
     */
    public void sendStringAsync(String string, double delay) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendString(string, delay);
            }
        }).start();
    }

    /**
     * send a string to the LED display
     *
     * @param string what to send
     * @param delay  how much to delay before shifting in next character
     */
    public void sendString(String string, double delay) {
        for (int i = 0; i < string.length(); i++) {
            String ledstring = "";
            int maxJ = 3;
            for (int j = 0; j < maxJ; j++) {
                if (i + j < string.length()) {
                    char c = string.charAt(i + j);
                    ledstring += c;
                    if (c == '.') {
                        maxJ++;
                    }
                } else {
                    break;
                }
            }
            setLEDs(ledstring);
            sleep(delay);
        }
    }

    private void sleep(double delay) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (delay * 1000.0));
        } catch (InterruptedException ex) {
            log.debug("TimeUnit.sleep InterruptedException: " + ex);
        }
    }

    //
    // constants used to talk to RailDriver
    //
    // these are the report ID's
    private final byte LEDCommand = (byte) 134;			// Command code to set the LEDs.
    private final byte SpeakerCommand = (byte) 133;		// Command code to set the speaker state.

    // Seven segment lookup table for digits ('0' thru '9')
    private final byte SevenSegment[] = {
        //'0'   '1'   '2'   '3'   '4'   '5'   '6'   '7'   '8'   '9'
        0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07, 0x7f, 0x6f};

    // Seven segment lookup table for alphas ('A' thru 'Z')
    private final byte SevenSegmentAlpha[] = {
        //'A'   'b'   'C'   'd'   'E'   'F'   'g'   'H'   'i'   'J'   
        0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71, 0x6F, 0x76, 0x04, 0x1E,
        //'K'   'L'   'm'   'n'   'o'   'P'   'q'   'r'   's'   't'   
        0x70, 0x38, 0x54, 0x23, 0x5C, 0x73, 0x67, 0x50, 0x6D, 0x44,
        //'u'   'v'   'W'   'X'   'y'   'z'
        0x1C, 0x62, 0x14, 0x36, 0x72, 0x49
    };

    // other seven segment display patterns
    private final byte BLANKSEGMENT = 0x00;
    private final byte QUESTIONMARK = 0x53;
    private final byte DASHSEGMENT = 0x40;
    private final byte DPSEGMENT = (byte) 0x80;

    // Set the LEDS.
    public void setLEDs(String ledstring) {
        byte[] buff = new byte[7];	// Segment buffer.
        Arrays.fill(buff, (byte) 0);

        if (ledstring == NULL) {
            ledstring = "---";
        }
        int outIdx = 2;
        for (int i = 0; i < ledstring.length(); i++) {
            char c = ledstring.charAt(i);
            if (Character.isDigit(c)) {
                //log.info("buff[{}] = {}", outIdx, "" + c);
                // Get seven segment code for digit.
                buff[outIdx] = SevenSegment[c - '0'];
            } else if (Character.isWhitespace(c)) {
                buff[outIdx] = BLANKSEGMENT;
            } else if (c == '_') {
                buff[outIdx] = BLANKSEGMENT;
            } else if (c == '?') {
                buff[outIdx] = QUESTIONMARK;
            } else if ((c >= 'A') && (c <= 'Z')) {
                // Get seven segment code for alpha.
                buff[outIdx] = SevenSegmentAlpha[c - 'A'];
            } else if ((c >= 'a') && (c <= 'z')) {
                // Get seven segment code for alpha.
                buff[outIdx] = SevenSegmentAlpha[c - 'a'];
            } else if (c == '-') {
                buff[outIdx] = DASHSEGMENT;
            } else // Is it a decimal point?
            if (c == '.') {
                // If so, OR in the decimal point segment.
                buff[outIdx + 1] |= DPSEGMENT;
                outIdx++;
            } else {    // everything else is ignored
                outIdx++;
            }
            outIdx--;
            if (outIdx < 0) {
                if (++i < ledstring.length()) {
                    if (ledstring.charAt(i) == '.') {
                        buff[0] |= DPSEGMENT;
                    }
                }
                break;
            }
        }
        sendMessage(hidDevice, buff, LEDCommand);
    }   // setLEDs

    public void setSpeakerOn(boolean onFlag) {
        byte[] buff = new byte[7];	// data buffer
        Arrays.fill(buff, (byte) 0);

        buff[5] = (byte) (onFlag ? 1 : 0);      // On / off

        sendMessage(hidDevice, buff, SpeakerCommand);
    }   // setSpeakerOn

    // Turn speaker on.
    public void speakerOn() {
        setSpeakerOn(true);
    }

    // Turn speaker off.
    public void speakerOff() {
        setSpeakerOn(false);
    }

    /**
     * send message to hid device {p}
     * <p>
     * @param hidDevice the hid device to send the message to
     * @param message   the message to send
     * @param reportID  the report ID
     */
    private void sendMessage(HidDevice hidDevice, byte[] message, byte reportID) {
        // Ensure device is open after an attach/detach event
        if (!hidDevice.isOpen()) {
            hidDevice.open();
        }

        int ret = hidDevice.write(message, message.length, (byte) reportID);
        if (ret >= 0) {
            log.debug("hidDevice.write returned: " + ret);
        } else {
            log.error("hidDevice.write error: " + hidDevice.getLastErrorMessage());
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        log.info("{}", event);
        HidDevice tHidDevice = event.getHidDevice();
        if (tHidDevice.getVendorId() == VENDOR_ID) {
            if (tHidDevice.getProductId() == PRODUCT_ID) {
                if ((SERIAL_NUMBER == null) || (tHidDevice.getSerialNumber().equals(SERIAL_NUMBER))) {
                    setupRailDriver(tHidDevice);
                }
            }
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        log.info("{}", event);
        if (hidDevice == event.getHidDevice()) {
            hidDevice = null;
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void hidFailure(HidServicesEvent event) {
        log.info("{}", event);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        log.info("{}", event);
        if (event.getPropertyName().equals("ancestor")) {
            //ancestor property change - closing throttle window
            // Remove all property change listeners and
            // dereference all throttle components
            activeThrottleFrame.removePropertyChangeListener(this);
            throttleWindow.removePropertyChangeListener(this);
            activeThrottleFrame = null;
            controlPanel = null;
            functionPanel = null;
            addressPanel = null;
            throttleWindow = null;
            // Now remove this propertyChangeListener from the model
            //global model
            //model.removePropertyChangeListener(self)
        } else if (event.getPropertyName().equals("ThrottleFrame")) {
            //Current throttle frame changed
            Object object = event.getNewValue();
            //log.debug("event.newValue(): " + object);
            if (object == null) {
                activeThrottleFrame = null;
                controlPanel = null;
                functionPanel = null;
                addressPanel = null;
            } else if (object instanceof ThrottleFrame) {
                activeThrottleFrame = (ThrottleFrame) object;
                addressPanel = activeThrottleFrame.getAddressPanel();
                controlPanel = activeThrottleFrame.getControlPanel();
                functionPanel = activeThrottleFrame.getFunctionPanel();
            }
        } else if (event.getPropertyName().equals("Value")) {
            // event.oldValue is the UsbNode
            // 
            //  uncomment the following line to see controller names
            Object obj = event.getOldValue();
            log.info("obj: " + obj);
            //log.info("|" + obj.getController().toString() + "|");
            //log.info(obj.getController().getName());
            //log.info(obj.getController().hashCode());
            //if (obj instanceof UsbNode) {
            //}
        }
    }

    //initialize logging
    private transient final static Logger log
            = LoggerFactory.getLogger(RailDriverMenuItem.class);
}

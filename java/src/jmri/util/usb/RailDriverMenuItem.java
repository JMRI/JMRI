package jmri.util.usb;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.usb.UsbDevice;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.throttle.AddressPanel;
import jmri.jmrit.throttle.ControlPanel;
import jmri.jmrit.throttle.FunctionButton;
import jmri.jmrit.throttle.FunctionPanel;
import jmri.jmrit.throttle.LoadXmlThrottlesLayoutAction;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.ThrottleWindow;
import jmri.util.MathUtil;
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

    private HidServices hidServices = null;

    private UsbDevice usbDevice = null;
    private HidDevice hidDevice = null;

    public RailDriverMenuItem(String name) {
        this();
        setText(name);
    }

    public RailDriverMenuItem() {

        super();

        //TODO: remove " (build in)" if/when this replaces Raildriver script
        setText("RailDriver Throttle (built in)");

        try {
            HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
            hidServicesSpecification.setAutoShutdown(true);
            hidServicesSpecification.setScanInterval(500);
            hidServicesSpecification.setPauseInterval(5000);
            hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

            // Get HID services using custom specification
            hidServices = HidManager.getHidServices(hidServicesSpecification);
            hidServices.addHidServicesListener(RailDriverMenuItem.this);

            log.debug("Starting HID services.");
            hidServices.start();

            // Provide a list of attached devices
            //log.info("Enumerating attached devices...");
            //for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            //    log.info(hidDevice.toString());
            //}
            //
            if (!invokeOnMenuOnly) {
                // Open the device device by Vendor ID, Product ID and serial number
                HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
                if (hidDevice != null) {
                    log.info("Got RailDriver hidDevice: " + hidDevice);
                    // Consider overriding dropReportIdZero on Windows
                    // if you see "The parameter is incorrect"
                    // HidApi.dropReportIdZero = true;
                    setupRailDriver(hidDevice);
                }
            }
        } catch (HidException ex) {
            log.error("HidException: {}", ex);
        }

        addActionListener((ActionEvent e) -> {
            log.info("RailDriverMenuItem Action!");
            // Open the device device by Vendor ID, Product ID and serial number
            HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
            if (hidDevice != null) {
                log.info("Got RailDriver hidDevice: " + hidDevice);
                // Consider overriding dropReportIdZero on Windows
                // if you see "The parameter is incorrect"
                // HidApi.dropReportIdZero = true;
                setupRailDriver(hidDevice);
            }
        });
    }

    //TODO: Remove this if/when the RailDriver script is removed
    private boolean invokeOnMenuOnly = true;

    private Thread thread = null;
    private ThrottleWindow throttleWindow = null;
    private ThrottleFrame activeThrottleFrame = null;
    private ControlPanel controlPanel = null;
    private FunctionPanel functionPanel = null;
    private AddressPanel addressPanel = null;

    private void setupRailDriver(HidDevice hidDevice) {
        this.hidDevice = hidDevice;
        if (hidDevice != null) {
            setLEDs("Pro");
            speakerOn();

            testRailDriver(false);  // set true to test RailDriver functions

            ThrottleFrameManager tfManager = InstanceManager.getDefault(ThrottleFrameManager.class);

            // if there's no active throttle frame
            if (activeThrottleFrame == null) {
                // we're going to try to open the default throttles layout
                try {
                    LoadXmlThrottlesLayoutAction lxta = new LoadXmlThrottlesLayoutAction();
                    if (!lxta.loadThrottlesLayout(new File(ThrottleFrame.getDefaultThrottleFilename()))) {
                        // if there's no default throttle layout...
                        // throw this exception so we'll create a new throttle window
                        throw new IOException();
                    }
                } catch (IOException ex) {
                    //log.debug("No default throttle layout, creating an empty throttle window");
                    // open a new throttle window and get its components
                    throttleWindow = tfManager.createThrottleWindow();
                    activeThrottleFrame = throttleWindow.addThrottleFrame();
                }
                // move throttle on screen so multiple throttles don't overlay each other
                //throttleWindow.setLocation(400 * numThrottles, 50 * numThrottles);
            }

            // since LoadXmlThrottlesLayoutAction uses an invokeLater to 
            // open the default throttles layout then we have to delay our
            // actions here until after that one is done.
            SwingUtilities.invokeLater(() -> {
                if (activeThrottleFrame == null) {
                    throttleWindow = tfManager.getCurrentThrottleFrame();
                    if (throttleWindow != null) {
                        activeThrottleFrame = throttleWindow.getCurrentThrottleFrame();
                    }
                }
                if (activeThrottleFrame != null) {
                    activeThrottleFrame.toFront();
                    controlPanel = activeThrottleFrame.getControlPanel();
                    functionPanel = activeThrottleFrame.getFunctionPanel();
                    addressPanel = activeThrottleFrame.getAddressPanel();

                    throttleWindow.addPropertyChangeListener(this);
                    activeThrottleFrame.addPropertyChangeListener(this);
                }
            });

            addPropertyChangeListener(this);

            // if I already have a thread running
            if (thread != null) {
                // interrupt it
                thread.interrupt();
                try {
                    // wait (500 mSec) for it to die
                    thread.join(500);
                } catch (InterruptedException ex) {
                    log.debug("InterruptedException : {}", ex);
                }
            }
            // start a new thread
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buff_old = new byte[14];	// read buffer
                    Arrays.fill(buff_old, (byte) 0);

                    while (!thread.isInterrupted()) {
                        byte[] buff_new = new byte[14];	// read buffer
                        int ret = hidDevice.read(buff_new);
                        if (ret >= 0) {
                            //log.debug("hidDevice.read: " + buff_new);
                            for (int i = 0; i < buff_new.length; i++) {
                                if (buff_old[i] != buff_new[i]) {
                                    //log.info("buff[{}] = {}", i, String.format("0x%02X", buff_new[i]));
                                    firePropertyChange("Value", "" + i, "" + buff_new[i]);
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
     * send a string to the LED display (asynchronously)
     *
     * @param string what to send
     * @param delay  how much to delay before shifting in next character
     */
    public void sendStringAsync(@Nonnull String string, double delay) {
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
    public void sendString(@Nonnull String string, double delay) {
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
    public void setLEDs(@Nonnull String ledstring) {
        byte[] buff = new byte[7];	// Segment buffer.
        Arrays.fill(buff, (byte) 0);

        int outIdx = 2;
        for (int i = 0; i < ledstring.length(); i++) {
            char c = ledstring.charAt(i);
            if (Character.isDigit(c)) {
                //log.debug("buff[{}] = {}", outIdx, "" + c);
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

        try {
            int ret = hidDevice.write(message, message.length, (byte) reportID);
            if (ret >= 0) {
                log.debug("hidDevice.write returned: " + ret);
            } else {
                log.error("hidDevice.write error: " + hidDevice.getLastErrorMessage());
            }
        } catch (IllegalStateException ex) {
            log.error("hidDevice.write Exception : " + ex);
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        log.info("hidDeviceAttached({})", event);
        HidDevice tHidDevice = event.getHidDevice();
        if (tHidDevice.getVendorId() == VENDOR_ID) {
            if (tHidDevice.getProductId() == PRODUCT_ID) {
                if ((SERIAL_NUMBER == null) || (tHidDevice.getSerialNumber().equals(SERIAL_NUMBER))) {
                    if (!invokeOnMenuOnly) {
                        setupRailDriver(tHidDevice);
                    }
                }
            }
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        log.info("hidDeviceDetached({})", event);
        if (hidDevice == event.getHidDevice()) {
            hidDevice = null;
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void hidFailure(HidServicesEvent event) {
        log.warn("hidFailure({})", event);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // log.debug("{}", event);
        if (event.getPropertyName().equals("ancestor")) {
            //ancestor property change - closing throttle window
            // Remove all property change listeners and
            // dereference all throttle components
            if (throttleWindow != null) {
                throttleWindow.removePropertyChangeListener(this);
                throttleWindow = null;
            }
            if (activeThrottleFrame != null) {
                activeThrottleFrame.removePropertyChangeListener(this);
                activeThrottleFrame = null;
            }
            controlPanel = null;
            functionPanel = null;
            addressPanel = null;
            // Now remove this propertyChangeListener from the model
            //global model
            //model.removePropertyChangeListener(self)
        } else if (event.getPropertyName().equals("ThrottleFrame")) {
            //Current throttle frame changed
            Object object = event.getNewValue();
            //log.debug("event.newValue(): " + object);
            if (object == null) {
                if (activeThrottleFrame != null) {
                    activeThrottleFrame.removePropertyChangeListener(this);
                    activeThrottleFrame = null;
                }
                controlPanel = null;
                functionPanel = null;
                addressPanel = null;
            } else if (object instanceof ThrottleFrame) {

                if (throttleWindow != null) {
                    throttleWindow.removePropertyChangeListener(this);
                    throttleWindow = null;
                }
                if (activeThrottleFrame != null) {
                    activeThrottleFrame.removePropertyChangeListener(this);
                    activeThrottleFrame = null;
                }

                activeThrottleFrame = (ThrottleFrame) object;
                throttleWindow = activeThrottleFrame.getThrottleWindow();

                throttleWindow.addPropertyChangeListener(this);
                activeThrottleFrame.addPropertyChangeListener(this);

                addressPanel = activeThrottleFrame.getAddressPanel();
                controlPanel = activeThrottleFrame.getControlPanel();
                functionPanel = activeThrottleFrame.getFunctionPanel();
            }
        } else if (event.getPropertyName().equals("Value")) {
            String oldValue = event.getOldValue().toString();
            String newValue = event.getNewValue().toString();
            //log.info("propertyChange \"Value\" old: " + oldValue + ", new: " + newValue);
            int index = -1;
            try {
                index = Integer.parseInt(oldValue);
            } catch (NumberFormatException ex) {
                log.error("RailDriver parse property old value ('{}') exception: {}", oldValue, ex);
            }
            int value = -1;
            try {
                value = Integer.parseInt(newValue);
            } catch (NumberFormatException ex) {
                log.error("RailDriver parse property new value ('{}') exception: {}", newValue, ex);
            }
            byte vByte = (byte) value;
            int vInt = 0xFF & (int) vByte;

            int fNum = -1;
            switch (index) {
                case 0: {
                    // REVERSER is the state of the reverser lever, values (much) less
                    // than 128 are forward, a value at or near 128 is neutral and values
                    // (much) greater than 128 are reverse.
                    log.debug("REVERSER value: {}", value);
                    if ((controlPanel != null) && controlPanel.isEnabled()) {
                        if (vInt < 120) {
                            controlPanel.setForwardDirection(true);
                        } else if (vInt > 136) {
                            controlPanel.setForwardDirection(false);
                        }
                    }
                    break;
                }
                case 1: {
                    // THROTTLE is the state of the Throttle (and dynamic brake).  Values
                    // (much) greater than 128 are for throttle (maximum throttle is are
                    // values close to 255), values near 128 are at the center position
                    // (idle/coasting), and values (much) less than 128 are for dynamic
                    // braking, with values aproaching 0 for full dynamic braking.
                    log.debug("THROTTLE value: " + value);

                    if (controlPanel != null) {
                        JSlider slider = controlPanel.getSpeedSlider();
                        if ((slider != null) && slider.isEnabled()) {
                            // lever front is negative (0xD0 to 0x80)
                            // back is positive (0x7F to 0x50)
                            // limit range to only positive side of lever
                            int throttle_min = 0x30;
                            int throttle_max = 0x78;
                            int v = MathUtil.pin(throttle_max - value, throttle_min, throttle_max);
                            // compute fraction (0.0 to 1.0)
                            double fraction = (v - throttle_min) / ((double) throttle_max - throttle_min);
                            fraction = (value < 0) ? 0.0 : fraction;
                            // convert fraction to slider setting
                            int setting = (int) (fraction * (slider.getMaximum() - slider.getMinimum()));
                            slider.setValue(setting);

                            if (value < 0) {
                                //TODO: dynamic braking
                                setLEDs("DBr");
                            } else {
                                String speed = String.format("%03d", setting);
                                //log.debug("••••    speed: " + speed);
                                setLEDs(speed);
                            }
                        }
                    }
                    break;
                }
                case 2: {
                    // AUTOBRAKE is the state of the Automatic (trainline) brake.  Large
                    // values for no braking, small values for more braking.
                    log.debug("AUTOBRAKE value: " + vInt);
                    break;
                }
                case 3: {
                    // INDEPENDBRK is the state of the Independent (engine only) brake.
                    // Like the Automatic brake: large values for no braking, small
                    // values for more braking.
                    log.debug("INDEPENDBRK value: " + vInt);
                    break;
                }
                case 4: {
                    // BAILOFF is the Independent brake 'bailoff', this is the spring
                    // loaded right movement of the Independent brake lever.  Larger
                    // values mean the lever has been shifted right.
                    log.debug("BAILOFF value: " + vInt);
                    break;
                }
                case 5: {
                    // HEADLIGHT is the state of the headlight switch.  A value below 128
                    // is off, a value near 128 is dim, and a number much larger than 128
                    // is full. This is an analog input w/detents, not a switch!
                    log.debug("HEADLIGHT value: " + vInt);
                    break;
                }
                case 6: {
                    // WIPER is the state of the wiper switch.  Much like the headlight
                    // switch, this is also an analog input w/detents, not a switch!
                    // Small values (much less than 128) are off, values near 128 are
                    // slow, and larger values are full.
                    log.debug("WIPER value: " + vInt);
                    break;
                }
                case 7:
                // DIGITAL1 is the leftmost eight blue buttons in the top row, BB1,
                // BB2 BB3, BB4, BB5, BB6, BB7, and BB8.
                case 8:
                // DIGITAL2 is the rightmost six blue buttons in the top row and the
                // leftmost two buttons in the bottom row, BB9, BB10, BB11, BB12,
                // BB13, BB14, BB15, and BB16.
                case 9:
                // DIGITAL3 is the eight buttons on the bottom row, starting with the
                // third from the left, BB17, BB18, BB19, BB20, BB21, BB22, BB23, and
                // BB24. 
                case 10:
                // DIGITAL4 is the rightmost four buttons on the bottom row, plus the
                // zoom up and zoom down, plus the pan right and pan up buttons, named
                // BB25, BB26, BB27, BB28, Zoom Up, Zoom Down, Pan Right, and Pan Up.
                case 11:
                // DIGITAL5 is the pan left and pan down, range up and range down, and
                // E-Stop up and E-Stop down switches, named Pan Left, Pan Down,
                // Range Up, Range Down, Emergency Brake Up, Emergency Brake Down.
                case 12: {
                    // DIGITAL6 is the whistle up and whistle down, Alert, Sand, P
                    // (Pantograph), and Bell buttons, named Whistle Up, Whistle Down,
                    // Alert, Sand, Pantograph, and Bell.
                    if (functionPanel != null) {
                        FunctionButton[] functionButtons = functionPanel.getFunctionButtons();
                        for (int bit = 0; bit < 8; bit++) {
                            boolean isDown = (0 != (vByte & (1 << bit)));
                            fNum = ((index - 7) * 8) + bit;
                            String ledString = String.format("F%d", fNum + 1);

                            switch (fNum) {
                                case 28: {  // zoom/rocker button up
                                    if ((addressPanel != null) && isDown) {
                                        addressPanel.selectRosterEntry();
                                        DccLocoAddress a = addressPanel.getCurrentAddress();
                                        ledString = "sel " + ((a != null) ? a.toString() : "null");
                                    }
                                    break;
                                }
                                case 29: {  // zoom/rocker button down
                                    if ((addressPanel != null) && isDown) {
                                        addressPanel.dispatchAddress();
                                        DccLocoAddress a = addressPanel.getCurrentAddress();
                                        ledString = "dis " + ((a != null) ? a.toString() : "null");
                                    }
                                    break;
                                }
                                case 30: {  // four way panning up
                                    if ((addressPanel != null) && isDown) {
                                        int selectedIndex = addressPanel.getRosterSelectedIndex();
                                        if (selectedIndex > 1) {
                                            addressPanel.setRosterSelectedIndex(selectedIndex - 1);
                                            ledString = String.format("Prev %d", selectedIndex - 1);
                                        }
                                    }
                                    break;
                                }
                                case 31: {  // four way panning right
                                    if (isDown) {
                                        if (throttleWindow != null) {
                                            throttleWindow.nextThrottleFrame();
                                        }
                                        ledString = "NXT";
                                    }
                                    break;
                                }
                                case 32: {  // four way panning down
                                    if ((addressPanel != null) && isDown) {
                                        RosterEntrySelectorPanel resp = addressPanel.getRosterEntrySelector();
                                        if (resp != null) {
                                            RosterEntryComboBox recb = resp.getRosterEntryComboBox();
                                            if (recb != null) {
                                                int cnt = recb.getItemCount();
                                                int selectedIndex = addressPanel.getRosterSelectedIndex();
                                                if (selectedIndex + 1 < cnt) {
                                                    try {
                                                        addressPanel.setRosterSelectedIndex(selectedIndex + 1);
                                                        ledString = String.format("Next %d", selectedIndex + 1);
                                                    } catch (ArrayIndexOutOfBoundsException ex) {
                                                        // ignore this
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                                case 33: {  // four way panning left
                                    if (isDown) {
                                        if (throttleWindow != null) {
                                            throttleWindow.previousThrottleFrame();
                                        }
                                        ledString = "PRE";
                                    }
                                    break;
                                }
                                case 34: {  // Gear Shift Up
                                    if (isDown) {
                                        // shuntFn
                                        functionButtons[3].changeState(false);
                                    }
                                    break;
                                }
                                case 35: {  // Gear Shift Down
                                    if (isDown) {
                                        // shuntFn
                                        functionButtons[3].changeState(true);
                                    }
                                    break;
                                }
                                case 36:
                                case 37: {  // Emergency Brake up/down
                                    if ((controlPanel != null) && isDown) {
                                        controlPanel.stop();
                                    }
                                    break;
                                }

                                case 38: {  // Alerter
                                    if (isDown) {
                                        fNum = 6;   // alertFn
                                    }
                                    break;
                                }
                                case 39: {  // Sander
                                    if (isDown) {
                                        fNum = 7;   // sandFn
                                    }
                                    break;
                                }
                                case 40: {  // Pantograph
                                    if (isDown) {
                                        fNum = 8;   // pantoFn
                                    }
                                    break;
                                }
                                case 41: {  // Bell
                                    if (isDown) {
                                        fNum = 1;   // bellFn
                                    }
                                    break;
                                }
                                case 42:
                                case 43: {  // Horn/Whistle
                                    fNum = 2;   // hornFn
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                            if (fNum < functionButtons.length) {
                                FunctionButton button = functionButtons[fNum];
                                if (button != null) {
                                    if (button.getIsLockable()) {
                                        if (isDown) {
                                            button.changeState(!button.getState());
                                        }
                                    } else {
                                        button.changeState(isDown);
                                    }
                                }
                            }
                            if (isDown) {
                                if (ledString.length() <= 3) {
                                    setLEDs(ledString);
                                } else {
                                    sendStringAsync(ledString, 0.333);
                                }
                            }
                        }   // for (int bit = 0; bit < 8; bit++)
                    }
                    break;
                }
                default: {
                    break;
                }
            }   // switch (index)
        }
    }
    //initialize logging
    private transient final static Logger log
            = LoggerFactory.getLogger(RailDriverMenuItem.class);
}

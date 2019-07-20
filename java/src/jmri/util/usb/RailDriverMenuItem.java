package jmri.util.usb;

import static java.lang.Float.NaN;

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
import jmri.*;
import jmri.implementation.AbstractShutDownTask;
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
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class RailDriverMenuItem extends JMenuItem
        implements HidServicesListener, PropertyChangeListener {

    private static final short VENDOR_ID = 0x05F3;
    private static final short PRODUCT_ID = 0x00D2;
    public static final String SERIAL_NUMBER = null;

    private HidServices hidServices = null;

    private HidDevice hidDevice = null;

    public RailDriverMenuItem(String name) {
        this();
        setText(name);
    }

    public RailDriverMenuItem() {

        super();

        // TODO: remove "(built in)" if/when this replaces Raildriver script
        setText(Bundle.getMessage("RdBuiltIn"));

        addPropertyChangeListener(this);

        addActionListener((ActionEvent e) -> {
            // menu item selected
            log.info("RailDriverMenuItem Action!");

            setupHidServices();

            // Open the device device by Vendor ID, Product ID and serial number
            HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
            if (hidDevice != null) {
                log.info("Got RailDriver hidDevice: {}", hidDevice);
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

    protected void setupHidServices() {
        try {
            HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
            hidServicesSpecification.setAutoShutdown(true);
            hidServicesSpecification.setScanInterval(500);
            hidServicesSpecification.setPauseInterval(5000);
            hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

            // Get HID services using custom specification
            hidServices = HidManager.getHidServices(hidServicesSpecification);
            hidServices.addHidServicesListener(RailDriverMenuItem.this);

            // do the services have to be started here?
            // They currently wait for the action to be triggered
            // so that they're not starting at ctor time, e.g. in tests
            // Provide a list of attached devices
            //log.info("Enumerating attached devices...");
            //for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            //    log.info(hidDevice.toString());
            //}
            //
            if (!invokeOnMenuOnly) {
                // start the HID services
                InstanceManager.getDefault(ShutDownManager.class)
                        .register(new AbstractShutDownTask("RailDriverMenuItem shutdown HID") {
                            // if we're going to start, we have to also stop
                            @Override
                            public boolean execute() {
                                hidServices.stop();
                                return true;
                            }
                        });
                log.debug("Starting HID services.");
                hidServices.start();

                // Open the device device by Vendor ID, Product ID and serial number
                HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
                if (hidDevice != null) {
                    log.info("Got RailDriver hidDevice: {}", hidDevice);
                    // Consider overriding dropReportIdZero on Windows
                    // if you see "The parameter is incorrect"
                    // HidApi.dropReportIdZero = true;
                    setupRailDriver(hidDevice);
                }
            }
        } catch (HidException ex) {
            log.error("HidException: {}", ex);
        }
    }

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
                        if (!hidDevice.isOpen()) {
                            hidDevice.open();
                        }
                        byte[] buff_new = new byte[14];	// read buffer
                        int ret = hidDevice.read(buff_new);
                        if (ret >= 0) {
                            //log.debug("hidDevice.read: {}", buff_new);
                            for (int i = 0; i < buff_new.length; i++) {
                                if (buff_old[i] != buff_new[i]) {
                                    if (i < 7) {    // analog values
                                        // convert to unsigned int
                                        int vInt = 0xFF & buff_new[i];
                                        // convert to double (0.0 thru 1.0)
                                        double vDouble = (256 - vInt) / 256.D;
                                        if (i == 1) {   // throttle
                                            // convert to float (-1.0 thru +1.0)
                                            vDouble = (2.D * vDouble) - 1.D;
                                        }
                                        String name = String.format("Axis %d", i);
                                        log.info("firePropertyChange(\"Value\", {}, {})", name, vDouble);
                                        firePropertyChange("Value", name, Double.toString(vDouble));
                                    } else {        // digital values
                                        byte xor = (byte) (buff_old[i] ^ buff_new[i]);
                                        for (int bit = 0; bit < 8; bit++) {
                                            byte mask = (byte) (1 << bit);
                                            if (mask == (mask & xor)) {
                                                int n = (8 * (i - 7)) + bit;
                                                String name = String.format("%d", n);
                                                boolean down = (mask == (buff_new[i] & mask));
                                                log.info("firePropertyChange(\"Value\", {}, {})", name, down ? "1" : "0");
                                                firePropertyChange("Value", name, down ? "1" : "0");
                                            }
                                        }
                                    }
                                    buff_old[i] = buff_new[i];
                                }
                            }
                        } else {
                            String error = hidDevice.getLastErrorMessage();
                            if (error != null) {
                                log.error("hidDevice.read error: {}", error);
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
                            StringBuilder s = new StringBuilder();
                            for (int i = 0; i < 3; i++) {
                                char ci = (char) (c + i);
                                ci = (char) (((ci - 'A') % 26) + 'A');
                                s.append(ci);
                                if (0 == ci % 3) {
                                    s.append('.');
                                }
                            }
                            setLEDs(s.toString());
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
            StringBuilder ledstring = new StringBuilder();
            int maxJ = 3;
            for (int j = 0; j < maxJ; j++) {
                if (i + j < string.length()) {
                    char c = string.charAt(i + j);
                    ledstring.append(c);
                    if (c == '.') {
                        maxJ++;
                    }
                } else {
                    break;
                }
            }
            setLEDs(ledstring.toString());
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
            int ret = hidDevice.write(message, message.length, reportID);
            if (ret >= 0) {
                log.debug("hidDevice.write returned: {}", ret);
            } else {
                log.error("hidDevice.write error: {}", hidDevice.getLastErrorMessage());
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
            //log.info("propertyChange \"Value\" old: {}, new: {}", oldValue, newValue);

            double value;
            try {
                value = Double.parseDouble(newValue);
            } catch (NumberFormatException ex) {
                log.error("RailDriver parse property new value ('{}')", newValue, ex);
                return;
            }

            if (oldValue.equals("Axis 0")) {
                // REVERSER is the state of the reverser lever, values greater
                // than 0.5 are forward, values near to 0.5 are neutral and
                // values (much) less than 0.5 are reverse.
                log.info("REVERSER value: {}", value);
                if ((controlPanel != null) && controlPanel.isEnabled()) {
                    if (value < 0.45) {
                        controlPanel.setForwardDirection(false);
                    } else if (value > 0.55) {
                        controlPanel.setForwardDirection(true);
                    }
                }
            } else if (oldValue.equals("Axis 1")) {
                // THROTTLE is the state of the Throttle (and dynamic brake).  Values
                // (much) greater than 0.0 are for throttle (maximum throttle is
                // values close to 1.0), values near 0.0 are at the center position
                // (idle/coasting), and values (much) less than 0.0 are for dynamic
                // braking, with values aproaching -1.0 for full dynamic braking.
                log.info("THROTTLE value: {}", value);

                if (controlPanel != null) {
                    JSlider slider = controlPanel.getSpeedSlider();
                    if ((slider != null) && slider.isEnabled()) {
                        // lever front is negative, back is positive
                        // limit range to only positive side of lever
                        double throttle_min = 0.125D;
                        double throttle_max = 0.7D;
                        double v = MathUtil.pin(value, throttle_min, throttle_max);
                        // compute fraction (0.0 to 1.0)
                        double fraction = (v - throttle_min) / (throttle_max - throttle_min);
                        // convert fraction to slider setting
                        int setting = (int) (fraction * (slider.getMaximum() - slider.getMinimum()));
                        slider.setValue(setting);

                        if (value < 0) {
                            //TODO: dynamic braking
                            setLEDs("DBr");
                        } else {
                            String speed = String.format("%03d", setting);
                            //log.info("speed: " + speed);
                            setLEDs(speed);
                        }
                    }
                }
            } else if (oldValue.equals("Axis 2")) {
                // AUTOBRAKE is the state of the Automatic (trainline) brake.  Large
                // values for no braking, small values for more braking.
                log.info("AUTOBRAKE value: {}", value);
            } else if (oldValue.equals("Axis 3")) {
                // INDEPENDBRK is the state of the Independent (engine only) brake.
                // Like the Automatic brake: large values for no braking, small
                // values for more braking.
                log.info("INDEPENDBRK value: {}", value);
            } else if (oldValue.equals("Axis 4")) {
                // BAILOFF is the Independent brake 'bailoff', this is the spring
                // loaded right movement of the Independent brake lever.  Larger
                // values mean the lever has been shifted right.
                log.info("BAILOFF value: {}", value);
            } else if (oldValue.equals("Axis 5")) {
                // HEADLIGHT is the state of the headlight switch.  A value below 0.5
                // is off, a value near 0.5 is dim, and a number much larger than 0.5
                // is full. This is an analog input w/detents, not a switch!
                log.info("HEADLIGHT value: {}", value);
            } else if (oldValue.equals("Axis 6")) {
                // WIPER is the state of the wiper switch.  Much like the headlight
                // switch, this is also an analog input w/detents, not a switch!
                // Small values (much less than 0.5) are off, values near 0.5 are
                // slow, and larger values are full.
                log.info("WIPER value: " + value);
            } else {
                log.info("FUNCTION {} value: {}", oldValue, value);
                if (functionPanel != null) {
                    FunctionButton[] functionButtons = functionPanel.getFunctionButtons();
                    boolean isDown = (value > 0.5D);

                    int fNum = -1;
                    try {
                        fNum = Integer.parseInt(oldValue);
                    } catch (NumberFormatException ex) {
                        //log.error("RailDriver parse property new value ('{}') exception: {}", newValue, ex);
                        return;
                    }

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
                }   // if (functionPanel != null)
            } // if (oldValue.equals(...) {} else...
        }   // if event.getPropertyName().equals("Value")
    }   // propertyChange

    //initialize logging
    private transient final static Logger log = LoggerFactory.getLogger(RailDriverMenuItem.class);

}

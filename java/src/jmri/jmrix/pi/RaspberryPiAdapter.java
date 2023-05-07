package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import javax.annotation.CheckForNull;

import jmri.jmrix.pi.simulator.GpioControllerSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * RaspberryPi managers to be handled.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender Copyright (C) 2015
 */
public class RaspberryPiAdapter extends jmri.jmrix.AbstractPortController {

    private static boolean _isSimulator = false;

    // in theory gpio can be static, because there will only ever
    // be one, but the library handles the details that make it a
    // singleton.
    private GpioController gpio = null;

    public RaspberryPiAdapter() {
        this(false);
    }

    public RaspberryPiAdapter(boolean isSimulator) {
        super(new RaspberryPiSystemConnectionMemo());
        log.debug("RaspberryPi GPIO Adapter Constructor called");
        setIsSimulator(isSimulator);
        this.manufacturerName = RaspberryPiConnectionTypeList.PI;
        try {
            if (!isSimulator) {
                gpio = GpioFactory.getInstance();
            } else {
                gpio = new GpioControllerSimulator();
            }
            opened = true;
        } catch (UnsatisfiedLinkError er) {
            log.error("Expected to run on Raspberry PI, but does not appear to be.");
        }
    }

    public static boolean isSimulator() {
        return _isSimulator;
    }

    private static void setIsSimulator(boolean isSimulator) {
        _isSimulator = isSimulator;
    }

    @Override
    public String getCurrentPortName() {
        return "GPIO";
    }

    @Override
    public void dispose() {
        super.dispose();
        gpio.shutdown(); // terminate all GPIO connections.
    }

    @Override
    public void connect() {
    }

    @Override
    public void configure() {
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public java.io.DataInputStream getInputStream() {
        return null;
    }

    @Override
    public java.io.DataOutputStream getOutputStream() {
        return null;
    }

    @Override
    public RaspberryPiSystemConnectionMemo getSystemConnectionMemo() {
        return (RaspberryPiSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void recover() {
    }

    /*
    * Get the GPIO Controller associated with this object.
    *
    * @return the associaed GPIO Controller or null if none exists
     */
    @CheckForNull
    public GpioController getGPIOController() {
        return gpio;
    }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiAdapter.class);

}

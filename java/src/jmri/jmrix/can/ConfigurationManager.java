package jmri.jmrix.can;

import java.util.Arrays;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does configuration for various CAN-based communications implementations.
 * <p>
 * TODO It would be good to replace this with properties-based method for redirecting
 * to classes in particular subpackages.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
abstract public class ConfigurationManager {

    final public static String SPROGCBUS = "SPROG CBUS";
    final public static String MERGCBUS = "MERG CBUS";
    final public static String OPENLCB = "OpenLCB";
    final public static String RAWCAN = "Raw CAN"; // TODO I18N
    final public static String TEST = "Test - do not use";

    public enum SubProtocol {
        NONE,
        CBUS
    }
    
    /**
     * Enumerate support for switching programming modes in connected hardware
     */
    public enum ProgModeSwitch {
        NONE,       // No support for switching programming modes, or no programmer,
                    // or unknown CBUS attached command station/programmer
        EITHER,     // Service mode or ops mode, but not both at the same time
        SPROG3PLUS  // Specific hardware choice
    }
    
    private static String[] options = new String[]{SPROGCBUS, MERGCBUS, OPENLCB, RAWCAN, TEST};

    /**
     * Create a new ConfigurationManager
     * @param memo System Connection
     */
    public ConfigurationManager(CanSystemConnectionMemo memo) {
        adapterMemo = memo;
    }
    
    /**
     * Provide the current set of "Option1" values
     * @return Copy of System Options Array
     */
    static public String[] getSystemOptions() {
        return Arrays.copyOf(options, options.length);
    }

    /**
     * Set the list of protocols to start with OpenLCB.
     */
    static public void setOpenLCB() {
        log.debug("setOpenLCB");
        options = new String[]{OPENLCB, MERGCBUS, RAWCAN, TEST};
    }

    /**
     * Set the list of protocols to start with MERG.
     */
    static public void setMERG() {
        log.debug("setMERG");
        options = new String[]{MERGCBUS, OPENLCB, RAWCAN, TEST};
    }

    /**
     * Set the list of protocols to start with SPROG.
     */
    static public void setSPROG() {
        log.debug("setSPROG");
        options = new String[]{SPROGCBUS};
    }

    protected CanSystemConnectionMemo adapterMemo;

    abstract public void configureManagers();

    /**
     * Get which managers this class provides.
     * @param type class to query.
     * @return true if provided, else false.
     */
    abstract public boolean provides(Class<?> type);

    abstract public <T> T get(Class<?> T);

    /**
     * Dispose of the ConfigurationManager
     */
    abstract public void dispose();

    abstract protected ResourceBundle getActionModelResourceBundle();

    private final static Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

}

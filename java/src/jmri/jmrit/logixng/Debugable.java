package jmri.jmrit.logixng;

/**
 * A LogixNG item that is debugable.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface Debugable extends Base {

    /**
     * Set the debug configuration for this male socket.
     * <P>
     * Each implementation of MaleSocket has their own implementation of
     * DebugConfig. Use reflection to get the proper class
     * &lt;package-name&gt;.debug.&lt;ClassName&gt;Debug that returns a JPanel
     * that can configure debugging for this male socket.
     * 
     * @param config the new configuration or null to turn off debugging
     */
    public void setDebugConfig(DebugConfig config);

    /**
     * Get the debug configuration for this male socket.
     * 
     * @return the configuration or null if debugging is turned off for this male socket
     */
    public DebugConfig getDebugConfig();

    /**
     * Create a debug configuration for this male socket.
     * 
     * @return the new configuration
     */
    public DebugConfig createDebugConfig();

    /**
     * Debug configuration for this male socket.
     * <P>
     * In some cases, it may be desirable to be able to execute the LogixNG
     * without fully working agains the layout. For example, when developing
     * a LogixNG for a club layout, it may be desirable to be able to run the
     * LogixNG without affecting turnouts on the layout while testing the
     * LogixNG.
     */
    public interface DebugConfig {
    }
    
}

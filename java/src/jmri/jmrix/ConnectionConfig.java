// ConnectionConfig.java
package jmri.jmrix;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 * @see JmrixConfigPane
 * @see AbstractSerialPortController
 */
public interface ConnectionConfig {

    public String name();

    public void loadDetails(JPanel details);

    public String getInfo();

    public PortAdapter getAdapter();

    public String getConnectionName();

    public String getManufacturer();

    public void setManufacturer(String Manufacturer);

    /**
     * Register the ConnectionConfig with the running JMRI process.
     */
    public void register();
    
    public void dispose();

    public boolean getDisabled();

    public void setDisabled(boolean disabled);

    /**
     * Determine if configuration needs to be written to disk.
     *
     * @return true if configuration needs to be saved, false otherwise
     */
    public boolean isDirty();

    /**
     * Determine if application needs to be restarted for configuration changes
     * to be applied.
     *
     * @return true if application needs to restart, false otherwise
     */
    public boolean isRestartRequired();
}

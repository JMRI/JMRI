// ConnectionConfig.java

package jmri.jmrix;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.8 $
 * @see JmrixConfigPane
 * @see AbstractSerialPortController
 */
public interface ConnectionConfig  {

    public String name();
    public void loadDetails(JPanel details);
    public String getInfo();
    
    public String getConnectionName();
    public String getManufacturer();
    public void setManufacturer(String Manufacturer);
    public void dispose();
    
}


// ConnectionTypeList.java
package jmri.jmrix;

/**
 * Definition of objects to handle configuring a layout connection.
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 * @see JmrixConfigPane
 * @see AbstractPortController
 * @see java.util.ServiceLoader
 */
public interface ConnectionTypeList {

    public String[] getAvailableProtocolClasses();

    public String[] getManufacturers();
}

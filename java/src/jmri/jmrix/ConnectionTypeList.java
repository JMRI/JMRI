package jmri.jmrix;

import jmri.spi.JmriServiceProviderInterface;

/**
 * Definition of objects to handle configuring a layout connection.
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @see JmrixConfigPane
 * @see AbstractPortController
 * @see java.util.ServiceLoader
 */
public interface ConnectionTypeList extends JmriServiceProviderInterface {

    public String[] getAvailableProtocolClasses();

    public String[] getManufacturers();
}

package jmri.jmrix;

import javax.annotation.Nonnull;
import jmri.spi.JmriServiceProviderInterface;

/**
 * Definition of objects to handle configuring a layout connection.
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of External System Connections page</a>.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @see JmrixConfigPane
 * @see ConnectionConfig
 * @see java.util.ServiceLoader
 */
public interface ConnectionTypeList extends JmriServiceProviderInterface {

    /**
     * Get a list of classes that can configure a layout connection for the
     * manufacturers specified in {@link #getManufacturers() }.
     *
     * @return an Array of classes or an empty Array if none
     */
    @Nonnull
    public String[] getAvailableProtocolClasses();

    /**
     * Get a list of manufacturer names supported by the classes specified in
     * {@link #getAvailableProtocolClasses() }.
     *
     * @return an Array of manufacturers or an empty Array if none
     */
    @Nonnull
    public String[] getManufacturers();
}

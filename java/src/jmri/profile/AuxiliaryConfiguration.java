package jmri.profile;

import org.w3c.dom.Element;

/**
 * JMRI local copy of the NetBeans Platform
 * org.netbeans.spi.project.AuxiliaryConfiguration.
 *
 * @author rhwood
 */
public interface AuxiliaryConfiguration {

    Element getConfigurationFragment(String elementName, String namespace, boolean shared);

    void putConfigurationFragment(Element fragment, boolean shared);

    boolean removeConfigurationFragment(String elementName, String namespace, boolean shared);

}

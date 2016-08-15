package jmri.profile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.w3c.dom.Element;

/**
 * JMRI local copy of the NetBeans Platform
 * org.netbeans.spi.project.AuxiliaryConfiguration.
 * <p>
 * <strong>Note</strong> This uses {@link org.w3c.dom.Element} instead of
 * {@link org.jdom2.Element} because the NetBeans Platform uses the DOM model
 * included in the JRE.
 *
 * @author Randall Wood
 */
public interface AuxiliaryConfiguration {

    /**
     * Get a configuration fragment as an XML element.
     * <p>
     * Multiple elements in a configuration file can have the same name as long
     * as they have different namespaces. If using versioned namespaces, this
     * would allow two different versions of JMRI with incompatible preferences
     * for a given element to use the same name without stomping on each other.
     * <p>
     * <strong>Note:</strong> Use
     * {@link jmri.util.jdom.JDOMUtil#toJDOMElement(org.w3c.dom.Element)} to
     * convert a non-null result to a JDOM {@link org.jdom2.Element}.
     *
     * @param elementName the name of the element.
     * @param namespace   the namespace of the element.
     * @param shared      true if the fragment is for all computers using this
     *                    profile, false if the fragment is for just the current
     *                    computer.
     * @return the matching Element or null if a matching element cannot be
     *         found.
     */
    @CheckForNull
    Element getConfigurationFragment(@Nonnull String elementName, @Nonnull String namespace, boolean shared);

    /**
     * Store a configuration fragement as an XML element.
     * <p>
     * Multiple elements in a configuration file can have the same name as long
     * as they have different namespaces. If using versioned namespaces, this
     * would allow two different versions of JMRI with incompatible preferences
     * for a given element to use the same name without stomping on each other.
     * <p>
     * <strong>Note:</strong> Use
     * {@link jmri.util.jdom.JDOMUtil#toW3CElement(org.jdom2.Element)} to
     * convert a JDOM {@link org.jdom2.Element} to a W3C
     * {@link org.w3c.dom.Element}. The JDOM element must have a namespace
     * associated with it.
     *
     * @param fragment the XML element. It must have a valid namespace property.
     * @param shared   true if the fragment is for all computers using this
     *                 profile, false if the fragment is for just the current
     *                 computer.
     */
    void putConfigurationFragment(@Nonnull Element fragment, boolean shared);

    /**
     * Remove a configuration fragment from the configuration.
     * <p>
     * Multiple elements in a configuration file can have the same name as long
     * as they have different namespaces. If using versioned namespaces, this
     * would allow two different versions of JMRI with incompatible preferences
     * for a given element to use the same name without stomping on each other.
     *
     * @param elementName the name of the element.
     * @param namespace   the namespace of the element.
     * @param shared      true if the fragment is for all computers using this
     *                    profile, false if the fragment is for just the current
     *                    computer.
     * @return true if the fragment could be removed, false otherwise.
     */
    boolean removeConfigurationFragment(String elementName, String namespace, boolean shared);

}

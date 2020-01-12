/**
 * The jmrix.internal package contains a JMRI connection implementation
 * for use without a layout.  There's no physical equipment connected to these
 * classes, which is why we call them "internal". 
 *
 *        <p>
 *
 * As a special case, a limited version of this 
 * connection type is automatically created by the
 * 
 * {@link jmri.InstanceManager InstanceManager} via the 
 * {@link jmri.InstanceManagerAutoDefault InstanceManagerAutoDefault}
 * mechanism for use by 
 * {@link jmri.managers.AbstractProxyManager ProxyManagers}. 
 * This is done because, historically,
 * users could create IT Turnouts, IS Sensors, etc
 * without configuring a specific system. 
 *
 * <h2>Related Documentation</h2>
 *
 * For overviews, tutorials, examples, guides, and tool documentation, please
 * see:
 * <ul>
 * <li><a href="http://jmri.org/">JMRI project overview page</a>
 * <li><a href="http://jmri.org/help/en/html/doc/Technical/index.shtml">JMRI
 * project technical info</a>
 * </ul>
 *
 * <!-- Put @see and @since tags down here. -->
 *
 * @see jmri.managers
 * @see jmri.implementation
 */
package jmri.jmrix.internal;

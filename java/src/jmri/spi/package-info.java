/**
 * Interfaces that can be implemented outside the JMRI.jar file to implement new
 * behavior in JMRI.
 *
 * Implementing classes need to be included in the containing JAR's
 * META-INF.services directory per the Java Service Provider Interface (SPI)
 * standards.
 *
 * The {@link jmri.spi.JmriServiceProviderInterface} interface serves to
 * identify the semantics of being a JMRI-specific SPI.
 *
 * Within the JMRI code, it is possible to use
 * {@code @org.openide.util.lookup.ServiceProvider(service = INTERFACE)} just
 * before the class declaration to have the class automatically included in the
 * JAR's META-INF.services directory, where INTERFACE is the interface to be
 * implemented.
 *
 * @see java.util.ServiceLoader
 */
package jmri.spi;

/**
 * Interfaces that can be implemented outside the JMRI.jar file to implement new
 * behavior in JMRI.
 *
 * Implementing classes need to be included in the containing JAR's
 * META-INF.services directory per the Java Service Provider Interface (SPI)
 * standards.
 *
 * The {@link JmriServiceProviderInterface} interface serves to identify the
 * semantics of being a JMRI-specific SPI.
 *
 * @see java.util.ServiceLoader
 */
package jmri.spi;

package jmri.spi;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Implementing or extending interfaces are available as Java Service Provider
 * Interfaces (SPI) within JMRI applications.
 *
 * This interface has no methods or fields and serves only to identify the
 * semantics of being a JMRI-specific SPI.
 *
 * @author Randall Wood (C) 2016
 */
@API(status = EXPERIMENTAL)
public interface JmriServiceProviderInterface {

}

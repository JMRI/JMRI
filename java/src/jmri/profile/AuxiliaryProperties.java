package jmri.profile;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * JMRI local copy of the NetBeans Platform
 * org.netbeans.spi.project.AuxiliaryProperties.
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public interface AuxiliaryProperties {

    String get(String key, boolean shared);

    Iterable<String> listKeys(boolean shared);

    void put(String key, String value, boolean shared);
}

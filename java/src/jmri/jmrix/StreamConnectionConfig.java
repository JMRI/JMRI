package jmri.jmrix;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface for objects that handle configuring a layout connection.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of External System Connections page</a>.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @see JmrixConfigPane
 * @see PortAdapter
 */
@API(status = EXPERIMENTAL)
public interface StreamConnectionConfig extends ConnectionConfig {
}

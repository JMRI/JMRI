package jmri.jmris.json;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Randall Wood
 * @deprecated since 4.19.4; use {@link jmri.server.json.JsonServerPreferences}
 *             instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated for replacement.")
@API(status = EXPERIMENTAL)
public class JsonServerPreferences extends jmri.server.json.JsonServerPreferences {
}

package jmri.server.json.idtag;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Constants used by the internal JMRI JSON IdTag service.
 *
 * @author Randall Wood (C) 2019
 */
@API(status = EXPERIMENTAL)
public class JsonIdTag {

    /**
     * {@value #IDTAG}
     */
    public static final String IDTAG = "idTag"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonIdTag() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

package jmri.server.json.memory;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Tokens used by the JSON service for Memory handling.
 *
 * @author Randall Wood (C) 2016
 */
@API(status = EXPERIMENTAL)
public class JsonMemory {

    public static final String MEMORY = "memory"; // NOI18N
    public static final String MEMORIES = "memories"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonMemory() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

package jmri.server.json.signalhead;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Constants used by the {@link jmri.server.json.signalhead} package.
 *
 * @author Randall Wood 2016
 */
@API(status = EXPERIMENTAL)
public class JsonSignalHead {

    /**
     * {@value #SIGNAL_HEAD}
     */
    public static final String SIGNAL_HEAD = "signalHead"; // NOI18N
    /**
     * {@value #SIGNAL_HEADS}
     */
    public static final String SIGNAL_HEADS = "signalHeads"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonSignalHead() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

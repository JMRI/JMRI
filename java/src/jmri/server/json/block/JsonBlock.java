package jmri.server.json.block;

/**
 * Constants used by the internal JMRI JSON Block service.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonBlock {

    /**
     * {@value #BLOCK}
     */
    public static final String BLOCK = "block"; // NOI18N
    /**
     * {@value #BLOCKS}
     */
    public static final String BLOCKS = "blocks"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonBlock() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}

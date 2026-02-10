package apps.jmrit.decoderdefn;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoderIndexBuilderTest {

    // no Constructor test, tested class only supplies static main method.

    @Test
    @Disabled("reconfigures logging for standalone operation")
    public void testDecoderIndexBuilderMain() {
        // Compilation will fail if class removed
        DecoderIndexBuilder.main(new String[]{});
    }

    private Thread.UncaughtExceptionHandler handler;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        handler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @AfterEach
    public void tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(handler);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderIndexBuilderTest.class);

}

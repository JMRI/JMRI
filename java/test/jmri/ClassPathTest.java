package jmri;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen
 */
public class ClassPathTest {

    /**
     * Announce classpath information into test log
     */
    @Test
    public void announceClasspath() {
        log.info("Tests running with java.class.path {}",
            System.getProperty("java.class.path")
        );
        log.info("Tests running with java.library.path {}",
            System.getProperty("java.library.path")
        );
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClassPathTest.class);
}
